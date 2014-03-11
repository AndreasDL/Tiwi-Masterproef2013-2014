<?php
//debugging
ini_set('display_startup_errors',1);
ini_set('display_errors',1);
error_reporting(-1);

include (__DIR__."/../database/AccessDatabase.php");

class TestPlanListController implements iController{
private $dbo;
    
    public function __construct(){
        $this->dbo = new AccessDatabase;
    }
    
    public function get($params){
        return json_encode($this->dbo->getTestPlanList());
    }

}