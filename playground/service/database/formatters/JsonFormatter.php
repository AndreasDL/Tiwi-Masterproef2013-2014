<?php
/**
 * formats output as json
 */

class JsonFormatter implements iFormatter{
    public function format($req){
        
        http_response_code($req->getStatus());
        if ($req->getStatus() == 200){
            //return json_encode(array('status' => $req->getStatus() , 'msg' => $req->getMsg(), "data" => $req->getData()));
            return json_encode($req->getData(),JSON_BIGINT_AS_STRING);
        }else{
            return json_encode($req->getMsg(),JSON_BIGINT_AS_STRING);
        }
    }
}
