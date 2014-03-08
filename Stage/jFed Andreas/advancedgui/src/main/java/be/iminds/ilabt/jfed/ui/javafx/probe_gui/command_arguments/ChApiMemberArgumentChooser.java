package be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments;

import be.iminds.ilabt.jfed.lowlevel.api.UniformFederationSliceAuthorityApi;
import be.iminds.ilabt.jfed.util.GeniUrn;
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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * StringArgumentChooser
 */
public class ChApiMemberArgumentChooser extends CommandArgumentChooser<List<UniformFederationSliceAuthorityApi.MemberTuple>> {
    class MyMemberTuple {
        public StringProperty urn = new SimpleStringProperty();
        public StringProperty role = new SimpleStringProperty();
        public MyMemberTuple(String urn, String role) {
            this.urn.set(urn);
            this.role.set(role);
        }
        public void addListeners() {
            this.urn.addListener(changeListener);
            this.role.addListener(changeListener);
        }
        public void removeListeners() {
            this.urn.removeListener(changeListener);
            this.role.removeListener(changeListener);
        }
    }

    private ChangeListener<String> changeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observableValue, String s, String s1) {
            updateMembers();
        }
    };

    @FXML private TableView<MyMemberTuple> table;

    ObservableList<MyMemberTuple> argument = FXCollections.observableArrayList();
    private ObjectProperty<List<UniformFederationSliceAuthorityApi.MemberTuple>> valueProperty = new SimpleObjectProperty<List<UniformFederationSliceAuthorityApi.MemberTuple>>();

    public ChApiMemberArgumentChooser(List<UniformFederationSliceAuthorityApi.MemberTuple> defaultArguments) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ChApiMemberArgumentChooser.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

            assert table != null;
            if (defaultArguments != null)
                for (UniformFederationSliceAuthorityApi.MemberTuple e : defaultArguments)
                    argument.add(new MyMemberTuple(e.memberUrn.toString(), e.role));
            table.setItems(this.argument);

            table.setEditable(true);

            Callback<TableColumn, TableCell> cellFactory =
                    new Callback<TableColumn, TableCell>() {
                        public TableCell call(TableColumn p) {
                            return new EditingCell();
                        }
                    };

            TableColumn keyCol = new TableColumn("Member URN");
            keyCol.setMinWidth(100);
            keyCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MyMemberTuple,String>,ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<MyMemberTuple, String> myEntryStringCellDataFeatures) {
                    return myEntryStringCellDataFeatures.getValue().urn;
                }
            });
            keyCol.setCellFactory(cellFactory);

            TableColumn valueCol = new TableColumn("Role");
            valueCol.setMinWidth(200);
            valueCol.setCellFactory(cellFactory);
            valueCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<MyMemberTuple,String>,ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<MyMemberTuple, String> myEntryStringCellDataFeatures) {
                    return myEntryStringCellDataFeatures.getValue().role;
                }
            });

            table.getColumns().addAll(keyCol, valueCol);

//            table.setTableMenuButtonVisible(true);

            argument.addListener(new ListChangeListener<MyMemberTuple>() {
                @Override
                public void onChanged(Change<? extends MyMemberTuple> change) {
                    while (change.next()) {
                        for (MyMemberTuple r : change.getRemoved())
                            r.removeListeners();
                        for (MyMemberTuple a : change.getAddedSubList())
                            a.addListeners();
                    }

                    //not the best way to link this, but I want to keep "value" in CommandArgumentChooser as it is
                    updateMembers();
                }
            });
            //init
            updateMembers();
            value = valueProperty;
            assert value != null;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void updateMembers() {
        List<UniformFederationSliceAuthorityApi.MemberTuple> newVal = new ArrayList<UniformFederationSliceAuthorityApi.MemberTuple>();
        for (MyMemberTuple e : argument)
            try {
                newVal.add(new UniformFederationSliceAuthorityApi.MemberTuple(new GeniUrn(e.urn.get()), e.role.get()));
            } catch (GeniUrn.GeniUrnParseException e1) {
                System.err.println("Ignoring invalid urn: "+e.urn.get());
                //ignore
            }
        valueProperty.set(newVal);
    }

    public void add() {
        argument.add(new MyMemberTuple("member_urn", "role"));
    }

    public void remove() {
        MyMemberTuple r = table.getSelectionModel().getSelectedItem();
        if (r != null)
            argument.remove(r);
    }



    private static class EditingCell extends TableCell<MyMemberTuple, String> {
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
