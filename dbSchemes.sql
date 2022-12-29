
CREATE DATABASE news;

-- POSTGRES_PASSWORD=pass -d -p 5432:5432 postgres
-- psql --user postgres -c 'create database news;'
 
-- CREATE TABLE IF NOT EXISTS public."raw_news"
-- (
-- 	"id"   BIGSERIAL PRIMARY KEY,
--     "title" varchar(500) NOT NULL,
--     "url" varchar(500) NOT NULL,
--     "published_datetime" timestamp with time zone,
--     "categories" char(500)    
-- )

DROP TABLE IF EXISTS public."news";
CREATE TABLE IF NOT EXISTS public."news"
(
	"news_hash" varchar(500) PRIMARY KEY NOT NULL,  
    "title" varchar(500) NOT NULL,
    "url" varchar(500) NOT NULL,
    "published_datetime" timestamp with time zone,
    "canonical_category" varchar(500) NOT NULL
)

DROP TABLE IF EXISTS public."uncategorized_news";
CREATE TABLE IF NOT EXISTS public."uncategorized_news"
(
	"news_hash" varchar(500) PRIMARY KEY NOT NULL,  
    "title" varchar(500) NOT NULL,
    "url" varchar(500) NOT NULL,
    "published_datetime" timestamp with time zone,
    "categories" varchar(500) NOT NULL
)

DROP TABLE IF EXISTS public."category_mapping";
CREATE TABLE IF NOT EXISTS public."category_mapping"
(
	"raw_category_hash" varchar(500) PRIMARY KEY NOT NULL,
	"raw_category" varchar(500) NOT NULL,
	"canonical_category" varchar(500) NOT NULL
)


-- Datamart relations
DROP TABLE IF EXISTS public."category_mapping"
CREATE TABLE IF NOT EXISTS public."category_mapping"
(
	 canonical_category varchar(500) NOT NULL REFERENCES public."category_mapping" (canonical_category),
	news_total int DEFAULT 0 NOT NULL,
	news_average_per_day int DEFAULT 0 NOT NULL,
	news_total int DEFAULT 0 NOT NULL,
	news_total int DEFAULT 0 NOT NULL,
	news_total_last_day int DEFAULT 0 NOT NULL,
	max_news_day date NOT NULL,
)


