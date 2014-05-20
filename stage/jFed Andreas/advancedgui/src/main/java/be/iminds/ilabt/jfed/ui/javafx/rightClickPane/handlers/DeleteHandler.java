package be.iminds.ilabt.jfed.ui.javafx.rightClickPane.handlers;


import be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components.NodePane;
import be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components.gNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class DeleteHandler implements EventHandler<ActionEvent> {
    private NodePane drawCanvas;
    private gNode node;

    public DeleteHandler(NodePane drawCanvas,gNode node) {
        this.drawCanvas = drawCanvas;
        this.node = node;
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        if (node != null)
            drawCanvas.deleteNode(node);
    }


}