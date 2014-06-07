package be.iminds.ilabt.jfed.lowlevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ApiMethodParameter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ApiMethodParameter {
    boolean required() default true;
    String name();
    String hint() default "";
    boolean multiLineString() default false;

    /** Default value to show in Gui. guiDefault is always in string format, even for boolean or int defaults */
    String guiDefault() default "";

    /** Should this optional option be included by default in the gui? (ignored for required options)*/
    boolean guiDefaultOptional() default false;

    /** explicitly state a type of argument, for tools using it. This is most usefull in cases where an argument has Type parameters (because reflection cannot see them).
     * This is also useful if for example, a Strings argument must actually be a valid slice urn.  */
    ApiMethodParameterType parameterType() default ApiMethodParameterType.NOT_SPECIFIED;
}
