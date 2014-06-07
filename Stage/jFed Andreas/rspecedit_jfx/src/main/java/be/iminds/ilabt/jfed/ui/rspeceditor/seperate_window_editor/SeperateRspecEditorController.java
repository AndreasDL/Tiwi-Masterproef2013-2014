package be.iminds.ilabt.jfed.ui.rspeceditor.seperate_window_editor;

import be.iminds.ilabt.jfed.ui.rspeceditor.editor.RspecEditorPanel;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.Rspec;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class SeperateRspecEditorController implements Initializable {
    @FXML private RspecEditorPanel rspecEditor;


    public SeperateRspecEditorController() {

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    SeperateRspecEditor seperateRspecEditor;
    public void setSeperateRspecEditor(SeperateRspecEditor seperateRspecEditor) {
        this.seperateRspecEditor = seperateRspecEditor;
    }

    public void finishEdit() {
        try {
            String rspecText = rspecEditor.shownRspecProperty().get().toGeni3RequestRspec();
            seperateRspecEditor.setResult(rspecText);
            seperateRspecEditor.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRspec(String inputRspec) {
        //TODO: this needs to take into account RSpec type
        rspecEditor.shownRspecProperty().set(Rspec.fromGeni3RequestRspecXML(inputRspec));
    }
}
