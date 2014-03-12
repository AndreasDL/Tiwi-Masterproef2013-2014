<?php

//new databaseAccess();

class AccessDummyDatabase {
    private $data;

    public function __construct() {
        

        $this->data = array();

        #pings from diff testbeds
        $temp = array();
        for ($i = 0; $i < 3; $i++) {
            $test = array();
            $test['testname'] = 'ping';
            $test['testbedId'] = "urn-testbed$i";
            $test['planId'] = 103 + $i;
            $test['resultId'] = 1452 + $i;
            $test['log'] = "http://www." . $test['testbedId'] . "com/Logs/" . $test['testname'] . "/log" . $test['resultId'];
            $test['results'] = array();
            $test['timestamp'] = "2014-03-14 18:03:".(37+$i);
            $subResult = array();
            $subResult['name'] = 'pingValue';
            $subResult['value'] = rand(20, 73);
            array_push($test['results'], $subResult);
            array_push($temp, $test);
        }
        $this->data['difpings'] = $temp;

        #pings from same testbed
        $temp = array();
        for ($i = 0; $i < 3; $i++) {
            $test = array();
            $test['testname'] = 'ping';
            $test['testbedId'] = "urn-testbed0";
            $test['planId'] = 103;
            $test['resultId'] = 1498 + $i;
            $test['log'] = "http://www." . $test['testbedId'] . "com/Logs/" . $test['testname'] . "/log" . $test['resultId'];
            $test['results'] = array();
            $test['timestamp'] = "2014-03-14 18:08:".(37+$i);
            $subResult = array();
            $subResult['name'] = 'pingValue';
            $subResult['value'] = rand(20, 93);
            array_push($test['results'], $subResult);
            array_push($temp, $test);
        }
        $this->data['pings'] = $temp;

        #stitches
        $temp = array();
        for ($i = 0; $i < 3; $i++) {
            $test = array();
            $test['testname'] = 'stitching-name' . $i;
            $test['testbeds'] = array();
            for ($j = 0; $j < 3; $j++) {
                array_push($test['testbeds'], "urn-testbed$j");
            }
            $test['planId'] = 78 + $i;
            $test['resultId'] = 2358 + $i;
            $test['log'] = "http://www." . $test['testname'] . "com/Logs/" . $test['testname'] . "/log" . $test['resultId'];
            $test['results'] = array();
            $test['timestamp'] = "2014-03-14 18:03:".(37+$i);
            $subs = array('setup', 'getUserCredential', 'generateRspec', 'createSlice', 'initStitching', 'callSCS', 'callCreateSlivers', 'waitForAllReady', 'loginAndPing', 'callDeletes');
            for ($j = 0; $j < sizeof($subs); $j++) {
                $subResult = array();
                $subResult['name'] = $subs[$j];
                $subResult['value'] = 'succes';
                array_push($test['results'], $subResult);
            }
            array_push($temp, $test);
        }
        $this->data['stitching'] = $temp;

        #tests
        #description
        $temp = array();
        $test = array();
        $test['definitionId'] = 'ping';
        $test['command'] = 'ping';
        $test['parameters'] = array(array('name' => 'timeout', 'type' => 'int'),
            array('name' => 'testbed', 'type', 'string'));
        $test['return'] = array(array('name' => 'pingValue',
                'type' => 'int',
                'description' => 'pingValue'));
        array_push($temp, $test);
        $test = array();
        $test['definitionId'] = 'stitching';
        $test['command'] = 'stitch';
        $test['parameters'] = array(array('name' => 'topology', 'type' => 'string'),
            array('name' => 'testbeds', 'type' => 'testbed[]'));
        $test['return'] = array(
            array('name' => 'setup', 'type' => 'string', 'description' => 'succes?'),
            array('name' => 'getUserCredential', 'type' => 'string', 'description' => 'succes?'),
            array('name' => 'generateRspec', 'type' => 'string', 'description' => 'succes?'),
            array('name' => 'createSlice', 'type' => 'string', 'description' => 'succes?'),
            array('name' => 'initStitching', 'type' => 'string', 'description' => 'succes?'),
            array('name' => 'callSCS', 'type' => 'string', 'description' => 'succes?'),
            array('name' => 'callCreateSlivers', 'type' => 'string', 'description' => 'succes?'),
            array('name' => 'waitForAllReady', 'type' => 'string', 'description' => 'succes?'),
            array('name' => 'loginAndPing', 'type' => 'string', 'description' => 'succes?'),
            array('name' => 'callDeletes', 'type' => 'string', 'description' => 'succes?'));
        array_push($temp, $test);
        $this->data['testDescription'] = $temp;

        #testplan
        $temp = array();
        for ($i = 0; $i < 3; $i++) {
            $test = array();
            $test['definitionId'] = 'ping';
            $test['parameters'] = array(array('name' => 'timeout', 'value' => rand(100, 136)),
                array('name' => 'testbed', 'value' => 'urn-testbed' . $i));
            $test['frequency'] = rand(60, 3600);
            $test['planId'] = $i;
            array_push($temp, $test);
        }
        for ($i = 3; $i < 6; $i++) {
            $test = array();
            $test['definitionId'] = 'stitching';
            $test['parameters'] = array(array('name' => 'topology', 'value' => 'ring'),
                array('name' => 'testbeds', 'value' => array('urn-testbed0', 'urn-testbed3')));
            $test['frequency'] = rand(3600, 10800);
            $test['planId'] = $i;
            array_push($temp, $test);
        }
        $this->data['testplan'] = $temp;

        $this->data['parameterGroup'] = $temp;
    }

    public function getLast($params) {
        return $this->data['difpings'];
    }

    public function getDetail($params) {
        return $this->data['stitching'][2];
    }

    public function getAverage($param) {
        $avg = 0;
        foreach ($this->data['pings'] as $ping) {
            $avg += $ping['results'][0]['value'];
        }
        $test = array();
        $test['testname'] = 'ping-average';
        $test['testbedId'] = "urn-testbed0";
        $test['results'] = array();
            $subResult = array();
            $subResult['name'] = 'average-pingValue';
            $subResult['value'] = $avg / sizeof($this->data['pings']);
        array_push($test['results'], $subResult);
        $test['from'] = "2014-03-1 18:03:37";
        $test['till'] = "2014-03-11 18:03:37";
        return $test;
    }

    public function getList($param) {
        return $this->data['pings'];
    }

    public function getTestDescription($testDescriptionId) {
        return $this->data['testDescription'][1];
    }

    public function getTestPlan($testPlanId) {
        return $this->data['testplan'][2];
    }

    public function getTestDescriptionList() {
        return $this->data['testDescription'];
    }

    public function getTestPlanList() {
        return $this->data['testplan'];
    }

}
