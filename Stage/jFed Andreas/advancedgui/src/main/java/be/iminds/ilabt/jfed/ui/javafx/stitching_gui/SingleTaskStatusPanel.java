package be.iminds.ilabt.jfed.ui.javafx.stitching_gui;

import be.iminds.ilabt.jfed.highlevel.controller.JavaFXTaskThread;
import be.iminds.ilabt.jfed.highlevel.controller.TaskThread;
import be.iminds.ilabt.jfed.highlevel.stitcher.ParallelStitcher;
import be.iminds.ilabt.jfed.ui.javafx.style.StyleHelper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * SingleTaskStatusPanel:
 *
 * visualizes task status with logs for call and exceptions.
 */
public class SingleTaskStatusPanel extends VBox implements ChangeListener<TaskThread.SingleTask.TaskState> {
    private String callName;
    private JavaFXTaskThread.Task task;
//    private ParallelStitcher.SliverReadyTracker tracker;

    private Label nameLabel;
    private Label statusLabel;
    private ProgressIndicator progressIndicator;

    public SingleTaskStatusPanel() {
        getStyleClass().add("panel-bordered");

        nameLabel = new Label("uninitialised");
        statusLabel = new Label("uninitialised");
        progressIndicator = new ProgressIndicator();
        progressIndicator.managedProperty().bind(progressIndicator.visibleProperty());
        progressIndicator.setVisible(false);

        VBox.setMargin(nameLabel, new Insets(5.0));
        VBox.setMargin(statusLabel, new Insets(10.0));
        VBox.setMargin(progressIndicator, new Insets(5.0));

        getChildren().addAll(nameLabel, statusLabel, progressIndicator);
    }

    public void setCallName(String callName) {
        this.callName = callName;
        nameLabel.setText(callName);
    }

    private Timeline retryTimoutUpdater;
    public void setTask(JavaFXTaskThread.Task task) {
        assert task != null;
        if (this.task != null) {
            throw new RuntimeException("This class assumes setTask is only called once ever. Needs rewrite to support task change.");
        }

        this.task = task;

        assert Platform.isFxApplicationThread();
        for (JavaFXTaskThread.SingleTask singleTask : task.getCurrentActiveSingleTasks()) {
            singleTask.stateProperty().addListener(this);
        }

        task.getCurrentActiveSingleTasks().addListener(new ListChangeListener<JavaFXTaskThread.SingleTask>() {
            @Override
            public void onChanged(Change<? extends JavaFXTaskThread.SingleTask> change) {
                while (change.next()) {
                    for (JavaFXTaskThread.SingleTask singleTask : change.getAddedSubList())
                        singleTask.stateProperty().addListener(SingleTaskStatusPanel.this);
                    //keep listening anyway
//                    for (JavaFXTaskThread.SingleTask singleTask : change.getRemoved())
//                        singleTask.stateProperty().removeListener(SingleTaskStatusPanel.this);
                }
            }
        });

        List<JavaFXTaskThread.FutureTask> currentFutures = SingleTaskStatusPanel.this.task.getFutures();
        if (!currentFutures.isEmpty()) {
            JavaFXTaskThread.FutureTask firstFutureTask = currentFutures.get(0);
            statusLabel.setText("Waiting before retry ("+(firstFutureTask.getTimeLeftMs().get()/1000)+" s)"); //TODO use observable value

            retryTimoutUpdater = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    List<JavaFXTaskThread.FutureTask> futures = SingleTaskStatusPanel.this.task.getFutures();
                    if (futures.isEmpty())
                        retryTimoutUpdater.stop();
                    else {
                        JavaFXTaskThread.FutureTask firstFutureTask = futures.get(0);
                        long timeout = firstFutureTask.getTimeLeftMs().get(); //TODO use observable value
                        if (timeout > 0)
                            statusLabel.setText("Waiting before retry ("+(timeout/1000)+" s)");
                    }
                }
            }));
            retryTimoutUpdater.setCycleCount(Timeline.INDEFINITE);
            retryTimoutUpdater.play();
        }
    }

//    public void setTracker(ParallelStitcher.SliverReadyTracker tracker) {
//        assert tracker != null;
//        if (this.tracker != null) {
//            throw new RuntimeException("This class assumes setTracker is only called once ever. Needs rewrite to support tracker change.");
//        }
//
//        this.tracker = tracker;
//    }



    private static List<String> styles = FXCollections.observableArrayList(
            "tasktstate_unsubmitted", "tasktstate_blocked", "tasktstate_queued",
            "tasktstate_running", "tasktstate_failed", "tasktstate_success");
    @Override
    public void changed(ObservableValue<? extends TaskThread.SingleTask.TaskState> observableValue,
                        TaskThread.SingleTask.TaskState oldTaskState,
                        TaskThread.SingleTask.TaskState newTaskState) {
        statusLabel.setText(newTaskState.name());

        String styleClass = null;
        switch (newTaskState) {
            case UNSUBMITTED: { progressIndicator.setVisible(false); styleClass = "tasktstate_unsubmitted"; break; }
            case BLOCKED:     { progressIndicator.setVisible(true);  styleClass = "tasktstate_blocked";     break; }
            case QUEUED:      { progressIndicator.setVisible(true);  styleClass = "tasktstate_queued";      break; }
            case RUNNING:     { progressIndicator.setVisible(true);  styleClass = "tasktstate_running";     break; }
            case FAILED:      { progressIndicator.setVisible(false); styleClass = "tasktstate_failed";      break; }
            case SUCCESS:     { progressIndicator.setVisible(false); styleClass = "tasktstate_success";     break; }
            default: progressIndicator.setVisible(false);
        }
        StyleHelper.setStyleClass(statusLabel, styleClass, styles);
    }
}
