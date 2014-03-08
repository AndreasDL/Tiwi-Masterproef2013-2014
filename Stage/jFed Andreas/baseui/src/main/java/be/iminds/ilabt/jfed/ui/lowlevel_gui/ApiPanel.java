package be.iminds.ilabt.jfed.ui.lowlevel_gui;

import be.iminds.ilabt.jfed.history.CredentialAndUrnHistory;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.ApiMethod;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityProvider;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityListModel;
import be.iminds.ilabt.jfed.ui.authority_choice_gui.AuthorityChoicePanel;
import be.iminds.ilabt.jfed.ui.userlogin_gui.UserLoginPanel;
import thinlet.Thinlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * ApiPanel
 */
public class ApiPanel<T extends AbstractApi> implements UserLoginPanel.UserLoginListener {
    private Thinlet thinlet;
    private CredentialAndUrnHistory history;
    private T targetObject;
    private Class targetClass;

    private GeniConnectionProvider connectionProvider;
    private UserLoginPanel userLoginConfigPanel;

    private Object tab;
    private Object tabPan;
    private Object methodsPan;
    private Object debugModeCheckBox;
    private Map<String, Method> availableMethods;
    private Map<String, Object> availableMethodButtons;

    private AuthorityListModel authorityListModel; //null if fixed to user authority
    private AuthorityChoicePanel authorityChoicePanel; //null if fixed to user auth


    private Logger logger;

    /** debug mode will print out a info during the call */
    public boolean isDebugMode() {
        if (debugModeCheckBox == null) return false;
        return thinlet.getBoolean(debugModeCheckBox, "selected");
    }


    private static Map<String, Method> findAvailableMethods(Class targetClass) {
            Map<String, Method> res = new HashMap<String, Method>();

            for (Method m : targetClass.getDeclaredMethods()) {//GeniConnection con,
                if (m.isAnnotationPresent(ApiMethod.class)) {
                    ApiMethod am = m.getAnnotation(ApiMethod.class);
                    Class[] parameterTypes = m.getParameterTypes();
                    if (parameterTypes.length == 0 || !GeniConnection.class.isAssignableFrom(parameterTypes[0]))
                        throw new RuntimeException("Method "+m+" does not have GeniConnection as first parameter but it does have @ApiMethod");
                    res.put(m.getName(), m);
                }
            }

            return res;
        }

    private Object activeSliceAuthorityLabel = null;
    private Object activeSliceAuthorityOverrideCheckBox = null;
    private Object activeSliceAuthorityUrlLabel = null;

    /**
     *
     * @param thinlet
     * @param history
     * @param api
     * @param connectionProvider
     * @param userLoginConfigPanel
     * @param authorityListModel The authority model containing all possible authorities.
     *                           Set this to {@code null} if there should be no choice of authority.
     *                           No AuthorityChoicePanel will then be shown, and the user's authority (retrieved from
     *                           the context) will be used for connections.
     * @throws IOException
     */
    public ApiPanel(Object tabbedPane, Thinlet thinlet, CredentialAndUrnHistory history, T api,
                    GeniConnectionProvider connectionProvider, UserLoginPanel userLoginConfigPanel,
                    AuthorityListModel authorityListModel, Logger logger) throws IOException {
        this.logger = logger;

        this.thinlet = thinlet;
        this.history = history;
        this.targetObject = api;
        this.targetClass = api.getClass();
        availableMethods = findAvailableMethods(targetClass);

        this.connectionProvider = connectionProvider;
        this.userLoginConfigPanel = userLoginConfigPanel;
        this.authorityListModel = authorityListModel;

        userLoginConfigPanel.addUserLoginConfigPanelListener(this);

        /*
         * Bug: For some reason, in some versions of thinlet, you can only add add a panel to a tab when that tab is
         *      added to a tabbedpane.
         * Bug workaround: This is why we have tabbedPane as argument and create the tab here instead of in ApiPanel.xml
         **/
        tabPan = thinlet.parse("ApiPanel.xml", this);

        debugModeCheckBox = thinlet.find(tabPan, "debugMode");
//        assert debugModeCheckBox != null;

        tab = thinlet.create("tab");
        thinlet.add(tabbedPane, tab);
        thinlet.add(tab, tabPan);
//        thinlet.setString(tab, "text", targetClass.getSimpleName());
        String fullname = targetClass.getSimpleName();
//        try {
//            Method getApiName = targetClass.getMethod("getApiName");
//            Object getApiNameRes = getApiName.invoke(null);
//            fullname = (String) getApiNameRes;
//        } catch (Exception e) {
//            //ignore, just use getSimpleName
//        }
        thinlet.setString(tab, "text", fullname);

        methodsPan = thinlet.find(tabPan, "methods");

        boolean isSA = api.getServerType().getRole().equals(ServerType.GeniServerRole.PROTOGENI_SA) || api.getServerType().getRole().equals(ServerType.GeniServerRole.PlanetLabSliceRegistry);
        boolean isCH = api.getServerType().getRole().equals(ServerType.GeniServerRole.PROTOGENI_CH);

        if (authorityListModel != null && !isSA && !isCH) {
            assert authorityListModel != null;
            this.authorityChoicePanel = new AuthorityChoicePanel(thinlet, authorityListModel, logger, userLoginConfigPanel);
            Object apiAuthChoicePanel = thinlet.find(tab, "apiAuthChoicePanel");
            assert apiAuthChoicePanel != null;
            thinlet.add(apiAuthChoicePanel, authorityChoicePanel.getThinletPanel());
        }

        if (isSA) {
            Object apiAuthChoicePanel = thinlet.find(tab, "apiAuthChoicePanel");
            assert apiAuthChoicePanel != null;

            activeSliceAuthorityLabel = thinlet.create("label");
            thinlet.add(apiAuthChoicePanel, activeSliceAuthorityLabel);
            activeSliceAuthorityUrlLabel = thinlet.create("label");
            thinlet.add(apiAuthChoicePanel, activeSliceAuthorityUrlLabel);

            activeSliceAuthorityOverrideCheckBox = thinlet.create("checkbox");
            thinlet.setString(activeSliceAuthorityOverrideCheckBox, "text", "Contact a different Slice Authority");
            thinlet.setBoolean(activeSliceAuthorityOverrideCheckBox, "selected", false);
            thinlet.setMethod(activeSliceAuthorityOverrideCheckBox, "action", "updateActiveSliceAuthority", tab, this);
            thinlet.add(apiAuthChoicePanel, activeSliceAuthorityOverrideCheckBox);

            assert authorityListModel != null;
            this.authorityChoicePanel = new AuthorityChoicePanel(thinlet, authorityListModel, logger, userLoginConfigPanel);
            thinlet.add(apiAuthChoicePanel, authorityChoicePanel.getThinletPanel());

            updateActiveSliceAuthority();
        }

        if (isCH) {
            Object apiAuthChoicePanel = thinlet.find(tab, "apiAuthChoicePanel");
            assert apiAuthChoicePanel != null;
        }

        availableMethodButtons = new HashMap<String, Object>();
        for (String availableMethod : availableMethods.keySet()) {
            Object but = thinlet.create("button");
            thinlet.setString(but, "name", availableMethod);
            thinlet.setString(but, "text", availableMethod);
            thinlet.setInteger(but, "weightx", 1);
            thinlet.setMethod(but, "action", "showMethodPanel('"+availableMethod+"')", tab, this);
            availableMethodButtons.put(availableMethod, but);
            thinlet.add(methodsPan, but);
        }
    }

    public void updateActiveSliceAuthority() {
        if (activeSliceAuthorityLabel == null)
            return;

        if (userLoginConfigPanel.isReady() && userLoginConfigPanel.getLoggedInGeniUser() != null && userLoginConfigPanel.getLoggedInGeniUser().getUserAuthority() != null) {
            thinlet.setString(activeSliceAuthorityLabel, "text", "Current user Slice Authority: "+ userLoginConfigPanel.getLoggedInGeniUser().getUserAuthority().getHrn());
            thinlet.setString(activeSliceAuthorityUrlLabel, "text", " URL: "+ userLoginConfigPanel.getLoggedInGeniUser().getUserAuthority().getUrl(targetObject.getServerType()));
            thinlet.setBoolean(activeSliceAuthorityOverrideCheckBox, "visible", true);
            if (thinlet.getBoolean(activeSliceAuthorityOverrideCheckBox, "selected")) {
                thinlet.setBoolean(authorityChoicePanel.getThinletPanel(), "visible", true);
                authorityChoicePanel.setAll("Contact Slice Authority: "/*labelText*/, true/*showLabel*/, true/*selectable*/, true/*editable*/, true/*addable*/);
            } else {
                thinlet.setBoolean(authorityChoicePanel.getThinletPanel(), "visible", false);
            }
        } else {
            thinlet.setString(activeSliceAuthorityLabel, "text", "No active user.");
            thinlet.setString(activeSliceAuthorityUrlLabel, "text", "");
//            authorityChoicePanel.setAll("Used Slice Authority"/*labelText*/, false/*showLabel*/, false/*selectable*/, false/*editable*/, false/*addable*/);
            thinlet.setBoolean(activeSliceAuthorityOverrideCheckBox, "visible", false);
            thinlet.setBoolean(authorityChoicePanel.getThinletPanel(), "visible", false);
        }
    }

    public void initLabel(Object apiLabel) {
        String fullname = targetClass.getSimpleName();
        try {
            Method getApiName = targetClass.getMethod("getApiName");
            Object getApiNameRes = getApiName.invoke(null);
            fullname = (String) getApiNameRes;
        } catch (Exception e) {
            //ignore, just use getSimpleName
        }
        thinlet.setString(apiLabel, "text", fullname);
    }

    private Object activePanel = null;

    public void cancelMethodPanel() {
        if (activePanel != null)
            thinlet.remove(activePanel);

        thinlet.setBoolean(methodsPan, "visible", true);
    }
    public void showMethodPanel(String methodName) {
        Method method = availableMethods.get(methodName);

        boolean isUserValid = userLoginConfigPanel.isReady() && userLoginConfigPanel.getLoggedInGeniUser() != null && userLoginConfigPanel.getLoggedInGeniUser().getUserAuthority() != null;

        AuthorityProvider authProvider;
        boolean forceNonUserSA = false;
        if (activeSliceAuthorityLabel != null && authorityListModel != null && authorityChoicePanel != null) {
            assert activeSliceAuthorityOverrideCheckBox != null;
            assert authorityChoicePanel != null;
            if (thinlet.getBoolean(activeSliceAuthorityOverrideCheckBox, "selected")) {
                System.out.println("WARNING: contacting Slice authority that is (possibly) not user authority: "+authorityChoicePanel.getAuthority());
                authProvider = authorityChoicePanel;
                forceNonUserSA = true;
            }
            else {
                if (!isUserValid) return;
                final GeniUser currentUser = userLoginConfigPanel.getLoggedInGeniUser();
                authProvider = new AuthorityProvider() {
                    @Override
                    public SfaAuthority getAuthority() {
                        return currentUser.getUserAuthority();
                    }
                }; //provides the user's authority
            }
        } else {
            if (authorityListModel != null && authorityChoicePanel != null)
                authProvider = authorityChoicePanel;
            else {
                if (!isUserValid) return;
                final GeniUser currentUser = userLoginConfigPanel.getLoggedInGeniUser();
                authProvider = new AuthorityProvider() {
                    @Override
                    public SfaAuthority getAuthority() {
                        return currentUser.getUserAuthority();
                    }
                }; //provides the user's authority
            }
        }

        CommandPanel<T> comPan = new CommandPanel<T>(thinlet, this, targetObject, targetClass, method,
                                                     connectionProvider, userLoginConfigPanel, authProvider,
                                                     this.getHistory(), forceNonUserSA);

        if (activePanel != null)
            thinlet.remove(activePanel);

        thinlet.setBoolean(methodsPan, "visible", false);
        activePanel = comPan.getPanel();
        thinlet.add(tabPan, activePanel);
    }

    public CredentialAndUrnHistory getHistory() {
        return history;
    }

    public Object getTab() {
        return tab;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public T getTargetObject() {
        return targetObject;
    }

    @Override
    public void onUserLoggedIn(/*UserLoginPanel userLoginConfigPanel*/GeniUser user) {
        updateActiveSliceAuthority();
    }
}
