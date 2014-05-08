"""
this script is a work of art created tested developped & design by Andreas De Lille.
Please treat accordingly & handle with care. 
Enjoy!
"""
import re
import pprint #debug
import os
import psycopg2
import sys

#settings
baseDir = "/home/drew/masterproef/f4ftestsuite/trunk/monitor_site/work/monitoring/contexts/"
certDir = "/home/drew/masterproef/scripts/overzetten/" #where to find all the certificates
firstDir = ['fls','international'] #dirs for ping, getVersion & list resources
loginDir = 'login_scenarios'
stitchingDir = 'stitching_scenarios'

dbname = "testdb"
user = "postgres"
dass = "post"
durl = "localhost"
pingFreq   = 300
listFreq   = 900
getVerFreq = 900
loginFreq  = 3600
stitchFreq = 86400
nextRun = "2014-1-7T12:00:00"
enabled = True


testbeds = {} #maped on testbedname
testbedurns = {} #we need also to know which urns are already in the database

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
	testbedurns[map['testedAggregateManagerUrn']] = True

def addpingTest(map,cur):
	cur.execute("INSERT INTO testinstances (testname,testDefinitionName,frequency,nextrun,enabled) VALUES(%s,%s,%s,%s,%s) RETURNING testinstanceid",(map['testbedname']+"ping","ping",pingFreq,nextRun,enabled))
	cur.execute("INSERT INTO parameterInstances (testinstanceId,parameterName,parametervalue) VALUES (%s,%s,%s)",(cur.fetchone()[0],"testbed",map['testbedname']))

def addListTest(map,cur):
	cur.execute("INSERT INTO testinstances (testname,testDefinitionName,frequency,nextrun,enabled) VALUES(%s,%s,%s,%s,%s) RETURNING testinstanceid",(map['testbedname']+"list","listResources",listFreq,nextRun,enabled))
	cur.execute("INSERT INTO parameterInstances (testinstanceId,parameterName,parametervalue) VALUES (%s,%s,%s)",(cur.fetchone()[0],"testbed",map['testbedname']))	

def addGetVersionTest(map,cur):
	if ("amversion" in map and map['amversion']== 3):
		cur.execute("INSERT INTO testinstances (testname,testDefinitionName,frequency,nextrun,enabled) VALUES(%s,%s,%s,%s,%s) RETURNING testinstanceid",(map['testbedname']+"getVerv3","getVersion3",getVerFreq,nextRun,enabled))
		cur.execute("INSERT INTO parameterInstances (testinstanceId,parameterName,parametervalue) VALUES (%s,%s,%s)",(cur.fetchone()[0],"testbed",map['testbedname']))	
	else :
		cur.execute("INSERT INTO testinstances (testname,testDefinitionName,frequency,nextrun,enabled) VALUES(%s,%s,%s,%s,%s) RETURNING testinstanceid",(map['testbedname']+"getVerv2","getVersion2",getVerFreq,nextRun,enabled))
		cur.execute("INSERT INTO parameterInstances (testinstanceId,parameterName,parametervalue) VALUES (%s,%s,%s)",(cur.fetchone()[0],"testbed",map['testbedname']))	

def addLoginTest(map,cur):
	if ("amversion" in map and map['amversion']== 3):
		cur.execute("INSERT INTO testinstances (testname,testDefinitionName,frequency,nextrun,enabled) VALUES(%s,%s,%s,%s,%s) RETURNING testinstanceid",(map['testbedname']+"login3","login3",loginFreq,nextRun,enabled))
		cur.execute("INSERT INTO parameterInstances (testinstanceId,parameterName,parametervalue) VALUES(%s,%s,%s)",(cur.fetchone()[0],"testbed",map['testbedname']))	
	else :
		cur.execute("INSERT INTO testinstances (testname,testDefinitionName,frequency,nextrun,enabled) VALUES(%s,%s,%s,%s,%s) RETURNING testinstanceid",(map['testbedname']+"login2","login2",loginFreq,nextRun,enabled))
		cur.execute("INSERT INTO parameterInstances (testinstanceId,parameterName,parametervalue) VALUES(%s,%s,%s)",(cur.fetchone()[0],"testbed",map['testbedname']))

def addStitchingTest(map,cur):
	cur.execute("INSERT INTO testinstances (testname,testDefinitionName,frequency,nextrun,enabled) VALUES(%s,%s,%s,%s,%s) RETURNING testinstanceid",(map['testname'],"stitch",listFreq,nextRun,enabled))
	testinstanceid = cur.fetchone()[0]
	cur.execute("INSERT INTO parameterInstances (testinstanceId,parameterName,parametervalue) VALUES(%s,%s,%s)",(testinstanceid,"scsUrl",map['testbedname']))

################################################################################################################################
###									Parse data
################################################################################################################################
print("Connecting to database")
con = psycopg2.connect(database=dbname,user=user,password=dass,host=durl)
cur = con.cursor()

print("Parsing existing data")
print("\tParsing fls monitoring & flsmonitoring_international")
for dir in firstDir:
	print("Dir:", dir)

	for file in os.listdir(baseDir+dir):
		if file.endswith(".properties"):
			f = open(baseDir+dir+"/"+file,'r')
			map = {"testbedname" : (file.split("=")[2]).split(".")[0]}
			for line in f:
				arr = line.split("=");
				if (len(arr) > 1): map[arr[0].strip()] = arr[1].strip()

			if len(map) >= 7 and map['testbedname'] not in testbeds : 
				addTestbed(map,cur)
				addpingTest(map,cur)
				addListTest(map,cur)
				addGetVersionTest(map,cur)
				testbeds[map['testbedname']] = map #save so future same tests can be ignored
			elif len(map) < 7:
				print("\tAdding testbed & ping & list & getVersion failed for %s" % map['testbedname'])
			f.close()
		con.commit() #commit after each file

print("\tParsing login tests")
print("Dir:", loginDir)
for file in os.listdir(baseDir+loginDir):
	if file.endswith(".properties"):
		f = open(baseDir+loginDir+'/'+file,'r')
		map = {"testbedname" : (file.split("=")[2]).split(".")[0]}
		for line in f:
			arr = line.split("=");
			if (len(arr) > 1): map[arr[0].strip()] = arr[1].strip()

		if len(map) >= 7 and map['testbedname'] not in testbeds :
			addTestbed(map,cur)
			addLoginTest(map,cur)
		elif len(map) < 7 : 
			print("\tAdding testbed & logintest failed for %s" % map['testbedname'])
		

print("\tParing Stitching tests")
print("Dir:", stitchingDir)
for file in os.listdir(baseDir+stitchingDir):
	if file.endswith(".properties"):
		f = open(baseDir+stitchingDir+'/'+file,'r')

		map = {}
		for line in f:
			arr = line.split("=");
			if (len(arr) > 1): map[arr[0].strip()] = arr[1].strip()
		map['stitchedAuthorityUrns'] = map['stitchedAuthorityUrns'].split()
		for i in range(len(map['stitchedAuthorityUrns'])): 
			map['stitchedAuthorityUrns'][i] = map['stitchedAuthorityUrns'][i].strip()
		pprint.pprint(map)

		if len(map) >= 7:
			#addTestbed(map,cur)
			addStitchingTest(map,cur)
		elif len(map) < 7 : 
			print("\tAdding testbed & stitchingTest failed for %s" % map['testbedname'])