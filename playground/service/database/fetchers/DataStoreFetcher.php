<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of DataStoreFether
 * Returns object that support the GENI Operational Monitoring Project
 * @author Andreas De Lille
 */
class DataStoreFetcher implements iFetcher {

    public function fetchDefinition(&$result, &$data) {
        echo "fetch definition not supported yet!";
    }

    public function fetchList(&$result, &$data, &$testDefinitions) {
        $map = array();
        $ret = array();

        while ($row = pg_fetch_assoc($result)) {
            
            
            if (($testDefinitions[$row['testdefinitionname']]['parameters'][$row['parametername']]['type'] == 'testbed' 
                    || $testDefinitions[$row['testdefinitionname']]['parameters'][$row['parametername']]['type'] == 'testbed[]')) {
                //map testinstanceid => testbed to change final layout
                $map[$row['testinstanceid']] = $row['parametervalue'];
                
                //put in stuffs wich require the testbed parameter
                $ret[$row['testinstanceid']]['id'] = 'is_available:'. $row['parametervalue'];//ophalen uit definities => definities zelf zijn verschillend & zit default niet in object
                $ret[$row['testinstanceid']]['subject'] = $GLOBALS['urlTestbed'] . '?testbedName=' . $row['parametervalue'];
            }
            
            if (!isset($ret[$row['testinstanceid']])) {
                $ret[$row['testinstanceid']] = array(
                    '$schema' => 'http://www.gpolab.bbn.com/monitoring/schema/20140131/data#',  
                    "description" => "Is aggregate manager responsive",
                    "eventType" => "ops_monitoring:is_available",
                    'units' => 'boolean',
                    'tsdata' => array()
                );
            }
            if ($row['returnname'] == 'testGetVersionXmlRpcCorrectness'){
                array_push($ret[$row['testinstanceid']]['tsdata'], 
                    array('ts'=> $row['timestamp'], 
                        'v' => ($row['returnvalue'] == 'SUCCESS' ? '1':'0')
                    )
                );
            }
        }
        
        //testinstanceid weghalen
        foreach ($ret as $instance => $results){
            array_push($data,$results);
        }
        
        
        //'id' => 'is_available:'
        //testbeds goed zetten
 /*{
   "$schema": "http://www.gpolab.bbn.com/monitoring/schema/20140131/data#",
    "id": "is_available:gpo-ig",
    "subject": "https://datastore.instageni.gpolab.bbn.com/info/aggregate/gpo-ig",
    "eventType": "ops_monitoring:is_available",
    "description": "Is aggregate manager responsive",
    "units": "boolean",
    "tsdata": [
      { "ts": 1391198716651283, "v": 1 },
      { "ts": 1391198776651284, "v": 1 },
      { "ts": 1391198836651284, "v": 1 },
      { "ts": 1391198896651284, "v": 1 },
      { "ts": 1391198956651284, "v": 1 },
      { "ts": 1391199016651285, "v": 1 }
    ]
 }*/

 
}

    public function fetchTestInstance(&$result, &$data) {
        echo "fetch instance not supported yet!";
    }

    public function fetchTestbed(&$result, &$data) {
        echo "fetch testbed not supported yet!";
    }

}
