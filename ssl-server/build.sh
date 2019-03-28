#!/bin/sh
# Verbose mode
set -x

echo "Building $TARGET\n\n"

# Build 
mvn package

#Create rpm directory structure
rm -rf rpm
mkdir -p rpm rpm/BUILD  rpm/BUILDROOT  rpm/RPMS  rpm/SOURCES  rpm/SPECS  rpm/SRPMS  rpm/tmp

# Copy files to rpm/SOURCES directory
echo "Preparing RPM Resources\n\n"

cp "target/sslserver.jar" 			rpm/SOURCES/sslserver.jar
cp config/server.config 			rpm/SOURCES/server.config
cp config/log4j.properties			rpm/SOURCES/log4j.properties
cp startServer.sh 					rpm/SOURCES/startServer.sh
cp stopServer.sh 					rpm/SOURCES/stopServer.sh
cp truststore.jks 					rpm/SOURCES/truststore.jks
cp keystore.jks						rpm/SOURCES/keystore.jks
cp sslserver.service				rpm/SOURCES/sslserver.service

JAVA_TEST_OPTIONS="-server -Xss228k -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:ParallelGCThreads=1"
JAVA_PROD_OPTIONS="-server -Xss128k -Xms8g -Xmx8g -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:ParallelGCThreads=4"

sed -i "s,JAVA_OPTIONS,$JAVA_TEST_OPTIONS,;s,JAR_FILE,$TARGET,;" rpm/SOURCES/startServer.sh

#Copy RPM Spec file
cp sslserver.spec rpm/SPECS/sslserver.spec

# Build rpm
echo "Building RPM\n\n"
cd rpm
rpmbuild -v -bb SPECS/sslserver.spec

