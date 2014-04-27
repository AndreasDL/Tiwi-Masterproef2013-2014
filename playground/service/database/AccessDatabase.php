<?php

include (__DIR__ . "/config.php"); //database config

/**
 * this class will contact the database
 */
class AccessDatabase {

    private $testbeds;
    private $testDefinitions;
    private $testInstances;

    /**
     * creates an AccessDatabase object
     */
    public function __construct() {
        //cache for faster processing of results
        $this->updateCache(); //eventueel op aparty thread steken en zo updaten
    }

    //resultcalls
    /**
     * Will return the last results
     * @param Request $request the request
     * @return array the last results filtered by the request
     */
    public function getLast(&$request) {
        //last => indien count niet gezet => op 1 zetten
        $params = $request->getParameters();
        if (!isset($params['count'])) {
            $params['count'] = array(1);
        }
        $request->setParameters($params);
        return $this->getList($request);
    }

    /**
     * returns a list of results limited to the 100 last results unless used in combination with to & from
     * @param type $request the request
     * @return type list of results filtered by the request
     */
    public function getList(&$request) {
        $params = $request->getParameters();
        if (!isset($params['till']) && !isset($params['from']) && isset($params['count']) && $params['count'][0] > $GLOBALS['maxList']) {
            $request->addMsg("Warn: count value higher that max allowed (" . $GLOBALS['maxList'] . "). Using max as count.");
            $params['count'] = $GLOBALS['maxList'];
        } else if (!isset($params['count'])) {
            $params['count'] = array($GLOBALS['maxList']);
        }
        $query = "select * from ("
                . "select *,dense_rank() over(partition by testname,testdefinitionname order by timestamp desc) rank from list"
                . ") vv ";

        $paramsForUse = array();
        $eindhaakje = "";

        //testbeds
        $this->addAnyIfNeeded($query, $params, $paramsForUse, "testbed", "list");
        if (sizeof($paramsForUse) > 0) {
            $eindhaakje = ')';
        }
        //testtypes
        //$this->addInIfNeeded($query, $params, $paramsForUse, "testtype", "testtype");
        $this->addInIfNeeded($query, $params, $paramsForUse, "testdefinitionname", "testdefinitionname");
        //status => per subtest nu !!
        $this->addInIfNeeded($query, $params, $paramsForUse, "status", "value");
        //resultid
        $this->addInIfNeeded($query, $params, $paramsForUse, "resultId", "id");
        //testname
        $this->addInIfNeeded($query, $params, $paramsForUse, "testname", "testname");
        //from
        $this->addGreaterThanIfNeeded($query, $params, $paramsForUse, "from", "timestamp");
        //till
        $this->addLowerThanIfNeeded($query, $params, $paramsForUse, "till", "timestamp");
        //count
        $this->addLowerThanIfNeeded($query, $params, $paramsForUse, "count", "rank");
        //haakje van any
        $query.=$eindhaakje;

        //print $query;

        $con = $this->getConnection();
        $result = pg_query_params($con, $query, $paramsForUse);
        $data = array();
        $request->getFetcher()->FetchList($result,$data,$this->testDefinitions);
        
        //echo $query;
        $this->closeConnection($con);
        return $data;
    }

    /**
     * gets the testbeds, testdefinition & testinstances and puts them in memory providing fast access
     */
    public function updateCache() {
        $this->testbeds = $this->getTestbed(new Request(new defaultFetcher()));
        $this->testDefinitions = $this->getTestDefinition(new Request(new defaultFetcher()));
        $this->testInstances = $this->getTestInstance(new Request(new defaultFetcher()));
    }

    //Config Calls
    /**
     * gets the definitions from the database, filtered by the request
     * @param Request $request the request
     * @return array the testdefinitions
     */
    public function getTestDefinition(&$request) {
        $params = $request->getParameters();
        $query = "select * from definitions";

        $paramsForUse = array();
        $this->addInIfNeeded($query, $params, $paramsForUse, "testtype", "tetyp");
        $this->addInIfNeeded($query, $params, $paramsForUse, "testdefinitionname", "testdefinitionname");

        $con = $this->getConnection();
        $result = pg_query_params($con, $query, $paramsForUse);
        $data = array();
        $request->getFetcher()->fetchDefinition($result, $data);

        $this->closeConnection($con);
        return $data;
    }

    /**
     * Returns the testinstances filtered by the request
     * @param Request $request the request
     * @return array the instances
     */
    public function getTestInstance(&$request) {
        $params = $request->getParameters();
        $query = "select *,nextrun  from instances "; //AT TIME ZONE 'CET'
        $paramsForUse = array();
        $eindhaakje = "";

        $this->addAnyIfNeeded($query, $params, $paramsForUse, "testbed", "instances");
        if (sizeof($paramsForUse) > 0) {
            $eindhaakje = ')';
        }
        $this->addInIfNeeded($query, $params, $paramsForUse, "testdefinitionname", "testdefinitionname");
        $this->addInIfNeeded($query, $params, $paramsForUse, "testname", "testname");
        $this->addInIfNeeded($query, $params, $paramsForUse, "testinstanceid", "testinstanceid");
        $this->addLowerThanIfNeeded($query, $params, $paramsForUse, "nextrun", "nextrun");

        $query .= $eindhaakje;

        $con = $this->getConnection();
        $result = pg_query_params($con, $query, $paramsForUse);
        $data = array();
        $request->getFetcher()->fetchTestInstance($result,$data);

        $this->closeConnection($con);
        return $data;
    }

    /**
     * gets the testbeds filtered by the request
     * @param Request $request the request
     * @return array(testbedname => array(urn,url))
     */
    public function getTestbed(&$request) {
        $params = $request->getParameters();
        $query = "select * from testbeds";
        $paramsForUse = array();
        $this->addInIfNeeded($query, $params, $paramsForUse, "testbed", "testbedName");

        $con = $this->getConnection();
        $result = pg_query_params($con, $query, $paramsForUse);
        $data = array();
        $request->getFetcher()->fetchTestbed($result, $data);
        $this->closeConnection($con);
        return $data;
    }

    //push calls

    /**
     * adds a result to the database when the request is valid.
     * A request is valid when the method/verb is post, the testinstance exists and all subresults defined in the testdefinition are given
     * @param Request $request the request
     */
    public function addResult(&$request) {
        //if ($request->getVerb() == 'POST'){
        $params = $request->getParameters();
        //print_r($params);
        //testinstanceid given & existing?  => ook mogelijk in database
        $valid = FALSE;
        $returnVals;
        if (isset($params['testinstanceid']) && isset($this->testInstances[$params['testinstanceid'][0]]) && strtoupper($request->getVerb()) == 'POST') {
            $valid = True;
            //kijk of alle return values opgegeven zijn
            $returnVals = $this->testDefinitions[$this->testInstances[$params['testinstanceid'][0]]['testdefinitionname']]['returnValues']; //['testtype']]['returnValues'];

            foreach ($returnVals as $key => $val) {
                if (!isset($params[$key][0])) {
                    $valid = False;
                    $request->addMsg($key . " Not given");
                    $request->setStatus(400);
                    break;
                }
            }
        } else {
            //$valid=False;
            $request->addMsg("testinstanceid not given or not valid! And/Or method not post");
            $request->setStatus(400);
        }

        if ($valid) {
            //TODO transaction
            $query = "insert into results (testinstanceid,log) values ($1,$2);";
            $subQuery = "insert into subresults(resultId,returnName,returnValue) values(lastval(),$1,$2);";
            $con = $this->getConnection();

            //result
            $data = array(
                $params['testinstanceid'][0],
                $params['log'][0]
            );
            pg_query($con, "BEGIN");
            $success = pg_query_params($con, $query, $data);

            //subresults
            foreach ($returnVals as $key => $val) {
                $success = $success && pg_query_params($con, $subQuery, array($key, $params[$key][0]));
            }

            if (!($success && pg_query($con, "COMMIT"))) {
                //committing failed?
                pg_query($con, "ROLLBACK");
            }

            $this->closeConnection($con);
        }
    }

    /**
     * updates the nextrun of the testinstance when the testinstance exists and the given nextrun is after the previousone.
     * @param Request $request
     */
    public function updateNextRun(&$request) {
        $params = $request->getParameters();
        //print_r($params);


        if (isset($params['nextrun']) && isset($params['testinstanceid']) && isset($this->testInstances[$params['testinstanceid'][0]]) && strtoupper($request->getVerb()) == 'POST') {
            $newTime = date("c", $params['nextrun'][0]);
            $oldTime = date("c", strtotime($this->testInstances[$params['testinstanceid'][0]]['nextrun']));
            //echo "new: $newTime old: $oldTime\n";
            //timestampe must be past the last timestamp
            if ($newTime > $oldTime) {
                //echo "timestamp ok!\n";
                //update in database
                $con = $this->getConnection();
                $query = "update testinstances SET nextrun = $1 where testinstanceid=$2;";
                //echo "newTime: " .$newTime . "\n";
                //$insTime = $newTime;
                //echo "insTime: " . $insTime . "\n";
                //echo "no: " . time() . "\n";
                $data = array($newTime, $params['testinstanceid'][0]);

                pg_query_params($con, $query, $data);
                $this->closeConnection($con);
            } else {
                $request->setStatus(400);
                $request->addMSg("nextrun timestamp must be after the current nextrun!");
            }
        } else {
            $request->addMsg("Either testinstanceid not given or not valid!, nextrun not given or method not post");
            $request->setStatus(400);
        }

        //echo "old: $oldTime new: $newTime";
    }

    //fix connection
    /**
     * returns the connection with the database
     * @return con the connection to the postgresql database
     */
    private function getConnection() {
        $con = pg_connect($GLOBALS['conString']) or die("Couldn't connect to database");
        return $con;
    }

    /**
     * closes the connection
     * @param type $con
     */
    private function closeConnection($con) {
        pg_close($con);
    }

    //fix query
    /**
     * Will add and any clause to the query. Use with caution after this function is used you need to check if there are parameters added in the paramsforuse.
     * If so the ending bracelet ')' should be added after all other parameters are added to the query.
     * This function will only add a clausule if the parameter is set.
     * @param string $query the query to add the any clausule
     * @param array $params parameters of the request
     * @param array $paramsForUse the array to use with pg_query_params
     * @param string $paramName the name of the parameter in the request
     * @param string $viewName the name of the view to use
     * @param string $colName the name in this case id 
     */
    private function addAnyIfNeeded(&$query, &$params, &$paramsForUse, $paramName, $viewName, $colName = 'id') {
        //not sure if this works 2 times on the same query
        if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            if (sizeof($paramsForUse) == 0) {
                $query .= " where ";
            } else {
                $query .= " and ";
            }
            $query .= $colName . "=any(select " . $colName . " from " . $viewName . " where parametervalue IN (";

            array_push($paramsForUse, $params[$paramName][0]);
            $query .= '$';
            $query .= sizeof($paramsForUse);

            for ($i = 1; $i < sizeof($params[$paramName]); $i++) {
                array_push($paramsForUse, $params[$paramName][$i]);
                $query .= ',$';
                $query .= sizeof($paramsForUse);
            }
            $query .= ") ";
        }
    }

    /**
     * adds an in clausule if the paramName is set in params
     * @param string $query the query to add the any clausule
     * @param array $params parameters of the request
     * @param array $paramsForUse the array to use with pg_query_params
     * @param string $paramName the name of the parameter in the request
     * @param string $colName the name of the column in the database associated with the paramname
     */
    private function addInIfNeeded(&$query, &$params, &$paramsForUse, $paramName, $colName) {

        if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            if (sizeof($paramsForUse) == 0) {
                $query .= " where ";
            } else {
                $query .= " and ";
            }

            $query .= $colName . " IN (";
            array_push($paramsForUse, $params[$paramName][0]);
            $query .= "$";
            $query .= sizeof($paramsForUse);
            for ($i = 1; $i < sizeof($params[$paramName]); $i++) {
                array_push($paramsForUse, $params[$paramName][$i]);
                $query .= ",$";
                $query .= sizeof($paramsForUse);
            }
            $query .= ") ";
        }
    }

    /**
     * adds an >= clausule if the paramName is set in params. although the name is greaterthan it is actually greater than or equal
     * @param string $query the query to add the any clausule
     * @param array $params parameters of the request
     * @param array $paramsForUse the array to use with pg_query_params
     * @param string $paramName the name of the parameter in the request
     * @param string $colName the name of the column in the database associated with the paramname
     */
    private function addGreaterThanIfNeeded(&$query, &$params, &$paramsForUse, $paramName, $colName) {
        if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            if (sizeof($paramsForUse) == 0) {
                $query .= "where ";
            } else {
                $query .= "and ";
            }

            $query .= $colName . " >= ";
            array_push($paramsForUse, $params[$paramName][0]);
            $query .= "$";
            $query .= sizeof($paramsForUse);
        }
    }

    /**
     * adds an <= clausule if the paramName is set in params. although the name is lowerthan it is actually lower than or equal
     * @param string $query the query to add the any clausule
     * @param array $params parameters of the request
     * @param array $paramsForUse the array to use with pg_query_params
     * @param string $paramName the name of the parameter in the request
     * @param string $colName the name of the column in the database associated with the paramname
     */
    private function addLowerThanIfNeeded(&$query, &$params, &$paramsForUse, $paramName, $colName) {
        if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            if (sizeof($paramsForUse) == 0) {
                $query .= "where ";
            } else {
                $query .= "and ";
            }

            $query .= $colName . " <= ";
            array_push($paramsForUse, $params[$paramName][0]);
            $query .= "$";
            $query .= sizeof($paramsForUse);
        }
    }

}
