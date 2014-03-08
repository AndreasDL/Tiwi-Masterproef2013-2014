use strict;
use warnings;
use DBI;
use DBD::mysql;

#auth
my $user   = "test";
my $pass   = "hoi"; 
my $dbname = "testdb";

#statements
my $drop   = "DROP TABLE hotel";
my $create = "create table hotel( id INTEGER, name VARCHAR(50), ping INTEGER, PRIMARY KEY (id));";
my $insert = "insert into hotel(name,id,ping) values (?,?,?)";

#exec()
my $dbh = DBI->connect('dbi:mysql:'.$dbname,$user,$pass) or die( $DBI::errstr . "\n" );
#clear
my $sth;
$sth = $dbh->prepare($drop);
$sth->execute();
$sth = $dbh->prepare($create);
$sth->execute();
#fill
for (my $i = 0; $i < 1000 ; $i++){
	my $ping = rand()*100;
	$sth = $dbh->prepare($insert);
	$sth->execute("hoi".$i,$i,$ping);
	print "hoi".$i , " " , $ping , "\n";
}
$dbh->disconnect if defined($dbh);