package be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.property.ListProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * StringArgumentChooser
 */
public class StringArgumentProvidedOptionsChooser extends CommandArgumentChooser<String> implements Initializable {
    @FXML private ComboBox<String> valueField;

    public StringArgumentProvidedOptionsChooser(ObservableList<String> options) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("StringArgumentProvidedOptionsChooser.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

            assert valueField != null;
            valueField.setItems(options);

            if (!valueField.getItems().isEmpty())
                valueField.getSelectionModel().selectLast();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        value = valueField.getSelectionModel().selectedItemProperty();
    }
}
