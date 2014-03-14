<?php
include (__DIR__."/../database/AccessDatabase.php");

class TestInstanceController implements iController{
    private $dbo;
    
    public function __construct(){
        $this->dbo = new AccessDatabase;
    }
    
    public function get($params){
        return json_encode($this->dbo->getTestInstance($params));
    }

}