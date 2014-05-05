<?php
/**
 * formats output a prettyjson to make it more readable
 */
class PrettyJsonFormatter implements iFormatter{
    public function format($req){
        
        http_response_code($req->getStatus());
        
        if ($req->getStatus() == 200){
            return "<pre>" . json_encode($req->getData(),JSON_PRETTY_PRINT|JSON_BIGINT_AS_STRING) . "</pre>";
        }else{
            return "<pre>" . json_encode($req->getMsg(),JSON_PRETTY_PRINT|JSON_BIGINT_AS_STRING) . "</pre>";
        }
    }
}

