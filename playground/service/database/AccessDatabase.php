<?php

include (__DIR__ . "/../config.php"); //database config

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
        
    }
    public function getList($params) {
        $query = "select * from "
                . "(select * ,"
                . " rank() over(partition by r1.TestInstanceId order by r1.timestamp desc) "
                . "from results r1"
                . " join (select testname,testinstanceId from testinstances) instance "
                . "on instance.testinstanceId = r1.testinstanceId) tabel";

        return $this->buildAndExecuteQuery($query, $params);
    }

    //Config Calls
    public function getTestDefinition($params) {
        $query = "select * from testdefinitions";
        return $this->buildAndExecuteQuery($query, $params);
    }
    public function getTestInstance($params) {
        $query = "select * from testInstances";       
        return $this->buildAndExecuteQuery($query,$params);
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
    private function buildAndExecuteQuery(&$query, &$params) {
        //Builds a query by adding where clauses
        //gets start query

        $paramsForUse = array(); //used to pushback used params        
        //test
        $colName = "testName"; //colum in database
        $paramName = "testType"; //name of parameter
        $this->addWhereInIfNeeded($query, $colName, $paramName, $params, $paramsForUse);

        ///testbeds
        //$colName = "testbed";
        //$paramName = "testbed";
        //$query = $this->addWhereIfNeeded($query, $colName, $paramName, $params, $paramsForUse);
        //=> nog niet mogelijk want want testbed zit in parameters & parameters is hardcoded
        
        //between
        //from
        $this->addWhereGreaterThanIfNeeded($query,"timestamp", "from", $params, $paramsForUse);
        //till
        $this->addWhereSmallerThanIfNeeded($query,"timestamp", "till", $params, $paramsForUse);
        
        //count
        $this->addWhereSmallerThanIfNeeded($query,"rank", "count", $params, $paramsForUse);
        
        //status
        //zit in results list en die is opgeslagen als json
        //NOTE hoe status van stitch?
        
        //stitching testname
        //in params

        echo "$query<br><br><br>";
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

        //return $query;
    }
    private function addWhereSmallerThanIfNeeded(&$query,$colName,$paramName, &$params,&$paramsForUse){
        if (isset($params[$paramName])) {
            //echo "count detected!!!!!!!!!!!!!!!!!!";
            array_push($paramsForUse, $params[$paramName][0]);
            if (sizeof($paramsForUse) == 1) {
                $query .= " WHERE ";
            } else {
                $query .= " AND ";
            }
            $query .= $colName." <= \$" . sizeof($paramsForUse);
        }
    }
    private function addWhereGreaterThanIfNeeded(&$query,$colName,$paramName, &$params,&$paramsForUse){
        if (isset($params[$paramName])) {
            //echo "count detected!!!!!!!!!!!!!!!!!!";
            array_push($paramsForUse, $params[$paramName][0]);
            if (sizeof($paramsForUse) == 1) {
                $query .= " WHERE ";
            } else {
                $query .= " AND ";
            }
            $query .= $colName." >= \$" . sizeof($paramsForUse);
        }
    }
    
    private function execQueryAndMakeDataStructure($query, $paramsForUse) {
        //executes query and puts results in a hash-array structure
        $con = $this->getConnection();
        $result = pg_query_params($con, $query, $paramsForUse);

        //put in datastructure
        $data = array();
        while ($row = pg_fetch_assoc($result)) {
            array_push($data, $row);
        }
        print_r($data);

        //close connection
        $this->closeConnection($con);
        //return 
        return $data;
    }
}
