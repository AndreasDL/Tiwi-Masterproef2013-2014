package be.iminds.ilabt.jfed.ui.rspeceditor.editor;

import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecLink;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecNode;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class RspecSelection {
    public RspecSelection() {
        selectedRspecNode.addListener(new ChangeListener<RspecNode>() {
            @Override
            public void changed(ObservableValue<? extends RspecNode> observableValue, RspecNode oldValue, RspecNode newValue) {
                if (newValue != null)
                    selectedRspecLink.set(null);
            }
        });
        selectedRspecLink.addListener(new ChangeListener<RspecLink>() {
            @Override
            public void changed(ObservableValue<? extends RspecLink> observableValue, RspecLink oldValue, RspecLink newValue) {
                if (newValue != null)
                    selectedRspecNode.set(null);
            }
        });
    }

    public void clearAllSelections() {
        selectedRspecNode.set(null);
        selectedRspecLink.set(null);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final ObjectProperty<RspecNode> selectedRspecNode = new SimpleObjectProperty<RspecNode>(null);

    public RspecNode getSelectedRspecNode() {
        return selectedRspecNode.get();
    }

    public void setSelectedRspecNode(RspecNode value) {
        selectedRspecNode.set(value);
    }

    public ObjectProperty<RspecNode> selectedRspecNodeProperty() {
        return selectedRspecNode;
    }
    
    public boolean isNodeSelected(RspecNode n) {
        if (selectedRspecNode.get() == null) return false;
        return selectedRspecNode.get().equals(n);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private final ObjectProperty<RspecLink> selectedRspecLink = new SimpleObjectProperty<RspecLink>(null);

    public RspecLink getSelectedRspecLink() {
        return selectedRspecLink.get();
    }

    public void setSelectedRspecLink(RspecLink value) {
        selectedRspecLink.set(value);
    }

    public ObjectProperty<RspecLink> selectedRspecLinkProperty() {
        return selectedRspecLink;
    }
    
    public boolean isLinkSelected(RspecLink l) {
        if (selectedRspecLink.get() == null) return false;
        return selectedRspecLink.get().equals(l);
    }
}
