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
        echo "connection established\n";

	//in pompen
	//testbeds
        echo "Creating Testbeds\n";
	$query = "insert into testbeds (testbedid,name) values($1,$2);";
	for ($i = 0 ; $i < $aantalTestbeds ; $i++){
		$data = array("urn-testbed$i","testbed$i");
		pg_query_params($con,$query,$data);
	}

	//testdefinitions
        echo "Creating TestDefinitions\n";
	$query = "insert into testdefinitions (testtype,testcommand,parameters,return) values($1,$2,$3,$4);";
        echo "\tCreating Ping test\n";
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
        echo "\tCreating Stitching test\n";
	$data = array('stitch',
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
        echo "Creating TestInstances\n";
	//ping
        echo "\tCreating Ping testInstances\n";
	$query = "insert into testinstances (testname,testtype,frequency,parameters) values ($1,$2,$3,$4);";
	for ($i = 0 ; $i < $aantalpinginstances; $i++){ 
		$data = array("ping",
                        "ping",
                        300,
                        '{240,urn-testbed'.$i.'}'
		);
		pg_query_params($con,$query,$data);
	}
	//stitching
        echo "\tCreating stitching testinstances\n";
	//$query = "insert into testinstances (testname,testType,frequency,parameters) values ($1,$2,$3);";
	for ($i = 0 ; $i < $aantalstitchinstances ; $i++){
		$data = array("stitchingtest$i","stitch",3600,
			'{ring,urn-testbed'.$i%$aantalTestbeds.',urn-testbed'.($i+1)%$aantalTestbeds.',urn-testbed'.($i+2)%$aantalTestbeds.'}'
			);
		pg_query_params($con,$query,$data);
	}

	//results
        echo "creating results\n";
        echo "!!Warning this may take some time because the script sleeps for 10 seconds after every round to get different timestamps\n";
	$query = "insert into results (testinstanceid,results,log) values ($1,$2,$3);";
	for ($j = 1 ; $j < $resultsPerInstances ; $j++){
		echo  "\tround $j \n";
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
	echo "done\n";
?>