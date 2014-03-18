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
        $query = "with view as ( "
                . "select *,r.resultid id from results r "
                    . "join (select * from subresults) sr on r.resultid = sr.resultid "
                    . "join (select * from testinstances) ti on ti.testinstanceid = r.testinstanceid "
                    . "join (select * from parameterinstances) pi on pi.testinstanceid = r.testinstanceid "
                . ") "
                . "select * from ( "
                    . "select *,dense_rank() over(partition by testname,testtype order by timestamp desc) rank from view"
                . ") vv ";
        
        $paramsForUse = array();
        $eindhaakje = "";
        
        //testbeds
        $this->addAnyIfNeeded($query, $params, $paramsForUse, "testbed");
        if(sizeof($paramsForUse) > 0){
            $eindhaakje=')';
        }
        //testtypes
        $this->addInIfNeeded($query, $params, $paramsForUse, "testtype","testtype");
        //status => per subtest nu !!
        $this->addInIfNeeded($query, $params, $paramsForUse, "status", "value");
        //from
        $this->addGreaterThanIfNeeded($query, $params, $paramsForUse, "from","timestamp");
        //till
        $this->addLowerThanIfNeeded($query, $params, $paramsForUse, "till","timestamp");
        //count
        $this->addLowerThanIfNeeded($query, $params, $paramsForUse, "count", "rank");
        //haakje van any
        $query.=$eindhaakje;
        
        $con = $this->getConnection();
        $result = pg_query_params($con,$query,$paramsForUse);
        $data = array();
        while ($row = pg_fetch_assoc($result)) {
            //array_push($data, $row);
            
            if (!isset($data[$row['resultid']])){
                $data[$row['resultid']] = array(
                    'testinstanceid' => $row['testinstanceid'],
                    'testtype'       => $row['testtype'],
                    'testname'       => $row['testname'],
                    'log'            => $row['log'],
                    'timestamp'      => $row['timestamp'],
                    'testbeds'       => array(),
                    'results'        => array()
                );
            }
            if ($row['parametername'] == 'testbedId' && !in_array($row['parametervalue'],$data[$row['resultid']]['testbeds'])){
                array_push($data[$row['resultid']]['testbeds'],$row['parametervalue']);
            }
            $data[$row['resultid']]['results'][$row['name']] = $row['value'];
            
        }
        
        $this->closeConnection($con);
        return $data;
        
    }
    //Config Calls
    public function getTestDefinition($params) {
        $query = "with view as ("
                    . "select *,t.testtype tetyp from testdefinitions t "
                        . "join (select * from parameterdefinitions) p on p.testtype = t.testtype "
                        . "join (select * from returndefinitions)    r on r.testtype = t.testtype"
                . ") "
                . "select * from view";
        
        $paramsForUse = array();
        $this->addInIfNeeded($query, $params, $paramsForUse, "testname","testname");
        $this->addInIfNeeded($query, $params, $paramsForUse, "testtype", "tetyp");
        
        $con = $this->getConnection();
        $result = pg_query_params($con, $query, $paramsForUse);
        $data = array();
        while ($row = pg_fetch_assoc($result)) {
            //array_push($data, $row);
            if (!isset($data[$row['testtype']])){
                $data[$row['testtype']] = array(
                    'testcommand' => $row['testcommand'],
                    'parameters'  => array(),
                    'return'      => array()
                );
            }
            $data[$row['testtype']]['parameters'][$row['parametername']]
                    =array('type'=>$row['parametertype'],
                        'description' => $row['parameterdescription']);
            
            $data[$row['testtype']]['return'][$row['returnname']]
                    =array('type'=>$row['returntype'],
                        'description' => $row['returndescription']);
        }
        
        $this->closeConnection($con);
        return $data;
        
    }
    public function getTestInstance($params) {
        $query = "with view as ("
                . "select t.testinstanceid as id,* from testinstances t "
                . "join (select * from parameterInstances) p on t.testinstanceid = p.testinstanceid) "
                . "select * from view ";
        $paramsForUse = array();
        $eindhaakje = "";
        
        $this->addAnyIfNeeded($query,$params, $paramsForUse, "testbed");
        if(sizeof($paramsForUse) > 0){
            $eindhaakje=')';
        }
        $this->addInIfNeeded($query, $params, $paramsForUse, "testtype", "testtype");
        $query .= $eindhaakje;
        
        $con = $this->getConnection();
        $result = pg_query_params($con, $query, $paramsForUse);
        $data = array();
        while ($row = pg_fetch_assoc($result)) {
            //array_push($data, $row);
            if (!isset($data[$row['id']])){
                $data[$row['id']]=array(
                    'testname'  => $row['testname'],
                    'testtype'  => $row['testtype'],
                    'frequency' => $row['frequency'],
                    'parameters' => array()
                );
            }
            array_push($data[$row['id']]['parameters'],
                array($row['parametername']=>$row['parametervalue'])
            );
        }

        
        $this->closeConnection($con);
        return $data;
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
    private function addAnyIfNeeded(&$query,&$params,&$paramsForUse,$paramName,$colName='id'){
        //not sure if this works 2 times on the same query
        if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            if (sizeof($paramsForUse) == 0){
                $query .= " where ";
            }else{
                $query .= " and ";
            }
            $query .= $colName."=any(select ".$colName." from view where parametervalue IN (";

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
    private function addInIfNeeded(&$query,&$params,&$paramsForUse,$paramName,$colName){
        
        if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            if (sizeof($paramsForUse) == 0){
                $query .= " where ";
            } else {
                $query .= " and ";
            }

            $query .= $colName." IN (";
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
    private function addGreaterThanIfNeeded(&$query,&$params,&$paramsForUse,$paramName,$colName){
         if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            if (sizeof($paramsForUse) == 0){
                $query .= "where ";
            } else {
                $query .= "and ";
            }

            $query .= $colName." >= ";
            array_push($paramsForUse, $params[$paramName][0]);
            $query .= "$";
            $query .= sizeof($paramsForUse);
        }
    }
    private function addLowerThanIfNeeded(&$query,&$params,&$paramsForUse,$paramName,$colName){
        if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            if (sizeof($paramsForUse) == 0){
                $query .= "where ";
            } else {
                $query .= "and ";
            }

            $query .= $colName." <= ";
            array_push($paramsForUse, $params[$paramName][0]);
            $query .= "$";
            $query .= sizeof($paramsForUse);
        } 
    }
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