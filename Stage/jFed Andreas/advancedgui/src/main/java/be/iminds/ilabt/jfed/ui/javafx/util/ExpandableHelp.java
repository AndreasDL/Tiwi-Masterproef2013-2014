package be.iminds.ilabt.jfed.ui.javafx.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

/**
 * ExpandableHelp
 */
public class ExpandableHelp extends HBox {
    private final StringProperty text = new SimpleStringProperty();

    private final Label iconLabel;
    private final Label textLabel;

    private final BooleanProperty expanded = new SimpleBooleanProperty(false);
    private final boolean popup;

    public ExpandableHelp() {
        this("", false);
    }
    public ExpandableHelp(String helpText) {
        this(helpText, false);
    }
    public ExpandableHelp(String helpText, boolean showInitially) {
        this.text.set(helpText);

        this.expanded.set(showInitially);

        popup = helpText.length() > 200;

        iconLabel = new Label("help"); //text only shown when helpIco style class not found
        iconLabel.getStyleClass().add("helpIco");
        iconLabel.managedProperty().bind(iconLabel.visibleProperty());

        textLabel = new Label();
        textLabel.getStyleClass().add("helpLabel");
        textLabel.setWrapText(true);
        textLabel.managedProperty().bind(textLabel.visibleProperty());
        textLabel.setVisible(showInitially);
        if (popup)
            textLabel.setText("Click to see help");
        else
            textLabel.textProperty().bind(text);

        getChildren().addAll(iconLabel, textLabel);

        iconLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (!popup) {
                    expanded.set(!expanded.get());
                } else {
                    JavaFXDialogUtil.showMessage(ExpandableHelp.this.text.get(), textLabel);
                }
            }
        });
        iconLabel.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                textLabel.setVisible(true);
            }
        });
        iconLabel.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (!expanded.get())
                    textLabel.setVisible(false);
            }
        });

        expanded.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean1) {
                textLabel.setVisible(expanded.get());
            }
        });
    }

    public String getText() {
        return text.get();
    }
    public void setText(String text) {
        this.text.set(text);
    }
    public StringProperty textProperty() {
        return text;
    }

    public void setExpanded(boolean expanded) {
        this.expanded.set(expanded);
    }
    public boolean getExpanded() {
        return expanded.get();
    }
    public BooleanProperty expandedProperty() {
        return expanded;
    }
}
