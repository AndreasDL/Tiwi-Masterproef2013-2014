package be.iminds.ilabt.jfed.ui.javafx.call_gui;

import be.iminds.ilabt.jfed.highlevel.controller.JavaFXTaskThread;
import be.iminds.ilabt.jfed.highlevel.controller.TaskThread;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;

/**
 * TaskLabel
 */
public class TaskLabel extends HBox {
    @FXML private Label id;
    @FXML private Label name;
    @FXML private Label state;

    private ObjectProperty<JavaFXTaskThread.SingleTask> task = new SimpleObjectProperty<JavaFXTaskThread.SingleTask>(null);

    private ChangeListener<? super TaskThread.SingleTask.TaskState> stateChangeListener;

    public TaskLabel() {
        URL location = getClass().getResource("TaskLabel.fxml");

        //do it manually: fxml loading is too slow
        id = new Label("");
        name = new Label("");
        state = new Label("");

        HBox.setMargin(id, new Insets(0, 10.0, 0, 0)); //top, right, bottom, left
        HBox.setMargin(name, new Insets(0, 10.0, 0, 0)); //top, right, bottom, left

        getChildren().addAll(id, name, state);

//        assert location != null;
//        FXMLLoader fxmlLoader = new FXMLLoader(location);
//        fxmlLoader.setRoot(this);
//        fxmlLoader.setController(this);

//        try {
//            fxmlLoader.load();

            stateChangeListener = new ChangeListener<TaskThread.SingleTask.TaskState>() {
                @Override
                public void changed(ObservableValue<? extends TaskThread.SingleTask.TaskState> observableValue,
                                    TaskThread.SingleTask.TaskState oldTaskState,
                                    TaskThread.SingleTask.TaskState newTaskState) {
                    changeState(newTaskState);
                }
            };

            task.addListener(new ChangeListener<JavaFXTaskThread.SingleTask>() {
                @Override
                public void changed(ObservableValue<? extends JavaFXTaskThread.SingleTask> observableValue,
                                    JavaFXTaskThread.SingleTask oldSingleTask,
                                    JavaFXTaskThread.SingleTask newSingleTask) {
                    if (oldSingleTask != null) {
                        state.textProperty().unbind();
                        oldSingleTask.stateProperty().removeListener(stateChangeListener);
                    }
                    if (newSingleTask != null) {
                        name.setText(newSingleTask.getName());
                        id.setText(newSingleTask.getId());
                        state.textProperty().bind(Bindings.convert(newSingleTask.stateProperty()));
                        newSingleTask.stateProperty().addListener(stateChangeListener);
                        changeState(newSingleTask.getState());
                    }
                }
            });

//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    private void changeState(TaskThread.SingleTask.TaskState newTaskState) {
        state.getStyleClass().removeAll(
                "tasktstate_unsubmitted", "tasktstate_blocked", "tasktstate_queued",
                "tasktstate_running", "tasktstate_failed", "tasktstate_success");
        switch (newTaskState) {
            case UNSUBMITTED: { state.getStyleClass().add("tasktstate_unsubmitted"); break; }
            case BLOCKED: { state.getStyleClass().add("tasktstate_blocked"); break; }
            case QUEUED: { state.getStyleClass().add("tasktstate_queued"); break; }
            case RUNNING: { state.getStyleClass().add("tasktstate_running"); break; }
            case FAILED: { state.getStyleClass().add("tasktstate_failed"); break; }
            case SUCCESS: { state.getStyleClass().add("tasktstate_success"); break; }
        }
    }

    public void setTask(JavaFXTaskThread.SingleTask t) {
        task.set(t);
    }

    public ObjectProperty<JavaFXTaskThread.SingleTask> taskProperty() {
        return task;
    }
}
