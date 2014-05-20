package be.iminds.ilabt.jfed.highlevel.history;

import be.iminds.ilabt.jfed.log.ApiCallDetails;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.log.ResultListener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ApiCallHistory
 */
public class ApiCallHistory implements ResultListener {
    private final Logger logger;

    public ApiCallHistory(Logger logger) {
        this.logger = logger;
        logger.addResultListener(this);
    }

    final ObservableList<ApiCallDetails> history = FXCollections.observableArrayList();
    public ObservableList<ApiCallDetails> getHistory() {
        return history;
    }

    @Override
    public void onResult(final ApiCallDetails result) {
        //this may be added asynchronously
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                history.add(result);
            }
        });
    }
}
