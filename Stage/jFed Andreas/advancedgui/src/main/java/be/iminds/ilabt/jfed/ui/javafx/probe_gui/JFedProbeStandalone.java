package be.iminds.ilabt.jfed.ui.javafx.probe_gui;

import be.iminds.ilabt.jfed.highlevel.controller.HighLevelController;
import be.iminds.ilabt.jfed.highlevel.model.EasyModel;
import be.iminds.ilabt.jfed.lowlevel.GeniConnectionPool;
import be.iminds.ilabt.jfed.lowlevel.userloginmodel.UserLoginModelManager;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityListModel;
import be.iminds.ilabt.jfed.lowlevel.authority.JFedAuthorityList;
import be.iminds.ilabt.jfed.ui.javafx.advanced_gui.debug.DebugPanel;
import be.iminds.ilabt.jfed.ui.javafx.userlogin.UserLoginController;
import be.iminds.ilabt.jfed.util.JavaFXLogger;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class JFedProbeStandalone extends Application {

    @Override
    public void start(final Stage stage) throws Exception {
        JavaFXLogger logger = new JavaFXLogger(true /*synchronous processing*/);
        AuthorityListModel authorityListModel = JFedAuthorityList.getAuthorityListModel();

        final UserLoginModelManager userLoginModel = new UserLoginModelManager(authorityListModel, logger);

        EasyModel easyModel = new EasyModel(logger, authorityListModel, userLoginModel);
        HighLevelController highLevelController = new HighLevelController(easyModel, new GeniConnectionPool());
        easyModel.setHighLevelController(highLevelController);

        if (debugEnabled)
            DebugPanel.showDebugPanel(easyModel);

        UserLoginController.showUserLogin(logger, easyModel, userLoginModel, false);

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        ProbeController.showProbe(easyModel, stage);
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static boolean debugEnabled = false;
    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("debug") || arg.equalsIgnoreCase("--debug") || arg.equalsIgnoreCase("-debug"))
                debugEnabled = true;
        }
        launch(args);
    }
}
