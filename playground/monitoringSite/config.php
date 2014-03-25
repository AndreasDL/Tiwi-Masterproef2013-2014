<?php
    global $webservice;
    $webservice         = 'http://localhost/service/index.php';
    $queryInternational = 'last?testtype=ping';
    $queryLocal         = 'last?testtype=ping&testbed=urn-testbed3,urn-testbed7,urn-testbed5,urn-testbed2,urn-testbed11';
    $queryStiching      = 'last?testtype=stitch';
    $queryTestbed       = 'testbed';
    
    global $urlInternational;
    global $urlLocal;
    global $urlStiching;
    global $urlTestbed;
    $urlInternational = $webservice.'/'.$queryInternational;
    $urlLocal         = $webservice.'/'.$queryLocal;
    $urlStiching      = $webservice.'/'.$queryStiching;
    $urlTestbed       = $webservice.'/'.$queryTestbed;
    
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
