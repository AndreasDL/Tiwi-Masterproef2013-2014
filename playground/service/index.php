<?php
//debugging
ini_set('display_startup_errors',1);
ini_set('display_errors',1);
error_reporting(-1);

    #autoload classes
    spl_autoload_register('apiAutoload');
    function apiAutoload($classname){
        if (preg_match('/[a-zA-Z]+Controller$/', $classname) && file_exists(__DIR__.'/controllers/'.$classname.'.php')) {
            include __DIR__ . '/controllers/' . $classname . '.php';
            return true;
        }
        else if (preg_match('/[a-zA-Z]+Formatter$/' , $classname) && file_exists(__DIR__.'/formatters/'.$classname.'.php')){
            include __DIR__."/formatters/" . $classname . '.php';
            return true;
        }else{
            return false;
        }
    }
    
    #controller bepalen
    $controller_name = "";
    if (isset ($_SERVER['PATH_INFO'])){
        $controller_name = ucfirst(explode('/',$_SERVER['PATH_INFO'])[1]);
    }
    if ($controller_name != ""){//controller gegeven
        $controller_name .= 'Controller';

        #request method
        $verb = $_SERVER['REQUEST_METHOD'];

        /*//Debug
        print "<b>Controller stuffs: </b><br>";
        print "$verb<br>";
        print "$controller_name<br>";
        */

        #parameters parsen (enkel get)
        //print "<b>Params</b><br>";
        $parameters = array();
        if (isset($_SERVER['QUERY_STRING'])) {
            //parameter gegeven
            parse_str($_SERVER['QUERY_STRING'], $parameters);

            //check if params are valid
            //no from/till met count
            //so also not with last => (last sets count at 1 if not given in databaseAccess)
            $valid = True;
            if (isset($parameters['from']) || isset($parameters['till'])){
                if(isset($parameters['count'])){
                    //fout : count bij from en/of till
                    echo "Error: count and from/till clause not allowed simultaneously!";
                    $valid=false;
                }else if ($controller_name == 'LastController'){
                    //fout : count bij last
                    echo "Error: Last and from/till clause not allowed simultaneously!";
                    $valid=false;
                }
            }
            
            //pas parsen als't geldig is
            if ($valid){
                //parameters gegeven & geldig
                //arrays van params parsen
                foreach ($parameters as $key => $value){
                    $parameters[$key] = explode(',',$value);
                }
                //print_r($parameters);

                //echo $parameters['format'];
                //redirect to controller
                $data = null;
                if (class_exists($controller_name)){
                    //print "<b>redirecting to $controller_name... </b><br>";
                    $controller = new $controller_name();
                    $data = $controller->get($parameters);
                }else{
                    echo "Error: $controller_name is not a valid function!";
                }

                if( isset($data) ){
                    $formatter;
                    if(isset($parameters['format']) && class_exists(ucfirst($parameters['format'][0]).'Formatter')){
                        $formatterName  = ucfirst($parameters['format'][0]);
                        $formatterName .= 'Formatter';
                        $formatter = new $formatterName();
                    }else{
                        //echo "dan maar json";
                        //if format doesn't exists => use json
                        $formatter = new JsonFormatter();
                    } 
                    echo $formatter->format($data);
                }
            }
        }
    }else{
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