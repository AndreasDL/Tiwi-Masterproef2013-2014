package be.iminds.ilabt.jfed.ui.javafx.advanced_gui;

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
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.net.URL;

public class JFedAdvancedGUI  extends Application {

    @Override
    public void start(final Stage stage) throws Exception {
        JavaFXLogger logger = new JavaFXLogger(true /*synchronous processing*/);
        AuthorityListModel authorityListModel = JFedAuthorityList.getAuthorityListModel();

        final UserLoginModelManager userLoginModel = new UserLoginModelManager(authorityListModel, logger);
        userLoginModel.load();

        EasyModel easyModel = new EasyModel(logger, authorityListModel, userLoginModel);
        HighLevelController highLevelController = new HighLevelController(easyModel, new GeniConnectionPool());
        easyModel.setHighLevelController(highLevelController);

        if (debugEnabled)
            DebugPanel.showDebugPanel(easyModel);

        UserLoginController.showUserLogin(logger, easyModel, userLoginModel, true/*requireAllUserInfo*/);
        //TODO: set logged in user as JavaFX ObjectProperty

        URL location = getClass().getResource("JFedAdvancedGui.fxml");
        assert location != null;
        FXMLLoader fxmlLoader = new FXMLLoader(location, null);

        BorderPane root = (BorderPane)fxmlLoader.load();
        JFedAdvancedGuiController controller = (JFedAdvancedGuiController)fxmlLoader.getController();

        controller.setUserLoginModel(userLoginModel);
        controller.setLogger(logger);
        controller.setEasyModel(easyModel);

        Scene scene = new Scene(root);

        //Get default screen of multi screen setup
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();

        stage.setScene(scene);

        if (width <= 1024 || height <= 768) {
            //fullscreen is NOT maximize
//            stage.setFullScreen(true);

            //this is also not maximize, but it is a lot closer
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();

            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
        }
        else
            stage.sizeToScene();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        stage.show();
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
