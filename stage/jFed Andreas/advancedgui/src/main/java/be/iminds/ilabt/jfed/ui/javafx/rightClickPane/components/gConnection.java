package be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components;

import javafx.scene.Group;
import javafx.scene.shape.Line;

public class gConnection extends Group {
    private gNode start,stop;
    private Line line;

    public gConnection(gNode start, gNode stop) {
        line = new Line();
        line.startXProperty().bind(start.getXMid());
        line.startYProperty().bind(start.getYMid());
        line.endXProperty().bind(stop.getXMid());
        line.endYProperty().bind(stop.getYMid());

        this.start = start;
        this.stop  = stop;

        line.setId("connection");

        getChildren().add(line);
    }

    public gNode getStart() {
        return start;
    }
    public gNode getStop() {
        return stop;
    }
    public boolean equals(gConnection c){
        return ( start.equals(c.getStart()) && stop.equals(c.getStop()) ) || ( start.equals(c.getStop()) && stop.equals(c.getStart()) ) ;
    }

    public boolean isPartOfConnection (gNode n){
        return start.equals(n) || stop.equals(n);
    }
    public String toString(){
        return "connection between " + start + " and " + stop;
    }
}
