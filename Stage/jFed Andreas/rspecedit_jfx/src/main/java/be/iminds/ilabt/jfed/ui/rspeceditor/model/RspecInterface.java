package be.iminds.ilabt.jfed.ui.rspeceditor.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RspecInterface {
    public RspecInterface(RspecNode node, RspecLink link, String id) {
        assert link != null;
        assert node != null;

        this.node = node;
        this.link = link;
        this.id.setValue(id);

        //check if not already added
        for (RspecInterface existingIface : node.getInterfaces())
            if (existingIface == this || this.getId().equals(existingIface.getId()))
                throw new RuntimeException("Duplicate interfaces in XML Rspec: id=\""+existingIface.getId()+"\"");
        for (RspecInterface existingIface : link.getInterfaces())
            if (existingIface == this || this.getId().equals(existingIface.getId()))
                throw new RuntimeException("Duplicate interfaces in XML Rspec: id=\""+existingIface.getId()+"\"");

        node.getInterfaces().add(this);
        link.getInterfaces().add(this);
    }
//    /** Special copy method*/
//    public static RspecInterface createCopy(RspecInterface o, Rspec rspec, RspecLink rspecLink) {
//        RspecNode node = rspec.getNodeById(o.getNode().getId());
//        assert node != null;
//        RspecInterface res = new RspecInterface(node, rspecLink, o.getId());
//        res.macAddress.set(o.macAddress.get());
//        return res;
//    }
    
    
    private final StringProperty id = new SimpleStringProperty();

    public String getId() {
        return id.get();
    }

    public void setId(String value) {
        id.set(value);
    }

    public StringProperty idProperty() {
        return id;
    }
    
    
    private final StringProperty macAddress = new SimpleStringProperty();

    public String getMacAddress() {
        return macAddress.get();
    }

    public void setMacAddress(String value) {
        macAddress.set(value);
    }

    public StringProperty macAddressProperty() {
        return macAddress;
    }
    
    
    
    
    private RspecNode node;
    public RspecNode getNode() {
        return node;
    }

    private RspecLink link;
    public RspecLink getLink() {
        return link;
    }

    public void delete() {
//        System.out.println("RSpecInterface(\""+getId()+"\").delete() node="+node+" link="+link+" link interfaces:");
//        if (link != null) {
//            for (RspecInterface iface : link.getInterfaces()) {
//                System.out.println("  iface="+iface.getId());
//            }
//        }

        assert link != null;
        assert node != null;
        if (node != null) {
            Object removed = node.getInterfaces().remove(this);
            assert removed != null;
        }
        if (link != null) {
            Object removed = link.getInterfaces().remove(this);
            assert removed != null;
        }
        node = null;
        link = null;
    }

    @Override
    public String toString() {
        return "RspecInterface{id=" + id.get() + '}';
    }
}
