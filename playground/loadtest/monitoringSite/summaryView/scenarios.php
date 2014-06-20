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
        <!-- Include jQuery and PowerTip -->
    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.js"></script>
    <!-- begin-scripts -->
    <script type="text/javascript" src="../jquery-powertip/src/core.js"></script>
    <script type="text/javascript" src="../jquery-powertip/src/csscoordinates.js"></script>
    <script type="text/javascript" src="../jquery-powertip/src/displaycontroller.js"></script>
    <script type="text/javascript" src="../jquery-powertip/src/placementcalculator.js"></script>
    <script type="text/javascript" src="../jquery-powertip/src/tooltipcontroller.js"></script>
    <script type="text/javascript" src="../jquery-powertip/src/utility.js"></script>
    <script type="text/javascript">
            $(function() {
                    // placement examples
                    $('.north').powerTip({ placement: 'n' });
                    $('.east').powerTip({ placement: 'e' });
                    $('.south').powerTip({ placement: 's' });
                    $('.west').powerTip({ placement: 'w' });
                    $('.north-west').powerTip({ placement: 'nw' });
                    $('.north-east').powerTip({ placement: 'ne' });
                    $('.south-west').powerTip({ placement: 'sw' });
                    $('.south-east').powerTip({ placement: 'se' });
                    $('.north-west-alt').powerTip({ placement: 'nw-alt' });
                    $('.north-east-alt').powerTip({ placement: 'ne-alt' });
                    $('.south-west-alt').powerTip({ placement: 'sw-alt' });
                    $('.south-east-alt').powerTip({ placement: 'se-alt' });
            });
    </script>
    <!-- end-scripts -->
    <link rel="stylesheet" type="text/css" href="../jquery-powertip/css/jquery.powertip.css" />
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
    Include ( __DIR__.'/StatusTable.php');
    $data = json_decode(file_get_contents($GLOBALS['urlStiching']),true);
    $data = $data['data'];
    $testDefinitions = json_decode(file_get_contents($GLOBALS['urlTestDefinitions']),true);
    $testDefinitions= $testDefinitions['data'];
    date_default_timezone_set('CET');
    //print_r($data);
    //$subTests=array('setUp','getUserCredential','generateRspec','createSlice','initStitching','callSCS','callCreateSlivers','waitForAllReady','loginAndPing','callDeletes');
    
    foreach ($data as $key => $row){
        $subTests=$testDefinitions[$row['testtype']]['returnValues'];
        //print_r($subTests);
        echo "<tr>";
            echo "<td>".date('d/m/Y - H:i:s',strtotime($row['timestamp']))."</td>";
            $secs = $row['results']['duration'] / 1000;
            $min = floor($secs/60);
            if (strlen($min) < 2){
                $min = '0' . $min;
            }
            $secs = $secs % 60;
            if (strlen($secs) < 2){
                $secs = '0' . $secs;
            }
            echo "<td align=right>".$min.":".$secs."</td>";
            
            echo getTable($subTests,$row);
            
            echo "<td><a href=../../".$row['log'].">log</a></td>";
            echo "<td><a href=../../".$row['results']['resultHtml'].">resultsHtml</a></td>";
            echo "<td><a href=../../".$row['results']['result-overview'].">overview</a></td>";
            
        echo "</tr>";
    }
  ?>
  </table>
    </div> <!-- /container -->
  </body>
</html>