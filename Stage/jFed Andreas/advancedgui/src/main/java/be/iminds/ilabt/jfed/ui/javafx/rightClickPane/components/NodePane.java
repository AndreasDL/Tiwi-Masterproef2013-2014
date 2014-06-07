package be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components;


import be.iminds.ilabt.jfed.ui.javafx.rightClickPane.handlers.ScrollHandler;
import javafx.animation.ScaleTransition;
import javafx.animation.ScaleTransitionBuilder;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.net.URL;

public class NodePane extends Pane {
    private Group nodes;
    private Group connections;
    private String type;

    private double zoom;
    private Slider zoomSlider;

    double startTransX, startTransY;
    private gNode connStart;
    private gNode connStop;


    public NodePane(Slider zoomSlider) {
        zoom = 1;
        this.zoomSlider = zoomSlider;

        nodes = new Group();
        connections = new Group();
        getChildren().addAll(connections,nodes);


        setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                    addNode(mouseEvent.getX(), mouseEvent.getY());
                }
            }
        });

        setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    startTransX = mouseEvent.getX();
                    startTransY = mouseEvent.getY();
                }
            }
        });

        setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    setTranslateX(mouseEvent.getX() - startTransX);
                    setTranslateY(mouseEvent.getY() - startTransY);
                }
            }
        });

        getStylesheets().add("/be/iminds/ilabt/jfed/ui/javafx/rightClickPane/components/BlackRed.css");

        ScrollHandler s = new ScrollHandler(zoomSlider);
        setOnScroll(s);
    }


    //public
    public void addNode(double x, double y) {
        gNode n = new gNode(this, type, x, y);

        nodes.getChildren().add(n);
    }
    public void deleteNode(gNode n) {
        int i = 0;
        //verbindingen
        while (i < connections.getChildren().size()) {
            if ( ((gConnection) connections.getChildren().get(i)).isPartOfConnection(n) ) {
                connections.getChildren().remove(i);
                System.out.println("i: " + i + " removed");
            } else {
                System.out.println("i: " + i);
                i++;
            }
        }

        i=0;
        //node
        while (i < nodes.getChildren().size() && !nodes.getChildren().get(i).equals(n))
            i++;

        if (i < nodes.getChildren().size())
            nodes.getChildren().remove(i);

    }

    //connections
    public void setConnStart(gNode connStart) {
        this.connStart = connStart;
    }
    public void setConnStop(gNode connStop) {
        this.connStop = connStop;
    }
    public void addConnection() {
        gConnection g = new gConnection(connStart , connStop);
        connections.getChildren().add(g);

        layout();
    }
    public gNode getNodeAtPosition(double x, double y){
        int i = nodes.getChildren().size() -1; //back => front cuz last component painted above first ones.
        while ( i >= 0 && !((gNode)nodes.getChildren().get(i)).isCoordinatePartOfNode(x,y) )
            i--;

        if (i >= 0){
            return (gNode)nodes.getChildren().get(i);
        }else{
            return null;
        }
    }

    public void setType(String type) {
        //sets current node type
        this.type = type;
    }
    public void clear() {
        nodes.getChildren().clear();
        connections.getChildren().clear();
        zoomDefault();
    }

    //zoom
    public void setZoom(double zoom) {
        this.zoom = zoom;

        ScaleTransition s = ScaleTransitionBuilder.create()
                .node(this)
                .duration(Duration.seconds(2))
                .toX(zoom)
                .toY(zoom)
                .cycleCount(1)
                .build();

        s.play();

    }
    public void zoomDefault() {
        zoomSlider.valueProperty().setValue(10);
    }

    //private
    private String getResourceFor(Class clazz, String path) {
        URL resourceURL = clazz.getResource(path);
        if (resourceURL == null) {
            throw new IllegalArgumentException("No resource exists at: " + path + " relative to " + clazz.getName());
        }

        return resourceURL.toExternalForm();
    }

}
