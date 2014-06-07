<?php
    global $webservice;
    $webservice           = 'http://localhost/service/index.php';
    $queryInternational   = 'last?testtype=ping';
    $queryLocal           = 'last?testtype=ping&testbed=testbed3,testbed7,testbed5,testbed2,testbed11';
    $queryStiching        = 'last?testtype=stitch';
    $queryLogin           = 'last?testtype=login';
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
    global $urlLogin;
    $urlInternational   = $webservice.'/'.$queryInternational;
    $urlLocal           = $webservice.'/'.$queryLocal;
    $urlStiching        = $webservice.'/'.$queryStiching;
    $urlTestbed         = $webservice.'/'.$queryTestbed;
    $urlAddTestbed      = $webservice.'/'.$queryAddTestbed;
    $urlAddTestInstance = $webservice.'/'.$queryAddTestInstance;
    $urlTestDefinitions = $webservice.'/'.$queryTestDefinitions;
    $urlLogin           = $webservice.'/'.$queryLogin;
    
    global $warnPing;
    global $fatalPing;
    $warnPing = 190;
    $fatalPing = -1;//timeout
    
    global $good;
    global $SUCCESS;
    global $fail;
    global $warn;
    global $fatal;
    global $skip;
    global $skipped;
    global $failed;
    global $WARN;
    global $FAILED;
    global $SKIP;
    global $SKIPPED;
    $good  = 'Good';
    $warn  = 'Warn';
    $skip  = 'SKIP';
    $skipped = 'SKIPPED';
    $fatal = 'FATAL';
    $failed = 'FAILED';
    $SUCCESS = 'SUCCESS';
    $WARN = 'WARN';
    $FAILED = 'FAILED';

    