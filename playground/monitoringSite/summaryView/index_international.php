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
    <link href="../css/bootstrap.css" rel="stylesheet">
    <link href='http://fonts.googleapis.com/css?family=Sintony' rel='stylesheet' type='text/css'>
    <link href="../css/style.css" rel="stylesheet">
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
    Include ( __DIR__.'/../config.php');
    
    parse_str($_SERVER['QUERY_STRING'], $parameters);
    $data = json_decode(file_get_contents($GLOBALS['webService'].'/last?testdefinitionname=ping,getVersion2,listResources'),true);
    $testbeds = json_decode(file_get_contents($GLOBALS['webService'].'/testbed'),true);
    
    //group results per testbeds
    $groupedData;
    foreach ($data as $key => $result){
        $groupedData[$result['testbeds'][0]][$result['testdefinitionname']] = $result['results'];
    }
    
    foreach ($groupedData as $name => $results){
        echo "<tr>";
            echo "<td><a href=".urlencode($testbeds[$name]['url']).">".$name."</a></td>";
            
            echo "<td bgcolor=";
            if ($results['ping']['pingValue'] == $GLOBALS['fatalPing'] )  {
                echo "#FF0000>unreachable";
            } else if($results['ping']['pingValue'] > $GLOBALS['warnPing']) {
                echo "#FF9933>".$results['ping']['pingValue'];
            }else{
                echo "#00FF00>".$results['ping']['pingValue'];
            }
            echo "</td>";
            
            echo "<td bgcolor=";
            if ($results['getVersion2']['testGetVersionXmlRpcCorrectness'] == $GLOBALS['fatal'] || $results['getVersion2']['testGetVersionXmlRpcCorrectness'] == 'FAILED')  {
                echo "#FF0000>".$results['getVersion2']['testGetVersionXmlRpcCorrectness'];
            } else if($results['ping']['testGetVersionXmlRpcCorrectness'] > $GLOBALS['warn']) {
                echo "#FF9933>".$results['getVersion2']['testGetVersionXmlRpcCorrectness'];
            }else{
                echo "#00FF00>".$results['getVersion2']['testGetVersionXmlRpcCorrectness'];
            }
            echo "</td>";
            
            echo "<td bgcolor=";
            if ($results['listResources']['count'] <= $GLOBALS['listFatal'] )  {
                echo "#FF0000>".$results['listResources']['count'];
            } else if($results['listResources']['count'] < $GLOBALS['listWarn']) {
                echo "#FF9933>".$results['listResources']['count'];
            }else{
                echo "#00FF00>".$results['listResources']['count'];
            }
            echo "</td>";
            
        echo "</tr>";
    }
    
  ?>
  </table>
    </div> <!-- /container -->
  </body>
</html>