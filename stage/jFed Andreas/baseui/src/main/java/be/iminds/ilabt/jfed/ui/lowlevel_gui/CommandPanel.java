package be.iminds.ilabt.jfed.ui.lowlevel_gui;

import be.iminds.ilabt.jfed.history.CredentialAndUrnHistory;
import be.iminds.ilabt.jfed.lowlevel.ApiMethod;
import be.iminds.ilabt.jfed.lowlevel.ApiMethodParameter;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityProvider;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceUrn;
import be.iminds.ilabt.jfed.ui.exception_gui.ExceptionHandlerDialog;
import be.iminds.ilabt.jfed.ui.lowlevel_gui.chooser.*;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceId;
import thinlet.Thinlet;

import java.awt.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * CommandPanel Is a panel that gathers parameters for a single command and offers a button to execute the command
 */
public class CommandPanel<T extends AbstractApi> {
    private Thinlet thinlet;
    private CredentialAndUrnHistory history;

    private ApiPanel apiPan;

    private T targetObject;
    private Class targetClass;
    private Method method;
    private ApiMethod methodAnnotation;

    private GeniConnectionProvider connectionProvider;
    private GeniUserProvider contextProvider;
    private AuthorityProvider authorityProvider;

    private Object rootPanel;
    private Object argsPanel;

    private List<Chooser> choosers;
    private List<HelpIcon> argumentHelpIcons;
    private List<String> argumentNames;

    private boolean forceNonUserSA;

    private static boolean useHelpLink = true;

    public CommandPanel(Thinlet thinlet, ApiPanel apiPan, T targetObject, Class targetClass, Method method,
                        GeniConnectionProvider connectionProvider, GeniUserProvider contextProvider, AuthorityProvider authorityProvider,
                        CredentialAndUrnHistory history, boolean forceNonUserSA) {
        this.targetObject = targetObject;
        this.targetClass = targetClass;
        this.method = method;
        this.forceNonUserSA = forceNonUserSA;


        this.connectionProvider = connectionProvider;
        this.contextProvider = contextProvider;
        this.authorityProvider = authorityProvider;

        this.apiPan = apiPan;
        this.thinlet = thinlet;
        this.history = history;

        if (method.isAnnotationPresent(ApiMethod.class))
            this.methodAnnotation = method.getAnnotation(ApiMethod.class);
        else
            throw new RuntimeException("Method "+method.getName()+" is missing @ApiMethod annotation");

        try {
            this.rootPanel = thinlet.parse("CommandPanel.xml", this);
            this.argsPanel = thinlet.find(rootPanel, "commandpan");
            assert rootPanel != null;
            assert argsPanel != null;
        } catch (IOException e) {
            throw new RuntimeException("CommandPanel failed to initialise: "+e.getMessage(), e);
        }

        //messy but it works for now (argumentHelpIcons should be part of createChoosers, probably each chooser should integrate with the help icon)
        this.argumentHelpIcons = new ArrayList<HelpIcon>();
        this.argumentNames = new ArrayList<String>();
        this.choosers = createChoosers();

        if (choosers.size() > 0) {
            int argsPanelColumns = thinlet.getInteger(argsPanel, "columns");

            Object argumentsTitleLabel = thinlet.create("label");
            thinlet.setString(argumentsTitleLabel, "text", "Arguments:");
            thinlet.setInteger(argumentsTitleLabel, "weightx", 0);
            thinlet.setInteger(argumentsTitleLabel, "weighty", 0);
            thinlet.setInteger(argumentsTitleLabel, "colspan", argsPanelColumns);
            thinlet.add(argsPanel, argumentsTitleLabel);

            Object argumentListPanel = thinlet.create("panel");
            thinlet.setInteger(argumentListPanel, "colspan", argsPanelColumns);
            thinlet.setInteger(argumentListPanel, "weightx", 1);
            thinlet.setInteger(argumentListPanel, "weighty", 1);
            thinlet.setInteger(argumentListPanel, "columns", 3);
            thinlet.setInteger(argumentListPanel, "gap", 4);
            thinlet.setInteger(argumentListPanel, "top", 4);
            thinlet.setInteger(argumentListPanel, "left", 4);

            int i = 0;
            for (Chooser chooser : choosers) {
                Object chooserPan = chooser.getThinletPanel();
                String argumentName = argumentNames.get(i);
                HelpIcon argumentHelpIcon = argumentHelpIcons.get(i);

    //            thinlet.setInteger(chooserPan, "colspan", 2);
                thinlet.setInteger(chooserPan, "colspan", 1);
                thinlet.setInteger(chooserPan, "weightx", 1);

                Object namePan = thinlet.create("panel");
                thinlet.setInteger(namePan, "colspan", 1);
                thinlet.setInteger(namePan, "weightx", 0);
                thinlet.setInteger(namePan, "weighty", 0);
                thinlet.setInteger(namePan, "columns", 3);

                Object marker = thinlet.create("label");
                thinlet.setColor(marker, "background", Color.BLACK);
                thinlet.setColor(marker, "foreground", Color.BLACK);
                thinlet.setString(marker, "text", "i");
                thinlet.setInteger(marker, "width", 10);
                thinlet.setInteger(marker, "weighty", 1);

                Object nameLabel = thinlet.create("label");
                thinlet.setString(nameLabel, "text", argumentName);
                Object nameLabel2 = thinlet.create("label");
                thinlet.setString(nameLabel2, "text", ":");

                if (!useHelpLink || argumentHelpIcon == null)
                    thinlet.add(namePan, nameLabel);
                if (argumentHelpIcon != null)
                    thinlet.add(namePan, argumentHelpIcon.getThinletComponent());
                thinlet.add(namePan, nameLabel2);

                thinlet.add(argumentListPanel, marker);
                thinlet.add(argumentListPanel, namePan);
                thinlet.add(argumentListPanel, chooserPan);

    //            thinlet.add(argsPanel, chooserPan);

                i++;
            }

            thinlet.add(argsPanel, argumentListPanel);

        }
    }

    public void initCommandLabel(Object comLab) {
        String api = targetClass.getSimpleName();
        thinlet.setString(comLab, "text", ""+api+" method "+(!useHelpLink?method.getName()+": ":""));
    }

    public void initHint(Object hint) {
        if (methodAnnotation.hint().equals("")) {
            thinlet.setBoolean(hint, "visible", false);
        }
        else {
            thinlet.setBoolean(hint, "visible", true);
            HelpIcon icon = new HelpIcon(thinlet, this.rootPanel, method.getName(), methodAnnotation.hint(), useHelpLink/*link*/, !useHelpLink/*icon*/);
            thinlet.add(hint, icon.getThinletComponent());
//            thinlet.setString(hint, "text", methodAnnotation.hint());
        }
    }

    public void call(boolean closeAfterCall) {
        try {
            boolean debugMode = apiPan.isDebugMode();
            GeniConnection con;
            connectionProvider.setDebugMode(debugMode);
            if (forceNonUserSA)
                con = connectionProvider.getConnectionByAuthority(contextProvider.getLoggedInGeniUser(), authorityProvider.getAuthority(), targetObject.getServerType());
            else
                con = connectionProvider.getConnectionByAuthority(contextProvider.getLoggedInGeniUser(), authorityProvider.getAuthority(), targetClass); //TODO probably bugged now, but thinlet GUI will be removed anyway
            ApiCallReply reply = execute(con, contextProvider.getLoggedInGeniUser(), debugMode);
            if (reply != null && closeAfterCall) {
                cancel();
            }
        } catch (Exception e) {
            ExceptionHandlerDialog.handleException(e);
        }
    }

    public void cancel() {
        apiPan.cancelMethodPanel();
    }

    public ApiCallReply execute(final GeniConnection connection, final GeniUser geniUser, boolean debugMode) throws GeniException {
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

                if (paramClass.equals(GeniUser.class))
                    parameters[i] = geniUser;
                else
                for (int j = 0; j < paramAnnotations.length; j++)
                    if (paramAnnotations[j].annotationType().equals(ApiMethodParameter.class)) {
                        ApiMethodParameter amp = (ApiMethodParameter) paramAnnotations[j];
                        Object c = choosers.get(chooserIndex++).getChoice();

                        //ResourceId needs to be converted to String in some cases
                        if (ResourceId.class.isInstance(c) && paramClass.equals(String.class))
                            c = ((ResourceId) c).getValue();

                        if (c != null && !paramClass.isInstance(c))
                            System.err.println("WARNING: expected class "+paramClass+" but got incompatible "+c.getClass()+". This will cause an error later on.");

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

        ApiCallReply methodResRep = null;
        try {
            Object methodRes = method.invoke(targetObject, parameters);
            methodResRep = (ApiCallReply) methodRes;
        } catch (Exception e) {
            if (e.getCause() != null && e.getCause() instanceof GeniException) {
//                System.err.println("Note: GeniException in invoked call => unwrapping GeniException and throwing it. see CommandPanel.execute(...)");
                throw (GeniException) e.getCause();
            }
            throw new GeniException("Exception invoking API call", e);
        }

        return methodResRep;
    }
    public List<Chooser> createChoosers() {
        List<Chooser> choosers = new ArrayList<Chooser>();

        String methodName = method.getName();

        Class[] parameterTypes = method.getParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();

        for (int i = 0; i < parameterTypes.length; i++) {
            Annotation[] paramAnnotations = annotations[i];
            Class paramClass = parameterTypes[i];

            for (int j = 0; j < paramAnnotations.length; j++)
                if (paramAnnotations[j].annotationType().equals(ApiMethodParameter.class)) {
                    ApiMethodParameter amp = (ApiMethodParameter) paramAnnotations[j];

                    Chooser chooser = null;

                    if (amp.name().equals("credentialList"))
                        chooser = new CredentialListChooser(thinlet, history, amp.required(), "Credential List");
                    else
                    if (amp.name().equals("urns"))
                        chooser = new UrnListChooser(thinlet, history, amp.required(), "URNs");
                    else
                    if (amp.name().equals("userCredential"))
                        chooser = new UserCredentialChooser(thinlet, history, amp.required(), "User Credential");
                    else
                    if (amp.name().equals("credential"))
                        chooser = new AnyCredentialChooser(thinlet, history, amp.required(), "Credential");
                    else
                    if (amp.name().equals("sliceCredential"))
                        chooser = new SliceCredentialChooser(thinlet, history, amp.required(), "Slice Credential");
                    else
                    if (amp.name().equals("clearingHouseCredential"))
                        chooser = new AnyCredentialChooser(thinlet, history, amp.required(), "ClearingHouse Credential");
                    else
                    if (amp.name().equals("UserSpecList") || amp.name().equals("users"))
                        chooser = new UserSpecListChooser(thinlet, history, contextProvider.getLoggedInGeniUser(), amp.required(), amp.name());
                    else
                    if (amp.name().equals("user") || amp.name().equals("userUrn"))
                        chooser = new UserUrnChooser(thinlet, history, contextProvider.getLoggedInGeniUser(), amp.required(), "User", amp.guiDefaultOptional());
                    else
                    if (amp.name().equals("slice") || amp.name().equals("sliceUrn"))
                        chooser = new SliceUrnChooser(thinlet, history, amp.required(), "Slice", amp.guiDefaultOptional(), contextProvider);
                    else
                    if (amp.name().equals("rspec"))
                        chooser = new RspecEditorChooser(thinlet, history, amp.required(), "RSpec", contextProvider);
//                        chooser = new RspecChooser(thinlet, history, amp.required(), "RSpec");
                    else
                    if (paramClass.equals(String.class)) {
                        String defaultVal = "";
                        if (!amp.guiDefault().equals("")) defaultVal = amp.guiDefault();
                        chooser = new StringChooser(thinlet, amp.required(), amp.name(), defaultVal, amp.guiDefaultOptional(), amp.multiLineString());
                    }
                    else
                    if (paramClass.equals(Integer.class)) {
                        int defaultVal = 1;
                        if (!amp.guiDefault().equals("")) defaultVal = Integer.parseInt(amp.guiDefault());
                        chooser = new IntegerChooser(thinlet, amp.required(), amp.name(), defaultVal, amp.guiDefaultOptional());
                    }
                    else
                    if (paramClass.equals(Boolean.class)) {
                        boolean defaultVal = false;
                        if (!amp.guiDefault().equals("")) defaultVal = Boolean.parseBoolean(amp.guiDefault());
                        chooser = new BooleanChooser(thinlet, amp.required(), amp.name(), defaultVal, amp.guiDefaultOptional());
                    }
                    else
                    if (paramClass.equals(ResourceId.class))
                        chooser = new ResourceIdChooser(thinlet, history, amp.required(), amp.name(), "");
                    else
                    if (paramClass.equals(ResourceUrn.class))
                        chooser = new UrnChooser(thinlet, history, amp.required(), amp.name(), "");
                    else
                        throw new RuntimeException("ERROR: Implementation incomplete: Unsupported parameter type for \""+amp.name()+"\": "+paramClass.getName());

                    if (amp.hint().equals("")) {
                        argumentHelpIcons.add(null);
                    } else {
                        HelpIcon icon = new HelpIcon(thinlet, this.rootPanel, amp.name(), amp.hint(), useHelpLink/*link*/, !useHelpLink/*icon*/);
                        argumentHelpIcons.add(icon);
                    }

                    argumentNames.add(amp.name());

                    choosers.add(chooser);
                }
        }

        return choosers;
    }

    public T getTargetObject() {
        return targetObject;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public Method getMethod() {
        return method;
    }

    public Object getPanel() {
        return rootPanel;
    }
}
