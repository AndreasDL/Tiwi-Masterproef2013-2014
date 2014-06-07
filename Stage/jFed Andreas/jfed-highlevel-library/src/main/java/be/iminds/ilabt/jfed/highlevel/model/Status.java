package be.iminds.ilabt.jfed.highlevel.model;

import javafx.beans.property.ObjectProperty;

/**
 * Status of a sliver of slice
 */
public enum Status {
    UNINITIALISED, /* nothing known about status*/
    READY,
    UNALLOCATED,   /* known not to exist */
    UNKNOWN,       /* known to exist, but no status known. probably it is changing. */
    CHANGING,
    FAIL;
}
