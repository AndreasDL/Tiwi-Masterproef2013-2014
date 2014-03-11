<?php
	$aantalTestbeds = 17;
	$aantalpinginstances = 11;
	$aantalstitchinstances = 3;
	
	

	$login = 'postgres';
	$pass = "post";
	$dbname = "testdb";
	$conString = "dbname=" . $dbname . " user=" . $login . " password=" . $pass;

	//connectie maken
	$con = pg_Connect($conString) or die('connection failed');

	//in pompen
	//testbeds
	$query = "insert into testbeds (testbedid,name) values($1,$2);";
	for ($i = 0 ; $i < $aantalTestbeds ; $i++){
		$data = array("urn-testbed$i","testbed$i");
		pg_query_params($con,$query,$data);
	}

	//testdefinitions
	$query = "insert into testdefinitions (testname,testcommand,parameters,return) values($1,$2,$3,$4);";
	$data = array('ping',
		'ping',
		json_encode(
			array(
				array('name' => 'timeout' , 'type' => 'integer' , 'description' => 'timeout for ping'),
				array('name' => 'testbed' , 'type' => 'testbed' , 'description' => 'testbed for ping')
			)
		),json_encode(
			array(
				array('name' => 'pingValue' , 'type' => 'integer')
			)
		)
	);
	pg_query_params($con,$query,$data);
	$data = array('Stitching',
		'stitch',
		json_encode(
			array(
				array('name' => 'testbeds' , 'type' => 'testbed[]' , 'description' => 'testbeds for stitching'),
				array('name' => 'topology' , 'type' => 'String'    , 'description' => 'used topology for instance ring or line')
			)
		),json_encode(
			array(
				array('name' => 'setup' , 'type' => 'string'),
				array('name' => 'getUserCredential', 'type' => 'string'),
				array('name' => 'generateRspec' , 'type' => 'string'),
				array('name' => 'createSlice' , 'type' => 'string'),
				array('name' => 'initStitching' , 'type' => 'string'),
				array('name' => 'callSCS' , 'type' => 'string'),
				array('name' => 'callCreateSlivers' , 'type' => 'string'),
				array('name' => 'waitForAllReady' , 'type' => 'string'),
				array('name' => 'loginAndPing' , 'type' => 'string'),
				array('name' => 'callDeletes' , 'type' => 'string')
			)
		)
	);
	pg_query_params($con,$query,$data);

	//testinstances
	$query = "insert into testinstances (testname,frequency,parameters) values ($1,$2,$3)";
	for ()

	//connectie sluiten
	pg_close($con);