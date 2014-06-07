package be.iminds.ilabt.jfed.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.zip.InflaterInputStream;

/**
 * DataConversionUtils
 */
public class DataConversionUtils {
    /**
     * @param s A base64 encoded string, containing compressed data. Compression is as specified in RFC 1950
     * @return the decoded string
     */
    public static String decompressFromBase64(String s) {
        String res = "";
        if (s.length() == 0)
            return s;
        try {
            byte[] b = Base64.decodeBase64(StringUtils.getBytesUtf8(s));
            BufferedReader br = new BufferedReader(new InputStreamReader(new InflaterInputStream(new ByteArrayInputStream(b))));
            String line = br.readLine();
            while (line != null) {
                res += line;
                line = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            System.err.println("Could not decompress string of length="+s.length());
            //e.printStackTrace();
            return null;
        }

        return res;
    }

    public static byte[] decodeBase64(String s) {
        byte[] b = Base64.decodeBase64(StringUtils.getBytesUtf8(s));
        return b;
    }

    public static String encodeBase64(byte[] b) {
        return Base64.encodeBase64String(b);
    }
}
