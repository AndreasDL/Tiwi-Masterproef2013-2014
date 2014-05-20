package be.iminds.ilabt.jfed.ui.rspeceditor.util;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.css.StyleManager;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * JavaFxStyleSheetHelper
 *
 * Something is wrong when using stylesheets in javaFX.
 * The problem occurs when the stylesheet is used in a dependent jar that is packed with one-jar.
 * The one-jar classloader seems to be bypassed, so the css is not found by javaFX
 * The URL given is valid, but somehow, it fails to load.
 *
 * This copies the css to a local dir and use that URL instead.
 *
 * A better solution is to stop using one-jar packaging
 *   NOTE: this is done now (using javafx packager from maven antrun), => making this Deprecated.
 */
@Deprecated
public class JavaFxStyleSheetHelper {
    private JavaFxStyleSheetHelper() {}

//don't use
//    public static String getStyleSheetURL(Class clazz, String resource) {
//        return clazz.getResource(resource).toExternalForm();
//    }
//
//    public static void assureLocalDefaultCss() {
//
//    }

//use workaround
//    private static void streamToFile(InputStream is, File outFile) throws IOException {
//        String res = "";
//        FileWriter fw = new FileWriter(outFile);
//
//        BufferedReader br = new BufferedReader(new InputStreamReader(is));
//        String line = br.readLine();
//        while (line != null) {
//            res += line + "\n";
//            fw.write(line);
//            line = br.readLine();
//        }
//
//        fw.close();
//    }
//
//    private static Map<URL, File> buffer = new HashMap<URL, File>();
//
//    public static String getStyleSheetURL(Class clazz, String resource) {
//        URL cssUrl = clazz.getResource(resource);
//        if (cssUrl == null) throw new RuntimeException("Error, stylesheet \""+resource+"\" for class \""+clazz.getName()+"\" not found!");
//        return getStyleSheetURL(cssUrl, clazz.getName()+"_"+resource);
//    }
//
//    private static String getStyleSheetURL(URL cssUrl, String helpname) {
//        try {
//            File f = buffer.get(cssUrl);
//            if (f != null) return f.toURI().toURL().toExternalForm();
//
//            //get a temp file to copy the CSS file to
//            File temp = File.createTempFile(helpname, ".css");
//
//            streamToFile(cssUrl.openStream(), temp);
//
//            buffer.put(cssUrl, temp);
//
//            return temp.toURI().toURL().toExternalForm();
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            return null;
//        }
//    }
//
//    private static boolean localDefaultCssSet = false;
//    public static void assureLocalDefaultCss() {
//        if (localDefaultCssSet) return;
//        URL caspianUrl = JavaFxStyleSheetHelper.class.getResource("/com/sun/javafx/scene/control/skin/caspian/caspian.css");
//        if (caspianUrl == null) throw new RuntimeException("Error, default stylesheet \"caspian.css\" not found!");
//
//        System.out.println("assureLocalDefaultCss() caspianUrl="+caspianUrl.toExternalForm());
//
//        String cssUrl = getStyleSheetURL(caspianUrl, "caspian");
//
//        StyleManager.getInstance().setDefaultUserAgentStylesheet(cssUrl);
////        StyleManager.getInstance().addUserAgentStylesheet(cssUrl);
//
//        localDefaultCssSet = true;
//    }
}
