<?php
// The data to send to the API
$postData = array(
    'kind' => 'blogger#post',
    'title' => 'A new post',
    'content' => 'With exciting content...'
);

// Create the context for the request
$context = stream_context_create(array(
    'http' => array(
        // http://www.php.net/manual/en/context.http.php
        'method' => 'POST',
        'header' => "Content-Type: application/json\r\n",
        'content' => json_encode($postData)
    )
));

// Send the request
$response = file_get_contents('http://localhost/service/test.php', FALSE, $context);

// Check for errors
if($response === FALSE){
    die('Error');
}
var_dump($response);
// Decode the response
$responseData = json_decode($response, TRUE);

// Print the date from the response
print "Response";
print_r($responseData);