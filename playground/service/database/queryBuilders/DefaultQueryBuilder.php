<?php

/**
 * Builds a query for more information take a look at the interface.
 * @author Andreas De Lille
 */
class DefaultQueryBuilder extends aQueryBuilder {

    public function buildList(&$query, &$params, &$paramsForUse) {
        if (!isset($params['till']) && !isset($params['from']) && isset($params['count']) && $params['count'][0] > $GLOBALS['maxList']) {
            $request->addMsg("Warn: count value higher that max allowed (" . $GLOBALS['maxList'] . "). Using max as count.");
            $params['count'] = $GLOBALS['maxList'];
        } else if (!isset($params['count'])) {
            $params['count'] = array($GLOBALS['maxList']);
        }

        $eindhaakje = "";
        //testbeds
        $this->addAnyIfNeeded($query, $params, $paramsForUse, "testbed", "list");
        if (sizeof($paramsForUse) > 0) {
            $eindhaakje = ')';
        }
        //testdefinition
        $this->addInIfNeeded($query, $params, $paramsForUse, "testdefinitionname", "testdefinitionname");
        //status => per subtest nu !! => gaat dus gwn resultaten teruggeven en die bepaalde subtesten negeren => niet nuttig
        //$this->addInIfNeeded($query, $params, $paramsForUse, "status", "value");
        //resultid
        $this->addInIfNeeded($query, $params, $paramsForUse, "resultid", "id");
        //testname
        $this->addInIfNeeded($query, $params, $paramsForUse, "testname", "testname");
        //testinstanceid
        $this->addInIfNeeded($query, $params, $paramsForUse, "testid", "testinstanceid");
        //from
        $this->addGreaterThanIfNeeded($query, $params, $paramsForUse, "from", "timestamp");
        //till
        $this->addLowerThanIfNeeded($query, $params, $paramsForUse, "till", "timestamp");
        //count
        $this->addLowerThanIfNeeded($query, $params, $paramsForUse, "count", "rank");
        //haakje van any
        $query.=$eindhaakje;
    }

    public function buildDefinition(&$query, &$params, &$paramsForUse) {
        $this->addInIfNeeded($query, $params, $paramsForUse, "testtype", "tetyp");
        $this->addInIfNeeded($query, $params, $paramsForUse, "testdefinitionname", "testdefinitionname");

        $query .= ' order by returnIndex';
    }

    public function buildTestInstance(&$query, &$params, &$paramsForUse) {
        $eindhaakje = "";

        $this->addAnyIfNeeded($query, $params, $paramsForUse, "testbed", "instances");
        if (sizeof($paramsForUse) > 0) {
            $eindhaakje = ')';
        }
        $this->addInIfNeeded($query, $params, $paramsForUse, "testdefinitionname", "testdefinitionname");
        $this->addInIfNeeded($query, $params, $paramsForUse, "testname", "testname");
        $this->addInIfNeeded($query, $params, $paramsForUse, "testinstanceid", "testinstanceid");
        
        $this->addLowerThanIfNeeded($query, $params, $paramsForUse, "nextrun", "nextrun");
        //nextrun & enabled
        if (isset($params['nextrun'])){
            $params['enabled'] = "true"; //zorgt ervoor dat we onderstaande functie kunnen gebruiken.
            $this->addInIfNeeded($query, $params, $paramsForUse, "enabled", "enabled");
        }

        $query .= $eindhaakje;
    }

    public function buildTestbed(&$query, &$params, &$paramsForUse) {
        $this->addInIfNeeded($query, $params, $paramsForUse, "testbedname", "testbedName");
        $this->addInIfNeeded($query, $params, $paramsForUse, "urn", "urn");//correcte codereing in html anders wordt + een spatie
        $this->addInIfNeeded($query, $params, $paramsForUse, "url", "url");
    }

    public function buildUser(&$query, &$params, &$paramsForUse) {
        $this->addInIfNeeded($query, $params, $paramsForUse, "username", "username");
        $this->addInIfNeeded($query, $params, $paramsForUse, "userauthorityurn", "userauthorityurn");
    }

}
