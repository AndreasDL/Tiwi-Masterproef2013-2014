DROP TABLE testbeds;
CREATE TABLE testbeds (
    testbedId character varying NOT NULL PRIMARY KEY,
    name character varying NOT NULL
);

DROP TABLE testDefinitions;
CREATE TABLE testDefinitions(
    testtype character varying NOT NULL PRIMARY KEY,
    testCommand character varying NOT NULL
);
DROP TABLE parameterDefinitions;
CREATE TABLE parameterDefinitions(
    testType character varying NOT NULL,
    parameterName text NOT NULL,
    parameterType text NOT NULL,
    parameterDescription text
);
DROP TABLE returnDefinitions;
CREATE TABLE returnDefinitions(
    testType character varying NOT NULL,
    returnName text NOT NULL,
    returnType text NOT NULL,
    returnDescription text
);

DROP TABLE testInstances;
CREATE TABLE testInstances(
    testinstanceId serial PRIMARY KEY,
    testname character varying ,
    testtype character varying NOT NULL,
    frequency integer
);
DROP TABLE parameterInstances;
CREATE TABLE parameterInstances(
    testinstanceId integer NOT NULL,
    parameterName text NOT NULL,
    parameterValue text NOT NULL
);

DROP TABLE results;
CREATE TABLE results(
    resultId serial NOT NULL PRIMARY KEY,
    testInstanceId integer NOT NULL,
    log character varying NOT NULL,
    timestamp timestamp default current_timestamp
);
DROP TABLE subResults;
CREATE TABLE subResults(
    resultId integer NOT NULL,
    name text NOT NULL,
    value text NOT NULL,
    primary key (resultId,name)
);
ALTER TABLE public.testbeds OWNER TO postgres;

ALTER TABLE public.testDefinitions OWNER TO postgres;
ALTER TABLE public.parameterDefinitions OWNER TO postgres;
ALTER TABLE public.returnDefinitions OWNER TO postgres;

ALTER TABLE public.testInstances OWNER TO postgres;
ALTER TABLE public.parameterInstances OWNER TO postgres;

ALTER TABLE public.results OWNER TO postgres;
ALTER TABLE public.subResults OWNER TO postgres;
