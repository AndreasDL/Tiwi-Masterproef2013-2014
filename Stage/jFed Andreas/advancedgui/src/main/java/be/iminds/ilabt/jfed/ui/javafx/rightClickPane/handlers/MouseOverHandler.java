package be.iminds.ilabt.jfed.ui.javafx.rightClickPane.handlers;

import be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components.gNode;
import javafx.animation.FillTransition;
import javafx.animation.FillTransitionBuilder;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class MouseOverHandler implements EventHandler<MouseEvent> {
    public gNode n;

    public MouseOverHandler(gNode n) {
        this.n = n;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        FillTransition f = FillTransitionBuilder.create()
                .duration(Duration.seconds(2))
                .shape(n.getMain())
                .fromValue(Color.web("5E0000"))
                .toValue(Color.RED)
                .cycleCount(1)
                .autoReverse(true)
                .build();

        if (mouseEvent.getEventType() == MouseEvent.MOUSE_ENTERED){
            f.play();
        }else if (mouseEvent.getEventType() == MouseEvent.MOUSE_EXITED){
            f.stop();
            f = FillTransitionBuilder.create()
                    .duration(Duration.seconds(2))
                    .shape(n.getMain())
                    .fromValue(Color.RED)
                    .toValue(Color.web("#5E0000"))
                    .cycleCount(1)
                    .autoReverse(true)
                    .build();
            f.play();
        }
    }
}

