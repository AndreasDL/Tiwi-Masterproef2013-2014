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
  <table border="1" >
  <tr>
   <th>Test Name</th>
   <th>Last execution (CET)</th>
   <th>Last failure</th>
   
   <th>Status</th>
   <th>log</th>
   <th>History</th>
  </tr>
  <?php
    //todo webservice via config file
    Include ( __DIR__.'/../config.php');
    
    $data = json_decode(file_get_contents($GLOBALS['urlStiching']),true);
    $data = $data['data'];
    date_default_timezone_set('CET');
    //print_r($data);
    $subTests=array('setUp','getUserCredential','generateRspec','createSlice','initStitching','callSCS','callCreateSlivers','waitForAllReady','loginAndPing','callDeletes');
    foreach ($data as $key => $row){
        echo "<tr>";
            echo "<td>".$row['testname']."</td>";
            
            echo "<td>".date('d/m/Y - H:i:s',strtotime($row['timestamp']))."</td>";
            echo "<td> Not supported yet!</td>";
            
            echo "<td><table RULES=COLS><tr>";
                foreach($subTests as $test){
                    echo "<td bgcolor=";
                    if ($row['results'][$test] == $GLOBALS['goodStitch']){
                        echo "#00FF00>";
                    }else if($row['results'][$test] == $GLOBALS['warnStitch']){
                        echo "#FF9933>";
                    }else if($row['results'][$test] == $GLOBALS['skipStitch']){
                        echo "#2942FF>";
                    }else{
                        echo "#FF0000>";
                    }
                    
                    echo "&nbsp&nbsp&nbsp</td>";
                }
            echo "</tr></table></td>";
            echo "<td><a href=".$row['log'].">log</a></td>";
            
            echo "<td><a href=./history.php?testname=".$row['testname'].">history</a></td>";
            
        echo "</tr>";
    }
  ?>
  </table>
    </div> <!-- /container -->
  </body>
</html>