package be.iminds.ilabt.jfed.testing.base;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.util.CommandExecutionContext;

import java.util.*;

/**
 * ApiTest:
 *
 *   for a test class you need:
 *     - user info
 *     - SA info if applicable
 *     - tested API info
 *     - connection (auto)
 *     - logger (auto)
 *
 *  per test meta data
 *     - name (default: auto from method name)
 *     - dependencies and preferred grouping (name or methodName)
 *        - hard dependency (skip if other failed or skipped)
 *        - soft dependency (always run after other (no matter if it succeeds, skips or fails))
 *        - group (a name that can be the same for a number of tests. These will be kept together, unless the other dependencies are used)
 *
 *  for each test log:
 *     - commands and replies
 *     - details, notes, warnings, errors and fatal errors (+ skips: the test can start to run and then decide it needs to be skipped)
 *     - info on the test
 *         - description of what it does
 *         - description of what is expected
 */
public abstract class ApiTest {
    private CommandExecutionContext testContext;
    public CommandExecutionContext getTestContext() {
        return testContext;
    }
    void setTestContext(CommandExecutionContext testContext) {
        this.testContext = testContext;
    }

    private Properties testConfig;
    public Properties getTestConfig() {
        return testConfig;
    }
    void setTestConfig(Properties testConfig) {
        this.testConfig = testConfig;
    }

    /** Overwrite to specify required config keys */
    public List<String> getRequiredConfigKeys() {
        return new ArrayList<String>();
    }
    /** Overwrite to specify optional config keys */
    public List<String> getOptionalConfigKeys() {
        return new ArrayList<String>();
    }

    public Logger getLogger() {
        return testContext.getLogger();
    }

    public abstract String getTestDescription();
    public abstract void setUp(CommandExecutionContext testContext);

    private ApiTestResult.ApiTestMethodResult currentTestResult;
    void setCurrentTestResult(ApiTestResult.ApiTestMethodResult currentTestResult) {
        this.currentTestResult = currentTestResult;
    }

    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
    public @interface Test {
        java.lang.String description() default "";
        java.lang.String[] groups() default {};

        java.lang.String[] hardDepends() default {};
        java.lang.String[] softDepends() default {};

        String dataProvider() default ""; //TODO implement

        //this concept (isReturnValue annotation) is under revision
//        /**
//         * If true, only the success of this test will be used as return value for the entire test run
//         * If there are no tests with this option, the success of all tests is used.
//         * If there are multiple tests in a class with this option, the result is unspecified!
//         * */
//        boolean ignoreForReturnValue() default false;
    }
    public @interface DataProvider { //TODO implement
        String name() default "";
    }


    public void note(String val) {
        currentTestResult.addLogLine(ApiTestResult.MethodLogLineType.NOTE, val);
    }

    public void skip(String reason) {
        currentTestResult.state = ApiTestResult.TestResultState.SKIPPED;
//        currentTestResult.addLogLine(ApiTestResult.MethodLogLineType.NOTE, "Test skipped: "+reason);
        throw new RunTests.SkipTestException(reason);
    }

    public void errorNonFatal(String val) {
        currentTestResult.state = ApiTestResult.TestResultState.FAILED;
        currentTestResult.addLogLine(ApiTestResult.MethodLogLineType.ERROR, val);
    }

    public void errorFatal(String val) {
        fatalError(val);
    }
    public void fatalError(String val) {
//        currentTestResult.addLogLine(ApiTestResult.MethodLogLineType.FATAL_ERROR, val);
        currentTestResult.state = ApiTestResult.TestResultState.FAILED;
        throw new AssertionError(val);
    }

    public void warn(String val) {
        currentTestResult.addLogLine(ApiTestResult.MethodLogLineType.WARN, val);
        if (currentTestResult.state == ApiTestResult.TestResultState.SUCCESS)
            currentTestResult.state = ApiTestResult.TestResultState.WARN;
    }

    public void warnIfNot(boolean v) {
        assertTrue(v, "warnIfNot failed");
    }
    public void warnIfNot(boolean v, String failText) {
        if (!v)
            warn(failText);
    }

    public static void assertTrue(boolean v) {
        assertTrue(v, "assertTrue failed");
    }
    public static void assertTrue(boolean v, String failText) {
        if (!v)
            throw new AssertionError(failText);
    }


    public static void assertFalse(boolean v) {
        assertFalse(v, "assertFalse failed");
    }
    public static void assertFalse(boolean v, String failText) {
        if (v)
            throw new AssertionError(failText);
    }


    public static void assertNotNull(Object o) {
        assertNotNull(o, "object is null");
    }
    public static void assertNotNull(Object o, String failText) {
        if (o == null)
            throw new AssertionError(failText);
    }


    public static void assertNull(Object o) {
        assertNotNull(o, "object is null");
    }
    public static void assertNull(Object o, String failText) {
        if (o != null)
            throw new AssertionError(failText);
    }

    public static void assertEquals(Object a, Object b, String failText) {
        if (!a.equals(b))
            throw new AssertionError(failText);
    }
    public static void assertEquals(Object a, Object b) {
        assertEquals(a, b, "object are not equal");
    }


    public static void assertNotEquals(Object a, Object b, String failText) {
        if (a.equals(b))
            throw new AssertionError(failText);
    }
    public static void assertNotEquals(Object a, Object b) {
        assertEquals(a, b, "object are equal");
    }

    public static void assertInstanceOf(Object o, Class<?> c) {
        assertInstanceOf(o, c, "object is not an instance of class \""+c.getName()+"\"");
    }
    public static void assertInstanceOf(Object o, Class<?> c, String failText) {
        if (!c.isInstance(o))
            throw new AssertionError(failText);
    }



    public Vector assertHashTableContainsVector(Hashtable t, String key) {
        Object o = t.get(key);
        assertNotNull(o);
        assertEquals(o.getClass(), Vector.class, "value for " + key + " is not a Vector but a " + o.getClass().getName());
        return (Vector) o;
    }
    public String assertHashTableContainsString(Hashtable t, String key) {
        Object o = t.get(key);
        assertNotNull(o);
        assertEquals(o.getClass(), String.class, "value for " + key + " is not a String but a " + o.getClass().getName());
        return (String) o;
    }
    public String assertHashTableContainsNonemptyString(Hashtable t, String key) {
        Object o = t.get(key);
        assertNotNull(o, "value for " + key + " is null");
        assertEquals(o.getClass(), String.class, "value for " + key + " is not a String but a " + o.getClass().getName());
        String s = (String) o;
        assertTrue(s.length() > 0, "value for " + key + " is an empty string");
        return s;
    }
}

