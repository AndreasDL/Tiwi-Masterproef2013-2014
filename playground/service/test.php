<?php

//debugging
ini_set('display_startup_errors', 1);
ini_set('display_errors', 1);
error_reporting(-1);

//$verb = $_SERVER['REQUEST_METHOD'];
//$url_elements = explode('/', $_SERVER['PATH_INFO']);
$gParameters = array();
$pParameters = array();

// first of all, pull the GET vars
if (isset($_SERVER['QUERY_STRING'])) {
    parse_str($_SERVER['QUERY_STRING'], $gParameters);
}
//print "<h3>Get-Params:</h3>";
//print_r($gParameters);


//print "<h3>Post-Params:</h3><br><br";
// now how about PUT/POST bodies? These override what we got from GET
$body = file_get_contents("php://input");
//print_r($body);
$content_type = false;
if (isset($_SERVER['CONTENT_TYPE'])) {
    $content_type = $_SERVER['CONTENT_TYPE'];
}
switch ($content_type) {
    case "application/json":
        $body_params = json_decode($body);
        if ($body_params) {
            foreach ($body_params as $param_name => $param_value) {
                $pParameters[$param_name] = $param_value;
            }
        }
}
//print_r($pParameters);
print json_encode(array('hoi'=>'hi','ik ben' => 'andreas','postdata' => $pParameters));

