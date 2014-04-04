<?php

class Request{
    private $parameters;
//    private $formatter;
    private $status;
    private $msg;
    private $verb;
    private $data;
    
    function __construct(&$parameters=array(), &$status='', &$msg='', &$verb='') {
        $this->parameters = $parameters;
        $this->status = $status;
        $this->msg = $msg;
        $this->verb = $verb;
    }
    
    
    
    public function getData() {
        return $this->data;
    }

    public function setData($data) {
        $this->data = $data;
    }

        public function getParameters() {
        return $this->parameters;
    }

    public function getStatus() {
        return $this->status;
    }

    public function getMsg() {
        return $this->msg;
    }

    public function getVerb() {
        return $this->verb;
    }

    public function setStatus($status) {
        $this->status = $status;
    }

    public function setMsg($msg) {
        $this->msg = $msg;
    }

    public function setVerb($verb) {
        $this->verb = $verb;
    }

    public function setParameters($parameters) {
        $this->parameters = $parameters;
    }




}

