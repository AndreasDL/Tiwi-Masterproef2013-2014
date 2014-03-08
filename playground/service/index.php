<?php
    #autoload classes
    spl_autoload_register('apiAutoload');
    function apiAutoload($classname){
        if (preg_match('/[a-zA-Z]+Controller$/', $classname)) {
            include __DIR__ . '/controllers/' . $classname . '.php';
            return true;
        } elseif (preg_match('/[a-zA-Z]+Model$/', $classname)) {
            include __DIR__ . '/models/' . $classname . '.php';
            return true;
        } elseif (preg_match('/[a-zA-Z]+View$/', $classname)) {
            include __DIR__ . '/views/' . $classname . '.php';
            return true;
        }
    }
    
    #controller bepalen
    $controller_name = ucfirst(explode('/', $_SERVER['PATH_INFO'])[1].'Controller');
    $verb = $_SERVER['REQUEST_METHOD'];
    
    print "Controller stuffs: <br>";
    print "$verb<br>";
    print "$controller_name<br>";
    
    #parameters parsen
    print "<br>Params<br>";
    $parameters = array();
    if (isset($_SERVER['QUERY_STRING'])) {
        parse_str($_SERVER['QUERY_STRING'], $parameters);
    }

    #redirecten naar controller
    print "<br><br>";
    if (class_exists($controller_name)){
        print "redirecting... ";
        $controller = new $controller_name();
        $result = $controller->get($parameters);
        print_r($result);
    }
?>