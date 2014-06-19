#clear database
su postgres -c "psql -d testdb < site/API/database/dummydatabase/database.sql";
#pass: post

#clear results
rm -rvf site/results;
rm -rvf site/monitor/monitorService/results;

#convert old to new
python playground/scripts/definitions.py
#python playground/scripts/wall1wall2wilab.py
python playground/scripts/convertOld.py 

#vergeet niet 
# -> cert in ~/.ssl/<certnaam> eventueel met bijhorende pass file indien cert encrypted is
# -> authorities.xml in ~/.jFed/authorities.xml
#monitoringSite en API in apachedir 