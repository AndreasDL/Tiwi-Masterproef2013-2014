package be.iminds.ilabt.jfed.ui.javafx.probe_gui.dialog;

import be.iminds.ilabt.jfed.highlevel.model.CredentialInfo;
import be.iminds.ilabt.jfed.ui.javafx.util.JavaFXDialogUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * CredentialViewDialog
 */
public class CredentialViewDialog { //TODO remove this and make CredentialEditDialog do this showing

    public static void showDialog(CredentialInfo credentialInfo) {
        final Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
        //        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initModality(Modality.APPLICATION_MODAL);

        //TODO add extra fields showing credential
        String message = credentialInfo.getCredential().getCredentialXml();

        VBox box = new VBox();
        HBox box2 = new HBox();
        box.setAlignment(Pos.CENTER);
        box2.setAlignment(Pos.CENTER);
        Button butOk = new Button("OK");
        Label credentialTypeLabel = new Label("Type:");
        final TextField credentialTypeField = new TextField(credentialInfo == null ? "geni_sfa" : credentialInfo.getCredential().getType());
        credentialTypeField.setEditable(false);

        Label credentialVersionLabel = new Label("Version:");
        final TextField credentialVersionField = new TextField(credentialInfo == null ? "3" : credentialInfo.getCredential().getVersion());
        credentialVersionField.setEditable(false);

        Label valueLabel = new Label("Xml value:");
        final TextArea valueArea = new TextArea(credentialInfo == null ? "" : credentialInfo.getCredential().getCredentialXml());
        valueArea.setEditable(false);
        VBox.setVgrow(valueArea, Priority.ALWAYS);
        box2.getChildren().add(butOk);
        box.getChildren().addAll(credentialTypeLabel, credentialTypeField, credentialVersionLabel, credentialVersionField, valueArea, box2);
        JavaFXDialogUtil.addDialogStyle(box);
        Scene scene = new Scene(box);
        dialog.setScene(scene);
        dialog.sizeToScene();

        butOk.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                dialog.close();
            }
        });

        dialog.show();
    }
}
