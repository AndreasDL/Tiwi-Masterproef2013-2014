use strict;
use warnings;
use DBI;
use DBD::mysql;

#auth
my $user   = "test";
my $pass   = "hoi"; 
my $dbname = "testdb";

#tables
my @tables = ({
	"name" => "testbeds",
	"columns" => "testbedid INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
		name VARCHAR(100)"
},{
	"name" => "tests",
	"columns" => "testid INTEGER NOT NULL,
		testbedid INTEGER NOT NULL,
		description VARCHAR(100),
		PRIMARY KEY(testid,testbedid)"
},{
	"name" => "results",
	"columns" => "testid INTEGER NOT NULL,
		testbedid INTEGER NOT NULL,
		resultid INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
		status VARCHAR(100),
		timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
		value VARCHAR(100)"
});

#no output buffering
$| = 1;

#script
print "connecting\n";
my $dbh = DBI->connect('dbi:mysql:'.$dbname,$user,$pass) or die( $DBI::errstr . "\n" );
#dropping
print "dropping tables\n";
my $sth;
foreach (@tables){
	print "\tdropping $_->{'name'}\n";
	$sth = $dbh->prepare("DROP TABLE $_->{'name'};");
	$sth->execute();
}
#creating
print "creating tables\n";
foreach (@tables){
	print "\tCreating table $_->{'name'}\n";
	$sth = $dbh->prepare("CREATE TABLE $_->{'name'} ($_->{'columns'});");
	$sth->execute();
}
#filling
print "filling tables\n";
print "\tfilling testbeds\n";
my @names = ("hoi","hi","andreas","iminds","ibcn");
for (my $i = 0 ; $i < 5 ; $i++){
	$sth = $dbh->prepare("insert into testbeds (name) values (?)");
	$sth->execute($names[$i]);
}
print "\tfilling tests\n";
my @tests = ("ping","version","freeRes","status","duration");
for (my $i = 0 ; $i < 5 ; $i++){
	for (my $j = 0 ; $j < 5 ; $j++){
		$sth = $dbh->prepare("insert into tests (testid,testbedid,description,frequency,command) values (?,?,?,?,?)");
		$sth->execute($j,$i,$tests[$j],$j,$tests[$j]);
	}
}
print "\tfilling results\n";
for (my $k=0;$k<3;$k++){
	for (my $i=0;$i<5;$i++){
		for(my $j=0;$j<5;$j++){
			$sth = $dbh->prepare("insert into results (testid,testbedid,status,value) values (?,?,?,?)");
			$sth->execute($j,$i,"good",$k);
		}
	}
	`sleep 1`;#different timestamp for each test
}
print "disconnecting\n";
$dbh->disconnect if defined($dbh);