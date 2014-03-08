package be.iminds.ilabt.jfed.ui.javafx.call_gui;

import be.iminds.ilabt.jfed.highlevel.controller.JavaFXTaskThread;
import be.iminds.ilabt.jfed.highlevel.controller.TaskThread;
import be.iminds.ilabt.jfed.highlevel.model.EasyModel;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.ui.javafx.log_gui.LogHistoryPanel;
import be.iminds.ilabt.jfed.ui.javafx.util.TimeUtils;
import be.iminds.ilabt.jfed.util.JFedJavaFXBindings;
import be.iminds.ilabt.jfed.util.JavaFXLogger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * TasksPanel
 */
public class TasksPanel extends BorderPane {
    private EasyModel easyModel;

    @FXML private LogHistoryPanel logPanel;

    @FXML private TaskList allTaskList;
    @FXML private TaskList dependingOnTaskList;
    @FXML private TaskList dependersTaskList;

    @FXML private TextField taskNameField;
    @FXML private Parent taskExceptionBox;
    @FXML private TextArea taskExceptionArea;
    @FXML private TextField taskStateField;

    @FXML private Button cancelButton;

    @FXML private HBox startTimeBox;
    @FXML private TextField startTimeField;
    @FXML private Label relStartTimeLabel;
    @FXML private HBox stopTimeBox;
    @FXML private TextField stopTimeField;
    @FXML private Label relStopTimeLabel;
    @FXML private Label canceledLabel;

    private ObjectProperty<JavaFXTaskThread.SingleTask> selectedSingleTask = new SimpleObjectProperty<JavaFXTaskThread.SingleTask>(null);

    public TasksPanel() {
        URL location = getClass().getResource("Calls.fxml");
        assert location != null;
        FXMLLoader fxmlLoader = new FXMLLoader(location);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

            if (easyModel != null)
                doAllInit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        taskExceptionBox.managedProperty().bind(taskExceptionBox.visibleProperty());
    }

    public EasyModel getEasyModel() {
        return easyModel;
    }

    public void setEasyModel(EasyModel easyModel) {
        assert this.easyModel == null;
        this.easyModel = easyModel;

        if (logPanel != null)
            doAllInit();
    }

    /** init after easymodel set, and fxml loaded.*/
    private void doAllInit() {
        assert logPanel != null;
        assert easyModel != null;


        startTimeBox.managedProperty().bind(startTimeBox.visibleProperty());
        stopTimeBox.managedProperty().bind(stopTimeBox.visibleProperty());
        canceledLabel.managedProperty().bind(canceledLabel.visibleProperty());

        ObservableList<JavaFXTaskThread.SingleTask> allTasks = JavaFXTaskThread.getInstance().getAllTasks();
        allTaskList.setItems(allTasks);

        final ChangeListener<Date> showTimeListener = new ChangeListener<Date>() {
            @Override
            public void changed(ObservableValue<? extends Date> observableValue, Date date, Date date1) {
                showTime();
            }
        };

        allTaskList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<JavaFXTaskThread.SingleTask>() {
            @Override
            public void changed(ObservableValue<? extends JavaFXTaskThread.SingleTask> observableValue,
                                JavaFXTaskThread.SingleTask oldSingleTask,
                                final JavaFXTaskThread.SingleTask newSingleTask) {
                if (oldSingleTask != null) {
                    taskStateField.textProperty().unbind();
                    oldSingleTask.runStartDateProperty().removeListener(showTimeListener);
                    oldSingleTask.runStopDateProperty().removeListener(showTimeListener);
                }

                selectedSingleTask.set(newSingleTask);

                cancelButton.disableProperty().unbind();
                canceledLabel.visibleProperty().unbind();

                if (updateListener != null && oldSingleTask != null) {
                    oldSingleTask.getException().removeListener(updateListener);
                    updateListener = null;
                }

                if (newSingleTask != null) {
                    taskNameField.textProperty().set(newSingleTask.getName());
                    taskStateField.textProperty().bind(Bindings.convert(newSingleTask.stateProperty()));
                    dependingOnTaskList.setItems(newSingleTask.getObservableDependsOn());
                    dependersTaskList.setItems(newSingleTask.getObservableDependingOnThis());
                    logPanel.setApiCallHistory(newSingleTask.getApiCallHistory());
                    cancelButton.disableProperty().bind(newSingleTask.completedProperty());
                    canceledLabel.visibleProperty().bind(newSingleTask.cancelledByUserProperty());

                    updateListener = new ChangeListener<Throwable>() {
                        @Override
                        public void changed(ObservableValue<? extends Throwable> observableValue, Throwable oldThrowable, Throwable newThrowable) {
                            updateException(newSingleTask);
                        }
                    };
                    newSingleTask.getException().addListener(updateListener);
                    updateException(newSingleTask);

                    newSingleTask.runStartDateProperty().addListener(showTimeListener);
                    newSingleTask.runStopDateProperty().addListener(showTimeListener);
                    showTime();
                }
                else {
                    taskExceptionBox.setVisible(false);
                    dependingOnTaskList.setItems(null);
                    dependersTaskList.setItems(null);
                    logPanel.clearApiCallHistory();
                    cancelButton.setDisable(true);
                    canceledLabel.setVisible(false);
                }
            }
        });

        if (!allTasks.isEmpty())
            allTaskList.getSelectionModel().selectFirst();
    }

    private ChangeListener<Throwable> updateListener = null;
    public void updateException(JavaFXTaskThread.SingleTask singleTask) {
        if (singleTask.getException().get() == null) {
            taskExceptionBox.setVisible(false);
            taskExceptionArea.setText("");
        } else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            singleTask.getException().get().printStackTrace(pw);
            pw.close();
            final String stacktrace = sw.getBuffer().toString();

            taskExceptionBox.setVisible(true);
            taskExceptionArea.setText(stacktrace);
        }
    }

    public void printStackTrace() {
        System.err.println("Stacktrace:\n"+taskExceptionArea.getText());
    }

    public void showTime() {
        JavaFXTaskThread.SingleTask task = selectedSingleTask.get();
        if (task != null) {
            Date start = task.runStartDateProperty().get();

            if (start != null) {
                startTimeBox.setVisible(true);
                startTimeField.setText(start+"");
                long millis = System.currentTimeMillis() - start.getTime();
                String relString = TimeUtils.formatMillis(millis, TimeUnit.DAYS, TimeUnit.SECONDS);
                relStartTimeLabel.setText("("+relString+" ago)");
            } else {
                startTimeBox.setVisible(false);
            }

            Date stop = task.runStopDateProperty().get();
            if (stop != null) {
                stopTimeBox.setVisible(true);
                stopTimeField.setText(stop+"");
                String res = "(";
                long millis = System.currentTimeMillis() - stop.getTime();
                res += TimeUtils.formatMillis(millis, TimeUnit.DAYS, TimeUnit.SECONDS)+" ago";
                if (start != null) {
                    long millis2 = stop.getTime() - start.getTime();
                    res += " = "+TimeUtils.formatMillis(millis2, TimeUnit.DAYS, TimeUnit.MILLISECONDS)+" after start";
                }
                res += ")";
                relStopTimeLabel.setText(res);
            } else {
                stopTimeBox.setVisible(false);
            }
        } else {
            startTimeBox.setVisible(false);
            stopTimeBox.setVisible(false);
        }
    }

    public void cancel() {
        JavaFXTaskThread.SingleTask task = selectedSingleTask.get();
        if (task != null) {
            JavaFXTaskThread.getInstance().cancel(task);
        }
    }

    private static Stage callsStage = null;
    public static void showTasks(EasyModel easyModel) {
        try {
            if (callsStage == null) {
                TasksPanel callsPanel = new TasksPanel();
                callsPanel.setEasyModel(easyModel);

                Scene scene = new Scene(callsPanel);

                callsStage = new Stage();
                callsStage.setScene(scene);
            }
            assert callsStage != null;
            callsStage.show();
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong showing the Calls: "+e.getMessage(), e);
        }
    }
}
