package be.iminds.ilabt.jfed.util;

import be.iminds.ilabt.jfed.highlevel.controller.TaskThread;
import be.iminds.ilabt.jfed.log.ApiCallDetails;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.log.ResultListener;
import javafx.application.Platform;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * JavaFXLogger exactly the same as Logger, but always handles events in JavaFX thread
 */
public class JavaFXLogger extends Logger {
    private boolean synchronous;
    /**
     * @param synchronous should fireResult be synchronous, or should it return after all results have been processed
     * */
    public JavaFXLogger(boolean synchronous) {
        this.synchronous = synchronous;
    }


    @Override
    public synchronized void fireResult(final ApiCallDetails reply) {
        if (!synchronous) {
            //asynchronous event processing
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    for (ResultListener l : resultListeners)
                        l.onResult(reply);
                }
            });
            return;
        }

        if (Platform.isFxApplicationThread())
            for (ResultListener l : resultListeners)
                l.onResult(reply);
        else {
            //synchronous event processing
            final Lock lock = new ReentrantLock();
            final Condition waiter = lock.newCondition();

            Platform.runLater(new Runnable() { @Override public void run() {

//                    JavaFXLogger.super.fireResult(reply);
                for (ResultListener l : resultListeners)
                    l.onResult(reply);

                lock.lock();
                waiter.signalAll();
                lock.unlock();
            } });

            lock.lock();
            try {
                waiter.await();
            } catch (InterruptedException e) {
                //TODO: handle?
                e.printStackTrace();
            }
            lock.unlock();

        }
    }

    private static class MyWrappingJavaFXLogger extends JavaFXLogger {
        private final JavaFXLogger parentLogger;
        public MyWrappingJavaFXLogger(boolean synchronous, JavaFXLogger parentLogger) { super(synchronous); this.parentLogger = parentLogger; }
        @Override
        public synchronized void fireResult(ApiCallDetails reply) {
            //fire for all parentLogger listeners, even if they changed
            parentLogger.fireResult(reply);

            //fire for any listener added to this new wrapping logger
            super.fireResult(reply);
        }
    }

    @Override
    public JavaFXLogger getWrappingLogger() {
        return new MyWrappingJavaFXLogger(synchronous, this);
    }

    public static Logger wrap(final JavaFXLogger logger) {
        return wrap(logger, null);
    }
    public static JavaFXLogger wrap(final JavaFXLogger logger, final ResultListener resultListener) {
        JavaFXLogger res = new MyWrappingJavaFXLogger(logger.synchronous, logger);
        if (resultListener != null)
            res.addResultListener(resultListener);
        return res;
    }
}
