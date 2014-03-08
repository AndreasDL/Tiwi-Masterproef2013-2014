<html>
<head>
	<title>database</title>
</head>
<body>
<?php
	if (!empty($_GET['name']) ){//check if valid request
		//request
		$name=$_GET['name'];
		$table = maketable();

		if (empty($table)){
			echo "id not found";
		}else{
			echo $table;
		}
	}else{
		echo "invalid request";
	}

	function maketable(){
		$con = mysqli_connect("localhost","test","hoi","testdb");
		if (mysqli_connect_errno()){
			echo "connect failed";
		}

		$query  = "select * from hotel";
		$result = mysqli_query($con,$query);
		if (!$result){
			die('query '.mysqli_error($con));
		}

		$table = "<table><tr><td>Id</td><td>Name</td><td>ping</td></tr>";
		while($row = mysqli_fetch_array($result)){
			$table .= "<tr><td>".$row['id']."</td><td>".$row['name']."</td><td>".$row['ping']."</td></tr>";
	    }
		mysqli_close($con);
		$table .= "</table>";
		return $table;

	}
?>
</body>
</html>