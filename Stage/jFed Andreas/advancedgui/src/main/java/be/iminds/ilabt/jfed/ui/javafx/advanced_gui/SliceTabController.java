/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.iminds.ilabt.jfed.ui.javafx.advanced_gui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import be.iminds.ilabt.jfed.highlevel.controller.HighLevelController;
import be.iminds.ilabt.jfed.highlevel.controller.TaskThread;
import be.iminds.ilabt.jfed.highlevel.model.*;
import be.iminds.ilabt.jfed.highlevel.stitcher.ParallelStitcher;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.ui.javafx.stitching_gui.CreateSliverStatusPanel;
import be.iminds.ilabt.jfed.ui.rspeceditor.editor.ComponentManagerInfo;
import be.iminds.ilabt.jfed.ui.rspeceditor.editor.RspecEditorPanel;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.Rspec;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecNode;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/** Note: A Slice Tab is specific for a slice. The slice always stays the same. Tab gets removed with slice if needed. */
public class SliceTabController extends Tab implements Initializable {
    private final TabPane parentTabPane;

    @FXML private TabPane sliceTabs;
    @FXML private Tab infoTab;
//    @FXML private Tab rspecTab;
//    @FXML private Tab statusTab;

    //    @FXML private TabPane rspecTabPane;
    @FXML private Tab rspecRequestTab;
    @FXML private Tab rspecManifestTab;

    @FXML private TextField nameField;
    @FXML private TextField urnField;
    @FXML private TextField uuidField;
    @FXML private TextField creatorUuidField;
    @FXML private TextField creatorUrnField;
    @FXML private TextField expirationDateField;
    @FXML private TextArea gidField;
    @FXML private TextArea credentialField;

    @FXML private RspecEditorPanel rspecRequestEditorPanel;
    @FXML private RspecEditorPanel rspecManifestEditorPanel;

    @FXML private Button rspecEditorGetManifestButton;
    @FXML private Label sliceStatusLabel;
    @FXML private Label sliceStatusLabel2;

    @FXML private ListView<Sliver> sliverListView;
    @FXML private ListView<Sliver> sliverListView2;


    @FXML private ListView<AuthorityInfo> authorityListView; //TODO init

    public SliceController sliceController;

    public SliceTabController(TabPane parentTabPane, SliceController sliceController) {
        this.parentTabPane = parentTabPane;
        this.sliceController = sliceController;

        URL location = getClass().getResource("SliceTab.fxml");
        assert location != null;
        FXMLLoader fxmlLoader = new FXMLLoader(location);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            Tab selfTab = (Tab) fxmlLoader.load();
            assert this == selfTab;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private class MyComponentIdMissingPane extends VBox implements RspecEditorPanel.ComponentIdMissingPane {
        private Label infoLabel;
        private Button fetchAdvertisementButton;
        private Button cancelButton;

        private AuthorityInfo authorityInfo;

        public MyComponentIdMissingPane() {
            infoLabel = new Label("No specific components known at this moment. Try fetching the Advertisement.");
            fetchAdvertisementButton = new Button("Fetch Advertisement");
            cancelButton = new Button("Cancel");
            fetchAdvertisementButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    if (authorityInfo == null) return;
                    TaskThread.getInstance().addTask(
                            sliceController.getSlice().getEasyModel().getHighLevelController().getAdvertisement(authorityInfo.getGeniAuthority())
                    );
                }
            });
            getChildren().addAll(infoLabel, fetchAdvertisementButton, cancelButton);

            setAlignment(Pos.TOP_CENTER);
            setMargin(infoLabel, new Insets(50.0/*top*/, 0.0/*right*/, 10.0/*bottom*/, 0.0/*left*/));
            setMargin(cancelButton, new Insets(50.0/*top*/, 0.0/*right*/, 0.0/*bottom*/, 0.0/*left*/));
        }

        @Override public Node getNode() { return this; }

        @Override
        public void updatePane(String cmUrn) {
            if (cmUrn == null) return;
            authorityInfo = sliceController.getSlice().getEasyModel().getAuthorityList().get(cmUrn);
            if (authorityInfo == null) {
                System.err.println("ERROR (will ignore): could not find authority for URN: " + cmUrn);
                fetchAdvertisementButton.setVisible(false);
                return;
            }
            fetchAdvertisementButton.setVisible(true);
        }

        @Override
        public void setCancelAction(EventHandler<ActionEvent> handler) {
            cancelButton.setOnAction(handler);
        }
    };

    private class AuthorityInfoCell extends ListCell<AuthorityInfo> {
        private Label label = new Label();
        @Override
        public void updateItem(final AuthorityInfo item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null)
                setGraphic(null);
            else {
                label.setText(item.getGeniAuthority().getName());
                setGraphic(label);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        final Slice slice = sliceController.getSlice();

        //TODO since slice name is read only, we can better set instead of bind...
        nameField.textProperty().bind(slice.nameProperty());
        urnField.textProperty().bind(slice.urnProperty());
        uuidField.textProperty().bind(slice.uuidProperty());
        creatorUrnField.textProperty().bind(slice.creator_urnProperty());
        creatorUuidField.textProperty().bind(slice.creator_uuidProperty());
        expirationDateField.textProperty().bind(Bindings.convert(slice.expirationDateProperty()));
        gidField.textProperty().bind(Bindings.convert(slice.gidProperty()));
        credentialField.textProperty().bind(Bindings.convert(slice.credentialProperty()));
        textProperty().bind(slice.nameProperty());

        Callback<ListView<Sliver>, ListCell<Sliver>> sliverListViewCellFactory = new Callback<ListView<Sliver>, ListCell<Sliver>>() {
            @Override
            public ListCell<Sliver> call(ListView<Sliver> list) {
                return new SliverOverviewListCell();
            }
        };
        sliverListView.setCellFactory(sliverListViewCellFactory);
        sliverListView2.setCellFactory(sliverListViewCellFactory);

        sliverListView.setItems(slice.getSlivers());
        sliverListView2.setItems(slice.getSlivers());

        //auto change height to exact needed for all items
        slice.getSlivers().addListener(new ListChangeListener<Sliver>() {
            @Override
            public void onChanged(Change<? extends Sliver> change) {
                System.out.println("slice.getSlivers() onChanged slice.getSlivers().size()="+slice.getSlivers().size());
                if (slice.getSlivers().isEmpty()) {
                    sliverListView2.setPrefHeight(20);
                    sliverListView2.setMaxHeight(20);
                    return;
                }
//                Sliver first = slice.getSlivers().get(0);
//                SliverOverviewListCell controller = sliceController.getSliverController(first);
//                assert(controller != null);
                //there is no clean way of doing this in JavaFX at the moment
                double firstItemHeight = 24;//TODO: figure out how to get it from the overviewNode (not straightForward)  ;controller.getOverviewNode().prefHeight(-1);
                double totalItemHeight = (slice.getSlivers().size() * firstItemHeight) + 2/*top and bottom pixel*/ + 20/*vertical scrollbar*/;

                System.out.println("slice.getSlivers() onChanged firstItemHeight="+firstItemHeight+" totalItemHeight="+totalItemHeight);

                sliverListView2.setPrefHeight(totalItemHeight);
                sliverListView2.setMaxHeight(totalItemHeight);
                sliverListView.setPrefHeight(totalItemHeight);
                sliverListView.setMaxHeight(totalItemHeight);
            }
        });
        sliverListView2.setPrefHeight(20);
        sliverListView2.setMaxHeight(20);
        sliverListView.setPrefHeight(20);
        sliverListView.setMaxHeight(20);

        slice.requestRspecProperty().bindBidirectional(rspecRequestEditorPanel.shownRspecProperty());

        slice.manifestRspecProperty().bindBidirectional(rspecManifestEditorPanel.shownRspecProperty());

        rspecManifestEditorPanel.setEditable(false);


        //put all known authorities as addable in editor
        final ObservableList<String> componentManagerUrns = FXCollections.observableArrayList();
        for (AuthorityInfo ai : slice.getEasyModel().getAuthorityList().authorityInfosProperty()) {
            componentManagerUrns.add(ai.getGeniAuthority().getUrn());
        }
        slice.editorComponentManagersProperty().set(componentManagerUrns);

        //keep list up to date automatically
        slice.getEasyModel().getAuthorityList().authorityInfosProperty().addListener(new ListChangeListener<AuthorityInfo>() {
            @Override
            public void onChanged(Change<? extends AuthorityInfo> change) {
                while (change.next()) {
                    for (AuthorityInfo ai : change.getRemoved())
                        componentManagerUrns.remove(ai.getGeniAuthority().getUrn());
                    for (AuthorityInfo ai : change.getAddedSubList())
                        componentManagerUrns.add(ai.getGeniAuthority().getUrn());
                }

                //put all known authorities as addable in editor
                ObservableList<String> componentManagerUrns = FXCollections.observableArrayList();
                for (AuthorityInfo ai : slice.getEasyModel().getAuthorityList().authorityInfosProperty()) {
                    componentManagerUrns.add(ai.getGeniAuthority().getUrn());
                }
                slice.editorComponentManagersProperty().set(componentManagerUrns);
            }
        });


        //make background of editor automatically reflect status of slice
        slice.statusProperty().addListener(new ChangeListener<Status>() {
            @Override
            public void changed(ObservableValue<? extends Status> observableValue, Status oldStatus, Status newStatus) {
                System.out.println("DEBUG: SliceTabController slice.statusProperty() changed to "+newStatus);
                switch (newStatus) {
                    case UNKNOWN: { rspecManifestEditorPanel.backgroundStyleClassProperty().set("rspec_background_changing"); break; }
                    case UNINITIALISED:
                    case UNALLOCATED: { rspecManifestEditorPanel.backgroundStyleClassProperty().set("rspec_background"); break; }
                    case CHANGING: { rspecManifestEditorPanel.backgroundStyleClassProperty().set("rspec_background_changing"); break; }
                    case FAIL: { rspecManifestEditorPanel.backgroundStyleClassProperty().set("rspec_background_fail"); break; }
                    case READY: { rspecManifestEditorPanel.backgroundStyleClassProperty().set("rspec_background_ready"); break; }
                }

                sliceStatusLabel.textProperty().set(newStatus.name());
                sliceStatusLabel2.textProperty().set(newStatus.name());
            }
        });

        //link slice properties to the correct rspecEditorPanel (and keep it up to date automatically)
        slice.manifestComponentManagersProperty().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> change) {
//                System.out.println("+++ DEBUG +++ Updating list of component managers for manifest editor. cms="+slice.manifestComponentManagersProperty().get());
                sliceComponentManagerInfoToRspecEditor(slice, slice.manifestComponentManagersProperty().get(), rspecManifestEditorPanel, false);
            }
        });
        slice.editorComponentManagersProperty().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> change) {
//                System.out.println("+++ DEBUG +++ Updating list of component managers for request editor. cms="+slice.editorComponentManagersProperty().get());
                sliceComponentManagerInfoToRspecEditor(slice, slice.editorComponentManagersProperty().get(), rspecRequestEditorPanel, true);
            }
        });

//        System.out.println("+++ DEBUG +++ Initialising list of component managers for manifest editor. cms="+slice.manifestComponentManagersProperty().get());
        sliceComponentManagerInfoToRspecEditor(slice, slice.manifestComponentManagersProperty().get(), rspecManifestEditorPanel, false);
//        System.out.println("+++ DEBUG +++ Initialising list of component managers for request editor. cms="+slice.editorComponentManagersProperty().get());
        sliceComponentManagerInfoToRspecEditor(slice, slice.editorComponentManagersProperty().get(), rspecRequestEditorPanel, true);

        assert sliceTabs != null;
        assert rspecManifestTab != null;
//        assert rspecTab != null;
//        sliceTabs.getSelectionModel().select(rspecTab);
//        sliceTabs.getSelectionModel().select(rspecRequestTab);
        sliceTabs.getSelectionModel().select(infoTab);

        rspecRequestEditorPanel.setComponentIdMissingPane(new MyComponentIdMissingPane());

        authorityListView.setItems(slice.getEasyModel().getAuthorityList().authorityInfosProperty());
        authorityListView.setCellFactory(new Callback<ListView<AuthorityInfo>, ListCell<AuthorityInfo>>() {
            @Override
            public ListCell<AuthorityInfo> call(ListView<AuthorityInfo> authorityInfoListView) {
                return new AuthorityInfoCell();
            }
        });
    }

    private final List<AuthorityInfo> authorityInfoBeingListenedTo = new ArrayList<AuthorityInfo>();
    ChangeListener<RSpecInfo> authorityInfoListener = new ChangeListener<RSpecInfo>() {
        @Override
        public void changed(ObservableValue<? extends RSpecInfo> observableValue, RSpecInfo oldRSpecInfo, RSpecInfo newRSpecInfo) {
            final Slice slice = sliceController.getSlice();
            sliceComponentManagerInfoToRspecEditor(slice, slice.editorComponentManagersProperty().get(), rspecRequestEditorPanel, true);
        }
    };

    private void sliceComponentManagerInfoToRspecEditor(Slice slice, List<String> componentManagerUrns, final RspecEditorPanel rspecEditorPanel, boolean refreshOnAdvertisementRspec) {
        List<AuthorityInfo> authorityInfoToIgnore = new ArrayList<AuthorityInfo>(authorityInfoBeingListenedTo);

        List<ComponentManagerInfo> res = new ArrayList<ComponentManagerInfo>();
        for (String cm : componentManagerUrns) {
            AuthorityInfo ai = slice.getEasyModel().getAuthorityList().get(cm);
            if (ai == null) continue;
            boolean removed = authorityInfoToIgnore.remove(ai);
            if (refreshOnAdvertisementRspec && !removed) {
                authorityInfoBeingListenedTo.add(ai);
                ai.allAdvertisementRspecProperty().addListener(authorityInfoListener);
                ai.availableAdvertisementRspecProperty().addListener(authorityInfoListener);
            }
            SfaAuthority auth = ai.getGeniAuthority();
            if (auth == null) {
                System.err.println("WARNING: Resolve Slice returned unknown component manager ");
            } else {
                ComponentManagerInfo componentManagerInfo = new ComponentManagerInfo(auth.getUrn(), auth.getNameForUrn());
                if (auth.getType() != null && auth.getType().equals("planetlab")) {
                    List<String> sliverTypes = new ArrayList<String>();
                    sliverTypes.add("plab-vserver");
                    componentManagerInfo.setSliverTypes(sliverTypes);
                }
                RSpecInfo rspec = ai.getAllAdvertisementRspec();
                if (rspec == null)
                    rspec = ai.getAvailableAdvertisementRspec();
                if (rspec != null) {
                    List<RspecNode> nodes = rspec.getRSpec().getNodes();
//                    System.out.println("+++ DEBUG +++ sliceComponentManagerInfoToRspecEditor +++ adding "+nodes.size()+" nodes to componentManagerInfo for auth "+auth.getName());
                    for (RspecNode node : nodes) {
                        componentManagerInfo.getComponentIds().add(
                                new ComponentManagerInfo.ComponentId(
                                        node.getComponentId(),
                                        node.getComponentName(),
                                        node)
                        );
                    }
                } else {
//                    System.out.println("+++ DEBUG +++ sliceComponentManagerInfoToRspecEditor +++ no nodes to add to componentManagerInfo for auth "+auth.getName());
                }
                componentManagerInfo.setIndex(ai.getIndex());
                res.add(componentManagerInfo);
            }
        }
        rspecEditorPanel.setComponentManagerInfo(res);

        if (refreshOnAdvertisementRspec)
            for (AuthorityInfo ai : authorityInfoToIgnore) {
                authorityInfoBeingListenedTo.remove(ai);
                ai.allAdvertisementRspecProperty().removeListener(authorityInfoListener);
                ai.availableAdvertisementRspecProperty().removeListener(authorityInfoListener);
            }
    }

    public void focusTab() {
        if (!parentTabPane.getTabs().contains(this))
            parentTabPane.getTabs().add(this);
        parentTabPane.getSelectionModel().select(this);
    }

    public void resolveSlice() {
        TaskThread.getInstance().addTask(
                sliceController.getSlice().getEasyModel().getHighLevelController().resolveSlice(sliceController.getSlice())
        );
    }

    public void statusSlice() {
        TaskThread.getInstance().addTask(
                sliceController.getSlice().getEasyModel().getHighLevelController().getSliceStatus(sliceController.getSlice())
        );
    }

    public void statusAndManifestSlice() {
        TaskThread.getInstance().addTasks(
                sliceController.getSlice().getEasyModel().getHighLevelController().getSliceStatusAndManifest(sliceController.getSlice())
        );
    }

    public void createSlivers() {
        rspecManifestEditorPanel.backgroundStyleClassProperty().set("rspec_background_changing");

        Rspec rspec = sliceController.getSlice().requestRspecProperty().get();
        if (HighLevelController.needsStitching(rspec)) {
            ParallelStitcher parallelStitcher = new ParallelStitcher(sliceController.getSlice().getEasyModel(), rspec.toGeni3RequestRspec());
            CreateSliverStatusPanel.showStitchingOverview(sliceController.getSlice().getEasyModel(), parallelStitcher);
            parallelStitcher.setSlice(sliceController.getSlice());
            parallelStitcher.start();
        } else {
            TaskThread.getInstance().addTasks(
                    sliceController.getSlice().getEasyModel().getHighLevelController().createSliversNoStitching(sliceController.getSlice())
            );
        }

        sliceTabs.getSelectionModel().select(rspecManifestTab);
    }

    public void getManifest() {
        TaskThread.getInstance().addTask(
                sliceController.getSlice().getEasyModel().getHighLevelController().getManifest(sliceController.getSlice())
        );
    }

    public void manifestToRequestEditor() {
        Slice slice = sliceController.getSlice();
        slice.requestRspecProperty().set(slice.manifestRspecProperty().get());
//        rspecTabPane.getSelectionModel().select(rspecRequestTab);
        sliceTabs.getSelectionModel().select(rspecRequestTab);
    }

    public void deleteSlice() {
        TaskThread.getInstance().addTask(
                sliceController.getSlice().getEasyModel().getHighLevelController().deleteSlice(sliceController.getSlice())
        );
    }

    public void checkAuthorityForSlivers() {
        AuthorityInfo authorityInfo = authorityListView.getSelectionModel().getSelectedItem();
        if (authorityInfo != null) {

            TaskThread.getInstance().addTask(
                    sliceController.getSlice().getEasyModel().getHighLevelController().getSliceStatus(sliceController.getSlice(), authorityInfo.getGeniAuthority())
            );
            TaskThread.getInstance().addTask(
                    sliceController.getSlice().getEasyModel().getHighLevelController().getSliceStatus(sliceController.getSlice(), authorityInfo.getGeniAuthority())
            );
        }
    }
}
