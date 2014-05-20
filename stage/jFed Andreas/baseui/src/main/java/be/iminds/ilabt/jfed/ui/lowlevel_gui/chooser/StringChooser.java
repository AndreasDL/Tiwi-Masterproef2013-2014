package be.iminds.ilabt.jfed.ui.lowlevel_gui.chooser;

import thinlet.Thinlet;

/**
 * StringChooser
 */
public class StringChooser extends AbstractChooser {
    public StringChooser(Thinlet thinlet, boolean required, String text, String defaultText, boolean ifOptionalAddDefault, boolean multiLine) {
        super(thinlet, required, text, !multiLine ?
                "<textfield name=\"choice\" text=\""+defaultText+"\" columns=\"10\" weightx=\"1\" />" :
                "<textarea name=\"choice\" text=\""+defaultText+"\" columns=\"10\" rows=\"5\" weightx=\"1\" />",
                ifOptionalAddDefault);
    }

    @Override
    public Object getChoiceValue() {
        return thinlet.getString(choice, "text");
    }
}
