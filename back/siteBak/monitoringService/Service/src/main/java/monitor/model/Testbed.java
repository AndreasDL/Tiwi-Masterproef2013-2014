/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.model;

/**
 *this class respresents a testbed
 * @author Andreas De Lille
 */
public class Testbed {
    private String testbedName,url,urn,userauthorityurn,passwordfilename,pemkeyandcertfilename,username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTestbedName() {
        return testbedName;
    }

    public void setTestbedName(String testbedName) {
        this.testbedName = testbedName;
    }

    public String getUserauthorityurn() {
        return userauthorityurn;
    }

    public void setUserauthorityurn(String userauthorityurn) {
        this.userauthorityurn = userauthorityurn;
    }



    public String getPasswordfilename() {
        return passwordfilename;
    }

    public void setPasswordfilename(String passwordfilename) {
        this.passwordfilename = passwordfilename;
    }

    public String getPemkeyandcertfilename() {
        return pemkeyandcertfilename;
    }

    public void setPemkeyandcertfilename(String pemkeyandcertfilename) {
        this.pemkeyandcertfilename = pemkeyandcertfilename;
    }

    /**
 * returns the name of the testbed
 * @return 
 */
    public String getestbedName() {
        return testbedName;
    }
/**
 * returns the url of the testbed
 * @return 
 */
    public String getUrl() {
        return url;
    }
/**
 * returns the urn of the testbed
 * @return 
 */
    public String getUrn() {
        return urn;
    }
    
}
