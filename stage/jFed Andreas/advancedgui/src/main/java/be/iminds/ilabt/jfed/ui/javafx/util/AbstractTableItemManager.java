package be.iminds.ilabt.jfed.ui.javafx.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class AbstractTableItemManager<T> extends BorderPane implements Initializable {
    private ListProperty<T> services = new SimpleListProperty<T>();



    private BooleanProperty editable = new SimpleBooleanProperty(true);
    public BooleanProperty editableProperty() { return editable; }
    public boolean getEditable() { return editable.get(); }
    public void setEditable(boolean edit) { editable.set(edit); }



    public ListProperty<T> servicesProperty() {
        return services;
    }

    @FXML protected Button addButton;
    @FXML protected TableView table;

    class StringEditingCell extends TableCell<T, String> {
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

            setText(getItem().toString());
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
                        if (textField.getText().equals(""))
                            commitEdit(null);
                        else
                            commitEdit(textField.getText());
                    }
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }

    class RemoveButtonCell extends TableCell<T, String> {
        private Button button;

        public RemoveButtonCell() {
            setEditable(false);
            button = new Button("X");
            button.visibleProperty().bind(editable);
            button.setOnAction(new EventHandler<ActionEvent> (){
                @Override
                public void handle(ActionEvent actionEvent) {
//                   T service = (T) getTableRow().getItem();
//                   executeServices.remove(service);
                    services.remove(getTableRow().getItem());
                }
            });
            setText(null);
            if (getTableRow() == null || getTableRow().getItem() == null)
                setGraphic(null);
            else
                setGraphic(button);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (getTableRow() == null || getTableRow().getItem() == null)
                setGraphic(null);
            else
                setGraphic(button);
        }
    }

    public AbstractTableItemManager() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AbstractTableItemManager.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    protected abstract int getColumnCount();
    protected abstract T emptyToAdd();

    @FXML protected void addCommand() {
        T newService = emptyToAdd();
        services.add(newService);
    }
    @FXML protected void removeSelectedCommand() {
        services.removeAll(table.getSelectionModel().getSelectedItems());
    }

    protected abstract String columnName(int colIndex);
    protected abstract ObservableValue<String> getColumnValue(T item, int colIndex);
    protected abstract void setColumnValue(T item, int colIndex, String value);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
//        table.editableProperty().bind(editable);
        table.setEditable(true);

        addButton.visibleProperty().bind(editable);

        Callback<TableColumn, TableCell> stringCellFactory =
                new Callback<TableColumn, TableCell>() {
                    public TableCell call(TableColumn p) {
                        return new StringEditingCell();
                    }
                };
        Callback<TableColumn, TableCell> removeButtonCellFactory =
                new Callback<TableColumn, TableCell>() {
                    public TableCell call(TableColumn p) {
                        return new RemoveButtonCell();
                    }
                };

        for (int i = 0; i < getColumnCount(); i++) {
            final int colIndex = i;

            TableColumn col = new TableColumn(columnName(i));

            col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<T, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<T, String> p) {
                    return getColumnValue(p.getValue(), colIndex);
                }
            });
            col.setCellFactory(stringCellFactory);
            col.setOnEditCommit(
                    new EventHandler<TableColumn.CellEditEvent<T, String>>() {
                        @Override
                        public void handle(TableColumn.CellEditEvent<T, String> t) {
                            T service = (T) t.getTableView().getItems().get(t.getTablePosition().getRow());
                            setColumnValue(service, colIndex, t.getNewValue());
                        }
                    }
            );

            table.getColumns().add(col);
        }


//        TableColumn shellCol = new TableColumn(columnName(0));
//        TableColumn commandCol = new TableColumn(columnName(1));

//        shellCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<T, String>, ObservableValue<String>>() {
//            public ObservableValue<String> call(TableColumn.CellDataFeatures<T, String> p) {
//                return getColumnValue(p.getValue(), 0);
//            }
//        });
//        commandCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<T, String>, ObservableValue<String>>() {
//            public ObservableValue<String> call(TableColumn.CellDataFeatures<T, String> p) {
//                return getColumnValue(p.getValue(), 1);
//            }
//        });
//
//        shellCol.setCellFactory(stringCellFactory);
//        shellCol.setOnEditCommit(
//                new EventHandler<TableColumn.CellEditEvent<T, String>>() {
//                    @Override
//                    public void handle(TableColumn.CellEditEvent<T, String> t) {
//                        T service = (T) t.getTableView().getItems().get(t.getTablePosition().getRow());
//                        setColumnValue(service, 0, t.getNewValue());
//                    }
//                }
//        );
//
//        commandCol.setCellFactory(stringCellFactory);
//        commandCol.setOnEditCommit(
//                new EventHandler<TableColumn.CellEditEvent<T, String>>() {
//                    @Override
//                    public void handle(TableColumn.CellEditEvent<T, String> t) {
//                        T service = (T) t.getTableView().getItems().get(t.getTablePosition().getRow());
//                        setColumnValue(service, 1, t.getNewValue());
//                    }
//                }
//        );
//        table.getColumns().addAll(shellCol, commandCol, removeCol);

        TableColumn removeCol = new TableColumn("");
        removeCol.visibleProperty().bind(editable);
        removeCol.setCellFactory(removeButtonCellFactory);
        table.getColumns().addAll(removeCol);

        services.addListener(new ChangeListener<ObservableList<T>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList<T>> observableValue,
                                ObservableList<T> oldServices,
                                ObservableList<T> newServices) {
                table.setItems(newServices);
            }
        });
    }
}
