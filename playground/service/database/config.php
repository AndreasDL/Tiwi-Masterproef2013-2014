<?php
    //config for database
    $db_user = "postgres";
    $db_pass = "post";
    $db_name = "testdb";
    $db_host = "localhost";
    $db_port = "5432";
    global $conString;
    $conString = "host=".$db_host." port=".$db_port." dbname=" . $db_name . " user=" . $db_user . " password=" . $db_pass;
    
    global $hmacAlgo;
    $hmacAlgo = 'sha512';
    