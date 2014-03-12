<?php
	$aantalTestbeds = 17;
	$aantalpinginstances = $aantalTestbeds;
	$aantalstitchinstances = 5;
	$resultsPerInstances = 10;

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
	//ping
	$query = "insert into testinstances (testname,frequency,parameters) values ($1,$2,$3);";
	for ($i = 0 ; $i < $aantalpinginstances; $i++){ 
		$data = array("ping",300,json_encode(array(
			array('name' => 'timeout' , 'value' => '240'),
			array('name' => 'testbed' , 'value' => "testbed$i")
		)));
		pg_query_params($con,$query,$data);
	}
	//stitching
	$query = "insert into testinstances (testname,frequency,parameters) values ($1,$2,$3);";
	for ($i = 0 ; $i < $aantalstitchinstances ; $i++){
		$data = array("stitchingtest$i",3600,
			json_encode(array(
				array('name' => 'topology' , 'value' => 'ring'),
				array('name' => 'testbeds' , 'value' => json_encode(
					array("testbed".$i%$aantalTestbeds , "testbed".($i+1)%$aantalTestbeds , "testbed".($i+2)%$aantalTestbeds)
				))
			)));
		pg_query_params($con,$query,$data);
	}

	//results
	$query = "insert into results (testinstanceid,results,log) values ($1,$2,$3);";
	for ($j = 1 ; $j < $resultsPerInstances ; $j++){
		echo  "round $j <br>";
		$instanceid = 1;
		//pings
		for ($i = 0 ; $i < $aantalpinginstances; $i++){
			$data = array(
				"$instanceid",
				json_encode(array(
					array('value' => rand(0,240))
				)),
				"http://f4f-mon-dev.intec.ugent.be/logs/$instanceid/rand(0-10000)"
			);
			pg_query_params($con,$query,$data);
			$instanceid++;
		}
		//testbeds
		for ($i = 0 ; $i < $aantalstitchinstances ; $i++){
			$data = array(
				"$instanceid",
				json_encode(
					array(
						array('name' => 'setup' , 'value' => 'Success'),
						array('name' => 'getUserCredential', 'value' => 'Success'),
						array('name' => 'generateRspec' , 'value' => 'Success'),
						array('name' => 'createSlice' , 'value' => 'Success'),
						array('name' => 'initStitching' , 'value' => 'Success'),
						array('name' => 'callSCS' , 'value' => 'Succes'),
						array('name' => 'callCreateSlivers' , 'value' => 'Success'),
						array('name' => 'waitForAllReady' , 'value' => 'Success'),
						array('name' => 'loginAndPing' , 'value' => 'Warn'),
						array('name' => 'callDeletes' , 'value' => 'Warn')
				)),
				"http://f4f-mon-dev.intec.ugent.be/logs/$instanceid/rand(0-10000)"
			);
			$instanceid++;
			pg_query_params($con,$query,$data);
		}
		
		sleep(3); //timestamps verschillend maken
	}



	//connectie sluiten
	pg_close($con);
	echo "done";