/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.model;

/**
 *
 * @author Andreas De Lille
 */
public class User {
    private String username,passwordfilename,pemkeyandcertfilename,userauthorityurn;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getUserauthorityurn() {
        return userauthorityurn;
    }

    public void setUserauthorityurn(String userauthorityurn) {
        this.userauthorityurn = userauthorityurn;
    }
    
    
}
