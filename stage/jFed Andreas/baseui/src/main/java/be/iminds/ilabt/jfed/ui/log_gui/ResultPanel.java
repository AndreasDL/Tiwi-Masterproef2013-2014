package be.iminds.ilabt.jfed.ui.log_gui;

import be.iminds.ilabt.jfed.history.CredentialAndUrnHistory;
import be.iminds.ilabt.jfed.log.ApiCallDetails;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.log.ResultListener;
import be.iminds.ilabt.jfed.lowlevel.ApiCallReply;
import be.iminds.ilabt.jfed.util.IOUtils;
import be.iminds.ilabt.jfed.util.XmlRpcPrintUtil;
import javafx.application.Platform;
import thinlet.FrameLauncher;
import thinlet.Thinlet;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * ResultPanel
 */
public class ResultPanel implements ResultListener {
    public interface ResultPanelCloseHandler {
        public void closeResultPanel();
    }
    private ResultPanelCloseHandler manager;
    private Logger logger;
    private Object panel;
    private Thinlet thinlet;

    //    private Object header;
    private Object resultChoice;
    private Object resText;
    private Object serverUrlText;
    private Object httpSentText;
    private Object httpReplyText;
    private Object xmlSentText;
    private Object xmlReplyText;
    private Object resultRawText;

    public List<ApiCallDetails> resultList;

    /**
     * @param manager may be null
     * @param history may be null
     * */
    public ResultPanel(ResultPanelCloseHandler manager, Thinlet thinlet, Logger logger, CredentialAndUrnHistory history) {
        this.manager = manager;
        this.logger = logger;

        boolean selfThinlet = thinlet == null;
        if (thinlet == null)
            thinlet = new Thinlet(); //our own thinlet

        this.thinlet = thinlet;
        this.resultList = new ArrayList<ApiCallDetails>();
        logger.addResultListener(this);

        try {
            this.panel = thinlet.parse(this.getClass().getResourceAsStream("ResultPanel.xml"), this);
            if (selfThinlet)
                thinlet.add(panel);
            resText = thinlet.find(panel, "resultText");
            serverUrlText = thinlet.find(panel, "serverUrl");
            httpSentText = thinlet.find(panel, "httpSent");
            httpReplyText = thinlet.find(panel, "httpReply");
            xmlSentText = thinlet.find(panel, "xmlSent");
            xmlReplyText = thinlet.find(panel, "xmlReply");
            resultRawText = thinlet.find(panel, "resultRawText");
//            header = thinlet.find(panel, "header");
            resultChoice = thinlet.find(panel, "resultChoice");
            assert(resText != null);
            assert(serverUrlText != null);
            assert(httpSentText != null);
            assert(httpReplyText != null);
            assert(xmlSentText != null);
            assert(xmlReplyText != null);
            assert(resultRawText != null);
//            assert(header != null);
        } catch (IOException e) {
            throw new RuntimeException("ResultPanel failed to initialise: "+e.getMessage(), e);
        }

        if (history != null) {
            //add all previous results to the list
            for (ApiCallDetails result : history.getResultList())
                onResult(result);
        }
    }

    public void saveAllResultToFile() {
        int selectedIndex = thinlet.getSelectedIndex(resultChoice);
        if (selectedIndex >= 0) {
            final JFileChooser fc = new javax.swing.JFileChooser();
            int returnVal = fc.showSaveDialog(null);
            if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                ApiCallDetails result = resultList.get(selectedIndex);
                String resultText = "";
                resultText += "Server URL: "+ result.getServerUrl() + "\n";
                resultText += "Api: "+ result.getApiName() + "\n";
                resultText += "Api Method: "+ result.getGeniMethodName() + "\n";
                resultText += "java Method: "+ result.getJavaMethodName() + "\n";
                if (result.getReply() != null)
                    resultText += "Result as Hashtable: "+ XmlRpcPrintUtil.printXmlRpcResultObject(result.getReply().getValue()) + "\n";
                resultText += "Result XmlRpc Request: "+ XmlRpcPrintUtil.printXmlRpcResultObject(result.getXmlRpcRequest()) + "\n\n";
                resultText += "Result XmlRpc Reply: "+ XmlRpcPrintUtil.printXmlRpcResultObject(result.getXmlRpcReply()) + "\n\n";
                resultText += "Result HTTP Request: "+ result.getHttpRequest() + "\n\n";
                resultText += "Result HTTP Reply: "+ result.getHttpReply() + "\n\n";

                IOUtils.stringToFile(file, resultText);
            }
        }
    }

    public void saveResultToFile(Object resultTextField) {
        final JFileChooser fc = new javax.swing.JFileChooser();
        int returnVal = fc.showSaveDialog(null);
        if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String resultText = thinlet.getString(resultTextField, "text");
            IOUtils.stringToFile(file, resultText);
        }
    }

    private void showResult(ApiCallDetails result) {
        int selectedIndex = thinlet.getSelectedIndex(resultChoice);
        if (selectedIndex == -1) {
            String shownName = result.getApiName()+" - "+result.getGeniMethodName()+" ("+result.getJavaMethodName()+")";
            thinlet.setString(resultChoice, "text", shownName+" :");
        }

        ApiCallReply reply = result.getReply();
        if (reply == null) {
            thinlet.setString(resText, "text", "Error, reply is null");
            thinlet.setString(resultRawText, "text", "Error, reply is null");
        } else {
            if (reply.getRawResult() instanceof Hashtable) {
                Hashtable rawHashtable = (Hashtable) reply.getRawResult();
                Object rawValue = rawHashtable.get("value");
                thinlet.setString(resultRawText, "text", XmlRpcPrintUtil.printXmlRpcResultObject(rawValue));
            } else
                thinlet.setString(resultRawText, "text", "ERROR: raw result not hashtable:\n\n"+ XmlRpcPrintUtil.printXmlRpcResultObject(reply.getRawResult()));
            if (reply.getGeniResponseCode() == null) {
                thinlet.setString(resText, "text", "Error, no GeniResponseCode!\n\n" + XmlRpcPrintUtil.printXmlRpcResultObject(reply.getValue()));
            } else
            if (reply.getGeniResponseCode().isSuccess()) {
                if (reply.getValue() != null && reply.getValue() instanceof String)
                    thinlet.setString(resText, "text", reply.getValue().toString());
                else
                    thinlet.setString(resText, "text", XmlRpcPrintUtil.printXmlRpcResultObject(reply.getValue()));
                // thinlet.setColor(resText, "foreground", Color.BLACK);
            } else {
                thinlet.setString(resText, "text", "ERROR: "+reply.getGeniResponseCode()+"\n"+reply.getOutput());
                //thinlet.setColor(resText, "foreground", Color.RED);
            }
        }

        thinlet.setString(serverUrlText, "text", result.getServerUrl());
        thinlet.setString(httpSentText, "text", result.getHttpRequest());
        thinlet.setString(httpReplyText, "text", result.getHttpReply());
        thinlet.setString(xmlSentText, "text", XmlRpcPrintUtil.printXmlRpcResultObject(result.getXmlRpcRequest()));
        thinlet.setString(xmlReplyText, "text", XmlRpcPrintUtil.printXmlRpcResultObject(result.getXmlRpcReply()));
    }

    public void resultSelected(Object resultChoice) {
        int selectedIndex = thinlet.getSelectedIndex(resultChoice);
        if (selectedIndex >= 0) {
            ApiCallDetails result = resultList.get(selectedIndex);
            showResult(result);
        }
    }

    @Override
    public void onResult(final ApiCallDetails result) {
        //this may run asynchronously

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                resultList.add(result);
                int newIndex = thinlet.getCount(resultChoice);
                Object resultItem = thinlet.create("choice");
                String shownName = result.getApiName()+" - "+result.getGeniMethodName()+" ("+result.getJavaMethodName()+")";
                thinlet.setString(resultItem, "text", shownName);
                thinlet.add(resultChoice, resultItem);
                thinlet.setString(resultChoice, "text", shownName);
                thinlet.setInteger(resultChoice, "selected", newIndex);

                showResult(result);
            }
        });
    }

    public Object getPanel() {
        return panel;
    }

    public Thinlet getThinlet() {
        return thinlet;
    }


    private FrameLauncher logFl = null;
    public void showLogFrame() {
        if (logFl == null) {
            logFl = new FrameLauncher("jFed Logs", thinlet, 700, 600) {
                public void	windowClosed(WindowEvent e) { }
                public void	windowClosing(WindowEvent e) { logFl.setVisible(false); }
            };
        }
        else
            logFl.setVisible(true);
    }
}
