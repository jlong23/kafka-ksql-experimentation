java -cp  ./target/oipa-kafka-streaming-0.0.1-SNAPSHOT.jar -Dlog4j.configuration=file:src/main/resources/log4j.properties  com.nationwide.nf.ips.streaming.consumer.OipaExampleConsumer "$@"
