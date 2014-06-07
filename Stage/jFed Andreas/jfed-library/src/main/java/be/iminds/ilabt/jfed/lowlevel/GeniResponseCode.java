package be.iminds.ilabt.jfed.lowlevel;

/**
 * GeniResponseCode
 */
public interface GeniResponseCode {
    boolean isSuccess();

    boolean isBusy();

    int getCode();

    String getDescription();
}
