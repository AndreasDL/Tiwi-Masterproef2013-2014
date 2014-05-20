<?php
include (__DIR__."/../database/AccessDatabase.php");
/**
 * Handles all requests for /testInstance
 * for more information take a look at the interface.
 */
class TestInstanceController implements iController{
    private $dbo;
    
    public function __construct(&$req){
        $this->dbo = new AccessDatabase($req->getQb(),$req->getFetcher());
    }
    
    public function get($params){
        return $this->dbo->getTestInstance($params);
    }

}