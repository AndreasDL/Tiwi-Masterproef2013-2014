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
29	FIU PGv1	https://pg-boss.cis.fiu.edu/	-1	2	-1	\N	\N
23	InstaGeni Missouri	https://www.instageni.rnet.missouri.edu	142.289999999999992	0	0	\N	\N
18	InstaGeni Princeton	https://www.instageni.cs.princeton.edu	97.3100000000000023	0	0	\N	\N
5	Larc Usp Brazil	http://www.emulab.larc.usp.br/	261.050000000000011	0	14	\N	\N
6	Orca ExoSM	https://wiki.exogeni.net/doku.php?id=public:experimenters:orca_sm	128.25	0	30	\N	\N
9	Orca FIU SM	https://wiki.exogeni.net/doku.php?id=public:experimenters:orca_sm	148.280000000000001	0	3	\N	\N
28	Orca OSF SM	https://wiki.exogeni.net/doku.php?id=public:experimenters:orca_sm	161.610000000000014	0	3	\N	\N
17	InstaGeni MAX	https://www.instageni.maxgigapop.net	100.219999999999999	0	0	\N	\N
24	InstaGeni Nysernet	https://www.instageni.nysernet.org	98.5900000000000034	0	0	\N	\N
25	InstaGeni SOX Atlanta	https://www.instageni.sox.net	134.810000000000002	0	0	\N	\N
33	LSU CRON	https://www.cron.loni.org	163.120000000000005	0	0	\N	\N
14	InstaGeni NorthWestern	https://www.instageni.northwestern.edu	138.069999999999993	0	0	\N	\N
4	Utah Emulab	http://www.emulab.net	169.75	0	143	\N	\N
19	InstaGeni BBN	https://www.instageni.gpolab.bbn.com	91.8199999999999932	0	0	\N	\N
20	InstaGeni Clemson	https://www.instageni.clemson.edu	137.180000000000007	0	0	\N	\N
21	InstaGeni GATech	https://www.instageni.rnoc.gatech.edu	134.490000000000009	0	0	\N	\N
16	InstaGeni Illinois	https://www.instageni.illinois.edu	140.659999999999997	0	0	\N	\N
22	InstaGeni Kettering	https://www.geni.kettering.edu	145.490000000000009	0	0	\N	\N
35	ION internet2 AM	https://geni-am.net.internet2.edu	129.490000000000009	2	-1	\N	\N
34	KetteringU emulab	https://boss.geni.kettering.edu	145.509999999999991	0	0	\N	\N
12	Orca NICTA SM	https://wiki.exogeni.net/doku.php?id=public:experimenters:orca_sm	305.629999999999995	0	3	\N	\N
36	NYU genirack	https://boss.genirack.nyu.edu	93.7000000000000028	0	0	\N	\N
11	Orca Duke SM	https://wiki.exogeni.net/doku.php?id=public:experimenters:orca_sm	128.159999999999997	0	3	\N	\N
7	Orca RCI SM	https://wiki.exogeni.net/doku.php?id=public:experimenters:orca_sm	129.449999999999989	0	3	\N	\N
15	InstaGeni Uky	https://www.lan.sdn.uky.edu	133.27000000000001	0	0	\N	\N
8	Orca BBN SM	https://wiki.exogeni.net/doku.php?id=public:experimenters:orca_sm	91.9399999999999977	0	3	\N	\N
27	Orca UFL SM	https://wiki.exogeni.net/doku.php?id=public:experimenters:orca_sm	130.949999999999989	0	3	\N	\N
10	Orca UH SM	https://wiki.exogeni.net/doku.php?id=public:experimenters:orca_sm	128.860000000000014	0	3	\N	\N
30	UKY emulab	https://www.uky.emulab.net	132.939999999999998	0	5	\N	\N
1	Virtual Wall	https://www.wall2.ilabt.iminds.be/	0.0800000000000000017	0	23	0	\N
2	w-iLab.t 2	http://www.wilab2.ilabt.iminds.be	27.7800000000000011	0	38	0	\N
26	InstaGeni Utah DDC	https://www.utahddc.geniracks.net	156.659999999999997	0	0	\N	\N
13	Orca UvA NL SM	https://wiki.exogeni.net/doku.php?id=public:experimenters:orca_sm	5.96999999999999975	0	3	\N	\N
3	Planetlab Europe	http://www.planet-lab.eu	30.6000000000000014	0	283	0	\N
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

