<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
   <title>Add a testbed</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">
    <!-- Le styles -->
    <link href="../css/bootstrap.css" rel="stylesheet">
    <link href='http://fonts.googleapis.com/css?family=Sintony' rel='stylesheet' type='text/css'>
    <link href="../css/style.css" rel="stylesheet">
    <link rel="shortcut icon" href="favicon.ico">
    <?php 
        Include ( __DIR__.'/../config.php');
    ?>
  </head>
  <body>
    <div id="header"></div>
    <div class="container" id="content"><h1>Add a testbed</h1>
        <form action="<?php echo $GLOBALS['urlAddTestbed'] ?>" method="post">
            <table>
                <tr><td><b>Name &nbsp;</b></td><td><input type="text" name="testbedName"></td></tr>
                <tr><td><b>Url: &nbsp;</b></td><td><input type="text" name="url"></td></tr>
                <tr><td><b>Urn: &nbsp;</b></td><td><input type="text" name="urn"></td></tr>
            </table>
            <input type="submit" value="add">
        </form>
    </div> <!-- /container -->
  </body>
</html>

