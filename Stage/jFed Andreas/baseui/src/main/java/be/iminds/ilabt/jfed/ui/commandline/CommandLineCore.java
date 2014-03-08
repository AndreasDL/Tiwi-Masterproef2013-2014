package be.iminds.ilabt.jfed.ui.commandline;

import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.GeniConnection;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceId;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceIdParser;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceUrn;
import be.iminds.ilabt.jfed.util.IOUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * CommandLineClient makes command line client helper methods from a class
 */
public class CommandLineCore<T> {
    private Class<T> targetClass;
    private T targetObject;

    private Map<String, Method> availableMethods;
    private Map<String, Method> availableMethodsLowerCase;

    private static Map<String, Method> findAvailableMethods(Class targetClass) {
        Map<String, Method> res = new HashMap<String, Method>();

        for (Method m : targetClass.getDeclaredMethods()) {//GeniConnection con,
            if (m.isAnnotationPresent(ApiMethod.class)) {
                ApiMethod am = m.getAnnotation(ApiMethod.class);
                Class[] parameterTypes = m.getParameterTypes();
                if (parameterTypes.length == 0 || !GeniConnection.class.isAssignableFrom(parameterTypes[0]))
                    throw new RuntimeException("Method "+m+" does not have GeniConnection as first parameter but it does have @ApiMethod");
                res.put(m.getName(), m);
            }
        }

        return res;
    }

    public CommandLineCore(Class<T> targetClass, T targetObject) {
        this.targetClass = targetClass;
        this.targetObject = targetObject;
        availableMethods = findAvailableMethods(targetClass);
        availableMethodsLowerCase = new HashMap<String, Method>();
        for (Map.Entry<String, Method> e : availableMethods.entrySet())
            availableMethodsLowerCase.put(e.getKey().toLowerCase(), e.getValue());
    }

    public Collection<String> getAvailableMethodNames() {
        return availableMethods.keySet();
    }

    public Collection<String> getAvailableMethodNamesLowerCase() {
        return availableMethodsLowerCase.keySet();
    }

    /**
     * @param methodName the name of the method
     * @return a String describing the syntax of the method.
     */
    public String getMethodSyntax(String methodName) {
        Method m = availableMethodsLowerCase.get(methodName.toLowerCase());
        Class[] parameterTypes = m.getParameterTypes();
        Annotation[][] annotations = m.getParameterAnnotations();

        String res = "";

        for (int i = 0; i < parameterTypes.length; i++) {
            Annotation[] paramAnnotations = annotations[i];
            Class paramClass = parameterTypes[i];
            for (int j = 0; j < paramAnnotations.length; j++)
                if (paramAnnotations[j].annotationType().equals(ApiMethodParameter.class)) {
                    ApiMethodParameter amp = (ApiMethodParameter) paramAnnotations[j];

                    if (res.length() > 0) res += " ";
                    if (amp.name().equals("credentialList")) {
                        res += "<credential file>";
                    } else {
                        if (amp.name().equals("rspec")) {
                            res += "<rspec file>";
                        } else {
                            if (amp.required()) res += "<"; else res += "[";
                            res += amp.name()+"";
                            if (paramClass.equals(Boolean.class)) res += " (true|false)";
                            if (paramClass.equals(Integer.class)) res += " (int)";
                            if (amp.required()) res += ">"; else res += "]";
                        }
                    }
                }
        }

        return res;
    }

    public static class ExecuteMethodResult {
        int exitvalue = 0;
        String output = "";
    }
    public ExecuteMethodResult executeMethod(GeniConnection connection, String methodName, List<String> arguments, boolean interactive) {
        Method m = availableMethodsLowerCase.get(methodName.toLowerCase());
        Class[] parameterTypes = m.getParameterTypes();
        Annotation[][] annotations = m.getParameterAnnotations();
        ExecuteMethodResult res = new ExecuteMethodResult();

        Object[] parameters = new Object[parameterTypes.length];

        LinkedList<String> remainingArguments = new LinkedList<String>(arguments);
        for (int i = 0; i < parameterTypes.length; i++) {
            Annotation[] paramAnnotations = annotations[i];
            Class paramClass = parameterTypes[i];

            if (GeniConnection.class.isAssignableFrom(paramClass))
                parameters[i] = connection;
            else
                for (int j = 0; j < paramAnnotations.length; j++)
                if (paramAnnotations[j].annotationType().equals(ApiMethodParameter.class)) {
                    ApiMethodParameter amp = (ApiMethodParameter) paramAnnotations[j];

                    String arg = null;
                    if (remainingArguments.isEmpty()) {
                        if (amp.required()) {
                            if (interactive)
                                arg = IOUtils.askCommandLineInput("Enter "+methodName+" argument '"+amp.name()+"'");
                            else {
                                System.err.println("Missing "+methodName+" argument '"+amp.name()+"'");
                                System.exit(1);
                            }
                        }
                        else
                            arg = null;
                        //throw new RuntimeException("Error: Not enough arguments specified for method "+methodName+". Syntax: "+methodName+" "+getMethodSyntax(methodName));
                    } else
                        arg = remainingArguments.pollFirst();

                    if (arg == null || arg.equals("null"))
                        parameters[i] = null;
                    else {
                        try {
                            if (amp.name().equals("credentialList")) {
                                List<GeniCredential> credList = new ArrayList<GeniCredential>();
                                String cred = IOUtils.fileToString(arg);
                                credList.add(new GeniCredential("credential from file "+arg, cred));
                                parameters[i] = credList;
                            } else {
                                if (amp.name().equals("rspec")) {
                                    parameters[i] = IOUtils.fileToString(arg);
                                } else {
                                    if (paramClass.equals(String.class))
                                        parameters[i] = arg;
                                    else
                                    if (paramClass.equals(Integer.class))
                                        parameters[i] = Integer.parseInt(arg);
                                    else
                                    if (paramClass.equals(Boolean.class))
                                        parameters[i] = Boolean.parseBoolean(arg);
                                    else
                                    if (paramClass.equals(ResourceId.class))
                                        parameters[i] = ResourceIdParser.parse(arg);
                                    else
                                    if (paramClass.equals(ResourceUrn.class))
                                        parameters[i] = new ResourceUrn(arg);
                                    else {
                                        res.exitvalue = 3;
                                        throw new RuntimeException("ERROR: Implementation incomplete: Unsupported parameter type: "+paramClass.getName());
                                    }
                                }
                            }
                        } catch (IOException e) {
                            res.exitvalue = 3;
                            throw new RuntimeException("ERROR: IOException reading credential file: "+e.getMessage(), e);
                        }
                    }
                }
        }

        try {
            Object methodRes = m.invoke(targetObject, parameters);
            ApiCallReply methodResRep = (ApiCallReply) methodRes;
            if (methodResRep.getGeniResponseCode().isSuccess()) {
                res.output += methodResRep.getValue().toString();
                res.exitvalue = 0;
            }
            else {
                res.output += "Error reported by server: "+methodResRep.getGeniResponseCode()+" output="+methodResRep.getOutput();
                if (methodResRep.getGeniResponseCode().isBusy())
                    res.exitvalue = 99;
                else
                    res.exitvalue = 1;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            res.exitvalue = 2;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            res.exitvalue = 2;
        }

        return res;
    }
}
