package be.iminds.ilabt.jfed.ui.javafx.rightClickPane.handlers;

import be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components.NodePane;
import be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components.gNode;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class ConnectionStartMaker implements EventHandler<MouseEvent> {
    private NodePane pane;
    private gNode start;

    public ConnectionStartMaker(NodePane pane, gNode start) {
        this.pane = pane;
        this.start = start;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        pane.setConnStart(start);
    }
}
