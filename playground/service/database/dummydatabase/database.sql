DROP VIEW list;
DROP VIEW definitions;
DROP VIEW instances;

DROP TABLE testbeds;
DROP TABLE parameterDefinitions;
DROP TABLE returnDefinitions;
DROP TABLE parameterInstances;
DROP TABLE subResults;
DROP TABLE results;
DROP TABLE testInstances;
DROP TABLE testDefinitions;

--table with all parameters and data about a testbed
CREATE TABLE testbeds (
    testbedName character varying NOT NULL PRIMARY KEY, 
    url character varying,
    urn character varying --UNIQUE
);

--the testdefinitions
--defines what a test looks like
CREATE TABLE testDefinitions(
    testtype character varying NOT NULL, --the internal type user in the jar.
    testDefinitionName character varying NOT NULL PRIMARY KEY, --the definitionname of the test, referred to by the instances
    testCommand character varying NOT NULL, --the command that the testdefinition should execute.
-- this could be more than only a command e.g. in an automated tester this could also be a list of parameters. strings between <> are viewed as an argument.
-- e.g. <testbed.urn> will look in the given parameters get the parameter with the name testbed then get it's urn.
-- e.g. <randomstring> will get the value of a parameter with the name randomstring
    geniDatastoreTestname character varying, --the name used for compability with the geni datastore
    geniDatastoreDesc character varying, --description used for compability with the geni datastore
    geniDatastoreUnits character varying --units used for compability with the geni datastore
);
--the parameterdefinitions
CREATE TABLE parameterDefinitions(
    testDefinitionName character varying NOT NULL references testDefinitions(testDefinitionName),--the name of the definition
    parameterName text NOT NULL,--the name of the parameter
    parameterType text NOT NULL,--the type of the parameter
    parameterDescription text--description of the parameter
);
--the returndefinitions
CREATE TABLE returnDefinitions(
    testDefinitionName character varying NOT NULL references testDefinitions(testDefinitionName),--the name of the definition
    returnName text NOT NULL,--the name of the returnvalue
    returnType text NOT NULL,--the type of the returnvalue
    returnDescription text,--the desciption of the returnDescription
    returnIndex integer--the index used for sorting
);

--the testinstances
--an instance of a testdefinition
CREATE TABLE testInstances(
    testinstanceId serial PRIMARY KEY,--id of the testinstance
    testname character varying UNIQUE,--name of the testinstance
    testDefinitionName character varying NOT NULL references testDefinitions(testDefinitionName),--the name of the definition
    enabled boolean NOT NULL default TRUE,--whether or not the test is enabled
    frequency integer,--the frequency of the test in seconds
    nextRun timestamp with time zone--the time when the test should run
);
--the parametervalues
CREATE TABLE parameterInstances(
    testinstanceId integer NOT NULL references testInstances(testinstanceId),--the id of the testinstance
    parameterName text NOT NULL, --the name of the parameter
    parameterValue text NOT NULL --the value of the parameter
);

--the results
CREATE TABLE results(
    resultId serial NOT NULL PRIMARY KEY,--the id of the result
    testInstanceId integer NOT NULL references testInstances(testinstanceId),--the testinstance of the result
    log character varying NOT NULL,--path of the log file
    timestamp timestamp with time zone default current_timestamp --the timestamp of the result
);
--the subresults
CREATE TABLE subResults(
    resultId integer NOT NULL references results(resultId),--id of the result to link to.
    returnName text NOT NULL, --name of the subresult
    returnValue text NOT NULL, --value of the subresult
    primary key (resultId,returnName)
);

--views
--list => easy access to results used primary by the /list call
CREATE VIEW list AS 
    select *,r.resultid id from results r
        join subresults sr using (resultid)
        join testinstances ti using (testinstanceid)
        join testdefinitions td using (testDefinitionName)
        join parameterinstances pi using (testinstanceid)
        join returnDefinitions rd using (testdefinitionname,returnname)
;
--definitions => easy access to the testdefinitions
CREATE VIEW definitions AS
    select *,t.testtype tetyp from testdefinitions t 
        join parameterdefinitions p using (testDefinitionName)
        join returndefinitions    r using (testDefinitionName)
;
--instances => easy access to the testinstances
CREATE VIEW instances AS
    select t.testinstanceid as id,* from testinstances t
        left join parameterInstances p using(testinstanceid)
;

ALTER TABLE public.testbeds OWNER TO postgres;
ALTER TABLE public.testDefinitions OWNER TO postgres;
ALTER TABLE public.parameterDefinitions OWNER TO postgres;
ALTER TABLE public.returnDefinitions OWNER TO postgres;
ALTER TABLE public.testInstances OWNER TO postgres;
ALTER TABLE public.parameterInstances OWNER TO postgres;
ALTER TABLE public.results OWNER TO postgres;
ALTER TABLE public.subResults OWNER TO postgres;

ALTER TABLE public.list OWNER TO postgres;
ALTER TABLE public.definitions OWNER TO postgres;
ALTER TABLE public.instances OWNER TO postgres;