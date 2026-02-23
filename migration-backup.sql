--
-- PostgreSQL database cluster dump
--

\restrict mOQn5HqT2FociBVr9XfCnynNP3Dfds9vjVFMgSlGEU1lfa4gAwKD5lFGV7M1DcC

SET default_transaction_read_only = off;

SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

--
-- Roles
--

CREATE ROLE root;
ALTER ROLE root WITH SUPERUSER INHERIT CREATEROLE CREATEDB LOGIN REPLICATION BYPASSRLS PASSWORD 'SCRAM-SHA-256$4096:KQ0MfG0Rj+WOPek1vzvCyQ==$y3ITcpqFwEHVFMXBmfQE5rb5G8oiqohHtMPQR2fSAMY=:gO241vPPD8+BdFvGUd2bhdhU7KhWK7T+8dKubLJXL94=';

--
-- User Configurations
--








\unrestrict mOQn5HqT2FociBVr9XfCnynNP3Dfds9vjVFMgSlGEU1lfa4gAwKD5lFGV7M1DcC

--
-- Databases
--

--
-- Database "template1" dump
--

\connect template1

--
-- PostgreSQL database dump
--

\restrict N02zolaNioK6sWoRbX4NGWGUrZYUfYy45GK47umDoPHgd6YSdovE3FlAYyUFphg

-- Dumped from database version 17.8 (Debian 17.8-1.pgdg13+1)
-- Dumped by pg_dump version 17.8 (Debian 17.8-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- PostgreSQL database dump complete
--

\unrestrict N02zolaNioK6sWoRbX4NGWGUrZYUfYy45GK47umDoPHgd6YSdovE3FlAYyUFphg

--
-- Database "informatics" dump
--

--
-- PostgreSQL database dump
--

\restrict xuSBYJ78WRdR9EUbC68aDkEBikA9CXP0IzsMAR5bYkQme5k7qgi1WZoaMvWYZL3

-- Dumped from database version 17.8 (Debian 17.8-1.pgdg13+1)
-- Dumped by pg_dump version 17.8 (Debian 17.8-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: informatics; Type: DATABASE; Schema: -; Owner: root
--

CREATE DATABASE informatics WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.utf8';


ALTER DATABASE informatics OWNER TO root;

\unrestrict xuSBYJ78WRdR9EUbC68aDkEBikA9CXP0IzsMAR5bYkQme5k7qgi1WZoaMvWYZL3
\connect informatics
\restrict xuSBYJ78WRdR9EUbC68aDkEBikA9CXP0IzsMAR5bYkQme5k7qgi1WZoaMvWYZL3

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: contest; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.contest (
    id bigint NOT NULL,
    enddate timestamp(6) without time zone,
    name character varying(255) NOT NULL,
    roomid bigint,
    scoringtype smallint NOT NULL,
    startdate timestamp(6) without time zone,
    upsolving boolean NOT NULL,
    upsolvingafterfinished boolean NOT NULL,
    version integer,
    CONSTRAINT contest_scoringtype_check CHECK (((scoringtype >= 0) AND (scoringtype <= 1)))
);


ALTER TABLE public.contest OWNER TO root;

--
-- Name: contest_principal; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.contest_principal (
    contest_id bigint NOT NULL,
    participants_id bigint NOT NULL
);


ALTER TABLE public.contest_principal OWNER TO root;

--
-- Name: contest_room; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.contest_room (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    open boolean NOT NULL
);


ALTER TABLE public.contest_room OWNER TO root;

--
-- Name: contest_room_contest; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.contest_room_contest (
    contest_room_id bigint NOT NULL,
    contests_id bigint NOT NULL
);


ALTER TABLE public.contest_room_contest OWNER TO root;

--
-- Name: contest_room_principal; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.contest_room_principal (
    contest_room_id bigint NOT NULL,
    teachers_id bigint NOT NULL,
    participants_id bigint NOT NULL
);


ALTER TABLE public.contest_room_principal OWNER TO root;

--
-- Name: contest_room_user; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.contest_room_user (
    contest_room_id bigint NOT NULL,
    teachers_id bigint NOT NULL,
    participants_id bigint NOT NULL
);


ALTER TABLE public.contest_room_user OWNER TO root;

--
-- Name: contest_seq; Type: SEQUENCE; Schema: public; Owner: root
--

CREATE SEQUENCE public.contest_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.contest_seq OWNER TO root;

--
-- Name: contest_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: root
--

ALTER SEQUENCE public.contest_seq OWNED BY public.contest.id;


--
-- Name: contest_task; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.contest_task (
    contest_id bigint NOT NULL,
    tasks_id bigint NOT NULL
);


ALTER TABLE public.contest_task OWNER TO root;

--
-- Name: contest_user; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.contest_user (
    contest_id bigint NOT NULL,
    participants_id bigint NOT NULL
);


ALTER TABLE public.contest_user OWNER TO root;

--
-- Name: contestant_result; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.contestant_result (
    id bigint NOT NULL,
    contestantid bigint NOT NULL,
    totalscore real NOT NULL,
    contest_id bigint NOT NULL,
    upsolving_contest_id bigint
);


ALTER TABLE public.contestant_result OWNER TO root;

--
-- Name: contestant_result_id_seq; Type: SEQUENCE; Schema: public; Owner: root
--

ALTER TABLE public.contestant_result ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.contestant_result_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO root;

--
-- Name: post; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.post (
    id bigint NOT NULL,
    content text NOT NULL,
    postdate timestamp(6) without time zone,
    roomid bigint,
    title character varying(255) NOT NULL,
    author_id bigint,
    createdate timestamp(6) without time zone,
    draftcontent text,
    lastupdatedate timestamp(6) without time zone,
    status smallint,
    version bigint,
    CONSTRAINT post_status_check CHECK (((status >= 0) AND (status <= 1)))
);


ALTER TABLE public.post OWNER TO root;

--
-- Name: post_id_seq; Type: SEQUENCE; Schema: public; Owner: root
--

ALTER TABLE public.post ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.post_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: postcomment; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.postcomment (
    id bigint NOT NULL,
    authorid bigint NOT NULL,
    comment character varying(1000) NOT NULL,
    createdate timestamp(6) without time zone NOT NULL,
    lastupdatedate timestamp(6) without time zone NOT NULL,
    parentid bigint,
    postid bigint NOT NULL
);


ALTER TABLE public.postcomment OWNER TO root;

--
-- Name: postcomment_id_seq; Type: SEQUENCE; Schema: public; Owner: root
--

ALTER TABLE public.postcomment ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.postcomment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: principal; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.principal (
    id bigint NOT NULL,
    email character varying(255) NOT NULL,
    first_name character varying(255) NOT NULL,
    last_name character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    password_salt character varying(255) NOT NULL,
    role character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    version integer,
    last_login timestamp(6) without time zone,
    registration_time timestamp(6) without time zone
);


ALTER TABLE public.principal OWNER TO root;

--
-- Name: principal_id_seq; Type: SEQUENCE; Schema: public; Owner: root
--

ALTER TABLE public.principal ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.principal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: recoverpassword; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.recoverpassword (
    id bigint NOT NULL,
    createtime timestamp(6) without time zone,
    link character varying(255),
    used boolean NOT NULL,
    userid bigint NOT NULL
);


ALTER TABLE public.recoverpassword OWNER TO root;

--
-- Name: recoverpassword_id_seq; Type: SEQUENCE; Schema: public; Owner: root
--

ALTER TABLE public.recoverpassword ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.recoverpassword_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: solved_problem; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.solved_problem (
    id bigint NOT NULL,
    last_attempt_at timestamp(6) without time zone NOT NULL,
    solved_at timestamp(6) without time zone,
    status character varying(255) NOT NULL,
    task_id bigint NOT NULL,
    user_id bigint NOT NULL,
    CONSTRAINT solved_problem_status_check CHECK (((status)::text = ANY ((ARRAY['FAILED'::character varying, 'SOLVED'::character varying])::text[])))
);


ALTER TABLE public.solved_problem OWNER TO root;

--
-- Name: solved_problem_id_seq; Type: SEQUENCE; Schema: public; Owner: root
--

ALTER TABLE public.solved_problem ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.solved_problem_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: submission; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.submission (
    id bigint NOT NULL,
    compilationmessage character varying(255),
    compilationresult character varying(255),
    currenttest integer,
    filename character varying(255) NOT NULL,
    language character varying(255) NOT NULL,
    score real,
    status smallint NOT NULL,
    submissiontime timestamp(6) without time zone NOT NULL,
    contest_id bigint,
    task_id bigint,
    user_id bigint,
    roomid bigint,
    submissionmemory integer,
    memory integer,
    "time" bigint,
    CONSTRAINT submission_status_check CHECK (((status >= 0) AND (status <= 13)))
);


ALTER TABLE public.submission OWNER TO root;

--
-- Name: submission_seq; Type: SEQUENCE; Schema: public; Owner: root
--

CREATE SEQUENCE public.submission_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.submission_seq OWNER TO root;

--
-- Name: submission_submissiontestresults; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.submission_submissiontestresults (
    submission_id bigint NOT NULL,
    idx character varying(255),
    memory integer,
    outcome character varying(255),
    text character varying(255),
    "time" integer,
    message character varying(255),
    score real,
    testkey character varying(255),
    teststatus smallint,
    CONSTRAINT submission_submissiontestresults_teststatus_check CHECK (((teststatus >= 0) AND (teststatus <= 5)))
);


ALTER TABLE public.submission_submissiontestresults OWNER TO root;

--
-- Name: tag; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.tag (
    id bigint NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.tag OWNER TO root;

--
-- Name: tag_id_seq; Type: SEQUENCE; Schema: public; Owner: root
--

CREATE SEQUENCE public.tag_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tag_id_seq OWNER TO root;

--
-- Name: tag_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: root
--

ALTER SEQUENCE public.tag_id_seq OWNED BY public.tag.id;


--
-- Name: task; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.task (
    id bigint NOT NULL,
    code character varying(255) NOT NULL,
    configaddress character varying(255),
    inputtemplate character varying(255),
    memorylimitmb integer,
    outputtemplate character varying(255),
    taskscoreparameter character varying(255),
    taskscoretype smallint,
    tasktype smallint,
    timelimitmillis integer,
    title character varying(255),
    contest_id bigint,
    checkertype smallint,
    taskorder integer DEFAULT 0 NOT NULL,
    CONSTRAINT task_checkertype_check CHECK (((checkertype >= 0) AND (checkertype <= 5))),
    CONSTRAINT task_taskscoretype_check CHECK (((taskscoretype >= 0) AND (taskscoretype <= 1))),
    CONSTRAINT task_tasktype_check CHECK (((tasktype >= 0) AND (tasktype <= 0)))
);


ALTER TABLE public.task OWNER TO root;

--
-- Name: task_id_seq; Type: SEQUENCE; Schema: public; Owner: root
--

ALTER TABLE public.task ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.task_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: task_metadata_tags; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.task_metadata_tags (
    task_metadata_id bigint NOT NULL,
    tag_id bigint NOT NULL
);


ALTER TABLE public.task_metadata_tags OWNER TO root;

--
-- Name: task_results; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.task_results (
    standings_id bigint NOT NULL,
    attempts integer,
    score real,
    successtime bigint,
    taskcode character varying(255),
    task_code character varying(255) NOT NULL
);


ALTER TABLE public.task_results OWNER TO root;

--
-- Name: task_statements; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.task_statements (
    task_id bigint NOT NULL,
    statement character varying(100000),
    language smallint NOT NULL,
    CONSTRAINT task_statements_language_check CHECK (((language >= 0) AND (language <= 1)))
);


ALTER TABLE public.task_statements OWNER TO root;

--
-- Name: task_testcase; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.task_testcase (
    task_id bigint NOT NULL,
    testcases_id bigint NOT NULL
);


ALTER TABLE public.task_testcase OWNER TO root;

--
-- Name: taskmetadata; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.taskmetadata (
    id bigint NOT NULL,
    difficultylevel integer,
    fullsolutions integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.taskmetadata OWNER TO root;

--
-- Name: testcase; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.testcase (
    id bigint NOT NULL,
    inputfileaddress character varying(255),
    outputfileaddress character varying(255),
    key character varying(255),
    taskid bigint,
    inputsnippet character varying(3000),
    outputsnippet character varying(255),
    publictestcase boolean DEFAULT false NOT NULL
);


ALTER TABLE public.testcase OWNER TO root;

--
-- Name: testcase_seq; Type: SEQUENCE; Schema: public; Owner: root
--

CREATE SEQUENCE public.testcase_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.testcase_seq OWNER TO root;

--
-- Name: worker; Type: TABLE; Schema: public; Owner: root
--

CREATE TABLE public.worker (
    id bigint NOT NULL,
    jobs_processed bigint NOT NULL,
    last_heartbeat timestamp(6) without time zone NOT NULL,
    start_time timestamp(6) without time zone NOT NULL,
    status character varying(255) NOT NULL,
    version integer,
    worker_id character varying(255) NOT NULL,
    CONSTRAINT worker_status_check CHECK (((status)::text = ANY ((ARRAY['ONLINE'::character varying, 'OFFLINE'::character varying, 'WORKING'::character varying])::text[])))
);


ALTER TABLE public.worker OWNER TO root;

--
-- Name: worker_id_seq; Type: SEQUENCE; Schema: public; Owner: root
--

ALTER TABLE public.worker ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.worker_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: contest id; Type: DEFAULT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest ALTER COLUMN id SET DEFAULT nextval('public.contest_seq'::regclass);


--
-- Name: tag id; Type: DEFAULT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.tag ALTER COLUMN id SET DEFAULT nextval('public.tag_id_seq'::regclass);


--
-- Data for Name: contest; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.contest (id, enddate, name, roomid, scoringtype, startdate, upsolving, upsolvingafterfinished, version) FROM stdin;
5	2025-11-18 14:32:00	contest1	1	0	2025-11-18 13:32:00	t	t	1
4	\N	test	1	0	\N	t	t	20
\.


--
-- Data for Name: contest_principal; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.contest_principal (contest_id, participants_id) FROM stdin;
\.


--
-- Data for Name: contest_room; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.contest_room (id, name, open) FROM stdin;
1	GLOBAL	t
\.


--
-- Data for Name: contest_room_contest; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.contest_room_contest (contest_room_id, contests_id) FROM stdin;
\.


--
-- Data for Name: contest_room_principal; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.contest_room_principal (contest_room_id, teachers_id, participants_id) FROM stdin;
\.


--
-- Data for Name: contest_room_user; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.contest_room_user (contest_room_id, teachers_id, participants_id) FROM stdin;
\.


--
-- Data for Name: contest_task; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.contest_task (contest_id, tasks_id) FROM stdin;
4	7
4	6
4	2
4	5
4	4
4	3
4	1
4	8
\.


--
-- Data for Name: contest_user; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.contest_user (contest_id, participants_id) FROM stdin;
\.


--
-- Data for Name: contestant_result; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.contestant_result (id, contestantid, totalscore, contest_id, upsolving_contest_id) FROM stdin;
3	4	100	4	\N
4	4	100	4	4
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	<< Flyway Baseline >>	BASELINE	<< Flyway Baseline >>	\N	root	2025-06-22 22:18:09.927341	0	t
2	1.1	public testcases	SQL	V1.1__public_testcases.sql	1728004111	root	2025-11-02 16:23:03.635637	12	t
3	1.2	snippet length adjustment	SQL	V1.2__snippet_length_adjustment.sql	-282611884	root	2025-11-03 14:53:43.450399	13	t
4	1.3	add task order column	SQL	V1.3__add_task_order_column.sql	1140941614	root	2025-12-11 15:41:25.828411	19	t
5	1.4	rename order to taskorder	SQL	V1.4__rename_order_to_taskorder.sql	1521819783	root	2025-12-12 18:54:16.929961	15	t
6	1.5	add task metadata and tags	SQL	V1.5__add_task_metadata_and_tags.sql	1846714202	root	2025-12-14 15:16:39.155638	13	t
\.


--
-- Data for Name: post; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.post (id, content, postdate, roomid, title, author_id, createdate, draftcontent, lastupdatedate, status, version) FROM stdin;
\.


--
-- Data for Name: postcomment; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.postcomment (id, authorid, comment, createdate, lastupdatedate, parentid, postid) FROM stdin;
\.


--
-- Data for Name: principal; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.principal (id, email, first_name, last_name, password, password_salt, role, username, version, last_login, registration_time) FROM stdin;
4	admin@admin.com	admin	admin	rsPjH2/faaOPb2dlwTp9pjQDklo9NvSwS0SkT3qs16o=	scljrZZkMsY=	ADMIN	admin	1	\N	\N
5	worker@informatics.local	Worker	Service	5DdFAtcCHq7ATPiYUVphp8rrU00hOcTCzQHPL5JU514=	ZLBzFEFrH8c=	WORKER	worker	9	\N	\N
\.


--
-- Data for Name: recoverpassword; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.recoverpassword (id, createtime, link, used, userid) FROM stdin;
\.


--
-- Data for Name: solved_problem; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.solved_problem (id, last_attempt_at, solved_at, status, task_id, user_id) FROM stdin;
\.


--
-- Data for Name: submission; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.submission (id, compilationmessage, compilationresult, currenttest, filename, language, score, status, submissiontime, contest_id, task_id, user_id, roomid, submissionmemory, memory, "time") FROM stdin;
1	\N	\N	1	admin1750664073383.cpp	CPP	\N	1	2025-06-23 07:34:33.383	4	1	4	1	\N	\N	\N
2	\N	\N	1	admin1750664418249.cpp	CPP	\N	1	2025-06-23 07:40:18.249	4	1	4	1	\N	\N	\N
406	\N	\N	4	admin1765563552130.cpp	CPP	90	11	2025-12-12 18:19:12.13	4	2	4	1	\N	\N	\N
3	\N	\N	1	admin1750664575530.cpp	CPP	\N	1	2025-06-23 07:42:55.53	4	2	4	1	\N	\N	\N
305	\N	\N	8	admin1762183093634.cpp	CPP	100	12	2025-11-03 15:18:13.634	4	6	4	1	\N	\N	\N
4	\N	\N	1	admin1750664606676.cpp	CPP	\N	1	2025-06-23 07:43:26.676	4	2	4	1	\N	\N	\N
5	\N	\N	3	admin1750664906143.cpp	CPP	\N	1	2025-06-23 07:48:26.143	4	2	4	1	\N	\N	\N
205	\N	\N	9	admin1750793264781.cpp	CPP	90	11	2025-06-24 19:27:44.781	4	4	4	1	\N	\N	\N
6	\N	\N	3	admin1750665041216.cpp	CPP	\N	1	2025-06-23 07:50:41.216	4	2	4	1	\N	\N	\N
153	\N	\N	9	admin1750783512619.cpp	CPP	90	11	2025-06-24 16:45:12.619	4	4	4	1	\N	\N	\N
52	\N	\N	3	admin1750665382022.cpp	CPP	\N	1	2025-06-23 07:56:22.022	4	2	4	1	\N	\N	\N
303	\N	\N	8	admin1762182431843.cpp	CPP	0	4	2025-11-03 15:07:11.843	4	6	4	1	\N	\N	\N
209	\N	\N	9	admin1750796536397.cpp	CPP	100	12	2025-06-24 20:22:16.397	4	5	4	1	\N	\N	\N
53	\N	\N	3	admin1750665519420.cpp	CPP	0	10	2025-06-23 07:58:39.42	4	2	4	1	\N	\N	\N
252	\N	\N	1	admin1750930717857.cpp	CPP	\N	1	2025-06-26 09:38:37.857	4	6	4	1	\N	\N	\N
206	\N	\N	9	admin1750793347571.cpp	CPP	100	12	2025-06-24 19:29:07.571	4	4	4	1	\N	\N	\N
102	Error processing message: java.lang.RuntimeException: Error during copy: cp: -r not specified; omitting directory '/sandbox/tasks/2/tests/aplusb.i05'\n	\N	4	admin1750696000556.cpp	CPP	40	12	2025-06-23 16:26:40.556	4	2	4	1	\N	\N	\N
202	\N	\N	9	admin1750792500494.cpp	CPP	90	11	2025-06-24 19:15:00.494	4	4	4	1	\N	\N	\N
253	\N	\N	1	admin1750933430604.cpp	CPP	\N	1	2025-06-26 10:23:50.604	4	6	4	1	\N	\N	\N
103	Error processing message: java.lang.RuntimeException: Error during copy: cp: -r not specified; omitting directory '/sandbox/tasks/2/tests/aplusb.i05'\n	\N	4	admin1750696137786.cpp	CPP	40	12	2025-06-23 16:28:57.786	4	2	4	1	\N	\N	\N
403	\N	\N	3	admin1765533737859.cpp	CPP	90	11	2025-12-12 10:02:17.859	4	2	4	1	\N	\N	\N
254	\N	\N	1	admin1750946193267.cpp	CPP	\N	1	2025-06-26 13:56:33.267	4	6	4	1	\N	\N	\N
353	\N	\N	9	admin1765469013662.cpp	CPP	90	11	2025-12-11 16:03:33.662	4	2	4	1	\N	\N	\N
207	\N	\N	9	admin1750793570642.cpp	CPP	100	12	2025-06-24 19:32:50.642	4	4	4	1	\N	\N	\N
203	\N	\N	9	admin1750792715587.cpp	CPP	90	11	2025-06-24 19:18:35.587	4	4	4	1	\N	\N	\N
152	\N	\N	9	admin1750776709794.cpp	CPP	100	12	2025-06-24 14:51:49.794	4	3	4	1	\N	\N	\N
452	\N	\N	3	admin1765566172768.cpp	CPP	100	12	2025-12-12 19:02:52.768	4	2	4	1	\N	\N	\N
306	\N	\N	8	admin1762184145635.cpp	CPP	0	10	2025-11-03 15:35:45.635	4	6	4	1	\N	\N	\N
405	\N	\N	4	admin1765535151741.cpp	CPP	90	11	2025-12-12 10:25:51.741	4	2	4	1	\N	\N	\N
304	\N	\N	8	admin1762182552323.cpp	CPP	10	11	2025-11-03 15:09:12.323	4	6	4	1	\N	\N	\N
204	\N	\N	9	admin1750793225416.cpp	CPP	0	10	2025-06-24 19:27:05.416	4	4	4	1	\N	\N	\N
302	\N	\N	8	admin1762182312633.cpp	CPP	0	4	2025-11-03 15:05:12.633	4	6	4	1	\N	\N	\N
208	\N	\N	9	admin1750795031930.cpp	CPP	100	12	2025-06-24 19:57:11.93	4	5	4	1	\N	\N	\N
603	\N	\N	9	admin1766416486401.cpp	CPP	\N	1	2025-12-22 15:14:46.401	4	8	4	1	\N	\N	\N
407	\N	\N	3	admin1765563620409.cpp	CPP	90	11	2025-12-12 18:20:20.409	4	2	4	1	\N	\N	\N
402	\N	\N	0	admin1765533024340.cpp	CPP	90	11	2025-12-12 09:50:24.34	4	2	4	1	\N	\N	\N
352	\N	\N	9	admin1765468747348.cpp	CPP	0	10	2025-12-11 15:59:07.348	4	2	4	1	\N	\N	\N
404	\N	\N	4	admin1765534653838.cpp	CPP	90	11	2025-12-12 10:17:33.838	4	2	4	1	\N	\N	\N
552	\N	\N	9	admin1766342566278.cpp	CPP	100	12	2025-12-21 18:42:46.278	4	8	4	1	\N	\N	\N
602	\N	\N	1	admin1766414903948.cpp	CPP	\N	1	2025-12-22 14:48:23.948	4	3	4	1	\N	\N	\N
502	\N	\N	3	admin1765725790513.cpp	CPP	\N	1	2025-12-14 15:23:10.513	4	2	4	1	\N	\N	\N
\.


--
-- Data for Name: submission_submissiontestresults; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.submission_submissiontestresults (submission_id, idx, memory, outcome, text, "time", message, score, testkey, teststatus) FROM stdin;
5	\N	2816	Hello, World!	\N	0		0	01	3
5	\N	3072	Hello, World!	\N	0		0	02	3
5	\N	3072	Hello, World!	\N	0		0	03	3
5	\N	3072	Hello, World!	\N	0		0	04	3
6	\N	3072	Hello, World!	\N	0		0	01	3
6	\N	3072	Hello, World!	\N	0		0	02	3
6	\N	3072	Hello, World!	\N	0		0	03	3
6	\N	3072	Hello, World!	\N	0		0	04	3
52	\N	3072	Hello, World!	\N	0		0	01	3
52	\N	2816	Hello, World!	\N	0		0	02	3
52	\N	3072	Hello, World!	\N	0		0	03	3
52	\N	3072	Hello, World!	\N	0		0	04	3
53	\N	3072	Hello, World!	\N	0		0	01	3
53	\N	3072	Hello, World!	\N	0		0	02	3
53	\N	3072	Hello, World!	\N	0		0	03	3
53	\N	3072	Hello, World!	\N	0		0	04	3
102	\N	3072	33	\N	0		1	01	5
102	\N	3072	195	\N	0		1	02	5
102	\N	3072	33	\N	0		1	03	5
102	\N	3072	195	\N	0		1	04	5
103	\N	3072	33	\N	0		1	01	5
103	\N	3072	195	\N	0		1	02	5
103	\N	3072	33	\N	0		1	03	5
103	\N	3072	195	\N	0		1	04	5
152	\N	3072	7	\N	0		1	01	5
152	\N	3072	13	\N	0		1	02	5
152	\N	3072	2	\N	0		1	03	5
152	\N	3072	6	\N	0		1	04	5
152	\N	3072	18	\N	0		1	05	5
152	\N	3072	10	\N	0		1	06	5
152	\N	3072	15	\N	0		1	07	5
152	\N	3072	8	\N	0		1	08	5
152	\N	3072	9	\N	0		1	09	5
152	\N	3328	16	\N	0		1	10	5
153	\N	3072	36639	\N	0		1	01	5
153	\N	3072	55039	\N	0		1	02	5
153	\N	3072	72527	\N	0		1	03	5
153	\N	3072	0	\N	0		1	04	5
153	\N	3072	1	\N	0		1	05	5
153	\N	3072	54915	\N	0		1	06	5
153	\N	3072	62237	\N	0		1	07	5
153	\N	3072	-158400	\N	0		0	08	3
153	\N	3072	72736	\N	0		1	09	5
153	\N	3072	44531	\N	0		1	10	5
202	\N	3072	36639	\N	0		1	01	5
202	\N	3072	55039	\N	0		1	02	5
202	\N	3072	72527	\N	0		1	03	5
202	\N	3072	0	\N	0		1	04	5
202	\N	3072	1	\N	0		1	05	5
202	\N	3072	54915	\N	0		1	06	5
202	\N	3072	62237	\N	0		1	07	5
202	\N	3072	-158400	\N	0		0	08	3
202	\N	3072	72736	\N	0		1	09	5
202	\N	3072	44531	\N	0		1	10	5
203	\N	3072	36639	\N	0		1	01	5
203	\N	3072	55039	\N	0		1	02	5
203	\N	3072	72527	\N	0		1	03	5
203	\N	3072	0	\N	0		1	04	5
203	\N	3072	1	\N	0		1	05	5
203	\N	3072	54915	\N	0		1	06	5
203	\N	3072	62237	\N	0		1	07	5
203	\N	3072	72736	\N	0		1	09	5
203	\N	3072	44531	\N	0		1	10	5
203	\N	3072	-158400	\N	0		0	08	3
204	\N	3328	Hello, World!	\N	0		0	01	3
204	\N	3072	Hello, World!	\N	0		0	02	3
204	\N	3072	Hello, World!	\N	0		0	03	3
204	\N	3072	Hello, World!	\N	0		0	04	3
204	\N	3072	Hello, World!	\N	0		0	05	3
204	\N	3072	Hello, World!	\N	0		0	06	3
204	\N	3072	Hello, World!	\N	0		0	07	3
204	\N	3072	Hello, World!	\N	0		0	09	3
204	\N	3072	Hello, World!	\N	0		0	10	3
204	\N	3072	Hello, World!	\N	0		0	08	3
205	\N	3072	36639	\N	0		1	01	5
205	\N	3072	55039	\N	0		1	02	5
205	\N	3072	72527	\N	0		1	03	5
205	\N	3072	0	\N	0		1	04	5
205	\N	3072	1	\N	0		1	05	5
205	\N	3072	54915	\N	0		1	06	5
205	\N	3072	62237	\N	0		1	07	5
205	\N	3072	72736	\N	0		1	09	5
205	\N	3072	44531	\N	0		1	10	5
205	\N	3072	-158400	\N	0		0	08	3
206	\N	3072	36639	\N	0		1	01	5
206	\N	3072	55039	\N	0		1	02	5
206	\N	3072	72527	\N	0		1	03	5
206	\N	3072	0	\N	0		1	04	5
206	\N	3072	1	\N	0		1	05	5
206	\N	3072	54915	\N	0		1	06	5
206	\N	3072	62237	\N	0		1	07	5
206	\N	3072	72736	\N	0		1	09	5
206	\N	3072	44531	\N	0		1	10	5
206	\N	3072	62237	\N	0		1	08	5
206	\N	3328	55039	\N	0		1	02	5
206	\N	3072	72527	\N	0		1	03	5
206	\N	3072	0	\N	0		1	04	5
206	\N	3072	1	\N	0		1	05	5
206	\N	3328	54915	\N	0		1	06	5
206	\N	3072	62237	\N	0		1	07	5
206	\N	2816	72736	\N	0		1	09	5
206	\N	3072	44531	\N	0		1	10	5
206	\N	3072	62237	\N	0		1	08	5
207	\N	3072	36639	\N	0		1	01	5
207	\N	3072	55039	\N	0		1	02	5
207	\N	3072	72527	\N	0		1	03	5
207	\N	3072	0	\N	0		1	04	5
207	\N	3072	1	\N	0		1	05	5
207	\N	2816	54915	\N	0		1	06	5
207	\N	3072	62237	\N	0		1	07	5
207	\N	3072	72736	\N	0		1	09	5
207	\N	3072	44531	\N	0		1	10	5
207	\N	3328	62237	\N	0		1	08	5
208	\N	3328	-224\n	\N	0		1	03	5
208	\N	3328	-9\n	\N	0		1	01	5
208	\N	3328	-56\n	\N	0		1	02	5
208	\N	3072	-62\n	\N	0		1	04	5
208	\N	3072	-704\n	\N	0		1	05	5
208	\N	3328	-96\n	\N	0		1	06	5
208	\N	3328	-531\n	\N	0		1	07	5
208	\N	3328	-2907\n	\N	0		1	08	5
208	\N	3328	-3187\n	\N	0		1	09	5
208	\N	3328	-4325\n	\N	0		1	10	5
209	\N	3328	-224\n	\N	27		1	03	5
209	\N	3328	-9\n	\N	25		1	01	5
209	\N	3328	-56\n	\N	23		1	02	5
209	\N	3328	-62\n	\N	24		1	04	5
209	\N	3328	-704\n	\N	25		1	05	5
209	\N	3328	-96\n	\N	24		1	06	5
209	\N	3328	-531\n	\N	24		1	07	5
209	\N	3328	-2907\n	\N	25		1	08	5
209	\N	3328	-3187\n	\N	25		1	09	5
209	\N	3584	-4325\n	\N	25		1	10	5
302	\N	2816		\N	125	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	1	2
302	\N	2816		\N	89	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	10	2
302	\N	3072	0\n	\N	37		1	2	5
302	\N	2816		\N	83	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	3	2
302	\N	2560		\N	82	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	4	2
302	\N	3072		\N	106	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	5	2
302	\N	2816		\N	84	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	6	2
302	\N	2816		\N	86	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	7	2
302	\N	2816		\N	84	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	8	2
302	\N	2560		\N	82	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	9	2
303	\N	2816		\N	91	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	1	2
303	\N	2816		\N	90	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	10	2
303	\N	3072	0\n	\N	24		1	2	5
303	\N	2816		\N	83	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	3	2
303	\N	3072		\N	100	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	4	2
303	\N	2816		\N	83	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	5	2
303	\N	3072		\N	82	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	6	2
303	\N	2816		\N	88	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	7	2
303	\N	2816		\N	85	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	8	2
303	\N	2816		\N	84	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	9	2
304	\N	2816		\N	91	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	1	2
304	\N	2816		\N	89	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	10	2
304	\N	3328	0\n	\N	43		1	2	5
304	\N	2816		\N	82	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	3	2
304	\N	2816		\N	83	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	4	2
304	\N	2816		\N	98	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	5	2
304	\N	2816		\N	81	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	6	2
304	\N	2816		\N	82	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	7	2
304	\N	2816		\N	92	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	8	2
304	\N	2816		\N	85	Segmentation fault (core dumped)\nCommand exited with non-zero status 139	0	9	2
305	\N	3072	3\n	\N	26		1	1	5
305	\N	3584	1\n	\N	27		1	10	5
305	\N	3072	0\n	\N	25		1	2	5
305	\N	3072	1\n	\N	24		1	3	5
305	\N	3072	1\n	\N	24		1	4	5
305	\N	3072	7\n	\N	24		1	5	5
305	\N	3072	46\n	\N	25		1	6	5
305	\N	3072	38\n	\N	23		1	7	5
305	\N	3072	73\n	\N	24		1	8	5
305	\N	3072	648\n	\N	41		1	9	5
306	\N	3072	Hello, World!	\N	26		0	1	3
306	\N	3072	Hello, World!	\N	26		0	10	3
306	\N	3072	Hello, World!	\N	45		0	2	3
306	\N	3072	Hello, World!	\N	23		0	3	3
306	\N	3072	Hello, World!	\N	24		0	4	3
306	\N	3072	Hello, World!	\N	30		0	5	3
306	\N	3072	Hello, World!	\N	24		0	6	3
306	\N	3072	Hello, World!	\N	36		0	7	3
306	\N	3072	Hello, World!	\N	24		0	8	3
306	\N	3072	Hello, World!	\N	24		0	9	3
352	\N	3072	0	\N	37		0	01	3
352	\N	2816	0	\N	43		0	02	3
352	\N	3072	0	\N	25		0	03	3
352	\N	3072	0	\N	49		0	04	3
352	\N	3072	0	\N	35		0	05	3
352	\N	3072	0	\N	37		0	06	3
352	\N	3072	0	\N	43		0	07	3
352	\N	3072	0	\N	26		0	08	3
352	\N	3072	0	\N	40		0	09	3
352	\N	2816	0	\N	40		0	10	3
353	\N	3072	33	\N	26		1	01	5
353	\N	2816	195	\N	40		1	02	5
353	\N	3072	33	\N	38		1	03	5
353	\N	3072	195	\N	43		1	04	5
353	\N	3072	52562	\N	27		1	05	5
353	\N	3072	72083685	\N	25		0	06	3
353	\N	3072	1141918033	\N	25		1	07	5
353	\N	3072	2000000005	\N	25		1	08	5
353	\N	3072	4000000000	\N	27		1	09	5
353	\N	2816	240000000000000	\N	26		1	10	5
402	\N	2816	33	\N	36		1	01	5
402	\N	3072	195	\N	25		1	02	5
402	\N	3072	33	\N	43		1	03	5
402	\N	3072	195	\N	26		1	04	5
402	\N	3072	52562	\N	25		1	05	5
402	\N	3072	72083685	\N	25		0	06	3
402	\N	3072	1141918033	\N	24		1	07	5
402	\N	3072	2000000005	\N	24		1	08	5
402	\N	3072	4000000000	\N	25		1	09	5
402	\N	3328	240000000000000	\N	38		1	10	5
403	\N	3072	33	\N	27		1	01	5
403	\N	3072	195	\N	27		1	02	5
403	\N	3072	33	\N	44		1	03	5
403	\N	3072	195	\N	24		1	04	5
403	\N	3072	52562	\N	26		1	05	5
403	\N	3072	72083685	\N	26		0	06	3
403	\N	3328	1141918033	\N	42		1	07	5
403	\N	3072	2000000005	\N	46		1	08	5
403	\N	3072	4000000000	\N	47		1	09	5
403	\N	3072	240000000000000	\N	25		1	10	5
404	\N	3072	33	\N	25		1	01	5
404	\N	3072	195	\N	26		1	02	5
404	\N	3072	33	\N	23		1	03	5
404	\N	3072	195	\N	23		1	04	5
404	\N	3072	52562	\N	25		1	05	5
404	\N	3072	72083685	\N	24		0	06	3
404	\N	3072	1141918033	\N	24		1	07	5
404	\N	2816	2000000005	\N	43		1	08	5
404	\N	2816	4000000000	\N	24		1	09	5
404	\N	3072	240000000000000	\N	24		1	10	5
405	\N	3072	33	\N	27		1	01	5
405	\N	2304	195	\N	27		1	02	5
405	\N	3072	33	\N	24		1	03	5
405	\N	3072	195	\N	23		1	04	5
405	\N	3072	52562	\N	34		1	05	5
405	\N	3072	72083685	\N	24		0	06	3
405	\N	3072	1141918033	\N	24		1	07	5
405	\N	3072	2000000005	\N	24		1	08	5
405	\N	3072	4000000000	\N	35		1	09	5
405	\N	3072	240000000000000	\N	24		1	10	5
406	\N	3072	33	\N	29		1	01	5
406	\N	3072	195	\N	26		1	02	5
406	\N	3072	33	\N	40		1	03	5
406	\N	3328	195	\N	25		1	04	5
406	\N	3072	52562	\N	37		1	05	5
406	\N	3072	72083685	\N	24		0	06	3
406	\N	3072	1141918033	\N	35		1	07	5
406	\N	3072	2000000005	\N	24		1	08	5
406	\N	3328	4000000000	\N	46		1	09	5
406	\N	3328	240000000000000	\N	23		1	10	5
407	\N	3072	33	\N	27		1	01	5
407	\N	3072	195	\N	26		1	02	5
407	\N	3072	33	\N	24		1	03	5
407	\N	2816	195	\N	24		1	04	5
407	\N	2816	52562	\N	24		1	05	5
407	\N	3072	72083685	\N	48		0	06	3
407	\N	3072	1141918033	\N	25		1	07	5
407	\N	2816	2000000005	\N	24		1	08	5
407	\N	3072	4000000000	\N	24		1	09	5
407	\N	3072	240000000000000	\N	24		1	10	5
452	\N	3072	33	\N	28		1	01	5
452	\N	2816	195	\N	48		1	02	5
452	\N	3072	33	\N	26		1	03	5
452	\N	3072	195	\N	26		1	04	5
452	\N	3072	52562	\N	25		1	05	5
452	\N	3072	72083685	\N	26		1	06	5
452	\N	3072	1141918033	\N	25		1	07	5
452	\N	3072	2000000005	\N	24		1	08	5
452	\N	3072	4000000000	\N	44		1	09	5
452	\N	2560	240000000000000	\N	28		1	10	5
502	\N	3072	33	\N	56		1	01	5
502	\N	3072	195	\N	24		1	02	5
502	\N	3072	33	\N	25		1	03	5
502	\N	3072	195	\N	25		1	04	5
502	\N	3072	52562	\N	24		1	05	5
502	\N	3072	72083685	\N	41		1	06	5
502	\N	3072	1141918033	\N	25		1	07	5
502	\N	3072	2000000005	\N	25		1	08	5
502	\N	3072	4000000000	\N	26		1	09	5
502	\N	3072	240000000000000	\N	25		1	10	5
552	\N	3072	31	\N	37		1	01	5
552	\N	3072	1	\N	42		1	02	5
552	\N	3072	21	\N	26		1	03	5
552	\N	3072	54	\N	26		1	04	5
552	\N	3072	20	\N	25		1	05	5
552	\N	3072	26	\N	25		1	06	5
552	\N	3072	3	\N	26		1	07	5
552	\N	3072	23	\N	39		1	08	5
552	\N	2816	39	\N	25		1	09	5
552	\N	3072	22	\N	24		1	10	5
603	\N	3072	23454310	\N	37		0	01	3
603	\N	3072	100	\N	27		0	02	3
603	\N	3072	12351	\N	23		0	03	3
603	\N	3072	100008	\N	26		0	04	3
603	\N	3072	74009	\N	25		0	05	3
603	\N	3072	95327	\N	37		0	06	3
603	\N	3072	100002	\N	25		0	07	3
603	\N	3072	51953	\N	24		0	08	3
603	\N	3072	77772	\N	25		0	09	3
603	\N	2816	86206	\N	24		0	10	3
\.


--
-- Data for Name: tag; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.tag (id, name) FROM stdin;
\.


--
-- Data for Name: task; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.task (id, code, configaddress, inputtemplate, memorylimitmb, outputtemplate, taskscoreparameter, taskscoretype, tasktype, timelimitmillis, title, contest_id, checkertype, taskorder) FROM stdin;
2	IO001	\N	aplusb.i*	128	aplusb.o*	10	0	0	1000	ორი რიცხვის ჯამი	4	0	0
6	us0405102	\N	lkcount.*.in	128	lkcount.*.out	10	0	0	1000	ტბების დათვლა	4	0	0
7	wUqCNSvVvlo3sw==	\N	middle.*.in	64	middle.*.out	10	0	0	128	საშუალო	4	0	0
8	v76rJy8xKY9kUQ==	\N	sumdigit.I*	64	sumdigit.O*	10	0	0	128	ციფრთა ჯამი	4	0	8
1	wh002	\N	threeone.I*	256	threeone.O*	10	0	0	1000	3*N+1	4	0	0
4	IO003	\N	timeinter.i*	128	timeinter.o*	10	0	0	1000	დროის შუალედი	4	0	0
5	rec001	\N	expression.i*	256	expression.o*	10	0	0	1000	გამოსახულების მინიმალური მნიშვნელობა	4	0	0
3	IO002	\N	twodigit.i*	128	twodigit.o*	10	0	0	1000	ორნიშნა რიცხვის ციფრთა ჯამი	4	0	0
\.


--
-- Data for Name: task_metadata_tags; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.task_metadata_tags (task_metadata_id, tag_id) FROM stdin;
\.


--
-- Data for Name: task_results; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.task_results (standings_id, attempts, score, successtime, taskcode, task_code) FROM stdin;
3	1	100	1766342566278	v76rJy8xKY9kUQ==	v76rJy8xKY9kUQ==
4	1	100	1766342566278	v76rJy8xKY9kUQ==	v76rJy8xKY9kUQ==
\.


--
-- Data for Name: task_statements; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.task_statements (task_id, statement, language) FROM stdin;
3	{"title":"ორნიშნა რიცხვის ციფრთა ჯამი","statement":"მოცემულია ორნიშნა მთელი დადებითი a რიცხვი. გამოთვალეთ მისი ციფრების ჯამი.","inputInfo":"ერთი  მთელი ორნიშნა რიცხვი - a (10<=a<=99).","outputInfo":"ერთი მთელი რიცხვი - a-ს ციფრების ჯამი.","notes":null}	1
2	{"title":"ორი რიცხვის ჯამი","statement":"მოცემულია ორი მთელი დადებითი a და b რიცხვი. გამოთვალეთ a+b.\\n\\n","inputInfo":"ერთ სტრიქონში ორი რიცხვი - a და b (0<a,b<=2000000000).","outputInfo":"ერთი მთელი რიცხვი - a და b რიცხვების ჯამი.","notes":null}	1
4	{"title":"დროის ინტერვალი","statement":"გამოთვალეთ სხვაობა დროის ორ მოცემულ მომენტს შორის.","inputInfo":"პირველ სტრიქონში სამი მთელი რიცხვი, რომლებიც აღწერენ დროის პირველ მომენტს ფორმატით: \\"საათი წუთი წამი\\". მეორე სტრიქონში სამი მთელი რიცხვი, რომლებიც აღწერენ დროის მეორე მომენტს იმავე ფორმატით.","outputInfo":"ერთი მთელი რიცხვი - დროის ორ მომენტს შორის გასული წამების რაოდენობა."}	1
6	{"title":"ტბების დათვლა","statement":"მოცემულია მართკუთხა არე ზომით NxM (1<N<200; 1<M<200).  თითოეული უჯრედი ამ არეზე წარმოადგენს ან წყალს (აღნიშვნა - 'W') ან ხმელეთს (აღნიშვნა - '.'). ორი 'W' განეკუთვნება ერთსა და იმავე ტბას, თუ ისინი განლაგებული არიან მეზობელ უჯრედებში ვერტიკალურად, ჰორიზონტალურად ან დიაგონალურად. გამოთვალეთ, რამდენი ტბაა მოცემულ არეზე.","inputInfo":"პირველ სტრიქონში ორი მთელი რიცხვი: N და M. მომდევნო N  სტრიქონიდან თითოეულში M ცალი სიმბოლო - 'W' ან '.'.","outputInfo":"ერთი მთელი რიცხვი - ტბების რაოდენობა.","notes":null}	1
7	{"statement":"ფერმერი ჯონი ათვალიერებს თავის ნახირს, რათა იპოვოს ყველაზე საშუალო წველადობის ძროხა. მას სურს იცოდეს, რამდენ რძეს იძლევა ეს „საშუალო“ ძროხა: ძროხების ნახევარი იძლევიან იმდენს ან მეტს, ვიდრე საშუალო, ნახევარი კი - იმდენს ან ნაკლებს.\\n\\nმოცემულია კენტი რაოდენობის ძროხები N (1 <= N < 10,000) და მათი რძის წარმოება (1..1,000,000), იპოვეთ მიღებული რძის საშუალო რაოდენობა ისე,რომ ძროხების ნახევარი იძლევიან ერთსა და იმავე რაოდენობის რძეს ან მეტს, ხოლო ნახევარი იძლევიან ერთსა და იმავე ან ნაკლებს.","title":"საშუალო","inputInfo":"* სტრიქონი 1: ერთი მთელი რიცხვი N\\n* სტრიქონები 2..N+1: თითოეული სტრიქონი შეიცავს ერთ მთელ რიცხვს, რომელიც წარმოადგენს ერთი ძროხის წველადობის რაოდენობას.","outputInfo":"* სტრიქონი 1: ერთი მთელი რიცხვი, რომელიც წარმოადგენს რძის საშუალო მედიანურ წველადობას."}	1
8	{"title":"ციფრთა ჯამი","statement":"მოცემულია მთელი რიცხვი N (1<N<2,000,000,000). დაწერეთ პროგრამა, რომელიც გამოთვლის მის ციფრთა ჯამს.","inputInfo":"ერთადერთ სტრიქონში მოცემულია ერთი მთელი რიცხვი N.","outputInfo":"ერთადერთ სტრიქონში გამოიტანეთ ერთი მთელი რიცხვი – N–ის ციფრთა ჯამი.","notes":null}	1
1	{"title":"3*N+1","statement":"ნატურალურ რიცხვ N-ისათვის აწარმოებენ შემდეგ ორ ოპერაციას: ა) თუ რიცხვი ლუწია, ჰყოფენ 2-ზე. ბ) თუ რიცხვი კენტია, ამრავლებენ 3-ზე და უმატებენ ერთს. შემდეგ იგივე ოპერაციას იმეორებენ მიღებულ რიცხვზე მანამ, ვიდრე რომელიმე ოპერაციის შემდეგ 1-ს არ მიიღებენ. დაწერეთ პროგრამა, რომელიც პირველ სტრიქონში გამოიტანს ზემოთ ნაჩვენები პროცესის დროს მიღებულ ყველა რიცხვს, ხოლო მეორეში - შესრულებულ  ოპერაციათა რაოდენობას.","inputInfo":"ერთი მთელი რიცხვი N (0<N<30000).","outputInfo":"პირველ სტრიქონში გამოიტანეთ იმ რიცხვთა მიმდევრობა, რომლებიც მიიღებიან ვიდრე საწყისი რიცხვი 1 არ გახდება. მეორე სტრიქონში გამოიტანეთ ერთი მთელი რიცხვი – პირველ სტრიქონში გამოტანილ რიცხვთა რაოდენობა.","notes":null}	1
\.


--
-- Data for Name: task_testcase; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.task_testcase (task_id, testcases_id) FROM stdin;
8	902
8	903
8	904
8	905
8	906
8	907
8	908
8	909
8	910
4	462
4	463
4	464
4	465
4	466
4	467
4	468
4	502
4	470
4	471
8	911
6	702
6	711
6	703
6	704
6	705
6	706
6	707
6	708
6	709
6	710
7	752
7	761
7	753
5	503
5	504
5	505
5	506
5	507
5	508
5	509
5	510
5	511
5	512
7	754
7	755
7	756
7	757
7	758
7	759
7	760
3	452
3	453
3	454
3	455
3	456
3	457
3	458
3	459
3	460
3	461
2	864
2	865
2	866
2	867
2	868
2	869
2	870
2	871
2	872
2	873
\.


--
-- Data for Name: taskmetadata; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.taskmetadata (id, difficultylevel, fullsolutions) FROM stdin;
\.


--
-- Data for Name: testcase; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.testcase (id, inputfileaddress, outputfileaddress, key, taskid, inputsnippet, outputsnippet, publictestcase) FROM stdin;
452	/files/tasks/3/tests/twodigit.i01	/files/tasks/3/tests/twodigit.o01	01	3	16	7	t
454	/files/tasks/3/tests/twodigit.i03	/files/tasks/3/tests/twodigit.o03	03	3	20	2	f
455	/files/tasks/3/tests/twodigit.i04	/files/tasks/3/tests/twodigit.o04	04	3	33	6	f
459	/files/tasks/3/tests/twodigit.i08	/files/tasks/3/tests/twodigit.o08	08	3	44	8	f
460	/files/tasks/3/tests/twodigit.i09	/files/tasks/3/tests/twodigit.o09	09	3	27	9	f
461	/files/tasks/3/tests/twodigit.i10	/files/tasks/3/tests/twodigit.o10	10	3	79	16	f
462	/files/tasks/4/tests/timeinter.i01	/files/tasks/4/tests/timeinter.o01	01	4	7  14   35\r\n17 25 14\r\n	36639	f
463	/files/tasks/4/tests/timeinter.i02	/files/tasks/4/tests/timeinter.o02	02	4	5 59 59\r\n21 17 18	55039	f
464	/files/tasks/4/tests/timeinter.i03	/files/tasks/4/tests/timeinter.o03	03	4	3 10 12\r\n23 18 59	72527	f
465	/files/tasks/4/tests/timeinter.i04	/files/tasks/4/tests/timeinter.o04	04	4	15 15 15\r\n15 15 15	0	f
468	/files/tasks/4/tests/timeinter.i07	/files/tasks/4/tests/timeinter.o07	07	4	4 5 6\r\n21 22 23	62237	f
470	/files/tasks/4/tests/timeinter.i09	/files/tasks/4/tests/timeinter.o09	09	4	2 50 49\r\n23 3 5	72736	f
471	/files/tasks/4/tests/timeinter.i10	/files/tasks/4/tests/timeinter.o10	10	4	9 37 49\r\n22 0 0	44531	f
502	/files/tasks/4/tests/timeinter.i08	/files/tasks/4/tests/timeinter.o08	08	4	6 5 4\r\n23 22 21	62237	f
505	/files/tasks/5/tests/expression.i03	/files/tasks/5/tests/expression.o03	03	5	7+1*2-3*3-2-9\r\n	-224\r\n	f
503	/files/tasks/5/tests/expression.i01	/files/tasks/5/tests/expression.o01	01	5	1+2-3*4\r\n	-9\r\n	f
504	/files/tasks/5/tests/expression.i02	/files/tasks/5/tests/expression.o02	02	5	7+1*2-3*3\r\n	-56\r\n	f
506	/files/tasks/5/tests/expression.i04	/files/tasks/5/tests/expression.o04	04	5	1+1*2-3*3-1-9\r\n	-62\r\n	f
507	/files/tasks/5/tests/expression.i05	/files/tasks/5/tests/expression.o05	05	5	7+1*2-9*3-2-9\r\n	-704\r\n	f
508	/files/tasks/5/tests/expression.i06	/files/tasks/5/tests/expression.o06	06	5	9-9-9-9+1+2-3*4\r\n	-96\r\n	f
509	/files/tasks/5/tests/expression.i07	/files/tasks/5/tests/expression.o07	07	5	9-9*9-9-9+1+2-3*4\r\n	-531\r\n	f
510	/files/tasks/5/tests/expression.i08	/files/tasks/5/tests/expression.o08	08	5	1+2-3*9+5*9+8-9+1+2-3*4\r\n	-2907\r\n	f
511	/files/tasks/5/tests/expression.i09	/files/tasks/5/tests/expression.o09	09	5	1+2-3*9+5-6*9+8-9+1+2-3*4\r\n	-3187\r\n	f
512	/files/tasks/5/tests/expression.i10	/files/tasks/5/tests/expression.o10	10	5	1+2-3*4+5-6*7+8-9+1+2-3*4+5-6\r\n	-4325\r\n	f
456	/files/tasks/3/tests/twodigit.i05	/files/tasks/3/tests/twodigit.o05	05	3	99	18	t
457	/files/tasks/3/tests/twodigit.i06	/files/tasks/3/tests/twodigit.o06	06	3	73	10	t
466	/files/tasks/4/tests/timeinter.i05	/files/tasks/4/tests/timeinter.o05	05	4	15 15 15\r\n15 15 16	1	t
467	/files/tasks/4/tests/timeinter.i06	/files/tasks/4/tests/timeinter.o06	06	4	1 1 1\r\n16 16 16	54915	t
458	/files/tasks/3/tests/twodigit.i07	/files/tasks/3/tests/twodigit.o07	07	3	87	15	t
870	/files/tasks/2/tests/aplusb.i07	/files/tasks/2/tests/aplusb.o07	07	2	899575554 242342479	1141918033	f
871	/files/tasks/2/tests/aplusb.i08	/files/tasks/2/tests/aplusb.o08	08	2	2000000000 5	2000000005	f
872	/files/tasks/2/tests/aplusb.i09	/files/tasks/2/tests/aplusb.o09	09	2	2000000000 2000000000	4000000000	f
873	/files/tasks/2/tests/aplusb.i10	/files/tasks/2/tests/aplusb.o10	10	2	120000000000000 120000000000000	240000000000000	f
453	/files/tasks/3/tests/twodigit.i02	/files/tasks/3/tests/twodigit.o02	02	3	58	13	t
703	/files/tasks/6/tests/lkcount.2.in	/files/tasks/6/tests/lkcount.2.out	2	6	4 4\n....\n....\n....\n....	4	f
704	/files/tasks/6/tests/lkcount.3.in	/files/tasks/6/tests/lkcount.3.out	3	6	1 1\nW	1	f
705	/files/tasks/6/tests/lkcount.4.in	/files/tasks/6/tests/lkcount.4.out	4	6	20 20\nW.W.W.W.W.W.W.W.W.W.\n.W.W.W.W.W.W.W.W.W.W\nW.W.W.W.W.W.W.W.W.W.\n.W.W.W.W.W.W.W.W.W.W\nW.W.W.W.W.W.W.W.W.W.\n.W.W.W.W.W.W.W.W.W.W\nW.W.W.W.W.W.W.W.W.W.\n.W.W.W.W.W.W.W.W.W.W\nW.W.W.W.W.W.W.W.W.W.\n.W.W.W.W.W.W.W.W.W.W\nW.W.W.W.W.W.W.W.W.W.\n.W.W.W.W.W.W.W.W.W.W\nW.W.W.W.W.W.W.W.W.W.\n.W.W.W.W.W.W.W.W.W.W\nW.W.W.W.W.W.W.W.W.W.\n.W.W.W.W.W.W.W.W.W.W\nW.W.W.W.W.W.W.W.W.W.\n.W.W.W.W.W.W.W.W.W.W\nW.W.W.W.W.W.W.W.W.W.\n.W.W.W.W.W.W.W.W.W.W	2	f
706	/files/tasks/6/tests/lkcount.5.in	/files/tasks/6/tests/lkcount.5.out	5	6	12 20\n....WWWW.......WW...\n...WW..WW..WWWWW..W.\n..WWW.....WW..WW..WW\n..WW.....WWWWW.WW.WW\n..W...W....W...W.W..\n..W...W....WWW.W.W..\n........WW.....W.W..\nWWWWWW.W..W..WW..WW.\nWWWWWW..WW..WW..WWWW\n.....W..W..W....W...\nWWW.WWW..W..W..W..WW\n.WW..W...W..W..W.WWW	1	f
702	/files/tasks/6/tests/lkcount.1.in	/files/tasks/6/tests/lkcount.1.out	1	6	10 12\nW........WW.\n.WWW.....WWW\n....WW...WW.\n.........WW.\n.........W..\n..W......W..\n.W.W.....WW.\nW.W.W.....W.\n.W.W......W.\n..W.......W.	1	t
707	/files/tasks/6/tests/lkcount.6.in	/files/tasks/6/tests/lkcount.6.out	6	6	30 30\n...........W.W....W.W......WWW\n..WWWW..WWW.....W.....W....W.W\n..W.......W.......W.W.WW......\n.W...WW.W.......W......WW.....\n..WWW.............W......WW...\n.W...W.......W..W.W.W.....W...\n...WW..W..WWW...WW............\nW........W.......W...W..W.....\n..W.....................WW..W.\n.W.....W.W.....WW.......WW..W.\nW....W..W....W.W.W..W....W....\n.....WWW...W.W.W.WW.....WW.W..\nWW..W....W.W..W..W.W...W......\n.W.WW..W...W....W..W..W.......\n.W..W..W..W..WW..WW.......WWW.\n.....WWW......WW.W..WW..W.W...\nW.W..W..W..W.......WWW.WW.W.W.\n...W...W.WW...W........W..WW..\n....W..W....WW.........W...W.W\nWWW.......W.......W...W.WWW...\nW.......W.....W.WW.W....WW..WW\nW.W..WWW....WW........W.W.....\nW.W..W.WW...W............WW.W.\n.W.W..W...W...WW.WW.W....W.WW.\n.WW.W......W...W...WW..W.W....\n..W...W..W..W.W.WWWW..W..W..W.\nW.W.WW.....WWWW.W...WWW.....W.\nWW..........W.......WWW..WWWW.\nW.WWWW...WWW.W.WW..W.W.W...WW.\n...W..W.....WWWW..W....W..W...	30	f
708	/files/tasks/6/tests/lkcount.7.in	/files/tasks/6/tests/lkcount.7.out	7	6	30 45\nW....WW...WW.W....W.WW.....WWW..WWWWW.WWWW...\n.WWWW..W....W.WW.WWW...W.W....W.WW.W.WW.....W\n.W.W.WW.W....W..W.W...WWW.......WWW........W.\n.W.W......WWW..WW...W....W..W.WW.W.W.....WW..\n...WW..W.WWWW...WW............W.W...W..W.....\n..W...W..W.W.W...WW..W.W......WW.......WW..W.\n.W..W..W.W.....WW.......WW..WWW.W..W..W...WW.\nWWWW.WW.WWW.........WWW...W.WWW.WW....WWW.W..\nWW..W..W.W.WW.W..W.W...W......WW.WW..W...W...\n.W..WWWW.....W..W..W.WWW.W..WW..WW......WWWW.\n.....WWW......WW.WW.WW..W.W.WWW.W..WW.W..W.W.\nW...WWWWWW.WWW..W.W...W.WWWWWWW.......W..WW..\n..W.WW.W....WWW.....W..W...W.WWWW.......W....\nW..W...WWWWW..WW....W..W....WW.WW.W....WW..WW\nWWW..WWW..W.WW...W....W.W.....W.W..W.WW...WW.\n..........WW.W..W.W..WW..W...WWWWW.W..W.W.WW.\n.WW.W......W...W...WW..W.W......W...WW.WWWW.W\n.WWWWW.WW.W..W.W.W.WW.....WWWW.W..WWWWW..W.W.\nWW..........WWW..W..WWW..WWWW.WWWWWWW..WWW.W.\nWW..W.WWW...WW....W..W.....WWWW..W....W..W.W.\n.WWW...W.WW...W.W.......WW..W...W..W....W..W.\nW.WWW.W...W..W...W..W.W...W.	30	f
709	/files/tasks/6/tests/lkcount.8.in	/files/tasks/6/tests/lkcount.8.out	8	6	75 50\nW....WW...WW.W....W.WW.....WWW..WWWWW.WWWW....WWWW\n..W....W.WW.WWW...W.W....W.WW.W.WW.....W.W.W.WW.W.\n...W..W.W...WWW.......WWW........W..W.W......WWW..\nWW...W....W..W.WW.W.W.....WW.....WW..W.WWWW...WW..\n..........W.W...W..W.......W...W..W.W.W...WW..W.W.\n.....WW.......WW..W..W..W..W.W.....WW.......WW..WW\nW.W..W..W...WW.WWWW.WW.WWW.........WWW...W.WWW.WW.\n...WWW.W..WW..W..W.W.WW.W..W.W...W......WW.WW..W..\n.W....W..WWWW.....W..W..W.WWW.W..WW..WW......WWWW.\n.....WWW......WW.WW.WW..W.W.WWW.W..WW.W..W.W.W...W\nWWWWW.WWW..W.W...W.WWWWWWW.......W..WW....W.WW.W..\n..WWW.....W..W...W.WWWW.......W....W..W...WWWWW..W\nW....W..W....WW.WW.W....WW..WWWWW..WWW..W.WW...W..\n..W.W.....W.W..W.WW...WW...........WW.W..W.W..WW..\nW...WWWWW.W..W.W.WW..WW.W......W...W...WW..W.W....\n..W...WW.WWWW.W.WWWWW.WW.W..W.W.W.WW.....WWWW.W..W\nWWWW..W.W.WW..........WWW..W..WWW..WWWW.WWWWWWW..W\nWW.W.WW..W.WWW...WW....W..W.....WWWW..W....W..W.W.\n.WWW...W.WW...W.W.......WW..W...W..W....W..W.W.WWW\n.W...W..W...W..W.W...W..W	75	f
710	/files/tasks/6/tests/lkcount.9.in	/files/tasks/6/tests/lkcount.9.out	9	6	100 100\n...........W.W..............W...WWW...WW............W.........W...............W.W..........W...W..W.\n......W...............W.W....................W......................W.....................W......W..\n...............................W....................................W..W.....W.................W..W.\nW..............W.............................W.W.........W.............W.........................W..\n......W..............W.....W..................WWW......WWW................W.........................\n.W..W.W..........W..................WW.........W.......................W....................W.W.....\n................W........W..WWW.W..W.......W.......................W.................WW......W......\nW....W...........W...WW........W.......W............W..............W.W..W..W..W.W.W.............W...\n......................W.......W....WW...W..WW....W..................W..............W....W...........\n..W......W..............................W....W...W...............W.W...W...........	100	f
711	/files/tasks/6/tests/lkcount.10.in	/files/tasks/6/tests/lkcount.10.out	10	6	100 100\nWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW\nWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW\nWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW\nWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW\nWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW\nWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW\nWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW\nWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW\nWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW\nWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW	1	f
752	/files/tasks/7/tests/middle.1.in	/files/tasks/7/tests/middle.1.out	1	7	5\n2\n4\n1\n3\n5	5	f
753	/files/tasks/7/tests/middle.2.in	/files/tasks/7/tests/middle.2.out	2	7	11\n565219\n927264\n397874\n184393\n727721\n476618\n38811\n592018\n862237\n206244\n821414	11\n565	f
754	/files/tasks/7/tests/middle.3.in	/files/tasks/7/tests/middle.3.out	3	7	33\n800973\n705119\n836525\n575046\n158957\n276052\n638044\n62225\n327375\n959860\n618434\n721462\n387270\n426725\n535350\n714111\n132668\n463792\n775213\n338531\n739500\n923861\n937867\n383037\n277094\n262542\n246329\n255888\n214982\n85963\n300080\n190496\n38411	33\n800	f
755	/files/tasks/7/tests/middle.4.in	/files/tasks/7/tests/middle.4.out	4	7	97\n896495\n866958\n614879\n830368\n762105\n285615\n986887\n605522\n982045\n66126\n324871\n832269\n341794\n634830\n54343\n883739\n185918\n90455\n94910\n688420\n59350\n807554\n731777\n67977\n924046\n741120\n358743\n756323\n125938\n119496\n704505\n883300\n121311\n643813\n423941\n412134\n55353\n388738\n666576\n166718\n279619\n884979\n344756\n337546\n788945\n687789\n686070\n769703\n919177\n442375\n343604\n608770\n870573\n657032\n881889\n597914\n181431\n213882\n87454\n209214\n974213\n977025\n955191\n972691\n12042\n605840\n763143\n596257\n544897\n19943\n709371\n398367\n878406\n580548\n689551\n992572\n177863\n404536\n939643\n250035\n418582\n782372\n154770\n143720\n631354\n164400\n976521\n858416\n767147\n862837\n272876\n386270\n45239\n505850\n727120\n961541\n558396	97\n896	f
902	/files/tasks/8/tests/sumdigit.I01	/files/tasks/8/tests/sumdigit.O01	01	8	234543082	31	f
903	/files/tasks/8/tests/sumdigit.I02	/files/tasks/8/tests/sumdigit.O02	02	8	1000	1	f
904	/files/tasks/8/tests/sumdigit.I03	/files/tasks/8/tests/sumdigit.O03	03	8	123456	21	f
905	/files/tasks/8/tests/sumdigit.I04	/files/tasks/8/tests/sumdigit.O04	04	8	999999	54	f
906	/files/tasks/8/tests/sumdigit.I05	/files/tasks/8/tests/sumdigit.O05	05	8	740027	20	f
907	/files/tasks/8/tests/sumdigit.I06	/files/tasks/8/tests/sumdigit.O06	06	8	953207	26	f
756	/files/tasks/7/tests/middle.5.in	/files/tasks/7/tests/middle.5.out	5	7	185\n407338\n917564\n4544\n20610\n679643\n273995\n517429\n768373\n929540\n208885\n19030\n704837\n319827\n963308\n427617\n624698\n244326\n604494\n745892\n922003\n713374\n442449\n808263\n786684\n156043\n378300\n694483\n690418\n855868\n291774\n63754\n878216\n806119\n157049\n628574\n176944\n821326\n735978\n167137\n241916\n212517\n229381\n907057\n252097\n928388\n417688\n232368\n475570\n503265\n542038\n541172\n330396\n420418\n369138\n675288\n910365\n97280\n654386\n757817\n107201\n893682\n127426\n3389\n695420\n617892\n435090\n242337\n136287\n505629\n404618\n548850\n564180\n637996\n953731\n649185\n931654\n74358\n846060\n265014\n675666\n450455\n845359\n415158\n422583\n540776\n188580\n43328\n147182\n933365\n407390\n387442\n142698\n906484\n564806\n876015\n24965\n237035\n825999\n287507\n491830\n683326\n790697\n589093\n475410\n457961\n223063\n571895\n773695\n94617\n220406\n801033\n921868\n684876\n664916\n822273\n482938\n753418\n155921\n224318\n700663\n802864\n880652\n399646\n491836\n309848\n858539\n420232\n564845\n223793\n200345\n731388\n369983\n607233\n925008\n641720\n25330\n274773\n142569\n264853\n59495\n216046\n163239\n454749\n86480\n321	185\n40	f
757	/files/tasks/7/tests/middle.6.in	/files/tasks/7/tests/middle.6.out	6	7	401\n228760\n703508\n100431\n866593\n62170\n193138\n783986\n358417\n83567\n268885\n376999\n588831\n428036\n295404\n118043\n241629\n470066\n895089\n852118\n280738\n558558\n872414\n334568\n897694\n604921\n544794\n181411\n683810\n358935\n484403\n484465\n356014\n795186\n881611\n295601\n917879\n704709\n299975\n700850\n670474\n729830\n263909\n259871\n174082\n35896\n552652\n465551\n790224\n125513\n57888\n196618\n257653\n966177\n778244\n85335\n345970\n320992\n792907\n323313\n721546\n597796\n590898\n779642\n3418\n521008\n784578\n833860\n598471\n792242\n248822\n36468\n317746\n653164\n984937\n434906\n286795\n862760\n285119\n54163\n86445\n220298\n984028\n837773\n974878\n26681\n34810\n154338\n42592\n105993\n283555\n402234\n98730\n483956\n976031\n24639\n356619\n22372\n190760\n430057\n719493\n172844\n421601\n530090\n821841\n176967\n939498\n314312\n380122\n94463\n246495\n617811\n787501\n779432\n935522\n864638\n954701\n595082\n17507\n532303\n872797\n209049\n344238\n649216\n239229\n148256\n103219\n157121\n246368\n20423\n591807\n120913\n549253\n988537\n85632\n803655\n787898\n477475\n734177\n628119\n387657\n414314\n69162\n178855\n762009\n974752\n63	401\n22	f
758	/files/tasks/7/tests/middle.7.in	/files/tasks/7/tests/middle.7.out	7	7	823\n688317\n607388\n380802\n890953\n738318\n58167\n494885\n821147\n40873\n413299\n35926\n696257\n47875\n215701\n954825\n249488\n931572\n352565\n310751\n299074\n912958\n323258\n382868\n233362\n717905\n570018\n86069\n813313\n781117\n452866\n635895\n282311\n651341\n375657\n441128\n217682\n710475\n345584\n845205\n25246\n125355\n394308\n825137\n620617\n116167\n403755\n839832\n15465\n502053\n890325\n215375\n43067\n815719\n712652\n295005\n827885\n448602\n813167\n864221\n294434\n570704\n393636\n399974\n690321\n379653\n98881\n566560\n889132\n377589\n158400\n184147\n965868\n298298\n158099\n989301\n979485\n347691\n407445\n766459\n34881\n55596\n282855\n496424\n965261\n134250\n595854\n700131\n512727\n807508\n912571\n75769\n780995\n514083\n875537\n273425\n283471\n238134\n313804\n427902\n859283\n911941\n215535\n96778\n529785\n939383\n248854\n12759\n421577\n367588\n300559\n557616\n795884\n189835\n635112\n866316\n57560\n688796\n512874\n756425\n132953\n259236\n29994\n297479\n520023\n307885\n213728\n351094\n545460\n280205\n15597\n154374\n284221\n665053\n357928\n331301\n281995\n674944\n202728\n209672\n699001\n778702\n198804\n120678\n995661\n10234	823\n68	f
759	/files/tasks/7/tests/middle.8.in	/files/tasks/7/tests/middle.8.out	8	7	2207\n547965\n944330\n637580\n305786\n815055\n438786\n510847\n748620\n374506\n671138\n639638\n394491\n464550\n682632\n405385\n105555\n976282\n750269\n255447\n727190\n280028\n810356\n897326\n121823\n411717\n125017\n137932\n533936\n252920\n119020\n527498\n197620\n621187\n707140\n185666\n60892\n767039\n383300\n88953\n977549\n944316\n391934\n48755\n911738\n228599\n380179\n549484\n832377\n54403\n607676\n317928\n529453\n428992\n531163\n806095\n562960\n65510\n985933\n867441\n724517\n820189\n248983\n545270\n533671\n580449\n622939\n655416\n453419\n848398\n350117\n393996\n256096\n616802\n317353\n524482\n630465\n113730\n200190\n363904\n38513\n673364\n115968\n191119\n616213\n401915\n96148\n314505\n692631\n680785\n476467\n663838\n167498\n568695\n757343\n791373\n446325\n217940\n365154\n788346\n56822\n848330\n697022\n131300\n149516\n169444\n647569\n482114\n273018\n404727\n676401\n168778\n580154\n949981\n201250\n491247\n700433\n847034\n405430\n226843\n158171\n527513\n282384\n179359\n440467\n991098\n147487\n330155\n657518\n740150\n811920\n941095\n487188\n363999\n909695\n739374\n569904\n820425\n411736\n322285\n297809\n820966\n456173\n973211\n61	2207\n5	f
760	/files/tasks/7/tests/middle.9.in	/files/tasks/7/tests/middle.9.out	9	7	5389\n859376\n479088\n259552\n569314\n247023\n269352\n583042\n193347\n195089\n34635\n692997\n108630\n528412\n480798\n584563\n498217\n238623\n509346\n332375\n544812\n832726\n172489\n233029\n348782\n44757\n518262\n601320\n65029\n678326\n670071\n38388\n497493\n413756\n944310\n346128\n366189\n781010\n260967\n965725\n129573\n232659\n964394\n33858\n93850\n178331\n316644\n433819\n638034\n808600\n929254\n766675\n624603\n652135\n789370\n66597\n424152\n701310\n44056\n562191\n760212\n999890\n517217\n460235\n657359\n219102\n934308\n858297\n859763\n628209\n861860\n628026\n964698\n181177\n475718\n741089\n300386\n146111\n950835\n715826\n962338\n768781\n716691\n965717\n927565\n120871\n199182\n42792\n390287\n288735\n328759\n442565\n332849\n925956\n845022\n515304\n366326\n455069\n782769\n494928\n379765\n658342\n949422\n986359\n936325\n357998\n367751\n859435\n388004\n431668\n682932\n188396\n665467\n634174\n244460\n96702\n781444\n961987\n749576\n454427\n488554\n478146\n957114\n807276\n807349\n254508\n483572\n560526\n417457\n42157\n214516\n806719\n142074\n372545\n494927\n672102\n989883\n459940\n295664\n203689\n812873\n724018\n737105\n557098\n53603	5389\n8	f
761	/files/tasks/7/tests/middle.10.in	/files/tasks/7/tests/middle.10.out	10	7	9999\n386395\n743191\n128411\n534375\n392726\n712399\n360855\n560817\n267751\n285384\n475609\n181591\n521780\n755903\n173218\n999224\n935741\n883697\n509596\n111396\n239119\n663312\n433393\n74275\n938452\n176922\n273965\n74724\n974341\n125948\n419622\n926001\n440312\n626946\n589425\n706381\n573041\n424686\n471749\n26725\n148565\n906353\n52785\n667752\n24775\n423082\n989831\n61906\n41152\n56540\n777591\n215695\n710176\n368025\n918614\n849023\n951813\n984737\n627806\n956696\n577019\n402718\n889684\n628250\n815299\n798998\n638263\n765082\n105243\n268106\n963549\n544044\n778894\n942104\n164307\n845026\n527414\n271379\n90737\n903385\n446029\n78544\n57879\n698390\n533774\n233998\n993937\n844231\n233593\n213083\n828452\n323817\n609966\n796702\n162522\n376867\n196599\n575165\n314616\n317323\n230757\n155275\n46251\n371175\n702194\n390249\n865709\n670518\n364476\n28343\n336757\n954467\n19581\n777368\n412822\n969888\n130632\n417333\n433393\n957345\n233218\n661635\n94602\n388995\n997062\n508678\n804278\n499183\n883761\n771029\n295666\n335199\n603222\n889718\n973915\n540162\n394508\n498776\n54093\n863499\n152614\n102236\n295477\n243398\n650	9999\n3	f
864	/files/tasks/2/tests/aplusb.i01	/files/tasks/2/tests/aplusb.o01	01	2	16 17	33	f
865	/files/tasks/2/tests/aplusb.i02	/files/tasks/2/tests/aplusb.o02	02	2	137 58	195	f
866	/files/tasks/2/tests/aplusb.i03	/files/tasks/2/tests/aplusb.o03	03	2	16 17	33	f
867	/files/tasks/2/tests/aplusb.i04	/files/tasks/2/tests/aplusb.o04	04	2	137 58	195	f
868	/files/tasks/2/tests/aplusb.i05	/files/tasks/2/tests/aplusb.o05	05	2	25194 27368	52562	f
869	/files/tasks/2/tests/aplusb.i06	/files/tasks/2/tests/aplusb.o06	06	2	13741343 58342342	72083685	f
908	/files/tasks/8/tests/sumdigit.I07	/files/tasks/8/tests/sumdigit.O07	07	8	1000002	3	f
909	/files/tasks/8/tests/sumdigit.I08	/files/tasks/8/tests/sumdigit.O08	08	8	519512	23	f
910	/files/tasks/8/tests/sumdigit.I09	/files/tasks/8/tests/sumdigit.O09	09	8	777666	39	f
911	/files/tasks/8/tests/sumdigit.I10	/files/tasks/8/tests/sumdigit.O10	10	8	862024	22	f
\.


--
-- Data for Name: worker; Type: TABLE DATA; Schema: public; Owner: root
--

COPY public.worker (id, jobs_processed, last_heartbeat, start_time, status, version, worker_id) FROM stdin;
8	12	2025-12-22 22:38:19.134	2025-12-22 14:41:10.568	ONLINE	956	main-8
11	0	2025-12-22 22:38:28.55	2025-12-22 14:41:22.999	ONLINE	954	main-9
\.


--
-- Name: contest_seq; Type: SEQUENCE SET; Schema: public; Owner: root
--

SELECT pg_catalog.setval('public.contest_seq', 5, true);


--
-- Name: contestant_result_id_seq; Type: SEQUENCE SET; Schema: public; Owner: root
--

SELECT pg_catalog.setval('public.contestant_result_id_seq', 8, true);


--
-- Name: post_id_seq; Type: SEQUENCE SET; Schema: public; Owner: root
--

SELECT pg_catalog.setval('public.post_id_seq', 1, false);


--
-- Name: postcomment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: root
--

SELECT pg_catalog.setval('public.postcomment_id_seq', 1, false);


--
-- Name: principal_id_seq; Type: SEQUENCE SET; Schema: public; Owner: root
--

SELECT pg_catalog.setval('public.principal_id_seq', 5, true);


--
-- Name: recoverpassword_id_seq; Type: SEQUENCE SET; Schema: public; Owner: root
--

SELECT pg_catalog.setval('public.recoverpassword_id_seq', 1, false);


--
-- Name: solved_problem_id_seq; Type: SEQUENCE SET; Schema: public; Owner: root
--

SELECT pg_catalog.setval('public.solved_problem_id_seq', 1, false);


--
-- Name: submission_seq; Type: SEQUENCE SET; Schema: public; Owner: root
--

SELECT pg_catalog.setval('public.submission_seq', 651, true);


--
-- Name: tag_id_seq; Type: SEQUENCE SET; Schema: public; Owner: root
--

SELECT pg_catalog.setval('public.tag_id_seq', 1, false);


--
-- Name: task_id_seq; Type: SEQUENCE SET; Schema: public; Owner: root
--

SELECT pg_catalog.setval('public.task_id_seq', 8, true);


--
-- Name: testcase_seq; Type: SEQUENCE SET; Schema: public; Owner: root
--

SELECT pg_catalog.setval('public.testcase_seq', 1001, true);


--
-- Name: worker_id_seq; Type: SEQUENCE SET; Schema: public; Owner: root
--

SELECT pg_catalog.setval('public.worker_id_seq', 11, true);


--
-- Name: contest contest_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest
    ADD CONSTRAINT contest_pkey PRIMARY KEY (id);


--
-- Name: contest_room_contest contest_room_contest_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_room_contest
    ADD CONSTRAINT contest_room_contest_pkey PRIMARY KEY (contest_room_id, contests_id);


--
-- Name: contest_room contest_room_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_room
    ADD CONSTRAINT contest_room_pkey PRIMARY KEY (id);


--
-- Name: contest_room_principal contest_room_principal_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_room_principal
    ADD CONSTRAINT contest_room_principal_pkey PRIMARY KEY (contest_room_id, teachers_id);


--
-- Name: contest_room_user contest_room_user_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_room_user
    ADD CONSTRAINT contest_room_user_pkey PRIMARY KEY (contest_room_id, teachers_id);


--
-- Name: contestant_result contestant_result_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contestant_result
    ADD CONSTRAINT contestant_result_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: post post_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.post
    ADD CONSTRAINT post_pkey PRIMARY KEY (id);


--
-- Name: postcomment postcomment_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.postcomment
    ADD CONSTRAINT postcomment_pkey PRIMARY KEY (id);


--
-- Name: principal principal_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.principal
    ADD CONSTRAINT principal_pkey PRIMARY KEY (id);


--
-- Name: recoverpassword recoverpassword_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.recoverpassword
    ADD CONSTRAINT recoverpassword_pkey PRIMARY KEY (id);


--
-- Name: solved_problem solved_problem_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.solved_problem
    ADD CONSTRAINT solved_problem_pkey PRIMARY KEY (id);


--
-- Name: submission submission_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.submission
    ADD CONSTRAINT submission_pkey PRIMARY KEY (id);


--
-- Name: tag tag_name_key; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.tag
    ADD CONSTRAINT tag_name_key UNIQUE (name);


--
-- Name: tag tag_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.tag
    ADD CONSTRAINT tag_pkey PRIMARY KEY (id);


--
-- Name: task_metadata_tags task_metadata_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.task_metadata_tags
    ADD CONSTRAINT task_metadata_tags_pkey PRIMARY KEY (task_metadata_id, tag_id);


--
-- Name: task task_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.task
    ADD CONSTRAINT task_pkey PRIMARY KEY (id);


--
-- Name: task_results task_results_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.task_results
    ADD CONSTRAINT task_results_pkey PRIMARY KEY (standings_id, task_code);


--
-- Name: task_statements task_statements_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.task_statements
    ADD CONSTRAINT task_statements_pkey PRIMARY KEY (task_id, language);


--
-- Name: taskmetadata taskmetadata_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.taskmetadata
    ADD CONSTRAINT taskmetadata_pkey PRIMARY KEY (id);


--
-- Name: testcase testcase_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.testcase
    ADD CONSTRAINT testcase_pkey PRIMARY KEY (id);


--
-- Name: contest_principal uk29lmx4bsu9ksui4ubw0ujy1u7; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_principal
    ADD CONSTRAINT uk29lmx4bsu9ksui4ubw0ujy1u7 UNIQUE (participants_id);


--
-- Name: worker uk40w0nd18gooqfq5faqdn9e6du; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.worker
    ADD CONSTRAINT uk40w0nd18gooqfq5faqdn9e6du UNIQUE (worker_id);


--
-- Name: contest uk7uabp24970ktvntu4ajw95wgp; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest
    ADD CONSTRAINT uk7uabp24970ktvntu4ajw95wgp UNIQUE (name);


--
-- Name: contest_user uk858jou4sunv7totsvrleq56qf; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_user
    ADD CONSTRAINT uk858jou4sunv7totsvrleq56qf UNIQUE (participants_id);


--
-- Name: contest_room_contest ukcqvp7tbnxyjr8no7l33vlth7j; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_room_contest
    ADD CONSTRAINT ukcqvp7tbnxyjr8no7l33vlth7j UNIQUE (contests_id);


--
-- Name: contest_task uket27nccxkp3jahj24c5bss5pa; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_task
    ADD CONSTRAINT uket27nccxkp3jahj24c5bss5pa UNIQUE (tasks_id);


--
-- Name: task ukkdgj4q8xbwxhymgapcqefiseu; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.task
    ADD CONSTRAINT ukkdgj4q8xbwxhymgapcqefiseu UNIQUE (code);


--
-- Name: contest_room_principal ukkluqi8tvskml4q0c6k1p1e1jm; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_room_principal
    ADD CONSTRAINT ukkluqi8tvskml4q0c6k1p1e1jm UNIQUE (teachers_id);


--
-- Name: contest_room_principal ukpdgk23tx381xtj4bmiyp8t2qy; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_room_principal
    ADD CONSTRAINT ukpdgk23tx381xtj4bmiyp8t2qy UNIQUE (participants_id);


--
-- Name: contest_room ukpl7qckft3tgk1kt1di98tvq9k; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_room
    ADD CONSTRAINT ukpl7qckft3tgk1kt1di98tvq9k UNIQUE (name);


--
-- Name: contest_room_user ukpr9pfcts23w3qljw7gigow0r7; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_room_user
    ADD CONSTRAINT ukpr9pfcts23w3qljw7gigow0r7 UNIQUE (teachers_id);


--
-- Name: task_testcase ukqud01hbqhklrym5tgnm5v247u; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.task_testcase
    ADD CONSTRAINT ukqud01hbqhklrym5tgnm5v247u UNIQUE (testcases_id);


--
-- Name: principal ukrsjolrak8rcat0953ac4eiab5; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.principal
    ADD CONSTRAINT ukrsjolrak8rcat0953ac4eiab5 UNIQUE (username);


--
-- Name: solved_problem ukse70wji9no79e1dmmym4xh0tb; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.solved_problem
    ADD CONSTRAINT ukse70wji9no79e1dmmym4xh0tb UNIQUE (user_id, task_id);


--
-- Name: contest_room_user uksukcm20kox12mgb1ft8nsxxqc; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_room_user
    ADD CONSTRAINT uksukcm20kox12mgb1ft8nsxxqc UNIQUE (participants_id);


--
-- Name: worker worker_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.worker
    ADD CONSTRAINT worker_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: root
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_task_metadata_tags_tag_id; Type: INDEX; Schema: public; Owner: root
--

CREATE INDEX idx_task_metadata_tags_tag_id ON public.task_metadata_tags USING btree (tag_id);


--
-- Name: idx_total_score; Type: INDEX; Schema: public; Owner: root
--

CREATE INDEX idx_total_score ON public.contestant_result USING btree (totalscore);


--
-- Name: submission fk1g5xv88nw5mmhgrwc29j9bryu; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.submission
    ADD CONSTRAINT fk1g5xv88nw5mmhgrwc29j9bryu FOREIGN KEY (task_id) REFERENCES public.task(id);


--
-- Name: submission_submissiontestresults fk1yqnyr541lrc3sw26qnopemey; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.submission_submissiontestresults
    ADD CONSTRAINT fk1yqnyr541lrc3sw26qnopemey FOREIGN KEY (submission_id) REFERENCES public.submission(id);


--
-- Name: task_statements fk3jcm8f4dhutr1n29e80g83bha; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.task_statements
    ADD CONSTRAINT fk3jcm8f4dhutr1n29e80g83bha FOREIGN KEY (task_id) REFERENCES public.task(id);


--
-- Name: contest_principal fk4b98vre8n1c1ggxglkkdxi5mt; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_principal
    ADD CONSTRAINT fk4b98vre8n1c1ggxglkkdxi5mt FOREIGN KEY (contest_id) REFERENCES public.contest(id);


--
-- Name: contest_principal fk6i6mr8uedvapflucwtg1hccye; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_principal
    ADD CONSTRAINT fk6i6mr8uedvapflucwtg1hccye FOREIGN KEY (participants_id) REFERENCES public.principal(id);


--
-- Name: contestant_result fk6lckv5ac8pg73lxuttlwpdy6g; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contestant_result
    ADD CONSTRAINT fk6lckv5ac8pg73lxuttlwpdy6g FOREIGN KEY (upsolving_contest_id) REFERENCES public.contest(id);


--
-- Name: contest_room_contest fk8fjn8tphbbdo995fyn9g6rv0y; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_room_contest
    ADD CONSTRAINT fk8fjn8tphbbdo995fyn9g6rv0y FOREIGN KEY (contests_id) REFERENCES public.contest(id);


--
-- Name: post fk91s5fqg3rb7xk6320dcttx55m; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.post
    ADD CONSTRAINT fk91s5fqg3rb7xk6320dcttx55m FOREIGN KEY (author_id) REFERENCES public.principal(id);


--
-- Name: contest_task fk9ojrb2x0t5r8jqnucoln93dke; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_task
    ADD CONSTRAINT fk9ojrb2x0t5r8jqnucoln93dke FOREIGN KEY (tasks_id) REFERENCES public.task(id);


--
-- Name: task_metadata_tags fk_task_metadata_tags_metadata; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.task_metadata_tags
    ADD CONSTRAINT fk_task_metadata_tags_metadata FOREIGN KEY (task_metadata_id) REFERENCES public.taskmetadata(id) ON DELETE CASCADE;


--
-- Name: task_metadata_tags fk_task_metadata_tags_tag; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.task_metadata_tags
    ADD CONSTRAINT fk_task_metadata_tags_tag FOREIGN KEY (tag_id) REFERENCES public.tag(id) ON DELETE CASCADE;


--
-- Name: taskmetadata fk_taskmetadata_task; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.taskmetadata
    ADD CONSTRAINT fk_taskmetadata_task FOREIGN KEY (id) REFERENCES public.task(id) ON DELETE CASCADE;


--
-- Name: contest_room_principal fka8m6w7bcfiw6tkvawyujdjexn; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_room_principal
    ADD CONSTRAINT fka8m6w7bcfiw6tkvawyujdjexn FOREIGN KEY (participants_id) REFERENCES public.principal(id);


--
-- Name: solved_problem fka9ephbfv17cirldru8dmch4x3; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.solved_problem
    ADD CONSTRAINT fka9ephbfv17cirldru8dmch4x3 FOREIGN KEY (user_id) REFERENCES public.principal(id);


--
-- Name: contest_room_contest fkb2y7ui655cjkfd6fvpi72rg8l; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_room_contest
    ADD CONSTRAINT fkb2y7ui655cjkfd6fvpi72rg8l FOREIGN KEY (contest_room_id) REFERENCES public.contest_room(id);


--
-- Name: task_results fkbh3v0jl4gan6ciy7v1ic5svxv; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.task_results
    ADD CONSTRAINT fkbh3v0jl4gan6ciy7v1ic5svxv FOREIGN KEY (standings_id) REFERENCES public.contestant_result(id);


--
-- Name: contestant_result fkc67er3m2agsflrmsse2jhl0bu; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contestant_result
    ADD CONSTRAINT fkc67er3m2agsflrmsse2jhl0bu FOREIGN KEY (contest_id) REFERENCES public.contest(id);


--
-- Name: contest_room_principal fkd4mcidlt5jxd1vcj7ueesxj5c; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_room_principal
    ADD CONSTRAINT fkd4mcidlt5jxd1vcj7ueesxj5c FOREIGN KEY (contest_room_id) REFERENCES public.contest_room(id);


--
-- Name: contest_task fkfuocqjrtu1uy2nh1rlu6iq2ui; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_task
    ADD CONSTRAINT fkfuocqjrtu1uy2nh1rlu6iq2ui FOREIGN KEY (contest_id) REFERENCES public.contest(id);


--
-- Name: task_testcase fkg3hhbeu0s6eood8o6u67b9odx; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.task_testcase
    ADD CONSTRAINT fkg3hhbeu0s6eood8o6u67b9odx FOREIGN KEY (testcases_id) REFERENCES public.testcase(id);


--
-- Name: task fklfsq4vfeyb8al1j8rcwyuwvtx; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.task
    ADD CONSTRAINT fklfsq4vfeyb8al1j8rcwyuwvtx FOREIGN KEY (contest_id) REFERENCES public.contest(id);


--
-- Name: contest_room_principal fklvx7jkcx5nf1og11tuydhxu8i; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_room_principal
    ADD CONSTRAINT fklvx7jkcx5nf1og11tuydhxu8i FOREIGN KEY (teachers_id) REFERENCES public.principal(id);


--
-- Name: task_testcase fkn1doy2mnujt5gi1284gb3licf; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.task_testcase
    ADD CONSTRAINT fkn1doy2mnujt5gi1284gb3licf FOREIGN KEY (task_id) REFERENCES public.task(id);


--
-- Name: contest_user fko4db13hpjuxyqhni4tlcbl5mi; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_user
    ADD CONSTRAINT fko4db13hpjuxyqhni4tlcbl5mi FOREIGN KEY (contest_id) REFERENCES public.contest(id);


--
-- Name: contest_room_user fkomcdo9tj0i803i2glg0a8p6tm; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.contest_room_user
    ADD CONSTRAINT fkomcdo9tj0i803i2glg0a8p6tm FOREIGN KEY (contest_room_id) REFERENCES public.contest_room(id);


--
-- Name: submission fkp29cctijnkrykua3c32kidfmr; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.submission
    ADD CONSTRAINT fkp29cctijnkrykua3c32kidfmr FOREIGN KEY (contest_id) REFERENCES public.contest(id);


--
-- Name: submission fkq51iufnemdkveusf03eg5w5pi; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.submission
    ADD CONSTRAINT fkq51iufnemdkveusf03eg5w5pi FOREIGN KEY (user_id) REFERENCES public.principal(id);


--
-- Name: solved_problem fktgvtsjo82fitr61ynmd55knjn; Type: FK CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.solved_problem
    ADD CONSTRAINT fktgvtsjo82fitr61ynmd55knjn FOREIGN KEY (task_id) REFERENCES public.task(id);


--
-- PostgreSQL database dump complete
--

\unrestrict xuSBYJ78WRdR9EUbC68aDkEBikA9CXP0IzsMAR5bYkQme5k7qgi1WZoaMvWYZL3

--
-- Database "postgres" dump
--

\connect postgres

--
-- PostgreSQL database dump
--

\restrict GK2RT8TzFYitusNcWrpTGu05S3bYxVqIVoOFkQhFqojvo4cFwuttcUGO3kpiMSA

-- Dumped from database version 17.8 (Debian 17.8-1.pgdg13+1)
-- Dumped by pg_dump version 17.8 (Debian 17.8-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- PostgreSQL database dump complete
--

\unrestrict GK2RT8TzFYitusNcWrpTGu05S3bYxVqIVoOFkQhFqojvo4cFwuttcUGO3kpiMSA

--
-- PostgreSQL database cluster dump complete
--

