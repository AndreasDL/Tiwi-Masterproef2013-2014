<?php

//include (__DIR__ . "/../config.php"); //database config
$aantalTestbeds = 17;
$aantalpinginstances = $aantalTestbeds;
$aantalstitchinstances = 10;
$aantallogininstances = 1;

$login = 'postgres';
$pass = "post";
$dbname = "testdb";
$conString = "dbname=" . $dbname . " user=" . $login . " password=" . $pass;

$puKey = "iminds";
$prKey = "virtualWall";

$homeDir = "/home/drew/";
$authDir = $homeDir . ".auth/";
$authFile = $authDir . "authorities.xml";

//connectie maken
$con = pg_Connect($conString) or die('connection failed');
echo "connection established\n";

echo "Clearing subResults\n";
$query = "with lijst as (select resultid,testinstanceid from results r
                join subresults sr using (resultid)
                join testinstances ti using (testinstanceid)
            where ti.testdefinitionname='login'
        )
        delete from subResults ssr where ssr.resultid = any(select resultid from lijst)";
pg_query($con,$query);
echo "Clearing Results\n";
$query2 = "with lijst as (select resultid,testinstanceid from results r
                join subresults sr using (resultid)
                join testinstances ti using (testinstanceid)
            where ti.testdefinitionname='login'
        )
        delete from results r where r.resultid = any(select resultid from lijst)";
pg_query($con,$query2);

//connectie sluiten
pg_close($con);
echo "done\n";
?>