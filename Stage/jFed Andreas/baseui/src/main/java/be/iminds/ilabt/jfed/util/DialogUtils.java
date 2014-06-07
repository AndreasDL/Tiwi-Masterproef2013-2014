package be.iminds.ilabt.jfed.util;

import thinlet.Thinlet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * DialogUtils
 */
public class DialogUtils {
    static public class DialogLauncher extends Dialog implements WindowListener {
        protected transient Thinlet content;
        private transient Image doublebuffer;

        /**
         * Construct a new Window  with the specified title, including the
         * given <i>thinlet</i> component. The frame is centered on the
         screen, and its
         * preferred size is specified (excluding the frame's borders). The
         icon is
         * the thinlet logo
         *
         * @param title the title to be displayed in the frame's border
         * @param content a <i>thinlet</i> instance
         * @param width the preferred width of the content
         * @param height the preferred height of the content
         */
        public DialogLauncher(Frame owner, boolean modal, String title,
                              Thinlet content, int width, int height, Image icon) {
            super(owner, title, modal);
            this.content = content;
            add(content, BorderLayout.CENTER);
            addWindowListener(this);
            pack();

            Insets is = getInsets();
            width += is.left + is.right;
            height += is.top + is.bottom;
            Dimension ss = getToolkit().getScreenSize();

            //
            // DAL - improve handling of multi-headed displays
            //
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] gs = ge.getScreenDevices();
            if (gs != null && gs.length > 1) {
                GraphicsConfiguration gc = gs[0].getDefaultConfiguration();
                if (gc != null) {
                    Rectangle r = gc.getBounds();
                    ss.setSize(r.getWidth(), r.getHeight());
                }
            }

            width = Math.min(width, ss.width);
            height = Math.min(height, ss.height);
            setBounds((ss.width - width) / 2, (ss.height - height) / 2,
                    width, height);
            // setVisible(true);
            //maximize: setBounds(-is.left, -is.top, ss.width + is.left + is.right, ss.height + is.top + is.bottom);
            if (icon != null)
                setIconImage(icon);
        }

        /**
         * Call the paint method to redraw this component without painting a
         * background rectangle
         */
        public void update(Graphics g) {
            paint(g);
        }

        /**
         * Create a double buffer if needed,
         * the <i>thinlet</i> component paints the content
         */
        public void paint(Graphics g) {
            if (doublebuffer == null) {
                Dimension d = getSize();
                doublebuffer = createImage(d.width, d.height);
            }
            Graphics dg = doublebuffer.getGraphics();
            dg.setClip(g.getClipBounds());
            super.paint(dg);
            dg.dispose();
            g.drawImage(doublebuffer, 0, 0, this);
        }

        /**
         * Clear the double buffer image (because the frame has been resized),
         * the overriden method lays out its components
         * (centers the <i>thinlet</i> component)
         */
        public void doLayout() {
            if (doublebuffer != null) {
                doublebuffer.flush();
                doublebuffer = null;
            }
            super.doLayout();
        }

        /**
         *
         */
        public void windowClosing(WindowEvent e) {
            if (content.destroy()) {
                dispose();
            }
            else {
                setVisible(true);
            }
        }

        public void windowOpened(WindowEvent e) {}
        public void windowClosed(WindowEvent e) {}
        public void windowIconified(WindowEvent e) {}
        public void windowDeiconified(WindowEvent e) {}
        public void windowActivated(WindowEvent e) {}
        public void windowDeactivated(WindowEvent e) {}
    }

    public static char[] getPassword(String question) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));
        JLabel label = new JLabel(question+":");
        JPasswordField pass = new JPasswordField();
        panel.add(label);
        panel.add(pass);
        String[] options = new String[]{"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, panel, question+"",
                JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);
        if(option == 0) // pressing OK button
        {
            char[] password = pass.getPassword();
            return password;
        } else {
            System.exit(0);
            return null;
        }
    }
    public static String getString(String question) {
        return getString(question, "");
    }
    public static String getString(String question, String defaultText) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));
        JLabel label = new JLabel(question+":");
        JTextField text = new JTextField(defaultText);
        panel.add(label);
        panel.add(text);
        String[] options = new String[]{"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, panel, question+"",
                JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);
        if(option == 0) // pressing OK button
        {
            String t = text.getText();
            return t;
        } else {
            return null;
        }
    }

    public static String getMultilineString(String question, int rows, int columns, String defaultText) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));
        JLabel label = new JLabel(question+":");
        JTextArea text = new JTextArea(defaultText, rows, columns);
        panel.add(label);
        panel.add(text);
        String[] options = new String[]{"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, panel, question+"",
                JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);
        if(option == 0) // pressing OK button
        {
            String t = text.getText();
            return t;
        } else {
            return null;
        }
    }

    public static boolean getYesNoAnswer(String question, String yes, String no) {
        Object[] options = {yes, no};
        int option = JOptionPane.showOptionDialog(null,
                question,
                question,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        return (option == 0);
    }

    public static boolean getYesNoAnswer(String question) {
        return getYesNoAnswer(question, "Yes", "No");
    }

    public static void errorMessage(String s) {
        JOptionPane.showMessageDialog(null, s, s, JOptionPane.ERROR_MESSAGE);
    }

    public static void infoMessage(String s) {
        JOptionPane.showMessageDialog(null, s, s, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void infoNonEditabaleTextField(String title, String header, String text) {
        final JTextArea textArea = new JTextArea();
        textArea.setText(text);
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
//        scrollPane.setViewportView(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        final JComponent[] components = new JComponent[] {
        		new JLabel(header),
                scrollPane
        };

        JOptionPane.showMessageDialog(null, components, title, JOptionPane.INFORMATION_MESSAGE);
    }








    public static class ShowModalDialogHelper {
        protected Thinlet thinlet;

        public ShowModalDialogHelper(Thinlet thinlet) {
            this.thinlet = thinlet;
        }

        public void close() {
            Object dialog = thinlet.find("dialog");
            thinlet.remove(dialog);
            synchronized (this) {
                notifyAll();
            }
        }
    }
//    public static void showModalDialog(final Thinlet thinlet, final String pan_xml, ShowModalDialogHelper handler) {
//        Object modaldialog = null;
//        try {
//            if (handler == null) //install default handler
//                handler = new ShowModalDialogHelper(thinlet);
//            modaldialog = thinlet.parse(new ByteArrayInputStream(pan_xml.getBytes()), handler);
//        } catch (Exception exc) { exc.printStackTrace(); return; }
//
//        System.out.println("Showing model dialog");
//
//        thinlet.add(modaldialog);
//        DialogLauncher dialogLauncher = new DialogLauncher(
//                        null/*frame owner*/,
//                        true/*modal*/,
//                        "TODO: title"/*title*/,
//                        thinlet,
//                        800/*width*/,
//                        500/*height*/,
//                        null /*icon*/);
//    }
    //old
//    public static Object showModalDialog(final Thinlet thinlet, final String pan_xml, ShowModalDialogHelper handler) {
//        String dialog_xml = "<dialog name=\"dialog\" text=\"Dialog\" modal=\"true\"\n" +
//                "top=\"4\" left=\"4\" bottom=\"4\" right=\"4\" gap=\"4\" columns=\"1\">\n" +
//                pan_xml+
//                "</dialog>";
//        Object modaldialog = null;
//        try {
//            if (handler == null) //install default handler
//                handler = new ShowModalDialogHelper(thinlet);
//            modaldialog = thinlet.parse(new ByteArrayInputStream(dialog_xml.getBytes()), handler);
//        } catch (Exception exc) { exc.printStackTrace(); return null;}
//
//        thinlet.add(modaldialog);
//
//        try {
//            EventQueue queue = thinlet.getToolkit().getSystemEventQueue();
//            while (thinlet.getParent(modaldialog) != null) {
//                AWTEvent event = queue.getNextEvent();
//                Object src = event.getSource();
//                if (event instanceof ActiveEvent) { // 1.2+
//                    ((ActiveEvent) event).dispatch();
//                } else
//                if (src instanceof Component) {
//                    ((Component) src).dispatchEvent(event);
//                } else if (src instanceof MenuComponent) {
//                    ((MenuComponent) src).dispatchEvent(event);
//                }
//            }
//        } catch(InterruptedException ie) { ie.printStackTrace(); }
//
//        return modaldialog;
//    }

//    public static class ErrorMessageHelper extends ShowModalDialogHelper {
//        private String briefText;
//        private String detailsText;
//        private Exception e;
//        private String httpSentText;
//        private String httpRecvText;
//
//        public ErrorMessageHelper(Thinlet thinlet, String briefText, String detailsText, Exception e) {
//            super(thinlet);
//            this.briefText = briefText;
//            this.detailsText = detailsText;
//            this.e = e;
//            this.httpSentText = "";
//            this.httpRecvText = "";
//
//            //find first cause that is GeniException
//            Throwable ex = e;
//            while (!(ex instanceof GeniException) && ex.getCause() != null) {
//                ex = ex.getCause();
//            }
//            if (ex instanceof GeniException) {
//                GeniException ge = (GeniException) e;
//                if (ge.getXmlRpcResult() != null) {
//                    httpSentText = ge.getXmlRpcResult().getRequestHttpContent();
//                    httpRecvText = ge.getXmlRpcResult().getResultHttpContent();
//                }
//            }
//        }
//
//        public void initExceptionPanel(Object brief, Object details, Object exMessage, Object stacktrace, Object httpSent, Object httpRecv) {
//            //note: thinlet label does not wrap on newline
//            thinlet.setString(brief, "text", StringUtils.wrap(briefText, 100));
//            thinlet.setString(details, "text", detailsText);
//            thinlet.setString(exMessage, "text", StringUtils.wrap(e.getMessage(), 100));
//            String stacktraceText = IOUtils.exceptionToStacktraceString(e);
//            thinlet.setString(stacktrace, "text", stacktraceText);
//            thinlet.setString(httpSent, "text", httpSentText);
//            thinlet.setString(httpRecv, "text", httpRecvText);
//        }
//
//        public void printToConsole() {
//            System.err.println("Printing error:\n"+
//                    StringUtils.wrap(briefText, 100)+"\n"+
//                    StringUtils.wrap(e.getMessage(), 100));
//            e.printStackTrace();
//        }
//    }
//    public static void errorMessage(final Thinlet thinlet, final String briefText, final String detailsText, final Exception e) {
//        System.err.println("DialogUtils.errorMessage Showing error message dialog for "+e);
//        InputStream guiXml = DialogUtils.class.getResourceAsStream("ExceptionPanel.xml");
//        String pan_xml = IOUtils.streamToString(guiXml);
//
//        ErrorMessageHelper handler = new ErrorMessageHelper(thinlet, briefText, detailsText, e);
//
//        showModalDialog(thinlet, pan_xml, handler);
//    }
}
