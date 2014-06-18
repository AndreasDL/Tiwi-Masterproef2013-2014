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
cur.execute(subQuery,("stitch", "context-file", "file", "username = <user.username>\n\
	passwordFilename = <user.passwordfilename>\n\
	pemKeyAndCertFilename = <user.pemkeyandcertfilename>\n\
	userAuthorityUrn = <user.userauthorityurn>\n\
	testedAggregateManagerUrn = <testedAggregateManager.urn>\n\
    stitchedAuthorityUrns= <stitchedAuthorities.urn>\n\
    \
    scsUrl = <scsUrl>"))
cur.execute(subQuery,("stitch", "user", "user", "user for authority"))
cur.execute(subQuery,("stitch", "testedAggregateManager", "testbed", ""))
cur.execute(subQuery,('stitch', 'stitchedAuthorities', 'testbed[]', 'testbeds to run test on'))
cur.execute(subQuery,("stitch", "scsUrl", "url", "testbed to run test on"))

cur.execute(retQuery,('stitch', 'resultHtml', 'file', 1,'results in html format'))
cur.execute(retQuery,('stitch', 'result-overview', 'file', 2,'results in xml format'))
cur.execute(retQuery,('stitch', 'setUp', 'string',3, 'status of subtest'))
cur.execute(retQuery,('stitch', 'getUserCredentials', 'string',4, 'status of subtest'))
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
    testedAggregateManagerUrn = <testbed.urn>\n\
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
    userAuthorityUrn = <user.userauthorityurn>\n\
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
    userAuthorityUrn = <user.userauthorityurn>\n\
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