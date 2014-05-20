package be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments;

import be.iminds.ilabt.jfed.highlevel.model.CredentialInfo;
import be.iminds.ilabt.jfed.highlevel.model.EasyModel;
import be.iminds.ilabt.jfed.ui.javafx.probe_gui.dialog.CredentialEditDialog;
import be.iminds.ilabt.jfed.ui.javafx.probe_gui.dialog.CredentialViewDialog;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * StringArgumentChooser
 */
public class MultiCredentialArgumentChooser extends CommandArgumentChooser<List<CredentialInfo>> {
    @FXML private ListView<CredentialInfo> listView;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button viewButton;

    private ObjectProperty<List<CredentialInfo>> valueProperty = new SimpleObjectProperty<List<CredentialInfo>>();

    private EasyModel easyModel;

    private final CredentialSubject credentialSubject;
    public MultiCredentialArgumentChooser(EasyModel easyModel, CredentialSubject credentialSubject) {
        this.easyModel = easyModel;
        this.credentialSubject = credentialSubject;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MultiCredentialArgumentChooser.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

            assert easyModel != null;
            assert listView != null;

            addButton.managedProperty().bind(addButton.visibleProperty());
            editButton.managedProperty().bind(editButton.visibleProperty());
            viewButton.managedProperty().bind(viewButton.visibleProperty());

            listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CredentialInfo>() {
                @Override
                public void changed(ObservableValue<? extends CredentialInfo> observableValue, CredentialInfo oldSelected, CredentialInfo newSelected) {
                    if (newSelected != null) {
                        editButton.setVisible(true);
                        viewButton.setVisible(true);
                    } else {
                        editButton.setVisible(false);
                        viewButton.setVisible(false);
                    }
                }
            });

            listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            switch (credentialSubject) {
                case USER: { listView.setItems(easyModel.getParameterHistoryModel().getUserCredentialsList()); break; }
                case SLICE: { listView.setItems(easyModel.getParameterHistoryModel().getSliceCredentialsList()); break; }
                case ANY: { listView.setItems(easyModel.getParameterHistoryModel().getAnyCredentialsList()); break; }
            }

            if (!listView.getItems().isEmpty()) {
                listView.getSelectionModel().selectLast();
            }

            final ObservableList<CredentialInfo> selectedItems = listView.getSelectionModel().getSelectedItems();
            selectedItems.addListener(new ListChangeListener<CredentialInfo>() {
                @Override
                public void onChanged(Change<? extends CredentialInfo> change) {
                    //not the best way to link this, but I want to keep "value" in CommandArgumentChooser as it is
                    valueProperty.set(new ArrayList<CredentialInfo>(selectedItems));
                }
            });
            //init
            valueProperty.set(new ArrayList<CredentialInfo>(selectedItems));
            value = valueProperty;
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
    public void edit() {
        CredentialInfo selected = listView.getSelectionModel().selectedItemProperty().get();

        CredentialInfo editedCredentialInfo = CredentialEditDialog.showDialog(false, selected);
        if (editedCredentialInfo != null) {
            ObservableList<CredentialInfo> creds = null;
            switch (credentialSubject) {
                case USER: { creds = easyModel.getParameterHistoryModel().getUserCredentialsList(); break; }
                case SLICE: { creds = easyModel.getParameterHistoryModel().getSliceCredentialsList(); break; }
                case ANY: { creds = easyModel.getParameterHistoryModel().getUncategorizedCredentiasList(); break; }
            }

            creds.remove(selected);
            creds.add(editedCredentialInfo);
        }
    }
    public void view() {
        CredentialInfo selected = listView.getSelectionModel().selectedItemProperty().get();
        if (selected != null)
            CredentialViewDialog.showDialog(selected);
    }
}
