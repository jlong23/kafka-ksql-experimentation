#/bin/bash

docker exec -it zookeeper kafka-topics --create --topic ips.oipalife.AsXML.raw --partitions 1 --replication-factor 1 --if-not-exists --zookeeper localhost:2181
docker exec -it zookeeper kafka-topics --create --topic ips.oipalife.correspondence.statements --partitions 1 --replication-factor 1 --if-not-exists --zookeeper localhost:2181
docker exec -it zookeeper kafka-topics --create --topic ips.oipalife.correspondence.sweeps --partitions 1 --replication-factor 1 --if-not-exists --zookeeper localhost:2181
docker exec -it zookeeper kafka-topics --create --topic ips.oipalife.correspondence.all --partitions 1 --replication-factor 1 --if-not-exists --zookeeper localhost:2181
docker exec -it zookeeper kafka-topics --create --topic ips.oipalife.AsXML.raw.key-stream --partitions 1 --replication-factor 1 --if-not-exists --zookeeper localhost:2181
curl -d "@ksql-index.json" -H "Content-Type: application/vnd.ksql.v1+json" -H "Accept: application/vnd.ksql.v1+json" -X POST http://localhost:8088/ksql
curl -d "@ksql-body.json" -H "Content-Type: application/vnd.ksql.v1+json" -H "Accept: application/vnd.ksql.v1+json" -X POST http://localhost:8088/ksql
rm *.log
echo
echo -- Starting Processor
./runProcessor.sh --bootstrap.servers localhost:9092 --topic ips.oipalife.AsXML.raw --extractor EXTRACTOR-oipaAnnualStatements.json >> ./processor.log &
echo
echo -- Starting Raw Consumer
./runConsumer.sh --bootstrap.servers localhost:9092 --topic ips.oipalife.AsXML.raw --group.id pressys.iib --delay 10 >> ./consumer-raw.log &
echo
echo -- Starting Reprint Consumer
./runConsumer.sh --bootstrap.servers localhost:9092 --topic ips.oipalife.correspondence.statements --group.id pressys.iib --delay 1000 >> ./consumer-reprint.log &
echo
echo -- Starting Test Data Producer
./runProducer.sh --bootstrap.servers localhost:9092 --topic ips.oipalife.AsXML.raw --file AnnualStatementTest.xml --messages 1000 --delay 10 --extractor EXTRACTOR-oipaAnnualStatements.json >>./producer.log &

echo
echo -- LOGS
tail -f ./processor.log ./consumer-raw.log ./consumer-reprint.log
