package be.iminds.ilabt.jfed.ui.javafx.style;

import javafx.scene.Node;
import javafx.scene.Parent;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * StyleHelper
 */
public class StyleHelper {
    /** example: getStyleUrl("main.css"); */
    public static URL getStyleUrl(String styleFile) {
        URL res = null;
        res = StyleHelper.class.getResource(styleFile);
        return res;
    }

    public static String getStyleUrlString(String styleFile) {
        return getStyleUrl(styleFile).toExternalForm();
    }

    /**
     * Add style sheets to a node.
     * The main style sheet (advanced_gui.css) and if not null, the specific style sheet will be added.
     * */
    public static void addStyleSheets(Parent parent, String specific) {
        String mainCssString = StyleHelper.getStyleUrlString("advanced_gui");
        String specificCssString = specific == null ? null : StyleHelper.getStyleUrlString(specific);
        if (specific != null)
            assert specificCssString != null;
        assert mainCssString != null;

        if (specificCssString != null)
            parent.getStylesheets().addAll(mainCssString, specificCssString);
        else
            parent.getStylesheets().addAll(mainCssString, specificCssString);
    }


    /**
     * Helpful method that sets a style while removeing others
     *
     * @param p the node on which to modify the styleClasses
     * @param styleToSet styleClass that needs to be set. may be null
     * @param stylesToUnset all other styles that need to be unset. This may include styleToSet, in which case it will be SET anyway. may be null or emtpty.
     * */
    public static void setStyleClass(Parent p, String styleToSet, List<String> stylesToUnset) {
        List<String> otherStyles = new ArrayList<String>();
        if (stylesToUnset != null)
            otherStyles.addAll(stylesToUnset);

        if (styleToSet != null)
            otherStyles.remove(styleToSet);

        p.getStyleClass().removeAll(otherStyles);
        if (styleToSet != null)
            if (!p.getStyleClass().contains(styleToSet))
                p.getStyleClass().add(styleToSet);
    }
    /**
     * Helpful method that sets a styles while removeing others
     *
     * @param p the node on which to modify the styleClasses
     * @param stylesToSet styleClasses that needs to be set. may be null or empty.
     * @param stylesToUnset all other styles that need to be unset. This may include items from stylesToSet, in which case they will be SET anyway. may be null or emtpty.
     * */
    public static void setStyleClass(Parent p, List<String> stylesToSet, List<String> stylesToUnset) {
        List<String> otherStyles = new ArrayList<String>();
        if (stylesToUnset != null)
            otherStyles.addAll(stylesToUnset);

        if (stylesToSet != null)
            otherStyles.removeAll(stylesToSet);

        p.getStyleClass().removeAll(otherStyles);
        if (stylesToSet != null)
            for (String styleToSet : stylesToSet)
                if (!p.getStyleClass().contains(styleToSet))
                    p.getStyleClass().add(styleToSet);
    }
}
