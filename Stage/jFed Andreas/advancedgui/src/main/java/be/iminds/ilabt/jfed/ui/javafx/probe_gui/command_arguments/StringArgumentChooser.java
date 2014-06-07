package be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments;

import be.iminds.ilabt.jfed.lowlevel.ApiMethodParameter;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * StringArgumentChooser
 */
public class StringArgumentChooser extends CommandArgumentChooser<String> implements Initializable {
    @FXML private TextField valueField;

    public StringArgumentChooser(String defaultValue) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("StringArgumentChooser.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            if (defaultValue != null)
                valueField.setText(defaultValue);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        value = valueField.textProperty();
    }
}
