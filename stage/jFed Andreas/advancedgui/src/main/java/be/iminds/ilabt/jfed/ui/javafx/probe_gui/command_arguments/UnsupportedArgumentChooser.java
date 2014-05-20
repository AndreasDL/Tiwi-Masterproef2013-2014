package be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments;

import be.iminds.ilabt.jfed.lowlevel.ApiMethodParameter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 */
public class UnsupportedArgumentChooser<T> extends CommandArgumentChooser<T> implements Initializable {
    @FXML protected Label textLabel;

    private Class parameterClass;
    public UnsupportedArgumentChooser(Class parameterClass) {
        this.parameterClass = parameterClass;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("UnsupportedArgumentChooser.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        value = new SimpleObjectProperty<T>(null); //dummy

        textLabel.setText("Note: Parameter of type " + parameterClass.getName() + " is not supported yet.");
    }
}
