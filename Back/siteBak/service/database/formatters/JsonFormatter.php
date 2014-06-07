<?php
/**
 * formats output as json
 * for more information take a look at the interface
 */

class JsonFormatter implements iFormatter{
    public function format($req){
        
        http_response_code($req->getStatus());
        if ($req->getStatus() == 200){
            $json = json_encode($req->getData());
            $json = preg_replace('/"ts":(.*)"([0-9]+)"/', '"ts":\1\2', $json); //64bit int on php are only support on 64bit machine WITH linux NOT on windows
            return $json;
        }else{
            return json_encode($req->getMsg());
        }
    }
}
