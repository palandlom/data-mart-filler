-- POSTGRES_PASSWORD=pass -d -p 5432:5432 postgres
-- psql --user postgres -c 'create database news;'
DROP TABLE IF EXISTS public."news";

CREATE TABLE IF NOT EXISTS public."news"
(
	"news_hash" varchar(500) PRIMARY KEY NOT NULL,  
    "title" varchar(500) NOT NULL,
    "url" varchar(500) NOT NULL,
    "published_datetime" timestamp with time zone,
    "canonical_category" varchar(500) NOT NULL
);

DROP TABLE IF EXISTS public."uncategorized_news";

CREATE TABLE IF NOT EXISTS public."uncategorized_news"
(
	"news_hash" varchar(500) PRIMARY KEY NOT NULL,  
    "title" varchar(500) NOT NULL,
    "url" varchar(500) NOT NULL,
    "published_datetime" timestamp with time zone,
    "categories" varchar(500) NOT NULL
);

DROP TABLE IF EXISTS public."category_mapping";

CREATE TABLE IF NOT EXISTS public."category_mapping"
(
	"raw_category_hash" varchar(500) PRIMARY KEY NOT NULL,
	"raw_category" varchar(500) NOT NULL,
	"canonical_category" varchar(500) NOT NULL
);


-- Add base mapping 
INSERT INTO public.category_mapping (raw_category_hash,raw_category,canonical_category) VALUES
     ('631d306c4c0a6dadc6518436c8ede7dc','ситуация с криптовалютой в россии','экономика'),
     ('59f94c979aa30e68565a28dd591f7ef3','экономика','экономика'),
     ('f6e9b0a8466aae711991d8d6e6215263','бизнес','экономика'),
     ('a7c1ae34b067a8ec4c4cf991c6b77769','экономика и бизнес','экономика'),
     ('fd50ecf4e78208ad12e6b9580c6e9c98','бизнес / транспорт','экономика'),
     ('703f08aa3d2c45d9383297e83ef66926','недвижимость','экономика'),
     ('fa2739b64157a5fd0497723a5ef0e5ca','финансы','экономика'),
     ('69289b99d7d742bf74855b0374b6d6b1','малый бизнес','экономика'),
     ('6ad77b2626487425e1ff1d50625db108','зимние виды спорта','спорт'),
     ('73d436f8a2bc2345299406f7353a5ee2','бокс/мма','спорт');
INSERT INTO public.category_mapping (raw_category_hash,raw_category,canonical_category) VALUES
     ('fa51cf603893b44d9f110404410a1fd8','интернет и сми','спорт'),
     ('c0e1278f1ed860abf51fa94507086b4a','спорт','спорт'),
     ('542176d36a48dd5d08413b3b103be82d','чемпионат россии по фигурному катанию','спорт'),
     ('79d674db6259e1c105de46dd91513305','хоккей','спорт'),
     ('d09944e83d8679e6e8754c057d2f26ab','сборные по хоккею','спорт'),
     ('23ecc72f43a3989d68ab0b498b47ce14','шахматы','спорт'),
     ('b092b356be7d5df387516d54f76e6376','туризм и отдых','спорт'),
     ('40e4f0871dae9945919adf4554720d3a','фигурное катание','спорт'),
     ('8c2fcfc43d0a1b1cb9d464ce923988a8','футбол в россии','спорт'),
     ('e3e54dc343c3774bc8cbe84761ca72f8','футбол','спорт');
INSERT INTO public.category_mapping (raw_category_hash,raw_category,canonical_category) VALUES
     ('6365911203845efb9bae723a32214b09','туризм в россии','спорт'),
     ('01c81627b5d30123c1e1b47c49657cb6','шахматные турниры','спорт'),
     ('6c944471041b8843f75904043a213dd2','массовый спорт','спорт'),
     ('c5e55bb2b3ef034b56dbceeaa05a8f16','зимние олимпийские виды спорта','спорт'),
     ('d736ff48b698c49e66690025847ef443','путешествия','путешествия'),
     ('2cdb245da9e2634f011c07e99d451f81','69-я параллель','политика'),
     ('e0f3d86d2dc537bb95b2be78fe03294c','военная операция на украине','политика'),
     ('a0ead327c5cae0ebbd9952f814b83a78','россия','политика'),
     ('d146b6146355db03655481c8f287b0fc','северо-запад','политика'),
     ('7571db0e60e4ae2a490b44cd89bb6d47','бывший ссср','политика');
INSERT INTO public.category_mapping (raw_category_hash,raw_category,canonical_category) VALUES
     ('77ad60bba1e372ad1e37f010fe76ca24','новости урала','политика'),
     ('7993cc3e44860555b3d834d6f727ba1c','ситуация на корейском полуострове','политика'),
     ('056d3f29f7a1b116e5d823e9b34b4505','повреждение корабля "союз мс-22"','политика'),
     ('7e2e2c2ea5acf876bc9dd87dd9001776','новости партнеров','политика'),
     ('b80871273f1d83e03bb7476397187389','мир','политика'),
     ('146501d56109d2c8687005904081ef5c','международная панорама','политика'),
     ('e1fa92f90999203bd9ed511686489c7b','политика','политика'),
     ('180c3ad8e13789fdeda36c4cf4c1b212','опросы общественного мнения','политика'),
     ('b3071f6dbe7337f2c902eb2d816330d7','моя страна','политика'),
     ('5684da494d1c25eea98b9309438df4a4','внутренняя политика','политика');
INSERT INTO public.category_mapping (raw_category_hash,raw_category,canonical_category) VALUES
     ('eb537858cc3c6bea8492827621e6d7a8','нижегородская область','политика'),
     ('44ff48e3a9c99505772bbe80697809d8','эскалация в косове','политика'),
     ('f60484076500d530e28e82903fb1fb65','внешняя политика','политика'),
     ('187ee7fc33decb82f6a302563d99a134','национальные проекты','политика'),
     ('18c1ba0c98fae707bc13131ee1bbb35e','частичная мобилизация в россии','политика'),
     ('e7e65062faf4d95361bc37e4f932bda2','задержание саакашвили в грузии','политика'),
     ('f2656fc16bf007ccf246240d85b9ef2d','арктика','политика'),
     ('3068609d82e17f0219029ae5188777e9','технологические стартапы в вузах рф','общество'),
     ('74105a52d1bfcfbe367ff37c9af134e0','силовые структуры','общество'),
     ('45fcd4c2432d1de993eca283752377bd','экология','общество');
INSERT INTO public.category_mapping (raw_category_hash,raw_category,canonical_category) VALUES
     ('d1e49ba9b2037b2ba4fcffa53336730a','религия','общество'),
     ('249300fc10b9397b3cb5fb6eb8e15409','среда обитания','общество'),
     ('6001cf4fec638315c2e1e1787508edd5','из жизни','общество'),
     ('a21b6711d01ce7da3e614b32ea8e9e44','забота о себе','общество'),
     ('b247b3e39e56d593ff843a354edf4436','криминал','общество'),
     ('01e60d997cacd1afda790a43c39cf0f3','ценности','общество'),
     ('80655ee85dae0be6adadb0ceefca9ec4','готов к труду и обороне','общество'),
     ('d5a66c147eb119fc249600b9a9dffb55','пожар в социальном приюте в кемерове','общество'),
     ('cc83494a2db0a6fa7ac30d9642e60567','происшествия','общество'),
     ('d0372d5eeefc31362303bc1bdd5cc0f0','образование','общество');
INSERT INTO public.category_mapping (raw_category_hash,raw_category,canonical_category) VALUES
     ('b9f1fbd376b1aceff50e8e6d2630feeb','армия и опк','общество'),
     ('37818ba1502e71917c96c6cdefd2522c','общество','общество'),
     ('6d02869e9a97b98b11ac0f3ca5549297','наука и техника','наука и техника'),
     ('dc596a59b3eedb75e1d1d1912f16222a','технологии','наука и техника'),
     ('ae00905f1730f6b60c2776ea2483c0dc','апк','наука и техника'),
     ('1fda27b32faff50f12a9bfd93aeff10d','электроэнергетика','наука и техника'),
     ('dd2dbadaa92cd730db43591930c388f2','безопасные качественные дороги','наука и техника'),
     ('1425758d25b63cdcc5b9ae50d1604ca3','медиа','медиа'),
     ('b5f5034946ad9acf599b39f207927996','музыка','культура'),
     ('6fbc59957b2dc2a209ae27480bfa78e5','культура','культура');
INSERT INTO public.category_mapping (raw_category_hash,raw_category,canonical_category) VALUES
     ('4b84fb1d90cb204d84b790bf6b6aa79d','кино','культура'),
     ('c3a51b621da339c2c82da43c5562a299','космос','космос');

