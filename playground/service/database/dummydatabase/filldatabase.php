<?php

include (__DIR__ . "/../config.php"); //database config
$aantalTestbeds = 17;
$aantalpinginstances = $aantalTestbeds;
$aantalstitchinstances = 5;
$resultsPerInstances = 10;

$login = 'postgres';
$pass = "post";
$dbname = "testdb";
$conString = "dbname=" . $dbname . " user=" . $login . " password=" . $pass;

//connectie maken
$con = pg_Connect($conString) or die('connection failed');
echo "connection established\n";

//in pompen
//testbeds
echo "Creating Testbeds\n";
$query = "insert into testbeds (testbedid,name) values($1,$2);";
for ($i = 0; $i < $aantalTestbeds; $i++) {
    $data = array("urn-testbed$i", "testbed$i");
    pg_query_params($con, $query, $data);
}

//testdefinitions
echo "Creating TestDefinitions\n";
$subQuery = "insert into parameterDefinitions (testType,parameterName,parameterType,parameterDescription) values ($1,$2,$3,$4);";
$retQuery = "insert into returnDefinitions (testType,returnName,returnType,returnDescription) values ($1,$2,$3,$4);";
$query = "insert into testdefinitions (testtype,testcommand) values($1,$2);";

echo "\tCreating Ping test\n";
$data = array("ping", "timeout", "integer", "timeout for ping test");
pg_query_params($con, $subQuery, $data);
$data = array("ping", "testbedId", "testbedId", "testbed for ping test");
pg_query_params($con, $subQuery, $data);
$data = array('ping', 'ping');
pg_query_params($con, $query, $data);
$data = array('ping', 'pingValue', 'integer', 'ping value');
pg_query_params($con, $retQuery, $data);

echo "\tCreating Stitching test\n";
$data = array("stitch", "topology", "string", "ring | line");
pg_query_params($con, $subQuery, $data);
$data = array("stitch", "testbedId", "testbedId[]", "multiple testbeds for ping test");
pg_query_params($con, $subQuery, $data);
$data = array('stitch', 'stitch');
pg_query_params($con, $query, $data);

$data = array('stitch', 'setUp', 'string', 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'getUserCredential', 'string', 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'generateRspec', 'string', 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'createSlice', 'string', 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'initStitching', 'string', 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'callSCS', 'string', 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'callCreateSlivers', 'string', 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'waitForAllReady', 'string', 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'loginAndPing', 'string', 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'callDeletes', 'string', 'status of subtest');
pg_query_params($con, $retQuery, $data);

//testinstances
echo "Creating TestInstances\n";
//ping
echo "\tCreating Ping testInstances\n";
$query = "insert into testinstances (testname,testtype,frequency) values ($1,$2,$3);";
$subQuery = "insert into parameterInstances (testinstanceId,parameterName,parametervalue) values (lastval(),$1,$2)";
for ($i = 0; $i < $aantalpinginstances; $i++) {
    $data = array(
        "ping voor testbed" . $i,
        'ping',
        '60'
    );
    pg_query_params($con, $query, $data);

    $data = array(
        'timeout',
        '300'
    );
    pg_query_params($con, $subQuery, $data);
    $data = array(
        'testbed',
        'urn-testbed' . $i
    );
    pg_query_params($con, $subQuery, $data);
}


//stitching
echo "\tCreating stitching testinstances\n";
for ($i = 0; $i < $aantalstitchinstances; $i++) {
    $data = array(
        "stiching" . $i,
        'stitch',
        '3600'
    );
    pg_query_params($con, $query, $data);

    $data = array(
        'topology',
        'ring'
    );
    pg_query_params($con, $subQuery, $data);
    $data = array(
        'testbedId',
        'urn-testbed' . $i
    );
    pg_query_params($con, $subQuery, $data);
    $data = array(
        'testbedId',
        'urn-testbed' . ($i + 1) % $aantalTestbeds
    );
    pg_query_params($con, $subQuery, $data);
    $data = array(
        'testbedId',
        'urn-testbed' . ($i + 2) % $aantalTestbeds
    );
    pg_query_params($con, $subQuery, $data);
}

//results &subresults
echo "creating results\n";
echo "!!Warning this may take some time because the script sleeps after every round to get different timestamps\n";
$query = "insert into results (testinstanceid,log) values ($1,$2);";
$subQuery = "insert into subresults(resultId,name,value) values(lastval(),$1,$2);";
for ($j = 1; $j < $resultsPerInstances; $j++) {
    echo "\tround $j \n";
    $instanceid = 1;
    //pings
    for ($i = 0; $i < $aantalpinginstances; $i++) {
        $data = array(
            "$instanceid",
            "http://f4f-mon-dev.intec.ugent.be/logs/$instanceid/" . rand(0, 10000)
        );
        pg_query_params($con, $query, $data);

        $data = array('pingValue', rand(0, 240));
        pg_query_params($con, $subQuery, $data);

        $instanceid++;
    }

    //testbeds
    for ($i = 0; $i < $aantalstitchinstances; $i++) {
        $data = array(
            "$instanceid",
            "http://f4f-mon-dev.intec.ugent.be/logs/$instanceid/" . rand(0, 10000)
        );
        pg_query_params($con, $query, $data);

        $data = array(
            'setUp',
            'Good'
        );
        pg_query_params($con, $subQuery, $data);
        $data = array(
            'getUserCredential',
            'Good'
        );
        pg_query_params($con, $subQuery, $data);
        $data = array(
            'generateRspec',
            'Good'
        );
        pg_query_params($con, $subQuery, $data);
        $data = array(
            'createSlice',
            'Good'
        );
        pg_query_params($con, $subQuery, $data);
        $data = array(
            'initStitching',
            'Good'
        );
        pg_query_params($con, $subQuery, $data);
        $data = array(
            'callSCS',
            'Good'
        );
        pg_query_params($con, $subQuery, $data);
        $data = array(
            'callCreateSlivers',
            'Good'
        );
        pg_query_params($con, $subQuery, $data);
        $data = array(
            'waitForAllReady',
            'FATAL'
        );
        pg_query_params($con, $subQuery, $data);
        $data = array(
            'loginAndPing',
            'SKIP'
        );
        pg_query_params($con, $subQuery, $data);
        $data = array(
            'CALLDeletes',
            'Warn'
        );
        pg_query_params($con, $subQuery, $data);


        $instanceid++;
    }

    sleep(3); //timestamps verschillend maken
}

//connectie sluiten
pg_close($con);
echo "done\n";
?>