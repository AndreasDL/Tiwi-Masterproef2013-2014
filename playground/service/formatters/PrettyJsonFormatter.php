<?php

class PrettyJsonFormatter implements iFormatter{
    public function format($data) {
        return "<pre>" . json_encode($data,JSON_PRETTY_PRINT) . "</pre>";
    }

}

