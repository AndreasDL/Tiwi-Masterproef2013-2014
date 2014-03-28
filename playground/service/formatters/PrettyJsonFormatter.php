<?php

class PrettyJsonFormatter implements iFormatter{
    public function format($data, $status = 200, $msg = 'Good!') {
        return "<pre>" . json_encode(array('status' => $status , 'msg' => $msg, "data" => $data),JSON_PRETTY_PRINT) . "</pre>";
    }

}

