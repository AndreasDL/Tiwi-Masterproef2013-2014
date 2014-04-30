<?php

include(__DIR__.'/QueryBuilder.php');
/**
 * will filter with the default values
 * @author Andreas De Lille
 */
class defaultFilter implements iFilter {

    private $qb;

    function __construct() {
        $this->qb = new QueryBuilder();
    }

    public function filterList(&$query, &$params, &$paramsForUse) {
        if (!isset($params['till']) && !isset($params['from']) && isset($params['count']) && $params['count'][0] > $GLOBALS['maxList']) {
            $request->addMsg("Warn: count value higher that max allowed (" . $GLOBALS['maxList'] . "). Using max as count.");
            $params['count'] = $GLOBALS['maxList'];
        } else if (!isset($params['count'])) {
            $params['count'] = array($GLOBALS['maxList']);
        }

        $eindhaakje = "";
        //testbeds
        $this->qb->addAnyIfNeeded($query, $params, $paramsForUse, "testbed", "list");
        if (sizeof($paramsForUse) > 0) {
            $eindhaakje = ')';
        }
        //testtypes
        //$this->addInIfNeeded($query, $params, $paramsForUse, "testtype", "testtype");
        $this->qb->addInIfNeeded($query, $params, $paramsForUse, "testdefinitionname", "testdefinitionname");
        //status => per subtest nu !!
        $this->qb->addInIfNeeded($query, $params, $paramsForUse, "status", "value");
        //resultid
        $this->qb->addInIfNeeded($query, $params, $paramsForUse, "resultId", "id");
        //testname
        $this->qb->addInIfNeeded($query, $params, $paramsForUse, "testname", "testname");
        //from
        $this->qb->addGreaterThanIfNeeded($query, $params, $paramsForUse, "from", "timestamp");
        //till
        $this->qb->addLowerThanIfNeeded($query, $params, $paramsForUse, "till", "timestamp");
        //count
        $this->qb->addLowerThanIfNeeded($query, $params, $paramsForUse, "count", "rank");
        //haakje van any
        $query.=$eindhaakje;
    }

    public function filterDefinition(&$query, &$params, &$paramsForUse) {
        $this->qb->addInIfNeeded($query, $params, $paramsForUse, "testtype", "tetyp");
        $this->qb->addInIfNeeded($query, $params, $paramsForUse, "testdefinitionname", "testdefinitionname");
    }

    public function filterTestInstance(&$query, &$params, &$paramsForUse) {
        $eindhaakje = "";

        $this->qb->addAnyIfNeeded($query, $params, $paramsForUse, "testbed", "instances");
        if (sizeof($paramsForUse) > 0) {
            $eindhaakje = ')';
        }
        $this->qb->addInIfNeeded($query, $params, $paramsForUse, "testdefinitionname", "testdefinitionname");
        $this->qb->addInIfNeeded($query, $params, $paramsForUse, "testname", "testname");
        $this->qb->addInIfNeeded($query, $params, $paramsForUse, "testinstanceid", "testinstanceid");
        $this->qb->addLowerThanIfNeeded($query, $params, $paramsForUse, "nextrun", "nextrun");

        $query .= $eindhaakje;
    }

    public function filterTestbed(&$query, &$params, &$paramsForUse) {
        $this->qb->addInIfNeeded($query, $params, $paramsForUse, "testbedName", "testbedName");
    }

}
