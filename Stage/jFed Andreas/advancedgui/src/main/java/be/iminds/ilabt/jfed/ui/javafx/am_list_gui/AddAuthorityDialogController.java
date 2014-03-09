package be.iminds.ilabt.jfed.ui.javafx.am_list_gui;

import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.ui.javafx.advanced_gui.JFedAdvancedGuiController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FXML Controller class
 *
 * @author wim
 */
public class AddAuthorityDialogController implements Initializable {
    @FXML private BorderPane pane;
    @FXML private TextField authorityUrnField;
    private Stage dialog;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String validatorCss = getClass().getResource("validation.css").toExternalForm();
        pane.getStylesheets().add(validatorCss);

        authorityUrnField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s1) {
                validate();
            }
        });
    }

    public void setDialog(Stage dialog) {
        this.dialog = dialog;
    }

    public void cancel() {
        authorityUrnField.setText("");
        dialog.close();
    }

    public void validate() {
        authorityUrnField.getStyleClass().removeAll("validation-error", "validation-warning");
        try {
            SfaAuthority.urnPartFromUrn(authorityUrnField.getText());
        } catch (GeniException e) {
            authorityUrnField.getStyleClass().add("validation-error");
        }
    }

    public void create() {
        authorityUrnField.getStyleClass().removeAll("validation-error", "validation-warning");
        try {
            SfaAuthority.urnPartFromUrn(authorityUrnField.getText());
            dialog.close();
        } catch (GeniException e) {
            System.err.println("Invalid Urn \"" + authorityUrnField.getText() + "\"");
            authorityUrnField.getStyleClass().add("validation-error");
        }
    }

    public String getAuthorityUrn() {
        if (authorityUrnField.getText().equals("")) return null;
        return authorityUrnField.getText();
    }

    public static String getDialogResult() {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
//        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initModality(Modality.APPLICATION_MODAL);

        URL location = AddAuthorityDialogController.class.getResource("AddAuthorityDialog.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(location, null);
        Parent box;
        try {
            box = (Parent) fxmlLoader.load();
            Scene scene = new Scene(box);
            AddAuthorityDialogController controller = (AddAuthorityDialogController) fxmlLoader.getController();
            controller.setDialog(dialog);
            dialog.setScene(scene);
            dialog.sizeToScene();
            dialog.showAndWait();
            if (controller.getAuthorityUrn() != null)
                return controller.getAuthorityUrn();
            return null;
        } catch (IOException ex) {
            Logger.getLogger(JFedAdvancedGuiController.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}