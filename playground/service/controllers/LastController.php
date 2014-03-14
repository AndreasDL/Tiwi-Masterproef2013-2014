<?php
include (__DIR__."/../database/AccessDatabase.php");

class LastController implements iController{
    //handles all requests for /last
    
    private $dbo;
    
    public function __construct(){
        $this->dbo = new AccessDatabase;
    }
    
    public function get($params){
        return json_encode($this->dbo->getLast($params));
    }
}