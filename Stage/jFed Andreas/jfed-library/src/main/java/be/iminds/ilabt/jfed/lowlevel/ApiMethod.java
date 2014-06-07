package be.iminds.ilabt.jfed.lowlevel;

import java.lang.annotation.*;

/**
 * ApiMethod
 */
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.METHOD)
public @interface ApiMethod {
    String hint() default "";
    boolean unprotected() default false;
}
