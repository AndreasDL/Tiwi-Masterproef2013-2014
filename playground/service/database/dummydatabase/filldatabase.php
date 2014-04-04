<?php

//include (__DIR__ . "/../config.php"); //database config
$aantalTestbeds = 7;
$aantalpinginstances = $aantalTestbeds;
$aantalstitchinstances = 2;
$aantallogininstances = 2;
$resultsPerInstances = 50;

$login = 'postgres';
$pass = "post";
$dbname = "testdb";
$conString = "dbname=" . $dbname . " user=" . $login . " password=" . $pass;

$puKey = "iminds";
$prKey = "virtualWall";

$authDir = "/home/drew/masterproef/site/service/auth/authorities.xml";
$outputDir = "/home/drew/masterproef/site/service/output/";

//connectie maken
$con = pg_Connect($conString) or die('connection failed');
echo "connection established\n";

//in pompen
//testbeds
$urls = array("iminds.be","facebook.Com","yahoo.com","google.com","hotmail.com");
echo "Creating Testbeds\n";
$query = "insert into testbeds (testbedName,url,urn) values($1,$2,$3);";
for ($i = 0; $i < $aantalTestbeds; $i++) {
    $data = array("testbed$i", $urls[rand(0,sizeof($urls)-1)] , "urn:publicid:IDN+wall2.ilabt.iminds.be+authority+cm");
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
$data = array("ping", "testbed", "testbed",  "name of testbed for ping test");
pg_query_params($con, $subQuery, $data);
$data = array('ping', "(fping -q -C 1 <testbed.url> 2>&1) | mawk '{print $3}'");
pg_query_params($con, $query, $data);
$data = array('ping', 'pingValue', 'integer', 'ping value');
pg_query_params($con, $retQuery, $data);

echo "\tCreating Stitching test\n";
$data = array("stitch", "topology", "string", "ring | line");
pg_query_params($con, $subQuery, $data);
$data = array("stitch", "testbed", "testbed[]", "multiple testbeds for stitching test");
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

echo "\tCreating Login test\n";
$data = array('login', 'java -ea -jar /work/jFed-bin/monitor/automated-testing-UNKNOWN_SVN_REVISION.jar --context-file /work/monitoring/contexts/login_scenarios/user=ftester-am=fiteagle_fuseco.properties --test-class be.iminds.ilabt.jfed.lowlevel.api.test.TestAggregateManager3 --group nodelogin --authorities-file /work/monitoring/authorities.xml --output-dir /work/monitoring/output/login_scenarios/user=ftester-am=fiteagle_fuseco/2014-04-02_15:35:47+02/test-output/');
pg_query_params($con, $query, $data);
$data = array("login", "context-file", "file", 'contextfile');
pg_query_params($con, $subQuery, $data);
$data = array("login", "test-class", "class" , 'class of test');
pg_query_params($con, $subQuery, $data);
$data = array("login", "group", "String", 'what');
pg_query_params($con, $subQuery, $data);
$data = array("login", "authorities-file", "file" , 'authoritiesfile');
pg_query_params($con, $subQuery, $data);
$data = array("login", "output-dir" , "directory", 'where to put output');
pg_query_params($con, $subQuery, $data);

$data = array('login', 'logfile', 'file','logfile');
pg_query_params($con, $retQuery, $data);
$data = array('login', 'exitStatus', 'file','exitstatus');
pg_query_params($con, $retQuery, $data);
$data = array('login', 'consoleoutput', 'file','consoleoutput');
pg_query_params($con, $retQuery, $data);

//testinstances
echo "Creating TestInstances\n";
$query = "insert into testinstances (testname,testtype,frequency) values ($1,$2,$3);";
pg_prepare($con,"query",$query);
$subQuery = "insert into parameterInstances (testinstanceId,parameterName,parametervalue) values (lastval(),$1,$2)";
pg_prepare($con,"subQuery",$subQuery);

//ping
echo "\tCreating Ping testInstances\n";
for ($i = 0; $i < $aantalpinginstances; $i++) {
    $data = array(
        "ping voor testbed" . $i,
        'ping',
        '60'
    );
    pg_execute($con,"query", $data);

    $data = array(
        'timeout',
        '300'
    );
    pg_execute($con, "subQuery", $data);
    $data = array(
        'testbed',
        'testbed' . $i
    );
    pg_execute($con, "subQuery", $data);
}
//stitching
echo "\tCreating stitching testinstances\n";
for ($i = 0; $i < $aantalstitchinstances; $i++) {
    $data = array(
        "stiching" . $i,
        'stitch',
        '3600'
    );
    pg_execute($con,"query", $data);

    $data = array(
        'topology',
        'ring'
    );
    pg_execute($con, "subQuery", $data);
    $data = array(
        'testbed',
        'testbed' . $i
    );
    pg_execute($con, "subQuery", $data);
    $data = array(
        'testbed',
        'testbed' . ($i + 1) % $aantalTestbeds
    );
    pg_execute($con, "subQuery", $data);
    $data = array(
        'testbed',
        'testbed' . ($i + 2) % $aantalTestbeds
    );
    pg_execute($con, "subQuery", $data);
}
//login
echo "\tCreating login testinstances\n";
for ($i = 0; $i < $aantallogininstances; $i++){
    $data = array("login" . $i,
            "login",
            "3600"
        );
    pg_execute($con,"query",$data);
    
    $data=array("context-file","username = ftester
passwordFilename = /work/monitoring/logins/ftester.pass
pemKeyAndCertFilename = /work/monitoring/logins/ftester_wall2.pem
userAuthorityUrn = urn:publicid:IDN+wall2.ilabt.iminds.be+authority+cm
testedAggregateManagerUrn = urn:publicid:IDN+fiteagle+authority+am");
    pg_execute($con,"subQuery",$data);
    
    $data=array("test-class","be.iminds.ilabt.jfed.lowlevel.api.test.TestAggregateManager3");
    pg_execute($con,"subQuery",$data);
    
    $data=array("group","nodelogin");
    pg_execute($con,"subQuery",$data);
    
    $data=array("authorities-file",$authDir);
    pg_execute($con,"subQuery",$data);
    
    $data=array("output-dir",$outputDir);
    pg_execute($con,"subQuery",$data);
    
}


//results &subresults
echo "creating results\n";
//echo "!!Warning this may take some time because the script sleeps after every round to get different timestamps\n";
$query = "insert into results (testinstanceid,log,timestamp) values ($1,$2,$3);";
pg_prepare($con,"query2",$query);
$subQuery = "insert into subresults(resultId,name,value) values(lastval(),$1,$2);";
pg_prepare($con,"subQuery2",$subQuery);
for ($j = 1; $j <= $resultsPerInstances; $j++) {
    echo "\tround $j \n";
    $instanceid = 1;
    //pings
    for ($i = 0; $i < $aantalpinginstances; $i++) {
        $data = array(
            "$instanceid",
            "http://f4f-mon-dev.intec.ugent.be/logs/$instanceid/" . rand(0, 10000),
            "2014-03-".rand(17,26)."T".rand(1,23).":".rand(0,59).":".rand(0,59)
        );
        pg_execute($con, "query2", $data);
        $pingVal = rand(30,240);
        if ($pingVal%7 == 0){//kans van 1 op 7 voor fatals
            $pingVal = -1;
        }
        $data = array('pingValue', $pingVal);
        pg_execute($con, "subQuery2", $data);

        $instanceid++;
    }

    //testbeds
    for ($i = 0; $i < $aantalstitchinstances; $i++) {
        $data = array(
            "$instanceid",
            "http://f4f-mon-dev.intec.ugent.be/logs/$instanceid/" . rand(0, 10000),
            "2014-03-".rand(1,25)."T".rand(1,23).":".rand(0,59).":".rand(0,59)
        );
        pg_execute($con, "query2", $data);
        
        $status= (rand(0,7)==1)?"Good":"Warn";
        $data = array(
            'setUp',
            $status
        );
        pg_execute($con, "subQuery2", $data);
        
        $status= (rand(0,7)==1)?"Warn":"Good";
        $data = array(
            'getUserCredential',
            $status
        );
        pg_execute($con, "subQuery2", $data);
        
        $status= (rand(0,7)==1)?"Warn":"Good";
        $data = array(
            'generateRspec',
            $status
        );
        pg_execute($con, "subQuery2", $data);
        $status= (rand(0,7)==1)?"Warn":"Good";
        $data = array(
            'createSlice',
            $status
        );
        pg_execute($con, "subQuery2", $data);
        $status= (rand(0,7)==1)?"Warn":"Good";
        $data = array(
            'initStitching',
            $status
        );
        pg_execute($con, "subQuery2", $data);
        $status= (rand(0,7)==1)?"Warn":"Good";
        $data = array(
            'callSCS',
            $status
        );
        pg_execute($con, "subQuery2", $data);
        $data = array(
            'callCreateSlivers',
            'FATAL'
        );
        pg_execute($con, "subQuery2", $data);
        $data = array(
            'waitForAllReady',
            'SKIP'
        );
        pg_execute($con, "subQuery2", $data);
        $data = array(
            'loginAndPing',
            'SKIP'
        );
        pg_execute($con, "subQuery2", $data);
        $data = array(
            'callDeletes',
            'Warn'
        );
        pg_execute($con, "subQuery2", $data);

        $instanceid++;
    }
}

echo "creating users\n";
$query = "insert into users (keyid,key) values ($1,$2)";
$data  = array($puKey , $prKey);
pg_query_params($con,$query,$data);



//connectie sluiten
pg_close($con);
echo "done\n";
?>