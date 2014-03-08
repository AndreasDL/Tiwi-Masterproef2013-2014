package be.iminds.ilabt.jfed.ui.rspeceditor.editor;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import be.iminds.ilabt.jfed.ui.rspeceditor.util.ObjectPropertyBindHelper;
import be.iminds.ilabt.jfed.ui.rspeceditor.util.SelectedObjectPropertyCopier;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecNode;

public class RspecNodeEditPanel implements Initializable, ChangeListener<RspecNode> {
    private SelectedObjectPropertyCopier<RspecNode> selectedObjectPropertyCopier = new SelectedObjectPropertyCopier<RspecNode>();
    
    @FXML private TextField idField;
    
    public RspecNodeEditPanel() { }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedObjectPropertyCopier.getBinders().add(new ObjectPropertyBindHelper<RspecNode>(idField.textProperty()) {
            @Override public Property objectProperty(RspecNode t) { return t.idProperty(); }
        });
    }
    
    public void setSelection(RspecSelection rspecSelection) {
        selectedObjectPropertyCopier.setSelectedObjectProperty(rspecSelection.selectedRspecNodeProperty());
        rspecSelection.selectedRspecNodeProperty().addListener(this);
    }
    
    public void onCommitChanges() {
        selectedObjectPropertyCopier.copyToSelectedObject();
    }
    
    @Override
    public void changed(ObservableValue<? extends RspecNode> observable, RspecNode oldVal, RspecNode newVal) {
       selectedObjectPropertyCopier.copyFromObject(newVal);
    }
}
