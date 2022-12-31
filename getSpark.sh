!# /bin/bash

sparkfile="spark-3.2.3-bin-hadoop3.2-scala2.13.tgz"
sparkdir="spark-3.2.3-bin-hadoop3.2-scala2.13/"
lib="./app/"

# Get spark jar and copy them into ./app folder
mkdir ${lib}
wget https://dlcdn.apache.org/spark/spark-3.2.3/${sparkfile} -P ${lib}
tar -xvf ${lib}spark-3.2.3-bin-hadoop3.2-scala2.13.tgz -C ${lib}
mv ${lib}${sparkdir}/jars/* ${lib}
rm -rf ${lib}${sparkdir}
rm -rf ${lib}${sparkfile}
 
# Get postgresql jdbc for spark jar and copy in into ./app/sparkjars folder
wget https://jdbc.postgresql.org/download/postgresql-42.5.1.jar -P ./app/sparkjars/
mkdir -p ./filler/sparkjars
cp ./app/sparkjars/postgresql-42.5.1.jar ./filler/sparkjars/
