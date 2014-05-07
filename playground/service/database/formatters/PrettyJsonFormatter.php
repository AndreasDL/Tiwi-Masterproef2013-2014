<?php
/**
 * Formats output a prettyjson to make it more readable.
 * For more information take a look at the interface
 */
class PrettyJsonFormatter implements iFormatter{
    public function format($req){
        
        http_response_code($req->getStatus());
        
        if ($req->getStatus() == 200){
            $json = json_encode($req->getData(),JSON_PRETTY_PRINT)/*|JSON_BIGINT_AS_STRING)*/;
            $json = preg_replace('/"ts":(.*)"([0-9]+)"/', '"ts":\1\2', $json); //64bit int on php are only support on 64bit machine WITH linux NOT on windows
            return "<pre>" . $json . "</pre>";
        }else{
            return "<pre>" . json_encode($req->getMsg(),JSON_PRETTY_PRINT)/*|JSON_BIGINT_AS_STRING)*/ . "</pre>";
        }
    }
}

