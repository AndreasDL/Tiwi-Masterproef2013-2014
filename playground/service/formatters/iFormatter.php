<?php
/**
 * formats the output to a given format.
 */
 interface iFormatter{
     /**
      * formats a request, returns the formatted request, e.g. in json.
      */
     public function format($req);
 }

