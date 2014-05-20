package be.iminds.ilabt.jfed.ui.javafx.probe_gui;

import be.iminds.ilabt.jfed.highlevel.controller.TaskThread;
import be.iminds.ilabt.jfed.highlevel.model.AuthorityInfo;
import be.iminds.ilabt.jfed.highlevel.model.CredentialInfo;
import be.iminds.ilabt.jfed.highlevel.model.EasyModel;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.*;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceId;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceIdParser;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceUrn;
import be.iminds.ilabt.jfed.ui.javafx.choosers.AuthorityChooser;
import be.iminds.ilabt.jfed.ui.javafx.log_gui.LogHistoryPanel;
import be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments.CommandArgumentChooser;
import be.iminds.ilabt.jfed.ui.javafx.util.ExpandableHelp;
import be.iminds.ilabt.jfed.ui.javafx.util.JavaFXDialogUtil;
import be.iminds.ilabt.jfed.util.ClientSslAuthenticationXmlRpcTransportFactory;
import be.iminds.ilabt.jfed.util.JavaFXLogger;
import be.iminds.ilabt.jfed.util.SSLCertificateDownloader;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * ProbeController
 */
public class ProbeController implements Initializable {
    private EasyModel easyModel;

    @FXML private LogHistoryPanel logPanel;

    @FXML private TreeView apiAndMethodTreeView;
    @FXML private Button callButton;


    //////////////////////////////// selection of URL ///////////////////////////////////
    @FXML private RadioButton useUserRadioButton;
    @FXML private RadioButton userAuthorityRadioButton;
    @FXML private RadioButton userCustomServerRadioButton;

    @FXML private HBox userAuthChoiceBox;
    @FXML private AuthorityChooser authChooser;

    @FXML private Label loggedInUserLabel;

    @FXML private HBox fixedServerURLBox;
    @FXML private VBox editableServerURLBox;

    @FXML private TextField serverUrlField;

    @FXML private TextField customServerUrlField;
    @FXML private CheckBox ignoreSelfSignedCheckBox;
    @FXML private Label ignoreSelfSignedCheckLabel;
    /////////////////////////////////////////////////////////////////////////////////////

    @FXML private VBox commandNameBox;
    @FXML private Label commandNameLabel;
    @FXML private ExpandableHelp commandHelp;


    @FXML private ScrollPane argumentScrollPane;
    @FXML private GridPane argumentGrid;
    private final ObservableList<CommandParameterController> argumentControllers = FXCollections.observableArrayList();
    private final ObservableList<CommandParameterModel> argumentModels = FXCollections.observableArrayList();


    private final ObservableList<AbstractApi> apis = FXCollections.observableArrayList();
    final ObservableList<MethodInfo> methods = FXCollections.observableArrayList();

    public ProbeController() {
    }

    public EasyModel getEasyModel() {
        return easyModel;
    }

    public void setEasyModel(EasyModel easyModel) {
        assert this.easyModel == null;
        this.easyModel = easyModel;
        JavaFXLogger logger = easyModel.getLogger();

        apis.add(new SliceAuthority(logger));
        apis.add(new PlanetlabSfaRegistryInterface(logger));
        apis.add(new AggregateManager2(logger));
        apis.add(new AggregateManager3(logger));
        apis.add(new ProtoGeniClearingHouse1(logger));

        apis.add(new StitchingComputationService(logger));

        apis.add(new UniformFederationRegistryApi(logger));
        apis.add(new UniformFederationMemberAuthorityApi(logger));
        apis.add(new UniformFederationSliceAuthorityApi(logger));

        if (logPanel != null && (apiAndMethodTreeView != null))
            doAllInit();
    }

    public void updateServerUrlChoiceOnApiChange(AbstractApi newSelectedAbstractApi) {
        boolean isSA = newSelectedAbstractApi == null ? false : newSelectedAbstractApi.getServerType().getRole().equals(ServerType.GeniServerRole.PROTOGENI_SA);

        if (true) {
            if (easyModel.getGeniUserProvider().isUserLoggedIn()) {
                loggedInUserLabel.setText("User "+easyModel.getGeniUserProvider().getLoggedInGeniUser().getUserUrn()+"");
                boolean wasInvisible = !useUserRadioButton.isVisible();
                useUserRadioButton.setVisible(true);
                if (wasInvisible && isSA)
                    useUserRadioButton.setSelected(true);
            }
            else {
                useUserRadioButton.setVisible(false);
                //loggedInUserLabel will not be visible now, we update text anyway
                loggedInUserLabel.setText("No User logged in.");
            }
        }
        else {
            useUserRadioButton.setVisible(false);
            //loggedInUserLabel will not be visible now, we update text anyway
            loggedInUserLabel.setText("Not using user's auth server URL");
        }

        updateFixedServerUrlField(newSelectedAbstractApi);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert logPanel != null;
        assert apiAndMethodTreeView != null;
        assert useUserRadioButton != null;

        //if any of these is set invisible, remove them from layout as well
        useUserRadioButton.managedProperty().bind(useUserRadioButton.visibleProperty());
        userAuthChoiceBox.managedProperty().bind(userAuthChoiceBox.visibleProperty());
        fixedServerURLBox.managedProperty().bind(fixedServerURLBox.visibleProperty());
        editableServerURLBox.managedProperty().bind(editableServerURLBox.visibleProperty());
        ignoreSelfSignedCheckLabel.managedProperty().bind(ignoreSelfSignedCheckLabel.visibleProperty());
        loggedInUserLabel.managedProperty().bind(loggedInUserLabel.visibleProperty());

        //bind radiobuttons to visible regions
        userAuthChoiceBox.visibleProperty().bind(userAuthorityRadioButton.selectedProperty().or(useUserRadioButton.selectedProperty()));
        fixedServerURLBox.visibleProperty().bind(userAuthorityRadioButton.selectedProperty().or(useUserRadioButton.selectedProperty()));
        editableServerURLBox.visibleProperty().bind(userCustomServerRadioButton.selectedProperty());

        //bind other logical connections
        loggedInUserLabel.visibleProperty().bind(useUserRadioButton.selectedProperty());
        authChooser.editableProperty().bind(userAuthorityRadioButton.selectedProperty());
        ignoreSelfSignedCheckLabel.visibleProperty().bind(ignoreSelfSignedCheckBox.selectedProperty());
        useUserRadioButton.visibleProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldIsVisible, Boolean newIsVisible) {
                if (!newIsVisible && useUserRadioButton.isSelected())
                    userAuthorityRadioButton.selectedProperty().set(true);
            }
        });

        if (easyModel != null)
            doAllInit();
    }

    private MethodInfo currentlySelectedTreeMethod = null;
    public AbstractApi currentlySelectedTreeApi() { return currentlySelectedTreeMethod == null ? null : currentlySelectedTreeMethod.api; }
    private Map<TreeItem<String>, MethodInfo> treeItemToMethodInfo;
    private Map<TreeItem<String>, AbstractApi> treeItemToAbstractApi;
    public void initTreeView() {
        TreeItem<String> rootItem = new TreeItem<String> ("APIs and Methods", null/*icon*/);
        rootItem.setExpanded(true);

        treeItemToMethodInfo = new HashMap<TreeItem<String>, MethodInfo>();
        treeItemToAbstractApi = new HashMap<TreeItem<String>, AbstractApi>();

        for (AbstractApi api : apis) {
            TreeItem<String> apiItem = new TreeItem<String> (api.getName());
            rootItem.getChildren().add(apiItem);
//            apiItem.setExpanded(true);
            treeItemToAbstractApi.put(apiItem, api);

            List<MethodInfo> methods = findAvailableMethods(api);
            for (MethodInfo method : methods) {
                TreeItem<String> methodItem = new TreeItem<String> (method.name);
                apiItem.getChildren().add(methodItem);
                treeItemToMethodInfo.put(methodItem, method);
            }
        }

        apiAndMethodTreeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object oldTreeItem, Object newTreeItem) {
                MethodInfo oldMethodInfo = treeItemToMethodInfo.get(oldTreeItem);
                MethodInfo newMethodInfo = treeItemToMethodInfo.get(newTreeItem);

                AbstractApi newAbstractApi = treeItemToAbstractApi.get(newTreeItem);
                if (newAbstractApi != null) {
                    TreeItem treeItem = (TreeItem) newTreeItem;
                    treeItem.setExpanded(!treeItem.isExpanded());
                }

                //this debug showed that this actually skips some changes! (weird)
//                System.out.println("changed from "+oldTreeItem+" to "+newTreeItem+"   "+
//                    (oldMethodInfo == null ? "null" : oldMethodInfo.name)+
//                    " -> "+
//                        (newMethodInfo == null ? "null" : newMethodInfo.name));

                currentlySelectedTreeMethod = newMethodInfo;
                selectMethod(oldMethodInfo, newMethodInfo);

                updateServerUrlChoiceOnApiChange(currentlySelectedTreeApi());
            }
        });

        apiAndMethodTreeView.setShowRoot(false);
        apiAndMethodTreeView.setRoot(rootItem);
    }

    public void updateFixedServerUrlField(AbstractApi newApi) {
        String authUrl = "NONE";
        boolean error = true;

        if (newApi != null) {
            ServerType st = newApi.getServerType();
            boolean isSA = st.getRole().equals(ServerType.GeniServerRole.PROTOGENI_SA);

            if (isSA && !useUserRadioButton.isSelected()) {
                if (!easyModel.getGeniUserProvider().isUserLoggedIn()) {
                    authUrl = "No user logged in";
                    error = true;
                }
                else {
                    GeniUser geniUser = easyModel.getGeniUserProvider().getLoggedInGeniUser();
                    assert geniUser != null;
                    SfaAuthority userSfaAuth = geniUser.getUserAuthority();
                    URL url = userSfaAuth.getUrl(st);
                    if (url == null) {
                        authUrl = "User Authority "+userSfaAuth.getName()+" has no URL for "+st;
                        error = true;
                    }
                    else {
                        authUrl = url.toExternalForm();
                        error = false;
                    }
                }
            } else {
                AuthorityInfo authorityInfo = authChooser.getSelectedAuthority();
                if (authorityInfo != null) {
                    URL url = authorityInfo.getGeniAuthority().getUrl(st);
                    if (url == null) {
                        authUrl = "Authority "+authorityInfo.getGeniAuthority().getName()+" has no URL for "+st;
                        error = true;
                    }
                    else {
                        authUrl = url.toExternalForm();
                        error = false;
                    }
                }
            }
        }

        serverUrlField.getStyleClass().remove("textFieldError");
        if (error)
            serverUrlField.getStyleClass().add("textFieldError");
        serverUrlField.setText(authUrl);
    }


    private ColumnConstraints colInfoInclude = new ColumnConstraints();
    private ColumnConstraints colInfoName = new ColumnConstraints();
    private ColumnConstraints colInfoValue = new ColumnConstraints();

    /** init after easymodel set, and fxml loaded. (typically after setEasyModel, but you never know)*/
    private void doAllInit() {
        assert logPanel != null;
        assert authChooser != null;

        assert easyModel != null;

        authChooser.setEasyModel(easyModel);

        initTreeView();

        authChooser.selectedAuthorityProperty().addListener(new ChangeListener<AuthorityInfo>() {
            @Override
            public void changed(ObservableValue<? extends AuthorityInfo> observableValue, AuthorityInfo oldAuthorityInfo, AuthorityInfo newAuthorityInfo) {
                updateFixedServerUrlField(currentlySelectedTreeApi());
            }
        });
        useUserRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldBool, Boolean newIsSelected) {
                if (newIsSelected) updateFixedServerUrlField(currentlySelectedTreeApi());
                if (newIsSelected && easyModel.getGeniUserProvider().isUserLoggedIn())
                    authChooser.select(easyModel.getGeniUserProvider().getLoggedInGeniUser().getUserAuthority());
            }
        });
        userAuthorityRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldBool, Boolean newIsSelected) {
                if (newIsSelected) updateFixedServerUrlField(currentlySelectedTreeApi());
            }
        });


        colInfoInclude = new ColumnConstraints();
        colInfoName = new ColumnConstraints();
        colInfoValue = new ColumnConstraints();
        colInfoValue.setHgrow(Priority.ALWAYS);
        colInfoValue.setMaxWidth(Double.MAX_VALUE);

//        colInfoInclude.setPercentWidth(10);
//        colInfoName.setPercentWidth(30);
//        colInfoValue.setPercentWidth(60);

        argumentGrid.getColumnConstraints().add(colInfoInclude);
        argumentGrid.getColumnConstraints().add(colInfoName);
        argumentGrid.getColumnConstraints().add(colInfoValue);

        RowConstraints headerRowinfo = new RowConstraints();
        argumentGrid.getRowConstraints().add(headerRowinfo);

        logPanel.setApiCallHistory(easyModel.getApiCallHistory());

        //if content of scrollpane is smaller than scrollpane, resize it to fit
////        argumentScrollPane.setFitToHeight(true);
//        argumentScrollPane.setFitToWidth(true);

////        argumentGrid.setPrefHeight(Region.USE_COMPUTED_SIZE);
////        argumentGrid.setPrefWidth(Region.USE_COMPUTED_SIZE);
//
//        argumentScrollPane.prefViewportWidthProperty().bind(argumentGrid.prefWidthProperty());
//        argumentScrollPane.prefViewportHeightProperty().bind(argumentGrid.prefHeightProperty());
//
////        argumentScrollPane.maxWidthProperty().bind(argumentGrid.prefWidthProperty());
////        argumentScrollPane.maxHeightProperty().bind(argumentGrid.prefHeightProperty());
////        argumentScrollPane.maxHeightProperty().set(Region.USE_PREF_SIZE);
//
////        argumentScrollPane.prefWidthProperty().bind(argumentGrid.prefWidthProperty());
////        argumentScrollPane.prefHeightProperty().bind(argumentGrid.prefHeightProperty());
    }

    private static class CommandParameterController {
        public final CommandParameterModel model;

        public final CheckBox includedCheckBox;
        public final Label requiredLabel;
        public final Control includedColumnItem;

        public final Label nameLabel;
        public final VBox commandArgumentChooserBox;
        public final ExpandableHelp commandArgumentChooserHelper;
        public final CommandArgumentChooser commandArgumentChooser;

        public CommandParameterController(CommandParameterModel model, EasyModel easyModel) {
            this.model = model;

            includedCheckBox = new CheckBox();
            requiredLabel = new Label("required");

            if (model.isRequired()) {
                includedColumnItem = requiredLabel;
            } else {
                includedColumnItem = includedCheckBox;
                includedCheckBox.selectedProperty().bindBidirectional(model.includedProperty());
            }

            nameLabel = new Label(model.getParameterName());

            commandArgumentChooser = CommandArgumentChooser.getCommandArgumentChooser(
                    model.getParameterName(),
                    model.getParameterClass(),
                    model.getAnnotation(),
                    easyModel,
                    model.getMethodInfo());
            assert commandArgumentChooser != null;
            assert commandArgumentChooser.valueProperty() != null : "commandArgumentChooser.valueProperty() is null for name="+model.getParameterName()+" class="+model.getParameterClass()+" chooserClass="+commandArgumentChooser.getClass().getName();
            model.valueProperty().bind(commandArgumentChooser.valueProperty());

            commandArgumentChooserBox = new VBox();
            String helpText = model.getAnnotation().hint();
            commandArgumentChooserHelper = new ExpandableHelp(helpText);
            if (helpText != null && !helpText.isEmpty())
                commandArgumentChooserBox.getChildren().add(commandArgumentChooserHelper);
            commandArgumentChooserBox.getChildren().add(commandArgumentChooser);

            commandArgumentChooserHelper.setMaxWidth(500.0);
            commandArgumentChooser.setMaxWidth(500.0);
        }

        public void add(GridPane gridPane, int row) {
            GridPane.setConstraints(includedColumnItem, 0/*col*/, row/*row*/);
            GridPane.setConstraints(nameLabel, 1, row);
            GridPane.setConstraints(commandArgumentChooserBox, 2, row);

//            GridPane.setMargin(includedColumnItem, new Insets(10, 10, 10, 10));
            GridPane.setHalignment(includedColumnItem, HPos.CENTER);

//            GridPane.setMargin(nameLabel, new Insets(10, 10, 10, 10));
            GridPane.setHalignment(nameLabel, HPos.RIGHT);

//            GridPane.setMargin(commandArgumentChooserBox, new Insets(10, 10, 10, 10));
            GridPane.setHalignment(commandArgumentChooserBox, HPos.CENTER);

            GridPane.setHgrow(includedColumnItem, Priority.NEVER);
            GridPane.setHgrow(nameLabel, Priority.NEVER);
            GridPane.setHgrow(commandArgumentChooserBox, Priority.ALWAYS);

            gridPane.getChildren().add(includedColumnItem);
            gridPane.getChildren().add(nameLabel);
            gridPane.getChildren().add(commandArgumentChooserBox);

            includedColumnItem.setMinWidth(includedColumnItem.getMaxWidth());
            nameLabel.setMinWidth(nameLabel.getMaxWidth());
            includedColumnItem.setPrefWidth(includedColumnItem.getMaxWidth());
            nameLabel.setPrefWidth(nameLabel.getMaxWidth());
        }

        public void remove(GridPane gridPane) {
            model.valueProperty().unbind();
            boolean removedItem = gridPane.getChildren().remove(includedColumnItem);
            boolean removedItem2 = gridPane.getChildren().remove(nameLabel);
            boolean removedItem3 = gridPane.getChildren().remove(commandArgumentChooserBox);
            assert removedItem;
            assert removedItem2;
            assert removedItem3;
        }
    }

    public static class MethodInfo {
        public AbstractApi api;
        public Method method;
        public ApiMethod annotation;
        public String name;

        public MethodInfo (AbstractApi api, Method method, ApiMethod annotation) {
            this.api = api;
            this.method = method;
            this.annotation = annotation;
            this.name = method.getName();
        }
        public MethodInfo (AbstractApi api, Method method) { this(api, method, method.getAnnotation(ApiMethod.class)); }

        public String toString() {
            return name;
        }
    }

    private static List<MethodInfo> findAvailableMethods(AbstractApi api) {
        Class targetClass = api.getClass();
        List<MethodInfo> res = new ArrayList<MethodInfo>();

        for (Method m : targetClass.getDeclaredMethods()) {//GeniConnection con,
            if (m.isAnnotationPresent(ApiMethod.class)) {
                ApiMethod am = m.getAnnotation(ApiMethod.class);
                Class[] parameterTypes = m.getParameterTypes();
                if (parameterTypes.length == 0 || !GeniConnection.class.isAssignableFrom(parameterTypes[0]))
                    throw new RuntimeException("Method "+m+" does not have GeniConnection as first parameter but it does have @ApiMethod");
                res.add(new MethodInfo(api, m, am));
            }
        }

        return res;
    }

    private void selectMethod(MethodInfo oldMethod, MethodInfo newMethod) {
        if (oldMethod != null || newMethod != null) {
            for (CommandParameterController controller: argumentControllers) {
                controller.remove(argumentGrid);
            }
            argumentControllers.clear();
            argumentModels.clear();

            argumentGrid.getRowConstraints().clear();
        }

        if (commandHelp != null)
            commandNameBox.getChildren().remove(commandHelp);

        if (newMethod != null) {
            assert argumentModels.isEmpty();

            RowConstraints headerRowinfo = new RowConstraints();
            argumentGrid.getRowConstraints().add(headerRowinfo);

            int rowIndex = 1;
            argumentModels.setAll(createCommandParameterModels(newMethod));
            for (CommandParameterModel model : argumentModels) {
                RowConstraints rowinfo = new RowConstraints();
//                rowinfo.setPercentHeight(50);
                argumentGrid.getRowConstraints().add(rowinfo);

                CommandParameterController controller = new CommandParameterController(model, easyModel);
                controller.add(argumentGrid, rowIndex++);

                argumentControllers.add(controller);
            }

            commandHelp = new ExpandableHelp(newMethod.annotation.hint());
            commandNameBox.getChildren().add(commandHelp);
            commandNameLabel.setText(newMethod.name);
            if (!commandNameLabel.getStyleClass().contains("boldText"))
                commandNameLabel.getStyleClass().add("boldText");
        } else {
            commandNameLabel.setText("None selected");
            commandNameLabel.getStyleClass().remove("boldText");
        }

        updateFixedServerUrlField(currentlySelectedTreeApi());
    }

    public List<CommandParameterModel> createCommandParameterModels(MethodInfo methodInfo) {
        List<CommandParameterModel> parameterModels = new ArrayList<CommandParameterModel>();

        Method method = methodInfo.method;
        String methodName = method.getName();

        Class[] parameterTypes = method.getParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();

        for (int i = 0; i < parameterTypes.length; i++) {
            Annotation[] paramAnnotations = annotations[i];
            Class paramClass = parameterTypes[i];

            for (int j = 0; j < paramAnnotations.length; j++)
                if (paramAnnotations[j].annotationType().equals(ApiMethodParameter.class)) {
                    ApiMethodParameter amp = (ApiMethodParameter) paramAnnotations[j];

                    CommandParameterModel commandParameterModel = new CommandParameterModel(amp.name(), paramClass, amp, methodInfo);

                    parameterModels.add(commandParameterModel);
                }
        }

        return parameterModels;
    }


    private GeniConnectionProvider connectionProvider = new GeniConnectionPool();
    @FXML private void call() {
        MethodInfo calledMethod = currentlySelectedTreeMethod;
        if (calledMethod == null) return;
        AbstractApi calledApi = calledMethod.api;

        System.out.println("Call method "+calledMethod.name+" with parameters:");
        for (CommandParameterModel param : argumentModels) {
            if (param.isRequired() || param.includedProperty().get())
                System.out.println("  - "+param.getParameterName()+" -> \""+param.valueProperty().get()+"\"");
            else
                System.out.println("  - Not included: "+param.getParameterName());
        }

        try {
            boolean debugMode = false; /*TODO*/
            GeniConnection con = null;
            connectionProvider.setDebugMode(debugMode);

            assert calledApi != null;
            GeniUser geniUser = null;
            if (easyModel.getGeniUserProvider().isUserLoggedIn())
                geniUser = easyModel.getGeniUserProvider().getLoggedInGeniUser();

            if (useUserRadioButton.isSelected()) {
                assert con == null;
                con = connectionProvider.getConnectionByUserAuthority(geniUser, calledApi.getServerType());
            }
            if (userAuthorityRadioButton.isSelected()) {
                assert con == null;
                con = connectionProvider.getConnectionByAuthority(geniUser, authChooser.getSelectedAuthority().getGeniAuthority(), calledApi.getClass());
            }
            if (userCustomServerRadioButton.isSelected()) {
                assert con == null;
                System.out.println("SECURITY WARNING: making connection which accepts all certificates");
                ClientSslAuthenticationXmlRpcTransportFactory.HandleUntrustedCallback handleUntrustedCallback;
                if (ignoreSelfSignedCheckBox.isSelected())
                    handleUntrustedCallback = new ClientSslAuthenticationXmlRpcTransportFactory.INSECURE_TRUSTALL_HandleUntrustedCallback();
                else {
                    handleUntrustedCallback = new ClientSslAuthenticationXmlRpcTransportFactory.HandleUntrustedCallback() {
                        @Override
                        public boolean trust(SSLCertificateDownloader.SSLCertificateJFedInfo sslCertificateJFedInfo) {
                            if (sslCertificateJFedInfo.isTrusted()) return true;

                            String problemDescription = "";
                            if (sslCertificateJFedInfo.isSelfSigned()) {
                                problemDescription += "The server's certificate is self signed. Certificate info:\n";
                                for (X509Certificate cert : sslCertificateJFedInfo.getChain()) {
                                    problemDescription += ""+cert.toString();
                                    problemDescription += "\n\n";
                                }
                            }
                            if (!sslCertificateJFedInfo.getSubjectMatchesHostname()) {
                                if (!problemDescription.equals(""))
                                    problemDescription += "\n\nADDITIONAL SECURITY PROBLEM:\n";
                                problemDescription += "The certificate's subject hostname does not match the server URL:\n";
                                problemDescription += "    Certificate Subject: "+sslCertificateJFedInfo.getSubject()+"\n";
                                problemDescription += "    Server Hostname: "+sslCertificateJFedInfo.getHostname()+"\n";
                            }
                            return JavaFXDialogUtil.show2ChoiceDialog(problemDescription,
                                    "I know what I am doing, I checked the certificate manually, and I trust the server",
                                    "I do not trust this",
                                    callButton);
                        }
                    };
                }
                con = connectionProvider.getConnectionByUrl(geniUser, new URL(customServerUrlField.getText()), handleUntrustedCallback);
            }

            if (con == null) {
                throw new RuntimeException("Authority to use for needed ServerType is unknown, or URL for needed ServerType is unknown: serverType="+calledApi.getServerType());
            }

            execute(calledMethod, con, geniUser, debugMode);
        } catch (Exception e) {
            throw new RuntimeException("Call failed: "+e.getMessage(), e);
        }
    }

    public void execute(MethodInfo methodInfo, final GeniConnection connection, final GeniUser geniUser, boolean debugMode) throws GeniException {
        final Method method = methodInfo.method;
        final AbstractApi targetObject = methodInfo.api;

        int chooserIndex = 0;

        final Class[] parameterTypes = method.getParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();

        final Object[] parameters = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Annotation[] paramAnnotations = annotations[i];
            Class paramClass = parameterTypes[i];

            if (GeniConnection.class.isAssignableFrom(paramClass))
                parameters[i] = connection;
            else {
                if (paramClass.equals(GeniUser.class)) { //TODO is this still useful? What is this for?
                    assert geniUser != null : "parameter required user, but no user is provided";
                    parameters[i] = geniUser;
                }
                else
                    for (int j = 0; j < paramAnnotations.length; j++)
                        if (paramAnnotations[j].annotationType().equals(ApiMethodParameter.class)) {
                            ApiMethodParameter amp = (ApiMethodParameter) paramAnnotations[j];
                            CommandParameterModel commandParameterModel = argumentModels.get(chooserIndex++);
                            Object c = null;
                            if (commandParameterModel.isRequired() || commandParameterModel.includedProperty().get()) {
                                c = commandParameterModel.valueProperty().get();
                                assert c != null : "c==null when isRequired="+commandParameterModel.isRequired()+" included="+commandParameterModel.includedProperty().get();

                                //The GUI elements sometimes return "higher level" objects, that the low level API does not know.
                                //We extract the lower level Objects that the API understands from them here
                                //Also, some classes can be converted to and from string, so here we make sure the correct one is used

                                //ResourceId needs to be converted to String in some cases
                                if (ResourceId.class.isInstance(c) && paramClass.equals(String.class))
                                    c = ((ResourceId) c).getValue();
                                //String needs to be converted to other classes in some cases
                                if (String.class.isInstance(c) && paramClass.equals(ResourceId.class))
                                    c = ResourceIdParser.parse((String) c);
                                if (String.class.isInstance(c) && paramClass.equals(ResourceUrn.class))
                                    c = new ResourceUrn((String) c);

                                //CredentialInfo needs to be converted to GeniCredential
                                if (CredentialInfo.class.isInstance(c) && paramClass.equals(GeniCredential.class))
                                    c = ((CredentialInfo) c).getCredential();

                                //list of CredentialInfo needs to be converted to list of GeniCredential
                                if (amp.name().equals("credentialList") || amp.parameterType().equals(ApiMethodParameterType.LIST_OF_CREDENTIAL)) {
                                    assert c != null;
                                    assert List.class.isInstance(c) : "Argument with name \"credentialList\" has type "+c.getClass().getName()+" instead of List. Value: "+c;

                                    List<GeniCredential> credentials = new ArrayList<GeniCredential>();
                                    List l = (List) c;

                                    for (Object o : l) {
                                        if (CredentialInfo.class.isInstance(o)) {
                                            CredentialInfo ci = (CredentialInfo) o;
                                            credentials.add(ci.getCredential());
                                        } else {
                                            if (GeniCredential.class.isInstance(o)) {
                                                GeniCredential cred = (GeniCredential) o;
                                                credentials.add(cred);
                                            } else {
                                                throw new RuntimeException("In list of credentials, expected a credential class (GeniCredential or CredentialInfo) but got incompatible "+o.getClass()+".");
                                            }
                                        }
                                    }

                                    c = credentials;
                                }
                            }

                            if (c != null && !paramClass.isInstance(c))
                                System.err.println("WARNING: (for argument "+amp.name()+") expected class "+paramClass+" but got incompatible "+c.getClass()+". This will cause an error later on.");

                            parameters[i] = c;
                        } else {
                            //ignored parameter
                            parameters[i] = null;
                        }
            }
            if (debugMode)
                System.out.println("DEBUG CommandPanel.execute parameters["+i+"]="+parameters[i]);
        }

        targetObject.setDebugMode(debugMode);

        TaskThread.Task call = new TaskThread.Task("Probe "+methodInfo.name+" Call") {
            @Override
            public void doTask(TaskThread.SingleTask singleTask) throws GeniException, InterruptedException {
                try {
                    //Note: add and remove listener is NOT the same as adding the logger. The difference is that an added
                    // logger will see calls to other API's on other threads as well, since it is added to the global
                    // logger instead of a logger specific for this call

                    Logger globalLogger = targetObject.getLogger();
                    assert globalLogger instanceof JavaFXLogger;
                    Logger myLogger = globalLogger.getWrappingLogger(singleTask);
                    assert myLogger instanceof JavaFXLogger;

//               don't do this:     targetObject.getLogger().addResultListener(singleTask);
                    targetObject.setLogger(myLogger);

                    Object methodRes = method.invoke(targetObject, parameters);

                    targetObject.setLogger(globalLogger);

//               don't do this:     targetObject.getLogger().removeResultListener(singleTask);


                    //cleanup: remove singleTask from myLogger
                    myLogger.removeResultListener(singleTask);

                    System.out.println("Calling probe task completed");

                    ApiCallReply methodResRep = (ApiCallReply) methodRes;
                    //invoke hides InterruptedException
//                } catch (InterruptedException e) {
//                    throw e;
//                }
                } catch (Exception e) { //TODO better exception handling?
                    if (e instanceof InterruptedException)
                        throw (InterruptedException) e;

                    System.out.println("Exception calling probe task: "+e); e.printStackTrace();
                    if (e.getCause() != null && e.getCause() instanceof GeniException) {
                        //                System.err.println("Note: GeniException in invoked call => unwrapping GeniException and throwing it. see CommandPanel.execute(...)");
                        throw (GeniException) e.getCause();
                    }
                    throw new GeniException("Exception invoking API call", e);
                }
            }
        };
        TaskThread.getInstance().addTask(call);
    }

    private static Stage probeStage = null;
    public static Stage showProbe(EasyModel easyModel) {
        return showProbe(easyModel, null);
    }
    public static Stage showProbe(EasyModel easyModel, Stage stage) {
        try {
            if (probeStage == null) {

                URL location = ProbeController.class.getResource("Probe.fxml");
                assert location != null;
                FXMLLoader fxmlLoader = new FXMLLoader(location, null);

                BorderPane root = (BorderPane)fxmlLoader.load();
                ProbeController controller = (ProbeController)fxmlLoader.getController();

                controller.setEasyModel(easyModel);

                Scene scene = new Scene(root);

                if (stage != null)
                    probeStage = stage;
                else
                    probeStage = new Stage();
                probeStage.setScene(scene);
            }
            if (stage != null)
                assert probeStage == stage;
            assert probeStage != null;
            probeStage.show();
            return probeStage;
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong showing the Probe: "+e.getMessage(), e);
        }
    }
}
