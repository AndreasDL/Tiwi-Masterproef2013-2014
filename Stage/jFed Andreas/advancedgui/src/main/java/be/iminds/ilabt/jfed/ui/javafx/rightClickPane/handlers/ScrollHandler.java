package be.iminds.ilabt.jfed.ui.javafx.rightClickPane.handlers;

import be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components.NodePane;
import javafx.event.EventHandler;
import javafx.scene.control.Slider;
import javafx.scene.input.ScrollEvent;

public class ScrollHandler implements EventHandler<ScrollEvent> {
    private Slider zoomSlider;
    private NodePane pane;


    public ScrollHandler(Slider zoomSlider) {
        this.zoomSlider = zoomSlider;
    }

    @Override
    public void handle(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() < 0) {
            zoomSlider.decrement();
        }else if (scrollEvent.getDeltaY() > 0){
            zoomSlider.increment();
        }
    }
}