package be.iminds.ilabt.jfed.ui.rspeceditor.editor;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecInterface;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecNode;
import be.iminds.ilabt.jfed.ui.rspeceditor.util.JavaFxStyleSheetHelper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecLink;

/**
 * Visualization of the link/LAN between 1 or more nodes.
 * 
 * This never adds the nodes.
 * This never edits or moves the nodes.
 */
public class RspecLinkController implements ChangeListener<Number> {
    private Button centralNode;
    
    private Group/*Pane*/ parent;
    
    private final RspecLink link;

    private final ListChangeListener<Node> nodeAddListener;
    private final ListChangeListener<RspecLink> removeListener;

    private final ObservableList<RspecNodeController> nodes = FXCollections.observableArrayList();
    private List<LinkController> links;
    private List<Button> linkLabels;

    private URL currentCssUrl;

    public RspecLinkController(Group/*Pane*/ parent, final RspecLink link, URL cssUrl) {
        this.currentCssUrl = cssUrl;

        this.link = link;
        
        this.parent = parent;
        links = new ArrayList<LinkController>();
        linkLabels = new ArrayList<Button>();

        centralNode = new Button("");
        centralNode.getStylesheets().add(currentCssUrl.toExternalForm());

        centralNode.getStyleClass().add("rspec_switchlabel");
        centralNode.setVisible(false);
        centralNode.setOnAction(new EventHandler() {
            @Override
            public void handle(Event t) {
                onClickedCentralNode();
            }
        });
        
        centralNode.setMaxHeight(10);
        //centralNode.setMaxWidth(10);
        parent.getChildren().add(centralNode);

        nodeAddListener = new ListChangeListener<Node>() {
                    @Override
                    public void onChanged(ListChangeListener.Change<? extends Node> change) {
                        while (change.next()) {
                            for (Node c : change.getAddedSubList())
                                if (c instanceof RspecNodeController) {
                                    RspecNodeController rn = (RspecNodeController) c;
                                    addNode(rn);
                                }
                        }
                    }
                };
        nodes.addListener(nodeAddListener);

        link.getInterfaces().addListener(new ListChangeListener<RspecInterface>() {
            @Override
            public void onChanged(Change<? extends RspecInterface> change) {
                while (change.next()) {
                    for (RspecInterface iface : change.getRemoved())
                        removeRspecIfaceViewController(iface);
                }
            }
        });

        removeListener = new ListChangeListener<RspecLink>() {
            @Override
            public void onChanged(Change<? extends RspecLink> change) {
                while (change.next()) {
                    for (RspecLink removedLink : change.getAddedSubList())
                        if (removedLink == link) {
                            assert link.getInterfaces().isEmpty() : "Before removing a link from the RSpec, all its interfaces must be removed";
                            removeFromParent();
                        }
                }
            }
        };
        link.getRspec().getLinks().addListener(removeListener);
        
        centralNode.textProperty().bind(link.idProperty());
    }

    public void removeFromParent() {
        if (centralNode == null) return;

        //break down everything
        nodes.removeListener(nodeAddListener);
        link.getRspec().getLinks().removeListener(removeListener);

        setRspecSelection(null);

        for (LinkController l : links) {
            l.setNodeA(null);
            l.setNodeB(null);
            parent.getChildren().remove(l);
        }

        for (Button b : linkLabels) {
            b.setOnAction(null);
            b.boundsInLocalProperty().removeListener(refreshOnChange);
            parent.getChildren().remove(b);
        }

        nodes.clear();
        links.clear();
        linkLabels.clear();

        assert nodes.isEmpty();
        assert links.isEmpty();
        assert linkLabels.isEmpty();

        centralNode.setOnAction(null);
        centralNode.setVisible(false);
        parent.getChildren().remove(centralNode);

        centralNode = null;
    }
    
    public void changeStyleSheet(String cssName) {
        assert cssName != null;
        currentCssUrl = getClass().getResource(cssName);
        String cssUrlString = currentCssUrl.toExternalForm();

        centralNode.getStylesheets().setAll(cssUrlString);
        for (Button linkLabel : linkLabels)
            linkLabel.getStylesheets().setAll(cssUrlString);
        for (LinkController link : links)
            link.changeStyleSheet(cssUrlString);
    }
    
    private int centerX, centerY;
    
    private void updateCenter() {
        if (nodes.isEmpty()) return;

        centerX = 0;
        centerY = 0;
        for (RspecNodeController r : nodes) {
            centerX += r.getLayoutX() + r.getLayoutBounds().getMinX() + (r.getLayoutBounds().getWidth()  / 2.0);
            centerY += r.getLayoutY() + r.getLayoutBounds().getMinY() + (r.getLayoutBounds().getHeight() / 2.0);
        }
        centerX /= nodes.size();
        centerY /= nodes.size();
        
        centralNode.setLayoutX((centerX + centralNode.getBoundsInLocal().getMinX()) - (centralNode.getBoundsInLocal().getWidth()  / 2.0));
        centralNode.setLayoutY((centerY + centralNode.getBoundsInLocal().getMinY()) - (centralNode.getBoundsInLocal().getHeight() / 2.0));
    }

    
    public ObservableList<RspecNodeController> getNodes() {
        return nodes;
    }

    public void updateLinkLabels() {
        if (nodes.size() > 2) {
            for (int i = 0; i < links.size(); i++) {
                LinkController l = links.get(i);

                Button b = linkLabels.get(i);
                b.setVisible(true);
                b.setLayoutX((l.getCenterX() + b.getBoundsInLocal().getMinX()) - (b.getBoundsInLocal().getWidth()  / 2.0));
                b.setLayoutY((l.getCenterY() + b.getBoundsInLocal().getMinY()) - (b.getBoundsInLocal().getHeight() / 2.0));
            }
        } else {
            for (int i = 0; i < linkLabels.size(); i++) {
                Button b = linkLabels.get(i);
                b.setVisible(false);
            }
        }
    }

    private ChangeListener refreshOnChange = new ChangeListener<Bounds>() {
                @Override
                public void changed(ObservableValue<? extends Bounds> ov, Bounds t, Bounds t1) {
                    refresh();
                }
            };

    public void addNode(RspecNodeController nodeController) {
        final RspecNode node = nodeController.getNode();

        nodeController.layoutXProperty().addListener(this);
        nodeController.layoutYProperty().addListener(this);
        nodeController.boundsInLocalProperty().addListener(refreshOnChange);

//        System.out.println("LinkController("+this.getLink().getId()+").addNode("+nodeController.getNode().getId()+") links.size()=="+links.size()+" nodes.size()=="+nodes.size());
        //first check if no duplicate nodecrontrollers exist
        RspecNodeController foundNodeController = null;
        for (RspecNodeController nc : nodes) {
            //check if same in 3 ways
            boolean same = nc == nodeController ||
                           nc.getNode() == nodeController.getNode() ||
                           nc.getNode().getId().equals(nodeController.getNode().getId());

            if (foundNodeController != null && same)
                throw new RuntimeException("Implementation ERROR: NodeController for \""+nodeController.getNode().getId()+"\" already added.");
            if (foundNodeController == null && same) {
                foundNodeController = nc;
            }
        }

        int linknr = nodes.size() - 1;
        while (links.size() < nodes.size()) {
            LinkController newLink = new LinkController();
            int newLinkIndex = links.size();
            links.add(newLink);
            parent.getChildren().add(newLink);
            newLink.setNodeA(nodes.get(newLinkIndex));
            
            Button linkLabel = new Button("X");
            if (currentCssUrl == null) throw new RuntimeException("Error loading RspecEditorStyle.css");
            linkLabel.getStylesheets().add(currentCssUrl.toExternalForm());
            linkLabel.getStyleClass().add("rspec_linklabel");
            parent.getChildren().add(linkLabel);
            linkLabel.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    RspecInterface iface = link.getInterfaceForNode(node);
                    assert iface != null;
                    iface.delete();
                }
            });
            linkLabels.add(linkLabel);
            linkLabel.boundsInLocalProperty().addListener(refreshOnChange);

            //note new linkLabel bounds are all zero initially!

            //no need to listen to links, as long as we listen to nodes (also, it would cause stack overflow due to infinite call loop)
//            newLink.startXProperty().addListener(this);
//            newLink.startYProperty().addListener(this);
//            newLink.endXProperty().addListener(this);
//            newLink.endYProperty().addListener(this);
        }
        
        assert linkLabels.size() == links.size();
        assert nodes.size() == links.size();
        
        centralNode.toFront();
        if (nodes.size() > 2) {
            links.get(1).setVisible(true);
            linkLabels.get(1).setVisible(true);
            centralNode.setVisible(true);
            updateLinkLabels();
            updateCenter();

            for (int i = 0; i < nodes.size(); i++) {
//                RspecNodeController node = nodes.get(i);
                LinkController l = links.get(i);
                Button ll = linkLabels.get(i);
                
                l.setNodeB(centralNode);
            }
        } else {
            if (links.size() > 0) {
                nodes.get(0).toFront();
                linkLabels.get(0).toFront();
                if (links.size() > 1) {
                    assert links.size() == 2;
                    links.get(1).setVisible(false);
                    linkLabels.get(1).setVisible(false);
                    links.get(0).setVisible(true);
                    linkLabels.get(0).setVisible(true);
                    links.get(0).setNodeB(nodes.get(1));
                } else {
                    assert links.size() == 1;
                    links.get(0).setVisible(false);
                    linkLabels.get(0).setVisible(false);                
                }
            }
            centralNode.setVisible(true);
            updateLinkLabels();
            updateCenter();
        }
        for (int i = 0; i < nodes.size(); i++) {
            LinkController l = links.get(i);
            l.toBack();
        }
    }

    public void removeRspecIfaceViewController(RspecInterface iface) {
        RspecNode node = iface.getNode();

        int indexNode = -1;
        int index = 0;
        for (RspecNodeController rspecNodeController : nodes) {
            if (rspecNodeController.getNode() == node) {
                assert indexNode == -1 : "Duplicate node \""+node.getId()+"\" for iface \""+iface.getId()+"\"";
                indexNode = index;
            }
            index++;
        }
        assert indexNode != -1 : "Could not find node \""+node.getId()+"\" in nodes: "+nodes;

        LinkController lc = links.get(indexNode);
        Button linkLabel = linkLabels.get(indexNode);

        nodes.remove(indexNode);
        links.remove(indexNode);
        linkLabels.remove(indexNode);

        lc.setNodeA(null);
        lc.setNodeB(null);
        parent.getChildren().remove(lc);
        linkLabel.setOnAction(null);
        parent.getChildren().remove(linkLabel);

        updateCenter();
        updateLinkLabels();
    }

    @Override
    public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
        refresh();
    }

    public void refresh() {
        updateLinkLabels();
        updateCenter();
    }

    Node getCenterComponent() {
        return centralNode;
    }

    public RspecLink getLink() {
        return link;
    }


//    private RspecEditorController rspecEditorController;
//    void setRspecEditorController(RspecEditorController rspecEditorController) {
//        this.rspecEditorController = rspecEditorController;
//    }


    private RspecSelection rspecSelection = null;
    private ChangeListener<RspecLink> rspecSelectionChangeListener;
    public void setRspecSelection(RspecSelection rspecSelection) {
        if (this.rspecSelection != null) {
            assert rspecSelectionChangeListener != null;
            this.rspecSelection.selectedRspecLinkProperty().removeListener(rspecSelectionChangeListener);
            rspecSelectionChangeListener = null;
        }
        this.rspecSelection = rspecSelection;

        if (rspecSelection != null) {
            rspecSelectionChangeListener = new ChangeListener<RspecLink>() {
                @Override
                public void changed(ObservableValue ov, RspecLink oldSelected, RspecLink newSelected) {
                    onSelectionChanged(newSelected);
                }
            };
            rspecSelection.selectedRspecLinkProperty().addListener(rspecSelectionChangeListener);
        }
    }

    public void onClickedCentralNode() {
        if (rspecSelection == null) return;
        rspecSelection.setSelectedRspecLink(this.link);
    }

    public void onSelectionChanged(RspecLink newSelected) {
        centralNode.getStyleClass().clear();
        if (link == newSelected)
            centralNode.getStyleClass().add("rspec_selected_switchlabel");
        else
            centralNode.getStyleClass().add("rspec_switchlabel");
    }
}
