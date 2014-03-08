<?php
	header("content-Type:application/json");
	include("function.php");

	if (!empty($_GET['name']) ){//check if valid request
		//request
		$name=$_GET['name'];
		//$price=getPrice($name);
		$ping=getPrice($name);

		if (empty($ping)){
			//book not found
			deliver_response(200,"id not found",NULL);
		}else{
			//book price
			deliver_response(200,"id found",$ping);
		}
	}else{
		deliver_response(200,"invalid request",NULL);
	}

	function deliver_response($status,$status_message,$data){
		
		header("HTTP/1.1 $status $status_message");
		$response['status']=$status;
		$response['status_message']=$status_message;
		$response['data']=$data;

		$json_response=json_encode($response);
		echo $json_response;
	}
?>