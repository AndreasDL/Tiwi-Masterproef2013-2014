<?php

//include (__DIR__ . "/../config.php"); //database config
$aantalTestbeds = 17;
$aantalpinginstances = $aantalTestbeds;
$aantalstitchinstances = 10;
//bepalen aantal login testen
$urns = array("fail" => "urn:publicid:IDN+omf+authority+sa", //failed direct
    "wall1" => "urn:publicid:IDN+wall1.ilabt.iminds.be+authority+cm", //virtualwall1
    "wall2" => "urn:publicid:IDN+wall2.ilabt.iminds.be+authority+cm"//virtualwall2
);
$resultsPerInstances = 20;

parse_str(implode('&', array_slice($argv, 1)), $_GET);
if (isset($_GET['testbeds'])){
    $aantalTestbeds = $_GET['testbeds'];
    $aantalpinginstances = $aantalTestbeds;
}
if(isset($_GET['results'])){
    $resultsPerInstances = $_GET['results'];
}
if(isset($_GET['stitch'])){
    $aantalstitchinstances = $_GET['stitch'];
}
if(isset($_GET['ping'])){
    $aantalpinginstances = $_GET['ping'];
}

$login = 'postgres';
$pass = "post";
$dbname = "testdb";
$conString = "dbname=" . $dbname . " user=" . $login . " password=" . $pass;

//wordt nog niet gebruikt
$puKey = "iminds";
$prKey = "virtualWall";

$homeDir = "/home/drew/";
$authDir = $homeDir . ".auth/";
$authFile = $authDir . "authorities.xml";

//connectie maken
$con = pg_Connect($conString) or die('connection failed');
echo "connection established\n";

//in pompen
//real
$query = "insert into testbeds (testbedName,url,urn) values($1,$2,$3);";
foreach ($urns as $name => $urn){
    $data=array($name,'http://www.'+$name+'.notreal',$urn);
    pg_query_params($con,$query,$data);
}
//fakes
$urls = array("iminds.be", "facebook.Com", "yahoo.com", "google.com", "hotmail.com");
$urnss = array("urn:publicid:IDN+wall2.ilabt.iminds.be+authority+cm",
    "urn:publicid:IDN+emulab.net+authority+cm",
    "urn:publicid:IDN+ple:ibbtple+authority+cm");
echo "Creating Testbeds\n";
for ($i = 0; $i < $aantalTestbeds; $i++) {
    $data = array("testbed$i", $urls[rand(0, sizeof($urls) - 1)], $urnss[$i % sizeof($urnss)]);
    pg_query_params($con, $query, $data);
}

//<editor-fold desc="Definitions" defaultstate="collapsed">
//testdefinitions
echo "Creating TestDefinitions\n";
$subQuery = "insert into parameterDefinitions (testDefinitionName,parameterName,parameterType,parameterDescription) values ($1,$2,$3,$4);";
$retQuery = "insert into returnDefinitions (testDefinitionName,returnName,returnType,returnDescription) values ($1,$2,$3,$4);";
$query = "insert into testdefinitions (testDefinitionName,testtype,testcommand) values($1,$2,$3);";

echo "\tCreating Ping test\n";
$data = array('ping','ping', "(fping -q -C 1 <testbed.url> 2>&1) | mawk '{print $3}'");
pg_query_params($con, $query, $data);
$data = array("ping", "timeout", "integer", "timeout for ping test");
pg_query_params($con, $subQuery, $data);
$data = array("ping", "testbed", "testbed", "name of testbed for ping test");
pg_query_params($con, $subQuery, $data);
$data = array('ping', 'pingValue', 'integer', 'ping value');
pg_query_params($con, $retQuery, $data);

echo "\tCreating Stitching test\n";
$data = array('stitch','stitch', '');
pg_query_params($con, $query, $data);
$data = array("stitch", "context-file", "file", "username = ftester
    passwordFilename = " . $authDir . "ftester.pass
    pemKeyAndCertFilename = " . $authDir . "getsslcert.txt
    userAuthorityUrn = <userAuthorityUrn>
    testedAggregateManagerUrn = <testedAggregateManager.urn>
    stitchedAuthorityUrns=urn:publicid:IDN+wall2.ilabt.iminds.be+authority+cm urn:publicid:IDN+utah.geniracks.net+authority+cm
    scsUrn = <scsUrn>
    scsUrl = <scsUrl>");
pg_query_params($con, $subQuery, $data);
$data = array("stitch", "userAuthorityUrn", "urn", "urn for authority");
pg_query_params($con, $subQuery, $data);
$data = array("stitch", "testedAggregateManager", "testbed", "testbed to run test on");
pg_query_params($con, $subQuery, $data);
$data = array("stitch", "scsUrn", "urn", "urn for authority");
pg_query_params($con, $subQuery, $data);
$data = array("stitch", "scsUrl", "url", "testbed to run test on");
pg_query_params($con, $subQuery, $data);

$data = array('stitch', 'resultHtml', 'file', 'results in html format');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'result-overview', 'file', 'results in xml format');
pg_query_params($con, $retQuery, $data);
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
$data = array('stitch', 'duration', 'long' , 'duration of test');
pg_query_params($con, $retQuery, $data);

echo "\tCreating Login test\n";
//login amv2
$data = array('login','login', ''); //--context-file <context-file>');
pg_query_params($con, $query, $data);
$data = array("login", "context-file", "file", "username = ftester
    passwordFilename = " . $authDir . "ftester.pass
    pemKeyAndCertFilename = " . $authDir . "getsslcert.txt
    userAuthorityUrn = <userAuthorityUrn>
    testedAggregateManagerUrn = <testedAggregateManager.urn>");
pg_query_params($con, $subQuery, $data);

$data = array("login", "userAuthorityUrn", "urn", "urn for authority");
pg_query_params($con, $subQuery, $data);
$data = array("login", "testedAggregateManager", "testbed", "testbed to run test on");
pg_query_params($con, $subQuery, $data);

$data = array('login', 'resultHtml', 'file', 'results in html format');
pg_query_params($con, $retQuery, $data);
$data = array('login', 'result-overview', 'file', 'results in xml format');
pg_query_params($con, $retQuery, $data);
$data = array('login', 'setUp', 'string', 'setup');
pg_query_params($con, $retQuery, $data);
$data = array('login', 'testGetVersionXmlRpcCorrectness', 'string', 'testGetVersionXmlRpcCorrectness');
pg_query_params($con, $retQuery, $data);
$data = array('login', 'testListResourcesAvailableNoSlice', 'string', '');
pg_query_params($con, $retQuery, $data);
$data = array('login', 'testCreateSliceSliver', 'string', '');
pg_query_params($con, $retQuery, $data);
$data = array('login', 'testCreateSliver', 'string', '');
pg_query_params($con, $retQuery, $data);
$data = array('login', 'testCreatedSliverBecomesReady', 'string', '');
pg_query_params($con, $retQuery, $data);
$data = array('login', 'checkManifestOnceSliverIsReady', 'string', '');
pg_query_params($con, $retQuery, $data);
$data = array('login', 'testNodeLogin', 'string', 'test node login');
pg_query_params($con, $retQuery, $data);
$data = array('login', 'testDeleteSliver', 'string', 'test delete sliver');
pg_query_params($con, $retQuery, $data);
$data = array('login', 'duration', 'long', 'duration of the test in millisecs');
pg_query_params($con, $retQuery, $data);
//login amv3
$data = array('login3','login3', '');
pg_query_params($con, $query, $data);
$data = array("login3", "context-file", "file", "username = ftester
    passwordFilename = " . $authDir . "ftester.pass
    pemKeyAndCertFilename = " . $authDir . "getsslcert.txt
    userAuthorityUrn = <userAuthorityUrn>
    testedAggregateManagerUrn = <testedAggregateManager.urn>");
pg_query_params($con, $subQuery, $data);

$data = array("login3", "userAuthorityUrn", "urn", "urn for authority");
pg_query_params($con, $subQuery, $data);
$data = array("login3", "testedAggregateManager", "testbed", "testbed to run test on");
pg_query_params($con, $subQuery, $data);

$data = array('login3', 'resultHtml', 'file', 'results in html format');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'result-overview', 'file', 'results in xml format');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'setUp', 'string', 'setup');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'testGetVersionXmlRpcCorrectness', 'string', 'testGetVersionXmlRpcCorrectness');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'createTestSlices', 'string', 'create slices');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'testAllocate', 'string', 'allocate test');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'testProvision', 'string', 'testProvision');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'testSliverBecomesProvisioned', 'string', 'test sliver becomes provisioned');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'testPerformOperationalAction', 'string', 'test perform operational action');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'testSliverBecomesStarted', 'string', 'test sliver becomes started');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'testNodeLogin', 'string', 'test node login');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'testDeleteSliver', 'string', 'test delete sliver');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'duration', 'long', 'duration of the test in millisecs');
pg_query_params($con, $retQuery, $data);



//generic
echo "\tCreating Generic Tests\n";
$data = array('loginGen','javaMain', '--context-file <context-file> '
    . '--test-class be.iminds.ilabt.jfed.lowlevel.api.test.TestAggregateManager2 '
    . '--group nodelogin '
    . '--output-dir <output-dir> '
    . '-q'
    ); //--context-file <context-file>');
pg_query_params($con, $query, $data);
$data = array("loginGen", "context-file", "file", "username = ftester
    passwordFilename = " . $authDir . "ftester.pass
    pemKeyAndCertFilename = " . $authDir . "getsslcert.txt
    userAuthorityUrn = <userAuthorityUrn>
    testedAggregateManagerUrn = <testedAggregateManager.urn>");
pg_query_params($con, $subQuery, $data);

$data = array("loginGen", "userAuthorityUrn", "urn", "urn for authority");
pg_query_params($con, $subQuery, $data);
$data = array("loginGen", "testedAggregateManager", "testbed", "testbed to run test on");
pg_query_params($con, $subQuery, $data);
//ye i really did spend like 3 hours looking at my code, because i forgot to add this line
$data = array("loginGen", "output-dir" , 'auto', 'generate an output directory');
pg_query_params($con,$subQuery,$data);

$data = array('loginGen', 'resultHtml', 'file', 'results in html format');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'result-overview', 'file', 'results in xml format');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'setUp', 'string', 'setup');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'testGetVersionXmlRpcCorrectness', 'string', 'testGetVersionXmlRpcCorrectness');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'testListResourcesAvailableNoSlice', 'string', '');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'testCreateSliceSliver', 'string', '');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'testCreateSliver', 'string', '');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'testCreatedSliverBecomesReady', 'string', '');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'checkManifestOnceSliverIsReady', 'string', '');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'testNodeLogin', 'string', 'test node login');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'testDeleteSliver', 'string', 'test delete sliver');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'duration', 'long', 'duration of the test in millisecs');
pg_query_params($con, $retQuery, $data);

// </editor-fold>

//<editor-fold desc="instances" defaultstate="collapsed">
//testinstances
echo "Creating TestInstances\n";
$query = "insert into testinstances (testname,testDefinitionName,frequency,enabled) values ($1,$2,$3,$4);";
pg_prepare($con, "query", $query);
$subQuery = "insert into parameterInstances (testinstanceId,parameterName,parametervalue) values (lastval(),$1,$2)";
pg_prepare($con, "subQuery", $subQuery);

//ping
echo "\tCreating Ping testInstances\n";
for ($i = 0; $i < $aantalpinginstances; $i++) {
    $data = array(
        "ping voor testbed" . $i,
        'ping',
        '60',
        true//(($i%2==1)?'true':'false')
    );
    pg_execute($con, "query", $data);

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
        "stitching" . $i,
        'stitch',
        '3600',
        true
    );
    pg_execute($con, "query", $data);

    $data = array("userAuthorityUrn", "urn:publicid:IDN+wall2.ilabt.iminds.be+authority+cm");
    pg_execute($con, "subQuery", $data);
    $data = array("testedAggregateManager", $name);
    pg_execute($con, "subQuery", $data);
    $data = array("scsUrn", "urn:publicid:IDN+geni.maxgigapop.net+auth+am");
    pg_execute($con, "subQuery", $data);
    $data = array("scsUrl", "http://geni.maxgigapop.net:8081/geni/xmlrpc");
    pg_execute($con, "subQuery", $data);

}
//login
echo "\tCreating login amv2 & amv3 testinstances\n";
foreach ($urns as $name => $urn) {
    $data = array($name,
        "login",
        "3600",
        true
    );
    pg_execute($con, "query", $data);

    $data = array("userAuthorityUrn", "urn:publicid:IDN+wall2.ilabt.iminds.be+authority+cm");
    pg_execute($con, "subQuery", $data);
    $data = array("testedAggregateManager", $name);
    pg_execute($con, "subQuery", $data);
    
    $data = array($name . "v3",
        "login3",
        "3600",
        true
    );
    pg_execute($con, "query", $data);
    $data = array("userAuthorityUrn", "urn:publicid:IDN+wall2.ilabt.iminds.be+authority+cm");
    pg_execute($con, "subQuery", $data);
    $data = array("testedAggregateManager", $name);
    pg_execute($con, "subQuery", $data);
    
    
    //generic
    $data = array($name . "gen",
        "loginGen",
        "3600",
        true
    );
    pg_execute($con, "query", $data);

    $data = array("userAuthorityUrn", "urn:publicid:IDN+wall2.ilabt.iminds.be+authority+cm");
    pg_execute($con, "subQuery", $data);
    $data = array("testedAggregateManager", $name);
    pg_execute($con, "subQuery", $data);
}
// </editor-fold>

//<editor-fold desc="Results" defaultstate="collapsed">
//results &subresults
echo "creating results\n";
//echo "!!Warning this may take some time because the script sleeps after every round to get different timestamps\n";
$query = "insert into results (testinstanceid,log,timestamp) values ($1,$2,$3);";
pg_prepare($con, "query2", $query);
$subQuery = "insert into subresults(resultId,returnName,returnValue) values(lastval(),$1,$2);";
pg_prepare($con, "subQuery2", $subQuery);
for ($j = 1; $j <= $resultsPerInstances; $j++) {
    echo "\tround $j \n";
    $instanceid = 1;
    //pings
    for ($i = 0; $i < $aantalpinginstances; $i++) {
        $data = array(
            "$instanceid",
            "http://f4f-mon-dev.intec.ugent.be/logs/$instanceid/" . rand(0, 10000),
            "2014-03-" . rand(17, 26) . "T" . rand(1, 23) . ":" . rand(0, 59) . ":" . rand(0, 59)
        );
        pg_execute($con, "query2", $data);
        $pingVal = rand(30, 240);
        if ($pingVal % 7 == 0) {//kans van 1 op 7 voor fatals
            $pingVal = -1;
        }
        $data = array('pingValue', $pingVal);
        pg_execute($con, "subQuery2", $data);

        $instanceid++;
    }

    //stitch
    for ($i = 0; $i < $aantalstitchinstances; $i++) {
        $data = array(
            "$instanceid",
            "http://f4f-mon-dev.intec.ugent.be/logs/$instanceid/" . rand(0, 10000),
            "2014-03-" . rand(1, 25) . "T" . rand(1, 23) . ":" . rand(0, 59) . ":" . rand(0, 59)
        );
        pg_execute($con, "query2", $data);

        $status = (rand(0, 7) == 1) ? "Good" : "Warn";
        $data = array(
            'setUp',
            $status
        );
        pg_execute($con, "subQuery2", $data);

        $status = (rand(0, 7) == 1) ? "Warn" : "Good";
        $data = array(
            'getUserCredential',
            $status
        );
        pg_execute($con, "subQuery2", $data);

        $status = (rand(0, 7) == 1) ? "Warn" : "Good";
        $data = array(
            'generateRspec',
            $status
        );
        pg_execute($con, "subQuery2", $data);
        $status = (rand(0, 7) == 1) ? "Warn" : "Good";
        $data = array(
            'createSlice',
            $status
        );
        pg_execute($con, "subQuery2", $data);
        $status = (rand(0, 7) == 1) ? "Warn" : "Good";
        $data = array(
            'initStitching',
            $status
        );
        pg_execute($con, "subQuery2", $data);
        $status = (rand(0, 7) == 1) ? "Warn" : "Good";
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

        $data = array(
            'duration',
            rand(3600000, 7200000)
        );
        pg_execute($con, "subQuery2", $data);

        $data = array(
            'resultHtml',
            ''
        );
        pg_execute($con, "subQuery2", $data);

        $data = array(
            'result-overview',
            ''
        );
        pg_execute($con, "subQuery2", $data);


        $instanceid++;
    }
}
// </editor-fold>

echo "creating users\n";
$query = "insert into users (keyid,key) values ($1,$2)";
$data = array($puKey, $prKey);
pg_query_params($con, $query, $data);

//connectie sluiten
pg_close($con);
echo "done\n";
?>