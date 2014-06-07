rm -rvf /root/www/service/;
rm -rvf /root/www/monitoringSite/;
rm -rvf /root/psqlHome/*;

rm -rvf /root/service/nbproject;
rm -rvf /root/monitoringSite/nbproject;

cp -rv /root/devel/service/ /root/www/;
cp -rv /root/devel/monitoringSite /root/www/;

cp -rv /root/devel/service/database/dummydatabase/* /root/psqlHome/;
cd /root/psqlHome;
su postgres -c "psql -d testdb < database.sql";
su postgres -c "php -f filldatabase.php home=/root/ results=5";
cd ~;

cp -rv /root/devel/stresstest-beta-jar-with-dependencies.jar /root/www;
cp -rv /root/devel/stressTest.sh /root/;
