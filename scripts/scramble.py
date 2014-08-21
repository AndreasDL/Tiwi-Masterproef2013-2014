import psycopg2
import sys
import pprint
import random


dbname = "monitoring"
user = "postgres"
dass = "post"
durl = "localhost"

if (len(sys.argv) > 1):
    dbname = str(sys.argv[1])

print("Connecting to database")
con = psycopg2.connect(database=dbname,user=user,password=dass,host=durl)
cur = con.cursor()
cus = con.cursor()

getTests = "select testinstanceid,frequency from testinstances"
changeNextRun = "update testinstances set nextrun = nextrun + interval '%s second' where testinstanceid = %s"

cur.execute(getTests)
test = cur.fetchone()
while (test != None):
	#pprint.pprint(test)
	time = random.randint(0,test[1]-1);
	cus.execute(changeNextRun,(time,test[0]))

	test = cur.fetchone()

con.commit()