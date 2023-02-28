## Dockerfile for solace-ep-kafka-demo
## Image to configure and exec solace-kafka-demo against a Kafka cluster

FROM openjdk:11.0.16-jdk

RUN mkdir /opt/kafka-demo
WORKDIR /opt/kafka-demo

RUN mkdir logs lib
COPY target/kafka-streams-examples-7.1.1-standalone.jar lib/
ADD kafka-demo-scripts/ ./scripts/
RUN chmod +x scripts/*.sh

ADD https://packages.confluent.io/archive/7.3/confluent-community-7.3.1.tar.gz ./
RUN tar xzf confluent-community-7.3.1.tar.gz
RUN rm -R confluent-community-7.3.1.tar.gz

CMD ["/opt/kafka-demo/scripts/auto-exec-demo.sh"]
