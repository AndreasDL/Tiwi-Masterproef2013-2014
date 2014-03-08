package be.iminds.ilabt.jfed.ui.lowlevel_gui;

import be.iminds.ilabt.jfed.util.DialogUtils;
import thinlet.Thinlet;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * HelpIcon: a help icon that can be clicked for more info
 */
public class HelpIcon {
    private Thinlet thinlet;
    private String fullHelp;
    private String helpTarget;

    private Object iconLabel;
    private Object helpDialog;
    private Thinlet helpDialogThinlet;
    private DialogUtils.DialogLauncher helpDialogDialogLauncher;

    public HelpIcon(Thinlet thinlet, Object dialogParent, String helpTarget, String fullHelp, boolean link, boolean useIcon) {
        if (link && useIcon)
            throw new IllegalArgumentException("Illegal option combination: HelpIcon's link and useIcon options cannot both be true.");

        this.thinlet = thinlet;
        this.helpTarget = helpTarget;
        this.fullHelp = fullHelp;

        BufferedImage icon = null;
        try {
//            icon = ImageIO.read(getClass().getResource("help-20x20.png"));
            icon = ImageIO.read(getClass().getResource("help-15x15.png"));
//            icon = ImageIO.read(getClass().getResource("help-10x10.png"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load help icon: "+e.getMessage(), e);
        }
        helpDialogThinlet = new Thinlet();
        try {
            InputStream guiXml = this.getClass().getResourceAsStream("HelpDialog.xml");
            assert(guiXml != null);
            helpDialog = helpDialogThinlet.parse(guiXml, this);
            helpDialogThinlet.add(helpDialog);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse thinlet HelpDialog.xml: "+e.getMessage(), e);
        }
        //thinlet.setBoolean(helpDialog, "visible", false);
//        thinlet.setIcon(helpDialog, "icon", icon);
        Object helpDialogLabel = thinlet.find(helpDialog, "label");
        Object helpDialogFullText = thinlet.find(helpDialog, "fulltext");
        assert helpDialogLabel != null;
        assert helpDialogFullText != null;
        thinlet.setString(helpDialogLabel, "text", "Help about \""+helpTarget+"\":");
        thinlet.setString(helpDialogFullText, "text", fullHelp);

        int w = thinlet.getInteger(helpDialogFullText, "width");
        int h = thinlet.getInteger(helpDialogFullText, "height");

        int lines = 0;
        for (int i = 0; i < fullHelp.length(); i++) {
            char c = fullHelp.charAt(i);
            if (c == '\n') lines++;
        }

        helpDialogDialogLauncher = new DialogUtils.DialogLauncher(
                null/*frame owner*/,
                false/*modal*/,
                ""+helpTarget+" Help"/*title*/,
                helpDialogThinlet,
                800/*width*/,
                50+(lines*30)/*height*/,
                icon);
        helpDialogDialogLauncher.setVisible(false);

        if (!link) {
            iconLabel = thinlet.create("button");

    //        Toolkit toolkit = Toolkit.getDefaultToolkit();
    //        MediaTracker tracker = new MediaTracker(thinlet);
    //        Image icon = toolkit.getImage("help.png");
    //        tracker.addImage(icon, 0);
    //        try {
    //            tracker.waitForAll();
    //        } catch (InterruptedException e) {
    //           //ignore
    //        }

            thinlet.setInteger(iconLabel, "weightx", 0);
            thinlet.setInteger(iconLabel, "weighty", 0);
            thinlet.setChoice(iconLabel, "halign", "left"); //Possible values are: fill, center, left, and right.
            thinlet.setChoice(iconLabel, "valign", "top");  //Possible values are: fill, center, top, and bottom.

            if (useIcon) {
                thinlet.setIcon(iconLabel, "icon", icon);
                thinlet.setString(iconLabel, "text", "");
            }
            else
                thinlet.setString(iconLabel, "text", "?");
            thinlet.setChoice(iconLabel, "type", "link");
        } else {
            iconLabel = thinlet.create("button");
            thinlet.setString(iconLabel, "text", helpTarget);
            thinlet.setChoice(iconLabel, "type", "link");
        }
        thinlet.setString(iconLabel, "tooltip", "Click for help about \""+helpTarget+"\"");
        thinlet.setMethod(iconLabel, "action", "onClickedHelp", thinlet, this);
    }

    public void onClickedHelp() {
        //thinlet.setBoolean(helpDialog, "visible", true);
        helpDialogDialogLauncher.setVisible(true);
    }

    public Object getThinletComponent() {
        return iconLabel;
    }


}
