<?php
include(__dir__ . '/iGuard.php');
include(__DIR__ . "/../config.php");

class defaultGuard implements iGuard{
    
    function __construct() {
        //print_r($this->getKey('iminds'));
    }

    public function isValid($request) {
        $result = FALSE;
        
        //check if keyid & hash is set
        if(isset($request['keyid']) && isset($request['hmac'])){
            $result = ($request['hmac'] === hash_hmac($GLOBALS['hmacAlgo'],$data,$this->getPrKey($request['keyid'])));
        }
        
        return $result;
    }
    
    private function getkey($keyid){
        $con = pg_connect($GLOBALS['conString']) or die("couldn't connect to database");
        $key;
        $query = "select key from users where keyid = ($1)";
        $params = array($keyid);
        $result = pg_query_params($con,$query,$params);
        
        while ($row = pg_fetch_assoc($result)){
            $key = $row['key'];
        }
        
        return $key;
    }

}

new defaultGuard();
