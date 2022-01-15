/*

SpinBox.js

Implements a spin box interface for a text field

Created by Kate Morley - http://code.iamkate.com/ - and released under the terms
of the CC0 1.0 Universal legal code:

http://creativecommons.org/publicdomain/zero/1.0/legalcode

*/

/* Creates a spin box. The parameter is:
 *
 * container - either the DOM node to contain the spin box or the ID of the node
 * options   - an object containing various parameters; this optional parameter
 *             defaults to the empty object
 *
 * The options object may contain the following keys:
 *
 * className - a class name to apply to the container; the class name with the
 *             suffixes 'Up' and 'Down' will be applied to the up and down
 *             buttons. The default value is 'spinBox'.
 * value     - the initial value for the spin box. The default is 0 (subject to
 *             restrictions on the minimum and maximum value) if the input
 *             element did not already exist, or the existing value if the input
 *             element did already exist.
 * step      - the value by which to increment or decrement the value. The
 *             default value is 1.
 * minimum   - the minimum allowed value. The default is not to have a minimum
 *             value.
 * maximum   - the maximum allowed value. The default is not to have a maximum
 *             value.
 * decimals  - the number of decimal places allowed. The default is 0.
 *
 * Note that the minimum, maximum, and decimal places restrictions are enforced
 * for values set by the spin box, but a value outside of these restrictions may
 * be typed by the user.
 */
export function SpinBox(container, options){

  // fetch the DOM node if a string was supplied
  if (typeof container == 'string'){
    container = document.getElementById(container);
  }

  // store the options and set the default values
  this.options = (options ? options : {});
  if (!('className' in this.options)) this.options.className = 'spinBox';
  if (!('step'      in this.options)) this.options.step      = 1;
  if (!('decimals'  in this.options)) this.options.decimals  = 0;

  // check whether the input field should be created
  var inputs = container.getElementsByTagName('input');
  if (inputs.length == 0){

    // create the input node
    this.input = document.createElement('input');
    this.setValue('value' in this.options ? this.options.value : 0);
    container.appendChild(this.input);

  }else{

    // store a reference to the input node
    this.input = inputs[0];
    this.setValue(this.options.value ? this.options.value : this.input.value);

  }

  // create the up button
  var upButton = document.createElement('span');
  upButton.appendChild(document.createElement('span'));
  container.appendChild(upButton);

  // create the down button
  var downButton = document.createElement('span');
  downButton.appendChild(document.createElement('span'));
  container.appendChild(downButton);

  // apply the classes
  container.className  += ' ' + this.options.className;
  upButton.className    = this.options.className + 'Up';
  downButton.className  = this.options.className + 'Down';

  // add the listeners
  this.addEventListener(
      this.input, 'mousewheel', this.handleMouseWheel, [], true);
  this.addEventListener(
      this.input, 'DOMMouseScroll', this.handleMouseWheel, [], true);
  this.addEventListener(this.input, 'keydown',   this.handleKeyDown,  [], true);
  this.addEventListener(this.input, 'keypress',  this.handleKeyPress, [], true);
  this.addEventListener(this.input, 'keyup',     this.stop);
  this.addEventListener(upButton,   'mousedown', this.start, [true]);
  this.addEventListener(upButton,   'mouseup',   this.stop);
  this.addEventListener(upButton,   'mouseout',  this.stop);
  this.addEventListener(downButton, 'mousedown', this.start, [false]);
  this.addEventListener(downButton, 'mouseup',   this.stop);
  this.addEventListener(downButton, 'mouseout',  this.stop);

}

/* Returns the current value. This will be a number, or the value NaN if the
 * current contents of the input field do not start with a valid number.
 */
SpinBox.prototype.getValue = function(){

  // parse and return the value
  return parseFloat(this.input.value);

}

/* Sets the value. Restrictions on the minimum and maximum value are enforced.
 * The parameter is:
 *
 * value - the value
 */
SpinBox.prototype.setValue = function(value){

  // ensure the value is within the permitted range
  if ('minimum' in this.options) value = Math.max(this.options.minimum, value);
  if ('maximum' in this.options) value = Math.min(this.options.maximum, value);

  // store the sign
  var sign = (value < 0 ? '-' : '');
  value = Math.abs(value);

  // determine the multiplier for rounding
  var multiplier = Math.pow(10, this.options.decimals);

  // split the value in to integer and fractional parts
  value = Math.round(value * multiplier);
  var integer    = (value - value % multiplier) / multiplier;
  var fractional = '' + value % multiplier;

  // add leading zeros to the fractional part
  while (fractional.length < this.options.decimals){
    fractional = '0' + fractional;
  }

  // set the value
  this.input.value =
      sign + integer + (this.options.decimals > 0 ? '.' + fractional : '');

  // check whether the browser can dispatch events
  if ('dispatchEvent' in this.input){

    // create the event
    try{
      var event = new Event('change', {bubbles : true, cancelable : true});
    }catch (e){
      var event = document.createEvent('Event');
      event.initEvent('change', true, true);
    }

    // dispatch the event
    this.input.dispatchEvent(event);

  }

}

/* Adds an event listener to a node. The event listener is bound to the current
 * value of 'this'. The parameters are:
 *
 * node         - the node
 * event        - the event name
 * listener     - the listener functions
 * parameters   - an array of additional parameters to pass to the listener;
 *                these are placed after the event parameter
 * allowDefault - true if the default action should not be prevented
 */
SpinBox.prototype.addEventListener = function(
    node, event, listener, parameters, allowDefault){

  // store a reference to the 'this' object
  var thisObject = this;

  // create the bound listener
  function boundListener(e){

    // get the event if it is not supplied
    if (!e) e = window.event;

    // call the listener
    listener.apply(thisObject, [e].concat(parameters));

    // prevent the default action if necessary
    if (!allowDefault){
      if (e.preventDefault){
        e.preventDefault();
      }else{
        e.returnValue = false;
      }
    }

  }

  // add the event listener
  if (node.addEventListener){
    node.addEventListener(event, boundListener, false);
  }else{
    node.attachEvent('on' + event, boundListener);
  }

}

/* Handles a mouse wheel event by updating the value if the field is active. The
 * parameter is:
 *
 * e - the event object
 */
SpinBox.prototype.handleMouseWheel = function(e){

  // check whether the field is active
  if (document.activeElement == this.input){

    // update the value
    if (e.wheelDelta){
      this.start(e, e.wheelDelta > 1);
    }else if (e.detail){
      this.start(e, e.detail < 1);
    }
    this.stop();

    // prevent the default action
    if (e.preventDefault){
      e.preventDefault();
    }else{
      e.returnValue = false;
    }

  }

}

/* Handles a key down event by starting updating if appropriate. The parameter
 * is:
 *
 * e - the event object
 */
SpinBox.prototype.handleKeyDown = function(e){

  // if the up or down keys were pressed, start updating
  if (e.keyCode == 38) this.start(e, true);
  if (e.keyCode == 40) this.start(e, false);

}

/* Handles a key press event by filtering out invalid characters. The parameter
 * is:
 *
 * e - the event object
 */
SpinBox.prototype.handleKeyPress = function(e){

  // determine the character code
  var charCode = ('charCode' in e ? e.charCode : e.keyCode);

  // allow special key presses
  if (charCode == 0 || e.altKey || e.ctrlKey || e.metaKey) return;

  // allow a minus sign if the value can be negative
  if (charCode == 45
      && (!('minimum' in this.options) || this.options.minimum < 0)){
    return;
  }

  // allow a decimal point if the value may contain decimals
  if (charCode == 46 && this.options.decimals > 0) return;

  // allow digits
  if (charCode >= 48 && charCode <= 57) return;

  // prevent the default action
  if (e.preventDefault){
    e.preventDefault();
  }else{
    e.returnValue = false;
  }

}

/* Starts updating the value. The parameters are:
 *
 * e  - the event object
 * up - true to increment the value, false to decrement the value
 */
SpinBox.prototype.start = function(e, up){

  // if the field is disabled or we are already updating, return immediately
  if (this.input.disabled || 'timeout' in this) return;

  // set the update step
  this.updateStep = (up ? this.options.step : -this.options.step);

  // initialise the timeout delay
  this.timeoutDelay = 500;

  // update the value
  this.update();

}

// Stops update the value.
SpinBox.prototype.stop = function(){

  // clear the timeout if it exists
  if ('timeout' in this){
    window.clearTimeout(this.timeout);
    delete this.timeout;
  }

}

// Updates the value.
SpinBox.prototype.update = function(){

  // determine the current value
  var value = parseFloat(this.input.value);
  if (isNaN(value)) value = 0;

  // update the value
  this.setValue(value + this.updateStep);

  // reduce the delay
  this.timeoutDelay = Math.max(20, Math.floor(this.timeoutDelay * 0.9));

  // call this function again
  var thisObject = this;
  this.timeout =
      window.setTimeout(function(){ thisObject.update(); }, this.timeoutDelay);

}
