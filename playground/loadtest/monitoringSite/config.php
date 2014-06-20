<?php
    global $webService;
    $webService           = 'http://localhost/API/loadtest/index.php';
    
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
