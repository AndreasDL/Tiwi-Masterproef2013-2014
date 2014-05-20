package be.iminds.ilabt.jfed.ui.lowlevel_gui.chooser;

import thinlet.Thinlet;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * StringChooser
 */
public abstract class AbstractChooser implements Chooser {
    protected Object pan;
    protected Object choice;
    protected Thinlet thinlet;
    protected boolean required;
    private Object reqCheck;

    public AbstractChooser(Thinlet thinlet, boolean required, String text, String choiceXml, boolean ifOptionalAddDefault) {
        this.thinlet = thinlet;
        this.required = required;

        //text display is disabled
        text = "";

        String guixml;

        if (text == null || text.equals(""))
            guixml =
                "<panel name=\"chooserpan\" columns=\"2\" gap=\"4\" top=\"4\" left=\"4\"  weightx=\"1\">\n" +
                "        <checkbox name=\"req\" text=\"\" action=\"handleRequired(req,choice)\" init=\"handleRequired(req,choice)\" selected=\""+required+"\"/>\n" +
                "        "+choiceXml+"\n" +
                "</panel>";
        else
            guixml =
                "<panel name=\"chooserpan\" columns=\"3\" gap=\"4\" top=\"4\" left=\"4\"  weightx=\"1\">\n" +
                "        <checkbox name=\"req\" text=\"\" action=\"handleRequired(req,lab,choice)\" init=\"handleRequired(req,lab,choice)\" selected=\""+required+"\"/>\n" +
                "        <label name=\"lab\" text=\""+(text.equals("")?"":text+":")+"\" />\n" +
                "        "+choiceXml+"\n" +
                "</panel>";
        try {
            pan = thinlet.parse(new ByteArrayInputStream(guixml.getBytes()), this);
            choice = thinlet.find(pan, "choice");
            reqCheck = thinlet.find(pan, "req");
            thinlet.setBoolean(reqCheck, "selected", ifOptionalAddDefault);
            if (text == null || text.equals(""))
                handleRequired(reqCheck, choice);
            else
                handleRequired(reqCheck, thinlet.find(pan, "lab"), choice);
            assert(choice != null);
            assert(reqCheck != null);
        } catch (IOException e) {
            throw new RuntimeException("Should never fail, but did for: "+guixml, e);
        }
    }

    public void handleRequired(Object reqCheck, Object lab, Object choice) {
        if (!required) {
            boolean sel = thinlet.getBoolean(reqCheck, "selected");
            thinlet.setBoolean(lab, "enabled", sel);
        }
        handleRequired(reqCheck, choice);
    }

    public void handleRequired(Object reqCheck, Object choice) {
        if (required) {
            thinlet.setBoolean(reqCheck, "visible", false);
        } else {
            thinlet.setBoolean(reqCheck, "visible", true);
            thinlet.setString(reqCheck, "tooltip", "Check this box to add this argument to the options.");
            boolean sel = thinlet.getBoolean(reqCheck, "selected");
            thinlet.setBoolean(choice, "visible", sel);
            if (!sel) {
                thinlet.setString(reqCheck, "text", "(option not added)");
                thinlet.setColor(reqCheck, "foreground", Color.GRAY);
            }
            else {
                thinlet.setColor(reqCheck, "foreground", Color.BLACK);
                thinlet.setString(reqCheck, "text", "Option enabled. Value:");
            }
        }
    }

    @Override
    public Object getThinletPanel() {
        return pan;
    }

    public abstract Object getChoiceValue();

    @Override
    public Object getChoice() {
        if (required || thinlet.getBoolean(reqCheck, "selected"))
            return getChoiceValue();
        else
            return null;
    }
}
