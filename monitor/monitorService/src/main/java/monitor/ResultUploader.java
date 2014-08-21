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
 * This class uploads testresults to the webservice.
 * @author Andreas De Lille
 */
public class ResultUploader implements Runnable{
    WebServiceAccess webAccess;
    private BlockingDeque<TestResult> resultsToSend;
    private Boolean stopping;
    
    /**
     * Creates a resultuploader.
     * @param webAccess the webaccessobject that uses this resultuploader.
     */
    public ResultUploader(WebServiceAccess webAccess) {
        this.webAccess = webAccess;
        resultsToSend = new LinkedBlockingDeque<>();
        this.stopping = false;
    }
    /**
     * adds a result to the upload queue
     * @param r the testResult to upload
     */
    public synchronized void addResultToQueue(TestResult r){
        resultsToSend.addLast(r);
    }
    
    /**
     * upload all results in queue untill stopping is set.
     */
    @Override
    public void run() {
        while(!stopping || resultsToSend.size() > 0){
            try {
                //TestResult r = resultsToSend.takeFirst();//bug keeps waiting even if there are no first tasks.
                TestResult r = resultsToSend.pollFirst(100,TimeUnit.MILLISECONDS);//check & wait max 100 ms than rerun the run method so it can check if stopping is set
                if (r != null){
                    webAccess.addResult(r);
                    if (! r.isLoadTest())
                        webAccess.updateNextRun(r);
                }
            } catch (InterruptedException ex) {
            }
        }
        
        this.stop();
    }
    /**
     * sets the boolean to stop this service as soon as the uploads are complete.
     */
    public void stop() {
        this.stopping = true;
    }
}