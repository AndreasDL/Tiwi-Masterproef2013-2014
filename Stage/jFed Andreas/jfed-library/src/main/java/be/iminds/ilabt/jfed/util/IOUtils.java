package be.iminds.ilabt.jfed.util;

import java.io.*;
import java.util.Scanner;

/**
 * IOUtils
 */
public class IOUtils {
    public static String askCommandLineInput(String question) {
        Scanner input=new Scanner(System.in);
        System.out.print(question+": ");
        return input.nextLine();
    }
    public static char[] askCommandLinePassword(String question) {
        Console cons = System.console();
        if ((cons = System.console()) != null) {
            char [] passwd = cons.readPassword("%s: ", question);
            return passwd;
        } else {
            //fallback if not a console
            Scanner input=new Scanner(System.in);
            System.out.print("*** everything you type might be shown *** "+question+": ");
            return input.nextLine().toCharArray();
        }
    }

    public static String fileToString(String filename) throws FileNotFoundException, IOException {
        return fileToString(new File(filename));
    }
    public static String fileToString(File file) throws FileNotFoundException, IOException {
        String res = "";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while (line != null) {
                res += line + "\n";
                line = br.readLine();
            }
//        } catch (Exception e) {
//            throw e;
//        } catch (IOException e) {
//            throw e;
        } finally {
            try {
                if (br != null) br.close();
            } catch(IOException e) { }
        }
        return res;
    }
    public static String streamToString(InputStream is) {
        String res = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            while (line != null) {
                res += line + "\n";
                line = br.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading inputstream '"+is+"': "+e.getMessage(), e);
        }
        return res;
    }
    public static void streamToFile(InputStream is, File outFile) {
        String res = "";
        try {
            FileWriter fw = new FileWriter(outFile);

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            while (line != null) {
                res += line + "\n";
                fw.write(line);
                line = br.readLine();
            }

            fw.close();
        } catch (IOException e) {
            throw new RuntimeException("Error reading inputstream '"+is+"' or writing to file \""+outFile.getName()+"\": "+e.getMessage(), e);
        }
    }
    public static String exceptionToStacktraceString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        String stacktrace = sw.getBuffer().toString();
        return stacktrace;
    }

    public static void stringToFile(String filename, String text) {
        stringToFile(new File(filename), text);
    }
    public static void stringToFile(File file, String text) {
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(text);
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException("Error writing '"+file+"': "+e.getMessage(), e);
        }
    }
}
