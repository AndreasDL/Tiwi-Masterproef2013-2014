<?php

include (__DIR__."/../database/AccessDatabase.php");

class AverageController implements iController{
private $dbo;
    
    public function __construct(){
        $this->dbo = new AccessDatabase;
    }
    
    public function get($params){
        return $this->dbo->getAverage($params);
    }

}