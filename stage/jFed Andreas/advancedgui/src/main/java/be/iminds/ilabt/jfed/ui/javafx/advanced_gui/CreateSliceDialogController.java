package be.iminds.ilabt.jfed.ui.javafx.advanced_gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author wim
 */
public class CreateSliceDialogController implements Initializable {
    @FXML private TextField sliceNameField;
    private Stage dialog;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
    
    public void setDialog(Stage dialog) {
        this.dialog = dialog;
    }
    
    public void cancel() {
        sliceNameField.setText("");
        dialog.close();
    }
    
    public void create() {
        dialog.close();        
    }

    public String getSliceName() {
        if (sliceNameField.getText().equals("")) return null;
        return sliceNameField.getText();
    }
    
    public static String getDialogResult() {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
//        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        URL location = CreateSliceDialogController.class.getResource("CreateSliceDialog.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(location, null);
        Parent box;
        try {
            box = (Parent) fxmlLoader.load();
            Scene scene = new Scene(box);
            CreateSliceDialogController controller = (CreateSliceDialogController) fxmlLoader.getController();
            controller.setDialog(dialog);
            dialog.setScene(scene);
            dialog.sizeToScene(); 
            dialog.showAndWait();
            if (controller.getSliceName() != null)
                return controller.getSliceName();
            return null;
        } catch (IOException ex) {
            Logger.getLogger(JFedAdvancedGuiController.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
