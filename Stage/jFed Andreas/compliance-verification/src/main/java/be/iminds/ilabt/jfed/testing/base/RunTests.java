package be.iminds.ilabt.jfed.testing.base;

import be.iminds.ilabt.jfed.log.ApiCallDetails;
import be.iminds.ilabt.jfed.log.ResultListener;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.test.TestClassList;
import be.iminds.ilabt.jfed.lowlevel.authority.*;
import be.iminds.ilabt.jfed.util.CommandExecutionContext;
import be.iminds.ilabt.jfed.util.GeniTrustStoreHelper;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RunTests: simply call org.testng.TestNG main
 */
public class RunTests {
    public interface TestListener {
        public void onStart(String testname, int testNr, int testCount);
        public void onResult(ApiTestResult.ApiTestMethodResult result, int testNr, int testCount);
        public void onAllTestDone(ApiTestResult result, int testCount);
    }

    private RunTests() {}

    static class SkipTestException extends RuntimeException {
        SkipTestException(String reason) {
            super(reason);
        }
    }

    private static class DepNode {
        public List<DepNode> dependsOn = new ArrayList<DepNode>();
        public List<DepNode> dependers = new ArrayList<DepNode>();
        public List<String> groups;
        public Method m;
        public ApiTest.Test a;
        public String mName;
        public DepNode(Method m, ApiTest.Test a) {
            this.m = m;
            this.a = a;
            this.mName = m.getName();
            this.groups = new ArrayList<String>();
            for (String g : a.groups())
                groups.add(g);
        }
    }
    private static DepNode findDepNode(String methodName, Collection<DepNode> depNodes) {
        //TODO make more efficient (though I doubt this is an issue)
        for (DepNode d : depNodes) {
            if (d.m.getName().equals(methodName)) return d;
        }
        return null;
    }

    private static interface DependancyRetriever {
        List<DepNode> getDependancies(DepNode node, List<DepNode> allDepNodes);
    }
    private static class DepNodeSortResult {
        List<List<Method>> methods = new ArrayList<List<Method>>();
        boolean hasCycle = false;
    }
    private static DepNodeSortResult sortUsingDependancies(Map<Method, ApiTest.Test> methods, DependancyRetriever dependancyRetriever) {
        List<DepNode> unlinkedNodes = new ArrayList<DepNode>();

        //   make all depnodes
        for (Map.Entry<Method, ApiTest.Test> e : methods.entrySet()) {
            Method m = e.getKey();
            ApiTest.Test a = e.getValue();

            unlinkedNodes.add(new DepNode(m, a));
        }

        return sortDepNodesUsingDependancies(unlinkedNodes, dependancyRetriever);
    }
    private static DepNodeSortResult sortUsingDependancies(List<Method> methods, DependancyRetriever dependancyRetriever) {
        List<DepNode> unlinkedNodes = new ArrayList<DepNode>();

        //   make all depnodes
        for (Method m : methods) {
            Annotation[] annotations = m.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof ApiTest.Test) {
                    unlinkedNodes.add(new DepNode(m, (ApiTest.Test)annotation));
                }
            }
        }

        return sortDepNodesUsingDependancies(unlinkedNodes, dependancyRetriever);
    }
    private static DepNodeSortResult sortDepNodesUsingDependancies(List<DepNode> unlinkedNodes, DependancyRetriever dependancyRetriever) {
        //   link all depnodes
        List<DepNode> depNodes = new ArrayList<DepNode>(unlinkedNodes);
        for (DepNode d : unlinkedNodes) {
            List<DepNode> deps = dependancyRetriever.getDependancies(d, depNodes);

            for (DepNode other : deps) {
                other.dependers.add(d);
                d.dependsOn.add(other);
            }
        }

        DepNodeSortResult res = new DepNodeSortResult();

        //   extract all depnodes (this step breaks the links and empties the depNodes list!)
        int prevSize = depNodes.size() + 1;
        while (depNodes.size() > 0 && depNodes.size() != prevSize) {
            prevSize = depNodes.size();
            List<DepNode> toRemove = new ArrayList<DepNode>();
            for (DepNode d : depNodes)
                if (d.dependsOn.isEmpty()) toRemove.add(d);

            List<Method> depMethList = new ArrayList<Method>();
            for (DepNode d: toRemove) {
                for (DepNode depender : d.dependers)
                    depender.dependsOn.remove(d);
                depMethList.add(d.m);
            }
            res.methods.add(depMethList);

            depNodes.removeAll(toRemove);
        }
        if (depNodes.size() > 0) {
            res.hasCycle = true;
            List<Method> leftoverList = new ArrayList<Method>();
            for (DepNode leftover: depNodes) {
                for (DepNode depender : leftover.dependers)
                    depender.dependsOn.remove(leftover);
                leftoverList.add(leftover.m);
            }
            res.methods.add(leftoverList);
        } else
            res.hasCycle = false;

        return res;
    }

    /* keep only methods in group and dependencies */
    private static Map<Method, ApiTest.Test> filterUsingGroup(Map<Method, ApiTest.Test> methods, DependancyRetriever dependancyRetriever, String group) {
        List<DepNode> unlinkedNodes = new ArrayList<DepNode>();

        //   make all depnodes
        for (Map.Entry<Method, ApiTest.Test> e : methods.entrySet()) {
            Method m = e.getKey();
            ApiTest.Test a = e.getValue();

            unlinkedNodes.add(new DepNode(m, a));
        }

        //   link all depnodes
        List<DepNode> allDepNodes = new ArrayList<DepNode>(unlinkedNodes);
        for (DepNode d : unlinkedNodes) {
            List<DepNode> deps = dependancyRetriever.getDependancies(d, allDepNodes);

            for (DepNode other : deps) {
                other.dependers.add(d);
                d.dependsOn.add(other);
            }
        }

        List<DepNode> depNodes = new ArrayList<DepNode>();
        if (group != null) {
            //create new depNodes, which includes only methods in group and dependencies, no others
            List<DepNode> toAdd = new ArrayList<DepNode>();
            for (DepNode d : allDepNodes)
                if (d.groups.contains(group))
                    toAdd.add(d);
            depNodes.addAll(toAdd);
//            System.out.println("Group filter was used. Methods to run (without deps): "+toAdd.size());
            if (toAdd.isEmpty()) {
                throw new RuntimeException("Error: group \""+group+"\" not found. No tests to run.");
            }
            while (!toAdd.isEmpty()) {
                List<DepNode> adding = new ArrayList<DepNode>(toAdd);
                toAdd.clear();
                for (DepNode d : adding) {
                    for (DepNode dd : d.dependsOn)
                        if (!depNodes.contains(dd) && !toAdd.contains(dd)) {
                            depNodes.add(dd);
                            toAdd.add(dd);
                        }
                }
            }
//            System.out.println("Group filter was used. Methods to run after adding deps: "+depNodes.size());
        } else {
            depNodes = allDepNodes;
        }

        Map<Method, ApiTest.Test> result = new HashMap<Method, ApiTest.Test>();
        for (DepNode d : depNodes) {
            result.put(d.m, d.a);
        }
        return result;
    }

    /**
     * @param group if not null, only experiments in this group (and their soft and hard dependencies) will run. */
    public static ApiTestResult runTest(CommandExecutionContext testContext, Properties testConfig, Class<? extends ApiTest> testClass, String group, TestListener listener, boolean fake) {
        ApiTest test = null;

        try {
            test = testClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        test.setTestContext(testContext);
        test.setTestConfig(testConfig);

        //TODO: check for required config keys?
//        test.getRequiredConfigKeys();
//        test.getOptionalConfigKeys();

        //get list of annotated methods
        Map<Method, ApiTest.Test> testMethods = new HashMap<Method, ApiTest.Test>();
        List<Method> methods = Arrays.asList(testClass.getMethods());
        for (Method m : methods) {
            Annotation[] annotations = m.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof ApiTest.Test) {
                    testMethods.put(m, (ApiTest.Test) annotation);
                }
            }
        }

        List<String> allMethodNames = new ArrayList<String>();
        for (Method m : methods)
            allMethodNames.add(m.getName());

        DependancyRetriever bothDependancyRetriever = new DependancyRetriever() {
            /*allowedMissingMethodnames is used to allow soft deps on methodnames that have been removed due to "group".
            * This whole mecanism may be removed if faulty methodnames are not considered an error. (but it is better to warn for them) */
            List<String> allowedMissingMethodnames;
            public DependancyRetriever init(List<String> allowedMissingMethodnames) {
                this.allowedMissingMethodnames = allowedMissingMethodnames;
                return this;
            }
            @Override
            public List<DepNode> getDependancies(DepNode node, List<DepNode> allDepNodes) {

                String[] hardDeps = node.a.hardDepends();
                String[] softDeps = node.a.softDepends();
                List<DepNode> res = new ArrayList<DepNode>();
                for (String hardDep : hardDeps) {
                    DepNode other = findDepNode(hardDep, allDepNodes);
                    if (other == null && !allowedMissingMethodnames.contains(hardDep))
                        throw new RuntimeException("Test Method \""+node.m.getName()+"\" hard depends on unknown method \""+hardDep+"\"");
                    if (other != null)
                        res.add(other);
                }
                for (String softDep : softDeps) {
                    DepNode other = findDepNode(softDep, allDepNodes);
                    if (other == null && !allowedMissingMethodnames.contains(softDep))
                        throw new RuntimeException("Test Method \""+node.m.getName()+"\" soft depends on unknown method \""+softDep+"\"");
                    if (other != null)
                        res.add(other);
                }
                return res;
            }
        }.init(allMethodNames);

        DependancyRetriever hardDependancyRetriever = new DependancyRetriever() {
            List<String> allowedMissingMethodnames;
            public DependancyRetriever init(List<String> allowedMissingMethodnames) {
                this.allowedMissingMethodnames = allowedMissingMethodnames;
                return this;
            }

            @Override
            public List<DepNode> getDependancies(DepNode node, List<DepNode> allDepNodes) {
                String[] hardDeps = node.a.hardDepends();
                List<DepNode> res = new ArrayList<DepNode>();
                for (String hardDep : hardDeps) {
                    DepNode other = findDepNode(hardDep, allDepNodes);
                    if (other == null && !allowedMissingMethodnames.contains(hardDep))
                        throw new RuntimeException("Test Method \""+node.m.getName()+"\" depends on unknown method \""+hardDep+"\"");
                    if (other != null)
                        res.add(other);
                }
                return res;
            }
        }.init(allMethodNames);

        DependancyRetriever allowingSoftDependancyRetriever = new DependancyRetriever() {
            @Override
            public List<DepNode> getDependancies(DepNode node, List<DepNode> allDepNodes) {
                String[] softDeps = node.a.softDepends();
                List<DepNode> res = new ArrayList<DepNode>();
                for (String softDep : softDeps) {
                    DepNode other = findDepNode(softDep, allDepNodes);
                    //don't care about missing emthodnames  if (other == null) throw new RuntimeException("Test Method \""+node.m.getName()+"\" depends on unknown method \""+hardDep+"\"");
                    if (other != null)
                        res.add(other);
                }
                return res;
            }
        };


        //sort test list using dependencies
        if (group != null)
            testMethods = filterUsingGroup(testMethods, hardDependancyRetriever, group);
        DepNodeSortResult hardSortRes = sortUsingDependancies(testMethods, bothDependancyRetriever);
        if (hardSortRes.hasCycle) {
            List<Method> cycleMethods = hardSortRes.methods.get(hardSortRes.methods.size() - 1);
            String cycleMethodsString = "";
            for (Method m : cycleMethods) {
                if (!cycleMethodsString.isEmpty()) cycleMethodsString += "\n";
                cycleMethodsString += " - "+m.getName();
            }
            throw new RuntimeException("There are cycles in the dependency graph! Methods in cycle:\n"+cycleMethodsString);
        }

        List<Method> sortedTestMethods = new ArrayList<Method>();
        for (List<Method> subList : hardSortRes.methods) {
//            System.out.println("New hard order");
//            DepNodeSortResult softSortRes = sortUsingDependancies(subList, softDependancyRetriever);
//            //cycles are ignored in soft deps
//            for (List<Method> softSubList : softSortRes.methods) {
//                if (softSortRes.methods.size() > 1)
//                    System.out.println("    New soft order");
//                for (Method m : softSubList) {
//                    System.out.println("        method "+m.getName());
//                    sortedTestMethods.add(m);
//                }
//            }
            for (Method m : subList) {
                sortedTestMethods.add(m);
            }
        }





        //run test setup
        //TODO probably better if we treat setup as just another test, instead of this copy-paste
        final ApiTestResult.ApiTestMethodResult setupRes = new ApiTestResult.ApiTestMethodResult();

        ResultListener setupResultListener = new ResultListener() {
            @Override
            public void onResult(ApiCallDetails result) {
                setupRes.apiCallDetails.add(result);
            }
        };
        test.getTestContext().getLogger().addResultListener(setupResultListener);

        test.setCurrentTestResult(setupRes);
        setupRes.methodName = "setUp";
        setupRes.description = "Test Class setup";
        setupRes.softDependencies = new ArrayList<String>();
        setupRes.hardDependencies = new ArrayList<String>();
        setupRes.startTimeMs = System.currentTimeMillis();
        setupRes.stopTimeMs = 0;
        try{
            setupRes.state = ApiTestResult.TestResultState.SUCCESS;
            if (!fake)
                test.setUp(testContext);
            else
                test.note("\"fake\" set, setup not really called");
            setupRes.stopTimeMs = System.currentTimeMillis();
        } catch (Throwable ex) {
            setupRes.stopTimeMs = System.currentTimeMillis();
            setupRes.exception = ex;
            setupRes.addLogLine(ApiTestResult.MethodLogLineType.EXCEPTION, "Caught exception while executing tst class setup: "+ex.getMessage());
            setupRes.state = ApiTestResult.TestResultState.FAILED;
        }
        setupRes.timeMs = setupRes.stopTimeMs - setupRes.startTimeMs;
        test.getTestContext().getLogger().removeResultListener(setupResultListener);

        ApiTestResult result = new ApiTestResult(testContext, testClass, group);
        result.resultList.add(setupRes);
        if (!setupRes.getState().equals(ApiTestResult.TestResultState.SUCCESS)) {
            //abort testing if setup fails
            result.returnValue = 2;
            listener.onAllTestDone(result, sortedTestMethods.size());
            return result;
        }







        //run them
        boolean allSuccessful = true;
        boolean allMaxWarn = true;
        //this concept (isReturnValue annotation) is under revision
//        boolean singleTestSuccessUsed = false;
//        boolean singleTestSuccess = true;

        List<String> failList = new ArrayList<String>();

        int testNr = 1;
//        for (Map.Entry<Method, ApiTest.Test> e : testMethods.entrySet()) {
        for (Method m : sortedTestMethods) {
            ApiTest.Test a = testMethods.get(m);
//            Method m = e.getKey();
//            ApiTest.Test a = e.getValue();

            ApiTestResult.ApiTestMethodResult res = new ApiTestResult.ApiTestMethodResult();

            boolean skip = false;
            for (String hardDep : a.hardDepends())
                if (failList.contains(hardDep)) {
                    res.addLogLine(ApiTestResult.MethodLogLineType.NOTE, "Test method skipped because hard dependency has failed: "+hardDep);
                    skip = true;
                }

            if (!a.dataProvider().equals("")) {
                System.err.println("TODO: implement dataProvider support or rework tests and remove it");
                res.addLogLine(ApiTestResult.MethodLogLineType.NOTE, "Test method skipped because it uses dataProvider which is currently not supported. (needs to be implemented)");
                skip = true;
            }

            listener.onStart(m.getName(), testNr, sortedTestMethods.size());
            if (!skip) {
                res = runSingleTest(test, m, a, fake);
            } else {
                //skip
                res.methodName = m.getName();
                res.description = a.description();
                res.softDependencies = new ArrayList<String>(Arrays.asList(a.softDepends()));
                res.hardDependencies = new ArrayList<String>(Arrays.asList(a.hardDepends()));
                res.startTimeMs = System.currentTimeMillis();
                res.stopTimeMs = System.currentTimeMillis();
                res.timeMs = 0;
                res.exception = null;
                res.state = ApiTestResult.TestResultState.SKIPPED;
            }
            result.resultList.add(res);
            //this concept (isReturnValue annotation) is under revision
//            if (a.isReturnValue()) {
//                assert !singleTestSuccessUsed : "behaviour singleTestSuccessUsed in case of multiple tests using it is not specified yet";
//                singleTestSuccessUsed = true;
//                singleTestSuccess = res.state.equals(ApiTestResult.TestResultState.SUCCESS);
//            }
            boolean skipHardDeps = false;
            switch (res.state) {
                case FAILED: { allSuccessful = false; allMaxWarn = false; skipHardDeps = true; break; }
                case SKIPPED: { allSuccessful = false; allMaxWarn = false; skipHardDeps = true; break; }
                case WARN: { allSuccessful = false; break; }
                default: break;
            }
            if (skipHardDeps) failList.add(m.getName());

            listener.onResult(res, testNr, sortedTestMethods.size());
            testNr++;
        }

        //retval:
        //  2: errors
        //  1: warnings but no errors
        //  0: all success
        int retValue = allMaxWarn ? 1 : 2;
        if (allSuccessful) retValue = 0;

        //this concept (isReturnValue annotation) is under revision
//        if (singleTestSuccessUsed) retValue = singleTestSuccess ? 0 : 2;

        result.returnValue = retValue;
//        System.out.println("Debug return value: allMaxWarn="+allMaxWarn+" allSuccessful="+allSuccessful+
//                " singleTestSuccessUsed="+singleTestSuccessUsed+" singleTestSuccess="+singleTestSuccess+
//                " retValue="+retValue);

        listener.onAllTestDone(result, sortedTestMethods.size());

        return result;
    }

    private static ApiTestResult.ApiTestMethodResult runSingleTest(ApiTest test, Method method, ApiTest.Test annotation, boolean fake) {
        final ApiTestResult.ApiTestMethodResult res = new ApiTestResult.ApiTestMethodResult();

        ResultListener resultListener = new ResultListener() {
            @Override
            public void onResult(ApiCallDetails result) {
                res.apiCallDetails.add(result);
            }
        };
        test.getTestContext().getLogger().addResultListener(resultListener);

        test.setCurrentTestResult(res);
        res.methodName = method.getName();
        res.description = annotation.description();
        res.softDependencies = new ArrayList<String>(Arrays.asList(annotation.softDepends()));
        res.hardDependencies = new ArrayList<String>(Arrays.asList(annotation.hardDepends()));
        res.startTimeMs = System.currentTimeMillis();
        res.stopTimeMs = 0;
        try{
            try {
                res.state = ApiTestResult.TestResultState.SUCCESS;
                if (!fake)
                    method.invoke(test, null);
                else
                    test.note("\"fake\" set: method not really called");
                res.stopTimeMs = System.currentTimeMillis();
            } catch (IllegalAccessException e1) {
                System.err.println("RunTest bug: IllegalAccessException in runSingleTest");
                e1.printStackTrace();
                res.exception = e1;
            } catch (InvocationTargetException e1) {
                if (e1.getCause() != null) {
                    throw e1.getCause();
                } else {
                    System.err.println("RunTest bug: InvocationTargetException in runSingleTest");
                    e1.printStackTrace();
                    res.exception = e1;
                }
            }
        } catch (SkipTestException ex) {
            res.stopTimeMs = System.currentTimeMillis();
            res.exception = ex;
            res.state = ApiTestResult.TestResultState.SKIPPED;
            if (ex.getMessage().equals(""))
                res.addLogLine(ApiTestResult.MethodLogLineType.NOTE, "Test method requested test to be skipped");
            else
                res.addLogLine(ApiTestResult.MethodLogLineType.NOTE, "Test method requested test to be skipped, reason: "+ex.getMessage());
        } catch (AssertionError ex) { //AssertionError is used to signal fatal errors
            res.stopTimeMs = System.currentTimeMillis();
            res.exception = ex;
            res.addLogLine(ApiTestResult.MethodLogLineType.FATAL_ERROR, ex.getMessage());
            res.state = ApiTestResult.TestResultState.FAILED;
        } catch (GeniException ex) {
            res.stopTimeMs = System.currentTimeMillis();
            res.exception = ex;
            res.addLogLine(ApiTestResult.MethodLogLineType.EXCEPTION, "Caught exception while executing test: "+ex.getMessage());
            res.state = ApiTestResult.TestResultState.FAILED;

            //try to log as much of the call as possible
            if (ex.getXmlRpcResult() != null) {
                XMLRPCCallDetails xmlRes = ex.getXmlRpcResult();
                res.apiCallDetails.add(new ApiCallDetails(null, xmlRes.getServerUrl(), null, null, null, null,
                        xmlRes.getRequestHttpContent(), xmlRes.getResultHttpContent(), xmlRes.getRequest(), xmlRes.getResultValueObject(),
                        null, xmlRes.getStartTime(), xmlRes.getStopTime(), xmlRes.getException()));
            }
        } catch (Throwable ex) {
            res.stopTimeMs = System.currentTimeMillis();
            res.exception = ex;
            res.addLogLine(ApiTestResult.MethodLogLineType.EXCEPTION, "Caught exception while executing test: "+ex.getMessage());
            res.state = ApiTestResult.TestResultState.FAILED;
        }
        res.timeMs = res.stopTimeMs - res.startTimeMs;
        test.getTestContext().getLogger().removeResultListener(resultListener);
        return res;
    }

    public static void help(Options options) {
        String footer_extra = "";
        for (String testclass : TestClassList.getInstance().allTestClasses()) {
            footer_extra += ("   - "+testclass+"\n");
            Collection<String> groups = TestClassList.getInstance().getGroups(testclass);
            if (!groups.isEmpty())
                footer_extra += ("      groups:\n");
            for (String group : groups)
                footer_extra += ("       - "+group+"\n");
        }

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(200, "jfed-compliance-tester-cli [options ... ] <command> [command arguments ...]", "Options:", options,
                "\n\nExample Test Context Properties File content:\n  username = <username>\n" +
                        "  passwordFilename = <filename of file containing password>\n" +
                        "  pemKeyAndCertFilename = <filename of file containing user certificate and private key in PEM format>\n" +
                        "  userAuthorityUrn = <URN of test user's authority>\n" +
                        "  testedAggregateManagerUrn = <URN of tested Aggregate Manager>\n\n"+
                        "Known test classes:\n"+footer_extra);

        //TODO: support a number of other syntaxes:
        //TODO    - 1 file (including context and 1 or more tests)
        //TODO    - 1 file with context + 1 file with one or more tests
    }

    public static void main(String [] args) throws IOException, ClassNotFoundException {
        //TODO use apache commons-cli
        CommandLineParser parser = new BasicParser();

        // create the Options
        Options options = new Options();
        options.addOption( OptionBuilder.withLongOpt("context-file")
                .withDescription( "The properties fil containing context details (login, tested server, etc.) (MANDATORY)" )
                .hasArg()
                .withArgName("CONTEXT PROPERTIES FILE")
                .isRequired()
                .create() );
        options.addOption( OptionBuilder.withLongOpt("test-class")
                .withDescription( "Fully quantified test class name (MANDATORY)" )
                .hasArg()
                .withArgName("CLASS NAME")
                .isRequired()
                .create() );
        options.addOption( OptionBuilder.withLongOpt( "clearinghouse" )
                .withDescription( "Fetch certificates etc from clearinghouse first" )
                .create() );
        options.addOption( OptionBuilder.withLongOpt( "output-dir" )
                .withDescription( "Directory to write the generated reports (html file + PNGs)." )
                .hasArg()
                .withArgName("DIR")
                .create() );
        options.addOption( OptionBuilder.withLongOpt( "group" )
                .withDescription( "Test group to execute within test class (default: all methods)" )
                .hasArg()
                .withArgName("NAME")
                .create() );
        options.addOption( OptionBuilder.withLongOpt( "fake" )
                .withDescription( "Do everything, expect for executing the actual tests (there are considered successful)" )
                .create() );
        options.addOption( "q", "quiet", false, "less output" );
        options.addOption( "d", "debug", false, "extra debugging output" );

        CommandLine line = null;
        try {
            // parse the command line arguments
            line = parser.parse( options, args );
        }
        catch( ParseException exp ) {
            System.err.println( "Command line argument Syntax error: " + exp.getMessage() );
            help(options);
            System.exit(3);
        }

        String outputdirArg = line.getOptionValue("output-dir");
        String group = line.getOptionValue("group");
        String testclassname = line.getOptionValue("test-class");
        String testContextFilename = line.getOptionValue("context-file");
        boolean clearinghouse = line.hasOption("clearinghouse");
        boolean debug = line.hasOption("debug");
        boolean fake = line.hasOption("fake");
        boolean silent = line.hasOption("quiet");

        if (group != null && group.equals("null"))
            group = null;
        if (group != null && group.equals(""))
            group = null;

        if (clearinghouse) {
            AuthorityListModel authorityListModel = JFedAuthorityList.getAuthorityListModel();
//            AuthorityListModel authorityListModel = new AuthorityListModel();
//            BuiltinAuthorityList.load(authorityListModel);
            UtahClearingHouseAuthorityList.load(authorityListModel);
            for (SfaAuthority curAuth : authorityListModel.getAuthorities())
                if (curAuth.getPemSslTrustCert() != null)
                    GeniTrustStoreHelper.addTrustedPemCertificateIfNotAdded(curAuth.getPemSslTrustCert());

            UtahClearingHouseAuthorityList.retrieveCertificates();
        }

        File testContextFile = new File(testContextFilename);
        if (!testContextFile.exists()) throw new FileNotFoundException("Cannot find Test Context properties file: "+testContextFilename);
        CommandExecutionContext testContext = CommandExecutionContext.loadFromFile(testContextFile);

        if (!silent) {
            System.out.println("Read context properties from file \""+testContextFilename+"\":");
            System.out.println("   Tested Authority:"+testContext.getTestedAuthority().getName());
            System.out.println("      URN:"+testContext.getTestedAuthority().getUrn());
            System.out.println("      Hrn:"+testContext.getTestedAuthority().getHrn());
            System.out.println("      Server certificate:"+testContext.getTestedAuthority().getPemSslTrustCert());
            System.out.println("      Allowed server certificate hostname aliases:"+testContext.getTestedAuthority().getAllowedCertificateHostnameAliases());
            for (Map.Entry<ServerType, URL> e : testContext.getTestedAuthority().getUrls().entrySet()) {
                System.out.println("      URL for "+e.getKey()+": "+e.getValue());
            }
            System.out.println("   User:"+testContext.getGeniUser().getUserUrn());
            System.out.println("      Authority URN:"+testContext.getGeniUser().getUserAuthority().getUrn());
        }

        Properties testConfig = new Properties();
        testConfig.load(new FileInputStream(testContextFile));

        Class testClass = Class.forName(testclassname);

        ApiTestResult result = runTest(testContext, testConfig, testClass, group, new TestListener() {
            @Override
            public void onStart(String testname, int testNr, int testCount) {
                System.out.print("Running "+testname+"...");
            }

            @Override
            public void onResult(ApiTestResult.ApiTestMethodResult result, int testNr, int testCount) {
//                System.out.println(" " + result.getState().toString());
            }
            @Override public void onAllTestDone(ApiTestResult result, int testCount) { }
        }, fake);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
        Date now = new Date();
        String outputfilename = "result.html";
        String outputdirname = "test-results-"+testClass.getSimpleName();
        if (group != null)
            outputdirname += "-"+group;
        outputdirname += "-"+dateFormat.format(now);
        if (outputdirArg != null) {
            outputdirname = outputdirArg;
        }
        File outputdir = new File(outputdirname).getAbsoluteFile();
        File outputfile = new File(outputdir, outputfilename);

        if (!outputdir.exists())
        {
            if (outputdir.getParentFile() != null && !outputdir.getParentFile().exists()) {
                throw new RuntimeException("Error: parent directory \""+outputdir.getParentFile()+"\""+
                        " of output file does not exist.");
            } else
                outputdir.mkdir();
        }

        System.out.println("Saving results to \""+outputfile.getPath()+"\"...");

        result.toHtml(outputfile);

        System.exit(result.getReturnValue());
    }
}
