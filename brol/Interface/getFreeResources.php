<?php
	include("response.php");
	$functieNaam = "getFreeResources";

	if (!empty($_GET['testbedId'])){
		$tid = $_GET['testbedId'];
		$data = getDuration($tid);

		if (empty($ping)){
			response($functieNaam,"Id not found",null);
		}else{
			response($functieNaam,"good",$data);
		}
	}else{
		response($functieNaam,"Id argument invalid",null);
	}

	function getFreeResources($testbedId){
		return "900";
	}
?>