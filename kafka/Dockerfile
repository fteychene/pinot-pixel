FROM openjdk:slim-buster

ARG kafka_version=3.1.0
ARG scala_version=2.13
ENV PATH=${PATH}:/opt/kafka/bin

ADD https://dlcdn.apache.org/kafka/$kafka_version/kafka_$scala_version-$kafka_version.tgz /tmp/kafka.tgz

RUN tar xfz /tmp/kafka.tgz -C /opt && mv /opt/kafka_$scala_version-$kafka_version /opt/kafka
RUN kafka-storage.sh format -t $(kafka-storage.sh random-uuid) -c /opt/kafka/config/kraft/server.properties

CMD ["/opt/kafka/bin/kafka-server-start.sh", "/opt/kafka/config/kraft/server.properties"]