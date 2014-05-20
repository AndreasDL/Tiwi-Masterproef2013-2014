package be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments;

import be.iminds.ilabt.jfed.util.GeniUrn;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * StringArgumentChooser
 */
public class MultiUrnArgumentProvidedOptionsChooser extends CommandArgumentChooser<List<GeniUrn>> {
    @FXML private ListView<String> valueField;
    @FXML private TextField addTextField;

    ObservableList<String> options = FXCollections.observableArrayList();
    private ObjectProperty<List<GeniUrn>> valueProperty = new SimpleObjectProperty<List<GeniUrn>>();

    public MultiUrnArgumentProvidedOptionsChooser(ObservableList<String> options) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MultiStringArgumentProvidedOptionsChooser.fxml")); //for now same fxml as string
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

            valueField.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<String>() {
                @Override
                public void onChanged(Change<? extends String> change) {
                    //not the best way to link this, but I want to keep "value" in CommandArgumentChooser as it is
                    setValue();
                }
            });
            //init
            setValue();
            value = valueProperty;
            assert value != null;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void setValue() {
        final ObservableList<String> selectedItems = valueField.getSelectionModel().getSelectedItems();
        List<GeniUrn> urns = new ArrayList<GeniUrn>();
        for (String urnString : selectedItems) {
            GeniUrn u = GeniUrn.parse(urnString);
            if (u != null)
                urns.add(u);
            else
                System.err.println("Invalid urn will be ignored: "+u);
        }
        valueProperty.set(urns);
    }

    public void add() {
        String toAdd = addTextField.getText();
        options.add(toAdd);
    }
}
