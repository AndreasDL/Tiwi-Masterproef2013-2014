package be.iminds.ilabt.jfed.util;

import junit.framework.Assert;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * RFC3339 unit tests
 */
public class RFC3339UtilTest {
    @Test
    public void testRFC3339() throws ParseException {
        String a1 = "1996-12-19T16:39:57.25-08:00";
        String a2 = "1996-12-20T00:39:57.25Z";
        String a3 = "1996-12-20T00:39:57.25+00:00";
        String b1 = "1990-12-31T23:59:60Z";
        String b2 = "1990-12-31T15:59:60-08:00";
        String c = "2013-04-22T05:18:52Z";

        Date a1d = RFC3339Util.rfc3339StringToDate(a1);
        Date a2d = RFC3339Util.rfc3339StringToDate(a2);
        Date a3d = RFC3339Util.rfc3339StringToDate(a3);
        Date b1d = RFC3339Util.rfc3339StringToDate(b1);
        Date b2d = RFC3339Util.rfc3339StringToDate(b2);
        Date cd = RFC3339Util.rfc3339StringToDate(c);

        String a1r = RFC3339Util.dateToRFC3339String(a1d);
        String a2r = RFC3339Util.dateToRFC3339String(a2d);
        String a3r = RFC3339Util.dateToRFC3339String(a3d);
        String b1r = RFC3339Util.dateToRFC3339String(b1d);
        String b2r = RFC3339Util.dateToRFC3339String(b2d);
        String cr = RFC3339Util.dateToRFC3339String(cd);

        Date a1dr = RFC3339Util.rfc3339StringToDate(a1r);
        Date a2dr = RFC3339Util.rfc3339StringToDate(a2r);
        Date a3dr = RFC3339Util.rfc3339StringToDate(a3r);
        Date b1dr = RFC3339Util.rfc3339StringToDate(b1r);
        Date b2dr = RFC3339Util.rfc3339StringToDate(b2r);
        Date cdr = RFC3339Util.rfc3339StringToDate(cr);

        Assert.assertEquals(a1d, a2d);
        Assert.assertEquals(a1d, a3d);
        Assert.assertEquals(b1d, b2d);

        Assert.assertEquals(a1dr, a1d);
        Assert.assertEquals(a2dr, a2d);
        Assert.assertEquals(a3dr, a3d);
        Assert.assertEquals(b1dr, b1d);
        Assert.assertEquals(b2dr, b2d);
        Assert.assertEquals(cdr, cd);
    }
}
