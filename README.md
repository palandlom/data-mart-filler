# Data-mart-filler

# General description

All phases of ETL-proccess to form/update data mart are performed by the application `filler` which is packed in jar-file. 

`filler` can be executed from `./app` directory in the following modes:
* `all` - multithread downloadind of the news from RSS-resourses to `./app/newsFiles/X_news` file - command `java -cp "./*" main.Main all`
* `prep` - categorize news from the new files appeared in `./app/newsFiles/` forder, insert news into filler-db tables `news`, `uncategorized_news` and add new "unmapped" categories to filler-db.`category_mapping` table as non-mapped ones
 file - command `java -cp "./*" main.Main prep`
* `uncat` - try to categorize uncategorized news from filler-db.`uncategorized_news` by mapping from the filler-db.`category_mapping` table
 file - command `java -cp "./*" main.Main uncat`
* `anl` - analyse filler-db.`news` table with recreation data-mart tables: `category_stat`, `category_to_source_stat`, `days_to_category_stat` - command `java -cp "./*" main.Main anl`

`filler`-jar should be placed in `./app` directory with required spark-jars.
`./app` directory will be bind to airflow-worker-container and `filler`-jar will be called by airflow-DAG `update-news-datamart`.
The Airflow-DAG [`update-news-datamart`](https://github.com/palandlom/data-mart-filler/blob/main/vol/dags/update-news-datamart.py) contains tasks for each of noted `filler`-modes. The DAG is started every day on 00:30.

The results of each run of the DAG:
- new file with news in `./app/newsFiles/`
- new deduplicated and categorised news in table `news`
- new deduplicated, uncategorised news in table `uncategorized_news` 
- created/updated tables `category_stat`, `category_to_source_stat`, `days_to_category_stat`

To access the filler-db use following account data:
```env
host 127.0.0.1
port 5432
user postgres
pass pass
dbName news
```
Other docs (presentation, schemes etc.) can be reseived by [data-mart-filler-files](https://drive.google.com/drive/folders/1LdsLgEfWh0A_FrS42kdk_shj8XcpNPRf?usp=share_link)

# Deploy 

## Get spark jars 
Sparks jars from `./jars` directory of (https://dlcdn.apache.org/spark/spark-3.2.3/spark-3.2.3-bin-hadoop3.2-scala2.13.tgz) should be placed in `./app` folder.
You can execute `./getSpark.sh` script for this, it will download and extract jars to `./app`

## Build filler-jar
`Filler` source code (`scala sbt-project`) is in `./filler` folder.
Current version of filler can be downloaded to `./app` from - [link](https://drive.google.com/drive/folders/1LdsLgEfWh0A_FrS42kdk_shj8XcpNPRf?usp=sharing) or can be build by:
```bash
  cd ./filler
  sbt assebly
```
Jar will be in `/filler/target/scala-2.13/filler_2.13-0.1.0-SNAPSHOT.jar` copy it to `./app`:
```bash
  cd ..
  cp ./filler/target/scala-2.13/filler-assembly-0.1.0-SNAPSHOT.jar ./app
```

## Run air-flow containders with filler

Create `.env`:
```bash
echo -e "AIRFLOW_UID=$(id -u)" > .env
```
In first time run `airflow-init` scenario:
```
docker compose up airflow-init
```
Then start airflow normally:
``` 
docker compose up

```
Airflow-DAG `update-news-datamart` can be started from DAGs list - (http://127.0.0.1:8080/home) 
To access airflow web-ui use user/pass: `airflow/airflow`


