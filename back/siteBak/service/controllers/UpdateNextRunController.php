<?php

include (__DIR__."/../database/AccessDatabase.php");
/**
 * handles all requests for /updateNextrun. Used to change the nextrun value of a test
 * for more information take a look at the interface.
 */
class UpdateNextRunController implements iController{
    //handles all requests for /last
    
    private $dbo;
    
    public function __construct(&$req){
        $this->dbo = new AccessDatabase($req->getQb(),$req->getFetcher());
    }
    
    public function get($params){
        return $this->dbo->updateNextRun($params);
    }
}
