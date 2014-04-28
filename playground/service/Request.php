<?php

/**
 * this class represents a request to the webservice.
 */
class Request {

    private $parameters;
    private $status;
    private $msg;
    private $verb;
    private $data;
    private $fetcher;
    private $filter;

    /**
     * this creates a request
     * @param iFetcher $fetcher the fetcher object to handle the results.
     * @param iFilter  $filter The filter to filter the results.
     * @param array $parameters the parameters get & post of the request
     * @param int $status http-like status code to tell if the request was succesfull
     * @param string $msg used for warnings and errormessages
     * @param string $verb the method (get,post, ...) of the request
     */
    function __construct(&$fetcher, &$filter, &$parameters = array(), &$status = '', &$msg = '', &$verb = '') {
        $this->fetcher = $fetcher;
        $this->filter = $filter;
        $this->parameters = $parameters;
        $this->status = $status;
        $this->msg = $msg;
        $this->verb = $verb;
    }

    /**
     * returns the fetcher object associated with this request.
     * @return iFetcher
     */
    public function getFetcher() {
        return $this->fetcher;
    }

    /**
     * Sets the fetcher object
     * @param iFetcher $fetcher
     */
    public function setFetcher($fetcher) {
        $this->fetcher = $fetcher;
    }

    /**
     * returns the filter
     * @return iFilter
     */
    public function getFilter() {
        return $this->filter;
    }

    /**
     * sets the filter
     * @param iFilter $filter
     */
    public function setFilter($filter) {
        $this->filter = $filter;
    }

    /**
     * the data/result of the request
     * @return array()
     */
    public function getData() {
        return $this->data;
    }

    /**
     * sets the data/ result of the request
     * @param array $data
     */
    public function setData($data) {
        $this->data = $data;
    }

    /**
     * returns the parameters associated with this request.All parameters are stored in arrays
     * @return array(name=>array(parameters))
     */
    public function getParameters() {
        return $this->parameters;
    }

    /**
     * returns the status of the request with http-like statuscodes
     * @return int
     */
    public function getStatus() {
        return $this->status;
    }

    /**
     * returns the message containing the warn & error messages of the request
     * @return string
     */
    public function getMsg() {
        return $this->msg;
    }

    /**
     * returns the verb of this request e.g. get/post/put ...
     * @return string
     */
    public function getVerb() {
        return $this->verb;
    }

    /**
     * sets the status of this request, status could still be overwritten.
     * @param string $status
     */
    public function setStatus($status) {
        $this->status = $status;
    }

    /**
     * adds a msg to the messages
     * @param string $msg
     */
    public function addMsg($msg) {
        if (strlen($this->msg) == 0) {
            $this->msg = $msg;
        } else {
            $this->msg = $this->msg . " | " . $msg;
        }
    }

    /**
     * returns the verb associated with this request. e.g. post/put/get
     * @param string $verb
     */
    public function setVerb($verb) {
        $this->verb = $verb;
    }

    /**
     * sets the parameters of the request all parameters are stored in arrays
     * @param array(name=>parameter)
     */
    public function setParameters($parameters) {
        $this->parameters = $parameters;
    }

}
