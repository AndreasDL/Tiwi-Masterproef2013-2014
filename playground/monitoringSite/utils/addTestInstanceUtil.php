<?php Include ( __DIR__ . '/../config.php'); ?>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <title>Add a testinstance</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="description" content="">
        <meta name="author" content="">
        <!-- Le styles -->
        <link href="../css/bootstrap.css" rel="stylesheet">
        <link href='http://fonts.googleapis.com/css?family=Sintony' rel='stylesheet' type='text/css'>
        <link href="../css/style.css" rel="stylesheet">
        <link rel="shortcut icon" href="favicon.ico">
        <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
        <script>
            $(document).ready(function() {
                var testbeds;
                var definitions;
                $.ajax({
                    url: "<?php echo $GLOBALS['urlTestbed']; ?>",
                    dataType: 'json',
                    async: false,
                    success: function(json){
                        testbeds = json['data'];
                    }
                });
                $.ajax({
                    url: "<?php echo $GLOBALS['urlTestDefinitions']; ?>",
                    dataType: 'json',
                    async: false,//make sure testtypes are loaded before initialising the parameters for the first item
                    success: function(json){
                        definitions = json['data'];
                        $.each(definitions,function(key,val){
                            $('#type').append("<option value="+key+">"+key+"</option>");
                        });
                }});
                
                var change = function() {
                    $('#table').find("tr:gt(2)").remove();
                    $.each(definitions[$('#type').val()]['parameters'], function(key, val) {
                        if (key == 'testbed'){
                            $('#table').append("<tr><td><b>" + key + "&nbsp;</b></td><td><select id=\"testbed\" name=" + key + "></td></td>");
                            $.each(testbeds,function(key,val){
                                $('#testbed').append("<option value="+key+">"+key+"</option>");
                            });
                        }else{
                            $('#table').append("<tr><td><b>" + key + "&nbsp;</b></td><td><input type=text id=" + key + " name=" + key + "></td></tr>");
                        }
                    })
                };
                $('#type').change(function(){change()});//update params when testtype changes
                change();//init first parameters
                
            });
        </script>
    </head>
    <body>
        <div id="header"></div>
        <div class="container" id="content"><h1>Add a testinstance</h1>
            <form action="<?php echo $GLOBALS['urlAddTestInstance']; ?>" method="post">
                <table id="table">
                    <tr><td><b>TestName &nbsp;</b></td><td><input type="text" name="testname"></td></tr>
                    <tr><td><b>Type &nbsp;</b></td><td><select id="type" name="testdefinitionname"></select></td></tr>
                    <tr><td><b>Frequency &nbsp;</b></td><td><input type="number" name="frequency"></td></tr>
                </table>
                <input type="submit" value="add">
            </form>
        </div> <!-- /container -->
    </body>
</html>