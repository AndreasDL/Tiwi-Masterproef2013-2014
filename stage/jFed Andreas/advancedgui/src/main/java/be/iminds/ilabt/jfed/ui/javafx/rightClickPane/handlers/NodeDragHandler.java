package be.iminds.ilabt.jfed.ui.javafx.rightClickPane.handlers;


import be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components.gNode;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class NodeDragHandler implements EventHandler<MouseEvent> {
    private gNode n;

    public NodeDragHandler(gNode n) {
        this.n = n;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {

        if (mouseEvent.isPrimaryButtonDown()){
            n.setxMid(mouseEvent.getX());
            n.setyMid(mouseEvent.getY());
        }
    }


}
