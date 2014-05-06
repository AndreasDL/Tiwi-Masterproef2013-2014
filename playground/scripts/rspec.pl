#!/usr/local/bin/perl
use warnings;
use strict;
use LWP::UserAgent;
use IO::Uncompress::Gunzip qw/gunzip $GunzipError/;
use JSON;
use Data::Dumper;

#settings
my $baseUrl = "http://f4f-mon-dev.intec.ugent.be/";
my $dataUrl = $baseUrl . 'service/index.php/last?testdefinitionname=listResources';
my $outputDir = "/home/drew/test/";

##########################################################################
my $ua = LWP::UserAgent->new ();
$ua->default_header('Accept-Encoding' => 'gzip');
my $hash = decode_json get ($ua, $dataUrl);

#print Dumper($hash);

#get rspec urls from /last call
foreach my $key ( keys %{$hash} ){
    my $rspecUrl = $baseUrl . $hash->{$key}->{"results"}->{"rspec"};
    my $testbed = $hash->{$key}->{"testbeds"}->[0];
    print "Rspec: " . $testbed . " url: " . $rspecUrl . "\n";

    get ($ua, $dataUrl);
    $ua->default_header('Accept-Encoding' => 'gzip');
    my $rspec = get($ua, $rspecUrl);

    open (RPCK, ">>$outputDir$testbed");
    print RPCK "$rspec";
    close (RPCK);
}

print "All file have been saved in " . $outputDir . "\n";
exit;





sub get{
    my ($ua, $url) = @_;
    my $response = $ua->get ($url);
    if ($response->is_success ()) {
        my $content_encoding = $response->header ('Content-Encoding');
        my $text = $response->content;
        if ($content_encoding) {
            #print "Content encoding is $content_encoding.\n";
            if ($content_encoding eq 'gzip') {
                my $uncompressed;
                gunzip \$text, \$uncompressed
                    or die "gunzip failed: $GunzipError.\n";
                #printf "Uncompressed from %d bytes to %d bytes.\n",
                #    length $text, length $uncompressed;
                $text = $uncompressed;
            }
        }
        else {
            #print "Content encoding is not set.\n";
        }
        return $text;
    }
    else {
        print STDERR "get '$url' failed: ", $response->status_line, "\n";
    }
}