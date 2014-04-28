<?php

/**
 * Builds a query
 * @author Andreas De Lille
 */
class QueryBuilder {
    function __construct() {
        
    }

    /**
     * Will add and any clause to the query. Use with caution after this function is used you need to check if there are parameters added in the paramsforuse.
     * If so the ending bracelet ')' should be added after all other parameters are added to the query.
     * This function will only add a clausule if the parameter is set.
     * @param string $query the query to add the any clausule
     * @param array $params parameters of the request
     * @param array $paramsForUse the array to use with pg_query_params
     * @param string $paramName the name of the parameter in the request
     * @param string $viewName the name of the view to use
     * @param string $colName the name in this case id 
     */
    public function addAnyIfNeeded(&$query, &$params, &$paramsForUse, $paramName, $viewName, $colName = 'id') {
        //not sure if this works 2 times on the same query
        if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            if (sizeof($paramsForUse) == 0) {
                $query .= " where ";
            } else {
                $query .= " and ";
            }
            $query .= $colName . "=any(select " . $colName . " from " . $viewName . " where parametervalue IN (";

            array_push($paramsForUse, $params[$paramName][0]);
            $query .= '$';
            $query .= sizeof($paramsForUse);

            for ($i = 1; $i < sizeof($params[$paramName]); $i++) {
                array_push($paramsForUse, $params[$paramName][$i]);
                $query .= ',$';
                $query .= sizeof($paramsForUse);
            }
            $query .= ") ";
        }
    }

    /**
     * adds an in clausule if the paramName is set in params
     * @param string $query the query to add the any clausule
     * @param array $params parameters of the request
     * @param array $paramsForUse the array to use with pg_query_params
     * @param string $paramName the name of the parameter in the request
     * @param string $colName the name of the column in the database associated with the paramname
     */
    public function addInIfNeeded(&$query, &$params, &$paramsForUse, $paramName, $colName) {

        if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            if (sizeof($paramsForUse) == 0) {
                $query .= " where ";
            } else {
                $query .= " and ";
            }

            $query .= $colName . " IN (";
            array_push($paramsForUse, $params[$paramName][0]);
            $query .= "$";
            $query .= sizeof($paramsForUse);
            for ($i = 1; $i < sizeof($params[$paramName]); $i++) {
                array_push($paramsForUse, $params[$paramName][$i]);
                $query .= ",$";
                $query .= sizeof($paramsForUse);
            }
            $query .= ") ";
        }
    }

    /**
     * adds an >= clausule if the paramName is set in params. although the name is greaterthan it is actually greater than or equal
     * @param string $query the query to add the any clausule
     * @param array $params parameters of the request
     * @param array $paramsForUse the array to use with pg_query_params
     * @param string $paramName the name of the parameter in the request
     * @param string $colName the name of the column in the database associated with the paramname
     */
    public function addGreaterThanIfNeeded(&$query, &$params, &$paramsForUse, $paramName, $colName) {
        if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            if (sizeof($paramsForUse) == 0) {
                $query .= "where ";
            } else {
                $query .= "and ";
            }

            $query .= $colName . " >= ";
            array_push($paramsForUse, $params[$paramName][0]);
            $query .= "$";
            $query .= sizeof($paramsForUse);
        }
    }

    /**
     * adds an <= clausule if the paramName is set in params. although the name is lowerthan it is actually lower than or equal
     * @param string $query the query to add the any clausule
     * @param array $params parameters of the request
     * @param array $paramsForUse the array to use with pg_query_params
     * @param string $paramName the name of the parameter in the request
     * @param string $colName the name of the column in the database associated with the paramname
     */
    public function addLowerThanIfNeeded(&$query, &$params, &$paramsForUse, $paramName, $colName) {
        if (isset($params[$paramName]) && strtoupper($params[$paramName][0]) != 'ALL') {
            if (sizeof($paramsForUse) == 0) {
                $query .= "where ";
            } else {
                $query .= "and ";
            }

            $query .= $colName . " <= ";
            array_push($paramsForUse, $params[$paramName][0]);
            $query .= "$";
            $query .= sizeof($paramsForUse);
        }
    }
}
