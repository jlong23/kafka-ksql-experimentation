#!/bin/bash

java -cp /app/target/oipa-kafka-streaming-0.0.1-SNAPSHOT.jar -Dlog4j.configuration=file:src/main/resources/log4j.properties com.nationwide.nf.ips.streaming.processor.StreamProcessor --bootstrap.servers localhost:9092 --topic ips.oipalife.AsXML.raw --extractor EXTRACTOR-oipaAnnualStatements.json
