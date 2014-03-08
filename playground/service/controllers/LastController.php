<html>
    <head>
        <title>LastController test</title>
    </head>
<body>
<?php
class LastController {
    public function get($params){
        print "LastController - get()<br>";
        foreach ($params as $key => $value){
            print "$key => $value<br>";
        }
        
        $link = pg_Connect("dbname=testdb user=postgres password=post");
        $result = pg_exec($link,"select * from results order by testbedId");
        
        print "<table>";
        print "<tr><th>testbed</th><th>ping</th><th>getVersion</th><th>free resources</th></tr>";
        while ($row  = pg_fetch_assoc($result)){
            print "<tr>"
                    . "<td><a href=\"$row[testbedurl]\">$row[testbedid]</a></td>"
                    . "<td>$row[pingvalue]</td>"
                    . "<td>$row[getversionstatus]</td>"
                    . "<td>$row[freeresources]</td>"
               . "</tr>";
        }
        
        print "</table>";
    }
}?>
</body>
</html>