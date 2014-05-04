<?php
/**
 * formats output a prettyjson to make it more readable
 */
class PrettyJsonFormatter implements iFormatter{
    public function format($req){
        
        http_response_code($req->getStatus());
        
        if ($req->getStatus() == 200){
            return "<pre>" . json_encode($req->getData(),JSON_PRETTY_PRINT) . "</pre>";
        }else{
            return "<pre>" . json_encode(array('status' => $req->getStatus() , 'msg' => $req->getMsg(), "data" => $req->getData()),JSON_PRETTY_PRINT) . "</pre>";
        }
    }
}

