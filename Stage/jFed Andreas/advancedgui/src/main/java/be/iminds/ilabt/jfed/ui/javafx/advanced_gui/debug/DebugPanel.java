package be.iminds.ilabt.jfed.ui.javafx.advanced_gui.debug;

import be.iminds.ilabt.jfed.highlevel.api.EasyAggregateManager2;
import be.iminds.ilabt.jfed.highlevel.api.EasySliceAuthority;
import be.iminds.ilabt.jfed.highlevel.controller.HighLevelController;
import be.iminds.ilabt.jfed.highlevel.controller.TaskThread;
import be.iminds.ilabt.jfed.highlevel.model.*;
import be.iminds.ilabt.jfed.highlevel.stitcher.ParallelStitcher;
import be.iminds.ilabt.jfed.history.UserInfo;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.AbstractGeniAggregateManager;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager2;
import be.iminds.ilabt.jfed.lowlevel.api.SliceAuthority;
import be.iminds.ilabt.jfed.lowlevel.api.StitchingComputationService;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityListModel;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityProvider;
import be.iminds.ilabt.jfed.lowlevel.authority.DebuggingAuthorityList;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.stitching.StitchingDirector;
import be.iminds.ilabt.jfed.lowlevel.stitching.SynchronousStitcher;
import be.iminds.ilabt.jfed.ui.javafx.call_gui.TasksPanel;
import be.iminds.ilabt.jfed.ui.javafx.probe_gui.ProbeController;
import be.iminds.ilabt.jfed.ui.javafx.stitching_gui.CreateSliverStatusPanel;
import be.iminds.ilabt.jfed.ui.javafx.util.JavaFXDialogUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * DebugPanel
 */
public class DebugPanel implements Initializable  {
    private EasyModel easyModel;
    private EasyModelDebugFaker faker;

    private @FXML TextField sliceNameField;

    public void setEasyModel(EasyModel easyModel) {
        this.easyModel = easyModel;
        this.faker = new EasyModelDebugFaker(easyModel);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

//    @FXML private void showManualCommandPanel() {
//        try {
//            //also show linked old manual command panel
//            CredentialAndUrnHistory history = new CredentialAndUrnHistory(easyModel.getLogger());
//            AuthorityListModel authorityListModel = easyModel.getAuthorityList().getAuthorityListModel();
//            UserLoginPanel userLoginPanel = new UserLoginPanel(authorityListModel, easyModel.getLogger());
//            final ManualCommandPanel mainPan = new ManualCommandPanel(authorityListModel, easyModel.getLogger(), history, userLoginPanel, userLoginPanel);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    @FXML private void probe() {
        ProbeController.showProbe(easyModel);
    }

    @FXML private void calls() {
        TasksPanel.showTasks(easyModel);
    }

    @FXML private void showStichingPanel() {
        if (stitchSliceUrn == null) {
            System.err.println("No stitchSliceUrn");
            return;
        }
       Slice stitchSlice = easyModel.getSlice(stitchSliceUrn);
        if (stitchSlice == null) {
            System.err.println("No slice found: " + stitchSliceUrn);
            return;
        }
        if (stitchSlice.getCredential() == null) {
            System.err.println("No slice credential for: " + stitchSliceUrn);
            return;
        }


        ParallelStitcher parallelStitcher = new ParallelStitcher(easyModel, stitchRspec);

        CreateSliverStatusPanel.showStitchingOverview(easyModel, parallelStitcher);

        parallelStitcher.setSlice(stitchSlice);

        parallelStitcher.start();
    }

    //quick and dirty for testing
    public static String getHTML(String urlToRead) {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        String result = "";
        try {
            url = new URL(urlToRead);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

//    @FXML private void browse() {
//        final WebView browser = new WebView();
//        final WebEngine webEngine = browser.getEngine();
//
//        BorderPane root = new BorderPane();
//        root.setPadding(new Insets(8, 8, 8, 8));
//        root.setCenter(browser);
//
//        Scene scene = new Scene(root);
//
//        Stage stage = new Stage();
//        stage.setTitle("HTML Browser");
//        stage.setWidth(500);
//        stage.setHeight(500);
//        stage.setScene(scene);
//        stage.show();
//
////        String html = getHTML("http://jfed.iminds.be/");
////        webEngine.loadContent(html);
//        webEngine.load("http://jfed.iminds.be/");
//    }

    @FXML private void addDebugAuth() {
        faker.addDebugAuth();
    }


    @FXML private void initFake() {
        faker.addFakeInfo();
    }

    @FXML private void addCM() {
        faker.addCm();
    }

    @FXML private void fakeAdvertisement() {
        faker.fakeAdvertisement();
    }

    @FXML private void fakeRequest() {
        faker.addSliceRequest();
    }
    @FXML private void fakeManifest() {
        faker.addSliceManifest();
    }
    @FXML private void fakeRefreshStatus() {
        faker.refreshStatus();
    }
    @FXML private void fakeRefreshManifest() {
        faker.refreshManifest();
    }


    public void createStitchSlice() {
        assert easyModel.getGeniUserProvider().isUserLoggedIn();
        GeniUser user = easyModel.getGeniUserProvider().getLoggedInGeniUser();

        if (stitchSliceName == null) {
            //create name and urn
            int randomNr;

            if (JavaFXDialogUtil.show2ChoiceDialog("Slice Name Nr?", "1", "random", sliceNameField)) {
                //1
                randomNr = 1;
            } else {
                //random
                randomNr = (int) Math.round(Math.random()*1000);
            }

            stitchSliceName = "stitchtest"+randomNr;
            stitchSliceUrn = "urn:publicid:IDN+"+user.getUserAuthority().getNameForUrn()+"+slice+"+stitchSliceName;

            //create slice
            SliceAuthority sa = new SliceAuthority(easyModel.getLogger(), true);

            TaskThread.Task task;
            if (JavaFXDialogUtil.show2ChoiceDialog("Register or GetCredential", "Register", "GetCredential", sliceNameField)) {
                task = easyModel.getHighLevelController().createSlice(stitchSliceName);
            } else {
                Slice stitchSlice = easyModel.logExistSliceUrn(user.getUserAuthority(), stitchSliceUrn);
                task = easyModel.getHighLevelController().getSliceCredential(stitchSlice);
            }

            TaskThread.getInstance().addTask(task);

            System.out.println("Creating slice "+stitchSliceUrn+"");
        } else
            System.out.println("Slice \"" + stitchSliceName + "\" already exists.");
    }

    private void fillInSliceDetails() {
        if (stitchCredentialList != null)
            return;

        if (stitchSliceName == null) {
            System.out.println("Slice name not created yet");
            return;
        }

        if (easyModel.getSlice(stitchSliceUrn) == null) {
            System.out.println("No slice "+stitchSliceUrn+" in EasyModel.\n   Known slices: ");
            for (Slice slice : easyModel.getSlices())
                System.out.println(""+slice.getUrn()+" -> "+slice.getName());
            return;
        }

        Slice slice = easyModel.getSlice(stitchSliceUrn);
        stitchCredentialList = new ArrayList<>();
        assert slice.getCredential() != null;
        stitchCredentialList.add(slice.getCredential());
    }

    String stitchRspec = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rspec type=\"request\" \n" +
            "    xmlns=\"http://www.geni.net/resources/rspec/3\" \n" +
            "    xmlns:planetlab=\"http://www.planet-lab.org/resources/sfa/ext/planetlab/1\" \n" +
            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
            "       xmlns:stitch=\"http://hpn.east.isi.edu/rspec/ext/stitch/0.1/\" \n" +
            "    xsi:schemaLocation=\"http://www.geni.net/resources/rspec/3 \n" +
            "    http://www.geni.net/resources/rspec/3/request.xsd\">  \n" +
            "  <node client_id=\"server-gpo\" component_manager_id=\"urn:publicid:IDN+instageni.gpolab.bbn.com+authority+cm\" exclusive=\"false\">\n" +
            "    <sliver_type name=\"emulab-openvz\"/>\n" +
            "    <services>\n" +
            "      <execute command=\"sudo /local/install-script.sh\" shell=\"sh\"/>\n" +
            "      <install install_path=\"/local\" url=\"http://www.gpolab.bbn.com/~lnevers/StitchDemo.tar.gz\"/>\n" +
            "   </services>\n" +
            "    <emulab:routable_control_ip xmlns:emulab=\"http://www.protogeni.net/resources/rspec/ext/emulab/1\"/>\n" +
            "    <interface client_id=\"server-gpo:if0\">\n" +
            "      <ip address=\"192.168.4.1\" netmask=\"255.255.255.0\" type=\"ipv4\"/>\n" +
            "    </interface>\n" +
            "  </node>\n" +
            "  <node client_id=\"client-utah\" component_manager_id=\"urn:publicid:IDN+utah.geniracks.net+authority+cm\" exclusive=\"false\">\n" +
            "    <sliver_type name=\"emulab-openvz\"/>\n" +
            "   <services>\n" +
            "      <execute command=\"sudo /local/install-script.sh\" shell=\"sh\"/>\n" +
            "      <install install_path=\"/local\" url=\"http://www.gpolab.bbn.com/~lnevers/StitchDemo.tar.gz\"/>\n" +
            "    </services>\n" +
            "    <interface client_id=\"client-utah:if0\">\n" +
            "      <ip address=\"192.168.4.2\" netmask=\"255.255.255.0\" type=\"ipv4\"/>\n" +
            "    </interface>\n" +
            "  </node>\n" +
            "  <link client_id=\"link\">\n" +
            "    <component_manager name=\"urn:publicid:IDN+instageni.gpolab.bbn.com+authority+cm\"/>                                                                                                        \n" +
            "    <component_manager name=\"urn:publicid:IDN+utah.geniracks.net+authority+cm\"/>                                                                                                              \n" +
            "    <interface_ref client_id=\"server-gpo:if0\"/>                                                                                                                                               \n" +
            "    <interface_ref client_id=\"client-utah:if0\"/>                                                                                                                                              \n" +
            "    <property source_id=\"server-gpo:if0\" dest_id=\"client-utah:if0\" capacity=\"1000\"/>                                                                                                          \n" +
            "    <property source_id=\"client-utah:if0\" dest_id=\"server-gpo:if0\" capacity=\"1000\"/>                                                                                                          \n" +
            "  </link>                                                                                                                                                                                     \n" +
            "</rspec>";
    private String stitchSliceName = null;
    @FXML private StitchingComputationService.SCSReply<StitchingComputationService.ComputePathResult> computePath() {
        fillInSliceDetails();
        assert stitchSliceName != null;
        assert stitchSliceUrn != null;

        try {
            HighLevelController cont = easyModel.getHighLevelController();

            //create auth if doesn't exist
            String sticherUrl = "http://oingo.dragon.maxgigapop.net:8081/geni/xmlrpc";
            String maxScsUrn = "urn:publicid:IDN+oingo.dragon.maxgigapop.net+auth+am";
            SfaAuthority stitcherAuth = null;
            if (easyModel.getAuthorityList().get(maxScsUrn) == null) {
                Map< ServerType, URL> urlMap = new HashMap< ServerType, URL>();
                urlMap.put(new ServerType(ServerType.GeniServerRole.SCS, 1), new URL(sticherUrl));
                stitcherAuth = new SfaAuthority(maxScsUrn, "Max SCS", urlMap, null, "scs");
                AuthorityList authorityList = easyModel.getAuthorityList();
                AuthorityListModel authorityListModel = authorityList.getAuthorityListModel();
                authorityListModel.addAuthority(stitcherAuth);
                authorityListModel.fireChange();

                assert easyModel.getAuthorityList().get(maxScsUrn) != null;
            } else
                stitcherAuth = easyModel.getAuthorityList().get(maxScsUrn).getGeniAuthority();


            //contact stitcher
            StitchingComputationService scs = new StitchingComputationService(easyModel.getLogger());
            GeniConnection con = new GeniPlainConnection(stitcherAuth, sticherUrl, false/*debug*/);
            //scs.getVersion(con);
            return scs.computePath(con, stitchSliceUrn, stitchRspec, null);
        } catch (Throwable e) {
            System.err.println("Exception in Stitching POC: "+e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private List<GeniCredential> stitchCredentialList;
    private String stitchSliceUrn;
    private StitchingDirector stitchingDirector;
    private SynchronousStitcher synchronousStitcher;
    @FXML private void createStitchingPOC() {
        fillInSliceDetails();
        assert easyModel.getGeniUserProvider().isUserLoggedIn();

        if (stitchingDirector != null || synchronousStitcher != null) {
            System.out.println("Delete previous stitchingWorkflow first!");
            return;
        }

        try {
            StitchingComputationService.SCSReply<StitchingComputationService.ComputePathResult> scsReply = computePath();

            assert stitchSliceUrn != null;
            assert stitchCredentialList != null;

            assert scsReply != null;
            assert scsReply.getCode() == 0;
            assert scsReply.getValue() != null;

            HighLevelController cont = easyModel.getHighLevelController();

            //create auth if doesn't exist
            String sticherUrl = "http://oingo.dragon.maxgigapop.net:8081/geni/xmlrpc";
            String maxScsUrn = "urn:publicid:IDN+oingo.dragon.maxgigapop.net+auth+am";
            SfaAuthority stitcherAuth = easyModel.getAuthorityList().get(maxScsUrn).getGeniAuthority();

            stitchingDirector = new StitchingDirector(easyModel.getAuthorityList().getAuthorityListModel());
            stitchingDirector.setComputePathResult(scsReply.getValue());

            synchronousStitcher = new SynchronousStitcher(easyModel.getLogger(), stitchingDirector);
            synchronousStitcher.setInsecure(); //disable failure on unknown certificates. Obviously very INSECURE

            synchronousStitcher.stitchCreate(
                    stitchCredentialList,
                    stitchSliceUrn,
                    easyModel.getGeniUserProvider().getLoggedInGeniUser());

        } catch (Throwable e) {
            System.err.println("Exception in Stitching POC: "+e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML private void deleteStitchingPOC() {
        fillInSliceDetails();
        assert easyModel.getGeniUserProvider().isUserLoggedIn();
        if (stitchingDirector != null && synchronousStitcher != null) {
            synchronousStitcher.deleteAll(stitchCredentialList, stitchSliceUrn, easyModel.getGeniUserProvider().getLoggedInGeniUser());
            stitchingDirector = null;
            synchronousStitcher = null;
            stitchSliceName = null;
            stitchSliceUrn = null;
        }
    }




    @FXML private void getCredential() {
        final GeniUser user = easyModel.getGeniUserProvider().getLoggedInGeniUser();

        EasySliceAuthority easySliceAuthority = new EasySliceAuthority(easyModel.getLogger(), easyModel,
                new AuthorityProvider() { @Override public SfaAuthority getAuthority() { return user.getUserAuthority(); } });

        try {
            easySliceAuthority.getCredential();
        } catch (GeniException e) {
            //TODO handle
            e.printStackTrace();
        }
    }
    @FXML private void resolveUser() {
        final GeniUser user = easyModel.getGeniUserProvider().getLoggedInGeniUser();

        EasySliceAuthority easySliceAuthority = new EasySliceAuthority(easyModel.getLogger(), easyModel,
                new AuthorityProvider() { @Override public SfaAuthority getAuthority() { return user.getUserAuthority(); } });

        try {
            UserInfo userInfo = easySliceAuthority.resolveUser(user.getUserUrn());

            if (userInfo.getSlices().size() > 0) {
                sliceNameField.setText(userInfo.getSlices().get(0));
            }
        } catch (GeniException e) {
            //TODO handle
            e.printStackTrace();
        }
    }
    @FXML private void resolveSlice() {
        final GeniUser user = easyModel.getGeniUserProvider().getLoggedInGeniUser();

        EasySliceAuthority easySliceAuthority = new EasySliceAuthority(easyModel.getLogger(), easyModel,
                new AuthorityProvider() { @Override public SfaAuthority getAuthority() { return user.getUserAuthority(); } });

        try {
            easySliceAuthority.resolveSlice(sliceNameField.getText());
        } catch (GeniException e) {
            //TODO handle
            e.printStackTrace();
        }
    }
    @FXML private void getCredentialSlice() {
        final GeniUser user = easyModel.getGeniUserProvider().getLoggedInGeniUser();

        EasySliceAuthority easySliceAuthority = new EasySliceAuthority(easyModel.getLogger(), easyModel,
                new AuthorityProvider() { @Override public SfaAuthority getAuthority() { return user.getUserAuthority(); } });

        try {
            easySliceAuthority.getCredential(easyModel.getSlice(sliceNameField.getText()));
        } catch (GeniException e) {
            //TODO handle
            e.printStackTrace();
        }
    }
    @FXML private void listSliceResources() {
        final GeniUser user = easyModel.getGeniUserProvider().getLoggedInGeniUser();

        EasyAggregateManager2 easyAggregateManager2 = new EasyAggregateManager2(easyModel.getLogger(), easyModel,user.getUserAuthority());

        try {
            easyAggregateManager2.listSliceResources (easyModel.getSlice(sliceNameField.getText()));
        } catch (GeniException e) {
            //TODO handle
            e.printStackTrace();
        }
    }


    private static Stage debugStage = null;
    public static void showDebugPanel(EasyModel easyModel) {
        try {
            URL location = DebugPanel.class.getResource("DebugPanel.fxml");
            assert location != null;
            FXMLLoader fxmlLoader = new FXMLLoader(location, null);

            BorderPane root = (BorderPane)fxmlLoader.load();
            DebugPanel controller = (DebugPanel)fxmlLoader.getController();

            controller.setEasyModel(easyModel);

            Scene scene = new Scene(root);

            if (debugStage == null) {
                debugStage = new Stage();
                debugStage.setScene(scene);
            }
            assert debugStage != null;
            debugStage.setX(0);
            debugStage.setY(0);
            debugStage.show();
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong showing the Debug Panel: "+e.getMessage(), e);
        }
    }


    public void exit() {
        System.exit(0);
    }
}
