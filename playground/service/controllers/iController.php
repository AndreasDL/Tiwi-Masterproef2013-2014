<?php
/**
 * this interface defines a controller. The controller has to call the right function in accesdatabase.php
 */
 interface iController{
     public function get($params);
 }