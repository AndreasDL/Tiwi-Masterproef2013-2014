<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
   <title>International federation Monitor</title>
	<meta http-equiv="refresh" content="5" >
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">
    <!-- Le styles -->
    <link href="css/bootstrap.css" rel="stylesheet">
    <link href='http://fonts.googleapis.com/css?family=Sintony' rel='stylesheet' type='text/css'>
    <link href="css/style.css" rel="stylesheet">
    <link rel="shortcut icon" href="ico/favicon.ico">
  </head>
  <body>
    <div id="header"></div>
    <div class="container" id="content"><h1>International Federation Monitoring</h1>
  <table border="1">
  <tr>
   <th>Testbed Name</th>
   <th>Ping latency (ms)</th>
   <th>GetVersion Status</th>
   <th>Free Resources</th>
  </tr>
  <?php
    //todo webservice via config file
    $data = json_decode(file_get_contents("http://localhost/service/index.php/last"),true);
    //print_r($data);
    foreach ($data as $row){
        echo "<tr>"
            . "<td>".$row['testbedid']."</td>"
            . "<td>".$row['pingvalue']."</td>"
            . "<td>".$row['getversionstatus']."</td>"
            . "<td>".$row['freeresources']."</td>";
    }
  ?>
  </table>
    </div> <!-- /container -->
  </body>
</html>