use strict;
use warnings;
use LWP::UserAgent;
 

my $ua = LWP::UserAgent->new;
 
my $server_endpoint = "http://f4f-mon-dev.intec.ugent.be/service/index.php/last?testdefinitionname=listResources";


# set custom HTTP request header fields
my $req = HTTP::Request->new(GET => $server_endpoint);
$req->header('content-type' => 'application/json');
#$req->header('x-auth-token' => 'kfksj48sdfj4jd9d');
 
my $resp = $ua->request($req);
if ($resp->is_success) {
    my $message = $resp->decoded_content;
    print "Received reply: $message\n";
}else {
    print "HTTP GET error code: ", $resp->code, "\n";
    print "HTTP GET error message: ", $resp->message, "\n";
}