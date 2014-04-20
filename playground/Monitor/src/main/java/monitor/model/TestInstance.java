/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author drew
 */
public class TestInstance {
    private String testname,testdefinitionname;
    private int frequency;
    private HashMap<String,ArrayList<String>> parameters;
    private String testinstanceid;
    private boolean enabled;
    private Timestamp lastrun;

    public Date getLastrun() {
        return lastrun;
    }
    
    public String getTestInstanceId() {
        return testinstanceid;
    }
    public boolean isEnabled(){
        return enabled;
    }
    public boolean isScheduled(){
        if (lastrun != null){
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(lastrun.getTime());
            cal.add(Calendar.SECOND, frequency);
            Timestamp later = new Timestamp(cal.getTime().getTime());
            Timestamp now = new Timestamp(System.currentTimeMillis());

            //System.out.println("last: " + lastrun + " next: " + later + " now: "+now);

            return now.after(later);
        }else{
            //null => instance has never run
            return true;
        }
    }
    
    public void setTestInstanceId(String instanceId) {
        this.testinstanceid = instanceId;
    }


    public String getTestname() {
        return testname;
    }

    public void setTestname(String testname) {
        this.testname = testname;
    }

    public String getTestDefinitionName() {
        return testdefinitionname;
    }

    public void setTestDefinitionName(String testdefinitionname) {
        this.testdefinitionname = testdefinitionname;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public HashMap<String, ArrayList<String>> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, ArrayList<String>> parameters) {
        this.parameters = parameters;
    }

}
