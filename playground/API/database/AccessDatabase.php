<?php

include (__DIR__ . "/config.php"); //database config
//include(__DIR__.'/filters/QueryBuilder.php');

/**
 * this class will contact the database
 */
class AccessDatabase {

    private $testbeds;
    private $testDefinitions;
    private $testInstances;
    private $fetcher;
    private $queryBuilder;

    /**
     * creates an AccessDatabase object
     */
    public function __construct($queryBuilder,$fetcher) {
        $this->queryBuilder = $queryBuilder;
        $this->fetcher = $fetcher;
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
        //init
        $params = $request->getParameters();
        $query = "select * from ("
                . "select *,dense_rank() over(partition by testname,testdefinitionname order by timestamp desc) rank from list"
                . ") vv ";
        $paramsForUse = array();
        
        //add all needed filters to query
        $request->getQb()->buildList($query, $params, $paramsForUse);
        //echo $query;
        //print_r($paramsForUse);

        //run query
        $con = $this->getConnection();
        $result = pg_query_params($con, $query, $paramsForUse);
        
        //fetch results
        $data = array();
        $request->getFetcher()->FetchList($result,$data,$this->testDefinitions);
        
        //close & return
        $this->closeConnection($con);
        return $data;
    }

    /**
     * gets the testbeds, testdefinition & testinstances and puts them in memory providing fast access
     */
    public function updateCache() {
        $this->testbeds = $this->getTestbed(new Request($this->fetcher,$this->queryBuilder));

        $this->testDefinitions = $this->getTestDefinition(new Request($this->fetcher,$this->queryBuilder));
        $this->testInstances = $this->getTestInstance(new Request($this->fetcher,$this->queryBuilder));
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
        $request->getQb()->buildDefinition($query, $params, $paramsForUse);

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
        $query = "select *,nextrun  from instances ";
        $paramsForUse = array();
       
        $request->getQb()->buildTestInstance($query, $params, $paramsForUse);

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

        $request->getQb()->buildTestbed($query, $params, $paramsForUse);

        $con = $this->getConnection();
        $result = pg_query_params($con, $query, $paramsForUse);
        $data = array();
        $request->getFetcher()->fetchTestbed($result, $data);
        $this->closeConnection($con);
        return $data;
    }
    
    /**
     * gets the users filtered by the request
     * @param Request $request the request
     * @return array (username => user)
     */
    public function getUser(&$request){
        $params = $request->getParameters();
        $query = "select * from users";
        $paramsForUse = array();
        $con = $this->getConnection();
        
        $request->getQb()->buildUser($query,$params,$paramsForUse);
        $result = pg_query_params($con,$query,$paramsForUse);
        $data = array();
        $request->getFetcher()->fetchUser($result,$data);
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
        if (isset($params['testinstanceid']) && isset($this->testInstances[$params['testinstanceid'][0]]) ){//&& strtoupper($request->getVerb()) == 'POST') {
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
            echo $request->getMsg();
            echo "<br>";
        } else {
            //$valid=False;
            $request->addMsg("testinstanceid not given or not valid! And/Or method not post");
            $request->setStatus(400);
        }

        echo $request->getMsg();
        echo "<br>";
            
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
            $newTime = date("c", $params['nextrun'][0]/1000);
            $oldTime = date("c", strtotime($this->testInstances[$params['testinstanceid'][0]]['nextrun']));
            echo "new: $newTime old: $oldTime\n";
            //timestampe must be past the last timestamp
            if ($newTime > $oldTime) {
                //$newTime /= 1000; //to secs
                
                echo "timestamp ok!\n";
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

}
