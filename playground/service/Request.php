<?php
/**
 * this class represents a request to the webservice.
 */
class Request{
    private $parameters;
//    private $formatter;
    private $status;
    private $msg;
    private $verb;
    private $data;
    /**
     * this creates a request
     * @param type $parameters the parameters get & post of the request
     * @param type $status http-like status code to tell if the request was succesfull
     * @param type $msg used for warnings and errormessages
     * @param type $verb the method (get,post, ...) of the request
     */
    function __construct(&$parameters=array(), &$status='', &$msg='', &$verb='') {
        $this->parameters = $parameters;
        $this->status = $status;
        $this->msg = $msg;
        $this->verb = $verb;
    }
    /**
     * the data/result of the request
     * @return type
     */
    public function getData() {
        return $this->data;
    }
/**
 * sets the data/ result of the request
 * @param type $data
 */
    public function setData($data) {
        $this->data = $data;
    }
/**
 * returns the parameters associated with this request.All parameters are stored in arrays
 * @return type hashmap name=>array(parameters)
 */
    public function getParameters() {
        return $this->parameters;
    }

    /**
     * returns the status of the request with http-like statuscodes
     * @return type
     */
    public function getStatus() {
        return $this->status;
    }

    /**
     * returns the message containing the warn & error messages of the request
     * @return type
     */
    public function getMsg() {
        return $this->msg;
    }
/**
 * returns the verb of this request e.g. get/post/put ...
 * @return type
 */
    public function getVerb() {
        return $this->verb;
    }
/**
 * sets the status of this request, status could still be overwritten.
 * @param type $status
 */
    public function setStatus($status) {
        $this->status = $status;
    }
/**
 * adds a msg to the messages
 * @param type $msg
 */
    public function addMsg($msg) {
        if (strlen($this->msg) == 0){
            $this->msg = $msg;
        }else{
            $this->msg = $this->msg." | ".$msg;
        }
    }
/**
 * returns the verb associated with this request. e.g. post/put/get
 * @param type $verb
 */
    public function setVerb($verb) {
        $this->verb = $verb;
    }
/**
 * sets the parameters of the request all parameters are stored in arrays
 * @param type $parameters hashmap name=>parameter as array
 */
    public function setParameters($parameters) {
        $this->parameters = $parameters;
    }




}

