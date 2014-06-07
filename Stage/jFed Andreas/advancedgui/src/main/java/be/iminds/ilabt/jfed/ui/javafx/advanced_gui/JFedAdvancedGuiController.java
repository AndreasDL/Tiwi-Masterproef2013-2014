/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.iminds.ilabt.jfed.ui.javafx.advanced_gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import be.iminds.ilabt.jfed.highlevel.controller.JavaFXTaskThread;
import be.iminds.ilabt.jfed.highlevel.controller.TaskThread;
import be.iminds.ilabt.jfed.highlevel.history.ApiCallHistory;
import be.iminds.ilabt.jfed.highlevel.model.AuthorityInfo;
import be.iminds.ilabt.jfed.highlevel.model.EasyModel;
import be.iminds.ilabt.jfed.highlevel.model.Slice;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.userloginmodel.UserLoginModelManager;
import be.iminds.ilabt.jfed.ui.javafx.about_gui.AboutBoxController;
import be.iminds.ilabt.jfed.ui.javafx.am_list_gui.AMList;
import be.iminds.ilabt.jfed.ui.javafx.call_gui.TasksPanel;
import be.iminds.ilabt.jfed.ui.javafx.compliance_test_gui.AutomatedTesterPanel;
import be.iminds.ilabt.jfed.ui.javafx.log_gui.LogHistoryPanel;
import be.iminds.ilabt.jfed.ui.javafx.probe_gui.ProbeController;
import be.iminds.ilabt.jfed.ui.javafx.userlogin.UserLoginController;
import be.iminds.ilabt.jfed.ui.javafx.util.JavaFXDialogUtil;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 *
 * @author wim
 */
public class JFedAdvancedGuiController implements Initializable {
    //    private final ListProperty<Slice> slices = new SimpleListProperty(FXCollections.observableArrayList()); //no local list, use list from High Level API
    private ReadOnlyListProperty<Slice> slices;
    private Map<Slice, SliceController> sliceControllers = new HashMap<Slice, SliceController>();

    private ApiCallHistory apiCallHistory;

    @FXML private ListView<AuthorityInfo> aggregateManagersListView;
    @FXML private ListView<Slice> sliceList;
    @FXML private TabPane tabPane;
    @FXML private Button refreshButton;
    @FXML private Label callCountLabel;
    @FXML private Label callOverviewLabel;
    @FXML private ProgressIndicator callBusyProgressIndicator;

    private UserLoginModelManager userLoginModel;
    public void setUserLoginModel(UserLoginModelManager userLoginModel) {
        this.userLoginModel = userLoginModel;
    }
    private Logger logger;
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    private EasyModel easyModel;
    public void setEasyModel(EasyModel easyModel) {
        assert this.easyModel == null;
        assert this.slices == null;
        assert easyModel != null;
        this.easyModel = easyModel;
        this.slices = easyModel.slicesProperty();
        assert this.slices != null;
        slices.addListener(new ListChangeListener<Slice>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Slice> change) {
                while (change.next()) {
                    for (Slice slice : change.getRemoved()) {
                        SliceController sliceController = sliceControllers.get(slice);
                        tabPane.getTabs().remove(sliceController.getSliceTab());
                        sliceControllers.remove(sliceController);
                    }
                    //adding to slice overview list will be automatic, as it listens to changes as well
                    //                    for (final Slice slice : change.getAddedSubList()) {
                    //                        SliceController sliceController = getSliceController(slice);
                    //                    }
                }
            }
        });

        assert sliceList != null;
        sliceList.setItems(slices);

        assert apiCallHistory == null;
        apiCallHistory = easyModel.getApiCallHistory();

        if (aggregateManagersListView != null)
            aggregateManagersListView.setItems(easyModel.getAuthorityList().authorityInfosProperty());
    }

    @FXML private void showManualCommandPanel() {
////        JavaFXDialogUtil.showMessage(("The manual command panel is not yet ported to JavaFX. Using old version: you will have to login again. Note that the call history is however linked.");
//        try {
//            //also show linked old manual command panel
//            AuthorityListModel authorityListModel = easyModel.getAuthorityList().getAuthorityListModel();
//            UserLoginPanel userLoginPanel = new UserLoginPanel(authorityListModel, easyModel.getLogger());
//            final ManualCommandPanel mainPan = new ManualCommandPanel(authorityListModel, easyModel.getLogger(), history, userLoginPanel, userLoginPanel);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        ProbeController.showProbe(easyModel);
    }

    private Stage historyStage;
    @FXML private void showLogHistory() {
        if (historyStage == null) {
            assert apiCallHistory != null;
            try {
                LogHistoryPanel logHistoryPanel = new LogHistoryPanel();
                logHistoryPanel.setApiCallHistory(apiCallHistory);

                historyStage = new Stage();
                historyStage.setTitle("Call Log History");
                Scene scene = new Scene(logHistoryPanel);
                historyStage.setScene(scene);
//                historyStage.sizeToScene();
                historyStage.setWidth(800.0);
                historyStage.setHeight(600.0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        historyStage.show();
    }

    @FXML private void taskOverview() {
        TasksPanel.showTasks(easyModel);
    }

    @FXML private void editAuthorityList() {
        AMList.editAuthorityList(easyModel.getAuthorityList(), userLoginModel);
    }

    @FXML private void showAutomatedTests() {
        AutomatedTesterPanel.showAutomatedTester(easyModel);
    }

    /** fetch SliceController, creating one if missing. */
    private SliceController getSliceController(Slice slice) {
        SliceController sliceController = sliceControllers.get(slice);

        if (sliceController == null) {
            sliceController = new SliceController(tabPane, slice, slices);
            sliceControllers.put(slice, sliceController); //TODO find an elegant link between slice and it's graphical objects
        }

        return sliceController;
    }

    public JFedAdvancedGuiController() {
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
//        Slice slice1 = createSlice("Test Slice 1");
//        Slice slice2 = createSlice("Test Slice 2");

        //learn sliceList how to display
        sliceList.setCellFactory(new Callback<ListView<Slice>, ListCell<Slice>>() {
            @Override
            public ListCell<Slice> call(ListView<Slice> list) {
                return new SliceCell();
            }
        }
        );

        final JavaFXTaskThread callThread = JavaFXTaskThread.getInstance();

        callCountLabel.textProperty().bind(Bindings.convert(callThread.unfinishedCallsCountProperty()));
        callBusyProgressIndicator.managedProperty().bind(callBusyProgressIndicator.visibleProperty());
        callBusyProgressIndicator.visibleProperty().bind(callThread.busyProperty());
        callThread.getAllTasks().addListener(new ListChangeListener<JavaFXTaskThread.SingleTask>() {
            @Override
            public void onChanged(Change<? extends JavaFXTaskThread.SingleTask> change) {
                String overviewText = "";
                for (JavaFXTaskThread.SingleTask call : callThread.getAllTasks()) {
                    if (call.getState() == TaskThread.SingleTask.TaskState.RUNNING) {
                        if (!overviewText.isEmpty())
                            overviewText += "   +    ";
                        overviewText += call.getName() + "";
                    }
                }
                callOverviewLabel.setText(overviewText);
            }
        });

        if (aggregateManagersListView != null) {
            Callback<ListView<AuthorityInfo>, ListCell<AuthorityInfo>> aggregateManagerCellFactory = new Callback<ListView<AuthorityInfo>, ListCell<AuthorityInfo>>() {
                @Override
                public ListCell<AuthorityInfo> call(ListView<AuthorityInfo> authorityInfoListView) {
                    return new AggregateManagerListCell();
                }};
            aggregateManagersListView.setCellFactory(aggregateManagerCellFactory);
        }
    }

    class SliceCell extends ListCell<Slice> {
        @Override
        public void updateItem(final Slice item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null)
                setGraphic(null);
            else {
                SliceController sliceController = getSliceController(item);
                assert(sliceController != null);
                setGraphic(sliceController.getOverviewNode());
            }
        }
    }

    public void refreshSlices() {
//        refreshButton.setDisable(true);

        List<TaskThread.Task> tasks = easyModel.getHighLevelController().refreshSlices();
        for (TaskThread.Task task : tasks) {
            assert task != null;
            if (task.isActive())
            {
                System.out.println("Note: refreshSlices call already running");
                return;
            }
        }

        TaskThread.getInstance().addTasks(tasks);
    }


    public void createSlice() {
        String newSliceName = CreateSliceDialogController.getDialogResult();
        if (newSliceName != null)
            createSlice(newSliceName);
    }

    public void createSlice(String sliceName) {
        TaskThread.getInstance().addTask(
                easyModel.getHighLevelController().createSlice(sliceName)
        );
    }

    public void userSettings() {
        UserLoginController.showUserLogin(logger, easyModel, userLoginModel, true/*requireAllUserInfo*/);
    }

    public void deleteSelectedSlices() {
        Map<Slice, SliceController> sliceControllersCopy = new HashMap(sliceControllers);
        List<Slice> sliceCopy = new ArrayList<Slice>(slices);
        for (Map.Entry<Slice, SliceController> e : sliceControllersCopy.entrySet()) {
            SliceController sliceController = e.getValue();

            if (sliceController.checkBox.isSelected()) {
                Slice slice = e.getKey();
                //don't remove it from the model
//                slices.remove(slice);

                //remove all slivers in the slice
                easyModel.getHighLevelController().deleteSlice(slice);
            }
        }
    }

    @FXML private void showAbout() {
        AboutBoxController.showAbout();
    }

}
