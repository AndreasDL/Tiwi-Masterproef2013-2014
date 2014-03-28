<?php

class JsonFormatter implements iFormatter{
    public function format($data, $status = 200, $msg = 'Good!') {
        return json_encode(array('status' => $status , 'msg' => $msg, "data" => $data));
    }
}
