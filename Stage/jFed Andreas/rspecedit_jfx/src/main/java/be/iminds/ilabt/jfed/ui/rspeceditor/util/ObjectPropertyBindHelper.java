package be.iminds.ilabt.jfed.ui.rspeceditor.util;

import javafx.beans.binding.StringExpression;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public abstract class ObjectPropertyBindHelper<T> {
    private Property boundProperty;
    private Property isNotNullProperty;

    public ObjectPropertyBindHelper(Property boundProperty) {
        assert boundProperty != null;
        this.boundProperty = boundProperty;
    }
    public ObjectPropertyBindHelper(final Property boundProperty, final Property<Boolean> isNotNullProperty) {
        assert boundProperty != null;
        this.boundProperty = boundProperty;
        this.isNotNullProperty = isNotNullProperty;

        if (isNotNullProperty != null) {
            isNotNullProperty.setValue(boundProperty.getValue() != null);
            isNotNullProperty.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldVal, Boolean newVal) {
                    if (oldVal != newVal && !newVal)
                        boundProperty.setValue(null);
                }
            });

            if (boundProperty instanceof StringExpression) {
                //indirectly bind to bound object
                StringExpression se = (StringExpression) boundProperty;
                se.isNotNull().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldVal, Boolean newVal) {
                        if (oldVal != newVal)
                            isNotNullProperty.setValue(newVal);
                    }
                });
            }
        }
    }
    
    public void bind(T o) {
        boundProperty.bind(objectProperty(o));
    }
    public void unbind(T o) {
        boundProperty.unbind();// .unbind(objectProperty(o));
    }
    public void bindBi(T o) {
        assert boundProperty != null;
        Property oProp = objectProperty(o);
        assert oProp != null;
        if (isNotNullProperty != null) {
            if (oProp instanceof StringExpression) {
                StringExpression se = (StringExpression) oProp;
                boolean isNotNull = se.isNotNull().getValue();
                isNotNullProperty.setValue(isNotNull);

                //not needed (yet) and hard to unbind:
//                se.isNotNull().addListener(new ChangeListener<Boolean>() {
//                    @Override
//                    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
//                        isNotNullProperty.setValue(newValue);
//                    }
//                });
            }
        }
        boundProperty.bindBidirectional(oProp);
    }
    public void unbindBi(T o) {
        boundProperty.unbindBidirectional(objectProperty(o));
    }
    public abstract Property objectProperty(T t);
    public Property getBoundProperty() {
        return boundProperty;
    }
}
