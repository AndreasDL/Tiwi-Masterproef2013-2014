package be.iminds.ilabt.jfed.ui.rspeceditor.model;

import javafx.beans.property.Property;

import java.lang.annotation.ElementType;
import java.util.List;

/**
 * GuiEditable annotation for Property fields
 */
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.FIELD})
public @interface GuiEditable {
    boolean editable() default true;
    boolean nullable() default false;
    Class clazz();
    Class listClass() default Object.class; //only needed when clazz is a Collection

    /** if guiName is not specified, name will be variable name without Property suffix (if any) */
    String guiName() default "";

    /** if guiHelp is not specified, the value for guiName will be used. */
    String guiHelp() default "";
}
