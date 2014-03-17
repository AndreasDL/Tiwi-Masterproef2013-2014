<?php

include (__DIR__ . "/config.php"); //database config

class AccessDatabase {

    public function __construct() {
        
    }

    //Result Calls
    public function getLast($params) {
        //last => indien count niet gezet => op 1 zetten
        if (!isset($params['count'])) {
            $params['count'] = array(1);
        }
        return $this->getList($params);
    }

    public function getDetail($params) {
        //later te maken => alle koloms aanwezig in tabel
        //=> beslissen welke dan extra worden meegegeven.
    }

    public function getAverage($params) {
        //gaat nog niet want resultaat zit in json codering
    }

    public function getList($params) {
        /*$query = "select * from "
                . "(select *,parameters[2] as testbed ,results[1] as value,"
                . " rank() over(partition by r1.TestInstanceId order by r1.timestamp desc) "
                . "from results r1"
                . " join (select * from testinstances) instance "
                . "on instance.testinstanceId = r1.testinstanceId) tabel";
*/
        $query = "select * from results r"
            . " join (select * from testInstances) ti on r.testinstanceId = ti.testinstanceId"
            . " join (select * from parameterInstances) pi on ti.testinstanceid = pi.testinstanceid"
            . " join (select * from subresults) s on s.resultid = r.resultid;";


        

        return $this->buildAndExecuteQuery($query, $params);
    }

    //Config Calls
    public function getTestDefinition($params) {
        $query = "select * from testdefinitions t "
                . "join (select * from parameterdefinitions) p on p.testtype = t.testtype "
                . "join (select * from returndefinitions)    r on r.testtype = t.testtype;";
        return $this->buildAndExecuteQuery($query, $params);
    }

    public function getTestInstance($params) {
        print_r($params);
        echo "<br><br>";
        $query = "with view as ("
                . "select t.testinstanceid as A,* from testinstances t "
                . "join (select * from parameterInstances) p on t.testinstanceid = p.testinstanceid) "
                . "select * from view ";
        $paramsForUse = array();    
        
        if (isset($params['testbed']) && $params['testbed'][0] != 'ALL'){
            $query .= "where A  = any(select A from view where parametervalue IN (";
            
            array_push($paramsForUse,$params['testbed'][0]);
            $query .= '$';
            $query .= sizeof($paramsForUse);
                
            for($i = 1 ; $i < sizeof($params['testbed']) ; $i++){
                array_push($paramsForUse,$params['testbed'][$i]);
                $query .= ',$';
                $query .= sizeof($paramsForUse);
            }
            $query .= ") ";
        }
        if (isset($params['testtype']) && $params['testtype'][0] != 'ALL'){
            if (! (isset($params['testbed']) && $params['testbed'][0] != 'ALL')){
                $query .= "where ";
            }else{
                $query .= "and ";
            }
            
            $query .= "testtype IN (";
            array_push($paramsForUse,$params['testtype'][0]);
            $query .= "$";
            $query .= sizeof($paramsForUse);
            for($i = 1 ; $i < sizeof($params['testtype']) ; $i++){
                array_push($paramsForUse,$params['testbed'][$i]);
                $query .= ",$";
                $query .= sizeof($paramsForUse);
            }
            $query .= ") ";
        }
        $query .= ');';
        echo $query;
        $con = $this->getConnection();
        
        $result = pg_query_params($con,$query,$paramsForUse);
        $data = array();
        while ($row = pg_fetch_assoc($result)) {
            array_push($data, $row);
        }
        
        print_r($data);

            
        
        $this->closeConnection($con);
        
    }

    //fix connection
    private function getConnection() {
        $con = pg_connect($GLOBALS['conString']) or die("Couldn't connect to database");
        return $con;
    }

    private function closeConnection($con) {
        pg_close($con);
    }
/*
    //fix query
    private function buildAndExecuteQuery(&$query, &$params) {
        //Builds a query by adding where clauses
        //gets start query

        $paramsForUse = array(); //used to pushback used params        
        //testname (for stitching tests, useless for ping tests)
        $this->addWhereInIfNeeded($query, "testname", "testname", $params, $paramsForUse);
        //testType
        $this->addWhereInIfNeeded($query, "testtype", "testtype", $params, $paramsForUse);

        //between
        //from
        $this->addWhereGreaterThanIfNeeded($query, "timestamp", "from", $params, $paramsForUse);
        //till
        $this->addWhereSmallerThanIfNeeded($query, "timestamp", "till", $params, $paramsForUse);

        //count
        $this->addWhereSmallerThanIfNeeded($query, "rank", "count", $params, $paramsForUse);

        //status
        //zit in results list en die is opgeslagen als json
        //NOTE hoe status van stitch?
        //stitching testname
        //in params
        //echo "$query<br><br><br>";
        return $this->execQueryAndMakeDataStructure($query, $paramsForUse);
    }

    //make where clause
    private function addWhereInIfNeeded(&$query, $colName, $paramName, &$params, &$paramsForUse) {
        //adds a where clause to query based on colom name and values in params
        ////where ... in (.. , .. , ..)
        //sql injection not possible via $colName, because $colName is hardcoded

        if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            //handle first one (add where/and keyword if needed)
            array_push($paramsForUse, $params[$paramName][0]);
            if (sizeof($paramsForUse) == 1) {
                $query .= " where $colName in ( \$";
            } else {
                $query .= " and $colName in ( \$";
            }
            $query .= sizeof($paramsForUse); //NEEDS TO BE 2 LINES OR IT WON'T WORK


            for ($index = 2; $index <= sizeof($params[$paramName]); $index++) {
                array_push($paramsForUse, $params[$paramName][$index - 1]);
                $query .= " , \$";
                $query .= sizeof($paramsForUse); //NEEDS TO BE 2 LINES OR IT WON'T WORK
            }
            $query .= " )";
        }

        //return $query; => input/output param otherwise duplication => less efficient 
        //so forcing user to use output param
    }

    private function addWhereSmallerThanIfNeeded(&$query, $colName, $paramName, &$params, &$paramsForUse) {
        if (isset($params[$paramName])) {
            //echo "count detected!!!!!!!!!!!!!!!!!!";
            array_push($paramsForUse, $params[$paramName][0]);
            if (sizeof($paramsForUse) == 1) {
                $query .= " WHERE ";
            } else {
                $query .= " AND ";
            }
            $query .= $colName . " <= \$" . sizeof($paramsForUse);
        }
    }

    private function addWhereGreaterThanIfNeeded(&$query, $colName, $paramName, &$params, &$paramsForUse) {
        if (isset($params[$paramName])) {
            //echo "count detected!!!!!!!!!!!!!!!!!!";
            array_push($paramsForUse, $params[$paramName][0]);
            if (sizeof($paramsForUse) == 1) {
                $query .= " WHERE ";
            } else {
                $query .= " AND ";
            }
            $query .= $colName . " >= \$" . sizeof($paramsForUse);
        }
    }

    //parse data
    private function execQueryAndMakeDataStructure($query, $paramsForUse) {
        //executes query and puts results in a hash-array structure
        $con = $this->getConnection();
        $result = pg_query_params($con, $query, $paramsForUse);

        //put in datastructure
        //put testbeds and tests together before returning
        $data = array();
        while ($row = pg_fetch_assoc($result)) {
            //print $row['parameters'][1];
            /*
              if (! isset($data[$row['testbed']])){
              $data[$row['testbed']] = array();
              }
              if (! isset($data[$row['testbed']][$row['testtype']]) ){
              $data[$row['testbed']][$row['testtype']] = array();
              }
              array_push($data[$row['testbed']][$row['testtype']], $row);
             * 
             
            array_push($data, $row);
        }
        print_r($data);

        //close connection
        $this->closeConnection($con);
        //return $data;
    }
*/
}
