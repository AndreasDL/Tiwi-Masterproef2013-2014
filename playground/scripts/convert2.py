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

#############################################################################settings#############################################################################
homeDir = "/home/drew/"
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

print("Connecting to database")
con = psycopg2.connect(database=dbname,user=user,password=dass,host=durl)
cur = con.cursor()

###############################################################################testDefinitions###############################################################################

print("creating testDefinitions")
subQuery = "insert into parameterDefinitions (testDefinitionName,parameterName,parameterType,parameterDescription) values (%s,%s,%s,%s);"
retQuery = "insert into returnDefinitions (testDefinitionName,returnName,returnType,returnIndex,returnDescription) values (%s,%s,%s,%s,%s);"
query = "insert into testdefinitions (testDefinitionName,testtype,geniDatastoreTestname,geniDatastoredesc,geniDatastoreUnits,testcommand) values(%s,%s,%s,%s,%s,%s);"

print("\tCreating Ping test")
cur.execute(query,('ping','ping', "", "", "" ,"(fping -q -C 1 <testbed.url> 2>&1) | mawk '{print $3}'"))
cur.execute(subQuery,("ping", "testbed", "testbed", "name of testbed for ping test"))
cur.execute(retQuery,('ping', 'pingValue', 'integer',1, 'ping value'))
con.commit()

print("\tCreating Stitching test")
cur.execute(query,('stitch','stitch', 'ops_monitoring:stitching', 'stichting test between multiple testbeds','boolean', ''))
cur.execute(subQuery,("stitch", "context-file", "file", "username = ftester\n\
	passwordFilename = ftester.pass\n\
	pemKeyAndCertFilename = getsslcert.txt\n\
	userAuthorityUrn = <userAuthorityUrn>\n\
	testedAggregateManagerUrn = <testedAggregateManager.urn>\n\
    stitchedAuthorityUrns= <stitchedAuthorities.urn>\n\
    \
    scsUrl = <scsUrl>"))
cur.execute(subQuery,("stitch", "userAuthorityUrn", "urn", "urn for authority"))
cur.execute(subQuery,("stitch", "testedAggregateManager", "testbed", ""))
cur.execute(subQuery,('stitch', 'stitchedAuthorities', 'testbed[]', 'testbeds to run test on'))
cur.execute(subQuery,("stitch", "scsUrl", "url", "testbed to run test on"))

cur.execute(retQuery,('stitch', 'resultHtml', 'file', 1,'results in html format'))
cur.execute(retQuery,('stitch', 'result-overview', 'file', 2,'results in xml format'))
cur.execute(retQuery,('stitch', 'setUp', 'string',3, 'status of subtest'))
cur.execute(retQuery,('stitch', 'getUserCredential', 'string',4, 'status of subtest'))
cur.execute(retQuery,('stitch', 'generateRspec', 'string',5, 'status of subtest'))
cur.execute(retQuery,('stitch', 'createSlice', 'string',6, 'status of subtest'))
cur.execute(retQuery,('stitch', 'initStitching', 'string',7, 'status of subtest'))
cur.execute(retQuery,('stitch', 'callSCS', 'string',8, 'status of subtest'))
cur.execute(retQuery,('stitch', 'callCreateSlivers', 'string',9, 'status of subtest'))
cur.execute(retQuery,('stitch', 'waitForAllReady', 'string',10, 'status of subtest'))
cur.execute(retQuery,('stitch', 'loginAndPing', 'string',11, 'status of subtest'))
cur.execute(retQuery,('stitch', 'callDeletes', 'string',12, 'status of subtest'))
cur.execute(retQuery,('stitch', 'duration', 'long' ,13, 'duration of test'))
cur.execute(retQuery,('stitch','returnValue', 'int' ,14, 'return value of the automatedTester'))
con.commit()

print("\tCreating Login2 test")
cur.execute(query,('login2','login2', 'ops_monitoring:login2', 'test login amv2','boolean' , '' ))
cur.execute(subQuery,("login2", "context-file", "file", "username = <user.username>\n\
    passwordFilename = <user.passwordfilename>\n\
    pemKeyAndCertFilename = <user.pemkeyandcertfilename>\n\
    userAuthorityUrn = <user.userauthorityurn>\n\
    testedAggregateManagerUrn = <user.urn>\n\
	timoutRetryIntervalMs = 5000\n\
	timoutRetryMaxCount = 20\n\
	busyRetryIntervalMs = 5000\n\
	busyRetryMaxCount = 50"))
cur.execute(subQuery,("login2", "testbed", "testbed", "testbed to run test on"))
cur.execute(subQuery,("login2", "user", "user", "user for authentication"))

cur.execute(retQuery,('login2', 'resultHtml', 'file',1, 'results in html format'))
cur.execute(retQuery,('login2', 'result-overview', 'file',2, 'results in xml format'))
cur.execute(retQuery,('login2', 'setUp', 'string',3, 'setup'))
cur.execute(retQuery,('login2', 'testGetVersionXmlRpcCorrectness', 'string',4, 'testGetVersionXmlRpcCorrectness'))
cur.execute(retQuery,('login2', 'testListResourcesAvailableNoSlice', 'string',5, ''))
cur.execute(retQuery,('login2', 'testCreateSliceSliver', 'string',6, ''))
cur.execute(retQuery,('login2', 'testCreateSliver', 'string',7, ''))
cur.execute(retQuery,('login2', 'testCreatedSliverBecomesReady', 'string',8, ''))
cur.execute(retQuery,('login2', 'checkManifestOnceSliverIsReady', 'string',9, ''))
cur.execute(retQuery,('login2', 'testNodeLogin', 'string',10, 'test node login'))
cur.execute(retQuery,('login2', 'testDeleteSliver', 'string',11, 'test delete sliver'))
cur.execute(retQuery,('login2', 'duration', 'long',12, 'duration of the test in millisecs'))
cur.execute(retQuery,('login2','returnValue', 'int' ,13, 'return value of the automatedTester'))
con.commit()

print("\tCreating Login3 test")
cur.execute(query,('login3','login3', 'ops_monitoring:login3', 'test login amv3','boolean', ''))
cur.execute(subQuery,("login3", "context-file", "file", "username = <user.username>\n\
    passwordFilename = <user.passwordfilename>\n\
    pemKeyAndCertFilename = <user.pemkeyandcertfilename>\n\
    userAuthorityUrn = <user.userauthorityurn>\n\
    testedAggregateManagerUrn = <testbed.urn>\n\
	timoutRetryIntervalMs = 5000\n\
	timoutRetryMaxCount = 20\n\
	busyRetryIntervalMs = 5000\n\
	busyRetryMaxCount = 50"))
cur.execute(subQuery,("login3", "testbed", "testbed", "testbed to run test on"))
cur.execute(subQuery,("login3", "user", "user", "user for authentication"))

cur.execute(retQuery,('login3', 'resultHtml', 'file',1, 'results in html format'))
cur.execute(retQuery,('login3', 'result-overview', 'file',2, 'results in xml format'))
cur.execute(retQuery,('login3', 'setUp', 'string',3, 'setup'))
cur.execute(retQuery,('login3', 'testGetVersionXmlRpcCorrectness', 'string',4, 'testGetVersionXmlRpcCorrectness'))
cur.execute(retQuery,('login3', 'createTestSlices', 'string',5, 'create slices'))
cur.execute(retQuery,('login3', 'testAllocate', 'string',6, 'allocate test'))
cur.execute(retQuery,('login3', 'testProvision', 'string',7, 'testProvision'))
cur.execute(retQuery,('login3', 'testSliverBecomesProvisioned', 'string',8, 'test sliver becomes provisioned'))
cur.execute(retQuery,('login3', 'testPerformOperationalAction', 'string',9, 'test perform operational action'))
cur.execute(retQuery,('login3', 'testSliverBecomesStarted', 'string',10, 'test sliver becomes started'))
cur.execute(retQuery,('login3', 'testNodeLogin', 'string',11, 'test node login'))
cur.execute(retQuery,('login3', 'testDeleteSliver', 'string',12, 'test delete sliver'))
cur.execute(retQuery,('login3', 'duration', 'long',13, 'duration of the test in millisecs'))
cur.execute(retQuery,('login3', 'returnValue', 'int' ,14, 'return value of the automatedTester'))
con.commit()

print("\tCreating getVersion 2 test")
cur.execute(query,('getVersion2','getVersion2', 'ops_monitoring:is_available','Is aggregate manager responsive','boolean',''))
cur.execute(subQuery,("getVersion2", "context-file", "file", "username = <user.username>\n\
    passwordFilename = <user.passwordfilename>\n\
    pemKeyAndCertFilename = <user.pemkeyandcertfilename>\n\
    userAuthorityUrn = <user.userauthorityUrn>\n\
    testedAggregateManagerUrn = <testbed.urn>"))
cur.execute(subQuery,("getVersion2", "testbed", "testbed", "testbed to run test on"))
cur.execute(subQuery,("getVersion2", "user", "user", "user for authentication"))

cur.execute(retQuery,("getVersion2", 'resultHtml', 'file',1, 'results in html format'))
cur.execute(retQuery,("getVersion2", 'result-overview', 'file',2, 'results in xml format'))
cur.execute(retQuery,("getVersion2", 'duration', 'long',3, 'duration of the test in millisecs'))
cur.execute(retQuery,("getVersion2",'returnValue', 'int' ,4, 'return value of the automatedTester'))
cur.execute(retQuery,("getVersion2", 'setUp', 'string',5, 'setup'))
cur.execute(retQuery,("getVersion2", 'testGetVersionXmlRpcCorrectness', 'string',6, 'testGetVersionXmlRpcCorrectness'))
con.commit()

print("\tCreating getVersion3 test")
cur.execute(query,('getVersion3','getVersion3', 'ops_monitoring:is_available', 'Is aggregate manager responsive','boolean', ''))
cur.execute(subQuery,("getVersion3", "context-file", "file", "username = <user.username>\n\
    passwordFilename = <user.passwordfilename>\n\
    pemKeyAndCertFilename = <user.pemkeyandcertfilename>\n\
    userAuthorityUrn = <user.userauthorityUrn>\n\
    testedAggregateManagerUrn = <testbed.urn>"))
cur.execute(subQuery,("getVersion3", "testbed", "urn", "urn for authority"))
cur.execute(subQuery,("getVersion3", "user", "user", "user for authentication"))

cur.execute(retQuery,("getVersion3", 'resultHtml', 'file',1, 'results in html format'))
cur.execute(retQuery,("getVersion3", 'result-overview', 'file',2, 'results in xml format'))
cur.execute(retQuery,("getVersion3", 'duration', 'long',3, 'duration of the test in millisecs'))
cur.execute(retQuery,("getVersion3",'returnValue', 'int' ,4, 'return value of the automatedTester'))
cur.execute(retQuery,("getVersion3", 'setUp', 'string',5, 'setup'))
cur.execute(retQuery,("getVersion3", 'testGetVersionXmlRpcCorrectness', 'string',6, 'testGetVersionXmlRpcCorrectness'))
con.commit()

print("\tCreating listResources test")
cur.execute(query,('listResources','listResources' ,'ops_monitoring:num_vms_allocated','count of free resources','count', '<testbed.urn>'))
cur.execute(subQuery,("listResources", "context-file", "file", "username = <user.username>\n\
    passwordFilename = <user.passwordfilename>\n\
    pemKeyAndCertFilename = <user.pemkeyandcertfilename>\n\
    userAuthorityUrn = <user.userauthorityurn>"))
cur.execute(subQuery,("listResources", "testbed", "testbed", "testbed to get the list recources from"))
cur.execute(subQuery,("listResources", "user", "user", "user for authentication"))

cur.execute(retQuery,("listResources", 'count', 'int',1, 'free resources'))
cur.execute(retQuery,("listResources", 'rspec', 'file',2, 'path of rspec file'))
con.commit()

###############################################################################Parsing###############################################################################

users = {} #mapped on username
testbeds = {} #maped on testbedname
testbedurns = {} #we need also to know which urns are already in the database
tests = {} #keep track of simple tests user => test

addUserQ = "INSERT INTO users (username,userAuthorityUrn,passwordFilename,pemKeyAndCertFilename) VALUES(%s,%s,%s,%s)"
addBedQ  = "INSERT INTO testbeds (testbedname,url,urn) VALUES(%s,%s,%s)"
addTestQ = "INSERT INTO testinstances (testname,testDefinitionName,frequency,nextrun,enabled) VALUES(%s,%s,%s,%s,%s) RETURNING testinstanceid"
addParQ  = "INSERT INTO parameterInstances (testinstanceId,parameterName,parametervalue) VALUES (%s,%s,%s)"

def addUser(map,cur):
	cur.execute(addUserQ,(\
		map['username'],\
		map['userAuthorityUrn'],\
		map['passwordFilename'],\
		map['pemKeyAndCertFilename']\
	))
	users[map['username']] = map
	tests[map['username']] = {"simple" : [], "login" : [], "getVersion" : [] , 'listResources' : [] }

def addTestbed(map,cur):
	cur.execute(addBedQ,
		(map['testbedname'],
		map['pinghost'],
		map['testedAggregateManagerUrn'],
		)) #add testbed
	testbedurns[map['testedAggregateManagerUrn']] = map
	testbeds[map['testbedname']] = map

def addpingTest(map,cur):
	cur.execute(addTestQ,(map['testbedname']+"ping","ping",pingFreq,nextRun,enabled))
	cur.execute(addParQ,(cur.fetchone()[0],"testbed",map['testbedname']))

def addListTest(map,cur):
	cur.execute(addTestQ,(map['testbedname']+"list","listResources",listFreq,nextRun,enabled))
	testinstanceid = cur.fetchone()[0]
	cur.execute(addParQ,(testinstanceid,"testbed",map['testbedname']))	
	cur.execute(addParQ,(testinstanceid,"username",map['username']))

def addGetVersionTest(map,cur):
	if ("amversion" in map and map['amversion']== 3):
		cur.execute(addTestQ,(map['testbedname']+"getVerv3","getVersion3",getVerFreq,nextRun,enabled))
		testinstanceid = cur.fetchone()[0]
		cur.execute(addParQ,(testinstanceid,"testbed",map['testbedname']))	
		cur.execute(addParQ,(testinstanceid,"username",map['username']))
	else :
		cur.execute(addTestQ,(map['testbedname']+"getVerv2","getVersion2",getVerFreq,nextRun,enabled))
		testinstanceid = cur.fetchone()[0]
		cur.execute(addParQ,(testinstanceid,"testbed",map['testbedname']))	
		cur.execute(addParQ,(testinstanceid,"username",map['username']))

def addLoginTest(map,cur):
	if ("amversion" in map and map['amversion']== 3):
		cur.execute(addTestQ,(map['testbedname']+"login3","login3",loginFreq,nextRun,enabled))
		testinstanceid = cur.fetchone()[0]
		cur.execute(addParQ,(testinstanceid,"testbed",map['testbedname']))	
		cur.execute(addParQ,(testinstanceid,"username",map['username']))
	else :
		cur.execute(addTestQ,(map['testbedname']+"login2","login2",loginFreq,nextRun,enabled))
		testinstanceid = cur.fetchone()[0]
		cur.execute(addParQ,(testinstanceid,"testbed",map['testbedname']))	
		cur.execute(addParQ,(testinstanceid,"username",map['username']))

def addStitchingTest(map,cur):
	cur.execute(addTestQ,(map['testname'],"stitch",listFreq,nextRun,enabled))
	testinstanceid = cur.fetchone()[0]
	cur.execute(addParQ,(testinstanceid,"username",map['username']))


################################################################################################################################
#####################								Parse data
################################################################################################################################

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

			if len(map) >= 7:
				if map['testbedname'] not in testbeds : addTestbed(map,cur)
				if map['username'] not in users: addUser(map,cur)
				if map['testbedname'] not in tests[map['username']]["simple"]:
					addpingTest(map,cur)
					addListTest(map,cur)
					addGetVersionTest(map,cur)
					tests[map['username']]["simple"].append(map['testbedname'])
			else :
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

		if len(map) >= 7:
			if map['testbedname'] not in testbeds :	addTestbed(map,cur)
			if map['username'] not in users : addUser(map,cur)
			if map["testbedname"] not in tests[map['username']]["login"] : 
				addLoginTest(map,cur)
				tests[map['username']]["login"].append(map['testbedname'])
		else :
			print("\tAdding testbed & logintest failed for %s" % map['testbedname'])
con.commit()

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
		#pprint.pprint(map)

		if len(map) >= 7:
			addStitchingTest(map,cur)
		elif len(map) < 7 : 
			print("\tAdding testbed & stitchingTest failed for %s" % map['testbedname'])