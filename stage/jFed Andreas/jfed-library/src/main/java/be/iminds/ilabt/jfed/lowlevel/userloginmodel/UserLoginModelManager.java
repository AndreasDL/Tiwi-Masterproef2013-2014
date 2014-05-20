package be.iminds.ilabt.jfed.lowlevel.userloginmodel;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.GeniUser;
import be.iminds.ilabt.jfed.lowlevel.GeniUserProvider;
import be.iminds.ilabt.jfed.lowlevel.SimpleGeniUser;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityListModel;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * UserLoginModelManager acts as a UserLoginModel, by refering to a specific UserLoginModel implementation
 *   UserLoginModelManager has 2 "states"
 *     - itself is a UserLoginModel (GeniUser + GeniUserProvider) that is the active, logged in user
 *     - it gives access to a modifiable UserLoginModel, changes to this model can be committed to the active model
 *
 *
 * UserLoginModel is a GeniUser + a GeniUserProvider
 *
 * GeniUser stores all preferences related to the user login:
 *   - SSL key and certificate file
 *
 *   it also stores the derived data that is often needed
 *   - authority to use
 *      - info about authority
 *      (stores urn separately, for cases where authority is not known, but urn is)
 *   - username
 *   - user urn
 *
 * It does not store any passwords (memory or disk), but it does store the unencrypted private key in memory (never on disk *)
 *
 * (* = the password might off course have been read from a file on disk where it already is in unencrypted form.)
 *
 *
 * GeniUserProvider is an interface for accessing the the currently logged in user. It allows for no user to be logged in
 *
 *
 * UserLoginModel is an implementation of GeniUser that is used as the underlying model for a control implementing a user login
 * UserLoginModel does not actually store much itself: it stores the real UserLoginModel implementation, which comes in
 * different versions. It does manage the preferences file, which also specifies which subtype is used.
 *
 * Note that UserLoginModel can be in invalid states, like GeniUserProvider. GeniUser cannot be.
 */
public class UserLoginModelManager implements GeniUser, GeniUserProvider {
    public enum UserLoginModelType {
        KEY_CERT_INTERNAL_INFO, KEY_CERT_EXTERNAL_INFO, PLANETLAB
    }

//    private static String KEY_CERT_INTERNAL_INFO = "KeyCertPemFileWithInternalInfo";
//    private static String KEY_CERT_EXTERNAL_INFO = "KeyCertPemFileWithExternalInfo";
//    private static String PLANETLAB = "Planetlab";

    private GeniUser loggedInUser;

    @Override
    public GeniUser getLoggedInGeniUser() {
        if (isUserLoggedIn())
            return this; //or   loggedInUser     difference: this updates automatically,loggedInUser is fixed forever
        else
            return null;
    }

    @Override
    public boolean isUserLoggedIn() {
        return loggedInUser != null;
    }

    @Override
    public PrivateKey getPrivateKey() {
        if (loggedInUser == null) return null;
        return loggedInUser.getPrivateKey();
    }

    @Override
    public X509Certificate getCertificate() {
        if (loggedInUser == null) return null;
        return loggedInUser.getCertificate();
    }

    @Override
    public SfaAuthority getUserAuthority() {
        if (loggedInUser == null) return null;
        return loggedInUser.getUserAuthority();
    }

    @Override
    public String getUserUrn() {
        if (loggedInUser == null) return null;
        return loggedInUser.getUserUrn();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //hack to be able to see if preferences changed.
    //   not using a real Preferences implementation, only the very small subset that is used is in here
    class UserLoginModelPreferences {

        public UserLoginModelPreferences() {
        }
        public void save(Preferences preferences) {
            try {
                preferences.clear();

                for (Map.Entry<String, Object> e : map.entrySet()) {
                    if (e.getValue() == null) continue;
                    if (e.getValue() instanceof Boolean) {
                        Boolean v = (Boolean) e.getValue();
                        preferences.putBoolean(e.getKey(), v);
                    } else {
                        preferences.put(e.getKey(), e.getValue().toString());
                    }
                }

                preferences.flush();
            } catch (BackingStoreException e) {
                throw new RuntimeException("Problem with internal Login data storage", e);
            }
        }
        public void load(Preferences preferences) {
            try {
                String[] keys = preferences.keys();

                map.clear();

                for (String key : keys) {
                    String val = preferences.get(key, null);
                    if (val != null)
                        if (val.equals("true") || val.equals("false")) {
                            map.put(key, new Boolean(val.equals("true")));
                        } else {
                            map.put(key, val);
                        }
                }

            } catch (BackingStoreException e) {
                throw new RuntimeException("Problem with internal Login data storage", e);
            }
        }

        private final Map<String, Object> map = new HashMap<String, Object>();

        public void put(String key, String value) {
            map.put(key, value);
        }
        public void putBoolean(String key, boolean value) {
            map.put(key, new Boolean(value));
        }
        public String get(String key, String defaultValue) {
            Object r = map.get(key);
            if (r == null) return defaultValue;
            return r.toString();
        }
        public Boolean getBoolean(String key, Boolean defaultValue) {
            Object v = map.get(key);
            if (v != null && v instanceof Boolean)
                return (Boolean) v;
            return defaultValue;
        }
        public void remove(String key) {
            map.remove(key);
        }

        @Override
        public String toString() {
            return "UserLoginModelPreferences" + map + "";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserLoginModelPreferences that = (UserLoginModelPreferences) o;

            if (!map.equals(that.map)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return map.hashCode();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private UserLoginModelType modelType;

    private UserLoginModel model = null;
    private KeyCertUserLoginModel keyCertUserLoginModel = null;
    private KeyCertWithManualInfoUserLoginModel keyCertWithManualInfoUserLoginModel = null;
    private PlanetlabUserLoginModel planetlabUserLoginModel = null;

    private final Preferences preferences;
    private AuthorityListModel authorityListModel;
    private Logger logger;

    public UserLoginModelManager(AuthorityListModel authorityListModel, Logger logger) {
        this.authorityListModel = authorityListModel;
        this.logger = logger;

        Preferences prefsRoot = Preferences.userRoot();
        preferences = prefsRoot.node("be.iminds.ilabt.jfed.ui.userloginmodel.UserLoginModelManager");
        load();
    }

    public void save() {
        System.out.println("UserLoginModelManager.save()    class="+model.getClass().getName()+"  modelType="+modelType);

        UserLoginModelPreferences prefsToSave = new UserLoginModelPreferences();
        prefsToSave.put("type", modelType.toString());
        model.save(prefsToSave);

        prefsToSave.save(preferences);
    }

    public boolean hasChanged() {
        UserLoginModelPreferences savedPrefs = new UserLoginModelPreferences();
        savedPrefs.load(preferences);

        UserLoginModelPreferences activePrefs = new UserLoginModelPreferences();
        activePrefs.put("type", modelType.toString());
        model.save(activePrefs);

        return !savedPrefs.equals(activePrefs);
    }

    public void reset() {
        modelType = UserLoginModelType.KEY_CERT_INTERNAL_INFO;
        model = new KeyCertUserLoginModel(authorityListModel, this);
//        model.reset();
        model.defaults();
    }

    public void load() {
        UserLoginModelPreferences savedPrefs = new UserLoginModelPreferences();
        savedPrefs.load(preferences);

        String typeString = savedPrefs.get("type", null);

        if (typeString != null) {
            modelType = UserLoginModelType.valueOf(typeString);
            if (modelType == null) {
                String allVals = ""; for (UserLoginModelType v : UserLoginModelType.values()) allVals += " " + v.toString();
                System.err.println("Unknown modelType: \""+typeString+"\" valid types:"+allVals);
                reset();
                return;
            }
        }
        else
            modelType = null;

        if (modelType == null) {
            reset();
            return;
        }

        switch (modelType) {
            case KEY_CERT_INTERNAL_INFO: {
                keyCertUserLoginModel = new KeyCertUserLoginModel(authorityListModel, this);
                model = keyCertUserLoginModel;
                break;
            }
            case KEY_CERT_EXTERNAL_INFO: {
                keyCertWithManualInfoUserLoginModel = new KeyCertWithManualInfoUserLoginModel(authorityListModel, this);
                model = keyCertWithManualInfoUserLoginModel;
                break;
            }
            case PLANETLAB: {
                planetlabUserLoginModel = new PlanetlabUserLoginModel(authorityListModel, this, logger);
                model = planetlabUserLoginModel;
                break;
            }
            default: {
                System.err.println("Invalid type stored: resetting stored info.");
                savedPrefs.put("type", null);
                break;
            }
        }
        model.load(savedPrefs);
    }

    public UserLoginModelType getUserLoginModelType() {
        return modelType;
    }

    public void setUserLoginModelType(UserLoginModelType newModelType) {
        this.modelType = newModelType;
        switch (modelType) {
            case KEY_CERT_INTERNAL_INFO: {
                this.model = getKeyCertUserLoginModel();
                break;
            }
            case KEY_CERT_EXTERNAL_INFO: {
                this.model = getKeyCertWithManualInfoUserLoginModel();
                break;
            }
            case PLANETLAB: {
                this.model = getPlanetlabUserLoginModel();
                break;
            }
            default: throw new RuntimeException("No support for modelType "+newModelType);
        }
    }

    public UserLoginModel getCurrentUserLoginModel() {
        return model;
    }



    public KeyCertUserLoginModel getKeyCertUserLoginModel() {
        if (keyCertUserLoginModel == null) {
            keyCertUserLoginModel = new KeyCertUserLoginModel(authorityListModel, this);
            keyCertUserLoginModel.defaults();
        }

        return keyCertUserLoginModel;
    }
    public KeyCertWithManualInfoUserLoginModel getKeyCertWithManualInfoUserLoginModel() {
        if (keyCertWithManualInfoUserLoginModel == null) {
            keyCertWithManualInfoUserLoginModel = new KeyCertWithManualInfoUserLoginModel(authorityListModel, this);
            keyCertWithManualInfoUserLoginModel.defaults();
        }

        return keyCertWithManualInfoUserLoginModel;
    }
    public PlanetlabUserLoginModel getPlanetlabUserLoginModel() {
        if (planetlabUserLoginModel == null) {
            planetlabUserLoginModel = new PlanetlabUserLoginModel(authorityListModel, this, logger);
            planetlabUserLoginModel.defaults();
        }

        return planetlabUserLoginModel;
    }


    public boolean login() {
        if (!model.isUserLoggedIn()) return false;
        loggedInUser = new SimpleGeniUser(model);
//        fireChange();
        return true;
    }
    public void logout() {
        loggedInUser = null;
//        fireChange();
    }

//    public static interface UserLoginPreferencesChangeListener {
//        public void onUserLoginPreferencesChanged(UserLoginModelManager userLoginModel, boolean valid);
//    }
//    private List<UserLoginPreferencesChangeListener> changeListeners = new ArrayList<UserLoginPreferencesChangeListener>();
//    private void fireChange() { //TODO make this private, and instead of calling this, make slice and sliver have a change listener mechanism triggering this?
//        for (UserLoginPreferencesChangeListener l : changeListeners)
//            l.onUserLoginPreferencesChanged(this, isUserLoggedIn());
//    }
//    public void addUserLoginPreferencesChangeListener(UserLoginPreferencesChangeListener l) {
//        changeListeners.add(l);
//    }
//    public void removeUserLoginPreferencesChangeListener(UserLoginPreferencesChangeListener l){
//        changeListeners.remove(l);
//    }


    @Override
    public boolean equals(Object o) {
        throw new RuntimeException("unimplemented");
//        if (this == o) return true;
//
//        //TODO
//
//        return true;
    }

    @Override
    public int hashCode() {
        throw new RuntimeException("unimplemented");
//        //TODO
//        return result;
    }
}
