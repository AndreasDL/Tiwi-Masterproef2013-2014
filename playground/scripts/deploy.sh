#cleanup
rm -rvf /root/www/*;
rm -rvf /root/psqlhome/*;
rm /root/.jFed/authorities.xml;
crontab -r;

#install
cp -vr /root/converted/monitoringSite /root/www/;
cp -vr /root/converted/API /root/www/;
cp -v  /root/converted/monitorService-beta-jar-with-dependencies.jar /root/www/;
cp -vr /root/converted/API/database/dummydatabase/* /root/psqlHome/;
cp -v  /root/converted/authorities.xml /root/.jFed/authorities.xml;

#reset database
cd /root/psqlHome;
su postgres -c "psql -d testdb < database.sql";
cd -;
#pass: post

#filldatabase
#python3.4
source ~/.virtualenvs/py34/bin/activate;
python /root/converted/definitions.py;
python /root/converted/wall1wall2wilab.py;
deactivate;
#python playground/scripts/convertOld.py #old to new use with caution

crontab /root/converted/crontab;
#vergeet niet 
# -> cert in ~/.ssl/<certnaam> eventueel met bijhorende pass file indien cert encrypted is
# -> authorities.xml in ~/.jFed/authorities.xml
#monitoringSite en API in apachedir 
