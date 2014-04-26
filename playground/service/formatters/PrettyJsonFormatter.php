<?php
/**
 * formats output a prettyjson to make it more readable
 */
class PrettyJsonFormatter implements iFormatter{
    public function format($req){//data, $status = 200, $msg = 'Good!') {
        return "<pre>" . json_encode(array('status' => $req->getStatus() , 'msg' => $req->getMsg(), "data" => $req->getData()),JSON_PRETTY_PRINT) . "</pre>";
    }
}

