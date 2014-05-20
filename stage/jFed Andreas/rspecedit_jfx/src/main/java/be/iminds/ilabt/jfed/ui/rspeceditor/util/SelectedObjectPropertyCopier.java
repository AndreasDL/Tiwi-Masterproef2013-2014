package be.iminds.ilabt.jfed.ui.rspeceditor.util;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.Property;

public class SelectedObjectPropertyCopier<T> {
    private List<ObjectPropertyBindHelper<T>> binders = new ArrayList<ObjectPropertyBindHelper<T>>();
    private Property<T> selectionProperty;    
    
    public void setSelectedObjectProperty(Property<T> selectionProperty) {
        this.selectionProperty = selectionProperty;
    }
    
    public List<ObjectPropertyBindHelper<T>> getBinders() {
        return binders;
    }
    
    public void copyToSelectedObject() {
        T selectedObject = selectionProperty.getValue();
        if (selectedObject == null) return;
        
        for (ObjectPropertyBindHelper b : binders) {
            b.objectProperty(selectedObject).setValue(b.getBoundProperty().getValue());
        }
        
    }
    public void copyFromSelectedObject() {
        T selectedObject = selectionProperty.getValue();
        copyFromObject(selectedObject);
    }
    public void copyFromObject(T t) {
        if (t == null) return;
        
        for (ObjectPropertyBindHelper b : binders) {
            //alternativly, we could bind and directly unbind again
            b.getBoundProperty().setValue(b.objectProperty(t).getValue());
        }
    }
}
