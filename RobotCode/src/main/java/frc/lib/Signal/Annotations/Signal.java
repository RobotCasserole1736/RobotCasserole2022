package frc.lib.Signal.Annotations;

import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Signal {

    String name() default "";

    String units() default "";

}
