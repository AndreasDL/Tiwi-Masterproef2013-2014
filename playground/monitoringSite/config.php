<?php
    global $webService;
    $webService           = 'http://localhost/service/index.php';
    /*$queryInternational   = 'last?testdefinitionname=ping,getVersion2';
    $queryLocal           = 'last?testdefinitionname=ping,getVersion2&testbed=wall1,wall2';*/
    
    /*
    global $urlInternational;
    global $urlLocal;
    global $urlTestbed;
    global $urlAddTestbed;
    global $urlAddTestInstance;
    global $urlTestDefinitions;
    global $urlLast;
    $urlLast            = $webservice.'/last';
    $urlInternational   = $webservice.'/'.$queryInternational;
    $urlLocal           = $webservice.'/'.$queryLocal;
    $urlTestbed         = $webservice.'/testbed';
    $urlAddTestbed      = $webservice.'/addTestbed';
    $urlAddTestInstance = $webservice.'/addTestInstance';
    $urlTestDefinitions = $webservice.'/testDefinition';
    */
    
    global $warnPing;
    global $fatalPing;
    $warnPing = 190;
    $fatalPing = -1;//timeout
    
    global $listFatal;
    global $listWarn;
    $listFatal = 0;
    $listWarn = 20;
    
    //needs cleanup
    global $good;
    global $SUCCESS;
    global $warn;
    global $fatal;
    global $skip;
    global $skipped;
    global $failed;
    global $WARN;
    global $FAILED;
    $good  = 'Good';
    $warn  = 'Warn';
    $skip  = 'SKIP';
    $skipped = 'SKIPPED';
    $fatal = 'FATAL';
    $failed = 'FAILED';
    $SUCCESS = 'SUCCESS';
    $WARN = 'WARN';
    $FAILED = 'FAILED';