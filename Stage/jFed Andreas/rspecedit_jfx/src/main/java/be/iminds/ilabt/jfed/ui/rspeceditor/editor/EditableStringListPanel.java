package be.iminds.ilabt.jfed.ui.rspeceditor.editor;

import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecNode;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * ExecuteServicesPanel
 */
public class EditableStringListPanel extends BorderPane implements Initializable  {
    @FXML protected Button addButton;
    @FXML protected Button deleteButton;
    @FXML protected ListView listView;
    private ListProperty<String> list = new SimpleListProperty<>(FXCollections.<String>observableArrayList());


    private BooleanProperty editable = new SimpleBooleanProperty(true);
    public BooleanProperty editableProperty() { return editable; }
    public boolean getEditable() { return editable.get(); }
    public void setEditable(boolean edit) { editable.set(edit); }



    public EditableStringListPanel() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("EditableStringListPanel.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    class StringEditingCell extends ListCell<String> {
            private TextField textField;

            public StringEditingCell() {
            }

            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    super.startEdit();
                    createTextField();
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                }
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();

                setText((String) getItem());
                setGraphic(null);
            }

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        if (textField != null) {
                            textField.setText(getString());
                        }
                        setText(null);
                        setGraphic(textField);
                    } else {
                        setText(getString());
                        setGraphic(null);
                    }
                }
            }

            private void createTextField() {
                textField = new TextField(getString());
                textField.editableProperty().bind(editable);
                textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
                textField.focusedProperty().addListener(new ChangeListener<Boolean>(){
                    @Override
                    public void changed(ObservableValue<? extends Boolean> arg0,
                        Boolean arg1, Boolean arg2) {
                            if (!arg2) {
                                commitEdit(textField.getText());
                            }
                    }
                });
            }

            private String getString() {
                return getItem() == null ? "" : getItem().toString();
            }
        }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listView.setItems(list);
        listView.setEditable(true);
        listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override public ListCell<String> call(ListView<String> stringListView) { return new StringEditingCell(); }
        });
        addButton.visibleProperty().bind(editable);
        deleteButton.visibleProperty().bind(editable);
    }

    public ListProperty<String> listProperty() {
        return list;
    }

    @FXML private void add() {
        String newItem = "new item";
        list.add(newItem);
        listView.edit(list.indexOf(newItem));
    }

    @FXML private void delete() {
       int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < list.size())
            list.remove(selectedIndex);
    }
}
