<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * this class will add filters to the query such as testbed=... testtype= ... => where testbed= ...
 * @author Andreas De Lille
 */
interface iFilter {

    /**
     * Fix filters for the list call
     * @param String $query the query
     * @param array $params the params given in the request
     * @param array $paramsForUse an (empty) array to store the used values in.
     */
    public function filterList(&$query, &$params, &$paramsForUse);

    /**
     * Fix filters for the testInstance call
     * @param String $query the query
     * @param array $params the params given in the request
     * @param array $paramsForUse an (empty) array to store the used values in.
     */
    public function filterTestInstance(&$query, &$params, &$paramsForUse);

    /**
     * Fix filters for the testbed call
     * @param String $query the query
     * @param array $params the params given in the request
     * @param array $paramsForUse an (empty) array to store the used values in.
     */
    public function filterTestbed(&$query, &$params, &$paramsForUse);

    /**
     * Fix filters for the testDefinition call
     * @param String $query the query
     * @param array $params the params given in the request
     * @param array $paramsForUse an (empty) array to store the used values in.
     */
    public function filterDefinition(&$query, &$params, &$paramsForUse);
}
