package be.iminds.ilabt.jfed.ui.javafx.rightClickPane.handlers;


import be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components.NodePane;
import be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components.gNode;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class ConnectionStopMaker implements EventHandler<MouseEvent> {
    private NodePane pane;
    private gNode node;

    public ConnectionStopMaker(NodePane pane, gNode node) {
        this.pane = pane;
        this.node = node;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        gNode stop = pane.getNodeAtPosition(mouseEvent.getSceneX()-252 , mouseEvent.getSceneY()-133);//-35);
        //bugged :)
        //niet mooi gemaakt

        if (stop != null){
            pane.setConnStop(stop);
            pane.addConnection();
        }else{
            System.out.println("Stop node not found => bug in " + "/be/iminds/ilabt/jfeb/ui/javafx/rightClickPane/handlers/ConnectionStopMaker");
        }
    }
}
