package be.iminds.ilabt.jfed.ui.javafx.rightClickPane.demos;


import be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components.PropertiesWindow;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class PropertiesDemo extends Application {
    @FXML PropertiesWindow prop;


    @Override
    public void start(Stage primaryStage) throws Exception{
        prop = new PropertiesWindow();

        primaryStage.setTitle("properties demo");
        Rectangle2D r = Screen.getPrimary().getBounds();
        primaryStage.setScene(new Scene(prop, r.getWidth(), r.getHeight()));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
