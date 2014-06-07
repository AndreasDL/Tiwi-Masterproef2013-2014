package be.iminds.ilabt.jfed.ui.lowlevel_gui.chooser;

import thinlet.Thinlet;

/**
 * StringChooser
 */
public class IntegerChooser  extends AbstractChooser {
    public IntegerChooser(Thinlet thinlet, boolean required, String text, int defaultInt, boolean ifOptionalAddDefau) {
        super(thinlet, required, text, "<textfield name=\"choice\" text=\""+defaultInt+"\" columns=\"10\" weightx=\"1\"/>", ifOptionalAddDefau);
    }

    @Override
    public Object getChoiceValue() {
        return Integer.parseInt(thinlet.getString(choice, "text"));
    }
}
