package be.iminds.ilabt.jfed.lowlevel;

import java.util.HashMap;
import java.util.Map;

/**
* GeniResponseCode
*/
public enum GeniCHResponseCode implements GeniResponseCode {
    NONE                    (0, "No error encountered – the return value is a successful result. An empty list form a query should be interpreted as ‘nothing found matching criteria’."),
    AUTHENTICATION_ERROR    (1, "The invoking tool or member did not provide appropriate credentials indicating that they are known to the CH or that they possessed the private key of the entity they claimed to be"),
    AUTHORIZATION_ERROR     (2, "The invoking tool or member does not have the authority to invoke the given call with the given arguments"),
    ARGUMENT_ERROR          (3, "The arguments provided to the call were mal-formed or mutually inconsistent."),
    DATABASE_ERROR          (4, "An error from the underlying database was returned. (More info should be provided in the ‘output’ return value]"),
    NOT_IMPLEMENTED_ERROR   (100, "The given method is not implemented on the server."),
    SERVER_ERROR            (101, "An error in the client/server connection"),

    INTERNAL_NONGENI_ERROR 	(50, "Internal error in client application"), //SPECIFIC FOR THIS CODE, NOT IN GENI API SPECIFICATION!
    SERVER_REPLY_ERROR      (51, "The client could not parse the server's reply. Either the server sent something strange, or this is a bug in the client"); //SPECIFIC FOR THIS CODE, NOT IN GENI API SPECIFICATION!

    private int code;
    private String description;
    GeniCHResponseCode(int code, String description) {
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

    private static Map<Integer, GeniCHResponseCode> responseCode_by_code = null;
    public static GeniCHResponseCode getByCode(int c) {
        if (responseCode_by_code == null) {
            responseCode_by_code = new HashMap<Integer, GeniCHResponseCode>();
            GeniCHResponseCode[] allCode = GeniCHResponseCode.values();
            for (int i = 0; i < allCode.length; i++) {
                GeniCHResponseCode grc = allCode[i];
                responseCode_by_code.put(grc.getCode(), grc);
            }
        }
        GeniCHResponseCode res = responseCode_by_code.get(c);
        if (res == null)
            throw new RuntimeException("GeniCHResponseCode "+c+" does not exists");
        if (res.getCode() != c)
            throw new RuntimeException("BUG in GeniCHResponseCode: mapped "+c+" onto "+res.getCode());
        return res;
    }

    @Override
    public String toString() {
        return "GeniCHResponseCode{" +
                "code=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}
