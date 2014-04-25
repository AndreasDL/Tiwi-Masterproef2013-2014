<?php

include (__DIR__ . "/config.php"); //database config
//include (__DIR__ . "/../Request.php");
include (__DIR__ . "/authentication/defaultGuard.php");


class AccessDatabase {

    private $testbeds;
    private $testDefinitions;
    private $testInstances;

    public function __construct() {
        //cache for faster processing of results
        $this->updateCache();//eventueel op aparty thread steken en zo updaten
    }

    //Result Calls
    public function getLast(&$request) {
        //last => indien count niet gezet => op 1 zetten
        $params = $request->getParameters();
        if (!isset($params['count'])) {
            $params['count'] = array(1);
        }
        $request->setParameters($params);
        return $this->getList($request);
    }
    public function getList(&$request) {
        $params = $request->getParameters();
        $query = "select * from ("
                . "select *,dense_rank() over(partition by testname,testdefinitionname order by timestamp desc) rank from list"
                . ") vv ";

        $paramsForUse = array();
        $eindhaakje = "";

        //testbeds
        $this->addAnyIfNeeded($query, $params, $paramsForUse, "testbed", "list");
        if (sizeof($paramsForUse) > 0) {
            $eindhaakje = ')';
        }
        //testtypes
        //$this->addInIfNeeded($query, $params, $paramsForUse, "testtype", "testtype");
        $this->addInIfNeeded($query, $params, $paramsForUse, "testdefinitionname", "testdefinitionname");
        //status => per subtest nu !!
        $this->addInIfNeeded($query, $params, $paramsForUse, "status", "value");
        //resultid
        $this->addInIfNeeded($query, $params, $paramsForUse, "resultId", "id");
        //testname
        $this->addInIfNeeded($query, $params, $paramsForUse, "testname", "testname");
        //from
        $this->addGreaterThanIfNeeded($query, $params, $paramsForUse, "from", "timestamp");
        //till
        $this->addLowerThanIfNeeded($query, $params, $paramsForUse, "till", "timestamp");
        //count
        $this->addLowerThanIfNeeded($query, $params, $paramsForUse, "count", "rank");
        //haakje van any
        $query.=$eindhaakje;

        //print $query;

        $con = $this->getConnection();
        $result = pg_query_params($con, $query, $paramsForUse);
        $data = array();
        while ($row = pg_fetch_assoc($result)) {
            //array_push($data, $row);

            if (!isset($data[$row['resultid']])) {
                $data[$row['resultid']] = array(
                    'testinstanceid' => $row['testinstanceid'],
                    //'testtype' => $row['testtype'],
                    'testdefinitionname' => $row['testdefinitionname'],
                    'testname' => $row['testname'],
                    'log' => $row['log'],
                    'timestamp' => date("c",strtotime($row['timestamp'])),
                    'testbeds' => array(),
                    'results' => array()
                );
            }
            if ($row['parametername'] == 'testbed' && !in_array($row['parametervalue'], $data[$row['resultid']]['testbeds'])) {
                array_push($data[$row['resultid']]['testbeds'], $row['parametervalue']);
            }
            //NOTE column names are ALWAYS LOWER CASE
            $data[$row['resultid']]['results'][$row['returnname']] = $row['returnvalue'];
        }
        //echo $query;
        $this->closeConnection($con);
        return $data;
    }
    public function updateCache() {
        //NIET PROPER
        //$fakeReq = new Request();
        $this->testbeds = $this->getTestbed(new Request()); //$fakeReq);
        $this->testDefinitions = $this->getTestDefinition(new Request());
        $this->testInstances = $this->getTestInstance(new Request());
        //$testDefinitions = json_decode();
        return;
    }
    //Config Calls
    public function getTestDefinition(&$request) {
        $params = $request->getParameters();
        $query = "select * from definitions";

        $paramsForUse = array();
        $this->addInIfNeeded($query, $params, $paramsForUse, "testtype", "tetyp");
        $this->addInIfNeeded($query, $params, $paramsForUse, "testdefinitionname", "testdefinitionname");

        $con = $this->getConnection();
        $result = pg_query_params($con, $query, $paramsForUse);
        $data = array();
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

        $this->closeConnection($con);
        return $data;
    }
    public function getTestInstance(&$request) {
        $params = $request->getParameters();
        $query = "select *,nextrun  from instances ";//AT TIME ZONE 'CET'
        $paramsForUse = array();
        $eindhaakje = "";

        $this->addAnyIfNeeded($query, $params, $paramsForUse, "testbed", "instances");
        if (sizeof($paramsForUse) > 0) {
            $eindhaakje = ')';
        }
        $this->addInIfNeeded($query, $params, $paramsForUse, "testdefinitionname", "testdefinitionname");
        $this->addInIfNeeded($query, $params, $paramsForUse, "testname", "testname");
        $this->addInIfNeeded($query, $params, $paramsForUse, "testinstanceid","testinstanceid");
        $this->addLowerThanIfNeeded($query, $params, $paramsForUse, "nextrun", "nextrun");
        
        $query .= $eindhaakje;

        $con = $this->getConnection();
        $result = pg_query_params($con, $query, $paramsForUse);
        $data = array();
        while ($row = pg_fetch_assoc($result)) {
            //array_push($data, $row);
            if (!isset($data[$row['id']])) {
                $data[$row['id']] = array(
                    'testname' => $row['testname'],
                    //'testtype' => $row['testtype'],
                    'testdefinitionname' => $row['testdefinitionname'],
                    'frequency' => $row['frequency'],
                    'enabled'   => ($row['enabled']=="t"?"True":"False"),
                    'nextrun'   => date("c",strtotime($row['nextrun'])),
                    //'type' => gettype($row['nextrun']),
                    'parameters' => array()
                );
            }
            if (!isset($data[$row['id']]['parameters'][$row['parametername']])) {
                $data[$row['id']]['parameters'][$row['parametername']] = array();
            }

            array_push($data[$row['id']]['parameters'][$row['parametername']], $row['parametervalue']);
        }

        $this->closeConnection($con);
        return $data;
    }
    public function getTestbed(&$request) {
        $params = $request->getParameters();
        $query = "select * from testbeds";
        $paramsForUse = array();
        $this->addInIfNeeded($query, $params, $paramsForUse, "testbed", "testbedName");

        $con = $this->getConnection();
        $result = pg_query_params($con, $query, $paramsForUse);
        $data = array();
        while ($row = pg_fetch_assoc($result)) {
            //array_push($data, $row);
            if (!isset($data[$row['testbedname']])) {
                $data[$row['testbedname']] = array('testbedName' => $row['testbedname'],
                    'url' => $row['url'],
                    'urn' => $row['urn']
                );
            }
        }
        $this->closeConnection($con);
        return $data;
    }

    //push calls
    public function addTestbed(&$request) {
        
        $params = $request->getParameters();
        
        print_r($params);
        $query = "insert into testbeds (testbedname,url,urn) values ($1,$2,$3);";
        $paramsForUse = array($params['testbedName'][0], $params['url'][0], $params['urn'][0]);
        $con = $this->getConnection();

        //auth?
        //testbed al bestaande? => moet uniek zijn => error uti database



        $result = pg_query_params($con, $query, $paramsForUse);
    }
    public function addResult(&$request) {
        //if ($request->getVerb() == 'POST'){
        $params = $request->getParameters();
        //print_r($params);
        //testinstanceid given & existing?  => ook mogelijk in database
        $valid = FALSE;
        $returnVals;
        if (isset($params['testinstanceid']) && isset($this->testInstances[$params['testinstanceid'][0]])
                && strtoupper($request->getVerb()) == 'POST') {
            $valid = True;
            //kijk of alle return values opgegeven zijn
            $returnVals = $this->testDefinitions[$this->testInstances[$params['testinstanceid'][0]]['testdefinitionname']]['returnValues'];//['testtype']]['returnValues'];

            foreach ($returnVals as $key => $val) {
                if (!isset($params[$key][0])) {
                    $valid = False;
                    $request->addMsg($key . " Not given");
                    $request->setStatus(400);
                    break;
                }
            }
        }else {
            //$valid=False;
            $request->addMsg("testinstanceid not given or not valid! And/Or method not post");
            $request->setStatus(400);
        }
        
        if ($valid) {
            //TODO transaction
            $query = "insert into results (testinstanceid,log) values ($1,$2);";
            $subQuery = "insert into subresults(resultId,returnName,returnValue) values(lastval(),$1,$2);";
            $con = $this->getConnection();

            //result
            $data = array(
                $params['testinstanceid'][0],
                $params['log'][0]
            );
            pg_query($con,"BEGIN");
            $success = pg_query_params($con, $query, $data);

            //subresults
            foreach ($returnVals as $key => $val) {
                $success = $success && pg_query_params($con, $subQuery, array($key, $params[$key][0]));
            }
            
            if(! ($success && pg_query($con,"COMMIT"))){
                //committing failed?
                pg_query($con,"ROLLBACK");
            }
            
            $this->closeConnection($con);
        }

        return;
    }
    public function addTestInstance(&$request){
        //post
        $params = $request->getParameters();
        print_r($params);
        if ($request->getVerb() == 'POST' && isset($params['testdefinitionname']) && isset($this->testDefinitions[$params['testtype'][0]])){
            //kjk of alle params ingevuld zijn
            $valid = True;
            foreach ($this->testDefinitions[$params['testdefinitionname'][0]]['parameters'] as $key => $value){
                if (!isset($params[$key][0])){
                    $valid=false;
                    $request->addMsg($key . " Not given");
                    $request->setStatus(400);
                    break;
                }
            }
            if ($valid){
                $con = $this->getConnection();
                $query = "insert into testinstances (testname,testdefinitionname,frequency) values ($1,$2,$3);";
                $data = array($params['testname'][0],$params['testdefinitionname'][0],$params['frequency']);
                
                pg_query($con,"BEGIN");
                $success = pg_query_params($con,$query,$data);
                
                $subQuery = "insert into parameterInstances (testinstanceId,parameterName,parametervalue) values (lastval(),$1,$2)";
                pg_prepare($con,"subQuery",$subQuery);
                
                foreach ($this->testDefinitions[$params['testdefinitionname'][0]]['parameters'] as $key => $value){
                    $success = $success && pq_execute($con,"subQuery",array($params[$key][0]));
                }
                
                $success = $success && pg_query("COMMIT");
                if (!$success){
                    pg_query("ROLLBACK");
                }
                
                
                //rollback
            }
        }else{
            $request->addMsg("Method not post and/or testtype not give or not valid");
            $request->setStatus(400);
        }
        //meer dan een ping per testbed? eventueel wel, met vrs parameters
        
        return;
    }    
    public function updateNextRun(&$request){
        $params = $request->getParameters();
        //print_r($params);
        
        
        if ( isset($params['nextrun']) 
                && isset($params['testinstanceid'])
                && isset($this->testInstances[$params['testinstanceid'][0]])
                && strtoupper($request->getVerb()) == 'POST') {
            $newTime = date("c",  $params['nextrun'][0]);
            $oldTime = date("c", strtotime($this->testInstances[$params['testinstanceid'][0]]['nextrun']));
            //echo "new: $newTime old: $oldTime\n";
            //timestampe must be past the last timestamp
            if ($newTime > $oldTime){
                //echo "timestamp ok!\n";
                
                //update in database
                $con = $this->getConnection();
                $query = "update testinstances SET nextrun = $1 where testinstanceid=$2;";
                //echo "newTime: " .$newTime . "\n";
                //$insTime = $newTime;
                //echo "insTime: " . $insTime . "\n";
                //echo "no: " . time() . "\n";
                $data = array($newTime,$params['testinstanceid'][0]);
                
                pg_query_params($con,$query,$data);
                $this->closeConnection($con);
            }else{
                $request->setStatus(400);
                $request->addMSg("nextrun timestamp must be after the current nextrun!");
            }
        }else{        
            $request->addMsg("Either testinstanceid not given or not valid!, nextrun not given or method not post");
            $request->setStatus(400);
        }
        
        //echo "old: $oldTime new: $newTime";
        
    }
    //fix connection
    private function getConnection() {
        $con = pg_connect($GLOBALS['conString']) or die("Couldn't connect to database");
        return $con;
    }

    private function closeConnection($con) {
        pg_close($con);
    }
    
    //fix query
    private function addAnyIfNeeded(&$query, &$params, &$paramsForUse, $paramName, $viewName, $colName = 'id') {
        //not sure if this works 2 times on the same query
        if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            if (sizeof($paramsForUse) == 0) {
                $query .= " where ";
            } else {
                $query .= " and ";
            }
            $query .= $colName . "=any(select " . $colName . " from " . $viewName . " where parametervalue IN (";

            array_push($paramsForUse, $params[$paramName][0]);
            $query .= '$';
            $query .= sizeof($paramsForUse);

            for ($i = 1; $i < sizeof($params[$paramName]); $i++) {
                array_push($paramsForUse, $params[$paramName][$i]);
                $query .= ',$';
                $query .= sizeof($paramsForUse);
            }
            $query .= ") ";
        }
    }

    private function addInIfNeeded(&$query, &$params, &$paramsForUse, $paramName, $colName) {

        if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            if (sizeof($paramsForUse) == 0) {
                $query .= " where ";
            } else {
                $query .= " and ";
            }

            $query .= $colName . " IN (";
            array_push($paramsForUse, $params[$paramName][0]);
            $query .= "$";
            $query .= sizeof($paramsForUse);
            for ($i = 1; $i < sizeof($params[$paramName]); $i++) {
                array_push($paramsForUse, $params[$paramName][$i]);
                $query .= ",$";
                $query .= sizeof($paramsForUse);
            }
            $query .= ") ";
        }
    }

    private function addGreaterThanIfNeeded(&$query, &$params, &$paramsForUse, $paramName, $colName) {
        if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            if (sizeof($paramsForUse) == 0) {
                $query .= "where ";
            } else {
                $query .= "and ";
            }

            $query .= $colName . " >= ";
            array_push($paramsForUse, $params[$paramName][0]);
            $query .= "$";
            $query .= sizeof($paramsForUse);
        }
    }

    private function addLowerThanIfNeeded(&$query, &$params, &$paramsForUse, $paramName, $colName) {
        if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            if (sizeof($paramsForUse) == 0) {
                $query .= "where ";
            } else {
                $query .= "and ";
            }

            $query .= $colName . " <= ";
            array_push($paramsForUse, $params[$paramName][0]);
            $query .= "$";
            $query .= sizeof($paramsForUse);
        }
    }

}
