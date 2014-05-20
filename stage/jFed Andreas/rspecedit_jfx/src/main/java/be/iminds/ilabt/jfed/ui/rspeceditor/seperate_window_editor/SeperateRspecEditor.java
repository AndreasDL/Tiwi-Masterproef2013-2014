package be.iminds.ilabt.jfed.ui.rspeceditor.seperate_window_editor;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SeperateRspecEditor
{
    private Stage stage;
    private boolean finished = false;


    public void start() {
        stage = new Stage();
        URL location = getClass().getResource("SeperateRspecEditor.fxml");
        assert location != null;
        FXMLLoader fxmlLoader = new FXMLLoader(location, null);

        Parent root = null;
        try {
            root = (Parent)fxmlLoader.load();

            SeperateRspecEditorController seperateRspecEditorController = ( SeperateRspecEditorController) fxmlLoader.getController();
            seperateRspecEditorController.setSeperateRspecEditor(this);
            seperateRspecEditorController.setRspec(inputRspec);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.showAndWait();


            synchronized(this){
                finished = true;
                this.notifyAll();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String inputRspec = null;
    private String resultRspec = null;
    public void setResult(String rspec) {
        this.resultRspec = rspec;
    }

    //init JavaFX if needed
    private static JFXPanel hack = new JFXPanel();

    /** Blocking call, show input rspec, returns rspec as edited by user, */
    public static String showEditor(String inputRspec) {
        final SeperateRspecEditor sre = new SeperateRspecEditor();
        sre.inputRspec = inputRspec;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                sre.start();
            }
        });

        //wait for stage to close
        synchronized(sre){
            while (!sre.isFinished()){
                try {
                    sre.wait();
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }

        return sre.resultRspec;
    }

    public void stop() {
        stage.close();
    }

    public boolean isFinished() {
        return finished;
    }
}
