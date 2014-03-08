package be.iminds.ilabt.jfed.ui.lowlevel_gui.chooser;

import be.iminds.ilabt.jfed.lowlevel.GeniUser;
import be.iminds.ilabt.jfed.lowlevel.api.UserSpec;
import be.iminds.ilabt.jfed.history.CredentialAndUrnHistory;
import be.iminds.ilabt.jfed.util.DialogUtils;
import thinlet.Thinlet;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * StringChooser
 */
public class UserSpecListChooser implements Chooser {
    private Object pan;
    private Object choice;
    private Thinlet thinlet;
    private String labelText;
    public CredentialAndUrnHistory history;
    public boolean required;

    private List<UserSpec> currentUsers;

    public UserSpecListChooser(Thinlet thinlet, CredentialAndUrnHistory history, GeniUser context, boolean required, String labelText) {
        this.thinlet = thinlet;
        assert(thinlet != null);
        this.required = required;
        this.labelText = labelText;
        this.history = history;
        this.currentUsers = new LinkedList<UserSpec>();
        this.currentUsers.add(new UserSpec(context.getUserUrn()));

        try {
            InputStream guiXml = this.getClass().getResourceAsStream("UserSpecListChooser.xml");
            assert(guiXml != null);
            this.pan = thinlet.parse(guiXml, this);
//            this.pan = thinlet.parse("UserSpecListChooser.xml", this); //does not work here for some reason!
            this.choice = thinlet.find(pan, "choice");
            assert(choice != null);
        } catch (IOException e) {
            throw new RuntimeException("Should never fail, but it did anyway", e);
        }
    }

    private String userSpecToListText(UserSpec us) {
        String listText = us.getUrn()+" ("+us.getSshKey().size()+" SSH keys)";
        if (!us.getSshKey().isEmpty()) {
            String firstKey = us.getSshKey().get(0);
            listText += " key="+firstKey.substring(0, 20)+"...";
        }
        return listText;
    }

    public void initCurrentUsers(Object choice) {
        for (UserSpec us : currentUsers) {
            String listText = userSpecToListText(us);

            Object item = thinlet.create("item");
            thinlet.setString(item, "text", userSpecToListText(us));
            thinlet.setString(item, "name", us.getUrn());
            thinlet.add(choice, item);
        }
    }

    public void select(Object choice, Object userurn, Object sshKeyCheck, Object sshKey, Object sshLab) {
        Object item = thinlet.getSelectedItem(choice);
        if (item != null) {
            String urn = thinlet.getString(item, "name");
            UserSpec spec = null;
            for (Iterator<UserSpec> it = currentUsers.iterator(); it.hasNext(); ) {
                UserSpec us = it.next();
                if (us.getUrn().equals(urn))
                    spec = us;
            }
            if (spec == null)
                throw new RuntimeException("Error, thinlet list item not found in currentUsers list: "+urn);
            thinlet.setString(userurn, "text", spec.getUrn());
            if (! spec.getSshKey().isEmpty()) {
                String key = spec.getSshKey().get(0);
                thinlet.setString(userurn, "text", key);
                thinlet.setBoolean(sshKeyCheck, "selected", true);
                handleSshKeyCheck(true, sshLab, sshKey);
            } else {
                thinlet.setBoolean(sshKeyCheck, "selected", false);
                handleSshKeyCheck(true, sshLab, sshKey);
            }

        } else {
            //thinlet.setString(user);
        }
    }

    public void add(Object choice, Object userurn, Object sshKeyCheck, Object sshKey) {
        String urn = thinlet.getString(userurn, "text");
        Vector<String> keyvect = new Vector<String>();
        if (thinlet.getBoolean(sshKeyCheck, "selected")) {
            String key = thinlet.getString(sshKey,  "text");
            keyvect.add(key);
        }
        UserSpec newUserSpec = new UserSpec(urn, keyvect);
        //make sure no 2 user urn's are the same
        for (Iterator<UserSpec> it = currentUsers.iterator(); it.hasNext(); ) {
            UserSpec us = it.next();
            if (us.getUrn().equals(urn)) {
                DialogUtils.errorMessage("User with same urn is already added: "+urn);
                return;
            }
        }
        currentUsers.add(newUserSpec);

        Object item = thinlet.create("item");
        thinlet.setString(item, "text", userSpecToListText(newUserSpec));
        thinlet.setString(item, "name", urn);
        thinlet.add(choice, item);
    }

    public void remove(Object choice, Object userurn, Object sshKeyCheck, Object sshKey) {
        Object[] items = thinlet.getSelectedItems(choice);
        for (Object item : items) {
            String urn = thinlet.getString(item, "name");
            for (Iterator<UserSpec> it = currentUsers.iterator(); it.hasNext(); ) {
                UserSpec us = it.next();
                if (us.getUrn().equals(urn))
                    it.remove();
            }
            thinlet.remove(item);
        }
    }

    public void initRequired(Object reqCheck, Object lab, Object choice, Object userurn, Object sshKeyCheck, Object sshKey, Object add, Object remove, Object sshlab, Object userurnlab) {
        thinlet.setBoolean(reqCheck, "selected", required);
//        thinlet.setString(lab, "text", labelText);
        handleRequired(reqCheck, lab, choice, userurn, sshKeyCheck, sshKey, add, remove, sshlab, userurnlab);
    }
    public void handleRequired(Object reqCheck, Object lab, Object choice, Object userurn, Object sshKeyCheck, Object sshKey, Object add, Object remove, Object sshlab, Object userurnlab) {
        boolean sel = true;
        if (required) {
            thinlet.setBoolean(reqCheck, "visible", false);
            sel = true;
        } else {
            thinlet.setBoolean(reqCheck, "visible", true);
            sel = thinlet.getBoolean(reqCheck, "selected");

            thinlet.setString(reqCheck, "tooltip", "Check this box to add this argument to the options.");
            if (!sel) {
                thinlet.setString(reqCheck, "text", "(option not added)");
                thinlet.setColor(reqCheck, "foreground", Color.GRAY);
            }
            else {
                thinlet.setColor(reqCheck, "foreground", Color.BLACK);
                thinlet.setString(reqCheck, "text", "Option enabled. Value:");
            }
        }

        thinlet.setBoolean(choice, "visible", sel);
        thinlet.setBoolean(userurn, "visible", sel);
        thinlet.setBoolean(sshKeyCheck, "visible", sel);
        thinlet.setBoolean(sshKey, "visible", sel);
        thinlet.setBoolean(add, "visible", sel);
        thinlet.setBoolean(remove, "visible", sel);
        thinlet.setBoolean(sshlab, "visible", sel);
        thinlet.setBoolean(userurnlab, "visible", sel);

        thinlet.setBoolean(lab, "enabled", sel);
        thinlet.setBoolean(choice, "enabled", sel);
        thinlet.setBoolean(userurn, "enabled", sel);
        thinlet.setBoolean(sshKeyCheck, "enabled", sel);
        thinlet.setBoolean(sshKey, "enabled", sel);
    }

    public void handleSshKeyCheck(boolean selected, Object sshLab, Object sshKey) {
        thinlet.setBoolean(sshLab, "enabled", selected);
        thinlet.setBoolean(sshKey, "enabled", selected);
    }

    @Override
    public Object getThinletPanel() {
        return pan;
    }

    @Override
    public Object getChoice() {
       return currentUsers;
    }
}
