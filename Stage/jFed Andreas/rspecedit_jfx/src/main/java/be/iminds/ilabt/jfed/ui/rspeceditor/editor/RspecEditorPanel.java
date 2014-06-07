package be.iminds.ilabt.jfed.ui.rspeceditor.editor;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import be.iminds.ilabt.jfed.ui.rspeceditor.util.AutoEditPanel;
import javafx.beans.property.*;
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
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.Rspec;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecInterface;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecLink;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecNode;
import javafx.util.Callback;

public class RspecEditorPanel extends SplitPane {
    private BooleanProperty editable = new SimpleBooleanProperty(true);
    public boolean isEditable() {
        return editable.get();
    }
    public void setEditable(boolean editable) {
        this.editable.set(editable);
    }
    public BooleanProperty editableProperty() {
        return editable;
    }

    @FXML private BorderPane leftBar;

    @FXML private BorderPane bottomResizablePanel;
    @FXML private BorderPane topResizablePanel;

    @FXML private VBox assignPanel;
    @FXML private ListView<ComponentManagerInfo.ComponentId> assignListView;

    //    @FXML private AnchorPane rspecPanel;
    @FXML private ScrollPane rspecScrollPanel;
    @FXML private AnchorPane rspecAnchorPanel;
    private Group rspecControllerGroup;
//    @FXML private RspecNodeEditPanel rspecNodeEditPanelController;
//    @FXML private RspecNodeLiveEditPanel rspecNodeLiveEditPanelController;
//    @FXML private RspecNodeShowPanel rspecNodeShowPanelController;

    @FXML private Line newLink;
    @FXML private Circle newLinkEnd;

    //    @FXML private AnchorPane rspecNodeEditPanel;
//    @FXML private AnchorPane rspecLinkEditPanel;
    @FXML private StackPane rspecEditStackPane;
    @FXML private ScrollPane rspecEditScrollPane;

    @FXML ListView addNodeListView;

    @FXML StringProperty backgroundStyleClass = new SimpleStringProperty("rspec_background");

    public RspecEditorPanel() {
        if (componentManagerInfos.isEmpty())
            this.componentManagerInfos.add(new ComponentManagerInfo(null, null));

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("RspecEditorPanel.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        assignListView.setCellFactory(new Callback<ListView<ComponentManagerInfo.ComponentId>, ListCell<ComponentManagerInfo.ComponentId>>() {
            @Override
            public ListCell<ComponentManagerInfo.ComponentId> call(ListView<ComponentManagerInfo.ComponentId> list) {
                return new ComponentIdCell();
            }
        });

        leftBar.visibleProperty().bind(editable);
        editable.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (newValue)
                    topResizablePanel.setLeft(leftBar);
                else
                    topResizablePanel.setLeft(null);
            }
        });
        if (!editable.get())
            topResizablePanel.setLeft(null);

        URL initialCssUrl = getClass().getResource("RspecEditorStyle.css");
        if (initialCssUrl == null) throw new RuntimeException("Error loading RspecEditorStyle.css");
        currentCssUrl = initialCssUrl;

        assert currentCssUrl != null;

        //set stylesheet now, and keep up to date as they are added to list
        for (Parent p : styledNodes)
            p.getStylesheets().setAll(currentCssUrl.toExternalForm());
        styledNodes.addListener(new ListChangeListener<Parent>() {
            @Override
            public void onChanged(Change<? extends Parent> change) {
                while (change.next()) {
//                    for (Parent pRemoved : change.getRemoved()) { }
                    for (Parent pAdded : change.getAddedSubList()) {
                        pAdded.getStylesheets().setAll(currentCssUrl.toExternalForm());
                    }
                }
            }
        });

        rspecAnchorPanel = new AnchorPane();
        rspecAnchorPanel.setMinSize(500, 500);
        rspecAnchorPanel.getStyleClass().add("rspec_background");
//        rspecAnchorPanel.getStyleClass().add("rspec_test");

        backgroundStyleClass.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String newStyleClass, String oldStyleClass) {
                rspecScrollPanel.getStyleClass().clear();
                rspecScrollPanel.getStyleClass().add(backgroundStyleClass.get());
                rspecAnchorPanel.getStyleClass().clear();
                rspecAnchorPanel.getStyleClass().add(backgroundStyleClass.get());
                System.out.println("DEBUG: Changed backgroundStyleClass to "+backgroundStyleClass.get());
            }
        });

        rspecControllerGroup = new Group();
        rspecScrollPanel.setContent(rspecAnchorPanel);
        rspecScrollPanel.setMinSize(200, 200);
//        rspecScrollPanel.setContent(rspecControllerGroup);

        //we can't use the scrollpane's panning, as we use drag events ourself
//        rspecScrollPanel.setPannable(true);
        rspecScrollPanel.setPannable(false);

        rspecAnchorPanel.getChildren().add(rspecControllerGroup);

        newLink = new Line(); newLink.getStyleClass().add("rspec_newlink");
        newLinkEnd = new Circle(); newLinkEnd.setRadius(3.5); newLinkEnd.getStyleClass().add("rspec_newlink_end");
        rspecControllerGroup.getChildren().addAll(newLink, newLinkEnd);

        rspecScrollPanel.addEventFilter(ScrollEvent.ANY, new ZoomHandler(rspecAnchorPanel));
//        rspecScrollPanel.addEventFilter(ScrollEvent.ANY, new ZoomHandler(rspecControllerGroup));

        this.getStylesheets().add(currentCssUrl.toExternalForm());

        rspecNodeAutoEditPanel = new AutoEditPanel(RspecNode.class);
        rspecNodeAutoEditPanel.editableProperty().bind(editable);
        rspecEditStackPane.getChildren().add(rspecNodeAutoEditPanel);
        rspecNodeAutoEditPanel.setSelection(selection.selectedRspecNodeProperty());
        rspecNodeAutoEditPanel.setSelectedObjectRemover(new AutoEditPanel.SelectedObjectRemover<RspecNode>() {
            @Override
            public void removeSelected(RspecNode selectedObject) {
                selection.setSelectedRspecNode(null);
                Rspec rspec = shownRspec.get();
                if (rspec != null) {
                    rspec.deleteNode(selectedObject);
                }
            }
        });
        Button doAssignButton = new Button("Assign to fixed node");
        doAssignButton.visibleProperty().bind(editable);
        doAssignButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) { doAssign(); }
        });
        rspecNodeAutoEditPanel.addButton(doAssignButton);
        selection.selectedRspecNodeProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object o1) {
                //cancel assign dialog, if it is open
                assignPanel.setVisible(false);
                if (componentIdMissingPane != null && componentIdMissingPane.getNode() != null)
                    componentIdMissingPane.getNode().setVisible(false);
                //visibility of rspecNodeAutoEditPanel will allways be handled by that panel itself
            }
        });

        rspecLinkAutoEditPanel = new AutoEditPanel(RspecLink.class);
        rspecLinkAutoEditPanel.editableProperty().bind(editable);
        rspecEditStackPane.getChildren().add(rspecLinkAutoEditPanel);
        rspecLinkAutoEditPanel.setSelection(selection.selectedRspecLinkProperty());
        rspecLinkAutoEditPanel.setSelectedObjectRemover(new AutoEditPanel.SelectedObjectRemover<RspecLink>() {
            @Override
            public void removeSelected(RspecLink selectedObject) {
                selection.setSelectedRspecLink(null);
                Rspec rspec = shownRspec.get();
                if (rspec != null) {
                    rspec.deleteLink(selectedObject);
                }
            }
        });


        //set preferred sizes
//        rspecEditStackPane.minHeightProperty().bind(rspecNodeAutoEditPanel.minHeightProperty());
//        rspecEditStackPane.minWidthProperty().bind(rspecNodeAutoEditPanel.minWidthProperty());
//        rspecEditStackPane.prefHeightProperty().bind(rspecNodeAutoEditPanel.prefHeightProperty());
//        rspecEditStackPane.prefWidthProperty().bind(rspecNodeAutoEditPanel.prefWidthProperty());
//        rspecEditScrollPane.prefViewportHeightProperty().bind(rspecNodeAutoEditPanel.prefHeightProperty());
//        rspecEditScrollPane.prefHeightProperty().bind(rspecNodeAutoEditPanel.prefHeightProperty());
//        rspecEditScrollPane.minHeightProperty().bind(rspecNodeAutoEditPanel.minHeightProperty());

        bottomResizablePanel.setMinHeight(100.0);
        topResizablePanel.setMinHeight(100.0);


//        rspecEditScrollPane.setFitToWidth(true); //resize rspecEditStackPane width to fit scrollpane width  (already done in fxml)


        shownRspec.addListener(new ChangeListener<Rspec>() {
            @Override
            public void changed(ObservableValue<? extends Rspec> ov, Rspec oldRspec, Rspec newRspec) {
                onChangeRspec(oldRspec, newRspec);
            }
        });

//        rspecNodeEditPanelController.setSelection(selection);
//        rspecNodeLiveEditPanelController.setSelection(selection);
//        rspecNodeShowPanelController.setSelection(selection);

//        Rspec dummyRspec = Rspec.dummy();
//        shownRspec.setValue(dummyRspec);
//        arrangeOnCircle(dummyRspec, rspecScrollPanel.getWidth(), rspecScrollPanel.getHeight());

        newLink.setVisible(false);
        newLinkEnd.setVisible(false);

//        Node mouseEventSource = rspecScrollPanel;
//        Node mouseEventSource = rspecControllerGroup;
        Node mouseEventSource = rspecAnchorPanel;


        addNodeListView.setCellFactory(new Callback<ListView<ComponentManagerInfo>, ListCell<ComponentManagerInfo>>() {
            @Override
            public ListCell<ComponentManagerInfo> call(ListView<ComponentManagerInfo> list) {
                return new ComponentManagerCell();
            }
        }
        );
        addNodeListView.setItems(componentManagerInfos);

        //TODO add buttons to add nodes from known component managers

        mouseEventSource.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (!editable.get()) return;

                assert fromNode == null || fromLink == null;
                double x = me.getX();
                double y = me.getY();
                dragOrigX = -1.0;
                dragOrigY = -1.0;

                newLink.setVisible(false);
                newLinkEnd.setVisible(false);

                RspecNodeController toNode = findNode(x, y);
                RspecLinkController toLink = findLink(x, y);

                if (fromNode != null) {
                    if (toNode != null) {
                        //no self links via editor
                        if (fromNode != toNode) {
                            RspecLink newRspecLink = new RspecLink(shownRspec.get(), shownRspec.get().nextLinkName());

                            RspecInterface fromIface = new RspecInterface(fromNode.getNode(), newRspecLink, shownRspec.get().nextIfaceName(fromNode.getNode()));
                            RspecInterface toIface = new RspecInterface(toNode.getNode(), newRspecLink, shownRspec.get().nextIfaceName(toNode.getNode()));

                            if (fromNode.getNode().getComponentManagerId() != null &&
                                    !newRspecLink.getComponentManagerUrns().contains(fromNode.getNode().getComponentManagerId()))
                                newRspecLink.getComponentManagerUrns().add(fromNode.getNode().getComponentManagerId());
                            if (toNode.getNode().getComponentManagerId() != null &&
                                    !newRspecLink.getComponentManagerUrns().contains(toNode.getNode().getComponentManagerId()))
                                newRspecLink.getComponentManagerUrns().add(toNode.getNode().getComponentManagerId());

                            if (newRspecLink.getComponentManagerUrns().size() > 1)
                                newRspecLink.getLinkTypes().setAll("gre-tunnel");

                            shownRspec.getValue().getLinks().add(newRspecLink);

                            System.out.println("Added link from " + fromNode.getNode().getId() + " to " + toNode.getNode().getId());

                            fromLink = null;
                            fromNode = null;
                            return;
                        }
                    }

                    if (toLink != null) {
                        //do not add yourself to the same link you are already in
                        if (!fromNode.getNode().getLinks().contains(toLink.getLink()))
                        {
                            RspecInterface fromIface = new RspecInterface(fromNode.getNode(), toLink.getLink(), shownRspec.get().nextIfaceName(fromNode.getNode()));

                            System.out.println("Added node to link: " + fromNode.getNode().getId() + " link=" + toLink.getLink().getId());

                            if (fromNode.getNode().getComponentManagerId() != null &&
                                    !toLink.getLink().getComponentManagerUrns().contains(fromNode.getNode().getComponentManagerId()))
                                toLink.getLink().getComponentManagerUrns().add(fromNode.getNode().getComponentManagerId());

                            if (toLink.getLink().getComponentManagerUrns().size() > 1 && !toLink.getLink().getLinkTypes().contains("gre-tunnel"))
                                toLink.getLink().getLinkTypes().setAll("gre-tunnel");

                            //update controller manually
                            //TODO make this happen automatically in RspecLinkController when node is added to link
                            toLink.getNodes().add(fromNode);
                            toLink.updateLinkLabels();

                            fromLink = null;
                            fromNode = null;
                            return;
                        } else {
                            System.out.println("Cannot add node to link it is already in: node="+fromNode.getNode().getId()+" link="+toLink.getLink().getId());
                        }
                    }
                }
                if (fromLink != null) {
                    if (toNode != null) {
                        //do not add yourself to the same link you are already in
                        if (!toNode.getNode().getLinks().contains(fromLink.getLink()))
                        {
                            RspecInterface toIface = new RspecInterface(toNode.getNode(), fromLink.getLink(), shownRspec.get().nextIfaceName(toNode.getNode()));

                            System.out.println("Added node to link: " + toNode.getNode().getId() + " link=" + fromLink.getLink().getId());

                            if (toNode.getNode().getComponentManagerId() != null &&
                                    !fromLink.getLink().getComponentManagerUrns().contains(toNode.getNode().getComponentManagerId()))
                                fromLink.getLink().getComponentManagerUrns().add(toNode.getNode().getComponentManagerId());

                            if (fromLink.getLink().getComponentManagerUrns().size() > 1 && !fromLink.getLink().getLinkTypes().contains("gre-tunnel"))
                                fromLink.getLink().getLinkTypes().setAll("gre-tunnel");

                            //update controller manually
                            //TODO make this happen automatically in RspecLinkController when node is added to link
                            fromLink.getNodes().add(toNode);
                            fromLink.updateLinkLabels();

                            fromLink = null;
                            fromNode = null;
                            return;
                        } else {
                            System.out.println("Cannot add node to link it is already in: node="+toNode.getNode().getId()+" link="+fromLink.getLink().getId());
                        }
                    }

                    if (toLink != null) {
                        fromLink = null;
                        fromNode = null;
                        //can't add link to link
                        return;
                    }
                }

            }
        });

        mouseEventSource.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (!editable.get()) return;

                assert fromNode == null || fromLink == null;
                double x = me.getX();
                double y = me.getY();
                dragOrigX = x;
                dragOrigY = y;

                if (fromNode != null) {
                    newLink.setStartX(fromNode.getCenterX());
                    newLink.setStartY(fromNode.getCenterY());
                    newLink.setEndX(x);
                    newLink.setEndY(y);
                    newLinkEnd.setLayoutX(x);
                    newLinkEnd.setLayoutY(y);
                    newLinkEnd.setVisible(true);
                    newLink.setVisible(true);
                } else {
                    if (fromLink != null) {
                        newLink.setStartX(centerXOf(fromLink.getCenterComponent()));
                        newLink.setStartY(centerYOf(fromLink.getCenterComponent()));
                        newLink.setEndX(x);
                        newLink.setEndY(y);
                        newLinkEnd.setLayoutX(x);
                        newLinkEnd.setLayoutY(y);
                        newLinkEnd.setVisible(true);
                        newLink.setVisible(true);
                    } else {
                        newLink.setVisible(false);
                        newLinkEnd.setVisible(false);
                    }
                }
            }
        });

        mouseEventSource.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (!editable.get()) return;

                assert fromNode == null || fromLink == null;

                if (!me.isPrimaryButtonDown()) {
                    double x = me.getX();
                    double y = me.getY();

                    fromNode = findNode(x, y);

                    if (fromNode != null) {
                        newLink.setStartX(fromNode.getCenterX());
                        newLink.setStartY(fromNode.getCenterY());
                        newLink.setEndX(x);
                        newLink.setEndY(y);
                        newLinkEnd.setLayoutX(x);
                        newLinkEnd.setLayoutY(y);
                        newLinkEnd.setVisible(true);
                        newLink.setVisible(true);
                        fromLink = null;
                    } else {
                        fromLink = findLink(x, y);
                        if (fromLink != null) {
                            newLink.setStartX(centerXOf(fromLink.getCenterComponent()));
                            newLink.setStartY(centerYOf(fromLink.getCenterComponent()));
                            newLink.setEndX(x);
                            newLink.setEndY(y);
                            newLinkEnd.setLayoutX(x);
                            newLinkEnd.setLayoutY(y);
                            newLinkEnd.setVisible(true);
                            newLink.setVisible(true);
                        } else {
                            newLink.setVisible(false);
                            newLinkEnd.setVisible(false);
                        }
                    }
                }
            }
        });

        mouseEventSource.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (!editable.get()) return;

                boolean panning = false;

//                System.out.println("Dragging");

                if (me.isShiftDown()) {
                    panning = true;
                    fromNode = null;
                    fromLink = null;
                }

                assert fromNode == null || fromLink == null;
                if (!panning && (fromNode != null || fromLink != null)) {
                    double x = me.getX();
                    double y = me.getY();

                    RspecNodeController toNode = findNode(x, y);

                    if (toNode != null) {
                        newLink.setEndX(toNode.getCenterX());
                        newLink.setEndY(toNode.getCenterY());
                        newLinkEnd.setVisible(false);
                    } else {
                        RspecLinkController toLink = findLink(x, y);
                        if (toLink != null) {
                            newLink.setEndX(centerXOf(toLink.getCenterComponent()));
                            newLink.setEndY(centerYOf(toLink.getCenterComponent()));
                            newLinkEnd.setVisible(false);
                        } else {
                            newLinkEnd.setVisible(true);
                            newLink.setEndX(x);
                            newLink.setEndY(y);
                            newLinkEnd.setLayoutX(x);
                            newLinkEnd.setLayoutY(y);
                        }
                    }
                } else {
                    newLink.setVisible(false);
                    newLinkEnd.setVisible(false);

                    panning = true;
                }

                if (panning) {
                    assert dragOrigX != -1.0;
                    assert dragOrigY != -1.0;
                    double dx = me.getX() - dragOrigX;
                    double dy = me.getY() - dragOrigY;
//                    System.out.println("Panning "+dx+" "+dy);
                    //TODO handle scaling
                    rspecScrollPanel.setHvalue(rspecScrollPanel.getHvalue() + dx);
                    rspecScrollPanel.setVvalue(rspecScrollPanel.getVvalue() + dy);
                }
            }
        }
        );
    }

    private RspecSelection selection = new RspecSelection();

    private RspecNodeController fromNode = null;
    private RspecLinkController fromLink = null;

    private AutoEditPanel rspecNodeAutoEditPanel;
    private AutoEditPanel rspecLinkAutoEditPanel;

    private URL currentCssUrl;

    private double dragOrigX = -1.0;
    private double dragOrigY = -1.0;

    private final ObservableList<ComponentManagerInfo> componentManagerInfos = FXCollections.observableArrayList();
    public void setComponentManagerInfo(List<ComponentManagerInfo> componentManagerInfos) {
        //use a temporary list, because otherwise, the list listeners are called before the coloring is done
        List<ComponentManagerInfo> newComponentManagerInfos = new ArrayList<ComponentManagerInfo>();
//        newComponentManagerInfos.add(new ComponentManagerInfo(null, null));  GENERIC CM to add nodes without component_manager_id field filled in
        newComponentManagerInfos.addAll(componentManagerInfos);
        //color component Managers
//        int i = 0;
//        for (ComponentManagerInfo ci : newComponentManagerInfos) {
//            //index is used for css class
//            ci.setIndex(i);
//
//            //color is not currently used
//            switch (i) {
//                case 0 :  { ci.setColor(Color.IVORY); break; }
//                case 1 :  { ci.setColor(Color.PINK); break; }
//                case 2 :  { ci.setColor(Color.HOTPINK); break; }
//                case 3 :  { ci.setColor(Color.BLUE); break; }
//                case 4 :  { ci.setColor(Color.GREEN); break; }
//                case 5 :  { ci.setColor(Color.YELLOW); break; }
//                case 6 :  { ci.setColor(Color.PURPLE); break; }
//                case 7 :  { ci.setColor(Color.ORANGE); break; }
//                case 8 :  { ci.setColor(Color.NAVY); break; }
//                case 9 :  { ci.setColor(Color.SKYBLUE); break; }
//                case 10 : { ci.setColor(Color.AQUA); break; }
//                default:  { ci.setColor(Color.color(Math.random(), Math.random(), Math.random())); break; }
//            }
//            i++;
//        }

        this.componentManagerInfos.setAll(newComponentManagerInfos);

        if (assignPanel.isVisible())
            doAssign();
        if (componentIdMissingPane != null && componentIdMissingPane.getNode().isVisible()) {
            componentIdMissingPane.getNode().setVisible(false);
            doAssign();
        }

        assert this.componentManagerInfos != null;
    }
    public ComponentManagerInfo findComponentManagerInfo(String urn) {
        if (urn == null) return null;
        for (ComponentManagerInfo cmi : componentManagerInfos)
            if (cmi != null && cmi.getUrn() != null && cmi.getUrn().equals(urn))
                return cmi;
        return null;
    }


    public interface ComponentIdMissingPane {
        public Node getNode();
        public abstract void updatePane(String cmUrn);

        void setCancelAction(EventHandler<ActionEvent> handler);
    }
    private ComponentIdMissingPane componentIdMissingPane;

    public void setComponentIdMissingPane(ComponentIdMissingPane componentIdMissingPane) {
        this.componentIdMissingPane = componentIdMissingPane;

        assert componentIdMissingPane != null;
        assert componentIdMissingPane.getNode() != null;
        componentIdMissingPane.setCancelAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                cancelAssign();
            }
        });
        componentIdMissingPane.getNode().setVisible(false);
        rspecEditStackPane.getChildren().add(componentIdMissingPane.getNode());
    }
    public ComponentIdMissingPane getComponentIdMissingPane() {
        return componentIdMissingPane;
    }



    class ComponentManagerCell extends ListCell<ComponentManagerInfo> {
        private Label label;
        private Button button;
        private HBox box;
        private HBox box2;

        private void initBox(final ComponentManagerInfo item) {
            assert item != null;

            if (item.getUrn() == null)
                label = new Label("<GENERIC>");
            else
                label = new Label(item.getUrnPart());
            button = new Button("Add Node");
            button.getStyleClass().add("rspec_node");
            button.getStyleClass().add("component_manager_"+item.getIndex());

            styledNodes.add(button); //will set stylesheet and keep it up to date
            box = new HBox();
            box2 = new HBox();
            box2.setAlignment(Pos.CENTER_RIGHT);
            box2.getChildren().addAll(button);
            box.getChildren().addAll(label, box2);
            HBox.setHgrow(box2, Priority.ALWAYS);

            button.visibleProperty().bind(editable);

            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    onAddNode(item);
                }
            });
        }

        @Override
        public void updateItem(final ComponentManagerInfo item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null)
                setGraphic(null);
            else {
                if (box == null)
                    initBox(item);
                setGraphic(box);
            }
        }
    }

    class ComponentIdCell extends ListCell<ComponentManagerInfo.ComponentId> {
        private Label label;

        @Override
        public void updateItem(final ComponentManagerInfo.ComponentId item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null)
                setGraphic(null);
            else {
                if (label == null) {
                    assert item.getUrn() != null;
                    label = new Label(item.getName() != null ? item.getName() : item.getUrn());
                }
//                System.out.println("Showing item: name=\""+item.getName()+"\" urn=\""+item.getUrn()+"\"");
                assert label != null;

                label.setText(item.getName() != null ? item.getName() : item.getUrn());
                setGraphic(label);
            }
        }
    }

    public void stopLinkMaking() {
        fromNode = null;
        fromLink = null;
        newLink.setVisible(false);
        newLinkEnd.setVisible(false);
    }

    private static double centerXOf(Node n) {
        double x = n.getLayoutX();
        return x + n.getBoundsInLocal().getMinX() + (n.getBoundsInLocal().getWidth() / 2.0);
    }

    private static double centerYOf(Node n) {
        return n.getLayoutY() + n.getBoundsInLocal().getMinY() + (n.getBoundsInLocal().getHeight() / 2.0);
    }

    private Map<RspecNode, RspecNodeController> nodeControllerMap = new HashMap<RspecNode, RspecNodeController>();
    private Map<RspecLink, RspecLinkController> linkControllerMap = new HashMap<RspecLink, RspecLinkController>();

    private ObjectProperty<Rspec> shownRspec = new SimpleObjectProperty<Rspec>(null);
    private ListChangeListener<RspecNode> nodeListChangeListener = new ListChangeListener<RspecNode>() {
        @Override
        public void onChanged(ListChangeListener.Change<? extends RspecNode> change) {
            while (change.next()) {
                for (RspecNode added : change.getAddedSubList()) {
                    addNode(added);
                }
                for (RspecNode removed : change.getRemoved()) {
                    RspecNodeController nc = nodeControllerMap.remove(removed);
                    rspecControllerGroup.getChildren().remove(nc);
                }
            }
        }
    };
    private ListChangeListener<RspecLink> linkListChangeListener = new ListChangeListener<RspecLink>() {
        @Override
        public void onChanged(ListChangeListener.Change<? extends RspecLink> change) {
            while (change.next()) {
                for (RspecLink added : change.getAddedSubList()) {
                    addLink(added);
                }
                for (RspecLink removed : change.getRemoved()) {
                    RspecLinkController lc = linkControllerMap.remove(removed);
//                    rspecControllerGroup.getChildren().remove(lc);
                    lc.removeFromParent();
                }
            }
        }
    };

    public void onChangeRspec(Rspec oldRspec, Rspec newRspec) {
        if (oldRspec == newRspec) return;

        if (oldRspec != null) {
            oldRspec.getNodes().removeListener(nodeListChangeListener);
            oldRspec.getLinks().removeListener(linkListChangeListener);
            for (RspecNodeController nc : nodeControllerMap.values()) {
                rspecControllerGroup.getChildren().remove(nc);
            }
            for (RspecLinkController lc : linkControllerMap.values()) {
                lc.removeFromParent();
            }
            nodeControllerMap.clear();
            linkControllerMap.clear();

            selection.clearAllSelections();
        }

        if (newRspec != null) {
            newRspec.getNodes().addListener(nodeListChangeListener);
            newRspec.getLinks().addListener(linkListChangeListener);
            for (RspecNode n : newRspec.getNodes())
                addNode(n);
            for (RspecLink l : newRspec.getLinks())
                addLink(l);
        }

        //arrangeOnCircle();
    }

    public void addNode(RspecNode node) {
        double origX = node.editorXProperty().get();
        double origY = node.editorYProperty().get();

        RspecNodeController nc = new RspecNodeController(node, currentCssUrl, rspecAnchorPanel, componentManagerInfos);
        nc.setRspecEditorController(this);
        nc.setRspecSelection(selection);
        nodeControllerMap.put(node, nc);

        rspecControllerGroup.getChildren().add(nc);

        if (origX < 0 || origY < 0) {
            //if no position set in rspecNode, use a random position
            double w = rspecScrollPanel.getWidth();
            double h = rspecScrollPanel.getHeight();
            if (w < 100.0) w = 300.0;
            if (h < 100.0) h = 300.0;
            //node at random position close to the center
            //TODO make node dragable from toolbar, or option to add node with right click menu
            double x = (Math.random() * (w/4.0)) + (3.0*w/8.0);
            double y = (Math.random() * (h/4.0)) + (3.0*h/8.0);
            nc.setLayoutX(x);
            nc.setLayoutY(y);
        }
    }

    public void addLink(RspecLink link) {
        RspecLinkController lc = new RspecLinkController(rspecControllerGroup, link, currentCssUrl);
        lc.setRspecSelection(selection);
        linkControllerMap.put(link, lc);

        for (RspecInterface iface : link.getInterfaces()) {
            RspecNode node = iface.getNode();
            RspecNodeController nc = nodeControllerMap.get(node);
            lc.getNodes().add(nc);
        }
        lc.refresh();
    }

    private double dist(double xa, double ya, double xb, double yb) {
        return Math.sqrt( ((xa-xb)*(xa-xb)) + ((ya-yb)*(ya-yb)) );
    }

    private double sqdist(double xa, double ya, double xb, double yb) {
        return ((xa-xb)*(xa-xb)) + ((ya-yb)*(ya-yb));
    }

    private final double maxDist = 40;

    /**
     * returns a node if there is one within range.
     * Returns the closest if multiple, returns NULL if no node close enough.
     */
    public RspecNodeController findNode(double x, double y) {
        RspecNodeController res = null;
        double curDistSq = maxDist*maxDist;
        for (RspecNodeController nc : nodeControllerMap.values()) {
            double nMinX = nc.getLayoutX() + nc.getBoundsInLocal().getMinX();
            double nMinY = nc.getLayoutY() + nc.getBoundsInLocal().getMinY();
            double nMaxX = nc.getLayoutX() + nc.getBoundsInLocal().getMaxX();
            double nMaxY = nc.getLayoutY() + nc.getBoundsInLocal().getMaxY();

            double nx = nMinX;
            double ny = nMinY;
            if (x >= nMinX && x <= nMaxX) nx = x;
            if (y >= nMinY && y <= nMaxY) ny = y;
            if (x > nMaxX) nx = nMaxX;
            if (y > nMaxY) ny = nMaxY;

            double sqD = sqdist(x,y,nx,ny);
            if (sqD <= (curDistSq)) {
                curDistSq = sqD;
                res = nc;
            }
        }
        return res;
    }

    /**
     * returns a link if there is a link center within range.
     * Returns the closest if multiple, returns NULL if no link close enough.
     */
    public RspecLinkController findLink(double x, double y) {
        RspecLinkController res = null;
        double curDistSq = maxDist*maxDist;
        for (RspecLinkController lc : linkControllerMap.values()) {
            Node n = lc.getCenterComponent();
            if (n == null) continue; //this means that the link is not shown anymore
            double nMinX = n.getLayoutX() + n.getBoundsInLocal().getMinX();
            double nMinY = n.getLayoutY() + n.getBoundsInLocal().getMinY();
            double nMaxX = n.getLayoutX() + n.getBoundsInLocal().getMaxX();
            double nMaxY = n.getLayoutY() + n.getBoundsInLocal().getMaxY();

            double nx = nMinX;
            double ny = nMinY;
            if (x >= nMinX && x <= nMaxX) nx = x;
            if (y >= nMinY && y <= nMaxY) ny = y;
            if (x > nMaxX) nx = nMaxX;
            if (y > nMaxY) ny = nMaxY;

            double sqD = sqdist(x,y,nx,ny);
            if (sqD <= (curDistSq)) {
                curDistSq = sqD;
                res = lc;
            }
        }
        return res;
    }

    private int style = 0;
    public void switchStyle() {
        style++;
        if (style > 1) style = 0;
        switch (style) {
            case 0: { changeStyleSheet("RspecEditorStyle.css"); break;  }
            case 1: { changeStyleSheet("RspecEditorStyle2.css"); break;  }
        }
    }

    private ObservableList<Parent> styledNodes = FXCollections.observableArrayList();

    public void changeStyleSheet(String cssName) {
        URL newCssUrl = getClass().getResource(cssName);
        currentCssUrl = newCssUrl;
        if (currentCssUrl == null) throw new RuntimeException("Error loading CSS: \""+cssName+"\"");
        assert currentCssUrl != null;
        String cssUrlString = currentCssUrl.toExternalForm();
//        String cssUrlString = JavaFxStyleSheetHelper.getStyleSheetURL(getClass(), cssName);
//        assert cssUrlString != null;

        for (RspecNodeController nc : nodeControllerMap.values())
            nc.changeStyleSheet(cssName);

        for (RspecLinkController lc : linkControllerMap.values())
            lc.changeStyleSheet(cssName);

        rspecScrollPanel.getStylesheets().setAll(cssUrlString);

        for (Parent p : styledNodes)
            p.getStylesheets().setAll(cssUrlString);
    }

    public void onAddNode(ComponentManagerInfo componentManagerInfo) {
        if (shownRspec.get() == null)
            shownRspec.set(new Rspec());

        assert shownRspec.get() != null;

        RspecNode newRspecNode = new RspecNode(shownRspec.get().nextNodeName());
        if (componentManagerInfo.getUrn() != null) {
            newRspecNode.setComponentManagerId(componentManagerInfo.getUrn());
            newRspecNode.setSliverTypeName(componentManagerInfo.getSliverTypes().get(0));
        }
        shownRspec.getValue().getNodes().add(newRspecNode);
    }

    public ObjectProperty<Rspec> shownRspecProperty() {
        return shownRspec;
    }

    public void arrangeOnCircle() {
        if (shownRspec.get() != null)
            arrangeOnCircle(shownRspec.get(), rspecScrollPanel.getWidth(), rspecScrollPanel.getHeight());

        //TODO find out what is going wrong so this can be removed:
        //for some reason, the auto refresh is not activating properly
        for (RspecLinkController lc : linkControllerMap.values())
            lc.refresh();
    }
    public static void arrangeOnCircle(Rspec rspec, double screenWidth, double screenHeight) {
        double w = screenWidth;
        double h = screenHeight;

        if (w < 100.0) w = 300.0;
        if (h < 100.0) h = 300.0;

        int size =  rspec.getNodes().size();
        double radiusW = w / 3.0; //diameter of 2/3 width
        double radiusH = h / 3.0; //diameter of 2/3 height

        double centerX = w / 2.0;
        double centerY = h / 2.0;
        double angle = 0.0;
        double step = step = (2*Math.PI) / size;
        for (RspecNode n :  rspec.getNodes()) {
            double x = (Math.cos(angle) * radiusW) + centerX;
            double y = (Math.sin(angle) * radiusH) + centerY;

            n.setEditorX(x);
            n.setEditorY(y);

            angle += step;
        }
    }

    private static final double MAX_SCALE = 5.0d;
    private static final double MIN_SCALE = 0.25d;



    private class ZoomHandler implements EventHandler<ScrollEvent> {

        private Node nodeToZoom;

        private ZoomHandler(Node nodeToZoom) {
            this.nodeToZoom = nodeToZoom;
        }

        @Override
        public void handle(ScrollEvent scrollEvent) {
            if (scrollEvent.isControlDown()) {
                final double scale = calculateScale(scrollEvent);
                nodeToZoom.setScaleX(scale);
                nodeToZoom.setScaleY(scale);
                scrollEvent.consume();
            }
        }

        private double calculateScale(ScrollEvent scrollEvent) {
            double scale = nodeToZoom.getScaleX() + scrollEvent.getDeltaY() / 100;

            if (scale <= MIN_SCALE)
                scale = MIN_SCALE;
            else if (scale >= MAX_SCALE)
                scale = MAX_SCALE;
            return scale;
        }
    }

    public void zoomIn() {
        double scale = rspecAnchorPanel.getScaleX();
        scale *= 1.5;
        if (scale <= MIN_SCALE)
            scale = MIN_SCALE;
        else if (scale >= MAX_SCALE)
            scale = MAX_SCALE;
        rspecAnchorPanel.setScaleX(scale);
        rspecAnchorPanel.setScaleY(scale);
    }

    public void zoomOut() {
        double scale = rspecAnchorPanel.getScaleX();
        scale /= 1.5;
        if (scale <= MIN_SCALE)
            scale = MIN_SCALE;
        else if (scale >= MAX_SCALE)
            scale = MAX_SCALE;
        rspecAnchorPanel.setScaleX(scale);
        rspecAnchorPanel.setScaleY(scale);
    }

    public void resetZoom() {
        rspecAnchorPanel.setScaleX(1.0);
        rspecAnchorPanel.setScaleY(1.0);
    }




    public void doAssign() {
        RspecNode selectedNode = selection.selectedRspecNodeProperty().get();
        if (selectedNode != null) {
            rspecNodeAutoEditPanel.setVisible(false);

            List<ComponentManagerInfo.ComponentId> componentIds = new ArrayList<ComponentManagerInfo.ComponentId>();
            String cmUrn = selectedNode.getComponentManagerId();
            if (cmUrn != null) {
                ComponentManagerInfo cmi = findComponentManagerInfo(cmUrn);
                if (cmi != null)
                    componentIds.addAll(cmi.getComponentIds());
            } else {
                for (ComponentManagerInfo cmi : componentManagerInfos)
                    componentIds.addAll(cmi.getComponentIds());
            }
            ObservableList<ComponentManagerInfo.ComponentId> r = FXCollections.observableArrayList(componentIds);
            System.out.println("doAssign selectedNode="+selectedNode.getId()+" nodes="+r.size()+" cmUrn="+cmUrn);

            if (r.isEmpty()) {
                assignPanel.setVisible(false);
                componentIdMissingPane.getNode().setVisible(true);

                componentIdMissingPane.updatePane(cmUrn);
            } else {
                assignPanel.setVisible(true);
                componentIdMissingPane.getNode().setVisible(false);

                //sort items by name or urn
                FXCollections.sort(r, new Comparator<ComponentManagerInfo.ComponentId>() {
                    @Override
                    public int compare(ComponentManagerInfo.ComponentId o1, ComponentManagerInfo.ComponentId o2) {
                        String n1 = o1.getName() == null ? o1.getUrn() : o1.getName();
                        String n2 = o2.getName() == null ? o2.getUrn() : o2.getName();
                        return n1.compareTo(n2);
                    }
                });

                assignListView.setItems(r);
            }
        } else {
            //nothing to do (but this shouldn't happen)
        }
    }

    public void okAssign() {
        RspecNode selectedNode = selection.selectedRspecNodeProperty().get();
        if (selectedNode != null) {
            ComponentManagerInfo.ComponentId componentId = assignListView.getSelectionModel().getSelectedItem();
            if (componentId != null) {
                selectedNode.setComponentId(componentId.getUrn());
                selectedNode.setComponentName(componentId.getName());
                RspecNode adNode = componentId.getAdvertisementRspecNode();
                if (adNode != null) {
                    //copy location
                    selectedNode.setLocation(adNode.getLocation());

                    //copy hardwaretypes
                    selectedNode.getHardwareTypes().addAll(adNode.getHardwareTypes());
                }
            }
        }

        cancelAssign();
    }

    public void cancelAssign() {
        rspecNodeAutoEditPanel.setVisible(true);
        assignPanel.setVisible(false);
        componentIdMissingPane.getNode().setVisible(false);
    }


    public StringProperty backgroundStyleClassProperty() {
        return backgroundStyleClass;
    }
}
