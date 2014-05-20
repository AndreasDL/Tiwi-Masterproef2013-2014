package be.iminds.ilabt.jfed.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A few helper functions for dates
 */
public class RFC3339Util {
    /**
     * Convert a java date object to a String representing the data in RFC 3339 format.
     *
     * @see  <a href="https://tools.ietf.org/html/rfc3339">RFC 3339</a>
     * @param date a java Date
     * @return a string representing the given date in RFC 3339 format
     */
    public static String dateToRFC3339String(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").format(date).replaceFirst("(\\d\\d)(\\d\\d)$", "$1:$2");
    }

    /**
     * Convert a String with a date in RFC 3339 format, to a java Date object
     *
     * @see  <a href="https://tools.ietf.org/html/rfc3339">RFC 3339</a>
     * @param rfc3339date a String with a date in RFC 3339 format
     * @return the rfc3339date input, converted to a java Date object
     */
    public static Date rfc3339StringToDate(String rfc3339date) throws ParseException {
        //TODO: currently, this removes the sub second part of a RFC3339 date!
        String parsableDateString = rfc3339date.replaceFirst("Z$", "+00:00").replaceFirst("(\\d\\d):(\\d\\d)$", "$1$2").replaceFirst("(\\d\\d)\\.\\d\\d([+-]\\d\\d\\d\\d)$","$1$2");
//        System.out.println("rfc3339StringToDate(\""+rfc3339date+"\") => \""+parsableDateString+"\"");
        return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").parse(parsableDateString);
    }
}
