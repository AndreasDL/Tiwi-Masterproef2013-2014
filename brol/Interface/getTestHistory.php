<?php
	include("response.php");
	$functieNaam = "getTestHistory";

	if (!empty($_GET['testbedId']) && !empty($_GET['testId'])){
		$tid = $_GET['testbedId'];
		$testid = $_GET['testId'];
		$data = getDuration($tid,$testid);

		if (empty($data)){
			response($functieNaam,"Id not found",null);
		}else{
			response($functieNaam,"good",$data);
		}
	}else{
		response($functieNaam,"arguments invalid",null);
	}

	function getTestHistory($testbedId,$testId){
		return "900";
	}
?>