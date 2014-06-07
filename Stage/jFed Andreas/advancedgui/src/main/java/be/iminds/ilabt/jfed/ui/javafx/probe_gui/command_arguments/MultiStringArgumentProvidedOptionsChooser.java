package be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments;

import be.iminds.ilabt.jfed.highlevel.model.CredentialInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * StringArgumentChooser
 */
public class MultiStringArgumentProvidedOptionsChooser extends CommandArgumentChooser<List<String>> {
    @FXML private ListView<String> valueField;
    @FXML private TextField addTextField;

    ObservableList<String> options = FXCollections.observableArrayList();
    private ObjectProperty<List<String>> valueProperty = new SimpleObjectProperty<List<String>>();

    public MultiStringArgumentProvidedOptionsChooser(ObservableList<String> options) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MultiStringArgumentProvidedOptionsChooser.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

            assert valueField != null;
            this.options.addAll(options);
            valueField.setItems(this.options);
            valueField.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            if (!valueField.getItems().isEmpty())
                valueField.getSelectionModel().selectLast();

            final ObservableList<String> selectedItems = valueField.getSelectionModel().getSelectedItems();
            selectedItems.addListener(new ListChangeListener<String>() {
                @Override
                public void onChanged(Change<? extends String> change) {
                    //not the best way to link this, but I want to keep "value" in CommandArgumentChooser as it is
                    valueProperty.set(new ArrayList<String>(selectedItems));
                }
            });
            //init
            valueProperty.set(new ArrayList<String>(selectedItems));
            value = valueProperty;
            assert value != null;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void add() {
        String toAdd = addTextField.getText();
        options.add(toAdd);
    }
}
