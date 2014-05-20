package be.iminds.ilabt.jfed.ui.javafx.probe_gui.command_arguments;

import be.iminds.ilabt.jfed.highlevel.model.AuthorityInfo;
import be.iminds.ilabt.jfed.highlevel.model.EasyModel;
import be.iminds.ilabt.jfed.lowlevel.ApiMethodParameter;
import be.iminds.ilabt.jfed.lowlevel.ApiMethodParameterType;
import be.iminds.ilabt.jfed.lowlevel.api.AbstractUniformFederationApi;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceId;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceUrn;
import be.iminds.ilabt.jfed.ui.javafx.probe_gui.ProbeController;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CommandArgumentChooser
 */
public abstract class CommandArgumentChooser<T> extends VBox {
    protected ReadOnlyProperty<T> value;
    public ReadOnlyProperty<T> valueProperty() {
        return value;
    }

    public enum CredentialSubject { USER, SLICE, ANY };

    public CommandArgumentChooser() {
        setMaxWidth(10000.0);
    }

    public static ApiMethodParameterType deriveType(String parameterName, Class parameterClass, ApiMethodParameter annotation,
                ProbeController.MethodInfo methodInfo) {
        if (annotation.name().equals("credentialList"))
            return ApiMethodParameterType.LIST_OF_CREDENTIAL;

        if (annotation.name().equals("slice") || annotation.name().equals("sliceUrn"))
            return ApiMethodParameterType.SLICE_URN;

        if (annotation.name().equals("urns"))
            return ApiMethodParameterType.LIST_OF_URN_STRING;


        if (annotation.name().equals("userCredential"))
            return ApiMethodParameterType.USER_CREDENTIAL_STRING;

        if (annotation.name().equals("credential"))
            return ApiMethodParameterType.CREDENTIAL_STRING;

        if (annotation.name().equals("sliceCredential"))
            return ApiMethodParameterType.SLICE_CREDENTIAL_STRING;

        if (annotation.name().equals("clearingHouseCredential"))
            return ApiMethodParameterType.CREDENTIAL_STRING;

        if (annotation.name().equals("UserSpecList") || annotation.name().equals("users"))
            return ApiMethodParameterType.LIST_OF_USERSPEC;


        if (annotation.name().equals("user") || annotation.name().equals("userUrn"))
            return ApiMethodParameterType.USER_URN;

        if (annotation.name().equals("rspec"))
            return ApiMethodParameterType.RSPEC_STRING;

        if (parameterClass.equals(ResourceId.class))
            return ApiMethodParameterType.URN;

        if (parameterClass.equals(ResourceUrn.class))
            return ApiMethodParameterType.URN;

        if (parameterClass.equals(String.class))
            if (annotation.multiLineString())
                return ApiMethodParameterType.STRING_MULTILINE;
            else
                return ApiMethodParameterType.STRING;

        if (parameterClass.equals(Boolean.class))
            return ApiMethodParameterType.BOOLEAN;

        if (parameterClass.equals(Integer.class))
            return ApiMethodParameterType.INTEGER;

        return ApiMethodParameterType.NOT_SPECIFIED;
    }

    public static <T> CommandArgumentChooser<T> getCommandArgumentChooser(
            String parameterName,
            Class<T> parameterClass,
            ApiMethodParameter annotation,
            EasyModel easyModel,
            ProbeController.MethodInfo methodInfo) {
        AuthorityInfo userAuth = null;
        if (easyModel.getGeniUserProvider().isUserLoggedIn()) {
            SfaAuthority sfaAuth = easyModel.getGeniUserProvider().getLoggedInGeniUser().getUserAuthority();
            userAuth = easyModel.getAuthorityList().get(sfaAuth);
        }

        ApiMethodParameterType paramType = ApiMethodParameterType.NOT_SPECIFIED;

        paramType = annotation.parameterType();

        if (paramType == ApiMethodParameterType.NOT_SPECIFIED)
            paramType = deriveType(parameterName, parameterClass, annotation, methodInfo);

        switch (paramType) {
            case STRING: { return (CommandArgumentChooser<T>) new StringArgumentChooser(annotation.guiDefault().equals("") ? null : annotation.guiDefault()); }
            case STRING_MULTILINE: { return (CommandArgumentChooser<T>) new MultiLineStringArgumentChooser(annotation.guiDefault().equals("") ? null : annotation.guiDefault()); }
            case INTEGER: {
                Integer defaultValue = null;
                if (!annotation.guiDefault().equals(""))
                    defaultValue = Integer.parseInt(annotation.guiDefault());
                return (CommandArgumentChooser<T>) new IntegerArgumentChooser(defaultValue);
            }
            case BOOLEAN: {
                Boolean defaultValue = null;
                if (!annotation.guiDefault().equals("")) {
                    defaultValue = false;
                    if (annotation.guiDefault().equalsIgnoreCase("true"))
                        defaultValue = true;
                    if (annotation.guiDefault().equalsIgnoreCase("yes"))
                        defaultValue = true;
                    if (annotation.guiDefault().equalsIgnoreCase("1"))
                        defaultValue = true;
                }
                return (CommandArgumentChooser<T>) new BooleanArgumentChooser(defaultValue);
            }
            case CREDENTIAL_STRING: { return (CommandArgumentChooser<T>) new CredentialArgumentChooser(easyModel, CredentialSubject.ANY); }
            case USER_CREDENTIAL_STRING: { return (CommandArgumentChooser<T>) new CredentialArgumentChooser(easyModel, CredentialSubject.USER); }
            case SLICE_CREDENTIAL_STRING: { return (CommandArgumentChooser<T>) new CredentialArgumentChooser(easyModel, CredentialSubject.SLICE); }
            case URN: {  return (CommandArgumentChooser<T>) new StringArgumentProvidedOptionsChooser(easyModel.getParameterHistoryModel().getAllUrnList()); }
            case SLICE_URN: { return (CommandArgumentChooser<T>) new UrnArgumentProvidedOptionsChooser(easyModel.getParameterHistoryModel().getSliceUrnsList(), "slice", userAuth); }
            case USER_URN: { return (CommandArgumentChooser<T>) new UrnArgumentProvidedOptionsChooser(easyModel.getParameterHistoryModel().getUserUrnsList(), "user", userAuth); }
            case RSPEC_STRING: {
                String defaultRspec = "<rspec type=\"request\" generated=\"2013-01-16T14:20:39Z\" xsi:schemaLocation=\"http://www.geni.net/resources/rspec/3 http://www.geni.net/resources/rspec/3/request.xsd \" xmlns:client=\"http://www.protogeni.net/resources/rspec/ext/client/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.geni.net/resources/rspec/3\">\n" +
                        "  <node client_id=\"PC\" component_manager_id=\"urn:publicid:IDN+"+
                        (easyModel.getGeniUserProvider().isUserLoggedIn() ? easyModel.getGeniUserProvider().getLoggedInGeniUser().getUserAuthority().getNameForUrn() : "<AM>")+
                        "+authority+cm\" exclusive=\"true\">\n" +
                        "    <sliver_type name=\"raw-pc\"/>\n" +
                        "  </node>\n" +
                        "</rspec>\n";
                return (CommandArgumentChooser<T>) new MultiLineStringArgumentChooser(annotation.guiDefault().equals("") ? defaultRspec : annotation.guiDefault());
            }
            case LIST_OF_CREDENTIAL: { return (CommandArgumentChooser<T>) new MultiCredentialArgumentChooser(easyModel, CredentialSubject.ANY); }
            case LIST_OF_URN_STRING: { return (CommandArgumentChooser<T>) new MultiStringArgumentProvidedOptionsChooser(easyModel.getParameterHistoryModel().getAllUrnList()); }
            case LIST_OF_USERSPEC: { return (CommandArgumentChooser<T>) new UserSpecListArgumentChooser(easyModel); }
            case LIST_OF_STRING: { return (CommandArgumentChooser<T>) new MultiStringArgumentProvidedOptionsChooser(FXCollections.<String>observableArrayList()); }
            case LIST_OF_SLICE_URN_STRING: { return (CommandArgumentChooser<T>) new MultiStringArgumentProvidedOptionsChooser(easyModel.getParameterHistoryModel().getSliceUrnsList()); }
            case LIST_OF_USER_URN_STRING: { return (CommandArgumentChooser<T>) new MultiStringArgumentProvidedOptionsChooser(easyModel.getParameterHistoryModel().getUserUrnsList());  }

            //TODO: smart selection of specific API fields
            //TODO: if GetVersion reply cached, use all fields from it
            case CH_API_FILTER: { return (CommandArgumentChooser<T>)
                    new MultiStringArgumentProvidedOptionsChooser(FXCollections.observableArrayList(getChApiFields(methodInfo))); }
            case CH_API_MATCH: { return (CommandArgumentChooser<T>)
                    new ChApiMatchArgumentChooser(getChApiFieldsMap(methodInfo)); } //TODO: allow multiple values per field! And make a more convenient GUI for searching using this.
            case CH_API_FIELDS: { return (CommandArgumentChooser<T>)
                    new MapStringToStringArgumentChooser(getChApiFieldsMap(methodInfo), "Field", "Value"); }


            case LIST_OF_URN: { return (CommandArgumentChooser<T>) new MultiUrnArgumentProvidedOptionsChooser(easyModel.getParameterHistoryModel().getAllUrnList()); }
            case LIST_OF_SLICE_URN: { return (CommandArgumentChooser<T>) new MultiUrnArgumentProvidedOptionsChooser(easyModel.getParameterHistoryModel().getSliceUrnsList()); }
            case LIST_OF_USER_URN: { return (CommandArgumentChooser<T>) new MultiUrnArgumentProvidedOptionsChooser(easyModel.getParameterHistoryModel().getUserUrnsList()); }


            case GENI_EXTRA_OPTIONS: { return (CommandArgumentChooser<T>) new MapStringToStringArgumentChooser(new HashMap<String, String>()); }

            case CH_API_LIST_MEMBER_TUPLES: { return (CommandArgumentChooser<T>) new ChApiMemberArgumentChooser(null/* todo cached members*/); }

            case MAP_OF_STRING_TO_STRING: { return (CommandArgumentChooser<T>) new MapStringToStringArgumentChooser(new HashMap<String, String>()); }
            case MAP_OF_STRING_TO_OBJECT: { return new UnsupportedArgumentChooser<T>(parameterClass); }

            case NOT_SPECIFIED:
            default: { return new UnsupportedArgumentChooser<T>(parameterClass); }
        }
    }

    public static Map<String, String> getChApiFieldsMap(ProbeController.MethodInfo methodInfo) {
        if (methodInfo.api instanceof AbstractUniformFederationApi) {
            AbstractUniformFederationApi gch = (AbstractUniformFederationApi) methodInfo.api;
            return gch.getMinimumFieldsMap(methodInfo.method);
        }
        return null;
    }

    public static List<String> getChApiFields(ProbeController.MethodInfo methodInfo) {
        if (methodInfo.api instanceof AbstractUniformFederationApi) {
            AbstractUniformFederationApi gch = (AbstractUniformFederationApi) methodInfo.api;
            return gch.getMinimumFieldNames(methodInfo.method);
        }
        return null;
    }
}
