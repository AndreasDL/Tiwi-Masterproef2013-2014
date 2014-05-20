package be.iminds.ilabt.jfed.ui.javafx.call_gui;

import be.iminds.ilabt.jfed.highlevel.controller.JavaFXTaskThread;
import be.iminds.ilabt.jfed.highlevel.controller.TaskThread;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;

/**
 * CallList
 */
public class TaskList extends ListView<JavaFXTaskThread.SingleTask> {
    public TaskList() {
        URL location = getClass().getResource("CallList.fxml");
        assert location != null;
        FXMLLoader fxmlLoader = new FXMLLoader(location);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

            this.setCellFactory(new Callback<ListView<JavaFXTaskThread.SingleTask>, ListCell<JavaFXTaskThread.SingleTask>>() {
                @Override
                public ListCell<JavaFXTaskThread.SingleTask> call(ListView<JavaFXTaskThread.SingleTask> callListView) {
                    return new CallCell();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class CallCell extends ListCell<JavaFXTaskThread.SingleTask> {
        private TaskLabel label = new TaskLabel();
        @Override
        public void updateItem(final JavaFXTaskThread.SingleTask item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null) {
                label.setTask(null);
                setGraphic(null);
            }
            else {
                label.setTask(item);
                setGraphic(label);
            }
        }
    }
//    private class CallCell extends ListCell<TaskThread.SingleTask> {
//        private Label label = new Label();
//        @Override
//        public void updateItem(final TaskThread.SingleTask item, boolean empty) {
//            super.updateItem(item, empty);
//
//            if (item == null) {
//                label.textProperty().unbind();
//                setGraphic(null);
//            }
//            else {
////                label.setText(item.getName() + " " + item.getState());
//
////                StringExpression stateString = Bindings.convert(item.stateProperty());
//                StringExpression labelText = Bindings.concat(item.getName(), " ", item.stateProperty());
//                label.textProperty().bind(labelText);
//                setGraphic(label);
//            }
//        }
//    }
}
