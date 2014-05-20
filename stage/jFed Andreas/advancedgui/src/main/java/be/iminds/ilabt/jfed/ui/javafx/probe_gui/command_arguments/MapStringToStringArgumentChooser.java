package be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StringArgumentChooser
 */
public class MapStringToStringArgumentChooser extends CommandArgumentChooser<Map<String, String>> {
    class MyEntry {
        public StringProperty key = new SimpleStringProperty();
        public StringProperty value = new SimpleStringProperty();
        public MyEntry(String key, String value) {
            this.key.set(key);
            this.value.set(value);
        }
        public void addListeners() {
            this.key.addListener(changeListener);
            this.value.addListener(changeListener);
        }
        public void removeListeners() {
            this.key.removeListener(changeListener);
            this.value.removeListener(changeListener);
        }
    }

    private ChangeListener<String> changeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observableValue, String s, String s1) {
            updateMap();
        }
    };

    @FXML private TableView<MyEntry> table;

    ObservableList<MyEntry> argument = FXCollections.observableArrayList();
    private ObjectProperty<Map<String, String>> valueProperty = new SimpleObjectProperty<Map<String, String>>();

    public MapStringToStringArgumentChooser(Map<String, String> defaultArguments) {
        this(defaultArguments, "Key", "Value");
    }
    public MapStringToStringArgumentChooser(Map<String, String> defaultArguments, String keyColHeader, String valueColHeader) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MapStringToStringArgumentChooser.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

            assert table != null;
            if (defaultArguments != null)
                for (Map.Entry<String, String> e : defaultArguments.entrySet())
                    argument.add(new MyEntry(e.getKey(), e.getValue()));
            table.setItems(this.argument);

            table.setEditable(true);

            Callback<TableColumn, TableCell> cellFactory =
                    new Callback<TableColumn, TableCell>() {
                        public TableCell call(TableColumn p) {
                            return new EditingCell();
                        }
                    };

            TableColumn keyCol = new TableColumn(keyColHeader);
            keyCol.setMinWidth(100);
            keyCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MyEntry,String>,ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<MyEntry, String> myEntryStringCellDataFeatures) {
                    return myEntryStringCellDataFeatures.getValue().key;
                }
            });
            keyCol.setCellFactory(cellFactory);

            TableColumn valueCol = new TableColumn(valueColHeader);
            valueCol.setMinWidth(200);
            valueCol.setCellFactory(cellFactory);
            valueCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MyEntry,String>,ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<MyEntry, String> myEntryStringCellDataFeatures) {
                    return myEntryStringCellDataFeatures.getValue().value;
                }
            });

            table.getColumns().addAll(keyCol, valueCol);

//            table.setTableMenuButtonVisible(true);

            argument.addListener(new ListChangeListener<MyEntry>() {
                @Override
                public void onChanged(Change<? extends MyEntry> change) {
                    while (change.next()) {
                        for (MyEntry r : change.getRemoved())
                            r.removeListeners();
                        for (MyEntry a : change.getAddedSubList())
                            a.addListeners();
                    }

                    //not the best way to link this, but I want to keep "value" in CommandArgumentChooser as it is
                    updateMap();
                }
            });

            //init
            updateMap();
            for (MyEntry e : argument)
                e.addListeners();

            value = valueProperty;
            assert value != null;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void updateMap() {
        Map<String, String> newMap = new HashMap<String, String>();
        for (MyEntry e : argument)
            newMap.put(e.key.get(), e.value.get());
        valueProperty.set(newMap);
//        System.out.println("DEBUG updateMap(): "+newMap);
    }

    public void add() {
        argument.add(new MyEntry("key", "value"));
    }

    public void remove() {
        MyEntry r = table.getSelectionModel().getSelectedItem();
        if (r != null)
            argument.remove(r);
    }



    private static class EditingCell extends TableCell<MyEntry, String> {
        private TextField textField;

        public EditingCell() {
        }

        @Override
        public void startEdit() {
//            System.out.println("startEdit");
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        //bug: somehow the caret isn't always shown, which is confusing
                        textField.requestFocus();
                        textField.selectAll();
                        textField.end();

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                textField.requestFocus();
                                textField.end();
                            }
                        });
                    }
                });
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
//            System.out.println("updateItem "+item);
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
            if (textField == null) {
                textField = new TextField(getString());
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
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }

}
