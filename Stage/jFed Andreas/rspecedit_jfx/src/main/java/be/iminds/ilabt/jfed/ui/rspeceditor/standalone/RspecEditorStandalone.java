package be.iminds.ilabt.jfed.ui.rspeceditor.standalone;

import java.net.URL;

import be.iminds.ilabt.jfed.ui.rspeceditor.util.JavaFxStyleSheetHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class RspecEditorStandalone extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        URL location = getClass().getResource("RspecEditorStandalone.fxml");
        assert location != null;
        FXMLLoader fxmlLoader = new FXMLLoader(location, null);

        Parent root = (Parent)fxmlLoader.load();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
        stage.sizeToScene();
    }

    public static void main(String [] args) {
        launch(args);
    }

}
