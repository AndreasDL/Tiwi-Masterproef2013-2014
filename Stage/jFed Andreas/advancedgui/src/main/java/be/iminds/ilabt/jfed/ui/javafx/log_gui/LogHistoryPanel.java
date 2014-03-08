package be.iminds.ilabt.jfed.ui.javafx.log_gui;

import be.iminds.ilabt.jfed.highlevel.history.ApiCallHistory;
import be.iminds.ilabt.jfed.log.ApiCallDetails;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * LogHistoryPanel
 *
 * TODO filters for logging
 * TODO follow and no follow mode
 * TODO no follow mode is a must
 *
 * TODO make another panel "CallHistoryPanel" that links calls with their results and shows history/ongoing/future calls
 */
public class LogHistoryPanel extends BorderPane {
    @FXML private ComboBox logChooser;
    @FXML private ToggleButton showNewCheckbox;

    @FXML private LogPanel logPanelController;

    //    private ApiCallHistory history;
    private ObservableList<ApiCallDetails> history;

    private BooleanProperty authShowLast;

    public LogHistoryPanel() {
        URL location = getClass().getResource("LogHistoryPanel.fxml");
        assert location != null;
        FXMLLoader fxmlLoader = new FXMLLoader(location);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            BorderPane self = (BorderPane) fxmlLoader.load();
            assert this == self;

            cssUrl = getClass().getResource("logpanel.css");
            if (cssUrl == null) throw new RuntimeException("Error loading logpanel.css");
            assert cssUrl != null;

            this.getStylesheets().add(cssUrl.toExternalForm());

            logChooser.setCellFactory(new Callback<ListView<ApiCallDetails>, ListCell<ApiCallDetails>>() {
                @Override
                public ListCell<ApiCallDetails> call(ListView<ApiCallDetails> list) {
                    return new ApiCallDetailsCell();
                }
            });
            logChooser.setButtonCell(new ApiCallDetailsCell());

            logPanelController.shownLogProperty().bind(logChooser.getSelectionModel().selectedItemProperty());

            authShowLast = showNewCheckbox.selectedProperty();
            authShowLast.set(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public BooleanProperty autoShowLastProperty() {
        return authShowLast;
    }
    public void setAutoShowLast(boolean val) {
        authShowLast.set(val);
    }
    public boolean isAutoShowLast() {
        return authShowLast.get();
    }

    private ListChangeListener<ApiCallDetails> apiChangeListener = null;
    public void clearApiCallHistory() {
        if (this.history != null) {
            history.removeListener(apiChangeListener);
            apiChangeListener = null;
            logChooser.setItems(FXCollections.observableArrayList());
            this.history = null;
        }
    }
    public void setApiCallHistory(ApiCallHistory history) {
        assert history != null;
        setApiCallHistory(history.getHistory());
    }
    public void setApiCallHistory(ObservableList<ApiCallDetails> history) {
        //only once
        if (this.history != null)
            clearApiCallHistory();

        assert history != null;
        assert apiChangeListener == null;

        this.history = history;

        apiChangeListener = new ListChangeListener<ApiCallDetails>() {
            @Override
            public void onChanged(Change<? extends ApiCallDetails> change) {
                //if first item added, select last item automatically
                //also, if item added and showNewCheckbox is selected, select last item automatically
                if ((showNewCheckbox.isSelected() || logChooser.getSelectionModel().isEmpty())
                        && !LogHistoryPanel.this.history.isEmpty()) {
                    //                    System.out.println("            => select one");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            logChooser.getSelectionModel().selectLast();
                        }
                    });
                }
            }
        };

        logChooser.setItems(history);
        history.addListener(apiChangeListener);

        if (!history.isEmpty())
            logChooser.getSelectionModel().selectLast();
    }

    private URL cssUrl;

    public void first() { logChooser.getSelectionModel().selectFirst(); }
    public void next() { logChooser.getSelectionModel().selectNext(); }
    public void prev() { logChooser.getSelectionModel().selectPrevious(); }
    public void last() { logChooser.getSelectionModel().selectLast(); }

    class ApiCallDetailsCell extends ListCell<ApiCallDetails> {
        private Label label = new Label();

        public ApiCallDetailsCell() {
            assert cssUrl != null;
            this.getStylesheets().add(cssUrl.toExternalForm());
            label.getStyleClass().add("select_log_label");
        }

        @Override
        public void updateItem(final ApiCallDetails item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null)
                setGraphic(null);
            else {
                label.getStyleClass().removeAll("select_log_label_success", "select_log_label_fail", "select_log_label_busy");
                label.setText(item.getApiName() + " - " + item.getGeniMethodName());
                boolean success = false;
                boolean busy = false;
                if (item.getReply() != null) {
                    if (item.getReply().getGeniResponseCode() != null) {
                        if (item.getReply().getGeniResponseCode().isSuccess())
                            success = true;
                        if (item.getReply().getGeniResponseCode().isBusy())
                            busy = true;
                    }
                }
                if (success)
                    label.getStyleClass().add("select_log_label_success");
                else {
                    if (busy)
                        label.getStyleClass().add("select_log_label_busy");
                    else
                        label.getStyleClass().add("select_log_label_fail");
                }
                setGraphic(label);
            }
        }
    }
}
