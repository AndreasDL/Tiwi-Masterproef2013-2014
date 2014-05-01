<?php

include (__DIR__ . "/../database/AccessDatabase.php");

class TestbedController implements iController {

    private $dbo;

    public function __construct(&$req) {
        $this->dbo = new AccessDatabase($req->getFilter(),$req->getFetcher());
    }

    public function get($params) {
        return $this->dbo->getTestbed($params);
    }

}
