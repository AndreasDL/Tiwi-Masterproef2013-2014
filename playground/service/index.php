<?php
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
    
    /* Debug
    print "Controller stuffs: <br>";
    print "$verb<br>";
    print "$controller_name<br>";
    */
    
    #parameters parsen enkel nog get params
    //print "Params<br>";
    $parameters = array();
    if (isset($_SERVER['QUERY_STRING'])) {
        parse_str($_SERVER['QUERY_STRING'], $parameters);
        foreach ($parameters as $key => $value){
            $parameters[$key] = explode(',',$value);
        }
    }
    #lijst met kommas omzetten naar arrays
    //print_r($parameters);
    
    //redirect to controller
    if (class_exists($controller_name)){
        //print "redirecting... ";
        $controller = new $controller_name();
        echo $controller->get($parameters);
    }