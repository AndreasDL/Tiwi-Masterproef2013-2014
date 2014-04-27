<?php
/**
 * formats output as json
 */
class JsonFormatter implements iFormatter{
    public function format($req){
        return json_encode(array('status' => $req->getStatus() , 'msg' => $req->getMsg(), "data" => $req->getData()));
    }
}
