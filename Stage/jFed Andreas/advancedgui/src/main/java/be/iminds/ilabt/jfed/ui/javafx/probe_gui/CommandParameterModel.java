package be.iminds.ilabt.jfed.ui.javafx.probe_gui;

import be.iminds.ilabt.jfed.lowlevel.ApiMethodParameter;
import javafx.beans.property.*;
import javafx.util.StringConverter;

/**
 * CommandParameterModel
 */
public class CommandParameterModel<T> {
    protected final Class parameterClass;
    protected final ApiMethodParameter annotation;
    protected final ProbeController.MethodInfo methodInfo;

    private final boolean required;
    private final BooleanProperty included = new SimpleBooleanProperty(true);
    private final StringProperty parameterName = new SimpleStringProperty();
    private final ObjectProperty<T> value = new SimpleObjectProperty(null);

    public CommandParameterModel(String parameterName, Class<T> parameterClass, ApiMethodParameter annotation, ProbeController.MethodInfo methodInfo) {
        assert parameterName != null;
        assert parameterClass != null;
        assert annotation != null;
        assert methodInfo != null;
        this.parameterName.set(parameterName);
        this.parameterClass = parameterClass;
        this.annotation = annotation;
        this.methodInfo = methodInfo;

        required = annotation.required();

        if (annotation.guiDefault() != null) {
            if (parameterClass.equals(String.class))
                value.set((T) annotation.guiDefault());
            if (parameterClass.equals(Integer.class))
                value.set((T) new Integer(Integer.parseInt(annotation.guiDefault())));
            if (parameterClass.equals(Double.class))
                value.set((T) new Double(Double.parseDouble(annotation.guiDefault())));
            if (parameterClass.equals(Long.class))
                value.set((T) new Long(Long.parseLong(annotation.guiDefault())));
        }

        if (!required)
            included.set(annotation.guiDefaultOptional());
    }

    public BooleanProperty includedProperty() {
        return included;
    }

    public StringProperty nameProperty() {
        return parameterName;
    }

    public ObjectProperty valueProperty() {
        return value;
    }

    public String getParameterName() {
        return parameterName.get();
    }

    public Class getParameterClass() {
        return parameterClass;
    }

    public ApiMethodParameter getAnnotation() {
        return annotation;
    }

    public boolean isRequired() {
        return required;
    }

    public ProbeController.MethodInfo getMethodInfo() {
        return methodInfo;
    }
}
