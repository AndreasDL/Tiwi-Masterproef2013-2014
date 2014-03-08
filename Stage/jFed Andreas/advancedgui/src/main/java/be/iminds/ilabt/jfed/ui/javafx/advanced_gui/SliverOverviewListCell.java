package be.iminds.ilabt.jfed.ui.javafx.advanced_gui;

import be.iminds.ilabt.jfed.highlevel.controller.TaskThread;
import be.iminds.ilabt.jfed.highlevel.model.RSpecInfo;
import be.iminds.ilabt.jfed.highlevel.model.Sliver;
import be.iminds.ilabt.jfed.highlevel.model.Status;
import be.iminds.ilabt.jfed.ui.rspeceditor.editor.RspecNodeController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.net.URL;

public class SliverOverviewListCell extends ListCell<Sliver>  {
    private Sliver sliver;

    private HBox overview;
    private Button refreshManifestButton;
    private Button refreshStatusButton;
    private Button deleteButton;

    private Label sliverNameLabel;
    private Label sliverStatusTextLabel;
    private Label sliverStatusLabel;
    private Label nodeCountTextLabel;
    private Label nodeCountLabel;

    public SliverOverviewListCell() {
        sliverNameLabel = new Label("NO SLIVER");
        sliverStatusTextLabel = new Label(" Status: ");
        sliverStatusLabel = new Label("<no status>");
        nodeCountTextLabel = new Label(" Node Count: ");
        nodeCountLabel = new Label("<no manifest>");

        refreshManifestButton = new Button("Refresh Manifest");
        refreshStatusButton = new Button("Refresh Status");
        deleteButton = new Button("Delete");

        overview = new HBox();
        overview.getChildren().addAll(sliverNameLabel, sliverStatusTextLabel, sliverStatusLabel, nodeCountTextLabel, nodeCountLabel);
        overview.getChildren().add(refreshManifestButton);
        overview.getChildren().add(refreshStatusButton);
        overview.getChildren().add(deleteButton);


        refreshManifestButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TaskThread.Task call = sliver.getSlice().getEasyModel().getHighLevelController().getManifest(sliver);
                TaskThread.getInstance().addTask(call);
            }
        });
        refreshStatusButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TaskThread.Task call = sliver.getSlice().getEasyModel().getHighLevelController().getSliverStatus(sliver);
                TaskThread.getInstance().addTask(call);
            }
        });
        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TaskThread.Task call = sliver.getSlice().getEasyModel().getHighLevelController().deleteSliver(sliver);
                TaskThread.getInstance().addTask(call);
            }
        });

        //a bit of a hack
        URL initialCssUrl = RspecNodeController.class.getResource("RspecEditorStyle.css");
        if (initialCssUrl == null) throw new RuntimeException("Error loading RspecEditorStyle.css");
        sliverStatusLabel.getStylesheets().add(initialCssUrl.toExternalForm());
    }

    private void updateStatusLabel() {
        sliverStatusLabel.getStyleClass().removeAll("rspec_background_changing", "rspec_background_fail", "rspec_background_ready", "rspec_background");
        switch (sliver.getStatus()) {
            case UNKNOWN:
            case CHANGING: { sliverStatusLabel.getStyleClass().add("rspec_background_changing"); break; }
            case FAIL: { sliverStatusLabel.getStyleClass().add("rspec_background_fail"); break; }
            case READY: { sliverStatusLabel.getStyleClass().add("rspec_background_ready"); break; }
            default:
            case UNINITIALISED:
            case UNALLOCATED: { sliverStatusLabel.getStyleClass().add("rspec_background"); break; }
        }
        sliverStatusLabel.setText("\"" + sliver.statusStringProperty().get() + "\" ("+sliver.getStatus()+")");
    }

    private void updateManifest() {
        RSpecInfo newRSpecInfo = sliver.getManifestRspec();
        if (newRSpecInfo == null || newRSpecInfo.getRSpec() == null)
            nodeCountLabel.textProperty().set("<no manifest>");
        else
            nodeCountLabel.textProperty().set(newRSpecInfo.getRSpec().getNodes().size()+"");
    }

    private ChangeListener<Status> statusChangeListener = new ChangeListener<Status>() {
        @Override
        public void changed(ObservableValue<? extends Status> observableValue, Status oldStatus, Status newStatus) {
            updateStatusLabel();
        }
    };
    private ChangeListener<String> statusStringChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observableValue, String oldStatus, String newStatus) {
            updateStatusLabel();
        }
    };

    private ChangeListener<RSpecInfo> rspecInfoChangeListener = new ChangeListener<RSpecInfo>() {
        @Override
        public void changed(ObservableValue<? extends RSpecInfo> observableValue, RSpecInfo oldRSpecInfo, RSpecInfo newRSpecInfo) {
            updateManifest();
        }
    };

    @Override
    public void updateItem(final Sliver item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null)
            setGraphic(null);
        else {
            if (sliver != item) {
                if (sliver != null) {
                    //cleanup previous
                    sliver.statusProperty().removeListener(statusChangeListener);
                    sliver.statusStringProperty().removeListener(statusStringChangeListener);
                    sliver.manifestRspecProperty().removeListener(rspecInfoChangeListener);
                }

                assert(overview != null);

                this.sliver = item;

                sliverNameLabel.setText("Slice "+sliver.getSlice().getName()+" sliver @ "+sliver.getAuthority().getName());
                sliver.statusProperty().addListener(statusChangeListener);
                sliver.statusStringProperty().addListener(statusStringChangeListener);
                sliver.manifestRspecProperty().addListener(rspecInfoChangeListener);

                //init of labels
                updateStatusLabel();
                updateManifest();
            }

            setGraphic(overview);
        }
    }
}
