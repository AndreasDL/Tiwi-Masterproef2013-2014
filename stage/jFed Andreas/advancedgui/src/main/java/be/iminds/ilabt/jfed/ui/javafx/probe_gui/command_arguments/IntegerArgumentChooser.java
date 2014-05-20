package be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * StringArgumentChooser
 */
public class IntegerArgumentChooser extends CommandArgumentChooser<Number> implements Initializable {
    @FXML private TextField valueField;

    private IntegerProperty integerProperty = new SimpleIntegerProperty();

    public IntegerArgumentChooser(Integer defaultValue) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("IntegerArgumentChooser.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            if (defaultValue != null)
                valueField.setText(defaultValue+"");
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        StringConverter conv = new NumberStringConverter();

        Bindings.bindBidirectional(valueField.textProperty(), integerProperty,  conv);

        value = integerProperty;
    }
}
