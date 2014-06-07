package be.iminds.ilabt.jfed.highlevel.model;

import be.iminds.ilabt.jfed.util.JFedJavaFXBindings;
import javafx.beans.binding.Bindings;
import javafx.collections.*;

import java.util.HashSet;

/**
 * ParameterHistoryModel contains credentials, urns, and other info that is usefull to fill in the fields of jFed Probe.
 * The EasyModelcontains an instance and adds to it, and the user can also manually add to it.
 */
public class ParameterHistoryModel {
    private class ObservableListSetLink<T> {
        final ObservableSet<T> set;
        final ObservableList<T> list;

        T adding = null;
        T removing = null;

        public ObservableListSetLink(final ObservableSet<T> set, final ObservableList<T> list) {
            this.set = set;
            this.list = list;
            set.addListener(new SetChangeListener<T>() {
                @Override
                public void onChanged(Change<? extends T> change) {
                    if (change.wasAdded()) {
                        T a = change.getElementAdded();
                        if (adding != null) {
                            assert adding == a;
                        } else {
                            adding = a;
                            list.add(adding);
                            adding = null;
                        }
                    }
                    if (change.wasRemoved()) {
                        T r = change.getElementAdded();
                        if (removing != null) {
                            assert removing == r;
                        } else {
                            removing = r;
                            list.remove(removing);
                            removing = null;
                        }
                    }
                }
            });

            list.addListener(new ListChangeListener<T>() {
                @Override
                public void onChanged(Change<? extends T> change) {
                    while (change.next()) {
                        for (T a : change.getAddedSubList()) {
                            if (adding != null) {
                                assert adding == a;
                            } else {
                                adding = a;
                                set.add(adding);
                                adding = null;
                            }
                        }
                        for (T r : change.getRemoved()) {
                            if (removing != null) {
                                assert removing == r;
                            } else {
                                removing = r;
                                set.remove(removing);
                                removing = null;
                            }
                        }
                    }
                }
            });
        }
    }


    public ParameterHistoryModel() {
        new ObservableListSetLink(userCredentialInfos, userCredentialInfosList);
        new ObservableListSetLink(sliceCredentialInfos, sliceCredentialInfosList);
        new ObservableListSetLink(uncategorizedCredentiaInfos, uncategorizedCredentiaInfosList);
        new ObservableListSetLink(sliceUrns, sliceUrnsList);
        new ObservableListSetLink(sliverUrns, sliverUrnsList);
        new ObservableListSetLink(userUrns, userUrnsList);
    }


    public void seeUrn(String urn) {
        if (urn.startsWith("urn:publicid:IDN+")) {
            if (urn.contains("+user+"))
                addUserUrn(urn);
            if (urn.contains("+slice+"))
                addSliceUrn(urn);
            if (urn.contains("+sliver+"))
                addSliverUrn(urn);
        }
    }
    public void seeUrnsInCredential(CredentialInfo credentialInfo) {
        try {
            seeUrn(credentialInfo.getCredential().getTargetUrn());
            seeUrn(credentialInfo.getCredential().getOwnerUrn());
        } catch (Throwable e) { } //ignore
    }


    private final ObservableSet<CredentialInfo> userCredentialInfos = FXCollections.observableSet(new HashSet<CredentialInfo>());
    private final ObservableList<CredentialInfo> userCredentialInfosList = FXCollections.observableArrayList();
    public ObservableSet<CredentialInfo> getUserCredentials() {
        return userCredentialInfos;
    }
    public ObservableList<CredentialInfo> getUserCredentialsList() {
        return userCredentialInfosList;
    }
    public void addUserCredential(CredentialInfo userCredentialInfo) {
        seeUrnsInCredential(userCredentialInfo);
        userCredentialInfos.add(userCredentialInfo);
    }

    private final ObservableSet<CredentialInfo> sliceCredentialInfos = FXCollections.observableSet(new HashSet<CredentialInfo>());
    private final ObservableList<CredentialInfo> sliceCredentialInfosList = FXCollections.observableArrayList();
    public ObservableSet<CredentialInfo> getSliceCredentials() {
        return sliceCredentialInfos;
    }
    public ObservableList<CredentialInfo> getSliceCredentialsList() {
        return sliceCredentialInfosList;
    }
    public void addSliceCredential(CredentialInfo sliceCredentialInfo) {
        seeUrnsInCredential(sliceCredentialInfo);
        sliceCredentialInfos.add(sliceCredentialInfo);
    }

    private final ObservableSet<CredentialInfo> uncategorizedCredentiaInfos = FXCollections.observableSet(new HashSet<CredentialInfo>());
    private final ObservableList<CredentialInfo> uncategorizedCredentiaInfosList = FXCollections.observableArrayList();
    public ObservableSet<CredentialInfo> getUncategorizedCredentias() {
        return uncategorizedCredentiaInfos;
    }
    public ObservableList<CredentialInfo> getUncategorizedCredentiasList() {
        return uncategorizedCredentiaInfosList;
    }
    public void addUncategorizedCredentia(CredentialInfo uncategorizedCredentiaInfo) {
        seeUrnsInCredential(uncategorizedCredentiaInfo);
        uncategorizedCredentiaInfos.add(uncategorizedCredentiaInfo);
    }

    /** getAnyCredentialsList
     * @return the concatenation of the user, slice and uncategorized credential lists */
    public ObservableList<CredentialInfo> getAnyCredentialsList() {
        return JFedJavaFXBindings.union(userCredentialInfosList, sliceCredentialInfosList, uncategorizedCredentiaInfosList);
    }



    public ObservableList<String> getAllUrnList() {
        return JFedJavaFXBindings.union(sliceUrnsList, sliverUrnsList, userUrnsList);
    }



    private final ObservableSet<String> sliceUrns = FXCollections.observableSet(new HashSet<String>());
    private final ObservableList<String> sliceUrnsList = FXCollections.observableArrayList();
    public ObservableSet<String> getSliceUrns() {
        return sliceUrns;
    }
    public ObservableList<String> getSliceUrnsList() {
        return sliceUrnsList;
    }
    public void addSliceUrn(String urn) {
        if (urn != null)
        sliceUrns.add(urn);
    }



    private final ObservableSet<String> sliverUrns = FXCollections.observableSet(new HashSet<String>());
    private final ObservableList<String> sliverUrnsList = FXCollections.observableArrayList();
    public ObservableSet<String> getSliverUrns() {
        return sliverUrns;
    }
    public ObservableList<String> getSliverUrnsList() {
        return sliverUrnsList;
    }
    public void addSliverUrn(String urn) {
        if (urn != null)
            sliverUrns.add(urn);
    }


    private final ObservableSet<String> userUrns = FXCollections.observableSet(new HashSet<String>());
    private final ObservableList<String> userUrnsList = FXCollections.observableArrayList();
    public ObservableSet<String> getUserUrns() {
        return userUrns;
    }
    public ObservableList<String> getUserUrnsList() {
        return userUrnsList;
    }
    public void addUserUrn(String urn) {
        if (urn != null)
            userUrns.add(urn);
    }
}
