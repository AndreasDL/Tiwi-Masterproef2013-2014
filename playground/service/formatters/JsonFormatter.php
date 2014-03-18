<?php

class JsonFormatter implements iFormatter{
    public function format($data) {
        return json_encode($data);
    }

}
