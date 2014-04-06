<?php
    global $webservice;
    $webservice           = 'http://localhost/service/index.php';
    $queryInternational   = 'last?testtype=ping';
    $queryLocal           = 'last?testtype=ping&testbed=testbed3,testbed7,testbed5,testbed2,testbed11';
    $queryStiching        = 'last?testtype=stitch';
    $queryTestbed         = 'testbed';
    $queryAddTestbed      = 'addTestbed';
    $queryAddTestInstance    = 'addTestInstance';
    $queryTestDefinitions = 'testDefinition';
    
    global $urlInternational;
    global $urlLocal;
    global $urlStiching;
    global $urlTestbed;
    global $urlAddTestbed;
    global $urlAddTestInstance;
    global $urlTestDefinitions;
    $urlInternational   = $webservice.'/'.$queryInternational;
    $urlLocal           = $webservice.'/'.$queryLocal;
    $urlStiching        = $webservice.'/'.$queryStiching;
    $urlTestbed         = $webservice.'/'.$queryTestbed;
    $urlAddTestbed      = $webservice.'/'.$queryAddTestbed;
    $urlAddTestInstance = $webservice.'/'.$queryAddTestInstance;
    $urlTestDefinitions = $webservice.'/'.$queryTestDefinitions;
    
    global $warnPing;
    global $fatalPing;
    $warnPing = 190;
    $fatalPing = -1;//timeout
    
    global $goodStitch;
    global $warnStitch;
    global $fatalStitch;
    global $skipStitch;
    $goodStitch  = 'Good';
    $warnStitch  = 'Warn';
    $skipStitch  = 'SKIP';
    $fatalStitch = 'FATAL';