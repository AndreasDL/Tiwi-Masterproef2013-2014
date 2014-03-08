package be.iminds.ilabt.jfed.lowlevel.api.test;

import be.iminds.ilabt.jfed.testing.base.ApiTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * TestClassList
 */
public class TestClassList {
    private static TestClassList ourInstance = new TestClassList();

    public static TestClassList getInstance() {
        return ourInstance;
    }

    private TestClassList() {
    }

//    private static List<String> allTestClasses() {
//        return getInstance().allTestClasses();
//    }

    public List<String> allTestClasses() {
        ArrayList<String> res = new ArrayList<String>();
        res.add("be.iminds.ilabt.jfed.lowlevel.api.test.ConnectivityTestSliceAuthority");
        res.add("be.iminds.ilabt.jfed.lowlevel.api.test.ConnectivityTestAggregateManager3");
        res.add("be.iminds.ilabt.jfed.lowlevel.api.test.ConnectivityTestAggregateManager2");

        res.add("be.iminds.ilabt.jfed.lowlevel.api.test.TestSliceAuthority");
        res.add("be.iminds.ilabt.jfed.lowlevel.api.test.TestAggregateManager2");
        res.add("be.iminds.ilabt.jfed.lowlevel.api.test.TestAggregateManager3");

        res.add("be.iminds.ilabt.jfed.lowlevel.api.test.TestAggregateManager2ListResources");
        res.add("be.iminds.ilabt.jfed.lowlevel.api.test.TestAggregateManager3ListResources");

        res.add("be.iminds.ilabt.jfed.lowlevel.api.test.TestPleCreateSliver");

        res.add("be.iminds.ilabt.jfed.lowlevel.api.test.StitchingTest");
        return res;
    }

    public Collection<String> getGroups(Class<? extends ApiTest> clazz) {
        Set<String> res = new HashSet<String>();

         //get list of annotated methods
        List<Method> methods = Arrays.asList(clazz.getMethods());
        for (Method m : methods) {
            Annotation[] annotations = m.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof ApiTest.Test) {
                    ApiTest.Test testAn = (ApiTest.Test) annotation;
                    for (String group : testAn.groups())
                        res.add(group);
                }
            }
        }

        return res;
    }

    public Collection<String> getGroups(String className) {
        try {
            Class<?> c = Class.forName(className);
            if (! ApiTest.class.isAssignableFrom(c)) {
                throw new RuntimeException("Class \""+className+"\" is not an ApiTest class");
            }
            Class<? extends ApiTest> cc = (Class<? extends ApiTest>) c;
            return getGroups(cc);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class \""+className+"\" not found", e);
        }
    }
}
