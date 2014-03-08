<?php
	include("response.php");
	$functieNaam = "getLastCheck";

	if (!empty($_GET['testbedId'])){
		$tid = $_GET['testbedId'];
		$data = getLastCheck($tid);

		if (empty($ping)){
			response($functieNaam,"Id not found",null);
		}else{
			response($functieNaam,"good",$data);
		}
	}else{
		response($functieNaam,"Id argument invalid",null);
	}

	function getLastCheck($testbedId){
		return "900";
	}
?