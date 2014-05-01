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
        //echo "fetch definition not supported yet!";
        while ($row = pg_fetch_assoc($result)) {
            //if (!isset($data[$row['testtype']])) {
            if (!isset($data[$row['testdefinitionname']])) {
                $data[$row['testdefinitionname']] = array(
                    'testdefinitionname' => $row['testdefinitionname'],
                    'testtype' => $row['testtype'],
                    'testcommand' => $row['testcommand'],
                    'genidatastoretestname' => $row['genidatastoretestname'],
                    'genidatastoredesc' => $row['genidatastoredesc'],
                    'parameters' => array(),
                    'returnValues' => array()
                );
            }
            $data[$row['testdefinitionname']]['parameters'][$row['parametername']] = array('type' => $row['parametertype'],
                'description' => $row['parameterdescription']
            );

            $data[$row['testdefinitionname']]['returnValues'][$row['returnname']] = array('type' => $row['returntype'],
                'description' => $row['returndescription']);
        }
        
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
                    "description" => $testDefinitions[$row['testdefinitionname']]['genidatastoredesc'],//"Is aggregate manager responsive",
                    "eventType" => $testDefinitions[$row['testdefinitionname']]['genidatastoretestname'],
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
}

    public function fetchTestInstance(&$result, &$data) {
                while ($row = pg_fetch_assoc($result)) {
            //array_push($data, $row);
            if (!isset($data[$row['id']])) {
                $data[$row['id']] = array(
                    'testname' => $row['testname'],
                    //'genidatastoretestname' => $row['genidatastoretestname'],
                    //'genidatastoredesc' => $row['genidatastoredesc'],
                    'testdefinitionname' => $row['testdefinitionname'],
                    'frequency' => $row['frequency'],
                    'enabled' => ($row['enabled'] == "t" ? "True" : "False"),
                    'nextrun' => date("c", strtotime($row['nextrun'])),
                    //'type' => gettype($row['nextrun']),
                    'parameters' => array()
                );
            }
            if (!isset($data[$row['id']]['parameters'][$row['parametername']])) {
                $data[$row['id']]['parameters'][$row['parametername']] = array();
            }

            array_push($data[$row['id']]['parameters'][$row['parametername']], $row['parametervalue']);
        }
    }

    public function fetchTestbed(&$result, &$data) {
            while ($row = pg_fetch_assoc($result)) {
            //array_push($data, $row);
            if (!isset($data[$row['testbedname']])) {
                $data[$row['testbedname']] = array('testbedName' => $row['testbedname'],
                    'url' => $row['url'],
                    'urn' => $row['urn']
                );
            }
        }
    }

}
