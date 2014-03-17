DROP TABLE testbeds;
CREATE TABLE testbeds (
	testbedId text NOT NULL PRIMARY KEY,
	name text NOT NULL
);

DROP TABLE testDefinitions;
CREATE TABLE testDefinitions(
	testtype text NOT NULL PRIMARY KEY,
	testCommand text NOT NULL,
	parameters text,
	return text
);

DROP TABLE testInstances;
CREATE TABLE testInstances(
	testinstanceId serial PRIMARY KEY,
	testName text,
        testType text,
	frequency integer,
	parameters text[]
);

DROP TABLE results;
CREATE TABLE results(
    resultId serial PRIMARY KEY,
    testInstanceId integer NOT NULL,
    results text[] NOT NULL,
    log text NOT NULL,
    timestamp timestamp default current_timestamp
);

ALTER TABLE public.testbeds OWNER TO postgres;
ALTER TABLE public.testDefinitions OWNER TO postgres;
ALTER TABLE public.testInstances OWNER TO postgres;
ALTER TABLE public.results OWNER TO postgres;