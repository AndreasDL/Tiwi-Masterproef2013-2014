package be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments;

import be.iminds.ilabt.jfed.highlevel.model.AuthorityInfo;
import be.iminds.ilabt.jfed.util.GeniUrn;
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
public class UrnArgumentProvidedOptionsChooser extends CommandArgumentChooser<String> implements Initializable {
    @FXML private ComboBox<String> valueField;

    private final String urnType;
    /**
     * @param urnType required type for urns. may be null to accept any type
     * @param defaultAuthInfo if a default urn is constructed, and this is not null, theis auth will be used
     * */
    public UrnArgumentProvidedOptionsChooser(ObservableList<String> options, String urnType, AuthorityInfo defaultAuthInfo) {
        this.urnType = urnType;

        if (options.isEmpty() && urnType != null) {
            options.add(new GeniUrn(
                    defaultAuthInfo == null ? "<auth>" : defaultAuthInfo.getGeniAuthority().getNameForUrn(),
                    urnType == null ? "<type>" : urnType,
                    "<name>").toString());
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("UrnArgumentProvidedOptionsChooser.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

            assert valueField != null;
            valueField.setItems(options);

            if (!valueField.getItems().isEmpty())
                valueField.getSelectionModel().selectLast();

            //TODO: add urn validation
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        value = valueField.getSelectionModel().selectedItemProperty();
    }
}
