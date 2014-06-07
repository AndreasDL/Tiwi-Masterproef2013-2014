package be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * StringArgumentChooser
 */
public class BooleanArgumentChooser extends CommandArgumentChooser<Boolean> implements Initializable {
    @FXML private CheckBox valueBox;

    public BooleanArgumentChooser(Boolean defaultValue) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("BooleanArgumentChooser.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

            if (defaultValue != null)
                valueBox.setSelected(defaultValue);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        value = valueBox.selectedProperty();
    }
}
