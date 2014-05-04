<?php

/**
 * Description of datastorefilter
 *
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
            //werkt nog niet helemaal
            
            //from
            isset($params['filters']['ts']['gte']) ? $params['filters']['ts']['gte'] = date('c' , $params['filters']['ts']['gte']) : '' ;
            $this->addGreaterThanIfNeeded($query, $params['filters']['ts'], $paramsForUse, "gte", "timestamp");
            
            //till
            isset($params['filters']['ts']['lt']) ? $params['filters']['ts']['lt'] = date('c' , $params['filters']['ts']['lt']) : '' ;
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
