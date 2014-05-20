package be.iminds.ilabt.jfed.ui.javafx.log_gui;

import be.iminds.ilabt.jfed.log.ApiCallDetails;
import be.iminds.ilabt.jfed.lowlevel.ApiCallReply;
import be.iminds.ilabt.jfed.ui.javafx.util.TimeUtils;
import be.iminds.ilabt.jfed.ui.rspeceditor.editor.RspecEditorPanel;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.Rspec;
import be.iminds.ilabt.jfed.util.IOUtils;
import be.iminds.ilabt.jfed.util.XmlRpcPrintUtil;
import be.iminds.ilabt.jfed.util.XmlUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Date;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class LogPanel implements Initializable {
    @FXML private TextArea resultText;
    @FXML private TextArea resultErrorText;

    @FXML private TextField authorityNameText;

    @FXML private VBox resultTabContent;
    private boolean showingResultErrorText = true;
    private boolean showingResultText = true;
    @FXML private Parent resultTextGroup;
    @FXML private Parent resultErrorTextGroup;

    @FXML private Label resultCode;
    @FXML private Label resultLabel;
    @FXML private TextArea lowLevelText;
    @FXML private TextField xmlRpcCommand;
    @FXML private TextArea xmlRpcSentText;
    @FXML private TextArea xmlRpcReceivedText;
    @FXML private TextField serverUrlText;
    @FXML private TextArea httpSentText;
    @FXML private TextArea httpReceivedText;

    @FXML private TabPane logTabPane;

    @FXML private Tab resultTab;
    @FXML private Tab lowLevelTab;
    @FXML private Tab XmlRpcTab;
    @FXML private Tab httpTab;
    //extra tabs
    @FXML private Tab rspecTab;
    @FXML private Tab exceptionTab;
    @FXML private Tab xmlTab;
    @FXML private Tab spewLogTab;
    @FXML private Tab htmlErrorTab;

    @FXML private TextArea exceptionTextField;

    @FXML private TextField callStartTimeField;
    @FXML private Label callStartTimeRelativeLabel;
    @FXML private TextField callStopTimeField;
    @FXML private Label callStopTimeRelativeLabel;

    private final ObjectProperty<ApiCallDetails> shownLog = new SimpleObjectProperty<ApiCallDetails>();

    public ObjectProperty<ApiCallDetails> shownLogProperty() {
        return shownLog;
    }
    public void setShownLog(ApiCallDetails details) {
        shownLog.set(details);
    }
    public ApiCallDetails getShownLog() {
        return shownLog.get();
    }


    public LogPanel() {
        shownLog.addListener(shownLogChangeListener);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        rspecShowPanel.editableProperty().set(false);
        logTabPane.getTabs().removeAll(rspecTab, xmlTab, spewLogTab, htmlErrorTab, exceptionTab);
        logTabPane.getSelectionModel().select(resultTab);
    }

    private void showResultText(boolean show) {
        if (showingResultText == show) return;
        showingResultText = show;
        if (!show)
            resultTabContent.getChildren().remove(resultTextGroup);
        else
            resultTabContent.getChildren().add(resultTextGroup);
    }

    private void showResultErrorText(boolean show) {
        if (showingResultErrorText == show) return;
        showingResultErrorText = show;
        if (!show)
            resultTabContent.getChildren().remove(resultErrorTextGroup);
        else
            resultTabContent.getChildren().add(resultErrorTextGroup);
    }

    public boolean isHtml(String text) { //TODO: better test
        String head = text;
        if (text.length() > 100)
            head = text.substring(0,  100);
        return head.contains("<html") || head.contains("<HTML") || head.contains("<!DOCTYPE HTML");
    }

    public boolean isXml(String text) { //TODO: better test
        String head = text;
        if (text.length() > 100)
            head = text.substring(0, 100);
        return head.contains("<?xml") || head.contains("<rspec");
    }

    public boolean isRspec(String text) { //TODO: better test
        String head = text;
        if (text.length() > 100)
            head = text.substring(0,  100);
        return head.contains("<rspec");
    }

    /*
    * protogeni_error_url "spewlogfile" is in the raw reply code, example:
    *
    *    <member><name>value</name><value><i4>0</i4></value></member>
    *    <member><name>output</name><value><string>No slice or aggregate here</string></value></member>
    *    <member><name>code</name><value>
    *      <struct>
    *         <member><name>protogeni_error_log</name><value><string>urn:publicid:IDN+emulab.net+log+48fcfd80d676d2a322dcfa02312d1fee</string></value></member>
    *         <member><name>protogeni_error_url</name><value><string>https://www.emulab.net/spewlogfile.php3?logfile=48fcfd80d676d2a322dcfa02312d1fee</string></value></member>
    *         <member><name>geni_code</name><value><i4>12</i4></value></member>
    *         <member><name>am_code</name><value><i4>12</i4></value></member>
    *         <member><name>am_type</name><value><string>protogeni</string></value></member>
    *      </struct></value>
    *    </member>
    *    </struct></value>
    * */
    public boolean hasSpewLog(ApiCallReply reply) {
        if (reply == null) return false;
        Object rawResult = reply.getRawResult();
        if (rawResult == null) return false;
        if (!(rawResult instanceof Hashtable)) return false;
        Hashtable rawHashtable = (Hashtable) rawResult;
        Object rawCode = rawHashtable.get("code");
        if (rawCode == null) return false;
        if (!(rawCode instanceof Hashtable)) return false;
        Hashtable codeHashTable = (Hashtable) rawCode;
        Object urlObject = codeHashTable.get("protogeni_error_url");
        if (urlObject == null) return false;
        if (!(urlObject instanceof String)) return false;
        return true;
    }

    @FXML private TextArea xmlText;
    private String xmlToFormat;
    public void showXml(String text) {
        this.xmlToFormat = text;
//        logTabPane.getTabs().addFront(xmlTab);
        logTabPane.getTabs().add(xmlTab);
//        xmlTab.setDisable(false);
    }
    @FXML private void showXmlTextLog() {
        if (xmlToFormat != null) {
            String formattedXml = XmlUtil.formatXmlFromString(xmlToFormat);
            xmlText.setText(formattedXml);
            xmlToFormat = null;
        }
    }

    //    @FXML private TextArea rspecText;
    @FXML private RspecEditorPanel rspecShowPanel;
    private String rspecToShow;
    public void showRspec(String rspecText) {
        this.rspecToShow = rspecText;
        logTabPane.getTabs().add(rspecTab);
//        rspecTab.setDisable(false);
    }
    @FXML private void showRspecPanel() {
        if (rspecToShow != null) {
            Rspec rspec = Rspec.fromGeni3RequestRspecXML(rspecToShow);
            rspecShowPanel.shownRspecProperty().set(rspec);
            rspecToShow = null;
        }
    }


    private String shownExceptionText = null;
    public void showException(Throwable exception) {
        if (exception != null) {
            logTabPane.getTabs().add(exceptionTab);
            String exceptionText = exception.getClass()+" "+exception.getMessage()+"\n";

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            pw.close();
            final String stacktrace = sw.getBuffer().toString();

            exceptionText += stacktrace+"\n";
            shownExceptionText = exceptionText;

            assert exceptionTextField != null;
            exceptionTextField.setText(exceptionText);
        }
    }
    @FXML private void printException() {
        if (shownExceptionText != null)
            System.err.println(shownExceptionText);
    }



    @FXML private WebView htmlWebView;
    private ApiCallDetails htmlApiCallDetails;
    public void showHtml(ApiCallDetails apiCallDetails) {
        this.htmlApiCallDetails = apiCallDetails;
        logTabPane.getTabs().add(htmlErrorTab);
//        htmlErrorTab.setDisable(false);
    }
    @FXML private void showHtmlErrorLog() {
        if (htmlApiCallDetails != null) {
            final WebEngine webEngine = htmlWebView.getEngine();
            webEngine.loadContent(htmlApiCallDetails.getHttpReply());
            htmlApiCallDetails = null;
        }
    }

    @FXML private WebView spewWebView;
    @FXML private TextField spewUrl;
    @FXML private TextField spewUrn;
    public void showSpewLogfile(ApiCallReply reply) {
        assert reply != null;
        Object rawResult = reply.getRawResult();
        assert rawResult != null;
        assert rawResult instanceof Hashtable;
        Hashtable rawHashtable = (Hashtable) rawResult;
        Object rawCode = (Hashtable) rawHashtable.get("code");
        assert rawCode != null;
        assert rawCode instanceof Hashtable;
        Hashtable codeHashTable = (Hashtable) rawCode;
        Object urlObject = codeHashTable.get("protogeni_error_url");
        assert urlObject != null;
        assert urlObject instanceof String;
        String url = (String) urlObject;

        String urn = "";
        Object urnObject = codeHashTable.get("protogeni_error_log");
        if (urnObject != null && urnObject instanceof String)
            urn = (String) urnObject;

        logTabPane.getTabs().add(spewLogTab);
//        spewLogTab.setDisable(false);

        spewUrl.setText(url);
        spewUrn.setText(urn);
    }
    public void showSpewLog() {
        final WebEngine webEngine = spewWebView.getEngine();
        webEngine.load(spewUrl.getText());
    }

    private String getSpew() {
        final WebEngine webEngine = spewWebView.getEngine();
        Document doc = webEngine.getDocument();
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            StringWriter sw = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.getBuffer().toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    @FXML private void saveSpew() {
        askSave(getSpew(), "txt");
    }
    @FXML private void saveXml() {
        askSave(xmlText.getText(), "xml");
    }
    @FXML private void saveResultText() {
        askSave(resultText.getText(), "txt");
    }
    @FXML private void saveResultError() {
        askSave(resultErrorText.getText(), "txt");
    }
    @FXML private void saveLowLevel() {
        askSave(lowLevelText.getText(), "txt");
    }
    @FXML private void saveXmlRpcSent() {
        askSave(xmlRpcSentText.getText(), "txt");
    }
    @FXML private void saveXmlRpcRecv() {
        askSave(xmlRpcReceivedText.getText(), "txt");
    }
    @FXML private void saveHttpSent() {
        askSave(httpSentText.getText(), "txt");
    }
    @FXML private void saveHttpRecv() {
        askSave(httpReceivedText.getText(), "txt");
    }
    @FXML private void saveAll() {
        ApiCallDetails apiCallDetails = shownApiCallDetails.get();
        String text = "Call \""+apiCallDetails.getGeniMethodName()+"\" in API \""+apiCallDetails.getApiName()+"\"\n";
        text += "Authority: \""+apiCallDetails.getAuthority().getName()+"\" urn="+apiCallDetails.getAuthority().getUrn()+"\n";
        text += "Server URL: \""+apiCallDetails.getServerUrl()+"\"\n\n";
        text += "Start time: \""+apiCallDetails.getStartTime()+"\"\n\n";
        text += "Stop time: \""+apiCallDetails.getStopTime()+"\"\n\n";
        text += "HTTP data sent:\n"+apiCallDetails.getHttpRequest()+"\n\n";
        text += "HTTP data recv:\n"+apiCallDetails.getHttpReply()+"\n\n";
        text += "XmlRpc data sent:\n"+XmlRpcPrintUtil.printXmlRpcResultObject(apiCallDetails.getXmlRpcRequest())+"\n\n";
        text += "XmlRpc data recv:\n"+XmlRpcPrintUtil.printXmlRpcResultObject(apiCallDetails.getXmlRpcReply())+"\n\n";

        ApiCallReply reply = apiCallDetails.getReply();
        if (reply != null && reply.getRawResult() instanceof Hashtable) {
            Hashtable rawHashtable = (Hashtable) reply.getRawResult();
            Object rawValue = rawHashtable.get("value");
            if (rawValue != null)
                text += "Reply recv:\n"+XmlRpcPrintUtil.printXmlRpcResultObject(rawValue)+"\n\n";
        }

        if (reply != null && hasSpewLog(reply)) {
            text += "Spew log:\n"+getSpew()+"\n\n";
        }

        askSave(text, "txt");

    }
    private void askSave(String text, String ext) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save " + ext);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(ext+" Files", "*."+ext),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = fileChooser.showSaveDialog(/*stage*/null);
        if (file != null) {
            IOUtils.stringToFile(file, text);
        }
    }

    private ObjectProperty<ApiCallDetails> shownApiCallDetails = new SimpleObjectProperty<>(null);
    private ChangeListener<ApiCallDetails> shownLogChangeListener = new ChangeListener<ApiCallDetails>() {
        @Override
        public void changed(ObservableValue<? extends ApiCallDetails> observableValue, ApiCallDetails oldApiCallDetails, ApiCallDetails newApiCallDetails) {
            //no need to bind! ApiCallDetails never changes...

//            rspecTab.setDisable(true);
//            xmlTab.setDisable(true);
//            spewLogTab.setDisable(true);
//            htmlErrorTab.setDisable(true);
            logTabPane.getTabs().removeAll(rspecTab, xmlTab, spewLogTab, htmlErrorTab, exceptionTab);

            shownApiCallDetails.set(newApiCallDetails);

            if (newApiCallDetails == null) {
                resultText.textProperty().set("");
                lowLevelText.textProperty().set("");
                xmlRpcCommand.textProperty().set("");
                xmlRpcSentText.textProperty().set("");
                xmlRpcReceivedText.textProperty().set("");
                authorityNameText.textProperty().set("");
                serverUrlText.textProperty().set("");
                callStartTimeField.textProperty().set("");
                callStartTimeRelativeLabel.textProperty().set("");
                callStopTimeField.textProperty().set("");
                callStopTimeRelativeLabel.textProperty().set("");
                httpSentText.textProperty().set("");
                httpReceivedText.textProperty().set("");

                showResultErrorText(false);
                showResultText(false);

                resultTab.setDisable(true);
                lowLevelTab.setDisable(true);
                XmlRpcTab.setDisable(true);
                httpTab.setDisable(true);
            } else {
                resultTab.setDisable(false);
                lowLevelTab.setDisable(false);
                XmlRpcTab.setDisable(false);
                httpTab.setDisable(false);

                if (newApiCallDetails.getAuthority() != null)
                    authorityNameText.textProperty().set(newApiCallDetails.getAuthority().getName());
                else
                    authorityNameText.textProperty().set("");
                serverUrlText.textProperty().set(newApiCallDetails.getServerUrl());

                Date startTime = newApiCallDetails.getStartTime();
                Date stopTime = newApiCallDetails.getStopTime();
                if (startTime == null) {
                    callStartTimeField.textProperty().set("");
                    callStartTimeRelativeLabel.textProperty().set("");
                } else {
                    callStartTimeField.textProperty().set(""+startTime);
                    long millis = System.currentTimeMillis() - startTime.getTime();
                    String relString = TimeUtils.formatMillis(millis, TimeUnit.DAYS, TimeUnit.SECONDS);
                    callStartTimeRelativeLabel.textProperty().set("("+relString+" ago)");
                }
                if (stopTime == null) {
                    callStopTimeField.textProperty().set("");
                    callStopTimeRelativeLabel.textProperty().set("");
                } else {
                    callStopTimeField.textProperty().set(""+stopTime);
                    String res = "(";
                    long millis = System.currentTimeMillis() - stopTime.getTime();
                    res += TimeUtils.formatMillis(millis, TimeUnit.DAYS, TimeUnit.SECONDS)+" ago";
                    if (startTime != null) {
                        long millis2 = stopTime.getTime() - startTime.getTime();
                        res += " = "+TimeUtils.formatMillis(millis2, TimeUnit.DAYS, TimeUnit.MILLISECONDS)+" after start";
                    }
                    res += ")";
                    callStopTimeRelativeLabel.textProperty().set(res);
                }

                httpSentText.textProperty().set(newApiCallDetails.getHttpRequest());
                httpReceivedText.textProperty().set(newApiCallDetails.getHttpReply());

                xmlRpcCommand.textProperty().set(newApiCallDetails.getGeniMethodName());
                xmlRpcSentText.textProperty().set(XmlRpcPrintUtil.printXmlRpcResultObject(newApiCallDetails.getXmlRpcRequest()));
                xmlRpcReceivedText.textProperty().set(XmlRpcPrintUtil.printXmlRpcResultObject(newApiCallDetails.getXmlRpcReply()));

                if (isHtml(newApiCallDetails.getHttpReply()))
                    showHtml(newApiCallDetails);

                ApiCallReply reply = newApiCallDetails.getReply();
                if (reply == null) {
                    resultCode.textProperty().set("ERROR: no reply");
                    showResultErrorText(false);
                    showResultText(false);

                    resultTab.setDisable(true);
                    lowLevelTab.setDisable(true);
                    lowLevelText.setText("ERROR: no reply");
                    lowLevelText.setDisable(true);
                } else {
                    resultTab.setDisable(false);
                    //result code and error output (error output only hidden if there is none and the call was successful)
                    if (reply.getGeniResponseCode() == null) {
                        resultCode.textProperty().set("ERROR: no GeniResponseCode");
                        showResultErrorText(true);
                    } else {
                        resultCode.textProperty().set("GeniResponseCode="+reply.getGeniResponseCode().getCode()+" = "+reply.getGeniResponseCode().getDescription());

                        if (reply.getGeniResponseCode().isSuccess() && (reply.getOutput() == null || reply.getOutput().isEmpty())) {
                            resultErrorText.setText("");
                            showResultErrorText(false);
                        } else {
                            resultErrorText.setText(reply.getOutput());
                            showResultErrorText(true);
                        }
                    }

                    //Result (hidden if no result)
                    if (reply.getValue() == null) {
                        showResultText(false);
                        resultText.textProperty().set("<null>");
                        resultText.setDisable(true);
                    } else {
                        boolean showingXml = false;
                        showResultText(true);
                        resultText.setDisable(false);
                        if (reply.getValue() instanceof String) {
                            String repliedString = (String) reply.getValue();
                            if (isRspec(repliedString))
                                showRspec(repliedString);

                            //show Xml by looking at high level reply, so also show when received compressed xml
                            if (isXml(repliedString)) {
                                showingXml = true;
                                showXml(repliedString);
                            }

                            resultText.textProperty().set(repliedString);
                        }
                        else {
//                            if (reply.getValue() instanceof GeniCredential)
//                                showXml(((GeniCredential)reply.getValue()).getCredentialXml());

                            resultText.textProperty().set(XmlRpcPrintUtil.printXmlRpcResultObject(reply.getValue()));
                        }

                        //show Xml by looking at low level reply, so even when reinterpreted (for example as GeniCredential). Does not show compressed Xml.
                        if (!showingXml && reply.getRawResult() != null && reply.getRawResult() instanceof Hashtable) {
                            Object rawValue = ((Hashtable)reply.getRawResult()).get("value");
                            if (rawValue != null && rawValue instanceof String && isXml((String)rawValue))
                                showXml((String)rawValue);
                        }

                    }


                    //Low level result (raw result Hashtable)
                    lowLevelTab.setDisable(false);
                    if (reply.getRawResult() instanceof Hashtable) {
                        Hashtable rawHashtable = (Hashtable) reply.getRawResult();
                        Object rawValue = rawHashtable.get("value");
                        if (rawValue == null) {
                            lowLevelText.setDisable(true);
                            lowLevelText.textProperty().set("ERROR: raw result is a hashtable without \"value\" field:\n\n" + XmlRpcPrintUtil.printXmlRpcResultObject(reply.getRawResult()));
                        }
                        else {
                            lowLevelText.setDisable(false);
                            lowLevelText.textProperty().set(XmlRpcPrintUtil.printXmlRpcResultObject(rawValue));
                        }
                    } else {
                        lowLevelText.setDisable(true);
                        lowLevelText.textProperty().set("ERROR: raw result not a hashtable:\n\n" + XmlRpcPrintUtil.printXmlRpcResultObject(reply.getRawResult()));
                    }

                    if (hasSpewLog(reply))
                        showSpewLogfile(reply);
                }

                if (newApiCallDetails.getException() != null)
                    showException(newApiCallDetails.getException());
            }
        }
    };
}
