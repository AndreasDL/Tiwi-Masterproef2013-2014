package be.iminds.ilabt.jfed.testing.base;

import be.iminds.ilabt.jfed.log.ApiCallDetails;
import be.iminds.ilabt.jfed.lowlevel.ApiCallReply;
import be.iminds.ilabt.jfed.util.CommandExecutionContext;
import be.iminds.ilabt.jfed.util.IOUtils;
import org.rendersnake.HtmlCanvas;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static org.rendersnake.HtmlAttributesFactory.*;

/**
 * ApiTestResult: list of test method results
 */
public class ApiTestResult {
    public enum TestResultState { SUCCESS, WARN, SKIPPED, FAILED };
    public enum MethodLogLineType { NOTE, WARN, ERROR, FATAL_ERROR, EXCEPTION };

    public static class MethodLogLine {
        MethodLogLineType type;
        String text;
        long timeMs;

        MethodLogLine(MethodLogLineType type, String text, long timeMs) {
            this.type = type;
            this.text = text;
            this.timeMs = timeMs;
        }
        MethodLogLine(MethodLogLineType type, String text) {
            this(type, text, System.currentTimeMillis());
        }
    }

    public static class ApiTestMethodResult {
        String methodName;
        String description;
        List<String> hardDependencies;
        List<String> softDependencies;

        List<MethodLogLine> logLines = new ArrayList<MethodLogLine>();
        List<ApiCallDetails> apiCallDetails = new ArrayList<ApiCallDetails>();
        Throwable exception;

        TestResultState state;

        long timeMs;
        long startTimeMs;
        long stopTimeMs;

        void addLogLine(MethodLogLineType type, String text) {
            logLines.add(new MethodLogLine(type, text));
        }

        public TestResultState getState() {
            return state;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getDescription() {
            return description;
        }

        public List<String> getHardDependencies() {
            return Collections.unmodifiableList(hardDependencies);
        }

        public List<String> getSoftDependencies() {
            return Collections.unmodifiableList(softDependencies);
        }

        public List<MethodLogLine> getLogLines() {
            return Collections.unmodifiableList(logLines);
        }

        public List<ApiCallDetails> getApiCallDetails() {
            return Collections.unmodifiableList(apiCallDetails);
        }

        public Throwable getException() {
            return exception;
        }

        public long getTimeMs() {
            return timeMs;
        }

        public long getStartTimeMs() {
            return startTimeMs;
        }

        public long getStopTimeMs() {
            return stopTimeMs;
        }
    }


    private CommandExecutionContext testContext;
    private Class<? extends ApiTest> testClass;
    private String testGroup;

    public ApiTestResult(CommandExecutionContext testContext, Class<? extends ApiTest> testClass, String testGroup) {
        this.testContext = testContext;
        this.testClass = testClass;
        this.testGroup = testGroup;
    }

    public CommandExecutionContext getTestContext() {
        return testContext;
    }

    public Class<? extends ApiTest> getTestClass() {
        return testClass;
    }

    /** ordered list of results */
    List<ApiTestMethodResult> resultList = new ArrayList<ApiTestMethodResult>();
    int returnValue = 0;

    List<ApiTestMethodResult> getResultList() {
        return Collections.unmodifiableList(resultList);
    }

    /*
     * return value meaning:
     * 2: errors
     * 1: warnings but no errors
     * 0: all success
     */
    public int getReturnValue() {
        return returnValue;
    }

    public String stateIconImage(TestResultState state) {
        switch (state) {
            case SKIPPED: return "skip.png";
            case SUCCESS: return "success.png";
            case WARN: return "warn.png";
            case FAILED: return "fail.png";
            default: return "fail.png";
        }
    }

    public String stateClass(TestResultState state) {
        switch (state) {
            case SKIPPED: return "skip";
            case SUCCESS: return "success";
            case WARN: return "warn";
            case FAILED: return "fail";
            default: return "fail";
        }
    }

    public void copyToFile(URL in, File fileOut) throws IOException {
        assert in != null;
        assert fileOut != null;
        OutputStream out = new FileOutputStream(fileOut);
        InputStream is = in.openStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        out.close();
    }

    public void addKeyValue(HtmlCanvas html, String clazz, String key, String value) throws IOException {
        html.div();
        html.span().content(key+": ", true);
        html.span(class_(clazz)).content(value, true);
        html._div();
    }
    public void addCollapsable(HtmlCanvas html, String clazz, String shortText, String fullContent) throws IOException {
//        if (!fullContent.isEmpty()) {
//            html.div(class_("reveal")).content(shortText);
//            html.div(style("display:none;").class_(clazz)).content(fullContent+"TEST TEST TEST", true);
//            html.div(class_("collapse").style("display:none;")).content("Collapse");
//        }

        if (fullContent.length() > 50) {
            String id = clazz+Math.random();

            html.div();
            html.span().content(shortText+": ", true);
            html.a(class_("reveal").onClick("switchVisibility('"+id+"')").title("Hide/Show " + shortText)).content("Hide/Show", true);
            html._div();
            html.div(style("display:none;").class_(clazz+"-big").id(id)).content(fullContent, true);
        } else {
            addKeyValue(html, clazz, shortText, fullContent);
        }

//<a onclick="switchMenu('toggle');" title="Test">Click Me</a>
// <br>
// <div id="toggle">This text toggles when Click Me is clicked.</div>
    }

    private static class SecurityRiskDescription {
        public String start;
        public String end;
        private SecurityRiskDescription(String start, String end) {
            this.start = start;
            this.end = end;
        }
    }
    public static List<SecurityRiskDescription> getAllSecurityRiskDescriptions() {
        List<SecurityRiskDescription> res = new ArrayList<SecurityRiskDescription>();
        res.add(new SecurityRiskDescription("<signed-credential", "</signed-credential>"));
        res.add(new SecurityRiskDescription("<owner_gid", "</owner_gid>"));
        res.add(new SecurityRiskDescription("<target_gid", "</target_gid>"));
        res.add(new SecurityRiskDescription("<X509Certificate", "<X509Certificate>"));

        res.add(new SecurityRiskDescription("&lt;signed-credential", "&lt;/signed-credential&gt;"));
        res.add(new SecurityRiskDescription("&lt;owner_gid", "&lt;/owner_gid&gt;"));
        res.add(new SecurityRiskDescription("&lt;target_gid", "&lt;/target_gid&gt;"));
        res.add(new SecurityRiskDescription("&lt;X509Certificate", "&lt;X509Certificate&gt;"));

        res.add(new SecurityRiskDescription("-----BEGIN RSA PRIVATE KEY-----", "-----END RSA PRIVATE KEY-----"));

        res.add(new SecurityRiskDescription("MIID", "[^A-Za-z0-9+/=._: \n-]")); //PEM Base64 certificate block detection (starts with MIID due to header)

//        res.add(new SecurityRiskDescription("", ""));
        return res;
    }

    /* Scans the input string for credentials, and removes them. */
    public String removeCredentials(String text) {
        StringBuffer sb = new StringBuffer();
        List<SecurityRiskDescription> secRisks = getAllSecurityRiskDescriptions();

//        System.out.println("Scanning string of size "+text.length()+" for "+secRisks.size()+" security risks.");

        while (text.length() > 0) {
            int firstStringIndex = -1;
            int secRiskIndex = -1;
            int i = 0;
            for (SecurityRiskDescription secRisk : secRisks) {
                int stringIndex = text.indexOf(secRisk.start);
                if (stringIndex >= 0 && (stringIndex < firstStringIndex || firstStringIndex == -1)) {
                    firstStringIndex = stringIndex;
                    secRiskIndex = i;
                }
                i++;
            }

            if (secRiskIndex != -1) {
                //add part before security risk
                String safePart = text.substring(0, firstStringIndex);
                sb.append(safePart);
                text = text.substring(firstStringIndex);

                //cut out security risk
                SecurityRiskDescription secRisk = secRisks.get(secRiskIndex);
//                System.out.println("   ********* Found security risk: "+secRisk.start+" @ position "+firstStringIndex);
                Pattern pattern = Pattern.compile(secRisk.end);
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    //int startIndex = matcher.start();
                    int endIndex = matcher.end();
                    String unsafe = text.substring(0, endIndex);
                    text = text.substring(endIndex);
                    sb.append("**** SECURITY: removed text that looked like a credential. ("+unsafe.length()+" characters) ****");
                } else {
                    //problem: out of paranoia, lose the rest of the string
                    sb.append("**** SECURITY: removed text that looked like a credential. WARNING: could not find end, so removed rest of text ("+text.length()+" characters) ****");
                    text = "";
                }

            } else {
//                System.out.println("   No (more) security risks found.");
                //add entire string
                sb.append(text);
                text = "";
            }
        }

        return sb.toString();
    }

    public void toHtml(File file) {
        int idHelper = 0;

        boolean embedCss = true;

        try {
            try {
                //copy css
                if (!embedCss) {
                    URL cssUrl = ApiTest.class.getResource("result.css");
                    if (cssUrl == null) throw new FileNotFoundException("could not find resource result.css");
                    copyToFile(cssUrl, new File(file.getParentFile(), "result.css"));
                }

                //copy png's
                URL failUrl = ApiTest.class.getResource("error15x15.png");
                URL okUrl = ApiTest.class.getResource("ok15x15.png");
                URL skipUrl = ApiTest.class.getResource("skip15x15.png");
                URL warnUrl = ApiTest.class.getResource("warning15x15.png");

                if (failUrl == null) throw new FileNotFoundException("could not find resource error15x15.png");
                if (okUrl == null) throw new FileNotFoundException("could not find resource ok15x15.png");
                if (skipUrl == null) throw new FileNotFoundException("could not find resource skip15x15.png");
                if (warnUrl == null) throw new FileNotFoundException("could not find resource warning15x15.png");

                copyToFile(failUrl, new File(file.getParentFile(), "fail.png"));
                copyToFile(warnUrl, new File(file.getParentFile(), "warn.png"));
                copyToFile(skipUrl, new File(file.getParentFile(), "skip.png"));
                copyToFile(okUrl, new File(file.getParentFile(), "success.png"));
            } catch (IOException e) {
                System.err.println("Error while loading generated html css or png. This will be ignored.");
                e.printStackTrace();
            }

            FileWriter writer = new FileWriter(file);
            HtmlCanvas html = new HtmlCanvas(writer);

            html
              .html()
                .head()
                    .title().content("tile")
                    .meta(name("description").add("content","description",false))
                    .macros().stylesheet("result.css");

            if (embedCss) {
                InputStream is = ApiTest.class.getResourceAsStream("result.css");
                String cssContent = IOUtils.streamToString(is);
                html.style(media("screen").type("text/css")).content(cssContent, false);
            }

            //javascript
            InputStream is = ApiTest.class.getResourceAsStream("result.js");
            String jsContent = IOUtils.streamToString(is);
            html.script(language("JavaScript1.3")).content(jsContent, false);


            html._head().body();

            html.h1().content("Test Settings");
            html.div(class_("test-settings"));
            addKeyValue(html, "value-data", "Test User URN", testContext.getGeniUser().getUserUrn());
            addKeyValue(html, "value-data", "Test User Authority", testContext.getUserAuthority().getUrn());
            html.br();
            addKeyValue(html, "value-data", "Tested Aggregate Manager (if applicable)", testContext.getTestedAuthority().getUrn());
            html.br();
            addKeyValue(html, "value-data", "Test Class", testClass.getName());
            if (testGroup != null)
                addKeyValue(html, "value-nondata", "Tested Methods", "Only group \""+testGroup+"\" + dependencies");
            else
                addKeyValue(html, "value-nondata", "Tested Methods", "All");
            try {
                ApiTest test = testClass.newInstance();
                addKeyValue(html, "value-data", "Test Description", test.getTestDescription());
            } catch (Exception e) {
                e.printStackTrace();
                addKeyValue(html, "value-data", "Test Description", "Error while retrieving: "+e.getMessage());
            }
            html._div();

            html.h1().content("Overview");

            int index = 0;
            for (ApiTestMethodResult res : resultList) {
                html.div(class_("methodoverview-full"));
                html.img(src(stateIconImage(res.state)));
                html.span(class_("methodoverview-methodname " + stateClass(res.state) + "header"))
                        .span().content(res.methodName, true)
                     ._span();
                html._div();
                index += 1;
            }
            index = 0;
            html.h1().content("Details");
            for (ApiTestMethodResult res : resultList) { //<img width="3%" src="skipped.png"/>
                html.span();
                html.div(class_("methoddetail-header " + stateClass(res.state) + "header"));//.content(res.methodName + "-> " +res.state, true);
                    html.img(src(stateIconImage(res.state)));
                    html.span().content(res.methodName);
                html._div();
                html.div(class_("methoddetail " + stateClass(res.state) + "body")); //.content(res.methodName + "-> " +res.state, true);
                html.div(class_("state")).content(res.state.toString());
                html.div(class_("description")).content(res.description);
                html.div(class_("times"));
                    html.span(class_("timeheader")).content("duration ");
                    html.span(class_("value-nondata")).content((res.timeMs / 1000.0) + "s");
                    html.span(class_("timeheader")).content(" from ");
                    html.span(class_("value-nondata")).content(new Date(res.startTimeMs).toString());
                    html.span(class_("timeheader")).content(" to ");
                    html.span(class_("value-nondata")).content(new Date(res.stopTimeMs).toString());
                html._div();
                if (res.exception != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    res.exception.printStackTrace(pw);
                    pw.close();
                    String stacktrace = sw.getBuffer().toString();
//                    html.div(class_("stacktrace")).content(stacktrace, true);
                    addCollapsable(html, "value-data", "Exception Stacktrace", stacktrace);
                }
                int apiCallCount = 1;
                for (ApiCallDetails details : res.apiCallDetails) {
                    String id = "api-call-"+(idHelper++);

                    html.div();

                    html.span().content("Api Call " + apiCallCount + ": ", true);
                    html.a(class_("reveal").onClick("switchVisibility('"+id+"')").title("Hide/Show Api Call")).content("Hide/Show", true);
                    html._div();

                    apiCallCount++;

                    html.div(style("display:none;").class_("api-call").id(id));
                    addKeyValue(html, "value-nondata", "API Name", details.getApiName());
//                    html.div(class_("api")).content(details.getApiName(), true);
                    if (details.getAuthority() != null) {
                        addKeyValue(html, "value-nondata", "Authority HRN", details.getAuthority().getHrn());
                        addKeyValue(html, "value-data", "Authority URN", details.getAuthority().getUrn());
//                        html.div(class_("authority-hrn")).content(details.getAuthority().getUrn(), true);
//                        html.div(class_("authority-urn")).content(details.getAuthority().getHrn(), true);
                    }
                    addKeyValue(html, "value-data", "API Method Name", details.getGeniMethodName());
                    addKeyValue(html, "value-data", "Java Method Name", details.getGeniMethodName());
                    addCollapsable(html, "value-data", "Parameters", removeCredentials(details.getMethodParameters()+""));
                    addKeyValue(html, "value-data", "Server URL", details.getServerUrl());
//                    html.div(class_("api-method-name")).content(details.getGeniMethodName(), true);
//                    html.div(class_("api-method-name-java")).content(details.getGeniMethodName(), true);
//                    html.div(class_("api-method-parameters")).content(details.getMethodParameters()+"", true);
//                    html.div(class_("api-method-server-url")).content(details.getServerUrl(), true);
//                    html.div(class_("xmlrpc-request")).content(details.getXmlRpcRequest()+"", true);
//                    html.div(class_("xmlrpc-reply")).content(details.getXmlRpcReply()+"", true);
//                    html.div(class_("http-request")).content(details.getHttpRequest(), true);
//                    html.div(class_("http-reply")).content(details.getHttpReply(), true);
                    addCollapsable(html, "value-data", "XMLRPC Request", removeCredentials(details.getXmlRpcRequest() + ""));
                    addCollapsable(html, "value-data", "XMLRPC Reply", removeCredentials(details.getXmlRpcReply() + ""));
                    addCollapsable(html, "value-data", "HTTP Request", removeCredentials(details.getHttpRequest()));
                    addCollapsable(html, "value-data", "HTTP Reply", removeCredentials(details.getHttpReply()));
                    ApiCallReply reply = details.getReply();
                    if (reply != null) {
                        if (reply.getGeniResponseCode() != null)
                            addKeyValue(html, "value-data", "Geni Response Code", reply.getGeniResponseCode().getCode()+" "+reply.getGeniResponseCode().getDescription());
//                        html.div(class_("geni-response-code")).content(reply.getGeniResponseCode().getCode()+" "+reply.getGeniResponseCode().getDescription(), true);
//                        html.div(class_("geni-value")).content(reply.getValue()+"", true);
//                        html.div(class_("geni-output")).content(reply.getOutput(), true);
                        addCollapsable(html, "value-data", "Geni Reply Value", removeCredentials(reply.getValue()+""));
                        addCollapsable(html, "value-data", "Geni Reply Output Reply", removeCredentials(reply.getOutput() + ""));
//                        html.div(class_("raw")).content(reply.getRawResult()+"", true);
                    }
                    html._div();
                }
                for (MethodLogLine logline : res.getLogLines()) {
                    String id = "logline-"+(idHelper++);

                    String type = logline.type.name();
                    html.div().content(type + ": " + logline.text, true);
                }
                html._div();
                html._span();
                index += 1;
            }

            html._body()
          ._html();

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
