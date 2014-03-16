<?php
//debugging
ini_set('display_startup_errors',1);
ini_set('display_errors',1);
error_reporting(-1);

    #autoload classes
    spl_autoload_register('apiAutoload');
    function apiAutoload($classname){
        if (preg_match('/[a-zA-Z]+Controller$/', $classname)) {
            include __DIR__ . '/controllers/' . $classname . '.php';
            return true;
        }
    }
    
    #controller bepalen
    $controller_name = "";
    if (isset ($_SERVER['PATH_INFO'])){
        $controller_name = ucfirst(explode('/', $_SERVER['PATH_INFO'])[1]);
    }
    $controller_name .= 'Controller';
    
    #request method
    $verb = $_SERVER['REQUEST_METHOD'];
    
    /*//Debug
    print "<b>Controller stuffs: </b><br>";
    print "$verb<br>";
    print "$controller_name<br>";
    */
    
    #parameters parsen enkel nog get params
    //print "<b>Params</b><br>";
    $parameters = array();
    if (isset($_SERVER['QUERY_STRING'])) {
        parse_str($_SERVER['QUERY_STRING'], $parameters);
        foreach ($parameters as $key => $value){
            $parameters[$key] = explode(',',$value);
        }
    }
    #lijst met kommas omzetten naar arrays
    //print_r($parameters);
    //print "<br><br>";
    
    //redirect to controller
    if (class_exists($controller_name)){
        //print "<b>redirecting to $controller_name... </b><br>";
        $controller = new $controller_name();
        
        //pretty print
        //echo "<pre>";
        echo json_encode(json_decode($controller->get($parameters)),JSON_PRETTY_PRINT);
        //echo "</pre>";
    }else{
        ?>
    <!DOCTYPE html>
    <html lang="en">
      <head>
        <meta charset="utf-8">
       <title>International federation Monitor</title>
            <!--<meta http-equiv="refresh" content="5" >-->
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
                <td><a href="./index.php/last">/last</a></td>
                <td>de alle laatste resultaten van elk testbed</td>
            </tr>
            <tr>
                <td><a href="./index.php/last?testtype=stitch">/last?testtype=stitch</a></td>
                <td>de alle laatste stitching resultaten van elk testbed</td>
            </tr>
            <tr>
                <td><a href="./index.php/last?testbed=urn-testbed1&testtype=ALL">/last?testbed=urn-testbed1&testtype=ALL</a></td>
                <td>Laatste resultaten van alle tests op testbed1</td>
            </tr>
            <tr>
                <td><a href="./index.php/list?testtype=stitch&testbed=urn-testbed1&count=3">/list?testtype=stitch&testbed=urn-testbed1&count=3</a></td>
                <td>De laatste 3 stitching resultaten van elk testbed1</td>
            </tr>
            <tr>
                <td><a href="./index.php/testDefinition?testtype=stitch">/testdefinition?testtype=stitch</a></td>
                <td>Beschrijving van de stitchtest</td>
            </tr>            
            <tr>
                <td><a href="./index.php/testInstance?testtype=stitch">/testInstance?testtype=stitch</a></td>
                <td>Geeft alle geplande tests weer van het type stitch</td>
            </tr>
        </table>
    </body>
</html>
    <?php } ?>