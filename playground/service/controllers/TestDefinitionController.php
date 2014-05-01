<?php
//debugging
ini_set('display_startup_errors',1);
ini_set('display_errors',1);
error_reporting(-1);

include (__DIR__."/../database/AccessDatabase.php");

class TestDefinitionController implements iController{

    private $dbo;
    
    public function __construct($req){
        $this->dbo = new AccessDatabase($req->getFilter(),$req->getFetcher());
    }
    
    public function get($params){
        return $this->dbo->getTestDefinition($params);
    }
}