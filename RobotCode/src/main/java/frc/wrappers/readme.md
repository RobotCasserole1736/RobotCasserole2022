# Sim Wrapper Usage

The purpose of simulation wrappers is to facilitate accurate-enough simulation of devices which are not well supported (yet) in WPILib's sim system.

In all cases:

* "Casserole*.java" classes are the ones to use in your code. They mirror the approprate API's, contain common code impelmentation for sim/real, and instantiate the correct sim/real class depending on whether we're on robot or not.
* "Real*.java" classes shoudl interact with real hardware, and only get used on the actual robot
* "Sim*.java" classes should simulate the real hardware's behavior, and only ever get used while running in simulation.
* "Abstract*.java" classes define the minimum API for each type of device that exists for both sim and real.