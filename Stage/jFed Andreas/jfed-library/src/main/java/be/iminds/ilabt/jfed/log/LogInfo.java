package be.iminds.ilabt.jfed.log;

/**
 * LogInfo gathers related logging info.
 *
 * LogInfo can be hierarchical, containing other LogInfo's
 *
 * For example (made up), a user clicks a button indicating he wants to create a slice:
 *   - LogInfo name="Create Slice"
 *     - LogItem (GUILogItem) Button Create Slice Clicked
 *     - LogItem (StringLogItem) "All checks for createslice passed"
 *     - LogInfo name="CreateSlice command"
 *        - LogInfo name="ToSend"
 *           - LogItem (XMLLogItemType) credential in XML
 *           - LogItem (CredentialItemType) credential info
 *           - ...
 *        - LogInfo name="Send"
 *           - LogItem Connection info including server URL, certificates and keys used, and whether connection was setup now, or existed already + timing info
 *           - LogItem HTTP sent with plaintext message
 *           - LogItem XMLRPC with content in its type (ex XML)
 *        - LogInfo name="Receive"
 *           - LogItem HTTP headers and data received + http timing info
 *           - LogItem XMLRPC content received in its type
 *        - LogInfo name="ProcessResponse"
 *           - LogItem (XMLLogItemType) received credential in XML
 *           - LogItem (CredentialItemType) received credential info
 *           - ...
 *     - LogInfo name="SliceStatus command"
 *        - ...
 *        - ...
 *
 *  Each Thread has a current LogInfo that any new LogInfo and LogItems are added to.
 *  That current logInfo can be retreived with getRoot
 *  new LogInfo's are added using the existing LogInfo's
 *    => update: not one per thread needed! just one root and everything thread safe.
 *
 *  TODO: what about requests that run in a separate thread?
 *     - thread safety is needed in all cases
 *     - either have a seperate LogInfo per thread
 *     - allow threads to adopt threadinfo from others.
 *        - probably best. imagine a GUI thread and a request thread for the example above.
 *          -> (bad option:) could also be all in a single thread, if all logging is reported to that thread
 *          -> probably each request will be an object handed to the other thread. That object could hold the LogInfo
 *            -> might not even need root per thread if all is passed on like tis anyway!
 *            => CONCLUSION: just make it thread safe and ignore threads
 *
 *
 * This should have listeners
 */


public class LogInfo {
    private String name;
    private LogInfo(String name) { this.name = name; }

    public static LogInfo root = null;
    public static LogInfo getRoot() {
        if (root == null) root = new LogInfo("root");
        return root;
    }
    /* Finish this logInfo, "level" */
    public void finish() {}
    public void add(LogItem item) {}
    /* Begin a new, deeper, level of Loginfo */
    public LogInfo begin() { return null; }
}
