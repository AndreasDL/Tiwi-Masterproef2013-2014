package be.iminds.ilabt.jfed.highlevel.model;

import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager2;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Date;

/**
 * Sliver
 */
public class Sliver {
    private Slice slice;
    private SfaAuthority authority;
    private ObjectProperty<RSpecInfo> requestRspec = new SimpleObjectProperty<RSpecInfo>();
    private ObjectProperty<RSpecInfo> manifestRspec = new SimpleObjectProperty<RSpecInfo>();

    //    private AggregateManager2.SliverStatus status;
    private final StringProperty statusString = new SimpleStringProperty("<no status known>");
    private final ObjectProperty<Status> status = new SimpleObjectProperty<Status>();
    private String urn;

    private String allocationStatus;
    private String operationalStatus;
    private Date expires;

    public Sliver(String urn, Slice slice, RSpecInfo requestRspec, RSpecInfo manifestRspec, SfaAuthority authority) {
        this.slice = slice;
        this.urn = urn;
        this.requestRspec.set(requestRspec);
        this.manifestRspec.set(manifestRspec);
        this.authority = authority;
//        status = null;
        status.set(Status.UNINITIALISED);
    }

    public Slice getSlice() {
        return slice;
    }

    public RSpecInfo getManifestRspec() {
        return manifestRspec.get();
    }

    public RSpecInfo getRequestRspec() {
        return requestRspec.get();
    }

    public void setManifestRspec(RSpecInfo manifestRspec) {
//        System.out.println("sliver "+this+" setManifestRspec called");
        this.manifestRspec.set(manifestRspec);
    }

    public void setRequestRspec(RSpecInfo requestRspec) {
        this.requestRspec.set(requestRspec);
    }

    public ObjectProperty<RSpecInfo> manifestRspecProperty() {
        return manifestRspec;
    }

    public SfaAuthority getAuthority() {
        return authority;
    }

//    public AggregateManager2.SliverStatus getStatus() {
//        return status;
//    }
//
//    public void setStatus(AggregateManager2.SliverStatus status) {
//        this.status = status;
//    }



    public String getStatusString() {
        return statusString.get();
    }

    public void setStatusString(String statusString) {
        this.statusString.set(statusString);
    }

    public StringProperty statusStringProperty() {
        return statusString;
    }



    public Status getStatus() {
        return status.get();
    }

    public void setStatus(Status status) {
        this.status.set(status);
    }

    public ObjectProperty<Status> statusProperty() {
        return status;
    }



    public String getUrn() {
        return urn;
    }
    public void setUrn(String urn) {
        slice.getEasyModel().getParameterHistoryModel().addSliverUrn(urn);
        this.urn = urn;
    }

    public void setAllocationStatus(String allocationStatus) {
        this.allocationStatus = allocationStatus;
    }

    public void setOperationalStatus(String operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }
}
