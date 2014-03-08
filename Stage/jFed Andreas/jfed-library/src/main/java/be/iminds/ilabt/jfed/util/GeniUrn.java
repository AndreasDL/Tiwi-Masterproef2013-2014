package be.iminds.ilabt.jfed.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*URN format:
*
* The syntax of URNs, in Backus-Naur form, is:[1]
*
*     <URN> ::= "urn:" <NID> ":" <NSS>
*
* This renders as:
*
*     urn:<NID>:<NSS>
*
* The leading urn: sequence is case-insensitive. <NID> is the namespace identifier, which determines the syntactic
* interpretation of <NSS>, the namespace-specific string. The functional requirements for uniform resource names are
* described in RFC 1737.
*/

/*
* public ID urns: http://www.ietf.org/rfc/rfc3151.txt
*
* The persistence of URNs in the "publicid" namespace is the same as
* the persistence of the corresponding public identifier.
*    *  The "publicid" namespace is available for a wide range of uses; it
* cannot be subjected to a uniform persistence policy.  As a general
* rule, formal public identifiers with registered owner identifiers
* are more likely to be persistent than informal public identifiers
* or formal public identifiers with unregistered owner identifiers.
*    *  One exception to this rule is the "IDN" scheme for producing a
* registered owner identifier from a domain name.  That scheme
* contains at least all the weaknesses associated with the
* persistence of domain names.
*/

/*
* urn publicid IDN scheme:
*
* ?
* */

/*
* Geni URN info:
*
* @link http://www.protogeni.net/wiki/URNs
* @link http://groups.geni.net/geni/wiki/GeniApiIdentifiers
*
*  The definitive description of URN structure is given in the GMOC proposal. However, as an informational
*  summary, ProtoGENI URNs can be considered in the form:
*
*      urn:publicid:IDN+toplevelauthority+resource-type+resource-name
*
*  where:
*
*  toplevelauthority
*      is an internationali[sz]ed domain name (which must match the one in the certificate of the authority which
*      issued the object name)
*  resource-type
*      is a string describing the type of the named object (the set of strings is described below)
*  resource-name
*      should uniquely identify the object among any other resources with identical toplevelauthority and
*      resource-type. It is important to realise that the ProtoGENI API attaches no other significance to this
*      field, and in particular, no relation is implied between objects with identical resource-name but differing
*      toplevelauthority or resource-type. However, individual authorities (and especially component managers) may
*      choose to define additional semantics for resource names.
* */
public class GeniUrn {
    //TODO: add support for sub-authority
    public class GeniUrnParseException extends Exception {
        public GeniUrnParseException(String badUrn) {
            super("String \""+badUrn+"\" is not a valid Geni Urn");
        }
    }

    private final String topLevelAuthority;
    private final String resourceType;
    private final String resourceName;

    /**
     * @throws GeniUrnParseException for invalid urns.
     * see parse(String urn) to get null result instead,
     * */
    public GeniUrn(String urn) throws GeniUrnParseException {
        //urn:publicid:IDN+toplevelauthority[:sub-authority]*\+resource-type\+resource-name
        String patternString = "urn:publicid:IDN\\+([0-9a-zA-Z._:-]*)\\+([a-zA-Z]*)\\+([+;%0-9a-zA-Z._:-]*)";
        Pattern urnPattern = Pattern.compile(patternString);
        Matcher urnMatcher = urnPattern.matcher(urn);
        if (!urnMatcher.matches())
            throw new GeniUrnParseException(urn);
        this.topLevelAuthority = urnMatcher.group(1);
        this.resourceType = urnMatcher.group(2);
        this.resourceName = urnMatcher.group(3);

        if (topLevelAuthority == null || resourceType == null || resourceName == null)
            throw new GeniUrnParseException(urn);

        if (topLevelAuthority.isEmpty() || resourceType.isEmpty() || resourceName.isEmpty())
            throw new GeniUrnParseException(urn);

        assert topLevelAuthority != null;
        assert resourceType != null;
        assert resourceName != null;
        assert !topLevelAuthority.isEmpty();
        assert !resourceType.isEmpty();
        assert !resourceName.isEmpty();
    }

    /** returns null for invalid urns. */
    public static GeniUrn parse(String urn) {
        if (urn == null) return null;
        try {
            GeniUrn res = new GeniUrn(urn);
            return res;
        } catch(GeniUrnParseException e) {
            return null;
        }
    }
    /** returns null for invalid urns. */
    public static boolean valid(String urn) {
        if (urn == null) return false;
        try {
            GeniUrn res = new GeniUrn(urn);
            return true;
        } catch(GeniUrnParseException e) {
            return false;
        }
    }

    public GeniUrn(String topLevelAuthority, String resourceType, String resourceName) {
        this.topLevelAuthority = topLevelAuthority;
        this.resourceType = resourceType;
        this.resourceName = resourceName;

        assert topLevelAuthority != null;
        assert resourceType != null;
        assert resourceName != null;
        assert !topLevelAuthority.isEmpty();
        assert !resourceType.isEmpty();
        assert !resourceName.isEmpty();
    }

    public String getTopLevelAuthority() {
        return topLevelAuthority;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeniUrn geniUrn = (GeniUrn) o;

        if (!resourceName.equals(geniUrn.resourceName)) return false;
        if (!resourceType.equals(geniUrn.resourceType)) return false;
        if (!topLevelAuthority.equals(geniUrn.topLevelAuthority)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = topLevelAuthority.hashCode();
        result = 31 * result + resourceType.hashCode();
        result = 31 * result + resourceName.hashCode();
        return result;
    }

    public String toString() {
        return "urn:publicid:IDN+"+topLevelAuthority+"+"+resourceType+"+"+resourceName;
    }
}
