package be.iminds.ilabt.jfed.ui.lowlevel_gui.chooser;

import thinlet.Thinlet;

/**
 * StringChooser
 */
public class BooleanChooser extends AbstractChooser {
    public BooleanChooser(Thinlet thinlet, boolean required, String text, boolean defaultBool, boolean ifOptionalAddDefault) {
        super(thinlet, required, text, "<checkbox name=\"choice\" text=\""+"\" selected=\""+defaultBool+"\"/>", ifOptionalAddDefault);
    }

    @Override
    public Object getChoiceValue() {
        return thinlet.getBoolean(choice, "selected");
    }
}
