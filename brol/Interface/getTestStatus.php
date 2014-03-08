<?php
	include("response.php");
	$functieNaam = "getDuration";

	if (!empty($_GET['testbedId'])){
		$tid = $_GET['testbedId'];
		$data = getDuration($tid);

		if (empty($data)){
			response($functieNaam,"testbedId or testId not found",null);
		}else{
			response($functieNaam,"good",$data);
		}
	}else{
		response($functieNaam,"Id argument invalid",null);
	}

	function getTestStatus($testbedId){
		return "900";
	}
?>