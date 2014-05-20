/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.iminds.ilabt.jfed.ui.javafx.advanced_gui;

import be.iminds.ilabt.jfed.highlevel.model.Slice;
import be.iminds.ilabt.jfed.highlevel.model.Sliver;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;

import java.util.HashMap;
import java.util.Map;

/**
 * centralizes all GUI components and the model related to a slice
 */
public class SliceController {
    //components part of overview list of slices
    public CheckBox checkBox;
    public Button button;
    public Button removeButton;

    private HBox overview;

    //list this is contained in and should be removed from if deleted
    private ObservableList<Slice> sliceList;

//    private final ListProperty<SliverOverviewListCell> sliverControllers = new SimpleListProperty(FXCollections.observableArrayList());
//    private final Map<Sliver, SliverOverviewListCell> sliverControllerMap = new HashMap<Sliver, SliverOverviewListCell>();

    //tab
    private TabPane parentTabPane;
    private SliceTabController sliceTabController;

    private final Slice slice;

    public SliceController(TabPane parentTabPane, Slice slice, ObservableList<Slice> sliceList) {
        this.parentTabPane = parentTabPane;
        this.slice = slice;
        this.sliceList = sliceList;

        assert(parentTabPane != null);
        assert(slice != null);

        this.checkBox = new CheckBox();
        this.button = new Button();
        this.removeButton = new Button();
        removeButton.setText("X");
        this.button.textProperty().bind(slice.nameProperty());
        this.button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                getSliceTab().focusTab();
            }
        });
        this.removeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                remove();
            }
        });

        overview = new HBox();
        overview.getChildren().addAll(checkBox, button, removeButton);

//        slice.getSlivers().addListener(new ListChangeListener<Sliver>() {
//
//            @Override
//            public void onChanged(Change<? extends Sliver> change) {
//                //add and remove sliverControllers as slivers get added and removed
//                while (change.next()) {
//                    for (Sliver sliver : change.getRemoved()) {
//                        SliverOverviewListCell sliverController = sliverControllerMap.remove(sliver);
//                        assert sliverController != null;
//                        sliverControllers.remove(sliverController);
//                    }
//                    for (Sliver sliver : change.getAddedSubList()) {
//                        //if other listeners are called before this one, they might have caused creation of the controller already
//                        if (!sliverControllerMap.containsKey(sliver)) {
//                            SliverOverviewListCell sliverController = new SliverOverviewListCell(sliver);
//                            sliverControllerMap.put(sliver, sliverController);
//                            sliverControllers.add(sliverController);
//                        }
//                    }
//                }
//            }
//        });
    }

    public Node getSliceNode() {
        return button;
    }

    public Node getSliceSelector() {
        return checkBox;
    }

    public Node getOverviewNode() {
        return overview;
    }

    public SliceTabController getSliceTab() {
        if (sliceTabController == null)
            sliceTabController = new SliceTabController(parentTabPane, this);
        return sliceTabController;
    }

    Slice getSlice() {
        return slice;
    }

    public void remove() {
        if (sliceList != null) {
            //delete all slivers
            slice.getEasyModel().getHighLevelController().deleteSlice(slice);

            //don't just remove from model
            //sliceList.remove(slice);
        }
    }

//    public ReadOnlyListProperty<SliverOverviewListCell> getSliverControllers() {
//        return sliverControllers;
//    }
//    public SliverOverviewListCell getSliverController(Sliver sliver) {
//        //this can be called BEFORE the listener that creates the sliverControllers is called.
//        //So we need to create the sliverController in that case.
//        SliverOverviewListCell res = sliverControllerMap.get(sliver);
//
//        if (res == null) {
//            res = new SliverOverviewListCell(sliver);
//            sliverControllerMap.put(sliver, res);
//            sliverControllers.add(res);
//        }
//
//        return res;
//    }
}
