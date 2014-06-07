package be.iminds.ilabt.jfed.ui.lowlevel_gui.chooser;

/**
 * Chooser
 */
public interface Chooser {
    /**
     * @return a thinlet panel allowing the selection
     */
    public Object getThinletPanel();

    /**
     * @return chosen object or null if not selected (only for non-required parameters)
     */
    public Object getChoice();
}
