package be.iminds.ilabt.jfed.rspec;

import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.NodeContents;
import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.RSpecContents;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * TestRspecJAXB
 */
public class TestRspecJAXB {
    public static void main(String [] args) throws FileNotFoundException, JAXBException {
        String filename = "/storage/wim/proj/fed4fire/Flack-examples/wall3/rspec-request-2node-gbitlink.xml";

        Class docClass = RSpecContents.class;
        String packageName = docClass.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance( packageName );
        Unmarshaller u = jc.createUnmarshaller();
        JAXBElement<RSpecContents> doc = (JAXBElement<RSpecContents>) u.unmarshal( new FileInputStream(new File(filename)));
        RSpecContents c = doc.getValue();
        System.out.println(c);
        System.out.println("generated="+c.getGenerated());
        JAXBElement first =(JAXBElement) c.getAnyOrNodeOrLink().get(0);
        NodeContents nc = (NodeContents) first.getValue();
        System.out.println("1st el client_id="+nc.getClientId());
        System.out.println("type="+c.getType().value());
    }
}
