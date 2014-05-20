package be.iminds.ilabt.jfed.ui.javafx.choosers;

import be.iminds.ilabt.jfed.highlevel.model.AuthorityInfo;
import be.iminds.ilabt.jfed.highlevel.model.CredentialInfo;
import be.iminds.ilabt.jfed.highlevel.model.EasyModel;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.ui.javafx.am_list_gui.AMList;
import be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments.CommandArgumentChooser;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * AuthorityChooser
 */
public class AuthorityChooser extends VBox {
    @FXML
    private ComboBox<AuthorityInfo> comboBox;

    private EasyModel easyModel;

//    private class AuthorityInfoCell extends ListCell<AuthorityInfo> {
//            private Label label = new Label();
//
//            public AuthorityInfoCell(boolean button) {
//                //if (button)
//                //    label.getStyleClass().add("?"); //how to prevent white text in button?
//
//                label.getStyleClass().add("select_authority_cell");
//            }
//
//            @Override
//            public void updateItem(final AuthorityInfo item, boolean empty) {
//                super.updateItem(item, empty);
//
//                if (item == null)
//                    setGraphic(null);
//                else {
//                    label.setText(item.getGeniAuthority().getName());
//                    setGraphic(label);
//                }
//            }
//        }

    public AuthorityChooser() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AuthorityChooser.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

            assert comboBox != null;

//            comboBox.setCellFactory(new Callback<ListView<AuthorityInfo>, ListCell<AuthorityInfo>>() {
//                        @Override
//                        public ListCell<AuthorityInfo> call(ListView<AuthorityInfo> list) {
//                            return new AuthorityInfoCell(false);
//                        }
//                    });
//            comboBox.setButtonCell(new AuthorityInfoCell(true));
            comboBox.setConverter(new StringConverter<AuthorityInfo>() {
                @Override
                public String toString(AuthorityInfo authorityInfo) {
                    assert authorityInfo != null;
                    assert authorityInfo.getGeniAuthority() != null;
                    return authorityInfo.getGeniAuthority().getName();
                }

                @Override
                public AuthorityInfo fromString(String s) {
                    throw new RuntimeException("unused");
                }
            });

            editable.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldBoolean, Boolean newBoolean) {
                    comboBox.disableProperty().set(!newBoolean);

                }
            });
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setEasyModel(EasyModel easyModel) {
        this.easyModel = easyModel;
        assert easyModel != null;

        assert comboBox != null;
        comboBox.setItems(easyModel.getAuthorityList().authorityInfosProperty());
        comboBox.getSelectionModel().selectFirst();
    }

    public AuthorityInfo getSelectedAuthority() {
        return comboBox.getSelectionModel().getSelectedItem();
    }

    public ReadOnlyObjectProperty<AuthorityInfo> selectedAuthorityProperty() {
        return comboBox.getSelectionModel().selectedItemProperty();
    }

    public void select(AuthorityInfo authorityInfo) {
        assert authorityInfo != null : "must always select something";
        assert easyModel != null : "not yet initialised";
        comboBox.getSelectionModel().select(authorityInfo);
    }


    public void select(SfaAuthority authorityInfo) {
        assert authorityInfo != null : "must always select something";
        assert easyModel != null : "not yet initialised";
        AuthorityInfo ai = easyModel.getAuthorityList().get(authorityInfo);
        select(ai);
    }

    public void editList() {
        AMList.editAuthorityList(easyModel.getAuthorityList(), easyModel.getGeniUserProvider());
    }

    private BooleanProperty editable = new SimpleBooleanProperty(true);
    public BooleanProperty editableProperty() {
        return editable;
    }
}
