package be.iminds.ilabt.jfed.highlevel.model;

import be.iminds.ilabt.jfed.lowlevel.authority.DebuggingAuthorityList;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.Rspec;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Random;

/**
 * EasyModelDebugFaker
 */
public class EasyModelDebugFaker {
    private EasyModel easyModel;
    private Slice slice1;
    private Slice slice2;
    private Sliver sliver1;
    private Sliver sliver2;
    private Sliver sliver3;

    public EasyModelDebugFaker(EasyModel easyModel) {
        this.easyModel = easyModel;
    }

    private final String FAKE_REQUEST_RSPEC =
            "<rspec type=\"request\" generated=\"2013-01-16T14:20:39Z\" xsi:schemaLocation=\"http://www.geni.net/resources/rspec/3 http://www.geni.net/resources/rspec/3/request.xsd \" xmlns:client=\"http://www.protogeni.net/resources/rspec/ext/client/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.geni.net/resources/rspec/3\">\n" +
                    "  <node client_id=\"PC\" component_manager_id=\"urn:publicid:IDN+wall3.test.ibbt.be+authority+cm\" exclusive=\"true\">\n" +
                    "    <sliver_type name=\"raw-pc\"/>\n" +
                    "  </node>\n" +
                    "</rspec>\n";

    public void addDebugAuth() {
        AuthorityList authorityList = easyModel.getAuthorityList();
        DebuggingAuthorityList.load(authorityList.getAuthorityListModel());
    }

    public void addFakeInfo() {
        assert easyModel.getAuthorityList() != null;
        AuthorityInfo auth = easyModel.getAuthorityList().get("urn:publicid:IDN+wall3.test.ibbt.be+authority+cm");
        assert auth != null : "wall3 is not in authorityList. List size: "+easyModel.getAuthorityList().authorityInfosProperty().size();

        slice1 = easyModel.logExistSliceName(auth.getGeniAuthority(), "FakeSlice1");
        slice2 = easyModel.logExistSliceName(auth.getGeniAuthority(), "FakeSlice2");
//        Sliver sliver = new Sliver("urn:publicid:IDN+wall3.test.ibbt.be+sliver+fakesliver", slice1,
//                new RSpecInfo(FAKE_REQUEST_RSPEC, RSpecInfo.RspecType.REQUEST, slice1, null, auth),
//                new RSpecInfo("", RSpecInfo.RspecType.MANIFEST, slice1, null, auth),
//                auth.getGeniAuthority());
//        slice1.addSliver(sliver);
    }

    public void addCm() {
        if (easyModel.getSlices().isEmpty()) {
            System.err.println("WARNING: no slice!");
            return;
        }
        Slice slice = easyModel.getSlices().get(0);
        slice.requestComponentManagersProperty().add("urn:publicid:IDN+wall3.test.ibbt.be+authority+cm");
        slice.requestComponentManagersProperty().add("urn:publicid:IDN+emulab.net+authority+cm");
    }

    private Random rand = new Random();

    public void refreshStatus() {
        AuthorityInfo auth1 = easyModel.getAuthorityList().get("urn:publicid:IDN+wall3.test.ibbt.be+authority+cm");
        AuthorityInfo auth2 = easyModel.getAuthorityList().get("urn:publicid:IDN+wilab2.ilabt.iminds.be+authority+cm");
        AuthorityInfo auth3 = easyModel.getAuthorityList().get("urn:publicid:IDN+emulab.net+authority+cm");

        if (slice1 == null) addFakeInfo();
        sliver1 = easyModel.logExistSliverGeniSingle(slice1.getUrn(), auth1.getGeniAuthority());
        sliver2 = easyModel.logExistSliverGeniSingle(slice1.getUrn(), auth2.getGeniAuthority());
        sliver3 = easyModel.logExistSliverGeniSingle(slice1.getUrn(), auth3.getGeniAuthority());
        ObservableList<Sliver> slivers = FXCollections.observableArrayList();
        slivers.addAll(sliver1, sliver2, sliver3);

        for (Sliver sliver: slivers) {
            switch (rand.nextInt(6)) {
                case 0: { sliver.setStatus(Status.READY); sliver.setStatusString("Fake READY"); break; }
                case 1: { sliver.setStatus(Status.FAIL); sliver.setStatusString("Fake FAIL"); break; }
                case 2: { sliver.setStatus(Status.CHANGING); sliver.setStatusString("Fake CHANGING"); break; }
                case 3: { sliver.setStatus(Status.UNALLOCATED); sliver.setStatusString("Fake UNALLOCATED"); break; }
                case 4: { sliver.setStatus(Status.UNINITIALISED); sliver.setStatusString("Fake UNINITIALISED"); break; }
                case 5: { sliver.setStatus(Status.UNKNOWN); sliver.setStatusString("Fake UNKNOWN"); break; }
            }
        }
    }
    public void refreshManifest() {
        AuthorityInfo auth1 = easyModel.getAuthorityList().get("urn:publicid:IDN+wall3.test.ibbt.be+authority+cm");
        AuthorityInfo auth2 = easyModel.getAuthorityList().get("urn:publicid:IDN+wall3.test.ibbt.be+authority+cm");
        AuthorityInfo auth3 = easyModel.getAuthorityList().get("urn:publicid:IDN+wall3.test.ibbt.be+authority+cm");

        if (slice1 == null) addFakeInfo();
        sliver1 = easyModel.logExistSliverGeniSingle(slice1.getUrn(), auth1.getGeniAuthority());
        sliver2 = easyModel.logExistSliverGeniSingle(slice1.getUrn(), auth2.getGeniAuthority());
        sliver3 = easyModel.logExistSliverGeniSingle(slice1.getUrn(), auth3.getGeniAuthority());
        ObservableList<Sliver> slivers = FXCollections.observableArrayList();
        slivers.addAll(sliver1, sliver2, sliver3);

        for (Sliver sliver: slivers) {
            //TODO
            System.err.println("TODO: EassyModelDebugFaker.refreshManifest()");
        }
    }

    public void addSliceManifest() {
        String manifestRspec = "<rspec xmlns=\"http://www.geni.net/resources/rspec/3\" xmlns:flack=\"http://www.protogeni.net/resources/rspec/ext/flack/1\" xmlns:client=\"http://www.protogeni.net/resources/rspec/ext/client/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" type=\"manifest\" generated_by=\"Flack\" generated=\"2013-01-11T10:30:10Z\" xsi:schemaLocation=\"http://www.geni.net/resources/rspec/3 http://www.geni.net/resources/rspec/3/request.xsd \">\n" +
                "  <node xmlns:flack=\"http://www.protogeni.net/resources/rspec/ext/flack/1\" client_id=\"PC\" component_manager_id=\"urn:publicid:IDN+wall3.test.ibbt.be+authority+cm\" exclusive=\"true\" component_id=\"urn:publicid:IDN+wall3.test.ibbt.be+node+n097-20b\" sliver_id=\"urn:publicid:IDN+wall3.test.ibbt.be+sliver+233\">\n" +
                "    <sliver_type name=\"raw-pc\"/>\n" +
                "    <interface client_id=\"PC:if0\" component_id=\"urn:publicid:IDN+wall3.test.ibbt.be+interface+n097-20b:eth3\" sliver_id=\"urn:publicid:IDN+wall3.test.ibbt.be+sliver+236\" mac_address=\"00259065bced\">\n" +
                "      <flack:interface_info addressUnset=\"true\" bound=\"false\"/>\n" +
                "    <ip address=\"10.10.1.1\" type=\"ipv4\"/></interface>\n" +
                "    <flack:node_info x=\"67\" y=\"116\" unbound=\"true\"/>\n" +
                "  <rs:vnode xmlns:rs=\"http://www.protogeni.net/resources/rspec/ext/emulab/1\" name=\"n097-20b\"/><host name=\"PC.TestSlice3.wall3-test-ibbt-be.wall3.test.ibbt.be\"/><services><login authentication=\"ssh-keys\" hostname=\"n097-20b.wall3.test.ibbt.be\" port=\"22\" username=\"wvdemeer\"/></services></node>\n" +
                "  <node xmlns:flack=\"http://www.protogeni.net/resources/rspec/ext/flack/1\" client_id=\"PC-0\" component_manager_id=\"urn:publicid:IDN+wall3.test.ibbt.be+authority+cm\" exclusive=\"true\" component_id=\"urn:publicid:IDN+wall3.test.ibbt.be+node+n097-19a\" sliver_id=\"urn:publicid:IDN+wall3.test.ibbt.be+sliver+234\">\n" +
                "    <sliver_type name=\"raw-pc\"/>\n" +
                "    <interface client_id=\"PC-0:if0\" component_id=\"urn:publicid:IDN+wall3.test.ibbt.be+interface+n097-19a:eth3\" sliver_id=\"urn:publicid:IDN+wall3.test.ibbt.be+sliver+237\" mac_address=\"00259065be5d\">\n" +
                "      <flack:interface_info addressUnset=\"true\" bound=\"false\"/>\n" +
                "    <ip address=\"10.10.1.2\" type=\"ipv4\"/></interface>\n" +
                "    <flack:node_info x=\"375\" y=\"172\" unbound=\"true\"/>\n" +
                "    <rs:vnode xmlns:rs=\"http://www.protogeni.net/resources/rspec/ext/emulab/1\" name=\"n097-19a\"/>"+
                "    <host name=\"PC-0.TestSlice3.wall3-test-ibbt-be.wall3.test.ibbt.be\"/>"+
                "    <services><login authentication=\"ssh-keys\" hostname=\"n097-19a.wall3.test.ibbt.be\" port=\"22\" username=\"wvdemeer\"/></services>"+
                "  </node>\n" +
                "  <link xmlns:flack=\"http://www.protogeni.net/resources/rspec/ext/flack/1\" client_id=\"lan0\" sliver_id=\"urn:publicid:IDN+wall3.test.ibbt.be+sliver+235\" vlantag=\"116\">\n" +
                "    <component_manager name=\"urn:publicid:IDN+wall3.test.ibbt.be+authority+cm\"/>\n" +
                "    <interface_ref client_id=\"PC:if0\" component_id=\"urn:publicid:IDN+wall3.test.ibbt.be+interface+n097-20b:eth3\" sliver_id=\"urn:publicid:IDN+wall3.test.ibbt.be+sliver+236\"/>\n" +
                "    <interface_ref client_id=\"PC-0:if0\" component_id=\"urn:publicid:IDN+wall3.test.ibbt.be+interface+n097-19a:eth3\" sliver_id=\"urn:publicid:IDN+wall3.test.ibbt.be+sliver+237\"/>\n" +
                "    <property source_id=\"PC:if0\" dest_id=\"PC-0:if0\" capacity=\"1000000\"/>\n" +
                "    <property source_id=\"PC-0:if0\" dest_id=\"PC:if0\" capacity=\"1000000\"/>\n" +
                "    <link_type name=\"lan\"/>\n" +
                "    <flack:link_info x=\"-1\" y=\"-1\" unboundVlantag=\"true\"/>\n" +
                "  </link>\n" +
                "  <client:client_info name=\"Flack\" environment=\"Flash Version: LNX 11,2,202,258, OS: Linux 2.6.32-34-generic, Arch: x86, Screen: 1920x1080 @ 72 DPI with touchscreen type none\" version=\"v14.46\" url=\"https://www.emulab.net/protogeni/flack2/flack.swf?mode=public&amp;debug=0&amp;usejs=1\"/>\n" +
                "  <flack:slice_info view=\"graph\"/>\n" +
                "</rspec>\n" +
                "\n";

        Rspec rSpec = Rspec.fromGeni3ManifestRspecXML(manifestRspec);

        slice1.manifestRspecProperty().set(rSpec);
    }
    public void addSliceRequest() {
        String manifestRspec = "<rspec type=\"request\" generated_by=\"Flack\" generated=\"2013-01-11T10:30:10Z\" xsi:schemaLocation=\"http://www.geni.net/resources/rspec/3 http://www.geni.net/resources/rspec/3/request.xsd \" xmlns:flack=\"http://www.protogeni.net/resources/rspec/ext/flack/1\" xmlns:client=\"http://www.protogeni.net/resources/rspec/ext/client/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.geni.net/resources/rspec/3\">\n" +
                "  <node client_id=\"PC\" component_manager_id=\"urn:publicid:IDN+wall3.test.ibbt.be+authority+cm\" exclusive=\"true\">\n" +
                "    <sliver_type name=\"raw-pc\"/>\n" +
                "    <interface client_id=\"PC:if0\">\n" +
                "      <flack:interface_info addressUnset=\"true\" bound=\"false\"/>\n" +
                "    </interface>\n" +
                "    <flack:node_info x=\"67\" y=\"116\" unbound=\"true\"/>\n" +
                "  </node>\n" +
                "  <node client_id=\"PC-0\" component_manager_id=\"urn:publicid:IDN+wall3.test.ibbt.be+authority+cm\" exclusive=\"true\">\n" +
                "    <sliver_type name=\"raw-pc\"/>\n" +
                "    <interface client_id=\"PC-0:if0\">\n" +
                "      <flack:interface_info addressUnset=\"true\" bound=\"false\"/>\n" +
                "    </interface>\n" +
                "    <flack:node_info x=\"375\" y=\"172\" unbound=\"true\"/>\n" +
                "  </node>\n" +
                "  <link client_id=\"lan0\">\n" +
                "    <component_manager name=\"urn:publicid:IDN+wall3.test.ibbt.be+authority+cm\"/>\n" +
                "    <interface_ref client_id=\"PC:if0\"/>\n" +
                "    <interface_ref client_id=\"PC-0:if0\"/>\n" +
                "    <property source_id=\"PC:if0\" dest_id=\"PC-0:if0\" capacity=\"1000000\"/>\n" +
                "    <property source_id=\"PC-0:if0\" dest_id=\"PC:if0\" capacity=\"1000000\"/>\n" +
                "    <link_type name=\"lan\"/>\n" +
                "    <flack:link_info x=\"-1\" y=\"-1\" unboundVlantag=\"true\"/>\n" +
                "  </link>\n" +
                "  <client:client_info name=\"Flack\" environment=\"Flash Version: LNX 11,2,202,258, OS: Linux 2.6.32-34-generic, Arch: x86, Screen: 1920x1080 @ 72 DPI with touchscreen type none\" version=\"v14.46\" url=\"https://www.emulab.net/protogeni/flack2/flack.swf?mode=public&amp;debug=0&amp;usejs=1\"/>\n" +
                "  <flack:slice_info view=\"graph\"/>\n" +
                "</rspec>\n";

        Rspec rSpec = Rspec.fromGeni3RequestRspecXML(manifestRspec);

        slice1.manifestRspecProperty().set(rSpec);
    }

    public void fakeAdvertisement() {
        AuthorityInfo auth = easyModel.getAuthorityList().get("urn:publicid:IDN+wall3.test.ibbt.be+authority+cm");
        String advertisementRspec = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rspec xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.geni.net/resources/rspec/3\" xmlns:emulab=\"http://www.protogeni.net/resources/rspec/ext/emulab/1\" xsi:schemaLocation=\"http://www.geni.net/resources/rspec/3 http://www.geni.net/resources/rspec/3/ad.xsd http://www.protogeni.net/resources/rspec/ext/emulab/1 http://www.protogeni.net/resources/rspec/ext/emulab/1/ptop_extension.xsd \" type=\"advertisement\" generated=\"2013-06-20T09:29:29Z\" expires=\"2013-06-20T09:29:29Z\">\n" +
                "  <node component_manager_id=\"urn:publicid:IDN+wall3.test.ibbt.be+authority+cm\" component_name=\"c300b\" component_id=\"urn:publicid:IDN+wall3.test.ibbt.be+node+c300b\" exclusive=\"true\">\n" +
                "    <hardware_type name=\"switch\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"lan\">\n" +
                "      <emulab:node_type type_slots=\"unlimited\" static=\"true\"/>\n" +
                "    </hardware_type>\n" +
                "    <available now=\"true\"/>\n" +
                "    <location country=\"BE\" longitude=\"3.734761\" latitude=\"51.036145\"/>\n" +
                "    <emulab:fd name=\"real-switch\" weight=\"0\"/>\n" +
                "  </node>\n" +
                "  <node component_manager_id=\"urn:publicid:IDN+wall3.test.ibbt.be+authority+cm\" component_name=\"n097-15a\" component_id=\"urn:publicid:IDN+wall3.test.ibbt.be+node+n097-15a\" exclusive=\"true\">\n" +
                "    <hardware_type name=\"pcgen03-5p\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <sliver_type name=\"raw-pc\">\n" +
                "      <disk_image name=\"urn:publicid:IDN+wall3.test.ibbt.be+image+emulab-ops:DEB60_64-STD\" os=\"Linux\" version=\"6\" description=\"Debian 6.0 Squeeze 64-bit\" default=\"true\"/>\n" +
                "      <disk_image name=\"urn:publicid:IDN+wall3.test.ibbt.be+image+emulab-ops:DEB60_64-STD\" os=\"Linux\" version=\"6\" description=\"Debian 6.0 Squeeze 64-bit\" default=\"true\"/>\n" +
                "    </sliver_type>\n" +
                "    <hardware_type name=\"pc\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"delay\">\n" +
                "      <emulab:node_type type_slots=\"2\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"delay-pcgen03-5p\">\n" +
                "      <emulab:node_type type_slots=\"2\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"pcgen03-5p-vm\">\n" +
                "      <emulab:node_type type_slots=\"20\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"pcvmgen03-5p\">\n" +
                "      <emulab:node_type type_slots=\"20\"/>\n" +
                "    </hardware_type>\n" +
                "    <sliver_type name=\"emulab-openvz\"/>\n" +
                "    <hardware_type name=\"pcvm\">\n" +
                "      <emulab:node_type type_slots=\"20\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"lan\">\n" +
                "      <emulab:node_type type_slots=\"unlimited\" static=\"true\"/>\n" +
                "    </hardware_type>\n" +
                "    <available now=\"true\"/>\n" +
                "    <location country=\"BE\" longitude=\"3.734761\" latitude=\"51.036145\"/>\n" +
                "    <emulab:trivial_bandwidth value=\"400000\"/>\n" +
                "    <emulab:fd name=\"virtpercent\" weight=\"100\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"cpu\" weight=\"2400\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"ram\" weight=\"24053\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"cpupercent\" weight=\"92\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"rampercent\" weight=\"80\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"5ports\" weight=\"0.6\"/>\n" +
                "    <emulab:fd name=\"OS-10100\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10095\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10094\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10074\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10066\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10097\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10018\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10069\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10108\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10127\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10018\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10077\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10098\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10055\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10121\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"connected-to-c300b\" weight=\"0.0\"/>\n" +
                "    <emulab:fd name=\"poweroff\" weight=\"0.9\"/>\n" +
                "  </node>\n" +
                "  <node component_manager_id=\"urn:publicid:IDN+wall3.test.ibbt.be+authority+cm\" component_name=\"n095-04a\" component_id=\"urn:publicid:IDN+wall3.test.ibbt.be+node+n095-04a\" exclusive=\"true\">\n" +
                "    <hardware_type name=\"pcgen03-1p\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <sliver_type name=\"raw-pc\">\n" +
                "      <disk_image name=\"urn:publicid:IDN+wall3.test.ibbt.be+image+emulab-ops:DEB60_64-STD\" os=\"Linux\" version=\"6\" description=\"Debian 6.0 Squeeze 64-bit\" default=\"true\"/>\n" +
                "      <disk_image name=\"urn:publicid:IDN+wall3.test.ibbt.be+image+emulab-ops:DEB60_64-STD\" os=\"Linux\" version=\"6\" description=\"Debian 6.0 Squeeze 64-bit\" default=\"true\"/>\n" +
                "    </sliver_type>\n" +
                "    <hardware_type name=\"pc\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"pcgen03-1p-vm\">\n" +
                "      <emulab:node_type type_slots=\"20\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"pcvmgen03-1p\">\n" +
                "      <emulab:node_type type_slots=\"20\"/>\n" +
                "    </hardware_type>\n" +
                "    <sliver_type name=\"emulab-openvz\"/>\n" +
                "    <hardware_type name=\"pcvm\">\n" +
                "      <emulab:node_type type_slots=\"20\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"lan\">\n" +
                "      <emulab:node_type type_slots=\"unlimited\" static=\"true\"/>\n" +
                "    </hardware_type>\n" +
                "    <available now=\"true\"/>\n" +
                "    <location country=\"BE\" longitude=\"3.734761\" latitude=\"51.036145\"/>\n" +
                "    <emulab:trivial_bandwidth value=\"400000\"/>\n" +
                "    <emulab:fd name=\"virtpercent\" weight=\"100\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"cpu\" weight=\"2400\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"ram\" weight=\"24053\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"cpupercent\" weight=\"92\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"rampercent\" weight=\"80\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"OS-10100\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10095\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10094\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10074\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10066\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10097\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10018\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10069\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10108\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10127\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10018\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10077\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10098\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10055\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10121\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"connected-to-c300b\" weight=\"0.0\"/>\n" +
                "  </node>\n" +
                "  <node component_manager_id=\"urn:publicid:IDN+wall3.test.ibbt.be+authority+cm\" component_name=\"switch0-0\" component_id=\"urn:publicid:IDN+wall3.test.ibbt.be+node+switch0-0\" exclusive=\"true\">\n" +
                "    <hardware_type name=\"SwitchPort\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <sliver_type name=\"raw-pc\">\n" +
                "      <disk_image name=\"urn:publicid:IDN+wall3.test.ibbt.be+image+emulab-ops:SWITCHPORT\" os=\"Linux\" version=\"6\" description=\"Image to swap in a switchport\" default=\"true\"/>\n" +
                "      <disk_image name=\"urn:publicid:IDN+wall3.test.ibbt.be+image+emulab-ops:SWITCHPORT\" os=\"Linux\" version=\"6\" description=\"Image to swap in a switchport\" default=\"true\"/>\n" +
                "    </sliver_type>\n" +
                "    <hardware_type name=\"pc\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"SwitchPort-vm\">\n" +
                "      <emulab:node_type type_slots=\"20\"/>\n" +
                "    </hardware_type>\n" +
                "    <sliver_type name=\"emulab-openvz\"/>\n" +
                "    <hardware_type name=\"pcvm\">\n" +
                "      <emulab:node_type type_slots=\"20\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"lan\">\n" +
                "      <emulab:node_type type_slots=\"unlimited\" static=\"true\"/>\n" +
                "    </hardware_type>\n" +
                "    <available now=\"true\"/>\n" +
                "    <location country=\"BE\" longitude=\"3.734761\" latitude=\"51.036145\"/>\n" +
                "    <emulab:trivial_bandwidth value=\"400000\"/>\n" +
                "    <emulab:fd name=\"virtpercent\" weight=\"100\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"cpu\" weight=\"2000\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"ram\" weight=\"512\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"cpupercent\" weight=\"92\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"rampercent\" weight=\"80\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"switchport\" weight=\"1\" violatable=\"true\"/>\n" +
                "    <emulab:fd name=\"OS-10125\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10125\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"connected-to-c300b\" weight=\"0.0\"/>\n" +
                "  </node>\n" +
                "  <node component_manager_id=\"urn:publicid:IDN+wall3.test.ibbt.be+authority+cm\" component_name=\"n095-16b\" component_id=\"urn:publicid:IDN+wall3.test.ibbt.be+node+n095-16b\" exclusive=\"true\">\n" +
                "    <hardware_type name=\"pcgen03-3p\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <sliver_type name=\"raw-pc\">\n" +
                "      <disk_image name=\"urn:publicid:IDN+wall3.test.ibbt.be+image+emulab-ops:DEB60_64-STD\" os=\"Linux\" version=\"6\" description=\"Debian 6.0 Squeeze 64-bit\" default=\"true\"/>\n" +
                "      <disk_image name=\"urn:publicid:IDN+wall3.test.ibbt.be+image+emulab-ops:DEB60_64-STD\" os=\"Linux\" version=\"6\" description=\"Debian 6.0 Squeeze 64-bit\" default=\"true\"/>\n" +
                "    </sliver_type>\n" +
                "    <hardware_type name=\"pc\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"delay\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"delay-pcgen03-3p\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"pcgen03-3p-vm\">\n" +
                "      <emulab:node_type type_slots=\"20\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"pcvmgen03-3p\">\n" +
                "      <emulab:node_type type_slots=\"20\"/>\n" +
                "    </hardware_type>\n" +
                "    <sliver_type name=\"emulab-openvz\"/>\n" +
                "    <hardware_type name=\"pcvm\">\n" +
                "      <emulab:node_type type_slots=\"20\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"lan\">\n" +
                "      <emulab:node_type type_slots=\"unlimited\" static=\"true\"/>\n" +
                "    </hardware_type>\n" +
                "    <available now=\"true\"/>\n" +
                "    <location country=\"BE\" longitude=\"3.734761\" latitude=\"51.036145\"/>\n" +
                "    <emulab:trivial_bandwidth value=\"400000\"/>\n" +
                "    <emulab:fd name=\"virtpercent\" weight=\"100\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"cpu\" weight=\"2400\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"ram\" weight=\"24053\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"cpupercent\" weight=\"92\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"rampercent\" weight=\"80\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"3ports\" weight=\"0.4\"/>\n" +
                "    <emulab:fd name=\"OS-10100\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10095\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10066\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10097\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10018\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10069\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10108\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10127\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10018\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10077\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10098\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10055\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10121\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"connected-to-c300b\" weight=\"0.0\"/>\n" +
                "    <emulab:fd name=\"poweroff\" weight=\"0.9\"/>\n" +
                "  </node>\n" +
                "  <node component_manager_id=\"urn:publicid:IDN+wall3.test.ibbt.be+authority+cm\" component_name=\"n095-16a\" component_id=\"urn:publicid:IDN+wall3.test.ibbt.be+node+n095-16a\" exclusive=\"true\">\n" +
                "    <hardware_type name=\"pcgen03-3p\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <sliver_type name=\"raw-pc\">\n" +
                "      <disk_image name=\"urn:publicid:IDN+wall3.test.ibbt.be+image+emulab-ops:DEB60_64-STD\" os=\"Linux\" version=\"6\" description=\"Debian 6.0 Squeeze 64-bit\" default=\"true\"/>\n" +
                "      <disk_image name=\"urn:publicid:IDN+wall3.test.ibbt.be+image+emulab-ops:DEB60_64-STD\" os=\"Linux\" version=\"6\" description=\"Debian 6.0 Squeeze 64-bit\" default=\"true\"/>\n" +
                "    </sliver_type>\n" +
                "    <hardware_type name=\"pc\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"delay\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"delay-pcgen03-3p\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"pcgen03-3p-vm\">\n" +
                "      <emulab:node_type type_slots=\"20\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"pcvmgen03-3p\">\n" +
                "      <emulab:node_type type_slots=\"20\"/>\n" +
                "    </hardware_type>\n" +
                "    <sliver_type name=\"emulab-openvz\"/>\n" +
                "    <hardware_type name=\"pcvm\">\n" +
                "      <emulab:node_type type_slots=\"20\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"lan\">\n" +
                "      <emulab:node_type type_slots=\"unlimited\" static=\"true\"/>\n" +
                "    </hardware_type>\n" +
                "    <available now=\"true\"/>\n" +
                "    <location country=\"BE\" longitude=\"3.734761\" latitude=\"51.036145\"/>\n" +
                "    <emulab:trivial_bandwidth value=\"400000\"/>\n" +
                "    <emulab:fd name=\"virtpercent\" weight=\"100\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"cpu\" weight=\"2400\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"ram\" weight=\"24053\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"cpupercent\" weight=\"92\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"rampercent\" weight=\"80\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"3ports\" weight=\"0.4\"/>\n" +
                "    <emulab:fd name=\"OS-10100\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10095\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10066\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10097\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10018\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10069\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10108\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10127\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10018\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10077\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10098\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10055\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10121\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"connected-to-c300b\" weight=\"0.0\"/>\n" +
                "    <emulab:fd name=\"poweroff\" weight=\"0.9\"/>\n" +
                "  </node>\n" +
                "  <node component_manager_id=\"urn:publicid:IDN+wall3.test.ibbt.be+authority+cm\" component_name=\"n095-17b\" component_id=\"urn:publicid:IDN+wall3.test.ibbt.be+node+n095-17b\" exclusive=\"true\">\n" +
                "    <hardware_type name=\"pcgen03-3p\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <sliver_type name=\"raw-pc\">\n" +
                "      <disk_image name=\"urn:publicid:IDN+wall3.test.ibbt.be+image+emulab-ops:DEB60_64-STD\" os=\"Linux\" version=\"6\" description=\"Debian 6.0 Squeeze 64-bit\" default=\"true\"/>\n" +
                "      <disk_image name=\"urn:publicid:IDN+wall3.test.ibbt.be+image+emulab-ops:DEB60_64-STD\" os=\"Linux\" version=\"6\" description=\"Debian 6.0 Squeeze 64-bit\" default=\"true\"/>\n" +
                "    </sliver_type>\n" +
                "    <hardware_type name=\"pc\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"delay\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"delay-pcgen03-3p\">\n" +
                "      <emulab:node_type type_slots=\"1\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"pcgen03-3p-vm\">\n" +
                "      <emulab:node_type type_slots=\"20\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"pcvmgen03-3p\">\n" +
                "      <emulab:node_type type_slots=\"20\"/>\n" +
                "    </hardware_type>\n" +
                "    <sliver_type name=\"emulab-openvz\"/>\n" +
                "    <hardware_type name=\"pcvm\">\n" +
                "      <emulab:node_type type_slots=\"20\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"lan\">\n" +
                "      <emulab:node_type type_slots=\"unlimited\" static=\"true\"/>\n" +
                "    </hardware_type>\n" +
                "    <available now=\"true\"/>\n" +
                "    <location country=\"BE\" longitude=\"3.734761\" latitude=\"51.036145\"/>\n" +
                "    <emulab:trivial_bandwidth value=\"400000\"/>\n" +
                "    <emulab:fd name=\"virtpercent\" weight=\"100\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"cpu\" weight=\"2400\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"ram\" weight=\"24053\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"cpupercent\" weight=\"92\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"rampercent\" weight=\"80\" violatable=\"true\" local_operator=\"+\"/>\n" +
                "    <emulab:fd name=\"3ports\" weight=\"0.4\"/>\n" +
                "    <emulab:fd name=\"OS-10100\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10095\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10066\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10097\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10018\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10069\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10108\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10127\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10018\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10077\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10098\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10055\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"OS-10121\" weight=\"0\"/>\n" +
                "    <emulab:fd name=\"connected-to-c300b\" weight=\"0.0\"/>\n" +
                "    <emulab:fd name=\"poweroff\" weight=\"0.9\"/>\n" +
                "  </node>\n" +
                "  <node component_manager_id=\"urn:publicid:IDN+wall3.test.ibbt.be+authority+cm\" component_name=\"internet\" component_id=\"urn:publicid:IDN+wall3.test.ibbt.be+node+internet\" exclusive=\"true\">\n" +
                "    <hardware_type name=\"ipv4\">\n" +
                "      <emulab:node_type type_slots=\"unlimited\" static=\"true\"/>\n" +
                "    </hardware_type>\n" +
                "    <available now=\"true\"/>\n" +
                "    <cloud/>\n" +
                "    <location country=\"BE\" longitude=\"3.734761\" latitude=\"51.036145\"/>\n" +
                "  </node>\n" +
                "  <node component_manager_id=\"urn:publicid:IDN+wall3.test.ibbt.be+authority+cm\" component_name=\"airswitch\" component_id=\"urn:publicid:IDN+wall3.test.ibbt.be+node+airswitch\" exclusive=\"true\">\n" +
                "    <hardware_type name=\"80211\">\n" +
                "      <emulab:node_type type_slots=\"unlimited\" static=\"true\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"80211a\">\n" +
                "      <emulab:node_type type_slots=\"unlimited\" static=\"true\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"80211b\">\n" +
                "      <emulab:node_type type_slots=\"unlimited\" static=\"true\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"80211g\">\n" +
                "      <emulab:node_type type_slots=\"unlimited\" static=\"true\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"flex900\">\n" +
                "      <emulab:node_type type_slots=\"unlimited\" static=\"true\"/>\n" +
                "    </hardware_type>\n" +
                "    <hardware_type name=\"xcvr2450\">\n" +
                "      <emulab:node_type type_slots=\"unlimited\" static=\"true\"/>\n" +
                "    </hardware_type>\n" +
                "    <available now=\"true\"/>\n" +
                "    <cloud/>\n" +
                "    <location country=\"BE\" longitude=\"3.734761\" latitude=\"51.036145\"/>\n" +
                "  </node>\n" +
                "</rspec>\n";

        //try parsing here already, that's easier for debugging
        Rspec rSpec = Rspec.fromGeni3AdvertisementRspecXML(advertisementRspec);

        auth.setAdvertisementRspec(false, advertisementRspec);
    }
}
