package be.iminds.ilabt.jfed.ui.javafx.rightClickPane.handlers;


import be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components.NodePane;
import be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components.RightClickMenu;
import be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components.gNode;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class RightClickHandler implements EventHandler<MouseEvent> {
    private NodePane pane;
    private RightClickMenu right;


    public RightClickHandler(NodePane pane, gNode node) {
        this.pane = pane;

        right = new RightClickMenu(pane, node);

    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            right.show(pane, mouseEvent.getScreenX(), mouseEvent.getScreenY());
        }

    }

}
