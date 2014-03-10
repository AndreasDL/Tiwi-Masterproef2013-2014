<?php
//new databaseAccess();

class AccessDatabase{
    //todo vars from property file
    private $login;
    private $pass;
    private $dbname;
    //database on localhost
    private $conString;
    private $data;
    
    public function __construct(){
        $this->login  = 'postgres';
        $this->pass  = "post";
        $this->dbname = "testdb";
        
        $this->conString = "dbname=".$this->dbname." user=".$this->login." password=".$this->pass;
        
        $this->data = array();
        for ($i = 0 ; $i < 3 ; $i++){
            $test = array();
            $test['testname']  = 'ping';
            $test['testbedId'] = "urn-testbed$i";
            $test['planId']    = 103+$i;
            $test['resultId']  = 1452 + $i;
            $test['log']       = "http://www.".$test['testbedId']."com/Logs/".$test['testname']."/log".$test['resultId'];
            $test['results']   = array();
                $subResult = array();
                $subResult['name'] = 'pingValue';
                $subResult['value'] = rand(20,73);
            array_push($test['results'],$subResult);
            
            array_push($this->data,$test);
        }
        
        for ( $i = 0 ; $i < 5 ; $i++){
            $test = array();
            $test['testname']  = 'stitching';
            $test['testbedId'] = "urn-testbed$i";
            $test['planId']    = 78+$i;
            $test['resultId']  = 2358 + $i;
            $test['log']       = "http://www.".$test['testbedId']."com/Logs/".$test['testname']."/log".$test['resultId'];
            $test['results']   = array();
            
            $subs = array('setup','getUserCredential','generateRspec','');
                $subResult = array();
                $subResult['name'] = 'setup';
                $subResult['value'] = 'succes';
            array_push($test['results'],$subResult);
            
            array_push($this->data,$test);
        }
        print_r($this->data);
    }
    
    public function getLast($params){
        $ret = $this->data;
        
        //return $ret;
    }
}