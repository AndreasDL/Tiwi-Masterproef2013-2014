package be.iminds.ilabt.jfed.log;

import java.util.ArrayList;
import java.util.List;

/**
 * Logger receives all results from servers and passes them on to interested listeners.
 *
 * This mechanism allows various classes to derive data from client actions, without initiating calls themselves.
 */
public class Logger {
    protected List<ResultListener> resultListeners = new ArrayList<ResultListener>();

    /**
     * fireResult returns after all results have been processed
     * @param reply
     */
    public synchronized void fireResult(ApiCallDetails reply) {
        for (ResultListener l : resultListeners)
            l.onResult(reply);
    }
    public synchronized void addResultListener(ResultListener l) {
        resultListeners.add(l);
    }
    public synchronized void removeResultListener(ResultListener l){
        resultListeners.remove(l);
    }



    /**
     * returns a new logger object, that share the listeners of the original (even when updated),
     * and accepts new listeners that are not in the original object. */
    public Logger getWrappingLogger() {
        final Logger parentLogger = this;
        Logger res = new Logger() {
            @Override
            public synchronized void fireResult(ApiCallDetails reply) {
                for (ResultListener l : parentLogger.resultListeners)
                    l.onResult(reply);
                for (ResultListener l : resultListeners)
                    l.onResult(reply);
            }
        };
        return res;
    }
    /**
     * @param l the listener to be added initially. may be null (no initial listener added in that case)
     * */
    public Logger getWrappingLogger(ResultListener l) {
        Logger res = getWrappingLogger();
        if (l != null)
            res.addResultListener(l);
        return res;
    }
}
