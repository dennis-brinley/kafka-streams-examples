#!/bin/bash

TOPICS_FILE="$@"
# BOOTSTRAP_SERVERS=${BOOTSTRAP_SERVERS:-"broker:9092"}
PARTITIONS=${PARTITIONS:-1}
REPLICATION_FACTOR=${REPLICATION_FACTOR:-1}

## GET BOOTSTRAP_SERVERS FROM CONFIG_FILE
while IFS='=' read -r key value; 
  do
    if [[ "${key}" == "bootstrap.servers" ]]; then
      BOOTSTRAP_SERVERS=${value}
    fi
  done < $CONFIG_FILE

while IFS= read -r TOPIC; 
  do
    [[ -z "$TOPIC" ]] || {
      echo "Topic: $TOPIC"
      kafka-topics --create --if-not-exists --bootstrap-server=$BOOTSTRAP_SERVERS --partitions $PARTITIONS --replication-factor $REPLICATION_FACTOR --topic $TOPIC --command-config $CONFIG_FILE 2>$DEMO_HOME/logs/create-topic-$TOPIC.log
    }
  done <$TOPICS_FILE
