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
        $query = "select * from ("
                    . "select *,dense_rank() over(partition by testname,testtype order by timestamp desc) rank from list"
                . ") vv ";
        
        $paramsForUse = array();
        $eindhaakje = "";
        
        //testbeds
        $this->addAnyIfNeeded($query, $params, $paramsForUse, "testbed","list");
        if(sizeof($paramsForUse) > 0){
            $eindhaakje=')';
        }
        //testtypes
        $this->addInIfNeeded($query, $params, $paramsForUse, "testtype","testtype");
        //status => per subtest nu !!
        $this->addInIfNeeded($query, $params, $paramsForUse, "status", "value");
        //resultid
        $this->addInIfNeeded($query, $params, $paramsForUse, "resultId", "id");
        //testname
        $this->addInIfNeeded($query, $params, $paramsForUse, "testname", "testname");
        //from
        $this->addGreaterThanIfNeeded($query, $params, $paramsForUse, "from","timestamp");
        //till
        $this->addLowerThanIfNeeded($query, $params, $paramsForUse, "till","timestamp");
        //count
        $this->addLowerThanIfNeeded($query, $params, $paramsForUse, "count", "rank");
        //haakje van any
        $query.=$eindhaakje;
        
        //print $query;
        
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
        $query = "select * from definitions";
        
        $paramsForUse = array();
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
        $query = "select * from instances ";
        $paramsForUse = array();
        $eindhaakje = "";
        
        $this->addAnyIfNeeded($query,$params, $paramsForUse, "testbed", "instances");
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
    private function addAnyIfNeeded(&$query,&$params,&$paramsForUse,$paramName,$viewName,$colName='id'){
        //not sure if this works 2 times on the same query
        if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            if (sizeof($paramsForUse) == 0){
                $query .= " where ";
            }else{
                $query .= " and ";
            }
            $query .= $colName."=any(select ".$colName." from ".$viewName." where parametervalue IN (";

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