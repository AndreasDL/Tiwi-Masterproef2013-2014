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

CREATE TABLE testbeds (
    testbedName character varying NOT NULL PRIMARY KEY, 
    url character varying,
    urn character varying --UNIQUE
);

CREATE TABLE testDefinitions(
    testtype character varying NOT NULL,-- PRIMARY KEY,
    testDefinitionName character varying NOT NULL PRIMARY KEY,
    testCommand character varying NOT NULL,
    geniDatastoreTestname character varying
);
CREATE TABLE parameterDefinitions(
    testDefinitionName character varying NOT NULL references testDefinitions(testDefinitionName),
    parameterName text NOT NULL,
    parameterType text NOT NULL,
    parameterDescription text
);
CREATE TABLE returnDefinitions(
    testDefinitionName character varying NOT NULL references testDefinitions(testDefinitionName),
    returnName text NOT NULL,
    returnType text NOT NULL,
    returnDescription text
);

CREATE TABLE testInstances(
    testinstanceId serial PRIMARY KEY,
    testname character varying UNIQUE,
    testDefinitionName character varying NOT NULL references testDefinitions(testDefinitionName),
    enabled boolean NOT NULL default TRUE,
    frequency integer,
    nextRun timestamp with time zone
);
CREATE TABLE parameterInstances(
    testinstanceId integer NOT NULL references testInstances(testinstanceId),
    parameterName text NOT NULL,-- references parameterDefinitions(parameterName),
    parameterValue text NOT NULL
);

CREATE TABLE results(
    resultId serial NOT NULL PRIMARY KEY,
    testInstanceId integer NOT NULL references testInstances(testinstanceId),
    log character varying NOT NULL,
    timestamp timestamp with time zone default current_timestamp
);
CREATE TABLE subResults(
    resultId integer NOT NULL references results(resultId),
    returnName text NOT NULL, --references returnDefinitions(returnName),
    returnValue text NOT NULL,
    primary key (resultId,returnName)
);


CREATE VIEW list AS 
    select *,r.resultid id from results r
        join subresults sr using (resultid)
        join testinstances ti using (testinstanceid)
        join parameterinstances pi using (testinstanceid)
;
CREATE VIEW definitions AS
    select *,t.testtype tetyp from testdefinitions t 
        join parameterdefinitions p using (testDefinitionName) --using(testtype)
        join returndefinitions    r using (testDefinitionName) --using(testtype)
;
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