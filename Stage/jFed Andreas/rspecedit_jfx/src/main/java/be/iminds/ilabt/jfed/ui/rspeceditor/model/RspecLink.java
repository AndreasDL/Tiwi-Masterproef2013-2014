package be.iminds.ilabt.jfed.ui.rspeceditor.model;

import be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.*;
import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.*;
import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.ComponentManager;
import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.InterfaceRefContents;
import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.LinkContents;
import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.LinkPropertyContents;
import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.LinkType;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javax.xml.bind.JAXBElement;
import java.util.Iterator;
import java.util.List;

public class RspecLink implements ListChangeListener<RspecInterface> {

    @Override
    public void onChanged(Change<? extends RspecInterface> change) {
//        System.out.println("RspecLink interface list changed");
//        while (change.next()) {
//            System.out.println("RspecLink interface list change: added "+change.getAddedSize()+" elements");
//        }
        updateLinkSettings();
    }


    @GuiEditable(nullable = false, clazz = String.class, guiName = "ID", guiHelp = "Unique name for this link")
    private final StringProperty id = new SimpleStringProperty();

    private final Rspec rspec;

    public RspecLink(Rspec rspec, String id) {
        this.rspec = rspec;
        this.id.setValue(id);
        interfaces.addListener(this);
        linkTypes.add("lan");
    }

//    /** copy constructor. Creates deep copy, including copy of interfaces. */
//    public RspecLink(Rspec rspec, RspecLink o) {
//        this.rspec = rspec;
//        id.set(o.id.get());
//
//        for (String c : o.componentManagerUrns)
//            componentManagerUrns.add(c);
//        for (String c : o.linkTypes)
//            linkTypes.add(c);
//        for (RspecInterface c : o.interfaces)
//            interfaces.add(RspecInterface.createCopy(c, rspec, this));
//        for (LinkSetting c : o.linkSettings) {
//            RspecInterface from = getInterfaceById(c.getFromIface().getId());
//            RspecInterface to = getInterfaceById(c.getToIface().getId());
//            assert from != null;
//            assert to != null;
//            linkSettings.add(LinkSetting.createCopy(from, to, c));
//        }
//    }

    public Rspec getRspec() {
        return rspec;
    }

    public String getId() {
        return id.get();
    }

    public void setId(String value) {
        id.set(value);
    }

    public StringProperty idProperty() {
        return id;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GuiEditable(nullable = false, clazz = ListProperty.class, listClass = String.class , guiName = "Component Manager URN(s)", guiHelp = "URN(s) of the component manager(s) for this link")
    private final ListProperty<String> componentManagerUrns = new SimpleListProperty<>(FXCollections.<String>observableArrayList());

    public ListProperty<String> getComponentManagerUrns() {
        return componentManagerUrns;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GuiEditable(nullable = false, clazz = ListProperty.class, listClass = String.class , guiName = "Link Type(s)", guiHelp = "Link type name(s)")
    private final ListProperty<String> linkTypes = new SimpleListProperty<>(FXCollections.<String>observableArrayList());

    public ListProperty<String> getLinkTypes() {
        return linkTypes;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * CApacity of 0, latency of 0 and loss of 0.0 will not be written to XML (as they are default)
     * */
    public static class LinkSetting {
        private RspecInterface fromIface;
        private RspecInterface toIface;
        private final IntegerProperty capacity_bps = new SimpleIntegerProperty(0);
        private final IntegerProperty latency_ms = new SimpleIntegerProperty(0);
        private final DoubleProperty packetLoss = new SimpleDoubleProperty(0.0); /*  [0.0, 1.0]  */

        public LinkSetting(RspecInterface fromIface, RspecInterface toIface) {
            this.fromIface = fromIface;
            this.toIface = toIface;
            capacity_bps.set(1000000000); //set default of 1 Gbit/s
        }

        //////////////////////////////////////////////////////

        public RspecInterface getFromIface() {
            return fromIface;
        }

        public RspecInterface getToIface() {
            return toIface;
        }

        //////////////////////////////////////////////////////

        public int getCapacity_bps() {
            return capacity_bps.get();
        }

        public void setCapacity_bps(Integer value) {
            capacity_bps.set(value);
        }

        public IntegerProperty capacity_bpsProperty() {
            return capacity_bps;
        }

        public boolean isCapacitySet() {
            return capacity_bps.get() != 0;
        }
        //////////////////////////////////////////////////////

        public int getLatency_ms() {
            return latency_ms.get();
        }

        public void setLatency_ms(Integer value) {
            latency_ms.set(value);
        }

        public IntegerProperty latency_msProperty() {
            return latency_ms;
        }

        public boolean isLatencySet() {
            return latency_ms.get() != 0;
        }
        //////////////////////////////////////////////////////

        public double getPacketLoss() {
            return packetLoss.get();
        }

        public void setPacketLoss(Double value) {
            packetLoss.set(value);
        }

        public DoubleProperty packetLossProperty() {
            return packetLoss;
        }

        public boolean isPacketLossSet() {
            return packetLoss.get() != 0.0;
        }

        @Override
        public String toString() {
            return "LinkSetting{" +
                    "toIface=" + toIface.toString() +
                    ", fromIface=" + fromIface.toString() +
                    '}';
        }
    }


    @GuiEditable(nullable = false, clazz = ListProperty.class, listClass = LinkSetting.class, guiName = "Link Setting", guiHelp = "Bandwidth, latency and loss settings for each one-directional link")
    private ListProperty<LinkSetting> linkSettings = new SimpleListProperty<LinkSetting>(FXCollections.<LinkSetting>observableArrayList());
    public ListProperty<LinkSetting> getLinkSettings() {
        return linkSettings;
    }
    public LinkSetting getLinkSetting(RspecInterface fromIface, RspecInterface toIface) {
        assert interfaces.contains(fromIface);
        assert interfaces.contains(toIface);
        for (LinkSetting ls : linkSettings) {
            if (ls.fromIface == fromIface && ls.toIface == toIface)
                return ls;
        }
        return null;
        //throw new RuntimeException("Error, link setting from "+fromIface.getId()+" to "+toIface.getId()+" not found");
    }
    public LinkSetting getLinkSetting(String fromIfaceName, String toIfaceName) {
        for (LinkSetting ls : linkSettings) {
            if (ls.fromIface.getId().equals(fromIfaceName) &&
                    ls.toIface.getId().equals(toIfaceName)
                    )
                return ls;
        }
        return null;
        //throw new RuntimeException("Error, link setting from "+fromIface.getId()+" to "+toIface.getId()+" not found");
    }

    /** remove unneeded link settings, and add all needed link settings*/
    public void updateLinkSettings() {
//        System.out.println("updateLinkSettings interface count=" + interfaces.size());

        //remove unneeded link settings
        for (Iterator<LinkSetting> it = linkSettings.iterator(); it.hasNext();) {
            LinkSetting ls = it.next();
            if (!interfaces.contains(ls.fromIface) || !interfaces.contains(ls.toIface)) {
                //remove linksetting
                it.remove();
            }
            if (ls.fromIface ==ls.toIface) {
                //remove linksetting
                it.remove();
            }
        }

        //add all missing needed link settings
        for (RspecInterface fromIface : interfaces)
            for (RspecInterface toIface : interfaces) {
                if (fromIface == toIface)
                    continue;
                boolean missing = true;
                for (LinkSetting ls : linkSettings)
                    if (ls.fromIface == fromIface && ls.toIface == toIface)
                    {
                        missing = false;
                        break;
                    }
                if (missing) {
                    //missing settings
                    LinkSetting newLinkSetting = new LinkSetting(fromIface, toIface);
                    linkSettings.add(newLinkSetting);
                }
            }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final ObservableList<RspecInterface> interfaces = FXCollections.observableArrayList();
    public ObservableList<RspecInterface> getInterfaces() {
        return interfaces;
    }

    public RspecInterface getInterfaceForNode(RspecNode node) {
        for (RspecInterface iface : interfaces) {
            if (iface.getNode() == node)
                return iface;
        }
        return null;
    }

    public RspecInterface getInterfaceById(String id) {
        for (RspecInterface iface : interfaces) {
            if (iface.getId().equals(id))
                return iface;
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void writePropertiesToGeni3RequestRspec(LinkContents linkContents) {
        linkContents.setClientId(getId());

        for (String cmUrn : componentManagerUrns) {
            ComponentManager cm = new ComponentManager();
            cm.setName(cmUrn);
            linkContents.getAnyOrPropertyOrLinkType().add(cm);
        }
        for (String linkTypeName : linkTypes) {
            LinkType linkType = new LinkType();
            linkType.setName(linkTypeName);
            linkContents.getAnyOrPropertyOrLinkType().add(linkType);
        }

        for (LinkSetting ls : linkSettings) {
            LinkPropertyContents lpc = new LinkPropertyContents();
            lpc.setSourceId(ls.getFromIface().getId());
            lpc.setDestId(ls.getToIface().getId());
            if (ls.isCapacitySet()) lpc.setCapacity(ls.getCapacity_bps()/1000+""); //Rspec takes capacity it in kbits/s
            if (ls.isLatencySet()) lpc.setLatency(ls.getLatency_ms()+"");
            if (ls.isPacketLossSet()) lpc.setPacketLoss(ls.getPacketLoss()+"");
            linkContents.getAnyOrPropertyOrLinkType().add(lpc);
        }

        //TODO: jfed:link_info store location like flack does
    }

    //used for joining manifests
    private be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LinkContents xmlLinkContents;
    public be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LinkContents getXmlManifestLinkContents() { return xmlLinkContents; }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// WARNING: very dirty cut and paste code reuse between setPropertiesFromGeni3RequestRspec and setPropertiesFromGeni3ManifestRspec ! /////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void setPropertiesFromGeni3RequestRspec(LinkContents linkContents) {
        this.xmlLinkContents = null;//linkContents;

        linkSettings.clear();
        linkTypes.clear();
        componentManagerUrns.clear();

        for (Object o : linkContents.getAnyOrPropertyOrLinkType()) {
            boolean handled = false;

            //sometimes, it's wrapped in a JAXBElement
            Object value = o;
            if (o instanceof JAXBElement) {
                JAXBElement el = (JAXBElement) o;
                value = el.getValue();
            }

            if (value instanceof InterfaceRefContents)
                handled = true; //already handled in parent
            if (value instanceof LinkPropertyContents) {
                handled = true;
                LinkPropertyContents lpc = (LinkPropertyContents) value;
                LinkSetting ls = getLinkSetting(lpc.getSourceId(), lpc.getDestId());
                if (ls == null) {
                    System.err.println("Link Property of link \""+getId()+"\" references interfaces \""+
                            lpc.getSourceId()+"\" and \""+lpc.getDestId()+
                            "\", but could not find that combination in linkSettings list: "+linkSettings.toString()+
                            " with interface list="+interfaces.toString());
                } else {
                    if (lpc.getCapacity() == null)
                        ls.setCapacity_bps(0);
                    else
                        ls.setCapacity_bps(Integer.parseInt(lpc.getCapacity()) * 1000); //Rspec takes capacity it in kbits/s

                    if (lpc.getLatency() == null)
                        ls.setLatency_ms(0);
                    else
                        ls.setLatency_ms(Integer.parseInt(lpc.getLatency()));

                    if (lpc.getPacketLoss() == null)
                        ls.setPacketLoss(0.0);
                    else
                        ls.setPacketLoss(Double.parseDouble(lpc.getPacketLoss()));
                }
            }
            if (value instanceof ComponentManager) {
                handled = true;
                ComponentManager cm = (ComponentManager) value;
                componentManagerUrns.add(cm.getName());
            }
            if (value instanceof LinkType) {
                handled = true;
                LinkType linkType = (LinkType) value;
                //todo linkType.getClazz() is currently ignored
                linkTypes.add(linkType.getName());
            }

            if (!handled) {
                if (o instanceof JAXBElement) {
                    System.out.println("Unhandled rspec linkContents.getAnyOrPropertyOrLinkType() JAXBElement value class in RspecLink.setPropertiesFromGeni3RequestRspec: " + value.getClass().getName());
                } else {
                    System.out.println("Unhandled rspec linkContents.getAnyOrPropertyOrLinkType() object value class in RspecLink.setPropertiesFromGeni3RequestRspec: " + value.getClass().getName());
                }
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// WARNING: very dirty cut and paste code reuse between setPropertiesFromGeni3RequestRspec and setPropertiesFromGeni3ManifestRspec ! /////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void setPropertiesFromGeni3ManifestRspec(be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LinkContents linkContents) {
        assert linkContents != null;
        this.xmlLinkContents = linkContents;

        for (Object o : linkContents.getAnyOrPropertyOrLinkType()) {
            boolean handled = false;

            //sometimes, it's wrapped in a JAXBElement
            Object value = o;
            if (o instanceof JAXBElement) {
                JAXBElement el = (JAXBElement) o;
                value = el.getValue();
            }

            if (value instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.InterfaceRefContents)
                handled = true; //already handled in parent
            if (value instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LinkPropertyContents) {
                handled = true;
                be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LinkPropertyContents lpc = (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LinkPropertyContents) value;
                LinkSetting ls = getLinkSetting(lpc.getSourceId(), lpc.getDestId());
                if (ls == null) {
                    throw new RuntimeException("Link Property of link \""+getId()+"\" references interfaces \""+
                            lpc.getSourceId()+"\" and \""+lpc.getDestId()+
                            "\", but could not find both in interfaces list: "+interfaces.toString());
                }

                if (lpc.getCapacity() == null)
                    ls.setCapacity_bps(0);
                else
                    ls.setCapacity_bps(Integer.parseInt(lpc.getCapacity()) * 1000);  //Rspec takes capacity it in kbits/s

                if (lpc.getLatency() == null)
                    ls.setLatency_ms(0);
                else
                    ls.setLatency_ms(Integer.parseInt(lpc.getLatency()));

                if (lpc.getPacketLoss() == null)
                    ls.setPacketLoss(0.0);
                else
                    ls.setPacketLoss(Double.parseDouble(lpc.getPacketLoss()));
            }
            if (value instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.ComponentManager) {
                handled = true;
                be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.ComponentManager cm = (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.ComponentManager) value;
                componentManagerUrns.add(cm.getName());
            }
            if (value instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LinkType) {
                handled = true;
                be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LinkType linkType = (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LinkType) value;
                //todo linkType.getClazz() is currently ignored
                linkTypes.add(linkType.getName());
            }

            if (!handled) {
                if (o instanceof JAXBElement) {
                    System.out.println("Unhandled rspec linkContents.getAnyOrPropertyOrLinkType() JAXBElement value class in RspecLink.setPropertiesFromGeni3RequestRspec: " + value.getClass().getName());
                } else {
                    System.out.println("Unhandled rspec linkContents.getAnyOrPropertyOrLinkType() object value class in RspecLink.setPropertiesFromGeni3RequestRspec: " + value.getClass().getName());
                }
            }
        }
    } /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// WARNING: very dirty cut and paste code reuse between setPropertiesFromGeni3RequestRspec and setPropertiesFromGeni3ManifestRspec ! /////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void setPropertiesFromGeni3AdvertisementRspec(be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.LinkContents linkContents) {
        assert linkContents != null;

        for (Object o : linkContents.getAnyOrPropertyOrLinkType()) {
            boolean handled = false;

            //sometimes, it's wrapped in a JAXBElement
            Object value = o;
            if (o instanceof JAXBElement) {
                JAXBElement el = (JAXBElement) o;
                value = el.getValue();
            }

            if (value instanceof be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.InterfaceRefContents)
                handled = true; //already handled in parent
            if (value instanceof be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.LinkPropertyContents) {
                handled = true;
                be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.LinkPropertyContents lpc = (be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.LinkPropertyContents) value;
                LinkSetting ls = getLinkSetting(lpc.getSourceId(), lpc.getDestId());
                if (ls == null) {
                    throw new RuntimeException("Link Property of link \""+getId()+"\" references interfaces \""+
                            lpc.getSourceId()+"\" and \""+lpc.getDestId()+
                            "\", but could not find both in interfaces list: "+interfaces.toString());
                }

                if (lpc.getCapacity() == null)
                    ls.setCapacity_bps(0);
                else
                    ls.setCapacity_bps(Integer.parseInt(lpc.getCapacity()) * 1000);  //Rspec takes capacity it in kbits/s

                if (lpc.getLatency() == null)
                    ls.setLatency_ms(0);
                else
                    ls.setLatency_ms(Integer.parseInt(lpc.getLatency()));

                if (lpc.getPacketLoss() == null)
                    ls.setPacketLoss(0.0);
                else
                    ls.setPacketLoss(Double.parseDouble(lpc.getPacketLoss()));
            }
            if (value instanceof be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.ComponentManager) {
                handled = true;
                be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.ComponentManager cm = (be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.ComponentManager) value;
                componentManagerUrns.add(cm.getName());
            }
            if (value instanceof be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.LinkType) {
                handled = true;
                be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.LinkType linkType = (be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.LinkType) value;
                //todo linkType.getClazz() is currently ignored
                linkTypes.add(linkType.getName());
            }

            if (!handled) {
                if (o instanceof JAXBElement) {
                    System.out.println("Unhandled rspec linkContents.getAnyOrPropertyOrLinkType() JAXBElement value class in RspecLink.setPropertiesFromGeni3RequestRspec: " + value.getClass().getName());
                } else {
                    System.out.println("Unhandled rspec linkContents.getAnyOrPropertyOrLinkType() object value class in RspecLink.setPropertiesFromGeni3RequestRspec: " + value.getClass().getName());
                }
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// WARNING: very dirty cut and paste code reuse between setPropertiesFromGeni3RequestRspec and setPropertiesFromGeni3ManifestRspec ! /////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "RspecLink{" +
                "id=" + id +
                ", rspec=" + rspec +
                ", componentManagerUrns=" + componentManagerUrns +
                ", linkSettings=" + linkSettings +
                ", interfaces=" + interfaces +
                '}';
    }
}
