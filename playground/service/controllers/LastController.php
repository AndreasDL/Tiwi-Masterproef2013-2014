<?php
include (__DIR__."/../database/AccessDatabase.php");

class LastController implements iController{
    //handles all requests for /last
    
    private $dbo;
    
    public function __construct(&$req){
        $this->dbo = new AccessDatabase($req->getFilter(),$req->getFetcher());
    }
    
    public function get($params){
        return $this->dbo->getLast($params);
    }
}