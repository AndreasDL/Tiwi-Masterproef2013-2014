package be.iminds.ilabt.jfed.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * LocalStringUtils
 */
public class TextUtil {
    public static String printArray(String[] arr) {
                            if (arr == null) return "null";
                            String res = "[";
                            boolean first = true;
                            for (String a : arr) {
                                if (!first)
                                    res += ", ";
                                res += a;
                                first = false;
                            }
                            res += "]";
                            return res;
                        }

    /**
     * @return the input string, with newlines inserted so that no line is longer than 'maxLineLen'
     * */
    public static String wrap(String s, int maxLineLen) {
        if (s.length() < maxLineLen)
            return s;
        String updated = "";
        int linecount = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n') linecount = 0;
            if (linecount >= maxLineLen-1) {
                updated += '\n';
                linecount = 0;
            }
            linecount++;
            updated += c;
        }

        return updated;
    }
    /**
     * @return the input string, with newlines inserted so that no line is longer than 'maxLineLen'
     * */
    public static String abbreviate(String s, int maxLineLen) {
        if (s.length() < maxLineLen)
            return s;
        return s.substring(0, maxLineLen) + "...";
    }

    public static List<String> getLines(String in) {
        List<String> res = new ArrayList<String>();

        for (String part : in.split("\n"))
            res.add(part);

        return res;
    }

    public static String mkString(Collection<?> c, String sep) {
        String res = "";
        for (Object i : c) {
            if (!res.isEmpty()) res += sep;
            res += i.toString();
        }
        return res;
    }
}