package be.iminds.ilabt.jfed.ui.rspeceditor.editor;

import java.io.IOException;
import java.net.URL;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.shape.Line;

public class LinkController extends Line implements ChangeListener<Number> {
    public LinkController() {
        URL location = getClass().getResource("Link.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(location, null);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    private Node nodeA;
    private Node nodeB;
    private double centerX, centerY;

    public void updateLink() {
        if (nodeA == null || nodeB == null) {
            setStartX(0.0);
            setStartY(0.0);
            setEndX(0.0);
            setEndY(0.0);
            centerX = 0.0;
            centerY = 0.0;
        } else {
            setStartX(nodeA.getLayoutX() + nodeA.getLayoutBounds().getMinX() + (nodeA.getLayoutBounds().getWidth() / 2.0));
            setStartY(nodeA.getLayoutY() + nodeA.getLayoutBounds().getMinY() + (nodeA.getLayoutBounds().getHeight() / 2.0));
            setEndX(nodeB.getLayoutX() + nodeB.getLayoutBounds().getMinX() + (nodeB.getLayoutBounds().getWidth() / 2.0));
            setEndY(nodeB.getLayoutY() + nodeB.getLayoutBounds().getMinY() + (nodeB.getLayoutBounds().getHeight() / 2.0));
            centerX = getStartX() > getEndX() ? getEndX() + ((getStartX() - getEndX())/2.0) : getStartX() + ((getEndX() - getStartX())/2.0);
            centerY = getStartY() > getEndY() ? getEndY() + ((getStartY() - getEndY())/2.0) : getStartY() + ((getEndY() - getStartY())/2.0);
        }
    }
    
    public double getCenterX() {
        return centerX;
    }
    
    public double getCenterY() {
        return centerY;
    }
    
    public Node getNodeA() {
        return nodeA;
    }

    public void addNode(Node node) {
        if (this.nodeA != null) {
            setNodeA(node);
            return;
        }
        if (this.nodeB != null) {
            setNodeB(node);
            return;
        }
        throw new RuntimeException("Only 2 links allowed");
    }
    
    public void setNodeA(Node nodeA) {
        if (this.nodeA != null) {
            this.nodeA.layoutXProperty().removeListener(this);
            this.nodeA.layoutYProperty().removeListener(this);
        }
        this.nodeA = nodeA;
        if (nodeA != null) {
            nodeA.layoutXProperty().addListener(this);
            nodeA.layoutYProperty().addListener(this);
            nodeA.boundsInLocalProperty().addListener(new ChangeListener<Bounds>() {
                @Override
                public void changed(ObservableValue<? extends Bounds> ov, Bounds t, Bounds t1) {
                    updateLink();
                }
            });
        }
        updateLink();
    }

    public Node getNodeB() {
        return nodeB;
    }

    public void setNodeB(Node nodeB) {
        if (this.nodeB != null) {
            this.nodeB.layoutXProperty().removeListener(this);
            this.nodeB.layoutYProperty().removeListener(this);
        }
        this.nodeB = nodeB;
        if (nodeB != null) {
            nodeB.layoutXProperty().addListener(this);
            nodeB.layoutYProperty().addListener(this);
            nodeB.boundsInLocalProperty().addListener(new ChangeListener<Bounds>() {
                @Override
                public void changed(ObservableValue<? extends Bounds> ov, Bounds t, Bounds t1) {
                    updateLink();
                }
            });
        }
        updateLink();
    }

    @Override
    public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
        updateLink();
    }

    public void changeStyleSheet(String cssName) {
        URL cssUrl = getClass().getResource(cssName);
        //cannot update here, must be updated with parent
        //this.getStylesheets().setAll(cssUrl.toString());
    }

}
