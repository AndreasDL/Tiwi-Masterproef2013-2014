import psycopg2

dbname = "testdb"
user = "postgres"
dass = "post"
durl = "localhost"
pingFreq   = 300
listFreq   = 900
getVerFreq = 900
loginFreq  = 3600
stitchFreq = 43200
nextRun = "2014-1-7T12:00:00"
enabled = True

if (len(sys.argv) > 1):
	dbname = str(sys.argv[1])

addUserQ = "INSERT INTO users (username,userAuthorityUrn,passwordFilename,pemKeyAndCertFilename) VALUES(%s,%s,%s,%s)"
addBedQ  = "INSERT INTO testbeds (testbedname,url,urn) VALUES(%s,%s,%s)"
addTestQ = "INSERT INTO testinstances (testname,testDefinitionName,frequency,nextrun,enabled) VALUES(%s,%s,%s,%s,%s) RETURNING testinstanceid"
addParQ  = "INSERT INTO parameterInstances (testinstanceId,parameterName,parametervalue) VALUES (%s,%s,%s)"
addResQ  = "INSERT INTO results (testinstanceid,log,timestamp) VALUES(%s,%s,%s) RETURNING resultid"
addSubResQ = "INSERT INTO subresults (resultid,returnname,returnvalue) VALUES(%s,%s,%s)"
certDir  = "/home/drew/.ssl/"

def addUser(cur,username,urn,passF,cert):
	cur.execute(addUserQ,(\
		username,\
		urn,\
		certDir+passF,\
		certDir+cert))
	con.commit()

def addTestbed(cur,name,urn,url):
	cur.execute(addBedQ,(name,url,urn))
	con.commit()

def addPingTest(cur,testbed):
	cur.execute(addTestQ,(testbed+"ping","ping",pingFreq,nextRun,enabled))
	cur.execute(addParQ,(cur.fetchone()[0],"testbed",testbed))
	con.commit()

def addGetVersion2Test(cur,testbed,user="ftester"):
	cur.execute(addTestQ,(testbed+"getVerv2","getVersion2",getVerFreq,nextRun,enabled))
	testinstanceid = cur.fetchone()[0]
	cur.execute(addParQ,(testinstanceid,"testbed",testbed))	
	cur.execute(addParQ,(testinstanceid,"user",user))
	con.commit()

def addListTest(cur,testbed,user="ftester"):
	cur.execute(addTestQ,(testbed+"list","listResources",listFreq,nextRun,enabled))
	testinstanceid = cur.fetchone()[0]
	cur.execute(addParQ,(testinstanceid,"testbed",testbed))	
	cur.execute(addParQ,(testinstanceid,"user",user))
	con.commit()

def addLogin2Test(cur,testbed,user="ftester"):
	cur.execute(addTestQ,(testbed+"login2","login2",loginFreq,nextRun,enabled))
	testinstanceid = cur.fetchone()[0]
	cur.execute(addParQ,(testinstanceid,"testbed",testbed))
	cur.execute(addParQ,(testinstanceid,"user",user))
	con.commit()

def addStitchingTest(cur,name,testbed1,testbed2,user="ftester",scs = "http://geni.maxgigapop.net:8081/geni/xmlrpc"):
	#werkt voor wall1wall2 niet zeker over andere
	cur.execute(addTestQ,(name,"stitch",stitchFreq,nextRun,enabled))
	testinstanceid = cur.fetchone()[0]
	cur.execute(addParQ,(testinstanceid,"user",user))
	cur.execute(addParQ,(testinstanceid,'stitchedAuthorities',testbed1))
	cur.execute(addParQ,(testinstanceid,'stitchedAuthorities',testbed2))
	#scs = "http://geni.maxgigapop.net:8081/geni/xmlrpc"
	cur.execute(addParQ,(testinstanceid,'scsUrl',scs))
	cur.execute(addParQ,(testinstanceid,'testedAggregateManager',testbed1))
	con.commit()

######################################Connection
print("Connecting to database")
con = psycopg2.connect(database=dbname,user=user,password=dass,host=durl)
cur = con.cursor()

######################################Users
addUser(cur,"ftester","urn:publicid:IDN+wall2.ilabt.iminds.be+authority+cm","pass","cert.pem")

######################################Testbed
addTestbed(cur,"wall1","urn:publicid:IDN+wall1.ilabt.iminds.be+authority+cm","www.wall1.ilabt.iminds.be")
addTestbed(cur,"wall2","urn:publicid:IDN+wall2.ilabt.iminds.be+authority+cm","www.wall2.ilabt.iminds.be")
addTestbed(cur,"wilab","urn:publicid:IDN+wilab2.ilabt.iminds.be+authority+cm","www.wilab2.ilabt.iminds.be")
addTestbed(cur,"fail","urn:publicid:IDN+omf+authority+sa","google.Com")

######################################testinstances
for testbed in ("wall1","wall2","fail","wilab"):
	addPingTest(cur,testbed)
	addGetVersion2Test(cur,testbed)
	addListTest(cur,testbed)
	addLogin2Test(cur,testbed)
addStitchingTest(cur,"wall1wall2","wall2","wall1")