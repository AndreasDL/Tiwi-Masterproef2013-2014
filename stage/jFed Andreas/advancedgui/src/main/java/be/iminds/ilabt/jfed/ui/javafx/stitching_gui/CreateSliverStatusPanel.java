package be.iminds.ilabt.jfed.ui.javafx.stitching_gui;

import be.iminds.ilabt.jfed.highlevel.controller.JavaFXTaskThread;
import be.iminds.ilabt.jfed.highlevel.controller.TaskThread;
import be.iminds.ilabt.jfed.highlevel.model.EasyModel;
import be.iminds.ilabt.jfed.highlevel.stitcher.ParallelStitcher;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * CreateSliverStatusPanel
 */
public class CreateSliverStatusPanel extends BorderPane {
    @FXML private Label aggStatusLabel;
    @FXML private GridPane overviewGrid;

    @FXML private Label scsCallAggStatus;
    @FXML private HBox scsCallDetails;

    private final Map<ParallelStitcher.SliverReadyTracker, Label> aggCallAggStatus = new HashMap<ParallelStitcher.SliverReadyTracker, Label>();
    private final Map<ParallelStitcher.SliverReadyTracker, HBox> aggCallDetails = new HashMap<ParallelStitcher.SliverReadyTracker, HBox>();

    private ParallelStitcher parallelStitcher;
    public CreateSliverStatusPanel() {
        URL location = getClass().getResource("CreateSliverStatusPanel.fxml");
        assert location != null;
        FXMLLoader fxmlLoader = new FXMLLoader(location);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String statusToString(ParallelStitcher.Status status) {
        switch (status) {
            case INIT: { return "Initialising"; }
            case WAITING_FOR_DEP: { return "Waiting for VLAN info"; }
            case WAITING: { return "Waiting for call to schedule"; }
            case CREATING:  { return "CreateSliver call in progress"; }
            case CHANGING: { return "Sliver is activating"; }
            case FAIL: { return "Failure"; }
            case READY: { return "Ready"; }
            default: { return status+""; }
        }
    }

    private int row = 0;
    private void addAggregate(final ParallelStitcher.SliverReadyTracker tracker) {
        Label nameLabel = new Label(tracker.getAuth().getName());
        final Label statusLabel = new Label(statusToString(tracker.getStatus().get()));
        HBox detailsBox = new HBox();

        GridPane.setRowIndex(nameLabel,   2+row);
        GridPane.setRowIndex(statusLabel, 2+row);
        GridPane.setRowIndex(detailsBox,  2+row);

        GridPane.setColumnIndex(nameLabel, 0);
        GridPane.setColumnIndex(statusLabel, 1);
        GridPane.setColumnIndex(detailsBox,  2);

        overviewGrid.getChildren().addAll(nameLabel, statusLabel, detailsBox);

        aggCallAggStatus.put(tracker, statusLabel);
        aggCallDetails.put(tracker, detailsBox);

        row++;

        GridPane.setMargin(nameLabel, new Insets(30.0 /*top*/, 0.0 /*right*/, 30.0 /*bottom*/, 5.0 /*left*/));
        GridPane.setMargin(statusLabel, new Insets(5.0));

        tracker.getCreateTasks().addListener(new ListChangeListener<TaskThread.Task>() {
            @Override
            public void onChanged(Change<? extends TaskThread.Task> change) {
                while (change.next()) {
                    assert change.getRemovedSize() == 0 : "assumed no tasks could get deleted from this list (it's a history)";
                    for (TaskThread.Task addedTask : change.getAddedSubList()) {
                        addAggregateTask(tracker, addedTask, "CreateSliver");
                    }
                }
            }
        });
        for (TaskThread.Task task : tracker.getCreateTasks()) {
            addAggregateTask(tracker, task, "CreateSliver");
        }

        tracker.getStatusTasks().addListener(new ListChangeListener<TaskThread.Task>() {
            @Override
            public void onChanged(Change<? extends TaskThread.Task> change) {
                while (change.next()) {
                    assert change.getRemovedSize() == 0 : "assumed no tasks could get deleted from this list (it's a history)";
                    for (TaskThread.Task addedTask : change.getAddedSubList()) {
                        addAggregateTask(tracker, addedTask, "SliverStatus");
                    }
                }
            }
        });
        for (TaskThread.Task task : tracker.getStatusTasks()) {
            addAggregateTask(tracker, task, "SliverStatus");
        }

        tracker.getDeleteTasks().addListener(new ListChangeListener<TaskThread.Task>() {
            @Override
            public void onChanged(Change<? extends TaskThread.Task> change) {
                while (change.next()) {
                    assert change.getRemovedSize() == 0 : "assumed no tasks could get deleted from this list (it's a history)";
                    for (TaskThread.Task addedTask : change.getAddedSubList()) {
                        addAggregateTask(tracker, addedTask, "DeleteSliver");
                    }
                }
            }
        });
        for (TaskThread.Task task : tracker.getDeleteTasks()) {
            addAggregateTask(tracker, task, "DeleteSliver");
        }

        tracker.getStatus().addListener(new ChangeListener<ParallelStitcher.Status>() {
            @Override
            public void changed(ObservableValue<? extends ParallelStitcher.Status> observableValue, ParallelStitcher.Status oldStatus, ParallelStitcher.Status newStatus) {
                statusLabel.setText(statusToString(newStatus));
            }
        });
    }

    private void addAggregateTask(final ParallelStitcher.SliverReadyTracker tracker, TaskThread.Task task, String callName) {
        HBox detailsBox = aggCallDetails.get(tracker);
        assert detailsBox != null: "tracker not found: "+tracker;

        SingleTaskStatusPanel pan = new SingleTaskStatusPanel();
        pan.setCallName(callName);
        JavaFXTaskThread.Task javaFxTask = JavaFXTaskThread.getInstance().getJavaFXTaskByTask(task);
        pan.setTask(javaFxTask);
//        pan.setTracker(tracker);
        HBox.setMargin(pan, new Insets(5.0));

        detailsBox.getChildren().add(pan);
    }

    private void addSCSTask(TaskThread.Task task) {
        SingleTaskStatusPanel pan = new SingleTaskStatusPanel();
        pan.setCallName("ComputePath");
        JavaFXTaskThread.Task javaFxTask = JavaFXTaskThread.getInstance().getJavaFXTaskByTask(task);
        pan.setTask(javaFxTask);
        HBox.setMargin(pan, new Insets(5.0));

        scsCallDetails.getChildren().add(pan);
    }

    public void setParallelStitcher(ParallelStitcher parallelStitcher) {
        if (this.parallelStitcher != null) {
            throw new RuntimeException("CreateSliverStatusPanel does not support changing ParallelStitcher");
        }

        this.parallelStitcher = parallelStitcher;

        parallelStitcher.getScsCallOk().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldBoolean, Boolean newBoolean) {
                if (newBoolean) {
                    scsCallAggStatus.setText("OK");
                } else {
                    scsCallAggStatus.setText("Not OK");
                }
            }
        });
        scsCallAggStatus.setText(parallelStitcher.getScsCallOk().get() ? "OK" : "Not OK");

        parallelStitcher.getScsRequestTasks().addListener(new ListChangeListener<TaskThread.Task>() {
            @Override
            public void onChanged(Change<? extends TaskThread.Task> change) {
                while (change.next()) {
                    assert change.getRemovedSize() == 0 : "we assumed no tasks could get deleted from this list (it's a history)";
                    for (TaskThread.Task addedTask : change.getAddedSubList()) {
                        addSCSTask(addedTask);
                    }
                }
            }
        });

        parallelStitcher.getSliverTrackers().addListener(new ListChangeListener<ParallelStitcher.SliverReadyTracker>() {
            @Override
            public void onChanged(Change<? extends ParallelStitcher.SliverReadyTracker> change) {
                while (change.next()) {
                    assert change.getRemovedSize() == 0 : "we assumed no tasks could get deleted from this list (it's a history)";
                    for (ParallelStitcher.SliverReadyTracker addedTracker : change.getAddedSubList()) {
                        addAggregate(addedTracker);
                    }
                }
            }
        });

        parallelStitcher.getStitchingFinished().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasfinished, Boolean isFinished) {
                updateAggStatusLabel();
            }
        });
        updateAggStatusLabel();
    }

    private void updateAggStatusLabel() {
        if (parallelStitcher.getStitchingFinished().get()) {
            if (parallelStitcher.getStitchingSuccess().get())
                aggStatusLabel.setText("SUCCESS");
            else
                aggStatusLabel.setText("FAILURE");
        } else {
            aggStatusLabel.setText("CHANGING");
        }
    }

    public void deleteAll() {
        System.out.println("Clicked Stitcher Delete All");
        parallelStitcher.delete();
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Stage stitchingStage = null;
    public static void showStitchingOverview(EasyModel easyModel, ParallelStitcher parallelStitcher) {
        try {
            if (stitchingStage == null) {
                CreateSliverStatusPanel stitchingPanel = new CreateSliverStatusPanel();
                stitchingPanel.setParallelStitcher(parallelStitcher);

                Scene scene = new Scene(stitchingPanel);

                stitchingStage = new Stage();
                stitchingStage.setScene(scene);
            }
            assert stitchingStage != null;
            stitchingStage.show();
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong showing the Stitching overview: "+e.getMessage(), e);
        }
    }
}
