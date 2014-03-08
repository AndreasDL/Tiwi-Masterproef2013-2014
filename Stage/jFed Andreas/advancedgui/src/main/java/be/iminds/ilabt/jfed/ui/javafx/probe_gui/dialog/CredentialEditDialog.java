package be.iminds.ilabt.jfed.ui.javafx.probe_gui.dialog;

import be.iminds.ilabt.jfed.highlevel.model.CredentialInfo;
import be.iminds.ilabt.jfed.lowlevel.GeniCredential;
import be.iminds.ilabt.jfed.ui.javafx.util.JavaFXDialogUtil;
import javafx.beans.property.*;
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
 * CredentialEditDialog
 */
public class CredentialEditDialog {

    public static CredentialInfo showAddDialog() {
        return showDialog(true, null);
    }
    public static CredentialInfo showDialog(final boolean add, final CredentialInfo credentialInfo) {
        assert add || credentialInfo != null;

        final Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
        //        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initModality(Modality.APPLICATION_MODAL);


        final ObjectProperty<CredentialInfo> resultProp = new SimpleObjectProperty(null);

        VBox box = new VBox();
        HBox box2 = new HBox();
        HBox.setHgrow(box2, Priority.ALWAYS);
//        box.setAlignment(Pos.CENTER);
        box2.setAlignment(Pos.CENTER);
        Button butOk = new Button("Use this Credential");
        Button butCancel = new Button(add ? "Do not add" : "Cancel Edit");
        Button butLoad = new Button("Load from file...");

        Label credentialTypeLabel = new Label("Type:");
        final TextField credentialTypeField = new TextField(credentialInfo == null ? "geni_sfa" : credentialInfo.getCredential().getType());

        Label credentialVersionLabel = new Label("Version:");
        final TextField credentialVersionField = new TextField(credentialInfo == null ? "3" : credentialInfo.getCredential().getVersion());

        Label valueLabel = new Label("Xml value:");
        final TextArea valueArea = new TextArea(credentialInfo == null ? "" : credentialInfo.getCredential().getCredentialXml());
        valueArea.setEditable(true);
        VBox.setVgrow(valueArea, Priority.ALWAYS);
        box2.getChildren().addAll(butOk, /*butLoad,*/ butCancel); //TODO: add load from file
        box.getChildren().addAll(credentialTypeLabel, credentialTypeField, credentialVersionLabel, credentialVersionField, valueLabel, valueArea, box2);

        JavaFXDialogUtil.addDialogStyle(box);
        Scene scene = new Scene(box);
        dialog.setScene(scene);
        dialog.sizeToScene();

        butOk.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String newName = "User Imported Credential";
                    if (!add && credentialInfo != null) {
                        newName = credentialInfo.getCredential().getName();
                        if (!newName.endsWith(" (Edited)"))
                            newName += " (Edited)";
                    }
                GeniCredential resGeniCredential = new GeniCredential(newName, valueArea.getText(), credentialTypeField.getText(), credentialVersionField.getText());
                CredentialInfo resCredentialInfo = new CredentialInfo(resGeniCredential);
                resultProp.set(resCredentialInfo);
                dialog.close();
            }
        });
        butCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                resultProp.set(null);
                dialog.close();
            }
        });

        dialog.showAndWait();

        return resultProp.get();
    }
}
