package be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments;

import be.iminds.ilabt.jfed.highlevel.model.CredentialInfo;
import be.iminds.ilabt.jfed.highlevel.model.EasyModel;
import be.iminds.ilabt.jfed.lowlevel.GeniCredential;
import be.iminds.ilabt.jfed.ui.javafx.probe_gui.dialog.CredentialEditDialog;
import be.iminds.ilabt.jfed.ui.javafx.probe_gui.dialog.CredentialViewDialog;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * StringArgumentChooser
 */
public class CredentialArgumentChooser extends CommandArgumentChooser<CredentialInfo> {
    @FXML private ComboBox<CredentialInfo> comboBox;

    private EasyModel easyModel;

    private final CredentialSubject credentialSubject;
    public CredentialArgumentChooser(EasyModel easyModel, CredentialSubject credentialSubject) {
        this.easyModel = easyModel;
        this.credentialSubject = credentialSubject;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CredentialArgumentChooser.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

            assert easyModel != null;
            assert comboBox != null;

            switch (credentialSubject) {
                case USER: { comboBox.setItems(easyModel.getParameterHistoryModel().getUserCredentialsList()); break; }
                case SLICE: { comboBox.setItems(easyModel.getParameterHistoryModel().getSliceCredentialsList()); break; }
                case ANY: { comboBox.setItems(easyModel.getParameterHistoryModel().getAnyCredentialsList()); break; }
            }

            if (!comboBox.getItems().isEmpty())
                comboBox.getSelectionModel().selectLast();

            value = comboBox.getSelectionModel().selectedItemProperty();
            assert value != null;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void add() {
        ObservableList<CredentialInfo> creds = null;
        switch (credentialSubject) {
            case USER: { creds = easyModel.getParameterHistoryModel().getUserCredentialsList(); break; }
            case SLICE: { creds = easyModel.getParameterHistoryModel().getSliceCredentialsList(); break; }
            case ANY: { creds = easyModel.getParameterHistoryModel().getUncategorizedCredentiasList(); break; }
        }
        CredentialInfo newCredentialInfo = CredentialEditDialog.showAddDialog();
        if (newCredentialInfo != null)
            creds.add(newCredentialInfo);
    }
    public void view() {
        CredentialInfo selected = comboBox.getSelectionModel().selectedItemProperty().get();
        if (selected != null)
            CredentialViewDialog.showDialog(selected);
    }
}
