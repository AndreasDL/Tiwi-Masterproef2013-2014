<?php

//include (__DIR__ . "/../config.php"); //database config
$aantalTestbeds = 17;
$aantalpinginstances = $aantalTestbeds;
$aantalstitchinstances = 10;
//bepalen aantal login testen
$urns = array("fail" => array("urn" => "urn:publicid:IDN+omf+authority+sa", //failed direct
        "url" => "google.Com"),
    "wall1" => array("urn" => "urn:publicid:IDN+wall1.ilabt.iminds.be+authority+cm",
        "url" => "www.wall1.ilabt.iminds.be"), //virtualwall1
    "wall2" => array("urn" => "urn:publicid:IDN+wall2.ilabt.iminds.be+authority+cm",
        "url" => "www.wall2.ilabt.iminds.be")//virtualwall2
);
$resultsPerInstances = 20;

$homeDir = "/home/drew/";


$login = 'postgres';
$pass = "post";
$dbname = "testdb";
$conString = "dbname=" . $dbname . " user=" . $login . " password=" . $pass;

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
if (isset($_GET['home'])){
    $homeDir = $_GET['home'];
}

//connectie maken
$con = pg_Connect($conString) or die('connection failed');
echo "connection established\n";

$authDir = $homeDir . ".auth/";
$authFile = $authDir . "authorities.xml";

//in pompen
/*
//real
$query = "insert into testbeds (testbedName,url,urn) values($1,$2,$3);";
foreach ($urns as $name => $bed){
    $data=array($name,$bed['url'],$bed['urn']);
    pg_query_params($con,$query,$data);
}
*/
//<editor-fold desc="Definitions" defaultstate="collapsed">
//testdefinitions
echo "Creating TestDefinitions\n";
$subQuery = "insert into parameterDefinitions (testDefinitionName,parameterName,parameterType,parameterDescription) values ($1,$2,$3,$4);";
$retQuery = "insert into returnDefinitions (testDefinitionName,returnName,returnType,returnIndex,returnDescription) values ($1,$2,$3,$4,$5);";
$query = "insert into testdefinitions (testDefinitionName,testtype,geniDatastoreTestname,geniDatastoredesc,geniDatastoreUnits,testcommand) values($1,$2,$3,$4,$5,$6);";

echo "\tCreating Ping test\n";
$data = array('ping','ping', "", "", "" ,"(fping -q -C 1 <testbed.url> 2>&1) | mawk '{print $3}'");
pg_query_params($con, $query, $data);
$data = array("ping", "testbed", "testbed", "name of testbed for ping test");
pg_query_params($con, $subQuery, $data);

$data = array('ping', 'pingValue', 'integer',1, 'ping value');
pg_query_params($con, $retQuery, $data);

echo "\tCreating Stitching test\n";
$data = array('stitch','stitch', 'ops_monitoring:stitching', 'stichting test between multiple testbeds','boolean', '');
pg_query_params($con, $query, $data);
$data = array("stitch", "context-file", "file", "username = ftester
    passwordFilename = " . $authDir . "ftester.pass
    pemKeyAndCertFilename = " . $authDir . "getsslcert.txt
    userAuthorityUrn = <userAuthorityUrn>
    testedAggregateManagerUrn = <testedAggregateManager.urn>
    stitchedAuthorityUrns= <stitchedAuthorities.urn>
    
    scsUrl = <scsUrl>");//scsUrn = <scsUrn>
pg_query_params($con, $subQuery, $data);
$data = array("stitch", "userAuthorityUrn", "urn", "urn for authority");
pg_query_params($con, $subQuery, $data);
$data = array("stitch", "testedAggregateManager", "testbed", "");//testbed to run test on");
pg_query_params($con, $subQuery, $data);
$data = array('stitch', 'stitchedAuthorities', 'testbed[]', 'testbeds to run test on');
pg_query_params($con,$subQuery,$data);
$data = array("stitch", "scsUrl", "url", "testbed to run test on");
pg_query_params($con, $subQuery, $data);


$data = array('stitch', 'resultHtml', 'file', 1,'results in html format');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'result-overview', 'file', 2,'results in xml format');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'setUp', 'string',3, 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'getUserCredential', 'string',4, 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'generateRspec', 'string',5, 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'createSlice', 'string',6, 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'initStitching', 'string',7, 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'callSCS', 'string',8, 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'callCreateSlivers', 'string',9, 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'waitForAllReady', 'string',10, 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'loginAndPing', 'string',11, 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'callDeletes', 'string',12, 'status of subtest');
pg_query_params($con, $retQuery, $data);
$data = array('stitch', 'duration', 'long' ,13, 'duration of test');
pg_query_params($con, $retQuery, $data);
$data = array('stitch','returnValue', 'int' ,14, 'return value of the automatedTester');
pg_query_params($con, $retQuery, $data);

echo "\tCreating Login test\n";
//login amv2
$data = array('login2','login2', 'ops_monitoring:login2', 'test login amv2','boolean' , '' ); //--context-file <context-file>');
pg_query_params($con, $query, $data);
$data = array("login2", "context-file", "file", "username = <testbed.username>
    passwordFilename = <testbed.passwordfilename>
    pemKeyAndCertFilename = <testbed.pemkeyandcertfilename>
    userAuthorityUrn = <testbed.userauthorityurn>
    testedAggregateManagerUrn = <testbed.urn>
timoutRetryIntervalMs = 5000
timoutRetryMaxCount = 20
busyRetryIntervalMs = 5000
busyRetryMaxCount = 50");
pg_query_params($con, $subQuery, $data);

$data = array("login2", "testbed", "testbed", "testbed to run test on");
pg_query_params($con, $subQuery, $data);

$data = array('login2', 'resultHtml', 'file',1, 'results in html format');
pg_query_params($con, $retQuery, $data);
$data = array('login2', 'result-overview', 'file',2, 'results in xml format');
pg_query_params($con, $retQuery, $data);
$data = array('login2', 'setUp', 'string',3, 'setup');
pg_query_params($con, $retQuery, $data);
$data = array('login2', 'testGetVersionXmlRpcCorrectness', 'string',4, 'testGetVersionXmlRpcCorrectness');
pg_query_params($con, $retQuery, $data);
$data = array('login2', 'testListResourcesAvailableNoSlice', 'string',5, '');
pg_query_params($con, $retQuery, $data);
$data = array('login2', 'testCreateSliceSliver', 'string',6, '');
pg_query_params($con, $retQuery, $data);
$data = array('login2', 'testCreateSliver', 'string',7, '');
pg_query_params($con, $retQuery, $data);
$data = array('login2', 'testCreatedSliverBecomesReady', 'string',8, '');
pg_query_params($con, $retQuery, $data);
$data = array('login2', 'checkManifestOnceSliverIsReady', 'string',9, '');
pg_query_params($con, $retQuery, $data);
$data = array('login2', 'testNodeLogin', 'string',10, 'test node login');
pg_query_params($con, $retQuery, $data);
$data = array('login2', 'testDeleteSliver', 'string',11, 'test delete sliver');
pg_query_params($con, $retQuery, $data);
$data = array('login2', 'duration', 'long',12, 'duration of the test in millisecs');
pg_query_params($con, $retQuery, $data);
$data = array('login2','returnValue', 'int' ,13, 'return value of the automatedTester');
pg_query_params($con, $retQuery, $data);

//login amv3
$data = array('login3','login3', 'ops_monitoring:login3', 'test login amv3','boolean', '');
pg_query_params($con, $query, $data);
$data = array("login3", "context-file", "file", "username = <testbed.username>
    passwordFilename = <testbed.passwordfilename>
    pemKeyAndCertFilename = <testbed.pemkeyandcertfilename>
    userAuthorityUrn = <testbed.userauthorityurn>
    testedAggregateManagerUrn = <testbed.urn>");
pg_query_params($con, $subQuery, $data);

$data = array("login3", "testbed", "testbed", "testbed to run test on");
pg_query_params($con, $subQuery, $data);

$data = array('login3', 'resultHtml', 'file',1, 'results in html format');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'result-overview', 'file',2, 'results in xml format');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'setUp', 'string',3, 'setup');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'testGetVersionXmlRpcCorrectness', 'string',4, 'testGetVersionXmlRpcCorrectness');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'createTestSlices', 'string',5, 'create slices');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'testAllocate', 'string',6, 'allocate test');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'testProvision', 'string',7, 'testProvision');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'testSliverBecomesProvisioned', 'string',8, 'test sliver becomes provisioned');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'testPerformOperationalAction', 'string',9, 'test perform operational action');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'testSliverBecomesStarted', 'string',10, 'test sliver becomes started');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'testNodeLogin', 'string',11, 'test node login');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'testDeleteSliver', 'string',12, 'test delete sliver');
pg_query_params($con, $retQuery, $data);
$data = array('login3', 'duration', 'long',13, 'duration of the test in millisecs');
pg_query_params($con, $retQuery, $data);
$data = array('login3','returnValue', 'int' ,14, 'return value of the automatedTester');
pg_query_params($con, $retQuery, $data);

//generic as an example
/*
echo "\tCreating Generic Tests\n";
//a login test defined generic; without hardcoded params like testclass and group
$data = array('loginGen','automatedTester', '','','boolean', '--context-file <context-file> '
    . '--test-class be.iminds.ilabt.jfed.lowlevel.api.test.TestAggregateManager2 '
    . '--group nodelogin '
    . '--output-dir <output-dir> '
    . '-q'
    ); 
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
$data = array("loginGen", "output-dir" , 'auto', 'generate an output directory');
pg_query_params($con,$subQuery,$data);

$data = array('loginGen', 'resultHtml', 'file',1, 'results in html format');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'result-overview', 'file',2, 'results in xml format');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'setUp', 'string',3, 'setup');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'testGetVersionXmlRpcCorrectness', 'string',4, 'testGetVersionXmlRpcCorrectness');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'testListResourcesAvailableNoSlice', 'string',5, '');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'testCreateSliceSliver', 'string',6, '');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'testCreateSliver', 'string',7, '');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'testCreatedSliverBecomesReady', 'string',8, '');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'checkManifestOnceSliverIsReady', 'string',9, '');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'testNodeLogin', 'string',10, 'test node login');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'testDeleteSliver', 'string',11, 'test delete sliver');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen', 'duration', 'long',12, 'duration of the test in millisecs');
pg_query_params($con, $retQuery, $data);
$data = array('loginGen','returnValue', 'int' ,13, 'return value of the automatedTester');
pg_query_params($con, $retQuery, $data);*/

echo "creating getVersion 2 & 3 tests\n";
$data = array('getVersion2','getVersion2', 'ops_monitoring:is_available','Is aggregate manager responsive','boolean','');
pg_query_params($con, $query, $data);
$data = array("getVersion2", "context-file", "file", "username = <testbed.username>
    passwordFilename = <testbed.passwordfilename>
    pemKeyAndCertFilename = <testbed.pemkeyandcertfilename>
    userAuthorityUrn = <testbed.userauthorityUrn>
    testedAggregateManagerUrn = <testbed.urn>");
pg_query_params($con, $subQuery, $data);

$data = array("getVersion2", "testbed", "testbed", "testbed to run test on");
pg_query_params($con, $subQuery, $data);

$data = array("getVersion2", 'resultHtml', 'file',1, 'results in html format');
pg_query_params($con, $retQuery, $data);
$data = array("getVersion2", 'result-overview', 'file',2, 'results in xml format');
pg_query_params($con, $retQuery, $data);
$data = array("getVersion2", 'duration', 'long',3, 'duration of the test in millisecs');
pg_query_params($con, $retQuery, $data);
$data = array("getVersion2",'returnValue', 'int' ,4, 'return value of the automatedTester');
pg_query_params($con, $retQuery, $data);
$data = array("getVersion2", 'setUp', 'string',5, 'setup');
pg_query_params($con, $retQuery, $data);
$data = array("getVersion2", 'testGetVersionXmlRpcCorrectness', 'string',6, 'testGetVersionXmlRpcCorrectness');
pg_query_params($con, $retQuery, $data);

$data = array('getVersion3','getVersion3', 'ops_monitoring:is_available', 'Is aggregate manager responsive','boolean', '');
pg_query_params($con, $query, $data);
$data = array("getVersion3", "context-file", "file", "username = <testbed.username>
    passwordFilename = <testbed.passwordfilename>
    pemKeyAndCertFilename = <testbed.pemkeyandcertfilename>
    userAuthorityUrn = <testbed.userauthorityUrn>
    testedAggregateManagerUrn = <testbed.urn>");
pg_query_params($con, $subQuery, $data);

$data = array("getVersion3", "testbed", "urn", "urn for authority");
pg_query_params($con, $subQuery, $data);
//$data = array("getVersion3", "testedAggregateManager", "testbed", "testbed to run test on");
//pg_query_params($con, $subQuery, $data);
$data = array("getVersion3", 'resultHtml', 'file',1, 'results in html format');
pg_query_params($con, $retQuery, $data);
$data = array("getVersion3", 'result-overview', 'file',2, 'results in xml format');
pg_query_params($con, $retQuery, $data);
$data = array("getVersion3", 'duration', 'long',3, 'duration of the test in millisecs');
pg_query_params($con, $retQuery, $data);
$data = array("getVersion3",'returnValue', 'int' ,4, 'return value of the automatedTester');
pg_query_params($con, $retQuery, $data);
$data = array("getVersion3", 'setUp', 'string',5, 'setup');
pg_query_params($con, $retQuery, $data);
$data = array("getVersion3", 'testGetVersionXmlRpcCorrectness', 'string',6, 'testGetVersionXmlRpcCorrectness');
pg_query_params($con, $retQuery, $data);

//listresources
$data = array('listResources','listResources' ,'ops_monitoring:num_vms_allocated','count of free resources','count', '<testbed.urn>');
pg_query_params($con,$query,$data);
$data = array("listResources", "testbed", "testbed", "testbed to get the list recources from");
pg_query_params($con, $subQuery, $data);
$data = array("listResources", "context-file", "file", "username = <testbed.username>
    passwordFilename = <testbed.passwordfilename>
    pemKeyAndCertFilename = <testbed.pemkeyandcertfilename>
    userAuthorityUrn = <testbed.userauthorityurn>");
pg_query_params($con, $subQuery, $data);
//$data = array("listResources", "userAuthorityUrn", "urn", "urn for authority");
//pg_query_params($con, $subQuery, $data);

$data = array("listResources", 'count', 'int',1, 'free resources');
pg_query_params($con, $retQuery, $data);
$data = array("listResources", 'rspec', 'file',2, 'path of rspec file');
pg_query_params($con, $retQuery, $data);

//connectie sluiten
pg_close($con);
echo "done\n";
?>