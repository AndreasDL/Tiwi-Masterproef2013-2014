/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import monitor.model.TestResult;

/**
 *
 * @author drew
 */
public class ResultUploader implements Runnable{
    WebServiceAccess webAccess;
    private BlockingDeque<TestResult> resultsToSend;
    
    public ResultUploader(WebServiceAccess webAccess) {
        this.webAccess = webAccess;
        resultsToSend = new LinkedBlockingDeque<>();
    }
    public void addResultToQueue(TestResult r){
        resultsToSend.addLast(r);
    }
    
    
    @Override
    public void run() {
        while(true){
            try {
                TestResult r = resultsToSend.takeFirst();
                webAccess.addResult(r);
            } catch (InterruptedException ex) {
                //ignore
            }
        }
    }
    
}
