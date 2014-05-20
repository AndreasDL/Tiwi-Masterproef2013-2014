package be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components;

import be.iminds.ilabt.jfed.ui.javafx.rightClickPane.handlers.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.net.URL;

public class gNode extends Group {
    //data
    private String type;
    private DoubleProperty xMid, yMid;

    //comp
    private Rectangle main;
    private Rectangle drag;
    private Label img;

    //layout
    public static final double DIMX = 25; //size from mid to border
    public static final double DIMY = 25;
    public static final double CORNER = 10;

    public gNode(NodePane pane, String type, double xMid, double yMid) {
        this.type = type;
        this.xMid = new SimpleDoubleProperty(xMid);
        this.yMid = new SimpleDoubleProperty(yMid);

        //GUI
        getStylesheets().add(getResourceFor(getClass(), "BlackRed.css"));

        main = new Rectangle(xMid - DIMX, yMid - DIMY, 2 * DIMX, 2 * DIMY);
        main.setId("rect-main");

        drag = new Rectangle(xMid - DIMX, yMid - DIMY, CORNER, CORNER);
        drag.setId("rect-drag");
        NodeDragHandler md = new NodeDragHandler(this);
        drag.setOnMouseDragged(md);

        img = new Label("",new ImageView( new Image(getClass().getResourceAsStream("comp.png"),50,50,true,true) ));
        img.setLayoutX(xMid-DIMX);
        img.setLayoutY(yMid-DIMY-5);

        img.setOnMousePressed(new ConnectionStartMaker(pane, this));
        img.setOnMouseReleased(new ConnectionStopMaker(pane, this));

        getChildren().addAll(main, img, drag);

        RightClickHandler right = new RightClickHandler(pane,this);
        setOnMouseReleased(right);

        MouseOverHandler m = new MouseOverHandler(this);
        setOnMouseEntered(m);
        setOnMouseExited(m);
    }

    public String getType() {
        return type;
    }
    public Rectangle getMain(){
        return main;
    }

    public DoubleProperty getXMid() {
        return xMid;
    }
    public DoubleProperty getYMid() {
        return yMid;
    }

    public void setxMid(double xMid) {
        this.xMid.setValue(xMid);
        main.setX(xMid - DIMX);
        drag.setX(xMid - DIMX);
        img.setLayoutX(xMid-DIMX);
    }
    public void setyMid(double yMid) {
        this.yMid.setValue(yMid);
        main.setY(yMid - DIMY);
        drag.setY(yMid - DIMY);
        img.setLayoutY(yMid-DIMY-5);
    }

    public boolean equals(gNode n) {
        return (type.equals(n.getType()) && (double)xMid.getValue() == (double)n.getXMid().getValue() && (double)yMid.getValue() == (double)n.getYMid().getValue());
    }
    public String toString(){
        return "node of type: " + type + " {x;y}= " + xMid.getValue() + " ; " + yMid.getValue();
    }

    private String getResourceFor(Class clazz, String path) {
        URL resourceURL = clazz.getResource(path);
        if (resourceURL == null) {
            throw new IllegalArgumentException("No resource exists at: " + path + " relative to " + clazz.getName());
        }

        return resourceURL.toExternalForm();
    }

    public boolean isCoordinatePartOfNode(double x, double y){
        double xup = xMid.getValue() - DIMX;
        double yup = yMid.getValue() - DIMY;
        double xdown = xMid.getValue() + DIMX;
        double ydown = yMid.getValue() + DIMY;

        System.out.println("x: " + x + " btw " + xup + " and " + xdown
                + " y: " + y + " btw " + yup + " and " + ydown
        );

        //return  (x > xMid.getValue() - DIMX && x < xMid.getValue() + DIMX) && (y > yMid.getValue() - DIMY && y < yMid.getValue() + DIMY);
        return x > xup && x < xdown && y > yup && y < ydown;
    }
}
