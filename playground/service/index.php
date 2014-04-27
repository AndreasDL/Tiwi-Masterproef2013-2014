<?php
//debugging
ini_set('display_startup_errors', 1);
ini_set('display_errors', 1);
error_reporting(-1);

include(__DIR__.'/Request.php');
#autoload classes
spl_autoload_register('apiAutoload');
function apiAutoload($classname) {
    if (preg_match('/[a-zA-Z]+Controller$/', $classname) && file_exists(__DIR__ . '/controllers/' . $classname . '.php')) {
        include __DIR__ . '/controllers/' . $classname . '.php';
        return true;
    } else if (preg_match('/[a-zA-Z]+Formatter$/', $classname) && file_exists(__DIR__ . '/database/formatters/' . $classname . '.php')) {
        include __DIR__ . "/database/formatters/" . $classname . '.php';
        return true;
    } else if (preg_match('/[a-zA-Z]+Fetcher/', $classname) && file_exists (__DIR__ . '/database/fetchers/' . $classname . '.php' )) {
        include __DIR__ . '/database/fetchers/' . $classname . '.php';
        return true;
    } else {
        return false;
    }
}


#controller bepalen
$data = null;
$status = '200';
$msg ='';//= 'Good!';
$parameters=array();
$fetcher = new defaultFetcher();

$formatter = new JsonFormatter(); //Default formatter
//getcontroller => no controller => show info page
$controller_name = "";
if (isset($_SERVER['PATH_INFO'])) {
    $controller_name = ucfirst(explode('/', $_SERVER['PATH_INFO'])[1]);
}
if ($controller_name != "") {
    //stop executing code when request in invalid.
    $valid = True;

    //get Controller if attempt for a controller is given
    $controller_name .= 'Controller';
    $controller = null;
    if (class_exists($controller_name)) {
        //print "<b>redirecting to $controller_name... </b><br>";
        $controller = new $controller_name();
    } else {
        $status = '404';
        $msg .= "Error: $controller_name is not a valid function!";
        $valid = False;
    }

    //define request method
    $verb = $_SERVER['REQUEST_METHOD'];
    $req = new Request($parameters,$status,$msg,$verb);
    
    //Parse params if request is valid & parameters are set
    //$parameters = array(); already declared
    if ($valid){
        //parse all params
        //GET
        if (isset($_SERVER['QUERY_STRING'])) {
            parse_str($_SERVER['QUERY_STRING'], $parameters);
        }
        //POST
        $body = file_get_contents('php://input');
        $content_type = false;
        if(isset($_SERVER['CONTENT_TYPE'])) {
            $content_type = $_SERVER['CONTENT_TYPE'];
        }
        switch($content_type) {
            case "application/json":
                $body_params = json_decode($body);
                if($body_params) {
                    foreach($body_params as $param_name => $param_value) {
                        $parameters[$param_name] = $param_value;
                    }
                }
                break;
            case "application/x-www-form-urlencoded":
                parse_str($body, $postvars);
                foreach($postvars as $field => $value) {
                    $parameters[$field] = $value;
                }
                break;
            default:
                foreach ($_POST as $key => $value){
                    $parameters[$key] = $value;
                }
                break;
        }
        
        //array of params (testbed=urn-testbed1,urn-testbed5) => 2 params!
        // Every parameter after this will be in an array even if there is only 1 !!
        foreach ($parameters as $key => $value) {
            $parameters[$key] = explode(',', $value);
        }
        
        //print_r($parameters);

        //GetFormat from params
        if (isset($parameters['format'])) {
            if (class_exists(ucfirst($parameters['format'][0]) . 'Formatter')) {
                $formatterName = ucfirst($parameters['format'][0]);
                $formatterName .= 'Formatter';
                $formatter = new $formatterName();
            } else {
                //don't set status=> 200 by default, this will work since it will also trigger when no format is given
                $msg = "Warn : Format not found, using Json instead";
            }
        }

        //check if params are valid
        //no from/till & count at same time
        //so also not with last => (last sets count at 1 if not given in databaseAccess)
        if (isset($parameters['from']) || isset($parameters['till'])) {
            if (isset($parameters['count'])) {
                //fout : count & from and/of till
                $status = '400';
                $msg = "Error: count and from/till clause not allowed simultaneously!";
                $valid = false;
            } else if ($controller_name == 'LastController') {
                //fout : count with last (because last uses list with count = 1)
                $status = '400';
                $msg = "Error: Last and from/till clause not allowed simultaneously!";
                $valid = false;
            }
        }
        
        $req = new Request($fetcher,$parameters,$status,$msg,$verb);
        //Only call database is request is valid
        if ($valid) {
            //print "<b>redirecting to $controller_name... </b><br>";
           
            $req->setData($controller->get($req));
        }
    }
    echo $formatter->format($req);
}else {
    ?>
    <!DOCTYPE html>
    <html lang="en">
        <head>
            <meta charset="utf-8">
            <title>International federation Monitor</title>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <meta name="description" content="">
            <meta name="author" content="">
            <!-- Le styles -->
            <link href="css/bootstrap.css" rel="stylesheet">
            <link href='http://fonts.googleapis.com/css?family=Sintony' rel='stylesheet' type='text/css'>
            <link href="css/style.css" rel="stylesheet">
            <link rel="shortcut icon" href="ico/favicon.ico">
        </head>
        <body>
            <div id="header"></div>
            <div class="container" id="content"><h1>jFed Monitoring Webservice - beta</h1>
                Welcome!<br>
                This is the beta version of the jFed monitoring Service.<br>
                On this page there a few example calls. These calls use dummy data.<br>
                There are only 2 tests defined at the moment, a ping tests and a stitching test.<br>
                <br><br>
                <table border="1">
                    <tr><th>call</th><th>explanation</th></tr>
                    <tr>
                        <td><a href="./index.php/last?format=PrettyJson">/last</a></td>
                        <td>Last results for each testbed</td>
                    </tr>
                    <tr>
                        <td><a href="./index.php/last?testdefinitionname=stitch&format=PrettyJson">/last?testdefinitionname=stitch</a></td>
                        <td>Same as above, but only stitching tests</td>
                    </tr>
                    <tr>
                        <td><a href="./index.php/last?testbed=testbed1,testbed5&testdefinitionname=ALL&format=PrettyJson">/last?testbed=testbed1,testbed5&testdefinitionname=ALL</a></td>
                        <td>All Last results for testbed1 and testbed5</td>
                    </tr>
                    <!--<tr>
                        <td><a href="./index.php/list?testdefinitionname=stitch&testbed=testbed1&count=3&format=PrettyJson">/list?testdefinitionname=stitch&testbed=testbed1&count=3</a></td>
                        <td>Last 3 stitching results for testbed1</td>
                    </tr>-->
                    <tr>
                        <td><a href="./index.php/list?from=2014-03-18T19:29:06&till=2014-03-25T20:00:00&format=PrettyJson">/list?from=2014-03-18T19:29:06&till=2014-03-25T20:00:00</a></td>
                        <td>All tests between 18/03/2014 19:29:06 and 25/03/2014 20:00:00</td>
                    </tr>
                    <tr>
                        <td><a href="./index.php/testDefinition?testdefinitionname=stitch&format=PrettyJson">/testdefinition?testdefinitionname=stitch</a></td>
                        <td>Definition of a stitching test</td>
                    </tr>
                    <tr>
                        <td><a href="./index.php/testDefinition?testtype=stitch&format=PrettyJson">/testdefinition?testtype=stitch</a></td>
                        <td>all defined tests of type stitching. This makes it possible to e.g. create 2 stitching tests with different parameters</td>
                    </tr>
                    <tr>
                        <td><a href="./index.php/testInstance?testdefinitionname=stitch&format=PrettyJson">/testInstance?testdefinitionname=stitch</a></td>
                        <td>All testinstance for testdefinitionname=stitching test</td>
                    </tr>
                    <tr>
                        <td><a href="./index.php/testInstance?testname=wall2&format=PrettyJson">/testInstance?testname=wall2</a></td>
                        <td>view testinstance with name2 in detail</td>
                    </tr>
                    <tr>
                        <td><a href="./index.php/testbed?format=PrettyJson">/testbed</a></td>
                        <td>View all testbeds</td>
                    </tr>
                </table>
        </body>
    </html>
<?php } ?>