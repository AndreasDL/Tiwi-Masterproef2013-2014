import psycopg2
import sys
import pprint
import random


dbname = "loadtest"
user = "postgres"
dass = "post"
durl = "localhost"

if (len(sys.argv) > 1):
    dbname = str(sys.argv[1])

print("Connecting to database")
con = psycopg2.connect(database=dbname,user=user,password=dass,host=durl)
cur = con.cursor()

cur.execute("delete from subresults")
cur.execute("delete from results")
con.commit()