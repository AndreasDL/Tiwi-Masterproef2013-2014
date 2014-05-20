package be.iminds.ilabt.jfed.ui.javafx.am_list_gui;

import be.iminds.ilabt.jfed.highlevel.model.AuthorityInfo;
import be.iminds.ilabt.jfed.lowlevel.ServerType;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * ExecuteServicesPanel
 */
public class UrlsPanel extends BorderPane implements Initializable {
    private ListProperty<AuthorityInfo.AuthorityUrl> authorityUrls =
            new SimpleListProperty<AuthorityInfo.AuthorityUrl>(FXCollections.<AuthorityInfo.AuthorityUrl>observableArrayList());



    private BooleanProperty editable = new SimpleBooleanProperty(true);
    public BooleanProperty editableProperty() { return editable; }
    public boolean getEditable() { return editable.get(); }
    public void setEditable(boolean edit) { editable.set(edit); }



    public UrlsPanel() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("UrlsPanel.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }



    public ListProperty<AuthorityInfo.AuthorityUrl> authorityUrlsProperty() {
        return authorityUrls;
    }

    @FXML protected Button addButton;
    @FXML protected TableView table;

    class StringEditingCell extends TableCell<AuthorityInfo.AuthorityUrl, String> {
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

    class URLEditingCell extends TableCell<AuthorityInfo.AuthorityUrl, URL> {
        private TextField textField;

        public URLEditingCell() {
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
        public void updateItem(URL item, boolean empty) {
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
                        URL parsedUrl = null;
                        try {
                            parsedUrl = new URL(textField.getText());
                        } catch (MalformedURLException e) { }

                        textField.getStyleClass().removeAll("validation-error", "validation-warning");
                        if (parsedUrl == null) {
                            //invalid URL, commit nothing
                            //TODO provide visual feedback
                            textField.getStyleClass().add("validation-error");
                        } else
                            commitEdit(parsedUrl);
                    }
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }

    class RemoveButtonCell extends TableCell<AuthorityInfo.AuthorityUrl, String> {
        private Button button;

        public RemoveButtonCell() {
            setEditable(false);
            button = new Button("X");
            button.getStyleClass().add("deleteIco");
            button.visibleProperty().bind(editable);
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    //                   T service = (T) getTableRow().getItem();
                    //                   executeServices.remove(service);
                    authorityUrls.remove(getTableRow().getItem());
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

    private static ObservableList<ServerType.GeniServerRole> allRoles = FXCollections.observableArrayList(ServerType.GeniServerRole.values());
    class SelectTypeCell extends ComboBoxTableCell<AuthorityInfo.AuthorityUrl, ServerType.GeniServerRole> {
//        private ComboBox<ServerType.GeniServerRole> combobox;

        public SelectTypeCell() {
            super(allRoles);
//            setEditable(false);
//            combobox = new ComboBox<ServerType.GeniServerRole>();
//            combobox.setItems(allRoles);
//            combobox.editableProperty().bind(editable);
//            setText(null);
//            if (getTableRow() == null || getTableRow().getItem() == null)
//                setGraphic(null);
//            else
//                setGraphic(combobox);
        }

        @Override
        public void updateItem(ServerType.GeniServerRole item, boolean empty) {
            super.updateItem(item, empty);
            if(item != null) {

            }
        }
    }

    @FXML protected void addCommand() {
        try {
            AuthorityInfo.AuthorityUrl newAuthInfo = new AuthorityInfo.AuthorityUrl(new ServerType(ServerType.GeniServerRole.AM, 2), new URL("https://example.com/xmlrpc/am2/"));
            authorityUrls.add(newAuthInfo);
        } catch (MalformedURLException e) {
            throw new RuntimeException("This should never happen", e);
        }
    }
    @FXML protected void removeSelectedCommand() {
        authorityUrls.removeAll(table.getSelectionModel().getSelectedItems());
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //        table.editableProperty().bind(editable);
        table.setEditable(true);

        addButton.visibleProperty().bind(editable);



        TableColumn<AuthorityInfo.AuthorityUrl, ServerType.GeniServerRole> selecttypeCol = new TableColumn("Role");
        selecttypeCol.setCellFactory(new Callback<TableColumn<AuthorityInfo.AuthorityUrl, ServerType.GeniServerRole>, TableCell<AuthorityInfo.AuthorityUrl, ServerType.GeniServerRole>>() {
            @Override
            public TableCell<AuthorityInfo.AuthorityUrl, ServerType.GeniServerRole> call(TableColumn<AuthorityInfo.AuthorityUrl, ServerType.GeniServerRole> authorityUrlGeniServerRoleTableColumn) {
                return new SelectTypeCell();
            }
        });
        selecttypeCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AuthorityInfo.AuthorityUrl, ServerType.GeniServerRole>,ObservableValue<ServerType.GeniServerRole>>() {

            @Override
            public ObservableValue<ServerType.GeniServerRole> call(TableColumn.CellDataFeatures<AuthorityInfo.AuthorityUrl, ServerType.GeniServerRole> authUrl) {
                return authUrl.getValue().roleProperty();
            }
        });
        table.getColumns().addAll(selecttypeCol);




        TableColumn<AuthorityInfo.AuthorityUrl, String> versionCol = new TableColumn("version");
        versionCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AuthorityInfo.AuthorityUrl, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<AuthorityInfo.AuthorityUrl, String> p) {
                return p.getValue().versionProperty();
            }
        });
        versionCol.setCellFactory(new Callback<TableColumn<AuthorityInfo.AuthorityUrl, String>, TableCell<AuthorityInfo.AuthorityUrl, String>>() {
            @Override
            public TableCell<AuthorityInfo.AuthorityUrl, String> call(TableColumn<AuthorityInfo.AuthorityUrl, String> authorityUrlStringTableColumn) {
                return new StringEditingCell();
            }
        });
        versionCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<AuthorityInfo.AuthorityUrl, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<AuthorityInfo.AuthorityUrl, String> t) {
                        AuthorityInfo.AuthorityUrl authUrl = (AuthorityInfo.AuthorityUrl) t.getTableView().getItems().get(t.getTablePosition().getRow());
                        authUrl.versionProperty().set(t.getNewValue());
                    }
                }
        );
        table.getColumns().add(versionCol);




        TableColumn<AuthorityInfo.AuthorityUrl, URL> urlCol = new TableColumn("url");
        urlCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<AuthorityInfo.AuthorityUrl, URL>, ObservableValue<URL>>() {
            public ObservableValue<URL> call(TableColumn.CellDataFeatures<AuthorityInfo.AuthorityUrl, URL> p) {
                return p.getValue().urlProperty();
            }
        });
        urlCol.setCellFactory(new Callback<TableColumn<AuthorityInfo.AuthorityUrl, URL>, TableCell<AuthorityInfo.AuthorityUrl, URL>>() {
            @Override
            public TableCell<AuthorityInfo.AuthorityUrl, URL> call(TableColumn<AuthorityInfo.AuthorityUrl, URL> authorityUrlURLTableColumn) {
                return new URLEditingCell();
            }
        });
        urlCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<AuthorityInfo.AuthorityUrl, URL>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<AuthorityInfo.AuthorityUrl, URL> t) {
                        AuthorityInfo.AuthorityUrl authUrl = (AuthorityInfo.AuthorityUrl) t.getTableView().getItems().get(t.getTablePosition().getRow());
                        authUrl.urlProperty().set(t.getNewValue());
                    }
                }
        );
        table.getColumns().add(urlCol);




        TableColumn removeCol = new TableColumn("");
        removeCol.visibleProperty().bind(editable);
        removeCol.setCellFactory(new Callback() {
            @Override
            public Object call(Object o) {
                return new RemoveButtonCell();
            }
        });
        table.getColumns().addAll(removeCol);


        //quick hack to get somewhat usefull widths  TODO automatically set correct widths
        selecttypeCol.prefWidthProperty().bind(table.widthProperty().divide(4));
        versionCol.prefWidthProperty().bind(table.widthProperty().divide(8));
        urlCol.prefWidthProperty().bind(table.widthProperty().divide(2));
        removeCol.prefWidthProperty().bind(table.widthProperty().divide(8));


        authorityUrls.addListener(new ChangeListener<ObservableList<AuthorityInfo.AuthorityUrl>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList<AuthorityInfo.AuthorityUrl>> observableValue,
                                ObservableList<AuthorityInfo.AuthorityUrl> oldUrls,
                                ObservableList<AuthorityInfo.AuthorityUrl> newUrls) {
                table.setItems(newUrls);
            }
        });
    }
}
