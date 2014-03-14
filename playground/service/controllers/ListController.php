<?php

include (__DIR__."/../database/AccessDatabase.php");

class ListController implements iController{
private $dbo;
    
    public function __construct(){
        $this->dbo = new AccessDatabase;
    }
    
    public function get($params){
        return json_encode($this->dbo->getList($params));
    }

}