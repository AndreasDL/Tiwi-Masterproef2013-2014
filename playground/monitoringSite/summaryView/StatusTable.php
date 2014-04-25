<?php

function getTable(&$subTests,&$row){
    $ret = "<td><table RULES=COLS><tr>";
    $i = 0;
        foreach($subTests as $name => $v){
            if ($name != 'duration' && $name != 'result-overview' && $name != 'resultHtml' && $name != 'returnValue'){
                $value = ucfirst($row['results'][$name]);
                $ret .= "<td bgcolor=";
                if ($value == $GLOBALS['good'] || $value == $GLOBALS['SUCCESS']){
                    $ret .= "#00FF00";
                }else if($value == $GLOBALS['warn'] || $value == $GLOBALS['WARN']){
                    $ret .= "#FF9933";
                }else if($value == $GLOBALS['skip'] || $value == $GLOBALS['skipped']){
                    $ret .= "#999999";
                }else{
                    $ret .= "#FF0000";
                }

                $title  = "<p><b>TestName:</b> ".$name."</p>";
                $title .= "<p><b>Status:</b> ".$value."</p>";
                $ret .= " class=south-west title=\"".$title."\"><a href=../../".$row['results']['resultHtml']."#test".$i." id=blackLink>&nbsp".$i."&nbsp</a></td>";
                $i++;
            }
        }
    $ret .= "</tr></table></td>";

    
    return $ret;
}

