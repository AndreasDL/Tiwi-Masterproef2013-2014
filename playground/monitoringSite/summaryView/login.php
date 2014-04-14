<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
   <title>International federation Monitor - Login</title>
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
    <div class="container" id="content"><h1>International Federation Monitoring - Login</h1>
  <table border="1" >
  <tr>
   <th>Test Name</th>
   <th>Last execution (CET)</th>
   <th>Last failure</th>
   <th>Last duration</th>
   <th>Status</th>
   <th>log</th>
   <th>History</th>
  </tr>
  <?php
    //todo webservice via config file
    Include ( __DIR__.'/../config.php');
    date_default_timezone_set('CET');
    $data = json_decode(file_get_contents($GLOBALS['urlLogin']),true);
    $data = $data['data'];
    $testDefinitions = json_decode(file_get_contents($GLOBALS['urlTestDefinitions']),true);
    $testDefinitions= $testDefinitions['data'];
    
    foreach ($data as $key => $row){
        $subTests=$testDefinitions[$row['testtype']]['returnValues'];
        echo "<tr>";
            echo "<td>".$row['testname']."</td>";
            
            echo "<td>".date('d/m/Y - H:i:s',strtotime($row['timestamp']))."</td>";
            echo "<td> Not supported yet!</td>";
            $secs = $row['results']['duration'] / 1000;
            $min = floor($secs/60000);
            if (strlen($min) < 2){
                $min = '0' . $min;
            }
            $secs = $secs % 60000;
            if (strlen($secs) < 2){
                $secs = '0' . $secs;
            }
            echo "<td>".$min.":".$secs."</td>";
            
            echo "<td><table RULES=COLS><tr>";
                foreach($subTests as $name => $v){
                    if ($name != 'duration' && $name != 'result-overview' && $name != 'resultHtml'){
                        $value = ucfirst($row['results'][$name]);
                        echo "<td bgcolor=";
                        if ($value == $GLOBALS['good'] || $value == $GLOBALS['SUCCESS']){
                            echo "#00FF00>";
                        }else if($value == $GLOBALS['warn'] || $value == $globals['WARN']){
                            echo "#FF9933>";
                        }else if($value == $GLOBALS['skip'] || $value == $GLOBALS['skipped']){
                            echo "#2942FF>";
                        }else{
                            echo "#FF0000>";
                        }
                        
                        echo "&nbsp".  getAbbreviation($name)."&nbsp</td>";
                    }
                }
            echo "</tr></table></td>";
            echo "<td><a href=".$row['log'].">log</a></td>";
            
            echo "<td><a href=./history.php?testname=".$row['testname'].">history</a></td>";
            
        echo "</tr>";
    }
    
    function getAbbreviation(&$s){
        $ret = substr($s,0,1);
        for ($i = 0 ; $i < strlen($s);$i++){
            if(ctype_upper($s{$i})){
                $ret = $ret. $s{$i};
            }
        }
        return $ret;
    }
  ?>
  </table>
    </div> <!-- /container -->
  </body>
</html>