package be.iminds.ilabt.jfed.ui.rspeceditor.model;

import be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.*;
import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.*;
import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.DiskImageContents;
import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.ExecuteServiceContents;
import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.HardwareTypeContents;
import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.InstallServiceContents;
import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.InterfaceContents;
import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.LoginServiceContents;
import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.NodeContents;
import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.ServiceContents;
import be.iminds.ilabt.jfed.ui.rspeceditor.rspec_ext.binding.Location;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.xerces.dom.ElementNSImpl;
import org.w3c.dom.*;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RspecNode {
    @GuiEditable(nullable = false, clazz = String.class, guiName = "ID", guiHelp = "Unique name for this node")
    private final StringProperty id = new SimpleStringProperty();

    public RspecNode(String id) {
        assert id != null : "RspecNode needs id != null";
        this.id.setValue(id);
        this.setSliverTypeName("raw-pc");
        this.exclusive.set(true);
        this.editorX.set(-1.0);
        this.editorY.set(-1.0);
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

    private final DoubleProperty editorX = new SimpleDoubleProperty();
    public double getEditorX() {
        return editorX.get();
    }

    public void setEditorX(double value) {
        editorX.set(value);
    }

    public DoubleProperty editorXProperty() {
        return editorX;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final DoubleProperty editorY = new SimpleDoubleProperty();
    public double getEditorY() {
        return editorY.get();
    }

    public void setEditorY(double value) {
        editorY.set(value);
    }

    public DoubleProperty editorYProperty() {
        return editorY;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //    @GuiEditable(nullable = true, clazz = String.class, guiName = "Hardware Type", guiHelp = "Name of Hardware Type")
//    private final StringProperty hardwareTypeName = new SimpleStringProperty();
//    public String getHardwareTypeName() {
//        return hardwareTypeName.get();
//    }
//
//    public void setHardwareTypeName(String value) {
//        hardwareTypeName.set(value);
//    }
//
//    public StringProperty hardwareTypeNameProperty() {
//        return hardwareTypeName;
//    }
    @GuiEditable(nullable = false, clazz = ListProperty.class, listClass = String.class , guiName = "Hardware Type(s)", guiHelp = "Hardware type name(s)")
    private final ListProperty<String> hardwareTypes = new SimpleListProperty<>(FXCollections.<String>observableArrayList());

    public ListProperty<String> getHardwareTypes() {
        return hardwareTypes;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GuiEditable(nullable = false, clazz = Boolean.class, guiName = "Exclusive", guiHelp = "Exclusive")
    private final BooleanProperty exclusive = new SimpleBooleanProperty();
    public boolean getExclusive() {
        return exclusive.get();
    }

    public void setExclusive(boolean value) {
        exclusive.set(value);
    }

    public BooleanProperty exclusiveProperty() {
        return exclusive;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @GuiEditable(nullable = false, clazz = String.class, guiName = "Sliver Type", guiHelp = "Sliver Type (default \"raw-pc\")")
    private final StringProperty sliverTypeName = new SimpleStringProperty();

    public String getSliverTypeName() {
        return sliverTypeName.get();
    }

    public void setSliverTypeName(String value) {
        sliverTypeName.set(value);
    }

    public StringProperty sliverTypeNameProperty() {
        return sliverTypeName;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class NodeLocation {
        private String country;
        private String latitude;
        private String longitude;

        public NodeLocation(String country, String latitude, String longitude) {
            this.country = country;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getCountry() { return country; }
        public String getLatitude() { return latitude; }
        public String getLongitude() { return longitude; }
    }

    //TODO: add to GUI (needs support for NodeLocation)
//    @GuiEditable(nullable = false, clazz = NodeLocation.class, guiName = "Location", guiHelp = "Location of node")
    private final ObjectProperty<NodeLocation> location = new SimpleObjectProperty<NodeLocation>();

    public NodeLocation getLocation() {
        return location.get();
    }

    public void setLocation(NodeLocation value) {
        location.set(value);
    }

    public ObjectProperty<NodeLocation> locationProperty() {
        return location;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GuiEditable(nullable = true, clazz = String.class, guiName = "OS Image URN", guiHelp = "URN of the OS Image")
    private final StringProperty osImage = new SimpleStringProperty();

    public String getOsImage() {
        return osImage.get();
    }

    public void setOsImage(String value) {
        osImage.set(value);
    }

    public StringProperty osImageProperty() {
        return osImage;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GuiEditable(nullable = true, clazz = String.class, guiName = "Component Manager URN", guiHelp = "URN of the component manager for this node")
    private final StringProperty componentManagerId = new SimpleStringProperty();

    public String getComponentManagerId() {
        return componentManagerId.get();
    }

    public void setComponentManagerId(String value) {
        componentManagerId.set(value);
    }

    public StringProperty componentManagerIdProperty() {
        return componentManagerId;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GuiEditable(nullable = true, clazz = String.class, guiName = "Component URN", guiHelp = "URN of the specific component this node is assigned to")
    private final StringProperty componentId = new SimpleStringProperty();

    public String getComponentId() {
        return componentId.get();
    }

    public void setComponentId(String value) {
        componentId.set(value);
    }

    public StringProperty componentIdProperty() {
        return componentId;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GuiEditable(nullable = true, clazz = String.class, guiName = "Component Name", guiHelp = "Name of the specific component this node is assigned to")
    private final StringProperty componentName = new SimpleStringProperty();

    public String getComponentName() {
        return componentName.get();
    }

    public void setComponentName(String value) {
        componentName.set(value);
    }

    public StringProperty componentNameProperty() {
        return componentName;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GuiEditable(nullable = true, editable = false,  clazz = String.class, guiName = "DNS Name", guiHelp = "DNS name of the specific host this node is assigned to")
    private final StringProperty hostName = new SimpleStringProperty();

    public String getHostName() {
        return hostName.get();
    }

    public void setHostName(String value) {
        hostName.set(value);
    }

    public StringProperty hostNameProperty() {
        return hostName;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class ExecuteService {
        private StringProperty shell = new SimpleStringProperty();
        private StringProperty command = new SimpleStringProperty();

        public ExecuteService(String shell, String command) {
            this.shell.set(shell);
            this.command.set(command);
        }

        public ExecuteService(ExecuteService o) {
            shell.set(o.shell.get());
            command.set(o.command.get());
        }

        public String getShell() { return shell.get(); }
        public String getCommand() { return command.get(); }

        public void setShell(String shell) { this.shell.set(shell); }
        public void setCommand(String command) { this.command.set(command); }

        public StringProperty shellProperty() { return shell; }
        public StringProperty commandProperty() { return command; }
    }

    @GuiEditable(nullable = false, clazz = ListProperty.class, listClass = ExecuteService.class , guiName = "Execute Services", guiHelp = "Service script to execute on start")
    private final ListProperty<ExecuteService> executeServices = new SimpleListProperty<ExecuteService>(FXCollections.<ExecuteService>observableArrayList());

    public List<ExecuteService> getExecuteServices() {
        return executeServices.get();
    }

    public void setExecuteServices(List<ExecuteService> value) {
        executeServices.set(FXCollections.observableArrayList(value));
    }

    public ListProperty<ExecuteService> executeServicesProperty() {
        return executeServices;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class InstallService {
        private StringProperty installPath = new SimpleStringProperty();
        private StringProperty url = new SimpleStringProperty();

        public InstallService(String installPath, String url) {
            this.installPath.set(installPath);
            this.url.set(url);
        }

        public InstallService(InstallService o) {
            installPath.set(o.installPath.get());
            url.set(o.url.get());
        }

        public String getInstallPath() { return installPath.get(); }
        public String getUrl() { return url.get(); }

        public void setInstallPath(String installPath) { this.installPath.set(installPath); }
        public void setUrl(String url) { this.url.set(url); }

        public StringProperty installPathProperty() { return installPath; }
        public StringProperty urlProperty() { return url; }
    }

    @GuiEditable(nullable = false, clazz = ListProperty.class, listClass = InstallService.class , guiName = "Install Services", guiHelp = "Archives to extract on start")
    private final ListProperty<InstallService> installServices = new SimpleListProperty<InstallService>(FXCollections.<InstallService>observableArrayList());

    public List<InstallService> getInstallServices() {
        return installServices.get();
    }

    public void setInstallServices(List<InstallService> value) {
        installServices.set(FXCollections.observableArrayList(value));
    }

    public ListProperty<InstallService> installServicesProperty() {
        return installServices;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class LoginService {
        private StringProperty authentication = new SimpleStringProperty();
        private StringProperty hostname = new SimpleStringProperty();
        private StringProperty port = new SimpleStringProperty();
        private StringProperty username = new SimpleStringProperty();

        public LoginService(String authentication, String hostname, String port, String username) {
            this.authentication.set(authentication);
            this.hostname.set(hostname);
            this.port.set(port);
            this.username.set(username);
        }

        public LoginService(LoginService o) {
            authentication.set(o.authentication.get());
            hostname.set(o.hostname.get());
            port.set(o.port.get());
            username.set(o.username.get());
        }

        public String getAuthentication() { return authentication.get(); }
        public void setAuthentication(String authentication) { this.authentication.set(authentication); }
        public StringProperty authenticationProperty() { return authentication; }

        public String getHostname() { return hostname.get(); }
        public void setHostname(String hostname) { this.hostname.set(hostname); }
        public StringProperty hostnameProperty() { return hostname; }

        public String getPort() { return port.get(); }
        public void setPort(String port) { this.port.set(port); }
        public StringProperty portProperty() { return port; }

        public String getUsername() { return username.get(); }
        public void setUsername(String username) { this.username.set(username); }
        public StringProperty usernameProperty() { return username; }
    }

    @GuiEditable(nullable = false, editable=false, clazz = ListProperty.class, listClass = LoginService.class , guiName = "Login Services", guiHelp = "Methods to log in to the node")
    private final ListProperty<LoginService> loginServices = new SimpleListProperty<LoginService>(FXCollections.<LoginService>observableArrayList());

    public List<LoginService> getLoginServices() {
        return loginServices.get();
    }

    public void setLoginServices(List<LoginService> value) {
        loginServices.set(FXCollections.observableArrayList(value));
    }

    public ListProperty<LoginService> loginServicesProperty() {
        return loginServices;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private ObservableList<RspecInterface> interfaces = FXCollections.observableArrayList();
    public ObservableList<RspecInterface> getInterfaces() {
        return interfaces;
    }

    public List<RspecLink> getLinks() {
        List<RspecLink> res = new ArrayList<>();

        for (RspecInterface iface : interfaces)
            res.add(iface.getLink());

        return Collections.unmodifiableList(res);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
     * write properties from this RspecNode to the Geni3Rspec Request "NodeContents"
     *interfaces must not be added here. all the rest must be added (including id)
     */
    public void writePropertiesToGeni3RequestRspec(NodeContents nodeContents) {
        nodeContents.setClientId(getId());

        //TODO temporary hack to force exclusive to false for planetlab nodes
        if (sliverTypeName.get().equals("plab-vserver") && exclusive.get()) {
            exclusive.set(false);
            System.err.println("WARNING: In Geni3 RSpec request for planetlab, \"plab-vserver\" sliver type implies exclusive=false (but exclusive was set to \"true\"). Exclusive has been set to false.");
        }

        //quick hack to get ple to work
        if (componentManagerId.get() != null && componentManagerId.get().startsWith("urn:publicid:IDN+ple")) {
            System.out.println("HACK: forcing \"sliver\" element to planetlab request RSpec");
//            org.w3c.dom.Element sliverTag = doc.getOwnerDocument().createElement("sliver");
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document document = db.newDocument();
//                Element sliverTag = document.createElement("sliver");
                Element sliverTag = document.createElementNS("http://www.geni.net/resources/rspec/3", "sliver");

                nodeContents.getAnyOrRelationOrLocation().add(sliverTag);
                System.out.println("HACK: forcing \"sliver\" element to planetlab request RSpec --> success!");
            } catch (ParserConfigurationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }


        nodeContents.setExclusive(exclusive.get());
        if (componentManagerId.get() != null)
            nodeContents.setComponentManagerId(componentManagerId.get());
        if (componentId.get() != null)
            nodeContents.setComponentId(componentId.get());
        if (componentName.get() != null)
            nodeContents.setComponentName(componentName.get());

        assert sliverTypeName.get() != null;
        NodeContents.SliverType sliverType = new NodeContents.SliverType();
        sliverType.setName(sliverTypeName.get());

        if (sliverTypeName.get().equals("raw-pc") && !exclusive.get()) {
            //TODO make sure this can't be?
            System.err.println("WARNING: In Geni3 RSpec request, raw-pc sliver type implies exclusive (but exclusive is set to \"false\")");
        }

        if (osImage.get() != null) {
            // part of sliver_type:   <disk_image name="urn:publicid:IDN+wall3.test.ibbt.be+image+emulab-ops//DEB60_64-STD"/>
            DiskImageContents diskImage = new DiskImageContents();
            diskImage.setName(osImage.get());
            sliverType.getAnyOrDiskImage().add(diskImage);
        }

        nodeContents.getAnyOrRelationOrLocation().add(sliverType);

        NodeLocation nodeLocation = location.get();
        if (nodeLocation != null) {
            be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.LocationContents lc = new be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.LocationContents();
            lc.setCountry(nodeLocation.getCountry());
            lc.setLatitude(nodeLocation.getLatitude());
            lc.setLongitude(nodeLocation.getLongitude());
            nodeContents.getAnyOrRelationOrLocation().add(lc);
        }

        for (String harwareTypeName : hardwareTypes) {
            HardwareTypeContents hardwareTypeContents = new HardwareTypeContents();
            hardwareTypeContents.setName(harwareTypeName);
            nodeContents.getAnyOrRelationOrLocation().add(hardwareTypeContents);
        }

        if (executeServices.size() > 0 || installServices.size() > 0) {
            ServiceContents serviceContents = new ServiceContents();

            for (ExecuteService executeService : executeServices) {
                ExecuteServiceContents executeServiceContents = new ExecuteServiceContents();
                executeServiceContents.setShell(executeService.getShell());
                executeServiceContents.setCommand(executeService.getCommand());

                serviceContents.getAnyOrLoginOrInstall().add(executeServiceContents);
            }

            for (InstallService installService : installServices) {
                InstallServiceContents installServiceContents = new InstallServiceContents();
                installServiceContents.setInstallPath(installService.getInstallPath());
                installServiceContents.setUrl(installService.getUrl());

                serviceContents.getAnyOrLoginOrInstall().add(installServiceContents);
            }

            nodeContents.getAnyOrRelationOrLocation().add(serviceContents);
        }
        //TODO: specific node (bound/unbound node) in relation to properties: it adds
        //TODO       add    component_id="urn:publicid:IDN+wall3.test.ibbt.be+node+n095-20a" component_name="n095-20a"  +
        //TODO            + <location latitude="51.036145" longitude="3.734761" country="BE"/>

        if (editorX.get() >= 0.0 || editorY.get() >= 0.0) {
            Location location = new Location();
            if (editorX.get() >= 0.0) location.setX(editorX.get());
            if (editorY.get() >= 0.0) location.setY(editorY.get());
            nodeContents.getAnyOrRelationOrLocation().add(location);
        }
    }

    private be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.NodeContents xmlNodeContents;
    public be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.NodeContents getXmlManifestNodeContents() { return xmlNodeContents; }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// WARNING: very dirty cut and paste code reuse between setPropertiesFromGeni3RequestRspec and setPropertiesFromGeni3ManifestRspec ! /////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
   * read properties from the Geni3Rspec Request "NodeContents" and write them to this RSpecNode
   * */
    public void setPropertiesFromGeni3ManifestRspec(be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.NodeContents nodeContents) {
        assert nodeContents != null;
        this.xmlNodeContents = nodeContents;
        exclusive.set(nodeContents.isExclusive() == null ? false : nodeContents.isExclusive());
        componentManagerId.set(nodeContents.getComponentManagerId());
        componentId.set(nodeContents.getComponentId());
        componentName.set(nodeContents.getComponentName());

        for (Object o : nodeContents.getAnyOrRelationOrLocation()) {
            boolean handled = false;

            Object value = o;

            if (o instanceof JAXBElement) {
                JAXBElement el = (JAXBElement) o;
                value = el.getValue();
            }

            if (value instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.InterfaceContents)
                handled = true; //already handled by Rspec class
            if (value instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.NodeContents.SliverType) {
                handled = true;
                be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.NodeContents.SliverType st = (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.NodeContents.SliverType) value;
                sliverTypeName.set(st.getName());

                for (Object o2 : st.getAnyOrDiskImage()) {
                    JAXBElement el2 = (JAXBElement) o2;
                    if (el2.getValue() instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.DiskImageContents) {
                        be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.DiskImageContents di = (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.DiskImageContents) el2.getValue();
                        osImage.set(di.getName());
                    }
                }
            }

            if (value instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.HardwareTypeContents) {
                handled = true;
                be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.HardwareTypeContents ht = (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.HardwareTypeContents) value;
                hardwareTypes.add(ht.getName());
            }

            if (value instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.ServiceContents) {
                handled = true;
                be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.ServiceContents sc = (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.ServiceContents) value;

                loginServices.clear();
                installServices.clear();
                executeServices.clear();

                for (Object o2 : sc.getAnyOrLoginOrInstall()) {
                    JAXBElement el2 = (JAXBElement) o2;
                    if (el2.getValue() instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LoginServiceContents) {
                        be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LoginServiceContents loginServiceContents =
                                (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LoginServiceContents) el2.getValue();
                        String username = loginServiceContents.getUsername();
                        loginServices.add(new LoginService(loginServiceContents.getAuthentication(), loginServiceContents.getHostname(), loginServiceContents.getPort(), username));
                    }
                    if (el2.getValue() instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.InstallServiceContents) {
                        be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.InstallServiceContents installServiceContents = (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.InstallServiceContents) el2.getValue();
                        installServices.add(new InstallService(installServiceContents.getInstallPath(), installServiceContents.getUrl()));
                    }
                    if (el2.getValue() instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.ExecuteServiceContents) {
                        be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.ExecuteServiceContents executeServiceContents = (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.ExecuteServiceContents) el2.getValue();
                        executeServices.add(new ExecuteService(executeServiceContents.getShell(), executeServiceContents.getCommand()));
                    }
                }
            }

            if (value instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.Host) {
                be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.Host host = (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.Host) value;
                hostName.set(host.getName());
            }

            if (value instanceof Location) {
                handled = true;
                Location location = (Location) value;
                if (location.getX() != null) editorX.set(location.getX());
                if (location.getY() != null) editorY.set(location.getY());
            }

            if (!handled)
                if (o instanceof JAXBElement) {
                    System.out.println("RSpecNode.setPropertiesFromGeni3RequestRspec Unhandled JAXBElement XML: type=" + value.getClass().getName()+" val="+value);
                } else {
                    System.out.println("RSpecNode.setPropertiesFromGeni3RequestRspec Unhandled XML: type=" + value.getClass().getName()+" val="+value);
                }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// WARNING: very dirty cut and paste code reuse between setPropertiesFromGeni3RequestRspec and setPropertiesFromGeni3ManifestRspec and setPropertiesFromGeni3AdvertisementRspec! /////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
   * read properties from the Geni3Rspec Request "NodeContents" and write them to this RSpecNode
   * */
    public void setPropertiesFromGeni3AdvertisementRspec(be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.NodeContents nodeContents) {
        assert nodeContents != null;
        exclusive.set(nodeContents.isExclusive());
        componentManagerId.set(nodeContents.getComponentManagerId());
        componentId.set(nodeContents.getComponentId());
        componentName.set(nodeContents.getComponentName());

        for (Object o : nodeContents.getAnyOrRelationOrLocation()) {
            boolean handled = false;

            Object value = o;

            if (o instanceof JAXBElement) {
                JAXBElement el = (JAXBElement) o;
                value = el.getValue();
            }

            if (value instanceof be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.InterfaceContents)
                handled = true; //already handled by Rspec class
            if (value instanceof be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.NodeContents.SliverType) {
                handled = true;
                be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.NodeContents.SliverType st = (be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.NodeContents.SliverType) value;
                sliverTypeName.set(st.getName());

                for (Object o2 : st.getAnyOrDiskImage()) {
                    boolean handled2 = false;
                    Object value2 = o2;
                    if (o2 instanceof JAXBElement) {
                        JAXBElement el2 = (JAXBElement) o2;
                        value2 = el2.getValue();
                    }

                    if (o2 instanceof ElementNSImpl) {
                        ElementNSImpl ns = (ElementNSImpl) o2;
                        System.out.println("RSpec advertisement node parsing TODO: getAnyOrDiskImage encountered ElementNSImpl, value="+ns);
                        handled2 = true;
                    }

                    if (value2 instanceof be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.DiskImageContents) {
                        be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.DiskImageContents di = (be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.DiskImageContents) value2;
                        osImage.set(di.getName());
                        handled2 = true;
                    }

                    if (!handled2)
                        if (o2 instanceof JAXBElement) {
                            System.out.println("RSpecNode.setPropertiesFromGeni3RequestRspec getAnyOrDiskImage Unhandled JAXBElement XML: type=" + value2.getClass().getName()+" val="+value2);
                        } else {
                            System.out.println("RSpecNode.setPropertiesFromGeni3RequestRspec getAnyOrDiskImage Unhandled XML: type=" + value2.getClass().getName()+" val="+value2);
                        }
                }
            }

            if (value instanceof be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.HardwareTypeContents) {
                handled = true;
                be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.HardwareTypeContents ht = (be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.HardwareTypeContents) value;
                hardwareTypes.add(ht.getName());
            }

            if (value instanceof be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.LocationContents) {
                handled = true;
                be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.LocationContents l = (be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.LocationContents) value;
                NodeLocation nodeLocation = new NodeLocation(l.getCountry(), l.getLatitude(), l.getLongitude());
                location.set(nodeLocation);
            }

            if (value instanceof be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.AvailableContents) {
                handled = true;
                be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.AvailableContents a = (be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.AvailableContents) value;
                //TODO handle AvailableContents
                System.out.println("RSpec advertisement node parsing TODO: handle AvailableContents");
            }

            if (value instanceof be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.ServiceContents) {
                handled = true;
                be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.ServiceContents sc = (be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.ServiceContents) value;

                for (Object o2 : sc.getAnyOrLoginOrInstall()) {
                    JAXBElement el2 = (JAXBElement) o2;
                    if (el2.getValue() instanceof be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.LoginServiceContents) {
                        be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.LoginServiceContents loginServiceContents = (be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.LoginServiceContents) el2.getValue();
                        String username = null;
                        loginServices.add(new LoginService(loginServiceContents.getAuthentication(), loginServiceContents.getHostname(), loginServiceContents.getPort(), null));
                    }
                }
            }

            if (value instanceof Location) {
                handled = true;
                Location location = (Location) value;
                if (location.getX() != null) editorX.set(location.getX());
                if (location.getY() != null) editorY.set(location.getY());
            }

            if (!handled)
                if (o instanceof JAXBElement) {
                    System.out.println("RSpecNode.setPropertiesFromGeni3RequestRspec Unhandled JAXBElement XML: type=" + value.getClass().getName()+" val="+value);
                } else {
                    System.out.println("RSpecNode.setPropertiesFromGeni3RequestRspec Unhandled XML: type=" + value.getClass().getName()+" val="+value);
                }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// WARNING: very dirty cut and paste code reuse between setPropertiesFromGeni3RequestRspec and setPropertiesFromGeni3ManifestRspec  and setPropertiesFromGeni3AdvertisementRspec! /////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
   * read properties from the Geni3Rspec Request "NodeContents" and write them to this RSpecNode
   * */
    public void setPropertiesFromGeni3RequestRspec(NodeContents nodeContents) {
        this.xmlNodeContents = null;//nodeContents;
        exclusive.set(nodeContents.isExclusive());
        componentManagerId.set(nodeContents.getComponentManagerId());
        componentId.set(nodeContents.getComponentId());
        componentName.set(nodeContents.getComponentName());

        for (Object o : nodeContents.getAnyOrRelationOrLocation()) {
            boolean handled = false;

            Object value = o;

            if (o instanceof JAXBElement) {
                JAXBElement el = (JAXBElement) o;
                value = el.getValue();
            }

            if (value instanceof InterfaceContents)
                handled = true; //already handled by Rspec class
            if (value instanceof NodeContents.SliverType) {
                handled = true;
                NodeContents.SliverType st = (NodeContents.SliverType) value;
                sliverTypeName.set(st.getName());

                for (Object o2 : st.getAnyOrDiskImage()) {
                    JAXBElement el2 = (JAXBElement) o2;
                    if (el2.getValue() instanceof DiskImageContents) {
                        DiskImageContents di = (DiskImageContents) el2.getValue();
                        osImage.set(di.getName());
                    }
                }
            }

            if (value instanceof HardwareTypeContents) {
                handled = true;
                HardwareTypeContents ht = (HardwareTypeContents) value;
                hardwareTypes.add(ht.getName());
            }

            if (value instanceof ServiceContents) {
                handled = true;
                ServiceContents sc = (ServiceContents) value;

                for (Object o2 : sc.getAnyOrLoginOrInstall()) {
                    JAXBElement el2 = (JAXBElement) o2;
                    if (el2.getValue() instanceof LoginServiceContents) {
                        LoginServiceContents loginServiceContents = (LoginServiceContents) el2.getValue();
                        loginServices.add(new LoginService(loginServiceContents.getAuthentication(), loginServiceContents.getHostname(), loginServiceContents.getPort(), null));
                    }
                    if (el2.getValue() instanceof InstallServiceContents) {
                        InstallServiceContents installServiceContents = (InstallServiceContents) el2.getValue();
                        installServices.add(new InstallService(installServiceContents.getInstallPath(), installServiceContents.getUrl()));
                    }
                    if (el2.getValue() instanceof ExecuteServiceContents) {
                        ExecuteServiceContents executeServiceContents = (ExecuteServiceContents) el2.getValue();
                        executeServices.add(new ExecuteService(executeServiceContents.getShell(), executeServiceContents.getCommand()));
                    }
                }
            }

            if (value instanceof Location) {
                handled = true;
                Location location = (Location) value;
                if (location.getX() != null) editorX.set(location.getX());
                if (location.getY() != null) editorY.set(location.getY());
            }

            if (!handled)
                if (o instanceof JAXBElement) {
                    System.out.println("RSpecNode.setPropertiesFromGeni3RequestRspec Unhandled JAXBElement XML: type=" + value.getClass().getName()+" val="+value);
                } else {
                    System.out.println("RSpecNode.setPropertiesFromGeni3RequestRspec Unhandled XML: type=" + value.getClass().getName()+" val="+value);
                }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// WARNING: very dirty cut and paste code reuse between setPropertiesFromGeni3RequestRspec and setPropertiesFromGeni3ManifestRspec and setPropertiesFromGeni3AdvertisementRspec ! /////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "RspecNode{" +
                "id=" + id +
                ", editorX=" + editorX +
                ", editorY=" + editorY +
                ", hardwareTypes=" + hardwareTypes +
                ", exclusive=" + exclusive +
                ", sliverTypeName=" + sliverTypeName +
                ", osImage=" + osImage +
                ", componentManagerId=" + componentManagerId +
                ", componentId=" + componentId +
                ", componentName=" + componentName +
                ", executeServices=" + executeServices +
                ", installServices=" + installServices +
                ", interfaces=" + interfaces +
                '}';
    }
}
