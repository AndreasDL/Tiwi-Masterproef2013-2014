<?php
/**
 * formats the output to a given format.
 */
 interface iFormatter{
     /**
      * formats a request, returns the formatted request, e.g. in json.
      * @param Request $req the request
      */
     public function format($req);
 }

