package be.iminds.ilabt.jfed.util;


import java.util.Hashtable;
import java.util.Vector;

/**
 * This helper class "stringifies" XMLRPC output (a structure made out of: Vector, Hashtable, String, Integer, Double, ...)
 * */
public class XmlRpcPrintUtil {
    private static String prependToEachNextLine(String in, String texttoprepend) {
        StringBuilder res = new StringBuilder(in.length()+100);
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c == '\n' && i != in.length()-1) {
                res.append(c + texttoprepend);
            } else
                res.append(c);
        }
        return res.toString();
    }
    public static String printXmlRpcResultObject(Object o) {
        return printXmlRpcResultObject(o, "");
    }

    private static String printXmlRpcResultObject(Object o, String texttoprepend) {
        if (o == null) return "null";
        StringBuilder res = new StringBuilder(100);
        boolean  knownClass = false;
        if (o.getClass() == Hashtable.class) {
            Hashtable h = (Hashtable) o;
            if (h.size() == 0)
                res.append("{ }");
            else {
                res.append("{\n");
                int j = 0;
                for (Object key : h.keySet()) {
                    Object val = h.get(key);
                    String preptext = texttoprepend;
                    int shiftLen = 6 + key.toString().length() + 3;
                    if (shiftLen > 6+6+3) shiftLen = 4+3;
                    for (int i = 0; i < shiftLen; i++)
                        preptext += " ";
                    if (key instanceof String)
                        res.append(texttoprepend+"\""+ key + "\" -> " + printXmlRpcResultObject(val, preptext));
                    else
                        res.append(texttoprepend+""+ key + " -> " + printXmlRpcResultObject(val, preptext));
                    if (j++ < h.keySet().size() - 1)
                        res.append(",");
                    res.append("\n");
                }
                res.append(texttoprepend+"}");
            }
            knownClass = true;
        }
        if (o.getClass() == Vector.class) {
            Vector v = (Vector) o;
            if (v.size() == 0)
                res.append("[ ]");
            else {
                res.append("[\n");
                int i = 0;
                for (Object e : v) {
                    res.append(texttoprepend + printXmlRpcResultObject(e, texttoprepend+"   "));
                    if (i++ < v.size() - 1)
                        res.append(",");
                    res.append("\n");
                }
                res.append(texttoprepend+"]");
            }
            knownClass = true;
        }
        if (o.getClass() == String.class) {
            String s = (String) o;
            if (s.length() > 10*1024*1024) //limit to 10MB
                res.append("\""+prependToEachNextLine("=== WARNING: String to long ("+s.length()+" chars) to visualize here! ===", texttoprepend)+"\"");
            else
                res.append("\""+prependToEachNextLine(s, texttoprepend)+"\"");
            knownClass = true;
        }
        //useless, as a string is added:
//        if (o.getClass() == GeniCredential.class) {
//            GeniCredential c = (GeniCredential) o;
//            res += "GeniCredential named \""+c.getName()+"\" size="+c.getCredentialXml().length();
//            knownClass = true;
//        }
        if (!knownClass )
            res.append(prependToEachNextLine(o.toString(), texttoprepend));
        return res.toString();
    }
}
