package be.iminds.ilabt.jfed.ui.rspeceditor.editor;

import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecLink;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;

public class LinkSettingsPanel extends TableView implements Initializable {
    private ListProperty<RspecLink.LinkSetting> linkSettings = new SimpleListProperty<RspecLink.LinkSetting>();

    public ListProperty<RspecLink.LinkSetting> linkSettingsProperty() {
        return linkSettings;
    }


    class IntegerEditingCell extends TableCell<RspecLink.LinkSetting, Integer> {
        private TextField textField;

        public IntegerEditingCell() {
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
        public void updateItem(Integer item, boolean empty) {
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
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
            textField.focusedProperty().addListener(new ChangeListener<Boolean>(){
                @Override
                public void changed(ObservableValue<? extends Boolean> arg0,
                    Boolean arg1, Boolean arg2) {
                        if (!arg2) {
                            if (textField.getText().equals(""))
                                commitEdit(null);
                            else
                                commitEdit(Integer.parseInt(textField.getText()));
                        }
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }

    class DoubleEditingCell extends TableCell<RspecLink.LinkSetting, Double> {
        private TextField textField;

        public DoubleEditingCell() {
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
        public void updateItem(Double item, boolean empty) {
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
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
            textField.focusedProperty().addListener(new ChangeListener<Boolean>(){
                @Override
                public void changed(ObservableValue<? extends Boolean> arg0,
                    Boolean arg1, Boolean arg2) {
                        if (!arg2) {
                            if (textField.getText().equals(""))
                                commitEdit(null);
                            else
                                commitEdit(Double.parseDouble(textField.getText()));
                        }
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }

    public LinkSettingsPanel() {
        setMinWidth(300);
        setMinHeight(200);

        setEditable(true);
        Callback<TableColumn, TableCell> integerCellFactory =
             new Callback<TableColumn, TableCell>() {
                 public TableCell call(TableColumn p) {
                    return new IntegerEditingCell();
                 }
             };
        Callback<TableColumn, TableCell> doubleCellFactory =
             new Callback<TableColumn, TableCell>() {
                 public TableCell call(TableColumn p) {
                    return new DoubleEditingCell();
                 }
             };

        TableColumn fromCol = new TableColumn("From");
        TableColumn toCol = new TableColumn("To");
        TableColumn capacityCol = new TableColumn("Capacity (bit/s)");
        TableColumn latencyCol = new TableColumn("Latency (ms)");
        TableColumn packlossCol = new TableColumn("Packet Loss (%)");

        fromCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RspecLink.LinkSetting, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<RspecLink.LinkSetting, String> p) {
                return p.getValue().getFromIface().idProperty();
            }
         });
        toCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RspecLink.LinkSetting, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<RspecLink.LinkSetting, String> p) {
                return p.getValue().getToIface().idProperty();
            }
         });
        capacityCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RspecLink.LinkSetting, Number>, ObservableValue<Number>>() {
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<RspecLink.LinkSetting, Number> p) {
                return p.getValue().capacity_bpsProperty();
            }
         });
        latencyCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RspecLink.LinkSetting, Number>, ObservableValue<Number>>() {
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<RspecLink.LinkSetting, Number> p) {
                return p.getValue().latency_msProperty();
            }
         });
        packlossCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<RspecLink.LinkSetting, Number>, ObservableValue<Number>>() {
            public ObservableValue<Number> call(TableColumn.CellDataFeatures<RspecLink.LinkSetting, Number> p) {
                return p.getValue().packetLossProperty().multiply(100.0);
            }
         });



        capacityCol.setCellFactory(integerCellFactory);
        capacityCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<RspecLink.LinkSetting, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<RspecLink.LinkSetting, Integer> t) {
                        RspecLink.LinkSetting linkSetting = (RspecLink.LinkSetting) t.getTableView().getItems().get(t.getTablePosition().getRow());
                        linkSetting.setCapacity_bps(t.getNewValue());
                    }
                }
        );


        latencyCol.setCellFactory(integerCellFactory);
        latencyCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<RspecLink.LinkSetting, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<RspecLink.LinkSetting, Integer> t) {
                        RspecLink.LinkSetting linkSetting = (RspecLink.LinkSetting) t.getTableView().getItems().get(t.getTablePosition().getRow());
                        linkSetting.setLatency_ms(t.getNewValue());
                    }
                }
        );


        packlossCol.setCellFactory(doubleCellFactory);
        packlossCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<RspecLink.LinkSetting, Double>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<RspecLink.LinkSetting, Double> t) {
                        RspecLink.LinkSetting linkSetting = (RspecLink.LinkSetting) t.getTableView().getItems().get(t.getTablePosition().getRow());
                        Double newval = t.getNewValue();
                        if (newval != null)
                            linkSetting.setPacketLoss(newval / 100.0);
                        else
                            linkSetting.setPacketLoss(null);
                    }
                }
        );


        this.getColumns().addAll(fromCol, toCol, capacityCol, latencyCol, packlossCol);

        linkSettings.addListener(new ChangeListener<ObservableList<RspecLink.LinkSetting>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList<RspecLink.LinkSetting>> observableValue,
                                ObservableList<RspecLink.LinkSetting> oldLinkSettings,
                                ObservableList<RspecLink.LinkSetting> newLinkSettings) {
                 LinkSettingsPanel.this.setItems(newLinkSettings);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    private void setLinkSettings(RspecLink.LinkSetting shownLinkSettings) {

    }
}
