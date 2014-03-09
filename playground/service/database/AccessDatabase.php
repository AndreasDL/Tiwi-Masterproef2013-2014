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
    }
    
    public function getConnection(){
        $con =  pg_Connect("$this->conString") or die('connection failed');
        return $con;
    }
    
    public function getLast($params){
        $ret = array();
        $data = pg_exec($this->getConnection(),"select * from results order by testbedId");
        while ($row  = pg_fetch_assoc($data)){
            //map testbedid op rij
            //overschijft atm wel vorige waarden, fixen met query
            array_push($ret, $row);
        }
        return $ret;
    }
}