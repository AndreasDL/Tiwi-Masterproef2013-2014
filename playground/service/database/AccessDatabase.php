<?php
//new databaseAccess();

class AccessDatabase{
    //todo vars from property file
    private $login;
    private $pass;
    private $dbname;
    //database on localhost
    private $conString;
    
    public function __construct(){
        $this->login  = 'postgres';
        $this->pass  = "post";
        $this->dbname = "testdb";
        
        $this->conString = "dbname=".$this->dbname." user=".$this->login." password=".$this->pass;
        //echo $this->conString;
    }
    
    public function getConnection(){
        $con = pg_Connect($this->conString) or die('connection failed');
        return $con;
    }
    
    public function getLast($params){
        //print_r($params);
        $ret = array();
        $query = "select r1.* from results r1 "
                    . "inner join ("
                        . "select testbedid,max(resultid) as max from results "
                        . "group by testbedid) r2 "
                    . "ON r2.testbedid = r1.testbedid and r1.resultid = r2.max";
        $paramsForUse = array();
        
        #params
        #tests => select values
        if (array_key_exists('test',$query)){
            
        }
        
        #prepare query
        #testbed => in where
        #keep parameters to use in an array, then bind the array
        #not given or set to all => no params added & all testbeds will be returned
        if (array_key_exists('testbed',$params) && strtoupper($params['testbed'][0]) != 'ALL'){
            #build $query
            $query .= ' where r1.testbedid = $1';
            array_push($paramsForUse , $params['testbed'][0]);
            for ($i = 2; $i <= sizeof($params['testbed']) ; $i++){
                $query .= " or r1.testbedid = \$$i";
                array_push($paramsForUse , $params['testbed'][$i-1]);
            }
            //echo "$query<br><br>";
            //print_r($paramsForUse);
            #parameters binden
        }
        
        //$data = pg_exec($this->getConnection(),$query);
        $data = pg_query_params($this->getConnection(),$query,$paramsForUse);
        
        while ($row  = pg_fetch_assoc($data)){
            //array me de rijtjes in
            array_push($ret, $row);
        }
        
        print_r($ret);
        return $ret;
    }
}