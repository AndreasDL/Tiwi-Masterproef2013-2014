package be.iminds.ilabt.jfed.ui.rspeceditor.editor;

import java.net.URL;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecNode;

public class RspecNodeController extends Button {
    private RspecNode node;

    private DragHelper dragHelper;

    private ObservableList<ComponentManagerInfo> componentManagerInfos;

    public RspecNodeController(RspecNode node, URL cssUrl, Parent parentToStayInside, ObservableList<ComponentManagerInfo> componentManagerInfos) {
        this.node = node;
        this.componentManagerInfos = componentManagerInfos;
        componentManagerInfos.addListener(new ListChangeListener<ComponentManagerInfo>() {
            @Override public void onChanged(Change<? extends ComponentManagerInfo> change) { onSelectionChanged(); }
        });

        if (cssUrl == null) throw new RuntimeException("Error loading RspecEditorStyle.css");
        this.getStylesheets().add(cssUrl.toString());
//          String cssUrlString = JavaFxStyleSheetHelper.getStyleSheetURL(getClass(), "RspecEditorStyle.css");
//          this.getStylesheets().add(cssUrlString);

//        onSelectionChanged();
        this.getStyleClass().add("rspec_node");
        this.getStyleClass().add("component_manager_" + getComponentManagerColorIndex(node));

        dragHelper = new DragHelper(this, parentToStayInside);

        textProperty().bind(node.idProperty());

        if (node.editorXProperty().get() > 0 && node.editorYProperty().get() > 0) {
            //rspec node has position set, so use that one
            this.layoutXProperty().set(node.editorXProperty().get());
            this.layoutYProperty().set(node.editorYProperty().get());
        }

        node.editorXProperty().bindBidirectional(this.layoutXProperty());
        node.editorYProperty().bindBidirectional(this.layoutYProperty());

        this.setOnAction(new EventHandler() {
            @Override
            public void handle(Event t) { onClicked(); }
        });
    }

    private RspecSelection rspecSelection;
    public void setRspecSelection(RspecSelection rspecSelection) {
        this.rspecSelection = rspecSelection;

        rspecSelection.selectedRspecNodeProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object oldSelected, Object newSelected) {
                onSelectionChanged();
            }
        });
    }

    public void onClicked() {
        if (rspecSelection == null) return;
        rspecSelection.setSelectedRspecNode(this.node);
    }

    public String getComponentManagerColorIndex(RspecNode node) {
        if (node.getComponentManagerId() == null || node.getComponentManagerId().isEmpty())
            return "0";
        for (ComponentManagerInfo ci : componentManagerInfos)
            if (ci.getUrn() != null && ci.getUrn().equals(node.getComponentManagerId()))
                return ci.getIndex()+"";
        return "";
    }

    public void onSelectionChanged() {
        this.getStyleClass().clear();
        this.getStyleClass().add("rspec_node");
        if (rspecSelection != null && node != null && rspecSelection.isNodeSelected(node))
            this.getStyleClass().add("rspec_selected_node");
        this.getStyleClass().add("component_manager_"+getComponentManagerColorIndex(node));
    }

    public double getCenterX() {
        return getLayoutX() + getBoundsInLocal().getMinX() + (getBoundsInLocal().getWidth() / 2.0);
    }

    public double getCenterY() {
        return getLayoutY() + getBoundsInLocal().getMinY() + (getBoundsInLocal().getHeight() / 2.0);
    }

    public RspecNode getNode() {
        return node;
    }

    void setRspecEditorController(RspecEditorPanel rspecEditorController) {
        dragHelper.setRspecEditorController(rspecEditorController);
    }

    public void changeStyleSheet(String cssName) {
        URL cssUrl = getClass().getResource(cssName);
        this.getStylesheets().setAll(cssUrl.toString());
    }

    public void setCenter(double x, double y) {
        double realX = x - ((this.getWidth() / 2)) + this.getBoundsInLocal().getMinX();
        double realY = y - ((this.getHeight() / 2)) + this.getBoundsInLocal().getMinY();
        setLayoutX(realX);
        setLayoutY(realY);
    }
}
