package be.iminds.ilabt.jfed.lowlevel.authority;

import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities;
import be.iminds.ilabt.jfed.util.IOUtils;

import javax.xml.bind.*;
import java.io.*;

/**
 * StoredAuthorityList
 */
public class StoredAuthorityList {
    static void load(File file, AuthorityListModel authorityListModel) {
        //not important if it doesn't exist
        if (!file.exists()) return;

        try {
            load(new FileInputStream(file), authorityListModel);
        } catch (FileNotFoundException e) {
            System.err.println("WARNING: Error reading authorities from XML file \"" + file + "\": " + e.getMessage());
            return;
        }
    }

    static void load(InputStream is, AuthorityListModel authorityListModel) {
        try {
            Class docClass = be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.class;
            String packageName = docClass.getPackage().getName();
            JAXBContext jc = JAXBContext.newInstance(packageName);
            Unmarshaller u = jc.createUnmarshaller();
//            JAXBElement<Authorities> doc =
//                    (JAXBElement<Authorities>) u.unmarshal(file);
//            be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities c = doc.getValue();
            be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities c = (Authorities) u.unmarshal(is);

            for (Authorities.Authority xmlAuthority : c.getAuthority()) {
                SfaAuthority sfaAuthority = SfaAuthority.fromXml(xmlAuthority);

                //find existing
                SfaAuthority existingSfaAuthority = authorityListModel.getByUrn(sfaAuthority.getUrn());
                if (existingSfaAuthority == null)
                    authorityListModel.addAuthority(sfaAuthority);
                else {
                    boolean replace = false;

                    if (existingSfaAuthority.isWasStored()) replace = true;
                    //TODO handle it more intelligently? (example: compare with source of saved, and let for example USER_PROVIDED always take priority?)
                    if (existingSfaAuthority.getSource() != null) {
                        switch (existingSfaAuthority.getSource()) {
                            case BUILTIN: replace = true; break;
                            case USER_PROVIDED: break;
                            case UTAH_CLEARINGHOUSE: break;
                        }
                    }

                    if (replace) {
                        authorityListModel.removeByUrn(sfaAuthority.getUrn());
                        authorityListModel.addAuthority(sfaAuthority);
                    } else {
                        //update
                        authorityListModel.mergeOrAdd(sfaAuthority);
                    }
                }
            }
        }
//        catch (IOException e) {
//            //TODO: improve
//            System.err.println("WARNING: Error reading authorities xml file \""+file.getName()+"\": "+e.getMessage());
//            e.printStackTrace();
//            return;
//        }
        catch (Exception e) {
            System.err.println("WARNING: Error reading authorities from XML: "+e.getMessage());
            e.printStackTrace();
            return;
        } finally {
            try { is.close(); } catch (IOException e) { /*ignore exceptions when closing*/ }
        }
    }

    static void save(File file, AuthorityListModel authorityListModel) {
        try {
            //first see if the dir exists, and try to create it if not.
            File dir = file.getParentFile();
            if (!dir.exists()) dir.mkdir();
            if (!dir.exists()) {
                System.err.println("WARNING: could not save authorities to \""+file.getName()+"\", as dir \""+dir.getName()+"\" could not be created.");
                return;
            }

            //write to file
            FileWriter writer = new FileWriter(file);
            be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities xmlAuthorities = new be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities();
            for (SfaAuthority auth : authorityListModel.getAuthorities()) {
                be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.Authority xmlAuth = auth.toXml();
                xmlAuthorities.getAuthority().add(xmlAuth);
            }

            JAXBContext context = JAXBContext.newInstance(be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(xmlAuthorities, writer);

            writer.close();
        } catch (JAXBException e) {
            System.err.println("WARNING: Error writing authorities to XML: "+e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("WARNING: Error writing authorities to XML file \""+file.getName()+"\": "+e.getMessage());
            e.printStackTrace();
        }
    }
}
