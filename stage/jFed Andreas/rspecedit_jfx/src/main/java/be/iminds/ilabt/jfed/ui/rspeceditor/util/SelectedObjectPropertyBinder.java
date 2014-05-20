package be.iminds.ilabt.jfed.ui.rspeceditor.util;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class SelectedObjectPropertyBinder<T> implements ChangeListener<T> {
    private List<ObjectPropertyBindHelper<T>> binders = new ArrayList<ObjectPropertyBindHelper<T>>();
    private boolean bidirectional;

    public SelectedObjectPropertyBinder(boolean bidirectional) {
        this.bidirectional = bidirectional;
    }

    public void setSelectedObjectProperty(Property<T> selectionProperty) {
        selectionProperty.addListener(this);
    }

    public List<ObjectPropertyBindHelper<T>> getBinders() {
        return binders;
    }

    @Override
    public void changed(ObservableValue<? extends T> observable, T oldVal, T newVal) {
        if (oldVal != null) {
            for (ObjectPropertyBindHelper b : binders) {
                if (bidirectional)
                    b.unbindBi(oldVal);
                else
                    b.unbind(oldVal);
            }
        }
        if (newVal != null) {
            for (ObjectPropertyBindHelper b : binders) {
                if (bidirectional)
                    b.bindBi(newVal);
                else
                    b.bind(newVal);
            }
        }
    }
}
