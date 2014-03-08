<?php
		include("functions.php");
	$functieNaam = "getPing";
	$user = "test";
	$pass = "hoi";
	$url = "localhost";
	$db = "testdb";

	if (!empty($_GET['testbedId'])){
		$data = getTestbed($_GET['testbedId']);

		if (empty($data)){
			response($functieNaam,"Id or from/till not valid",null);
		}else{
			response($functieNaam,"good",$data);
		}
	}else{
		response($functieNaam,"Expecting a testbedid",null);
	}

	function getTestbed($testbedId){
		global $user,$pass,$url,$db;	
		$con = mysqli_connect($url,$user,$pass,$db);

		if(mysqli_connect_errno()){
			error($functieNaam,"Connection to database failed");
		}else{
			stmt = $con->prepare('select * from testbedts where testbedid=? and testid=1 order by timestamp DESC limit 1');
			$stmt->bind_param('s', $testbedId);
			$stmt->execute();
			$result = $stmt->get_result();
			mysqli_close($con);
			
			if (!$result){
				return null;
			}else{
				$row = mysqli_fetch_array($result);//answer = 1 row
				return $row['value'];
			}
		}
	}

?>