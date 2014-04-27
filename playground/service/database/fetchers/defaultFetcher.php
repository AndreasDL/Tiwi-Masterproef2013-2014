<?php

/**
 * This class will parse the object to the default layout.
 * @author Andreas De Lille
 */
class defaultFetcher implements iFetcher {

    public function fetchList(&$result, &$data, &$testDefinitions) {
        while ($row = pg_fetch_assoc($result)) {
            if (!isset($data[$row['resultid']])) {
                $data[$row['resultid']] = array(
                    'testinstanceid' => $row['testinstanceid'],
                    //'testtype' => $row['testtype'],
                    'testdefinitionname' => $row['testdefinitionname'],
                    'testname' => $row['testname'],
                    'log' => $row['log'],
                    'timestamp' => date("c", strtotime($row['timestamp'])),
                    'testbeds' => array(),
                    'results' => array()
                );
            }

            if (($testDefinitions[$row['testdefinitionname']]['parameters'][$row['parametername']]['type'] == 'testbed' || $testDefinitions[$row['testdefinitionname']]['parameters'][$row['parametername']]['type'] == 'testbed[]') && !in_array($row['parametervalue'], $data[$row['resultid']]['testbeds'])) {
                array_push($data[$row['resultid']]['testbeds'], $row['parametervalue']);
            }


            //NOTE column names are ALWAYS LOWER CASE
            $data[$row['resultid']]['results'][$row['returnname']] = $row['returnvalue'];
        }
    }

    public function fetchDefinition(&$result, &$data) {
        while ($row = pg_fetch_assoc($result)) {
            //if (!isset($data[$row['testtype']])) {
            if (!isset($data[$row['testdefinitionname']])) {
                $data[$row['testdefinitionname']] = array(
                    'testdefinitionname' => $row['testdefinitionname'],
                    'testtype' => $row['testtype'],
                    'testcommand' => $row['testcommand'],
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

    public function fetchTestInstance(&$result,&$data) {
        while ($row = pg_fetch_assoc($result)) {
            //array_push($data, $row);
            if (!isset($data[$row['id']])) {
                $data[$row['id']] = array(
                    'testname' => $row['testname'],
                    //'testtype' => $row['testtype'],
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
