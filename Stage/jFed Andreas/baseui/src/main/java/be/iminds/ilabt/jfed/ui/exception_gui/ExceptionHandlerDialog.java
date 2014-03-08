package be.iminds.ilabt.jfed.ui.exception_gui;

import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.util.DialogUtils;
import be.iminds.ilabt.jfed.util.IOUtils;
import be.iminds.ilabt.jfed.util.TextUtil;
import thinlet.Thinlet;

import java.io.IOException;
import java.io.InputStream;

/**
 * ErroHandlerDialog
 */
public class ExceptionHandlerDialog extends Thinlet {
    public ExceptionHandlerDialog() {
        try {
            InputStream guiXml = ExceptionHandlerDialog.class.getResourceAsStream("ExceptionPanel.xml");
            Object modaldialog = parse(guiXml, this);
            add(modaldialog);
        } catch (IOException e) {
            throw new RuntimeException("Exception initializing Exception Handler Dialog: "+e.getMessage(), e);
        }

    }

    public void close() {
//        execptionPanFl.setVisible(false);
        dialogLauncher.setVisible(false);
    }

    private String briefText, detailsText, httpSentText, httpRecvText;
    private Exception e;
    private Object briefThinletObject, detailsThinletObject, exMessageThinletObject, stacktraceThinletObject, serverURLThinletObject, httpSentThinletObject, httpRecvThinletObject;

    public void initExceptionPanel(Object brief, Object details, Object exMessage, Object stacktrace, Object serverURL, Object httpSent, Object httpRecv) {
        this.briefThinletObject = brief;
        this.detailsThinletObject = details;
        this.exMessageThinletObject = exMessage;
        this.stacktraceThinletObject = stacktrace;
        this.serverURLThinletObject = serverURL;
        this.httpSentThinletObject = httpSent;
        this.httpRecvThinletObject = httpRecv;
    }
    public void updateExceptionPanel(String briefText, String detailsText, Exception e, String serverUrlText, String httpSentText, String httpRecvText) {
        this.briefText = briefText;
        this.detailsText = detailsText;
        this.e = e;

        //note: thinlet label does not wrap on newline
        setString(briefThinletObject, "text", TextUtil.wrap(briefText, 100));
        setString(detailsThinletObject, "text", detailsText);
        setString(exMessageThinletObject, "text", TextUtil.wrap(e.getMessage(), 100));
        String stacktraceText = e == null ? "(no exception)" : IOUtils.exceptionToStacktraceString(e);
        setString(stacktraceThinletObject, "text", stacktraceText);
        setString(serverURLThinletObject, "text", serverUrlText);
        setString(httpSentThinletObject, "text", httpSentText);
        setString(httpRecvThinletObject, "text", httpRecvText);
    }

    public void printToConsole() {
        if (e != null) {
            System.err.println("Printing exception:\n"+
                    TextUtil.wrap(briefText, 100)+"\n"+
                    TextUtil.wrap(e.getMessage(), 100));
            e.printStackTrace();
        }
    }


//    private FrameLauncher execptionPanFl = null;
    private DialogUtils.DialogLauncher dialogLauncher;
    public void showExceptionPanel() {
        dialogLauncher = new DialogUtils.DialogLauncher(
                        null/*frame owner*/,
                        true/*modal*/,    //checked and confirmed: setVisible(true) will not return until dialog closed or setVisible(false), if modal is set
                        "TODO: title"/*title*/,
                        this,
                        800/*width*/,
                        500/*height*/,
                        null /*icon*/);
        dialogLauncher.setVisible(true);

//        if (execptionPanFl == null)
//            execptionPanFl = new FrameLauncher("Exception", this, 640, 480) {
//                public void	windowClosed(WindowEvent e) {  }
//                public void	windowClosing(WindowEvent e) { close(); }
//            };
//        else
//            execptionPanFl.setVisible(true);

        //TODO block until closed here?
    }

    private static ExceptionHandlerDialog instance = null;

    /**
     * TODO Should this block until user closes dialog?
     * */
    public static void handleException(Exception e) {
        handleException(null, null, e);
    }
    public static  void handleException(final String briefText, final String detailsText, Exception e) {

        String message = null;
        if (briefText != null) message += briefText+"\n";
        if (e != null) message += e.getMessage();
        String details = null;

        //find first cause that is GeniException
        Throwable ex = e;
        while (ex != null && (!(ex instanceof GeniException)) && ex.getCause() != null)
            ex = ex.getCause();

        String serverUrlText = "";
        String httpSentText = "";
        String httpRecvText = "";

        if (ex != null && ex instanceof GeniException) {
            GeniException ge = (GeniException) ex;
            if (ge.getXmlRpcResult() != null) {
                serverUrlText = ge.getXmlRpcResult().getServerUrl();
                httpSentText = ge.getXmlRpcResult().getRequestHttpContent();
                httpRecvText = ge.getXmlRpcResult().getResultHttpContent();
            }
            else {
                details = "No details";
                serverUrlText = "No XmlRpcResult details";
                httpSentText = "No XmlRpcResult details";
                httpRecvText = "No XmlRpcResult details";
            }

            if (ge.getGeniResponseCode() != null) {
                details = message + "\n\n" + details;
                message = (briefText == null ? "" : (briefText+"\n"))  + ge.getGeniResponseCode().getDescription();
            }
        } else {
            if (ex != null)
                details = e.getClass().getName()+":\n"+e.getMessage();
        }

        if (detailsText != null) {
            details = detailsText + "\n\n" + details;
        }

        if (instance == null)
            instance = new ExceptionHandlerDialog();
        instance.updateExceptionPanel(message, details, e, serverUrlText, httpSentText, httpRecvText);
        instance.showExceptionPanel();
    }
}
