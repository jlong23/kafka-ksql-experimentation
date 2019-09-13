FROM openjdk:8
MAINTAINER Jay A. Long

RUN mkdir -p /app/target

WORKDIR /tmp
COPY ./target/oipa-kafka-streaming-0.0.1-SNAPSHOT.jar /tmp/

RUN mv /tmp/oipa-kafka-streaming-0.0.1-SNAPSHOT.jar /app/target

COPY ./docker-processor/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]

CMD ["run"]