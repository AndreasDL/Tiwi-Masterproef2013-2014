package be.iminds.ilabt.jfed.ui.javafx.compliance_test_gui;

import be.iminds.ilabt.jfed.highlevel.model.EasyModel;
import be.iminds.ilabt.jfed.lowlevel.GeniUser;
import be.iminds.ilabt.jfed.lowlevel.api.test.TestClassList;
import be.iminds.ilabt.jfed.testing.base.ApiTest;
import be.iminds.ilabt.jfed.testing.base.ApiTestResult;
import be.iminds.ilabt.jfed.testing.base.RunTests;
import be.iminds.ilabt.jfed.ui.javafx.choosers.AuthorityChooser;
import be.iminds.ilabt.jfed.ui.javafx.log_gui.LogHistoryPanel;
import be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments.CommandArgumentChooser;
import be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments.StringArgumentChooser;
import be.iminds.ilabt.jfed.ui.javafx.util.JavaFXDialogUtil;
import be.iminds.ilabt.jfed.util.CommandExecutionContext;
import be.iminds.ilabt.jfed.util.IOUtils;
import be.iminds.ilabt.jfed.util.JavaFXLogger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * ProbeController
 */
public class AutomatedTesterPanel extends BorderPane {
    private EasyModel easyModel;

    @FXML private AuthorityChooser targetAuthChooser;
    @FXML private Label userAuthLabel;

    @FXML private ListView<String> testClassesListView;
    @FXML private ListView<String> testGroupsListView;

    @FXML private Button runButton;

    @FXML private VBox argumentsBox;

    @FXML private WebView webView;

    @FXML private Parent progressBarBox;
    @FXML private Label progressLabel;
    @FXML private ProgressBar progressBar;


    private ObservableList<String> testGroups = FXCollections.observableArrayList();

    public AutomatedTesterPanel() {
        URL location = getClass().getResource("AutomatedTester.fxml");
        assert location != null;
        FXMLLoader fxmlLoader = new FXMLLoader(location);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ObservableList<String> testClasses = FXCollections.observableArrayList(allUnitTestClasses());
        testClassesListView.setItems(testClasses);
        testGroupsListView.setItems(testGroups);

        testClassesListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldSelectedItem, String newSelectedItem) {
                updateSelectedClass(newSelectedItem);
            }
        });
        updateSelectedClass(testClassesListView.getSelectionModel().selectedItemProperty().get());

        progressBarBox.managedProperty().bind(progressBarBox.visibleProperty());
        progressBarBox.setVisible(false);
    }

    public EasyModel getEasyModel() {
        return easyModel;
    }

    public static List<String> allUnitTestClasses() {
        List<String> res = TestClassList.getInstance().allTestClasses();
        return res;
    }


    public void setEasyModel(EasyModel easyModel) {
        assert this.easyModel == null;
        this.easyModel = easyModel;
        JavaFXLogger logger = easyModel.getLogger();

        targetAuthChooser.setEasyModel(easyModel);
    }


    private Class<ApiTest> testClass;
    private Map<String, CommandArgumentChooser> extraArguments = new HashMap<>();
    public void updateSelectedClass(String selectedClassName) {
        if (selectedClassName == null) {
            testClass = null;
            testGroups.clear();
            return;
        }

        testClass = null;
        try {
            testClass = (Class<ApiTest>) Class.forName(selectedClassName);
            assert ApiTest.class.isAssignableFrom(testClass) : testClass.getName()+" is not a ApiTest class";
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Bug in Test GUI: could not find test class "+selectedClassName, e);
        }
        assert testClass != null;

        testGroups.setAll(testGroups(testClass));

        List<String> requiredProps = new ArrayList<>();
        List<String> optionalProps = new ArrayList<>();

        try {
            ApiTest test = testClass.newInstance();
            optionalProps = test.getOptionalConfigKeys();
            requiredProps = test.getRequiredConfigKeys();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        extraArguments.clear();
        argumentsBox.getChildren().clear();

        List<String> allKeys = new ArrayList<>(requiredProps);
        allKeys.addAll(optionalProps);
        if (!allKeys.isEmpty()) {
            Label head = new Label("Extra Test Arguments: ");
            VBox.setMargin(head, new Insets(10.0/*top*/, 0.0/*right*/, 0.0/*bottom*/, 0.0/*left*/));
            argumentsBox.getChildren().add(head);

            for (String key : allKeys) {
                boolean required = requiredProps.contains(key);

                CommandArgumentChooser commandArgumentChooser = new StringArgumentChooser("");
                HBox hBox = new HBox();
                Label l = new Label(key+":");
                HBox.setMargin(l, new Insets(0.0/*top*/, 10.0/*right*/, 0.0/*bottom*/, 0.0/*left*/));
                HBox.setHgrow(commandArgumentChooser, Priority.ALWAYS);
                hBox.getChildren().addAll(l, commandArgumentChooser);
                VBox.setMargin(hBox, new Insets(10.0/*top*/, 0.0/*right*/, 0.0/*bottom*/, 10.0/*left*/));
                argumentsBox.getChildren().add(hBox);
                extraArguments.put(key, commandArgumentChooser);
            }
        }

        if (runTask == null)
            progressBarBox.setVisible(false);
    }

    public List<String> testGroups(Class<ApiTest> testClass) {
        Set<String> allGroups = new HashSet<>();
        Map<Method, ApiTest.Test> testMethods = new HashMap<Method, ApiTest.Test>();
        List<Method> methods = Arrays.asList(testClass.getMethods());
        for (Method m : methods) {
            Annotation[] annotations = m.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof ApiTest.Test) {
                    ApiTest.Test testAnnotation = (ApiTest.Test) annotation;
                    testMethods.put(m, testAnnotation);
                    for (String group : testAnnotation.groups())
                        allGroups.add(group);
                }
            }
        }
        List<String> res = new ArrayList<>(allGroups);
        res.add("<no group>");
        return res;
    }

    Task<ApiTestResult> runTask;
    @FXML private void runTests() {
        if (runTask == null)
            progressBarBox.setVisible(false);

        if (runTask != null) {
            JavaFXDialogUtil.errorMessage("Already running tests. Wait until current tests are finished.", targetAuthChooser.getScene().getWindow());
            return;
        }
        if (!easyModel.getGeniUserProvider().isUserLoggedIn()) {
            JavaFXDialogUtil.errorMessage("Can only run tests if user is logged in: log in first", targetAuthChooser.getScene().getWindow());
            return;
        }
        if (targetAuthChooser.getSelectedAuthority() == null) {
            JavaFXDialogUtil.errorMessage("No target authority is selected", targetAuthChooser.getScene().getWindow());
            return;
        }
        if (testClassesListView.getSelectionModel().selectedItemProperty() == null || testClass == null) {
            JavaFXDialogUtil.errorMessage("No test is selected", targetAuthChooser.getScene().getWindow());
            return;
        }
        assert testClass != null;

        GeniUser user = easyModel.getGeniUserProvider().getLoggedInGeniUser();

        final CommandExecutionContext testContext =  new CommandExecutionContext(
                user,
                user.getUserAuthority(),
                targetAuthChooser.getSelectedAuthority().getGeniAuthority(),
                easyModel.getLogger());

        String group = testGroupsListView.getSelectionModel().getSelectedItem();
        if (group != null && group.equals("<no group>"))
            group = null;
        final String testGroup = group;

        final Properties props = new Properties();
        for (Map.Entry<String, CommandArgumentChooser> e : extraArguments.entrySet()) {
            String key = e.getKey();
            CommandArgumentChooser chooser = e.getValue();
            if (chooser.valueProperty() != null && !chooser.valueProperty().equals(""))
                props.setProperty(key, (String) chooser.valueProperty().getValue());
        }

        System.out.println("Running Test: " + testClass.getName() + " group=" + group);

        runTask = new Task<ApiTestResult>() {
            @Override protected ApiTestResult call() throws Exception {
                System.out.println("   Starting Tests");
                updateMessage("Starting Tests");
                updateProgress(0, 100);

                ApiTestResult result = RunTests.runTest(testContext, props, testClass, testGroup, new RunTests.TestListener() {
                        @Override
                        public void onStart(String testname, int testNr, int testCount) {
                            System.out.println("Running test: " + testname + "...");
                            updateMessage("Running test: " + testname);
                            updateProgress(testNr-1, testCount);
                        }

                        @Override
                        public void onResult(ApiTestResult.ApiTestMethodResult result, int testNr, int testCount) {
                            System.out.println("     Test result: " + result);
                            updateProgress(testNr, testCount);
                        }
                        @Override
                        public void onAllTestDone(ApiTestResult result, int testCount) {
                            updateProgress(testCount, testCount);
                            updateMessage("All tests completed");
                        }
                    }, false);
                final String outputfilename = "result-selectedClass.html";
                result.toHtml(new File(outputfilename));

                System.out.println("   Test Finished");

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        WebEngine webEngine = webView.getEngine();
                        try {
                            webEngine.loadContent(IOUtils.fileToString(outputfilename));
                        } catch (IOException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
//                        webEngine.load("file://"+outputfilename);
                        System.out.println("   Showing test");
                    }
                });

                runTask = null;
                return result;
            }
        };
        progressBar.progressProperty().bind(runTask.progressProperty());
        progressLabel.textProperty().bind(runTask.messageProperty());
        progressBarBox.setVisible(true);
        new Thread(runTask).start();
    }



    private static Stage stage = null;
    public static Stage showAutomatedTester(EasyModel easyModel) {
        return showAutomatedTester(easyModel, null);
    }
    public static Stage showAutomatedTester(EasyModel easyModel, Stage stage) {
        try {
            if (AutomatedTesterPanel.stage == null) {

                AutomatedTesterPanel automatedTesterPanel = new AutomatedTesterPanel();

                automatedTesterPanel.setEasyModel(easyModel);

                Scene scene = new Scene(automatedTesterPanel);

                if (stage != null)
                    AutomatedTesterPanel.stage = stage;
                else
                    AutomatedTesterPanel.stage = new Stage();
                AutomatedTesterPanel.stage.setScene(scene);
            }
            if (stage != null)
                assert AutomatedTesterPanel.stage == stage;
            assert AutomatedTesterPanel.stage != null;
            AutomatedTesterPanel.stage.show();
            return AutomatedTesterPanel.stage;
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong showing the Automated test panel: "+e.getMessage(), e);
        }
    }
}
