package be.iminds.ilabt.jfed.ui.javafx.util;

import be.iminds.ilabt.jfed.ui.javafx.style.StyleHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * JavaFXDialogUtil
 */
public class JavaFXDialogUtil {
    static public void addDialogStyle(Parent p) {
        p.getStylesheets().add(StyleHelper.getStyleUrlString("advanced_gui.css"));
        p.getStylesheets().add(StyleHelper.getStyleUrlString("dialog.css"));
    }

    public static void errorMessage(String s) {
        errorMessage(s, null);
    }
    public static void errorMessage(String s, Window window) {
        showMessage(s, window); //TODO add error icon
    }

    public static Window getWindowFromNode(Node node) {
        if (node == null) return null;
        if (node.getScene() == null) return null;
        return node.getScene().getWindow();
    }

    public static void showMessage(String message) {
        showMessage(message, (Window)null);
    }
    public static void showMessage(String message, Node nodeFromWindow) {
        showMessage(message, getWindowFromNode(nodeFromWindow));
    }
    public static void showMessage(String message, Window window) {
        final Stage dialog = new Stage();
        if (window != null) {
            dialog.initOwner(window);
            dialog.initModality(Modality.WINDOW_MODAL);
        } else
            dialog.initModality(Modality.APPLICATION_MODAL);

        dialog.initStyle(StageStyle.UTILITY);

        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        Button but = new Button("OK");
        but.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                dialog.close();
            }
        });
        TextArea msgLabel = new TextArea(message);
        msgLabel.setEditable(false);
        msgLabel.setWrapText(true);
        msgLabel.setMinHeight(200.0);
        msgLabel.setMinWidth(500.0);
        VBox.setVgrow(msgLabel, Priority.ALWAYS);
        box.getChildren().addAll(msgLabel, but);
        addDialogStyle(box);
        Scene scene = new Scene(box);
        dialog.setScene(scene);
        dialog.sizeToScene();
        dialog.showAndWait();
    }

    /** returns true if choiceA, false if choiceB */
    public static boolean show2ChoiceDialog(String message, String choiceA, String choiceB) {
        return show2ChoiceDialog(message, choiceA, choiceB, (Window)null);
    }
    public static boolean show2ChoiceDialog(String message, String choiceA, String choiceB, Node nodeFromWindow) {
        return show2ChoiceDialog(message, choiceA, choiceB, getWindowFromNode(nodeFromWindow));
    }
    public static boolean show2ChoiceDialog(String message, String choiceA, String choiceB, Window window) {
        final Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
        if (window != null) {
            dialog.initOwner(window);
            dialog.initModality(Modality.WINDOW_MODAL);
        } else
            dialog.initModality(Modality.APPLICATION_MODAL);

        final BooleanProperty choiceProp = new SimpleBooleanProperty(false); //bit of a hack to use this in this way

        VBox box = new VBox();
        HBox box2 = new HBox();
        box.setAlignment(Pos.CENTER);
        box2.setAlignment(Pos.CENTER);
        Button butA = new Button(choiceA);
        Button butB = new Button(choiceB);
        TextArea msgLabel = new TextArea(message);
        msgLabel.setEditable(false);
        VBox.setVgrow(msgLabel, Priority.ALWAYS);
        box2.getChildren().addAll(butA, butB);
        box.getChildren().addAll(msgLabel, box2);
        addDialogStyle(box);
        Scene scene = new Scene(box);
        dialog.setScene(scene);
        dialog.sizeToScene();

        butA.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                choiceProp.set(true);
                dialog.close();
            }
        });
        butB.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                choiceProp.set(false);
                dialog.close();
            }
        });

        dialog.showAndWait();

        return choiceProp.get();
    }
}
