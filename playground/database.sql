DROP TABLE testbeds;
CREATE TABLE testbeds (
	testbedId character varying NOT NULL PRIMARY KEY,
	name character varying NOT NULL
);

DROP TABLE testDefinitions;
CREATE TABLE testDefinitions(
	testName character varying NOT NULL PRIMARY KEY,
	testCommand character varying NOT NULL,
	parameters character varying,
	return character varying
);

DROP TABLE testInstances;
CREATE TABLE testInstances(
	testinstanceId serial PRIMARY KEY,
	testName character varying,
	frequency integer,
	parameters character varying
);

DROP TABLE results;
CREATE TABLE results(
	resultId serial PRIMARY KEY,
	testInstance integer NOT NULL,
	results character varying NOT NULL,
	log character varying NOT NULL,
	timestamp timestamp default current_timestamp
);

ALTER TABLE public.testbeds OWNER TO postgres;
ALTER TABLE public.testDefinitions OWNER TO postgres;
ALTER TABLE public.testInstances OWNER TO postgres;
ALTER TABLE public.results OWNER TO postgres;