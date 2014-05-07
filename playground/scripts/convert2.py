import re
import pprint #debug
import os
import psycopg2

#settings
baseDir = "/home/drew/masterproef/f4ftestsuite/trunk/monitor_site/work/monitoring/contexts/"
certDir = "/home/drew/masterproef/scripts/overzetten/" #where to find all the certificates
firstDir = ['fls','international'] #dirs for ping, getVersion & list resources

dbname = "testdb"
user = "postgres"
dass = "post"
durl = "localhost"
pingFreq = 300
listFreq = 3600
nextRun = "2014-5-7T12:00:00"
enabled = False

testbeds = {}

def addTestbed(map,cur):
	cur.execute("INSERT INTO testbeds (testbedname,url,urn,username,UserAuthorityUrn,passwordFilename,pemKeyAndCertFilename) VALUES(%s,%s,%s,%s,%s,%s,%s)",
		(map['testbedname'],
		map['pinghost'],
		map['testedAggregateManagerUrn'],
		map['username'],
		map['userAuthorityUrn'],
		certDir + map['passwordFilename'].split("/")[-1],
		certDir + map['pemKeyAndCertFilename'].split("/")[-1]
		)) #add testbed

def addpingTest(map,cur):
	cur.execute("INSERT INTO testinstances (testname,testDefinitionName,frequency,nextrun,enabled) VALUES(%s,%s,%s,%s,%s) RETURNING testinstanceid",(map['testbedname']+"ping","ping",pingFreq,nextRun,enabled))
	cur.execute("insert into parameterInstances (testinstanceId,parameterName,parametervalue) values (%s,%s,%s)",(cur.fetchone()[0],"testbed",map['testbedname']))


################################################################################################################################
###									Parse data
################################################################################################################################
print("Connecting to database")
con = psycopg2.connect(database=dbname,user=user,password=dass,host=durl)
cur = con.cursor()

print("Parsing existing data")
for dir in firstDir:
	print("Dir:", baseDir+dir)
	for file in os.listdir(baseDir+dir):
		if file.endswith(".properties"):

			f = open(baseDir+dir+"/"+file,'r')
			#map with default values
			map = {"testbedname" : (file.split("=")[2]).split(".")[0]}#, "url" : "",
			 #"urn" : "" , "pemKeyAndCertFilename" : "", "userAuthorityUrn" : "", 
			 #"testedAggregateManagerUrn" : "", "pinghost": "", "username" : "", "passwordFilename" : ""}

			for line in f:
				arr = line.split("=");
				if (len(arr) > 1): map[arr[0].strip()] = arr[1].strip()

			if len(map) > 7 and map['testbedname'] not in testbeds : 
				print("adding testbed, ping, list, getVersion test for:",map['testbedname'])
				addTestbed(map,cur)
				addpingTest(map,cur)
				testbeds[map['testbedname']] = map
				pprint.pprint(map)
				
			f.close()
		con.commit() #commit after each file
	
