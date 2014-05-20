package be.iminds.ilabt.jfed.ui.unittest_gui;

import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.api.test.TestClassList;
import be.iminds.ilabt.jfed.lowlevel.authority.JFedAuthorityList;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityListModel;
import be.iminds.ilabt.jfed.lowlevel.userloginmodel.UserLoginModelManager;
import be.iminds.ilabt.jfed.testing.base.ApiTestResult;
import be.iminds.ilabt.jfed.util.CommandExecutionContext;
import be.iminds.ilabt.jfed.testing.base.RunTests;
import be.iminds.ilabt.jfed.ui.authority_choice_gui.AuthorityChoicePanel;
import be.iminds.ilabt.jfed.ui.log_gui.ResultPanel;
import be.iminds.ilabt.jfed.ui.userlogin_gui.UserLoginPanel;
import be.iminds.ilabt.jfed.util.TextUtil;
import thinlet.FrameLauncher;
import thinlet.Thinlet;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

/**
 * UnitTestPanel
 */
public class UnitTestPanel extends Thinlet implements UserLoginPanel.UserLoginListener {
    public static void main(String [] args) {
        Logger logger = new Logger();
//        AuthorityListModel authorityListModel = new AuthorityListModel();
//        BuiltinAuthorityList.load(authorityListModel);
        AuthorityListModel authorityListModel = JFedAuthorityList.getAuthorityListModel();


        final UserLoginPanel configPanel = new UserLoginPanel(authorityListModel, logger);

        final UnitTestPanel unitTestPanel = new UnitTestPanel(logger, configPanel, authorityListModel);

        configPanel.addCloseHandler(new UserLoginPanel.ConfigPanelCloseHandler() {
            public void closeConfig() {
                unitTestPanel.showFrame();
            }
        });

        configPanel.showConfigFrame();
    }

    private static Image iconError = initIcon("forbidden15x15.png");
    private static Image iconPass = initIcon("ok15x15.png");
    private static Image iconSkip = initIcon("skip15x15.png");
    private static Image initIcon(String name) {
        BufferedImage icon = null;
        try {
            icon = ImageIO.read(RunTests.class.getResource(name));
            return icon;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load \""+name+"\" icon: "+e.getMessage(), e);
        }
    }


    public String getTestDescription(String className) {
       //static String getTestDescription()
        try {
            Class c = Class.forName(className);
            Method m = c.getMethod("getTestDescription");
            Object res = m.invoke(c.newInstance());
            if (res instanceof String)
                return (String) res;

        } catch (ClassNotFoundException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        }

        return null;
    }


    public static List<String> allUnitTestClasses() {
        List<String> res = TestClassList.getInstance().allTestClasses();
        return res;
    }


//    private UnitTestPanelManager manager;
    private Logger logger;
    private Thinlet thinlet;

    private UserLoginPanel configPanel;

    private Object resultList;
    private Object userAuthLabel;

    private AuthorityChoicePanel authorityChoicePanel;
    private ResultPanel logPan;

//    public interface UnitTestPanelManager {
//        public void closeUnitTestPanel();
//    }

    public UnitTestPanel(/*UnitTestPanelManager manager,*/Logger logger, UserLoginPanel configPanel, AuthorityListModel authorityListModel) {
//        this.manager = manager;
        this.logger = logger;
        this.thinlet = this;
        this.configPanel = configPanel;

        this.authorityChoicePanel = new AuthorityChoicePanel(thinlet, authorityListModel, logger, configPanel);
        logPan = new ResultPanel(new ResultPanel.ResultPanelCloseHandler() {
            @Override
            public void closeResultPanel() {
                //ignore
            }
        }, null, logger, null);

        try {
            Object panel = thinlet.parse(this.getClass().getResourceAsStream("UnitTestPanel.xml"), this);
            add(panel);

            resultList = thinlet.find(panel, "resultList");
            assert(resultList != null);
            userAuthLabel = thinlet.find(panel, "userAuthLabel");
            assert(userAuthLabel != null);

            configPanel.addUserLoginConfigPanelListener(this);
        } catch (IOException e) {
            throw new RuntimeException("UnitTestPanel failed to initialise: "+e.getMessage(), e);
        }
    }

    @Override
    public void onUserLoggedIn(GeniUser geniUser) {
        updateUserAuthLabel();
    }

    public void updateUserAuthLabel() {
        SfaAuthority userAuth = configPanel.getUserLoginModel().getUserAuthority();
        thinlet.setString(userAuthLabel, "text", userAuth.getName());
    }

    private FrameLauncher unitTestFl = null;
    public void showFrame() {
        updateUserAuthLabel();

        if (unitTestFl == null) {
            unitTestFl = new FrameLauncher("jFed Automated Compliance Verification Tests", this, 800, 600) {
                public void	windowClosed(WindowEvent e) { }
                public void	windowClosing(WindowEvent e) {
                    /*manager.closeUnitTestPanel();*/
                    /*unitTestFl.setVisible(false);*/
                    System.exit(0);
                }
            };
        } else
            unitTestFl.setVisible(true);
    }

    public void clearLog(Object resultList) {
//        System.out.println("Removing items from log: "+logItems.size());
        lastItem = null;
        for (Object logItem : logItems)
            thinlet.remove(logItem);
        logItems.clear();
    }

    private Object lastItem = null;
    private List<Object> logItems = new ArrayList<Object>();
    private void replaceLastItem(final String text, final Image icon, final int level) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (lastItem != null) {
                    thinlet.remove(lastItem);
                    logItems.remove(lastItem);
                }
                lastItem = null;
                addItemNow(text, icon, level);
            }
        });
    }
    private void addToLastItem(final String text, final Image icon, final int level) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                String newText = text;
                if (lastItem != null) {
                    newText = thinlet.getString(lastItem, "text").trim() + newText;
                    thinlet.remove(lastItem);
                    logItems.remove(lastItem);
                }
                lastItem = null;
                addItemNow(newText, icon, level);
            }
        });
    }
    private void addItem(final String text, final Image icon, final int level) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                addItemNow(text, icon, level);
            }
        });
    }
    private void addItemNow(final String text, final Image icon, final int level) {
                Object item = thinlet.create("item");
        String newText = text;
        for (int i = 0; i < level; i++) {
            newText = "     "+newText;
        }
        newText = TextUtil.wrap(newText, 100);
        if (level >= 2) {
            thinlet.setString(item, "text", newText);
            thinlet.setChoice(item, "alignment", "right");
        }
        else
            thinlet.setString(item, "text", newText);
        if (icon != null)
            thinlet.setIcon(item, "icon", icon);
        thinlet.add(resultList, item);
        logItems.add(item);
        lastItem = item;
    }

    private class MyTestListener implements RunTests.TestListener {

        @Override
        public void onStart(String testname, int testNr, int testCount) {
            addItem("Testing " + testname + "... ", null, 1);
        }

        @Override
        public void onResult(ApiTestResult.ApiTestMethodResult result, int testNr, int testCount) {
            Image icon = null;
            switch (result.getState()) {
                case SUCCESS: { icon = iconPass; break; }
                case FAILED: { icon = iconError; break; }
                case WARN: { icon = iconError; break; }
                case SKIPPED: { icon = iconSkip; break; }
                default: { icon = iconError; break; }
            }
            addToLastItem(result.getState().name(), icon, 1);
        }
        @Override public void onAllTestDone(ApiTestResult result, int testCount) { }
    }

    private Thread unitTestThread = null;
    public void runUnitTestsInThread(final Object runButton, final String group) {
        if (unitTestThread != null) {
            assert !unitTestThread.isAlive() : "Unit tests are still running, so button should not have been pressed.";
            boolean interrupted = true;
            while (interrupted) {
                interrupted = false;
                try {
                    unitTestThread.join();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            unitTestThread = null;
        }
        final List<String> selectedClasses = new ArrayList<String>();
        for (Map.Entry<Object, String> entry : unitTestCheckboxes.entrySet()) {
            Object checkbox = entry.getKey();
            String className = entry.getValue();

            if (thinlet.getBoolean(checkbox, "selected"))
                selectedClasses.add(className);
        }
        thinlet.setBoolean(runButton, "enabled", false);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                runUnitTests(selectedClasses, group);

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        thinlet.setBoolean(runButton, "enabled", true);
                    }
                });
            }
        };
        unitTestThread = new Thread(r);
        unitTestThread.start();
    }

    public void runUnitTests(List<String> selectedClasses, String group) {
        CommandExecutionContext testContext =  new CommandExecutionContext(
                                getConfigPanel().getLoggedInGeniUser(),
                                getUserLoginPreferences().getUserAuthority(),
                                getTestedAuthority(),
                                getLogger());

        for (String selectedClass : selectedClasses) {
            Class testClass = null;
            try {
                testClass = Class.forName(selectedClass);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Bug in Test GUI: could not find test class "+selectedClass, e);
            }
            if (group.equals("") || group.equals("null")) group = null;
            ApiTestResult result = RunTests.runTest(testContext, new Properties(), testClass, group, new MyTestListener(), false); //TODO add group support to the unit test panel
            String outputfilename = "result-selectedClass.html";
            result.toHtml(new File(outputfilename));

            thinlet.repaint(resultList);
        }
    }

    public SfaAuthority getTestedAuthority() {
        return authorityChoicePanel.getAuthority();
    }

    public UserLoginModelManager getUserLoginPreferences() {
        return configPanel.getUserLoginModel();
    }

    public UserLoginPanel getConfigPanel() {
        return configPanel;
    }

    public Logger getLogger() {
        return logger;
    }



    public void initAuthChoicePanel(Object thinletAuthChoicePanel) {
        add(thinletAuthChoicePanel, authorityChoicePanel.getThinletPanel());
    }

    private Map<Object, String> unitTestCheckboxes;
    public void initUnitTestChoicePanel(Object unitTestChoicePanel) {
        unitTestCheckboxes = new HashMap<Object, String>();
        for (String className : allUnitTestClasses()) {
            Object checkbox = thinlet.create("checkbox");
            String desc = getTestDescription(className);
//            if (desc == null) desc = ""; else desc = ": "+desc;
//            thinlet.setString(checkbox, "text", className.replaceAll(".*\\.", "") + desc);
            thinlet.setString(checkbox, "text", className.replaceAll(".*\\.", ""));
            thinlet.setString(checkbox, "tooltip", desc);

//            thinlet.setBoolean(checkbox, "selected", true);
            thinlet.setBoolean(checkbox, "selected", false);
            thinlet.add(unitTestChoicePanel, checkbox);
            unitTestCheckboxes.put(checkbox, className);
        }
    }

    public void showLogs() {
        logPan.showLogFrame();
    }
}
