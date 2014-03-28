<?php
//debugging
ini_set('display_startup_errors', 1);
ini_set('display_errors', 1);
error_reporting(-1);

#autoload classes
spl_autoload_register('apiAutoload');

function apiAutoload($classname) {
    if (preg_match('/[a-zA-Z]+Controller$/', $classname) && file_exists(__DIR__ . '/controllers/' . $classname . '.php')) {
        include __DIR__ . '/controllers/' . $classname . '.php';
        return true;
    } else if (preg_match('/[a-zA-Z]+Formatter$/', $classname) && file_exists(__DIR__ . '/formatters/' . $classname . '.php')) {
        include __DIR__ . "/formatters/" . $classname . '.php';
        return true;
    } else {
        return false;
    }
}

#controller bepalen
$data = null;
$status = '200';
$msg = 'Good!';

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

    ////Parse params if request is valid & parameters are set
    //only GET ATM
    $parameters = array();
    if ($valid && isset($_SERVER['QUERY_STRING'])) {
        //parse all params
        parse_str($_SERVER['QUERY_STRING'], $parameters);
        foreach ($parameters as $key => $value) {
            $parameters[$key] = explode(',', $value);
        }

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
                //fout : count bij from en/of till
                $status = '400';
                $msg = "Error: count and from/till clause not allowed simultaneously!";
                $valid = false;
            } else if ($controller_name == 'LastController') {
                //fout : count bij last
                $status = '400';
                $msg = "Error: Last and from/till clause not allowed simultaneously!";
                $valid = false;
            }
        }

        //Only call database is request is valid
        if ($valid) {
            //print "<b>redirecting to $controller_name... </b><br>";
            $data = $controller->get($parameters);
        }
    }
    echo $formatter->format($data, $status, $msg);
} else {
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
                Dit is de beta versie van de jFed monitoring webservice.<br>
                Hieronder vindt je een aantal voorbeeld calls.<br>
                De return waarde is momenteel altijd json & dummy waarden.<br>
                Er zijn momenteel 2 testen gedefinieerd, ping & stitching.<br>
                De testbeds zijn momenteel identificeerbaar met de urn , voorgesteld door bv. urn-testbed1 voor testbed1.<br>
                <br><br>
                <table border="1">
                    <tr><th>call</th><th>uitleg</th></tr>
                    <tr>
                        <td><a href="./index.php/last?format=PrettyJson">/last</a></td>
                        <td>de alle laatste resultaten van elk testbed</td>
                    </tr>
                    <tr>
                        <td><a href="./index.php/last?testtype=stitch&format=PrettyJson">/last?testtype=stitch</a></td>
                        <td>de alle laatste stitching resultaten van elk testbed</td>
                    </tr>
                    <tr>
                        <td><a href="./index.php/last?testbed=urn-testbed1&testtype=ALL&format=PrettyJson">/last?testbed=urn-testbed1&testtype=ALL</a></td>
                        <td>Laatste resultaten van alle tests op testbed1</td>
                    </tr>
                    <tr>
                        <td><a href="./index.php/list?testtype=stitch&testbed=urn-testbed1&count=3&format=PrettyJson">/list?testtype=stitch&testbed=urn-testbed1&count=3</a></td>
                        <td>De laatste 3 stitching resultaten van elk testbed1</td>
                    </tr>
                    <tr>
                        <td><a href="./index.php/list?format=PrettyJson&from=2014-03-18T19:29:06&till=2014-03-18T19:29:10">/index.php/list?from=2014-03-18T19:29:06&till=2014-03-18T19:29:10</a></td>
                        <td>De Alle testen tussen 18/03/2014 19:29:06 tot 18/03/2014 19:29:10</td>
                    </tr>
                    <tr>
                        <td><a href="./index.php/testDefinition?testtype=stitch&format=PrettyJson">/testdefinition?testtype=stitch</a></td>
                        <td>Beschrijving van de stitchtest</td>
                    </tr>            
                    <tr>
                        <td><a href="./index.php/testInstance?testtype=stitch&format=PrettyJson">/testInstance?testtype=stitch</a></td>
                        <td>Geeft alle geplande tests weer van het type stitch</td>
                    </tr>
                    <tr>
                        <td><a href="./index.php/testbed?format=PrettyJson">/testbed</a></td>
                        <td>Geeft alle testbeds weer.</td>
                    </tr>

                </table>
        </body>
    </html>
<?php } ?>