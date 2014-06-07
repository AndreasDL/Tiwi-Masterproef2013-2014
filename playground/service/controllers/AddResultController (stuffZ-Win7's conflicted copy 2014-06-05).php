<?php

include (__DIR__."/../database/AccessDatabase.php");

class AddResultController implements iController{
    //handles all requests for /last
    
    private $dbo;
    
    public function __construct(){
        $this->dbo = new AccessDatabase;
    }
    
    public function get($params){
        return $this->dbo->addResult($params);
    }
}