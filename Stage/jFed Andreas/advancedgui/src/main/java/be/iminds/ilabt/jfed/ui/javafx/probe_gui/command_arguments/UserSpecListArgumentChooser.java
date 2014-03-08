package be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments;

import be.iminds.ilabt.jfed.highlevel.model.EasyModel;
import be.iminds.ilabt.jfed.lowlevel.api.UserSpec;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * StringArgumentChooser
 */
public class UserSpecListArgumentChooser extends CommandArgumentChooser<List<UserSpec>> {
    @FXML private ListView<UserSpec> userSpecListView;
    @FXML private TextField userUrnTextField;
    @FXML private CheckBox sshCheckBox;
    @FXML private TextArea sshTextField;
    @FXML private Button addButton;
    @FXML private Button removeButton;

    ObservableList<UserSpec> userSpecs = FXCollections.observableArrayList();
    private ObjectProperty<List<UserSpec>> valueProperty = new SimpleObjectProperty<List<UserSpec>>();

    private class MyUserSpec extends UserSpec {
        public MyUserSpec(String urn, Collection<String> sshKey) {
            super(urn, sshKey);
        }

        public MyUserSpec(String urn) {
            super(urn);
        }

        @Override
        public String toString() {
            if (!sshKey.isEmpty()) {
                String keysString = "";
                //                      keysString += "[ ";
                boolean first = true;
                for (String key : sshKey) {
                    if (!first)
                        keysString += ", ";
                    if (key.length() < 10)
                        keysString += "\""+key+"\"";
                    else
                        keysString += "\""+key.substring(0, 10)+"...\"";
                    first = false;
                }
                //                        keysString += " ]";

                if (sshKey.size() == 1)
                    return urn + " with key " + keysString;
                else
                    return urn + " with keys: " + keysString;
            }
            else
                return urn;
        }
    }

    private EasyModel easyModel;
    public UserSpecListArgumentChooser(EasyModel easyModel) {
        this.easyModel = easyModel;

        if (easyModel.getGeniUserProvider().isUserLoggedIn()) {
            String userUrn = easyModel.getGeniUserProvider().getLoggedInGeniUser().getUserUrn();
            assert userUrn != null;
            List<String> userKeys = easyModel.getUserKeys();
            if (userKeys == null) userKeys = new ArrayList<String>();
            userSpecs.add(new MyUserSpec(userUrn, userKeys));
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("UserSpecListArgumentChooser.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

            assert userSpecListView != null;
            userSpecListView.setItems(this.userSpecs);
            userSpecListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            if (!userSpecListView.getItems().isEmpty())
                userSpecListView.getSelectionModel().selectLast();

            final ObservableList<UserSpec> selectedItems = userSpecListView.getSelectionModel().getSelectedItems();
            selectedItems.addListener(new ListChangeListener<UserSpec>() {
                @Override
                public void onChanged(Change<? extends UserSpec> change) {
                    //not the best way to link this, but I want to keep "value" in CommandArgumentChooser as it is
                    valueProperty.set(new ArrayList<UserSpec>(selectedItems));
                }
            });
            //init
            valueProperty.set(new ArrayList<UserSpec>(selectedItems));
            value = valueProperty;
            assert value != null;

            sshTextField.disableProperty().bind(sshCheckBox.selectedProperty().not());

            ChangeListener<UserSpec> setEditItemChangeListener = new ChangeListener<UserSpec>() {
                @Override
                public void changed(ObservableValue<? extends UserSpec> observableValue, UserSpec oldUserSpec, UserSpec newUserSpec) {
//                    System.out.println("edited item changed to "+newUserSpec);
                    if (newUserSpec != null) {
                        addButton.setText("Edit");

                        userUrnTextField.setText(newUserSpec.getUrn());
                        if (newUserSpec.getSshKey().isEmpty()) {
                            sshCheckBox.setSelected(false);
                            sshTextField.setText("");
                        } else {
                            sshCheckBox.setSelected(true);
                            String keys = "";
                            for (String key : newUserSpec.getSshKey())
                                keys += key+"\n";
                            sshTextField.setText(keys);
                        }
                    }
                }
            };

            addButton.setText("Add");
//            userSpecListView.getFocusModel().focusedItemProperty()
            userSpecListView.getSelectionModel().selectedItemProperty().addListener(setEditItemChangeListener);
            if (!this.userSpecs.isEmpty())
                setEditItemChangeListener.changed(null, null, this.userSpecs.get(0));

            userUrnTextField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String oldText, String newText) {
                    boolean existingInList = isUrnInList(newText);
                    if (existingInList) {
                        addButton.setText("Edit");
                        removeButton.setDisable(false);
                    }
                    else {
                        addButton.setText("Add");
                        removeButton.setDisable(true);
                    }
                }
            });
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private boolean isUrnInList(String userUrn) {
        for (UserSpec userSpec : userSpecs)
            if (userSpec.getUrn().equals(userUrn))
                return true;
        return false;
    }

    public void remove() {
        String userUrn = userUrnTextField.getText();
        List<UserSpec> toRemove = new ArrayList<UserSpec>();
        for (UserSpec userSpec : userSpecs)
            if (userSpec.getUrn().equals(userUrn))
                toRemove.add(userSpec);
        userSpecs.removeAll(toRemove);
    }
    public void add() {
        String userUrn = userUrnTextField.getText();
        List<String> userKeys = new ArrayList<String>();
        if (sshCheckBox.isSelected()) {
            String keysText = sshTextField.getText();
            String [] keys = keysText.split("\n");
            for (String key : keys)
                if (!key.isEmpty())
                    userKeys.add(key);
        }

        //if already exists, remove, so we replace it.
        List<UserSpec> toRemove = new ArrayList<UserSpec>();
        for (UserSpec userSpec : userSpecs)
            if (userSpec.getUrn().equals(userUrn))
                toRemove.add(userSpec);
        userSpecs.removeAll(toRemove);

        userSpecs.add(new MyUserSpec(userUrn, userKeys));
    }
}
