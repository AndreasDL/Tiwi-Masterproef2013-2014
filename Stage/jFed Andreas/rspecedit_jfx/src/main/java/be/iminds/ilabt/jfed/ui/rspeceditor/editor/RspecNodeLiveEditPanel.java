package be.iminds.ilabt.jfed.ui.rspeceditor.editor;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import be.iminds.ilabt.jfed.ui.rspeceditor.util.ObjectPropertyBindHelper;
import be.iminds.ilabt.jfed.ui.rspeceditor.util.SelectedObjectPropertyBinder;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecNode;

public class RspecNodeLiveEditPanel implements Initializable {
    private SelectedObjectPropertyBinder<RspecNode> selectedObjectPropertyBinder = new SelectedObjectPropertyBinder<RspecNode>(true);
    
    @FXML private TextField idField;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedObjectPropertyBinder.getBinders().add(new ObjectPropertyBindHelper<RspecNode>(idField.textProperty()) {
            @Override public Property objectProperty(RspecNode t) { return t.idProperty(); }
        });
    }
    
    public void setSelection(RspecSelection rspecSelection) {
        selectedObjectPropertyBinder.setSelectedObjectProperty(rspecSelection.selectedRspecNodeProperty());
    }
}
