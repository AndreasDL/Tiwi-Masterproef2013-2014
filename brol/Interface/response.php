<?php
	header("content-Type:application/json");

	function response($requestName,$statusMessage,$data){
		header("HTTP/1.1 $statusMessage");
		$response['requestName']  = $requestName;
		$response['statusMessage'] = $statusMessage;
		$response['data'] = $data;

		$json_response=json_encode($response);
		echo $json_response;
	}
?>