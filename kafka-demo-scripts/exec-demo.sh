#!/bin/bash

#################################################################
##  exec-demo.sh
##  ------------
##  THIS SCRIPT ASSUMES THAT $CONFIG_FILE ENV VAR IS SET
##  DEFINING FILE WITH bootstrap.servers AND schema.registry.url
##  PROPERTIES ALONG WITH AUTHENTICATION SETTINGS.

set -m

# BOOTSTRAP_SERVERS=${BOOTSTRAP_SERVERS:-broker:9092}
# SCHEMA_REGISTRY_URL=${SCHEMA_REGISTRY_URL:-http://schema-registry:8081}
RESTPORT=${RESTPORT:-5432}
JAR=${JAR:-"$DEMO_HOME/lib/kafka-streams-examples-7.1.1-standalone.jar"}
PIDS=()
[[ -z "$CONFIG_FILE" ]] && CONFIG_FILE_ARG="" || CONFIG_FILE_ARG="--config-file $CONFIG_FILE"
ADDITIONAL_ARGS=${ADDITIONAL_ARGS:-""}
LOG_DIR=${LOG_DIR:="$DEMO_HOME/logs"}
PIDS_FILE=${PIDS_FILE:="$DEMO_HOME/.microservices.pids"}

echo "Starting microservices from $JAR"
echo "Config File arg: $CONFIG_FILE"
echo "OrdersService REST Port: $RESTPORT"
echo "Additional Args: $ADDITIONAL_ARGS"

echo "Starting OrdersService"
java -cp $JAR io.confluent.examples.streams.microservices.OrdersService --port $RESTPORT $CONFIG_FILE_ARG $ADDITIONAL_ARGS >$LOG_DIR/OrdersService.log 2>&1 &
PIDS+=($!)
echo "Giving OrdersService time to start (15 seconds)"
sleep 15

echo "Adding Inventory"
java -cp $JAR io.confluent.examples.streams.microservices.AddInventory $CONFIG_FILE_ARG $ADDITIONAL_ARGS >$LOG_DIR/AddInventory.log 2>&1 &
sleep 5

echo "Starting KStreams Services"
for SERVICE in "InventoryService" "FraudService" "OrderDetailsService" "ValidationsAggregatorService" ; do
  echo "Starting $SERVICE"
  java -cp $JAR io.confluent.examples.streams.microservices.$SERVICE $CONFIG_FILE_ARG $ADDITIONAL_ARGS >$LOG_DIR/$SERVICE.log 2>&1 &
  PIDS+=($!)
done
sleep 10

echo "Starting PostOrdersAndPayments Service"
java -cp $JAR io.confluent.examples.streams.microservices.PostOrdersAndPayments --order-service-url "http://localhost:$RESTPORT" $CONFIG_FILE_ARG >$LOG_DIR/PostOrdersAndPayments.log 2>&1 &
PIDS+=($!)
sleep 10

echo "Starting EmailService"
java -cp $JAR io.confluent.examples.streams.microservices.EmailService $CONFIG_FILE_ARG $ADDITIONAL_ARGS >$LOG_DIR/EmailService.log 2>&1 &
PIDS+=($!)
sleep 3

echo "Starting Producers"
for PRODUCER in "ProduceCustomers" "ProduceOrders" "ProducePayments" ; do
  echo "Starting $PRODUCER"
  java -cp $JAR io.confluent.examples.streams.microservices.util.$PRODUCER $CONFIG_FILE_ARG $ADDITIONAL_ARGS >$LOG_DIR/$PRODUCER.log 2>&1 &
  PIDS+=($!)
done
sleep 2

echo "Starting Consumers"
for CONSUMER in "ConsumeCustomers" "ConsumeOrders" "ConsumePayments" ; do
  echo "Starting $CONSUMER"
  java -cp $JAR io.confluent.examples.streams.microservices.util.$CONSUMER $CONFIG_FILE_ARG $ADDITIONAL_ARGS >$LOG_DIR/$CONSUMER.log 2>&1 &
  PIDS+=($!)
done
sleep 2

echo "Microservice processes running under PIDS: ${PIDS[@]}"
echo "${PIDS[@]}" > $PIDS_FILE 
# wait $PIDS
