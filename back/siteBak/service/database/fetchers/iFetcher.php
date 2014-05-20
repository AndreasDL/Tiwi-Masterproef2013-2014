<?php

/**
 * the fetcher will recieve an result from a database then parse it to a hashmap/array.
 * the formatter will then encode the object to json/...
 * this class won't be used for post requests
 * @author Andreas De Lille
 */
interface iFetcher {

    /**
     * This function will fetch & format the result of a list call
     * @param array result the result object from pg_connect
     * @param array data an (empty) array to store the data in.
     * @param array testDefinitions for lookups
     */
    public function fetchList(&$result,&$data,&$testDefinitions);
    
    /**
     * this function will fetch & format the result of a testdefinitionsCall.
     * @param array result the result object from pg_connect
     * @param array data an (empty) array to store the data in.
     */
    public function fetchDefinition(&$result,&$data);  
    
    /**
     * this function will fetch & format the results of a testinstanceCall
     * @param array result the result object from pg_connect
     * @param array data an (empty) array to store the data in.
     */
    public function fetchTestInstance(&$result,&$data);
    
    /**
     * this function will fetch & format the result of a testbedCall.
     * @param array result the result object from pg_connect
     * @param array data an (empty) array to store the data in.
     */
    public function fetchTestbed(&$result,&$data);
    
    /**
     * this function will fetch & format the result of a /user call.
     * @param array $result the result object from pg_connect
     * @param array $data 
     */
    public function fetchUser(&$result,&$data);
}
