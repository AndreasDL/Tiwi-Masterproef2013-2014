<?php
	include("functions.php");
	$functieNaam = "getPing";

	if (!empty($_GET['testbedId'])){
		$tid = $_GET['testbedId'];
		$data = getPing($tid);

		if (empty($data)){
			response($functieNaam,"Id not valid",null);
		}else{
			response($functieNaam,"good",$data);
		}
	}else{
		response($functieNaam,"expecting a testbedid",null);
	}

	function getPing($testbedId){
		$user = "test";
		$pass = "hoi";
		$url = "localhost";
		$db = "testdb";

		$con = mysqli_connect($url,$user,$pass,$db);
		if(mysqli_connect_errno()){
			error($functieNaam,"Connection to database failed");
		}else{
			$query = "select value from results where testbedid=$testbedId and testid=1 order by timestamp DESC limit 1";
			$result = mysqli_query($con,$query);
			mysqli_close($con);
			
			if (!$result){
				error($functieNaam,"id not valid");
				return null;
			}else{
				$row = mysqli_fetch_array($result);//answer = 1 row
				return $row['value'];
			}
		}
	}
?>