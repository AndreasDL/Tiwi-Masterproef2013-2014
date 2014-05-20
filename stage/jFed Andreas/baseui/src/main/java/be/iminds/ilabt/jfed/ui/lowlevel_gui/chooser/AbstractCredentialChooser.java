package be.iminds.ilabt.jfed.ui.lowlevel_gui.chooser;

import be.iminds.ilabt.jfed.lowlevel.GeniCredential;
import be.iminds.ilabt.jfed.history.CredentialAndUrnHistory;
import be.iminds.ilabt.jfed.util.IOUtils;
import thinlet.Thinlet;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * StringChooser
 */
public abstract class AbstractCredentialChooser implements Chooser {
    protected Object pan;
    protected Object cpan;
    protected Object choiceThinletObject;
    protected Thinlet thinlet;
    protected CredentialAndUrnHistory history;
    private String labelText;
    protected List<GeniCredential> defaultCredList;
    protected boolean multiSelect;

    private List<Object> selectionListItems;

    protected String getAddButtonXMl() {
        return "<button name=\"addBut\" text=\"Add from file...\" action=\"addCredential\"/>\n";
    }


    public AbstractCredentialChooser(Thinlet thinlet, CredentialAndUrnHistory history, boolean required, String labelText, List<GeniCredential> defaultCredList) {
        this(thinlet, history, required, labelText, defaultCredList, false);
    }

    /**
     * @param multiSelect if true, allow selection of multiple credentials.
     *                    if false, allow selection of only a single credential
    * */
    public AbstractCredentialChooser(Thinlet thinlet, CredentialAndUrnHistory history, boolean required, String labelText, List<GeniCredential> defaultCredList, boolean multiSelect) {
        this.thinlet = thinlet;
        this.history = history;
        this.labelText = labelText;
        this.defaultCredList = new ArrayList<GeniCredential>(defaultCredList);
        this.multiSelect = multiSelect;
        assert(required);

        init();
    }

    public void init() {
        String guixml = null;

        if (!multiSelect)
            guixml =  "<panel name=\"chooserpan\" columns=\"2\" gap=\"4\" top=\"4\" left=\"4\" weightx=\"1\">\n" +
//                        "        <label text=\""+labelText+":\" colspan=\"2\" />\n" +
                        "        <combobox name=\"choice\" text=\"\" editable=\"false\" weightx=\"1\" colspan=\"1\" />\n" +
                        getAddButtonXMl()+
                        "</panel>";
        else
            guixml =
                "<panel name=\"chooserpan\" columns=\"2\" gap=\"4\" top=\"4\" left=\"4\" weightx=\"1\" weighty=\"1\">\n" +
                "        <label text=\"Select one or multiple Credentials:\" colspan=\"2\" />\n" +
                "        <list selection=\"multiple\" name=\"choice\" weightx=\"1\" weighty=\"1\" colspan=\"2\"/>\n" +
                "        <button name=\"addBut\" text=\"Add from file...\" action=\"addCredential\" colspan=\"2\" weightx=\"0\" halign=\"right\"/>\n"+
                "</panel>";

        if (pan != null) {
            thinlet.remove(cpan);
        } else {
            pan = thinlet.create("panel");
            thinlet.setInteger(pan, "weightx", 1);
        }

        if (multiSelect)
            selectionListItems = new ArrayList<Object>();

        try {
            cpan = thinlet.parse(new ByteArrayInputStream(guixml.getBytes()), this);
            thinlet.add(pan, cpan);

            choiceThinletObject = thinlet.find(pan, "choice");

            int i = 0;
            for (GeniCredential cred : defaultCredList) {
                Object c = null;
                if (multiSelect) {
                    c = thinlet.create("item");
                    selectionListItems.add(c);
                }
                else
                    c = thinlet.create("choice");

                thinlet.setString(c, "text", cred.getName());

                if (multiSelect && i == defaultCredList.size()-1)
                    thinlet.setBoolean(c, "selected", true);

                thinlet.add(choiceThinletObject, c);
                i++;
            }
            if (!multiSelect) {
                if (!defaultCredList.isEmpty()) {
                    GeniCredential cred = defaultCredList.get(0);
                    thinlet.setString(choiceThinletObject, "text", cred.getName());
                    thinlet.setInteger(choiceThinletObject, "selected", 0);
                } else
                    thinlet.setColor(choiceThinletObject, "background", Color.RED);
            }
        } catch (IOException e) {
            throw new RuntimeException("Should never fail, but did for: "+guixml, e);
        }

    }

    public void addCredential() {
        try {
            final JFileChooser fc = new javax.swing.JFileChooser();
            int returnVal = fc.showOpenDialog(null);
            if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                String fileContent = IOUtils.fileToString(file);
                GeniCredential cred = new GeniCredential("Loaded from file \""+file.getPath()+"\"", fileContent);
                addCredential(cred);
                defaultCredList.add(cred);
                init();
            }
        } catch (IOException e) {
            //todo handle this exception cleaner
            throw new RuntimeException("Error adding credential: "+e.getMessage(), e);
        }
    }

    public abstract void addCredential(GeniCredential cred);

    @Override
    public Object getThinletPanel() {
        return pan;
    }

    public List<GeniCredential> getGeniCredentials() {
        assert selectionListItems != null;
        List<GeniCredential> res = new ArrayList<GeniCredential>();
        int i = 0;
        for (GeniCredential cred : defaultCredList) {
            Object selectionListItem = selectionListItems.get(i++);
            boolean selected = thinlet.getBoolean(selectionListItem, "selected");
            if (selected)
                res.add(cred);
        }
        return res;
    }
    public GeniCredential getGeniCredential() {
        String chosen = thinlet.getString(choiceThinletObject, "text");
        int selectedIndex = thinlet.getInteger(choiceThinletObject, "selected");
        if (selectedIndex == -1) {
            return null;
            //            throw new RuntimeException("Invalid credential chosen: "+chosen);
        }
        GeniCredential cred = defaultCredList.get(selectedIndex);
        return cred;
    }

    public abstract Object getChoiceValue(GeniCredential chosenCred);
    public Object getChoiceValue(List<GeniCredential> chosenCreds) {
        if (chosenCreds.size() == 1)
            return getChoiceValue(chosenCreds.get(0));
        throw new UnsupportedOperationException("This Chooser does not support multiple selection.");
    }

    @Override
    public Object getChoice() {
        if (multiSelect)
            return getChoiceValue(getGeniCredentials());
        else
            return getChoiceValue(getGeniCredential());
    }
}
