package be.iminds.ilabt.jfed.ui.javafx.rightClickPane.demos;


import be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components.RspecEditorRightClickPane;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class DemoApp extends Application {
    private RspecEditorRightClickPane pane;

    @Override
    public void start(Stage primaryStage) throws Exception{
        pane = new RspecEditorRightClickPane();

        primaryStage.setTitle("DemoApp");
        Rectangle2D r = Screen.getPrimary().getBounds();
        Scene s = new Scene(pane, r.getWidth(), r.getHeight());
        primaryStage.setScene(s);
        primaryStage.show();

        System.out.println();
    }


    public static void main(String[] args) {
        launch(args);
    }

}
