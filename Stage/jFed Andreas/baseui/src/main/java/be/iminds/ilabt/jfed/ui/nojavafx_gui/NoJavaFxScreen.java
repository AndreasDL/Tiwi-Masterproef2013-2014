package be.iminds.ilabt.jfed.ui.nojavafx_gui;

import be.iminds.ilabt.jfed.ui.lowlevel_gui.ManualCommandPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * NoJavaFxScreen: alterntive start for when no JavaFX is present
 */
public class NoJavaFxScreen extends JApplet {
    JLabel label;
    JButton button;
    JTextArea textField;

//    public NoJavaFxScreen() {
//    }

    private static String text = "It appears that JavaFX is missing.\n" +
            "The RSpecEditor in this GUI requires JavaFX. You need to have at least JavaFX 2.2 to run it.\n" +
            "Installing Sun/Oracle Java7 will automatically install JavaFX.\n" +
            "\n" +
            "You can continue by clicking the button below, but the RSpecEditor will crash when started.";

    //Called when this applet is loaded into the browser.
    public void init() {
        //Execute a job on the event-dispatching thread; creating this applet's GUI.
        try {
            //removed invokeAndWait because: Exception in thread "AWT-EventQueue-0" java.lang.Error: Cannot call invokeAndWait from the event dispatcher thread
//            SwingUtilities.invokeAndWait(new Runnable() {
//                public void run() {
                    setLayout(new BorderLayout());
                    label = new JLabel("No JavaFX found. RSpecEditor will not work");
                    add(label, BorderLayout.NORTH);
                    button = new JButton("Continue without javaFX");
                    add(button, BorderLayout.SOUTH);
                    button.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            continueWithout();
                        }
                    });
                    textField = new JTextArea(text);
                    //TODO disable editing if possible?
                    add(textField, BorderLayout.CENTER);
//                }
//            });
        } catch (Exception e) {
            System.err.println("NoJavaFxScreen GUI init didn't complete successfully");
        }
    }

    public void continueWithout() {
        //TODO hide this applet?

        try {
            String [] args = {};
            ManualCommandPanel.main(args);
        } catch (Exception e) {
            System.err.println("Error in ManualCommandPanel, will exit: "+e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
