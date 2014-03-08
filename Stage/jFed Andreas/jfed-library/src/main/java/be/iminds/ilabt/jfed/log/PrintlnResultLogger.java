package be.iminds.ilabt.jfed.log;

/**
 * PrintlnResultLogger
 */
public class PrintlnResultLogger implements ResultListener {
    @Override
    public void onResult(ApiCallDetails result) {
        System.out.println(result);
    }
}
