DROP VIEW list;
DROP VIEW definitions;
DROP VIEW instances;

DROP TABLE testbeds;
DROP TABLE testDefinitions;
DROP TABLE parameterDefinitions;
DROP TABLE returnDefinitions;
DROP TABLE testInstances;
DROP TABLE parameterInstances;
DROP TABLE results;
DROP TABLE subResults;

CREATE TABLE testbeds (
    testbedId character varying NOT NULL PRIMARY KEY,
    name character varying NOT NULL,
    url  character varying
);

CREATE TABLE testDefinitions(
    testtype character varying NOT NULL PRIMARY KEY,
    testCommand character varying NOT NULL
);
CREATE TABLE parameterDefinitions(
    testType character varying NOT NULL,
    parameterName text NOT NULL,
    parameterType text NOT NULL,
    parameterDescription text
);
CREATE TABLE returnDefinitions(
    testType character varying NOT NULL,
    returnName text NOT NULL,
    returnType text NOT NULL,
    returnDescription text
);

CREATE TABLE testInstances(
    testinstanceId serial PRIMARY KEY,
    testname character varying ,
    testtype character varying NOT NULL,
    frequency integer
);
CREATE TABLE parameterInstances(
    testinstanceId integer NOT NULL,
    parameterName text NOT NULL,
    parameterValue text NOT NULL
);

CREATE TABLE results(
    resultId serial NOT NULL PRIMARY KEY,
    testInstanceId integer NOT NULL,
    log character varying NOT NULL,
    timestamp timestamp default current_timestamp
);
CREATE TABLE subResults(
    resultId integer NOT NULL,
    name text NOT NULL,
    value text NOT NULL,
    primary key (resultId,name)
);

CREATE VIEW list AS 
    select *,r.resultid id from results r
        join subresults sr using (resultid)
        join testinstances ti using (testinstanceid)
        join parameterinstances pi using (testinstanceid)
;
CREATE VIEW definitions AS
    select *,t.testtype tetyp from testdefinitions t 
        join parameterdefinitions p using(testtype)
        join returndefinitions    r using(testtype)
;
CREATE VIEW instances AS
    select t.testinstanceid as id,* from testinstances t
        join parameterInstances p using(testinstanceid)
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