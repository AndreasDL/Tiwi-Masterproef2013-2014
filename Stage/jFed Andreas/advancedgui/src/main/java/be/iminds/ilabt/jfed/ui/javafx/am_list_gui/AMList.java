package be.iminds.ilabt.jfed.ui.javafx.am_list_gui;

import be.iminds.ilabt.jfed.highlevel.model.AuthorityInfo;
import be.iminds.ilabt.jfed.highlevel.model.AuthorityList;
import be.iminds.ilabt.jfed.lowlevel.GeniUserProvider;
import be.iminds.ilabt.jfed.lowlevel.authority.*;
import be.iminds.ilabt.jfed.ui.javafx.util.EditableStringListPanel;
import be.iminds.ilabt.jfed.ui.javafx.util.JavaFXDialogUtil;
import be.iminds.ilabt.jfed.ui.rspeceditor.util.ObjectPropertyBindHelper;
import be.iminds.ilabt.jfed.ui.rspeceditor.util.SelectedObjectPropertyBinder;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * AMList: Overview of known Aggregate Managers. Allows editing, adding and retrieving from internet lists
 */
public class AMList extends BorderPane implements Initializable {
    private AuthorityList authorityList;
    private GeniUserProvider geniUserProvider;

    private ObjectProperty<AuthorityInfo> selectedAuthorityInfo = new SimpleObjectProperty<>(null);

    @FXML private ListView<AuthorityInfo> internalAuthorityList;
    @FXML private ListView<AuthorityInfo> externalAuthorityList;

    @FXML private HBox urlsPanelHBox;
    private UrlsPanel urlsPanel;

    @FXML private Button saveButton;
    @FXML private Button discardButton;
    @FXML private Button deleteButton;
    @FXML private Button showXmlButton;

    @FXML private HBox allowedCertAliasesPanelHBox;
    private EditableStringListPanel allowedCertAliasesPanel;

    public AMList(AuthorityList authorityList, GeniUserProvider geniUserProvider) {
        this.authorityList = authorityList;
        this.geniUserProvider = geniUserProvider;

        URL location = getClass().getResource("AMList.fxml");
        assert location != null;
        FXMLLoader fxmlLoader = new FXMLLoader(location);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            BorderPane self = (BorderPane) fxmlLoader.load();
            assert this == self;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML private TextField nameField;
    @FXML private TextField urnField;
    @FXML private TextField urnPartField;
    @FXML private CheckBox reconnectCheckBox;
    @FXML private TextArea certField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        internalAuthorityList.setCellFactory(new Callback<ListView<AuthorityInfo>, ListCell<AuthorityInfo>>() {
            @Override
            public ListCell<AuthorityInfo> call(ListView<AuthorityInfo> authorityInfoListView) {
                return new InternalAuthorityCell(true, true);
            }
        });
        externalAuthorityList.setCellFactory(new Callback<ListView<AuthorityInfo>, ListCell<AuthorityInfo>>() {
            @Override
            public ListCell<AuthorityInfo> call(ListView<AuthorityInfo> authorityInfoListView) {
                return new ExternalAuthorityCell();
            }
        });

        internalAuthorityList.setItems(authorityList.authorityInfosProperty());

        urlsPanel = new UrlsPanel();
        urlsPanelHBox.getChildren().add(urlsPanel);
        HBox.setHgrow(urlsPanel, Priority.ALWAYS);

        allowedCertAliasesPanel = new EditableStringListPanel();
        allowedCertAliasesPanelHBox.getChildren().add(allowedCertAliasesPanel);
        HBox.setHgrow(allowedCertAliasesPanel, Priority.ALWAYS);

        SelectedObjectPropertyBinder<AuthorityInfo> selectedObjectPropertyBinder = new SelectedObjectPropertyBinder(true/*bidirectional*/);

        selectedAuthorityInfo.addListener(new ChangeListener<AuthorityInfo>() {
            @Override
            public void changed(ObservableValue<? extends AuthorityInfo> observableValue, AuthorityInfo oldAuthorityInfo, AuthorityInfo newAuthorityInfo) {
                if (oldAuthorityInfo != null) {
                    urnPartField.textProperty().unbind();
                }

                if (newAuthorityInfo != null) {
                    urlsPanel.authorityUrlsProperty().set(newAuthorityInfo.getUrls());
                    allowedCertAliasesPanel.listProperty().set(newAuthorityInfo.getAllowedCertificateHostnameAliases());
                    urnPartField.textProperty().bind(newAuthorityInfo.urnPartProperty());
                }
            }
        });

        selectedObjectPropertyBinder.getBinders().add(new ObjectPropertyBindHelper<AuthorityInfo>(nameField.textProperty()) {
            @Override
            public Property objectProperty(AuthorityInfo o) {
                return o.nameProperty();
            }
        });

        selectedObjectPropertyBinder.getBinders().add(new ObjectPropertyBindHelper<AuthorityInfo>(urnField.textProperty()) {
            @Override
            public Property objectProperty(AuthorityInfo o) {
                return o.urnProperty();
            }
        });

        selectedObjectPropertyBinder.getBinders().add(new ObjectPropertyBindHelper<AuthorityInfo>(certField.textProperty()) {
            @Override public Property objectProperty(AuthorityInfo o) {
                return o.pemSslTrustCertProperty();
            }
        });

        selectedObjectPropertyBinder.getBinders().add(new ObjectPropertyBindHelper<AuthorityInfo>(reconnectCheckBox.selectedProperty()) {
            @Override
            public Property objectProperty(AuthorityInfo o) {
                return o.reconnectEachTimeProperty();
            }
        });

        selectedAuthorityInfo.bind(internalAuthorityList.getSelectionModel().selectedItemProperty());
        selectedObjectPropertyBinder.setSelectedObjectProperty(selectedAuthorityInfo);

        selectedAuthorityInfo.addListener(new ChangeListener<AuthorityInfo>() {
            @Override
            public void changed(ObservableValue<? extends AuthorityInfo> observableValue, AuthorityInfo oldAuthorityInfo, AuthorityInfo newAuthorityInfo) {
                //auto commit is not needed: we commit on save. this keeps discard possible until save
//                //auto commit
//                if (saveButton == null && oldAuthorityInfo != null && !oldAuthorityInfo.committedProperty().get()) {
//                    System.out.println("Auto committing previous selected authority");
//                    oldAuthorityInfo.commit();
//                }

                boolean showButtons = newAuthorityInfo != null && newAuthorityInfo.getEasyModel() != null;
                if (saveButton != null)
                    saveButton.setVisible(showButtons); //commit button is currently disabled
                discardButton.setVisible(showButtons);
                deleteButton.setVisible(showButtons);
                showXmlButton.setVisible(newAuthorityInfo != null);

                if (newAuthorityInfo == null) {
                    if (oldAuthorityInfo != null) {
                        if (saveButton != null)
                            saveButton.disableProperty().unbind();
                        discardButton.disableProperty().unbind();
                    }
                } else {
                    if (oldAuthorityInfo != null) {
                        if (saveButton != null)
                            saveButton.disableProperty().unbind();
                        discardButton.disableProperty().unbind();
                    }
                    if (saveButton != null)
                        saveButton.disableProperty().bind(newAuthorityInfo.committedProperty());
                    discardButton.disableProperty().bind(newAuthorityInfo.committedProperty());
                }
            }
        });

        //clear selection when selecting in external list
        externalAuthorityList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<AuthorityInfo>() {
            @Override
            public void changed(ObservableValue<? extends AuthorityInfo> observableValue, AuthorityInfo oldAuthorityInfo, AuthorityInfo newAuthorityInfo) {
                if (newAuthorityInfo != null)
                    select(null);
            }
        });
    }

    public void select(AuthorityInfo authorityInfo) {
        if (authorityInfo == null)
            internalAuthorityList.getSelectionModel().clearSelection();
        else
            internalAuthorityList.getSelectionModel().select(authorityInfo);
    }

    class InternalAuthorityCell extends ListCell<AuthorityInfo> {
        private HBox hbox = new HBox();
        private HBox hbox2 = new HBox();
        private Label label = new Label();
        private Label unsavedIco = new Label();
        private Button delete = new Button("Delete");
        private Button edit = new Button("Edit");

        private boolean deletable;
        private boolean editable;

        public InternalAuthorityCell(boolean deletable, boolean editable) {
            this.deletable = deletable;
            this.editable = editable;

            delete.getStyleClass().add("deleteIco");
            edit.getStyleClass().add("editIco");

            unsavedIco.getStyleClass().add("unsavedIco");

            hbox.getChildren().add(label);
            hbox.getChildren().add(unsavedIco);
            unsavedIco.setVisible(false);
            if (deletable) {
                hbox2.getChildren().add(delete);
            }
//            hbox2.getChildren().add(edit);
            hbox2.setAlignment(Pos.CENTER_RIGHT);
            HBox.setHgrow(hbox, Priority.ALWAYS);
            HBox.setHgrow(hbox2, Priority.ALWAYS);
            hbox.getChildren().add(hbox2);
            if (!editable) edit.setText("view");
        }

        @Override
        public void updateItem(final AuthorityInfo item, boolean empty) {
            super.updateItem(item, empty);

            unsavedIco.visibleProperty().unbind();
            label.textProperty().unbind();

            if (item == null)
                setGraphic(null);
            else {
                label.textProperty().bind(item.nameProperty());
                unsavedIco.visibleProperty().bind(item.savedProperty().not());

                if (deletable)
                    delete.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            item.delete();
                            select(null);
                        }
                    });

                edit.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        select(item);
                    }
                });

                setGraphic(hbox);
            }
        }
    }


    class ExternalAuthorityCell extends ListCell<AuthorityInfo> {
        private HBox hbox = new HBox();
        private HBox hbox2 = new HBox();
        private Label label = new Label();
        private Button add = new Button("Add");

        public ExternalAuthorityCell() {
            hbox.getChildren().add(label);
            hbox2.getChildren().add(add);
            hbox2.setAlignment(Pos.CENTER_RIGHT);
            HBox.setHgrow(hbox, Priority.ALWAYS);
            HBox.setHgrow(hbox2, Priority.ALWAYS);
            hbox.getChildren().add(hbox2);
            add.getStyleClass().add("addIco");
        }

        @Override
        public void updateItem(final AuthorityInfo item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null)
                setGraphic(null);
            else {
                label.setText(item.getGeniAuthority().getName());

                add.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        addToInternal(item);
                    }
                });

                setGraphic(hbox);
            }
        }
    }





    private static Stage authorityListStage = null;
    public static void editAuthorityList(AuthorityList authorityList, GeniUserProvider geniUserProvider) {
        if (authorityListStage == null) {
            try {
                AMList amList = new AMList(authorityList, geniUserProvider);

                authorityListStage = new Stage();
                authorityListStage.setTitle("View & Edit Authority List");
                Scene scene = new Scene(amList);
                authorityListStage.setScene(scene);
                authorityListStage.sizeToScene();
//                authorityListStage.setWidth(800.0);
//                authorityListStage.setHeight(600.0);
            } catch (Exception e) {
                System.err.println("Failed to initialize AuthorityList GUI: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        authorityListStage.show();
    }


    public void saveToInternal() {
        selectedAuthorityInfo.get().commit();
    }

    public void discard() {
        selectedAuthorityInfo.get().restore();
    }

    public void delete() {
        selectedAuthorityInfo.get().delete();
    }

    public void viewXML() {
        String xml = selectedAuthorityInfo.get().getGeniAuthority().toXmlString();
        JavaFXDialogUtil.showMessage(xml, internalAuthorityList);
    }

    public void addNew() {
        String newAuthUrn = AddAuthorityDialogController.getDialogResult();
        if (newAuthUrn == null) return;
        if (authorityList.get(newAuthUrn) != null) {
            JavaFXDialogUtil.showMessage("An authority with the URN \""+newAuthUrn+"\" already exists.\n\nInstead of creating a new one, the existing one will be opened for editing.", internalAuthorityList);
            select(authorityList.get(newAuthUrn));
        } else {
            AuthorityInfo newAuthorityInfo = authorityList.add(newAuthUrn);
            select(newAuthorityInfo);
        }
    }

    public void addToInternal(AuthorityInfo externalAuthorityInfo) {
        if (externalAuthorityInfo == null) return;
        if (authorityList.get(externalAuthorityInfo.getGeniAuthority().getUrn()) != null) {
            SfaAuthority external = externalAuthorityInfo.getGeniAuthority();
            boolean overwrite = JavaFXDialogUtil.show2ChoiceDialog(
                    "An authority with the URN \""+external.getUrn()+"\" already exists.\n\n"+
                            "What do you want to do?\n"+
                            " - Open existing one and overwrite internal data with external data (noting saved until you click save).\n"+
                            " - Just open the existing internal authority, and discard the external data.",
                    "Overwrite with external data",
                    "Open internal version for editing", internalAuthorityList);

            AuthorityInfo existingAuthorityInfo = authorityList.get(external.getUrn());

            if (overwrite) {
                existingAuthorityInfo.getGeniAuthority().updateAll(external.getHrn(), external.getUrls(), external.getGid(), external.getType());
                existingAuthorityInfo.getGeniAuthority().setAllowedCertificateHostnameAlias(external.getAllowedCertificateHostnameAliases());
                existingAuthorityInfo.getGeniAuthority().setPemSslTrustCert(external.getPemSslTrustCert());
                existingAuthorityInfo.restore();
                select(existingAuthorityInfo);
            }
            else
                select(existingAuthorityInfo);
        } else {
            //store a copy in internal list
            AuthorityInfo newAuthorityInfo = authorityList.add(new SfaAuthority(externalAuthorityInfo.getGeniAuthority()));
//            select(newAuthorityInfo);
        }

        externalAuthorityList.getSelectionModel().clearSelection();
    }


    public void scan() {
        SfaAuthority createdSfaAuth = ScanAuthorityDialogController.getDialogResult(authorityList, geniUserProvider);
        if (createdSfaAuth == null) return;
        if (authorityList.get(createdSfaAuth.getUrn()) != null) {
            boolean overwrite = JavaFXDialogUtil.show2ChoiceDialog(
                    "An authority with the URN \""+createdSfaAuth.getUrn()+"\" already exists.\n\n"+
                            "What do you want to do?\n"+
                            " - Open existing one and overwrite data with scanned data (noting saved until you click save).\n"+
                            " - Just open the existing one, and discard the scanned data.",
                    "Overwrite with scanned data",
                    "Discard scanned data", internalAuthorityList);

            AuthorityInfo existingAuthorityInfo = authorityList.get(createdSfaAuth.getUrn());

            if (overwrite) {
                existingAuthorityInfo.getGeniAuthority().updateAll(createdSfaAuth.getHrn(), createdSfaAuth.getUrls(), createdSfaAuth.getGid(), createdSfaAuth.getType());
                existingAuthorityInfo.getGeniAuthority().setAllowedCertificateHostnameAlias(createdSfaAuth.getAllowedCertificateHostnameAliases());
                existingAuthorityInfo.getGeniAuthority().setPemSslTrustCert(createdSfaAuth.getPemSslTrustCert());
                existingAuthorityInfo.restore();
                select(existingAuthorityInfo);
            }
            else
                select(existingAuthorityInfo);
        } else {
            AuthorityInfo newAuthorityInfo = authorityList.add(createdSfaAuth);
            select(newAuthorityInfo);
        }
    }

    public void loadList() {
        //reload from file
        authorityList.load();
    }

    public void saveList() {
        authorityList.save();
    }

    public void resetList() {
        authorityList.resetToInternalDefaults();
    }

    public void viewXMLList() {
        try{
            //write to file
            StringWriter writer = new StringWriter();
            be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities xmlAuthorities = new be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities();
            for (SfaAuthority auth : authorityList.getAuthorityListModel().getAuthorities()) {
                be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.Authority xmlAuth = auth.toXml();
                xmlAuthorities.getAuthority().add(xmlAuth);
            }

            JAXBContext context = JAXBContext.newInstance(be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(xmlAuthorities, writer);

            writer.close();
            JavaFXDialogUtil.showMessage(""+writer.getBuffer().toString(), internalAuthorityList);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadF4f() {
        ObservableList<AuthorityInfo> externalList = FXCollections.observableArrayList();

        AuthorityListModel authorityListModel = new AuthorityListModel();
        Fed4FireAuthorityList.load(authorityListModel);
        int i = 0;
        for (SfaAuthority sfaAuthority : authorityListModel.getAuthorities()) {
            AuthorityInfo authorityInfo = new AuthorityInfo(null, sfaAuthority, i++);
            externalList.add(authorityInfo);
        }

        externalAuthorityList.setItems(externalList);
    }
    public void loadUtahHttp() {
        ObservableList<AuthorityInfo> externalList = FXCollections.observableArrayList();

        AuthorityListModel authorityListModel = new AuthorityListModel();
        UtahClearingHouseAuthorityList.load(authorityListModel);
        int i = 0;
        for (SfaAuthority sfaAuthority : authorityListModel.getAuthorities()) {
            AuthorityInfo authorityInfo = new AuthorityInfo(null, sfaAuthority, i++);
            externalList.add(authorityInfo);
        }

        externalAuthorityList.setItems(externalList);
    }
    public void loadUtahCH() {
        JavaFXDialogUtil.showMessage("This option is not yet implemented", internalAuthorityList);
    }
    public void loadAnyCH() {
        JavaFXDialogUtil.showMessage("This option is not yet implemented", internalAuthorityList);
    }
}
