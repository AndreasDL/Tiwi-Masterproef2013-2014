package be.iminds.ilabt.jfed.lowlevel.authority;

import java.io.File;

/**
 * jFedCombinedAuthorityList
 *
 * This combines authority info from various sources, to create the list that is used in all jFed tools.
 */
public class JFedAuthorityList {
    private AuthorityListModel authorityListModel;
    private File file;

    //TODO: place these in a separate util file!
    private static String getUserDataDirectory() {
        return System.getProperty("user.home") + File.separator + ".jFed" + File.separator;
    }

    private JFedAuthorityList() {
        authorityListModel = new AuthorityListModel();

        load();
    }
    public void load() {
        authorityListModel.removeAll();

        file = new File(getUserDataDirectory(), "authorities.xml");

        //first read from the Fed4FireAuthorityList
        boolean fed4fireSuccess = false;
        try {
            fed4fireSuccess = Fed4FireAuthorityList.load(authorityListModel);
        } catch (Throwable e) {
            System.err.println("Something went wrong while trying to fetch Fed4Fire AuthorityList: "+e);
            fed4fireSuccess = false;
        }

        //then overwrite from the default StoredAuthorityList if it exists.

        //although the entries in BuiltinAuthorityList would be overwritten by StoredAuthorityList,
        //   we can't do both, as deleted entries from BuiltinAuthorityList would be restored...
        if (!fed4fireSuccess && !file.exists())
            BuiltinAuthorityList.load(authorityListModel);
        else {
            try {
                StoredAuthorityList.load(file, authorityListModel);
            } catch (Throwable e) {
                System.err.println("Something went wrong while trying to read AuthorityList at "+file+": "+e);
                BuiltinAuthorityList.load(authorityListModel);
            }
        }
    }

    private static JFedAuthorityList instance = null;
    public static JFedAuthorityList getInstance() {
        if (instance == null)
            instance = new JFedAuthorityList();
        return instance;
    }
    public static AuthorityListModel getAuthorityListModel() {
        JFedAuthorityList i = getInstance();
        return i.authorityListModel;
    }

    public void save() {
        System.out.println("Saving jFed authority list to file \""+file+"\"");
        StoredAuthorityList.save(file, authorityListModel);
    }
}
