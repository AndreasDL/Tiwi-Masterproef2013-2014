package be.iminds.ilabt.jfed.ui.javafx.advanced_gui;

import be.iminds.ilabt.jfed.highlevel.controller.TaskThread;
import be.iminds.ilabt.jfed.highlevel.model.AuthorityInfo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;

/**
 * AggregateManagerListCell
 */
public class AggregateManagerListCell extends ListCell<AuthorityInfo> {
    private AuthorityInfo authorityInfo;

    @FXML private HBox overview;
    @FXML private Label nameLabel;

    public AggregateManagerListCell() {
        URL location = getClass().getResource("AggregateManagerListCell.fxml");
        assert location != null;
        FXMLLoader fxmlLoader = new FXMLLoader(location, null);
//        fxmlLoader.setRoot(?);
        fxmlLoader.setController(this);

        try {
            overview = (HBox) fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create AggregateManagerListCell: "+e.getMessage(), e);
        }

        Object controllerTest = fxmlLoader.getController();
        assert controllerTest == this;
    }

    @Override
    public void updateItem(final AuthorityInfo item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null)
            setGraphic(null);
        else {
            if (authorityInfo != item) {
                if (authorityInfo != null) {
                    //cleanup previous
//                    sliver.statusProperty().removeListener(statusChangeListener);
                }

                assert(overview != null);

                this.authorityInfo = item;

//                sliver.statusProperty().addListener(statusChangeListener);
                nameLabel.setText(authorityInfo.getGeniAuthority().getName());
            }

            setGraphic(overview);
        }
    }

    public void viewInfo() {
        assert authorityInfo != null;

        //TODO
    }

    public void getVersion() {
        assert authorityInfo != null;

        TaskThread.Task call = authorityInfo.getEasyModel().getHighLevelController().getVersion(authorityInfo.getGeniAuthority());
        TaskThread.getInstance().addTask(call);
    }

    public void fetchAdvertisement() {
        assert authorityInfo != null;

        TaskThread.Task call = authorityInfo.getEasyModel().getHighLevelController().getAdvertisement(authorityInfo.getGeniAuthority());
        TaskThread.getInstance().addTask(call);
    }
}
