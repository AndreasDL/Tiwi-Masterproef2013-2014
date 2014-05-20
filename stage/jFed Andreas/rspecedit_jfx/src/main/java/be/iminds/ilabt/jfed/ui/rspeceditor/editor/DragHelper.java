package be.iminds.ilabt.jfed.ui.rspeceditor.editor;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;

class DragHelper implements EventHandler<MouseEvent> {
    private Node n;
    private double sceneX;
    private double sceneY;
    private double layoutX;
    private double layoutY;
    
    private RspecEditorPanel rspecEditorController;

    private Parent parentToStayInside;

    public DragHelper(final Node n, final Parent parentToStayInside) {
        this.n = n;
        this.parentToStayInside = parentToStayInside;

        sceneX = -1;
        sceneY = -1;
        layoutX = n.getLayoutX();
        layoutY = n.getLayoutY();
        n.setCursor(Cursor.OPEN_HAND);
        
        n.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
//             System.out.println(n+" DragHelper mousepressed reset");
                reset(me);
            }
        });
        n.setOnMouseDragged(this);
    }

    public void reset(MouseEvent me) {
        sceneX = me.getSceneX();
        layoutX = n.getLayoutX();
        sceneY = me.getSceneY();
        layoutY = n.getLayoutY();
    }
    
    @Override
    public void handle(MouseEvent me) {
        if (me.isPrimaryButtonDown()) {
            if (sceneX == -1 || sceneY == -1) 
                reset(me);
            double dx = me.getSceneX() - sceneX;
            double dy = me.getSceneY() - sceneY;

            //scale detect and compensate
            Node curNode = n;
            while (curNode != null) {
                if (curNode.getScaleX() != 1.0) {
                    dx /= curNode.getScaleX();
                }
                if (curNode.getScaleY() != 1.0)
                    dy /= curNode.getScaleY();

                curNode = curNode.getParent();
            }

            double parentMaxX = parentToStayInside.getBoundsInLocal().getWidth() - n.getBoundsInLocal().getMaxX();
            double parentMaxY = parentToStayInside.getBoundsInLocal().getHeight() - n.getBoundsInLocal().getMaxY();

            double newLayoutX = layoutX + dx;
            if (newLayoutX < 0) newLayoutX = 0;
            if (newLayoutX > parentMaxX) newLayoutX = parentMaxX;
            double newLayoutY = layoutY + dy;
            if (newLayoutY < 0) newLayoutY = 0;
            if (newLayoutY > parentMaxY) newLayoutY = parentMaxY;

            n.setLayoutX(newLayoutX);
            n.setLayoutY(newLayoutY);
//             System.out.println(n+" DragHelper set layoutX="+(layoutX + dx)+" layoutY="+(layoutY + dy));
            n.setCursor(Cursor.CLOSED_HAND);
            
            if (rspecEditorController != null)
                rspecEditorController.stopLinkMaking();
        } else {
            n.setCursor(Cursor.OPEN_HAND);
        }
    }

    public void setRspecEditorController(RspecEditorPanel rspecEditorController) {
        this.rspecEditorController = rspecEditorController;
    }
}
