<?php
	//echo getPrice('a');
	function getPrice($name){
		$books=array("a"=>"1","b"=>"2","c"=>"3");

		if ( !array_key_exists($name, $books)){
			return NULL;
		}else{
			return $books[$name];
		}
	}
?>