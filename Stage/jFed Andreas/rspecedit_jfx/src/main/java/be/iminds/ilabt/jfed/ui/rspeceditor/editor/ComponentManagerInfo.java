package be.iminds.ilabt.jfed.ui.rspeceditor.editor;

import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * ComponentManagerInfo info about component manager, for use by RSpec editor
 */
public class ComponentManagerInfo {

    public static class ComponentId {
        private String urn;
        private String name;
        private RspecNode advertisementRspecNode;

        public ComponentId(String urn, String name, RspecNode advertisementRspecNode) {
            this.urn = urn;
            this.name = name;
            this.advertisementRspecNode = advertisementRspecNode;
        }

        public String getUrn() {
            return urn;
        }

        public String getName() {
            return name;
        }

        public RspecNode getAdvertisementRspecNode() {
            return advertisementRspecNode;
        }
    }

    private String urn;
    private String urnPart;
//    private Color color;
    private int index;
    private ObservableList<String> sliverTypes =FXCollections.observableArrayList();

    private ObservableList<String> osImages = FXCollections.observableArrayList();
    private ObservableList<ComponentId> componentIds = FXCollections.observableArrayList();

    public ComponentManagerInfo(String urn, String urnPart) {
        this.urn = urn;
        this.urnPart = urnPart;
        sliverTypes.add("raw-pc");
    }

    public void setSliverTypes(List<String> sliverTypes) {
        this.sliverTypes.setAll(sliverTypes);
    }

    public List<String> getSliverTypes() {
        return sliverTypes;
    }

    public String getUrn() {
        return urn;
    }

    public String getUrnPart() {
        return urnPart;
    }

    public ObservableList<String> getOsImages() {
        return osImages;
    }

    public ObservableList<ComponentId> getComponentIds() {
        return componentIds;
    }

//    public Color getColor() {
//        return color;
//    }
//
//    public void setColor(Color color) {
//        this.color = color;
//    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
