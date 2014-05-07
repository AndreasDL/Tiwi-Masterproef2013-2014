import re
import pprint #debug
import os
import psycopg2

#settings
baseDir = "/home/drew/masterproef/f4ftestsuite/trunk/monitor_site/";
dbname = "testdb"
user = "postgres"
dass = "post"
durl = "localhost"
pingFreq = 300
listFreq = 3600
nextRun = "2014-5-7T12:00:00"
enabled = False


"""
echo "Creating TestInstances\n";
$query = "insert into testinstances (testname,testDefinitionName,frequency,nextrun,enabled) values ($1,$2,$3,$4,$5);";
pg_prepare($con, "query", $query);
$subQuery = "insert into parameterInstances (testinstanceId,parameterName,parametervalue) values (lastval(),$1,$2)";
pg_prepare($con, "subQuery", $subQuery);
"""
def addPingIfNeeded(testbed,cur):
	if " pinglatency" in testbed["tests"]:
		print("\t\t\tPing test Added")
		cur.execute("insert into testinstances (testname,testDefinitionName,frequency,nextrun,enabled) values (%s,%s,%s,%s,%s) RETURNING testinstanceid;",(testbed['testbedname']."ping","ping",pingFreq,nextRun,enabled))
		cur.execute("insert into parameterInstances (testinstanceId,parameterName,parametervalue) values (%s,%s,%s)",(cur.fetchone()[0],"testbed",testbed['testbedname']))
"""
def addGetVersionIfNeeded(testbed,cur):
	if " pinglatency" in testbed["tests"]:
		print("\t\t\tGetVersion test Added")
		cur.execute("insert into testinstances (testname,testDefinitionName,frequency,nextrun,enabled) values (%s,%s,%s,%s,%s) RETURNING testinstanceid;",(testbed['testbedname'],"ping",pingFreq,nextRun,enabled))
		cur.execute("insert into parameterInstances (testinstanceId,parameterName,parametervalue) values (%s,%s,%s)",(cur.fetchone()[0],"testbed",testbed['testbedname']))
"""
def addListResourcesIfNeeded(testbed,cur):
	if " freeresources" in testbed["tests"]:	
		print("\t\t\tListResources test Added")
		cur.execute("insert into testinstances (testname,testDefinitionName,frequency,nextrun,enabled) values (%s,%s,%s,%s,%s) RETURNING testinstanceid;",(testbed['testbedname']."list","listResources",listFreq,nextRun,enabled))
		cur.execute("insert into parameterInstances (testinstanceId,parameterName,parametervalue) values (%s,%s,%s)",(cur.fetchone()[0],"testbed",testbed['testbedname']))



################################################################################################################################
###									Parse data
################################################################################################################################
print("Parsing existing data")

#create datastruct
testbeds = {} #testbedname => testbed
contexts = {} #testid => context
results = {}  #resultid => result
prob = {}     #problemtype => list of problems

for file in os.listdir(baseDir):
	if (file.endswith(".sql")):
		
		print("opening",file);
		f = open(baseDir+file,'r')
		
		#drop useless first lines
		header = ""
		if file.startswith("db_dump_flsmonitoring"): 
#testbedid, testbedname, testbedurl, pinglatency, getversionstatus, freeresources, aggregatetestbedstate, last_check
			for line in f:
				if line.find("COPY flstestbeds") != -1: 
					header = line.split(",")
					#pprint.pprint(header);
					break;

			for line in f:
				if line.find("\\.") != -1 : break; # we don't need the tail
				
				lst = line.split("\t")
				if lst[1] not in testbeds: 
					testbeds[lst[1]] = {}
					testbeds[lst[1]]['tests'] = {}
				testbeds[lst[1]]['testbedid'] = lst[0]
				testbeds[lst[1]]['testbedname'] = lst[1]
				testbeds[lst[1]]['testbedurl'] = lst[2]
				for test in header[3:6]:
					testbeds[lst[1]]['tests'][test] = True

		else:
			#test_context
#id, name, category, description, contextfilename, testbed_urn
			for line in f:#remove stuffs above table content
				if line.find("COPY test_context") != -1 :
					header = line.split("(")
					header = header[1].split(")")
					header = header[0:-1]
					header = header[0].split(",")
					#pprint.pprint(header);
					break;

			for line in f:
				if line.find("\\.") != -1 :break;

				lst = line.split("\t")
				con = {}
				for i in range(len(lst)):
					con[header[i]] = lst[i]
				contexts[lst[1]] = con

			#results
			for line in f:#remove stuffs above table content
				if line.find("COPY test_results") != -1:
					header = line.split("(")
					header = header[1].split(")")
					header = header[0:-1]
					header = header[0].split(",")
					#pprint.pprint(header);
					break;

			for line in f:
				if line.find("\\.") != -1: break;

				lst = line.split("\t")
				res = {}
				for i in range(len(lst)):
					res[header[i]] = lst[i]
				results[lst[0]] = res



			#testbeds
#id, name, fed4fire, international, description, type, urn, interface_url, info-_url
			for line in f:
				if line.find("COPY testbeds") != -1: 
					header = line.split(",")
					#pprint.pprint(header);
					break

			for line in f:
				if line.find("\\.") != -1: break;#we don't need the tail

				lst = line.split("\t");
				if lst[1] not in testbeds: 
					testbeds[lst[1]] = {};
					testbeds[lst[1]]['tests'] = {}
				testbeds[lst[1]]['testbedid'] = lst[0]
				testbeds[lst[1]]['testbedname'] = lst[1]
				testbeds[lst[1]]['testbedurn'] = lst[6]
				for test in header[3:6]:
					testbeds[lst[1]]['tests']['scenarios'] = True


		f.close()


print("\tFound", len(testbeds), "testbeds")
print("\tFound", len(contexts), "test contexts")
print("\tFound", len(results), "results")
pprint.pprint(testbeds);
#pprint.pprint(contexts);
#pprint.pprint(results);
################################################################################################################################
###										Convert & insert in new system
################################################################################################################################
print("Connecting to database")
con = psycopg2.connect(database=dbname,user=user,password=dass,host=durl)


print("Converting & storage")

#testbeds
print("\tAdding testbeds")
prob["missingUrn"]  = []
prob["missingUrl"]  = []
prob["missingBoth"] = []
cur = con.cursor()
for testbedname in testbeds:
	testbed = testbeds[testbedname]
	print("\t\tAdd testbed", testbedname)
	if ("testbedurn" in testbed and "testbedurl" in testbed): #both
		cur.execute("INSERT INTO testbeds (testbedname,url,urn) VALUES(%s,%s,%s)",(testbed['testbedname'],testbed['testbedurl'],testbed['testbedurn'])) #add testbed
	elif ("testbedurl" in testbed): #url but no urn
		cur.execute("INSERT INTO testbeds (testbedname,url,urn) VALUES(%s,%s,%s)",(testbed['testbedname'],testbed['testbedurl'],"")) #add testbed
		prob['missingUrn'].append(testbedname)
	elif ("testbedurn" in testbed) : #urn but no url
		cur.execute("INSERT INTO testbeds (testbedname,url,urn) VALUES(%s,%s,%s)",(testbed['testbedname'],"",testbed['testbedurn'])) #add testbed
		prob['missingUrl'].append(testbedname)
	else : #none
		cur.execute("INSERT INTO testbeds (testbedname,url,urn) VALUES(%s,%s,%s)",(testbed['testbedname'],"","")) #add testbed
		prob['missingBoth'].append(testbedname)
	addPingIfNeeded(testbed,cur)
con.commit()