<?php
include (__DIR__."/../database/AccessDatabase.php");
/**
 * Handles all requests for /testDefinition
 * for more information take a look at the interface.
 */
class TestDefinitionController implements iController{

    private $dbo;
    
    public function __construct($req){
        $this->dbo = new AccessDatabase($req->getQb(),$req->getFetcher());
    }
    
    public function get($params){
        return $this->dbo->getTestDefinition($params);
    }
}