<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
   <title>International federation Monitor</title>
	<meta http-equiv="refresh" content="10" >
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">
    <!-- Le styles -->
    <link href="css/bootstrap.css" rel="stylesheet">
    <link href='http://fonts.googleapis.com/css?family=Sintony' rel='stylesheet' type='text/css'>
    <link href="css/style.css" rel="stylesheet">
    <link rel="shortcut icon" href="favicon.ico">
  </head>
  <body>
    <div id="header"></div>
    <div class="container" id="content"><h1>International Federation Monitoring</h1>
  <table border="1">
  <tr>
   <th>Testbed Name</th>
   <th>Ping latency (ms)</th>
   <th>GetVersion status</th>
   <th>Free resources</th>
  </tr>
  <?php
    //todo webservice via config file
    Include ( __DIR__.'/config.php');
    
    $data = json_decode(file_get_contents($GLOBALS['urlInternational']),true);
    $data = $data['data'];
    $testbedurls = json_decode(file_get_contents($GLOBALS['urlTestbed']),true);
    $testbedurls = $testbedurls['data'];
    //print_r($data);
    foreach ($data as $key => $row){
        echo "<tr>";
            echo "<td><a href=".$testbedurls[$row['testbeds'][0]]['url'].">".$row['testbeds'][0]."</a></td>";
            
            echo "<td bgcolor=";
            if ($row['results']['pingValue'] == $GLOBALS['fatalPing'] )  {
                echo "#FF0000>unreachable";
            } else if($row['results']['pingValue'] > $GLOBALS['warnPing']) {
                echo "#FF9933>".$row['results']['pingValue'];
            }else{
                echo "#00FF00>".$row['results']['pingValue'];
            }
            echo "</td>";
            
            echo "<td> Not supported yet!</td>";
            echo "<td> Not supported yet!</td>";
        echo "</tr>";
    }
  ?>
  </table>
    </div> <!-- /container -->
  </body>
</html>