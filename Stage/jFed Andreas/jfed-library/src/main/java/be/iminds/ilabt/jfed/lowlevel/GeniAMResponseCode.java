package be.iminds.ilabt.jfed.lowlevel;

import java.util.HashMap;
import java.util.Map;

/**
* GeniResponseCode
*/
public enum GeniAMResponseCode implements GeniResponseCode {
    GENIRESPONSE_SUCCESS          (0,  "Success"                                                                                           ),
    GENIRESPONSE_BADARGS          (1,  "Bad Arguments: malformed arguments"                                                                ),
    GENIRESPONSE_ERROR            (2,  "Error (other)"                                                                                     ),
    GENIRESPONSE_FORBIDDEN        (3,  "Operation Forbidden: eg supplied credentials do not provide sufficient privileges (on given slice)"),
    GENIRESPONSE_BADVERSION       (4,  "Bad Version (eg of RSpec)"                                                                         ),
    GENIRESPONSE_SERVERERROR      (5,  "Server Error"                                                                                      ),
    GENIRESPONSE_TOOBIG           (6,  "Too Big (eg request RSpec)"                                                                        ),
    GENIRESPONSE_REFUSED          (7,  "Operation Refused"                                                                                 ),
    GENIRESPONSE_TIMEDOUT         (8,  "Operation Timed Out"                                                                               ),
    GENIRESPONSE_DBERROR          (9,  "Database Error"                                                                                    ),
    GENIRESPONSE_RPCERROR         (10, "RPC Error"                                                                                         ),
    GENIRESPONSE_UNAVAILABLE      (11, "Unavailable (eg server in lockdown)"                                                               ),
    GENIRESPONSE_SEARCHFAILED     (12, "Search Failed (eg for slice)"                                                                      ),
    GENIRESPONSE_UNSUPPORTED      (13, "Operation Unsupported"                                                                             ),
    GENIRESPONSE_BUSY             (14, "Busy (resource, slice); try again later"                                                           ),
    GENIRESPONSE_EXPIRED          (15, "Expired (eg slice)"                                                                                ),
    GENIRESPONSE_INPROGRESS       (16, "In Progress"                                                                                       ),
    GENIRESPONSE_ALREADYEXISTS    (17, "Already Exists (eg the slice}"                                                                     ),
    GENIRESPONSE_VLAN_UNAVAILABLE (24, "VLAN tag(s) requested not available (likely stitching failure)"                                    ),
    SERVERBUSY 	                  (-32001, "Server is (temporarily) too busy; try again later"                                             ),

    INTERNAL_NONGENI_ERROR 	      (50, "Internal error in client application"),  //SPECIFIC FOR THIS CODE, NOT IN GENI API SPECIFICATION!
    SERVER_REPLY_ERROR            (51, "The client could not parse the server's reply. Either the server sent something strange, or this is a bug in the client");//SPECIFIC FOR THIS CODE, NOT IN GENI API SPECIFICATION!

    private int code;
    private String description;
    GeniAMResponseCode(int code, String description) {
        this.code = code;
        this.description = description;
    }
    @Override
    public boolean isSuccess() { return code == 0 || code == 16; }
    @Override
    public boolean isBusy() { return code == 14 || code == -32001; }
    @Override
    public int getCode() { return code; }
    @Override
    public String getDescription() { return description; }

    private static Map<Integer, GeniAMResponseCode> responseCode_by_code = null;
    public static GeniAMResponseCode getByCode(int c) {
        if (responseCode_by_code == null) {
            responseCode_by_code = new HashMap<Integer, GeniAMResponseCode>();
            GeniAMResponseCode[] allCode = GeniAMResponseCode.values();
            for (int i = 0; i < allCode.length; i++) {
                GeniAMResponseCode grc = allCode[i];
                responseCode_by_code.put(grc.getCode(), grc);
            }
        }
        GeniAMResponseCode res = responseCode_by_code.get(c);
        if (res == null)
            throw new RuntimeException("GeniAMResponseCode "+c+" does not exists");
        if (res.getCode() != c)
            throw new RuntimeException("BUG in GeniAMResponseCode: mapped "+c+" onto "+res.getCode());
        return res;
    }

    @Override
    public String toString() {
        return "GeniAMResponseCode{" +
                "code=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}
