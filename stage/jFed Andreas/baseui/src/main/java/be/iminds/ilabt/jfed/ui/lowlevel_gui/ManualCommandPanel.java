package be.iminds.ilabt.jfed.ui.lowlevel_gui;

import be.iminds.ilabt.jfed.history.CredentialAndUrnHistory;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.api.*;
import be.iminds.ilabt.jfed.lowlevel.authority.*;
import be.iminds.ilabt.jfed.ui.log_gui.ResultPanel;
import be.iminds.ilabt.jfed.ui.userlogin_gui.UserLoginPanel;
import thinlet.FrameLauncher;
import thinlet.Thinlet;

import java.awt.event.WindowEvent;

/**
 * RawMainPanel
 */
public class ManualCommandPanel extends Thinlet implements UserLoginPanel.ConfigPanelCloseHandler, AuthorityProvider {
    public interface ManualCommandPanelCloseHandler {
        public void closeManualCommandPanel();
    }
    private ManualCommandPanelCloseHandler closeHandler;
    public void setRawMainPanelCloseHandler(ManualCommandPanelCloseHandler closeHandler) {
        this.closeHandler = closeHandler;
    }

    private CredentialAndUrnHistory history;
    private UserLoginPanel allConfigPanel;
    private GeniUserProvider contextProvider;
    private Logger logger;
    private AuthorityListModel authorityListModel;
//    private AuthorityChoicePanel authorityChoicePanel; //AuthorityChoicePanel is now on ApiPanel

    public static void main(String[] main_args) throws Exception {
        Logger logger = new Logger();
//        AuthorityListModel authorityListModel = new AuthorityListModel();
//        BuiltinAuthorityList.load(authorityListModel);
        AuthorityListModel authorityListModel = JFedAuthorityList.getAuthorityListModel();
        UserLoginPanel allConfigPanel = new UserLoginPanel(authorityListModel, logger);
        CredentialAndUrnHistory history = new CredentialAndUrnHistory(logger);

        final ManualCommandPanel mainPan = new ManualCommandPanel(authorityListModel, logger, history, allConfigPanel, allConfigPanel);
        mainPan.setRawMainPanelCloseHandler(new ManualCommandPanelCloseHandler() {
            public void closeManualCommandPanel() {
                System.exit(0);
            }
        });

        allConfigPanel.addCloseHandler(new UserLoginPanel.ConfigPanelCloseHandler() {
            public void closeConfig() {
                mainPan.showManualCommandPan();
            }
        });
    }

    private FrameLauncher mainPanFl = null;
    public void showManualCommandPan() {
        if (mainPanFl == null)
            mainPanFl = new FrameLauncher("jFed Probe", this, 800, 600) {
                public void	windowClosed(WindowEvent e) { /*super.windowClosed(e);*/ }
                public void	windowClosing(WindowEvent e) { /*super.windowClosing(e);*/ mainPanFl.setVisible(false);
                    if (closeHandler != null)
                        closeHandler.closeManualCommandPanel();
                }
            };
        else
            mainPanFl.setVisible(true);
    }

    public void runConfig() {
        if (allConfigPanel == null)
            throw new RuntimeException("BUG: Manual command panel does not have config.");
        allConfigPanel.showConfigFrame();
    }

    public void closeConfig() {
        showManualCommandPan();
    }

    /**
     *
     * @param allConfigPanel may be null, in that case, the mainpanel does not offer an option to confure the config panel
     * */
    public ManualCommandPanel(AuthorityListModel authorityListModel, Logger logger, CredentialAndUrnHistory history,
                              UserLoginPanel allConfigPanel, UserLoginPanel contextProvider) throws Exception {
        this.allConfigPanel = allConfigPanel;
        this.history = history;
        this.contextProvider = contextProvider;
        this.logger = logger;
        this.authorityListModel = authorityListModel;
//        this.authorityChoicePanel = new AuthorityChoicePanel(this, this.authorityListModel);

        Object mainPane = parse("ManualCommandPanel.xml");
        if (mainPane == null) throw new RuntimeException("mainPane == null");
        add(mainPane);

        Object tabbedPane = find(mainPane, "serverPane");
        if (tabbedPane == null) throw new RuntimeException("serverPane not found");

        GeniConnectionProvider connectionProvider = new GeniConnectionPool();

        SliceAuthority sa = new SliceAuthority(this.logger);
        ApiPanel<SliceAuthority> apiSa = new ApiPanel<SliceAuthority>(tabbedPane, this, history, sa, connectionProvider, contextProvider, authorityListModel, logger);
        PlanetlabSfaRegistryInterface plsfr = new PlanetlabSfaRegistryInterface(this.logger);
        ApiPanel<PlanetlabSfaRegistryInterface> apiPlsfr = new ApiPanel<PlanetlabSfaRegistryInterface>(tabbedPane, this, history, plsfr, connectionProvider, contextProvider, authorityListModel, logger);

        AggregateManager2 am2 = new AggregateManager2(this.logger);
        ApiPanel<AggregateManager2> apiAm2 = new ApiPanel<AggregateManager2>(tabbedPane, this, history, am2, connectionProvider, contextProvider, authorityListModel, logger);
        AggregateManager3 am3 = new AggregateManager3(this.logger);
        ApiPanel<AggregateManager3> apiAm3 = new ApiPanel<AggregateManager3>(tabbedPane, this, history, am3, connectionProvider, contextProvider, authorityListModel, logger);
        ProtoGeniClearingHouse1 ch = new ProtoGeniClearingHouse1(this.logger);
        ApiPanel<ProtoGeniClearingHouse1> apiCh = new ApiPanel<ProtoGeniClearingHouse1>(tabbedPane, this, history, ch, connectionProvider, contextProvider, null, logger);


        Object configButton = find(mainPane, "configButton");
        Object menuBar = find(mainPane, "menuBar");
        if (configButton == null) throw new RuntimeException("configButton not found");
        if (menuBar == null) throw new RuntimeException("menuBar not found");
        if (allConfigPanel == null) {
            remove(configButton);
            setBoolean(menuBar, "visible", false);
        }

//        add(tabbedPane, apiSa.getTab());
//        add(tabbedPane, apiAm2.getTab());
//        add(tabbedPane, apiAm3.getTab());
//        add(tabbedPane, apiCh.getTab());

        ResultPanel resPan = new ResultPanel(null, this, this.logger, history);

        add(mainPane, resPan.getPanel());

        if (allConfigPanel != null) {
            this.allConfigPanel.addCloseHandler(this);
            runConfig();
        }
    }

    /**
     * provides the User's authority
    **/
    @Override
    public SfaAuthority getAuthority() {
        return contextProvider.getLoggedInGeniUser().getUserAuthority();
    }

//    public void initAuthChoicePanel(Object thinletAuthChoicePanel) {
//        add(thinletAuthChoicePanel, authorityChoicePanel.getThinletPanel());
//    }
}
