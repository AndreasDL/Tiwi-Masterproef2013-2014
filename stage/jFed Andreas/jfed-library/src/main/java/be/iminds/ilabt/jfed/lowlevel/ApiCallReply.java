package be.iminds.ilabt.jfed.lowlevel;

/**
 * <p>APICallReply represents the reply to a call of a method of a class implementing AbstractApi
 *
 * In Protogeni/Geni/SFA there are always 3 things in a reply:
 *   - a response code, see {@link GeniAMResponseCode}
 *   - a value, which is the result of the call. This can be null if the call failed
 *   - output, which is ussually empty if the call is successful, but is filled in the case an error occurred, to give more information
 *
 * Not all API's have the same method to return these, so there are multiple implementations
 */
public interface ApiCallReply<T> {
    public GeniResponseCode getGeniResponseCode();
    public T getValue();

    /** can be null */
    public String getOutput();

    public Object getRawResult();
}
