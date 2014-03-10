use strict;
use warnings;

my $file = "dumdat.sql";
open F , ">$file";

#create tables
print F "DROP TABLE results;
CREATE TABLE results (
	resultId serial PRIMARY KEY,
	testbedId character varying NOT NULL,
	testbedurl character varying NOT NULL,
	pingValue integer,
	pingStatus character varying,
	getVersionStatus character varying,
	freeResources integer
);
ALTER TABLE public.results OWNER TO postgres;
COPY results (testbedId, testbedurl, pingValue, pingStatus, getVersionStatus, freeResources)  FROM stdin;
";
for (my $i = 2 ;$i < 1000;$i++){
	my $testbedId = "testbed";
	$testbedId .= $i % 10;

	my $url = "http://www.$testbedId.com/";

	my $ping = int(rand()*200);
	my $pingStat = "Good";

	if ($ping > 100){
		$pingStat = "Warn";
	}

	my $ver = "ok";
	if (int(rand()*2) == 1){
		$ver = "NotOk";
	}
	my $freeresources = int(rand()*37);
	print F "$testbedId\t$url\t$ping\t$pingStat\t$ver\t$freeresources\n";
}
print F "\\.\n\n";
close F;