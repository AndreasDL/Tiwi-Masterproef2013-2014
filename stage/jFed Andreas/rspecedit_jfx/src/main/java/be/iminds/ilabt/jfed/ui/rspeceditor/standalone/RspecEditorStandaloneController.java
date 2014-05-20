package be.iminds.ilabt.jfed.ui.rspeceditor.standalone;

import be.iminds.ilabt.jfed.ui.rspeceditor.editor.RspecEditorPanel;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.Rspec;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

public class RspecEditorStandaloneController implements Initializable {
    @FXML private RspecEditorPanel rspecEditor;


    public RspecEditorStandaloneController() {

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @FXML private TextArea rspecText;
    public void rspecToEditor() {
        //TODO: this needs to take into account RSpec type
        Rspec rspec = Rspec.fromGeni3RequestRspecXML(rspecText.getText());
        rspecEditor.shownRspecProperty().set(rspec);
    }
    
    public void editorToRspec() {
        Rspec rspec = rspecEditor.shownRspecProperty().get();
        String rspecString = rspec.toGeni3RequestRspec();
        rspecText.setText(rspecString);
    }
}
