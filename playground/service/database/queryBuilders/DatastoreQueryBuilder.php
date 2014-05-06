<?php

/**
 * Builds a query with the ?q= param to support the geni datastore. For more information take a look at the superclass.
 * @author Andreas De Lille
 */
class DatastoreQueryBuilder extends aQueryBuilder {

    public function buildDefinition(&$query, &$params, &$paramsForUse) {
        
    }

    public function buildList(&$query, &$params, &$paramsForUse) {
        //print_r($params);
        //?q=
        //{"filters":
        //  {"eventType": ["ops_monitoring:is_available"],
        //  "ts":{"gte":1391192225475202,"lt":1391192225480000},
        //  "obj":{"type":"aggregate",
        //      "id":["gpo-ig","utah-ig","rci-eg"]
        //}}}
        
        $eindhaakje = '';
        if (isset($params['filters']['obj']) && $params['filters']['obj']['type'] == 'aggregate') {
            $this->addAnyIfNeeded($query, $params['filters']['obj'], $paramsForUse, 'id', 'list');
        }
        if (sizeof($paramsForUse) > 0) {
            $eindhaakje = ')';
        }

        //eventType
        $this->addInIfNeeded($query, $params['filters'], $paramsForUse, "eventType", "genidatastoretestname");

        
        if (isset($params['filters']['ts'])) {
            
            //from
            if (isset($params['filters']['ts']['gte'])){
                $params['filters']['ts']['gte'] = array(date('c', $params['filters']['ts']['gte']/1000000)); //accurate to a second
            }
            $this->addGreaterThanIfNeeded($query, $params['filters']['ts'], $paramsForUse, "gte", "timestamp");
            
            //till
            if (isset($params['filters']['ts']['lt'])){
                $params['filters']['ts']['lt'] = array(date('c' , $params['filters']['ts']['lt']/1000000));//nauwkeurig tot op de seconde
            }
            $this->addLowerThanIfNeeded($query, $params['filters']['ts'], $paramsForUse, "lt", "timestamp");
        }

        //haakje van any
        $query.=$eindhaakje;
    }

    public function buildTestInstance(&$query, &$params, &$paramsForUse) {
        
    }

    public function buildTestbed(&$query, &$params, &$paramsForUse) {
        
    }

}
