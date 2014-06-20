#clear database
su postgres -c "psql -d monitoring < site/API/database/dummydatabase/database.sql;
psql -d loadtest < site/API/database/dummydatabase/database.sql;";
#pass: post

#clear results
rm -rvf site/results;
rm -rvf site/monitor/monitorService/results;

#convert old to new
python playground/scripts/definitions.py "monitoring";
python playground/scripts/convertOld.py "monitoring" "False";
#python playground/scripts/wall1wall2wilab.py

python playground/scripts/definitions.py "loadtest";
python playground/scripts/convertOld.py "loadtest" "False";

#vergeet niet 
# -> cert in ~/.ssl/<certnaam> eventueel met bijhorende pass file indien cert encrypted is
# -> authorities.xml in ~/.jFed/authorities.xml
#monitoringSite en API in apachedir 

# Site 2 keer goed zetten (juiste databank)
# zelfde met API
# zelfde met jars