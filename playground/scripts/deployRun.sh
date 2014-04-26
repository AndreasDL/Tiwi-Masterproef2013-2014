rm -rvf /root/www/longrun/*;
rm -rvf /root/psqlHome/*;

rm -rvf /root/longrun/service/nbproject;
rm -rvf /root/longrun/monitoringSite/nbproject;

mkdir /root/www/longrun
cp -rv /root/longrun/* /root/www/longrun/;

cp -rv /root/longrun/service/database/dummydatabase/* /root/psqlHome/;
cd /root/psqlHome;
su postgres -c "psql -d testrundb < database.sql";
su postgres -c "php -f fillrundatabase.php home=/root/ results=0 stitch=1 testbeds=0";
cd ~;

cp -rv /root/longrun/Service-beta-jar-with-dependencies.jar /root/www/longrun/;

#set crontab
crontab /root/longrun/crontab
