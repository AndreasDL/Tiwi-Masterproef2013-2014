package be.iminds.ilabt.jfed.ui.javafx.about_gui;

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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AboutBoxController implements Initializable {
    @FXML private BorderPane pane;
    @FXML private TextArea textArea;
    private Stage dialog;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        readManifestInfo();

        URL css = getClass().getResource("about.css");
        if (css != null)
            pane.getStylesheets().add(css.toExternalForm());

        textArea.setText("jFed toolkit\n" +
                "Copyright iMinds VZW, Belgium\n" +
                "Homepage: http://jfed.iminds.be\n" +
                "Release: "+version+"\n" +
                "SVN revision: "+build);
    }

    private static String build = null;
    private static String version = null;

    public static void readManifestInfo() {
        if (build != null || version != null) return;

        try {
            Enumeration<URL> resources = AboutBoxController.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                Manifest manifest = new Manifest(resources.nextElement().openStream());
                String title = manifest.getMainAttributes().getValue("Implementation-Title");
//                System.out.println("DEBUG: "+title+" -> manifest.getMainAttributes()="+manifest.getMainAttributes().entrySet());
                if (title != null && title.equals("jFed GUI")) {
//                    System.out.println("DEBUG:      jFed -> manifest.getMainAttributes()="+manifest.getMainAttributes().entrySet());
                    build = manifest.getMainAttributes().getValue("Implementation-Build");
                    version = manifest.getMainAttributes().getValue("Implementation-Version");
                    return;
                }
            }
        } catch (IOException E) {
            return;
        }
        return;
    }

    public void setDialog(Stage dialog) {
        this.dialog = dialog;
    }


    public void ok() {
        dialog.close();
    }

    public static void showAbout() {
        Stage dialog = new Stage();
//        dialog.initStyle(StageStyle.UTILITY);
//        dialog.initModality(Modality.WINDOW_MODAL);
//        dialog.initModality(Modality.APPLICATION_MODAL);

        URL location = AboutBoxController.class.getResource("AboutBox.fxml");
        assert location != null : "Could not find AboutBox.fxml for about dialog";
        FXMLLoader fxmlLoader = new FXMLLoader(location, null);
        Parent box;
        try {
            box = (Parent) fxmlLoader.load();
            Scene scene = new Scene(box);
            AboutBoxController controller = (AboutBoxController) fxmlLoader.getController();
            controller.setDialog(dialog);
            dialog.setScene(scene);
            dialog.sizeToScene();
            dialog.show();
        } catch (IOException ex) {
            Logger.getLogger(JFedAdvancedGuiController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
