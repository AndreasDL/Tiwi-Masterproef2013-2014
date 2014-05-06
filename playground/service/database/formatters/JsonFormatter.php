<?php
/**
 * formats output as json
 * for more information take a look at the interface
 */

class JsonFormatter implements iFormatter{
    public function format($req){
        
        http_response_code($req->getStatus());
        if ($req->getStatus() == 200){
            //return json_encode(array('status' => $req->getStatus() , 'msg' => $req->getMsg(), "data" => $req->getData()));
            return json_encode($req->getData(),JSON_BIGINT_AS_STRING|JSON_NUMERIC_CHECK);
        }else{
            return json_encode($req->getMsg(),JSON_BIGINT_AS_STRING);
        }
    }
}
