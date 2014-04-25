/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package monitor;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import monitor.model.TestResult;

/**
 *
 * @author drew
 */
public class ResultUploader implements Runnable{
    WebServiceAccess webAccess;
    private BlockingDeque<TestResult> resultsToSend;
    private Boolean stopping;
    
    public ResultUploader(WebServiceAccess webAccess) {
        this.webAccess = webAccess;
        resultsToSend = new LinkedBlockingDeque<>();
        this.stopping = false;
    }
    public synchronized void addResultToQueue(TestResult r){
        resultsToSend.addLast(r);
    }
    
    
    @Override
    public void run() {
        while(!stopping || resultsToSend.size() > 0){
            try {
                //TestResult r = resultsToSend.takeFirst();//bug keeps waiting even if there are no first tasks.
                TestResult r = resultsToSend.pollFirst(100,TimeUnit.MILLISECONDS);//check & wait max 100 ms than rerun the run method so it can check if stopping is set
                if (r != null){
                    if (! r.isLoadTest())
                        webAccess.updateNextRun(r);
                    webAccess.addResult(r);
                }
            } catch (InterruptedException ex) {
            }
        }
        
        this.stop();
    }
    
    public void stop(){
        this.stopping = true;
    }
}
