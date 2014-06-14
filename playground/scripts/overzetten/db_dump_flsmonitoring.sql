--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: dblink; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS dblink WITH SCHEMA public;


--
-- Name: EXTENSION dblink; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION dblink IS 'connect to other PostgreSQL databases from within a database';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: flstestbeds; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE flstestbeds (
    testbedid integer,
    testbedname character varying NOT NULL,
    testbedurl character varying,
    pinglatency double precision,
    getversionstatus integer,
    freeresources bigint DEFAULT 0,
    aggregatetestbedstate integer,
    last_check timestamp with time zone
);


ALTER TABLE public.flstestbeds OWNER TO postgres;

--
-- Data for Name: flstestbeds; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY flstestbeds (testbedid, testbedname, testbedurl, pinglatency, getversionstatus, freeresources, aggregatetestbedstate, last_check) FROM stdin;
15	Ofelia (Bristol vtam)	https://alpha.fp7-ofelia.eu/doc/index.php/Testbed_Bristol	11.2200000000000006	0	6	0	2014-05-13 10:55:02+00
7	NITOS Broker	http://nitlab.inf.uth.gr/NITlab/index.php/testbed	72.2800000000000011	0	38	0	2014-05-13 11:00:02+00
12	NITOS SFAWrap	http://nitlab.inf.uth.gr/NITlab/index.php/testbed	30.6099999999999994	0	112	0	2014-05-13 11:00:02+00
3	Planetlab Europe	http://www.planet-lab.eu	30.6099999999999994	0	283	0	2014-05-13 11:00:02+00
17	Virtual Wall 1	https://www.wall1.ilabt.iminds.be	0.110000000000000001	0	13	-1	\N
13	Koren	http://www.fed4fire.eu/testbeds-and-tools/koren.html	292.949999999999989	0	2	-1	\N
9	Norbit	http://omf.mytestbed.net/projects/omf/wiki/DeploymentSite	-2	-1	-2	0	2014-05-03 08:01:18+00
11	Ofelia (Bristol openflow)	https://alpha.fp7-ofelia.eu/doc/index.php/Testbed_Bristol	11.1600000000000001	0	40	0	2014-05-13 10:55:02+00
10	Ofelia (i2CAT openflow)	https://alpha.fp7-ofelia.eu/doc/index.php/Testbed_Barcelona	11.1600000000000001	0	43	0	2014-05-13 10:55:02+00
8	SmartSantander	http://www.smartsantander.eu	53.2000000000000028	0	0	0	2014-05-13 10:50:01+00
16	Ofelia (i2CAT vtam)	https://alpha.fp7-ofelia.eu/doc/index.php/Testbed_Barcelona	11.8699999999999992	0	4	0	2014-05-13 10:55:02+00
5	NETMODE	http://www.netmode.ntua.gr/main/index.php?option=com_content&view=article&id=103&Itemid=83	62.2199999999999989	0	20	0	2014-05-13 10:59:22+00
4	FUSECO	http://www.fokus.fraunhofer.de/en/fokus_testbeds/fuseco_playground/index.html	16.0899999999999999	0	18	0	2014-05-13 11:00:03+00
1	Virtual Wall 2	https://www.wall2.ilabt.iminds.be/	0.0700000000000000067	0	23	0	2014-05-13 11:00:09+00
14	Virtual Wall 2 (openflow)	http://fed4fire-testbeds.ilabt.iminds.be/ilabt-documentation/openflow.html	1.91999999999999993	0	2	0	2014-05-13 11:00:09+00
2	w-iLab.t 2	http://www.wilab2.ilabt.iminds.be	169.860000000000014	0	38	0	2014-05-13 10:59:58+00
6	BonFIRE	http://www.bonfire-project.eu/infrastructure/bonfire-infrastructure-health-map	31.1700000000000017	-1	-2	0	2014-05-13 11:00:29+00
\.


--
-- Name: flstestbeds_testbedid_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY flstestbeds
    ADD CONSTRAINT flstestbeds_testbedid_key UNIQUE (testbedid);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- Name: flstestbeds; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE flstestbeds FROM PUBLIC;
REVOKE ALL ON TABLE flstestbeds FROM postgres;
GRANT ALL ON TABLE flstestbeds TO postgres;
GRANT ALL ON TABLE flstestbeds TO oml;


--
-- PostgreSQL database dump complete
--

