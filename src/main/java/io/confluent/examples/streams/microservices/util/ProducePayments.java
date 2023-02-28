package io.confluent.examples.streams.microservices.util;

import io.confluent.examples.streams.avro.microservices.Payment;
import io.confluent.examples.streams.utils.MonitoringInterceptorUtils;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static io.confluent.examples.streams.microservices.domain.beans.OrderId.id;
import static io.confluent.examples.streams.microservices.util.MicroserviceUtils.*;

public class ProducePayments {

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(final String[] args) throws Exception {

        final Options opts = new Options();
        opts.addOption(Option.builder("b")
                .longOpt("bootstrap-servers").hasArg().desc("Kafka cluster bootstrap server string").build())
                .addOption(Option.builder("s")
                        .longOpt("schema-registry").hasArg().desc("Schema Registry URL").build())
                .addOption(Option.builder("c")
                        .longOpt("config-file").hasArg().desc("Java properties file with configurations for Kafka Clients").build())
                .addOption(Option.builder("h")
                        .longOpt("help").hasArg(false).desc("Show usage information").build());

        final CommandLine cl = new DefaultParser().parse(opts, args);

        final Properties defaultConfig = Optional.ofNullable(cl.getOptionValue("config-file", null))
                .map(path -> {
                    try {
                        return buildPropertiesFromConfigFile(path);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse(new Properties());

//        final String bootstrapServers = cl.getOptionValue("b", DEFAULT_BOOTSTRAP_SERVERS);
//        final String schemaRegistryUrl = cl.getOptionValue("schema-registry", DEFAULT_SCHEMA_REGISTRY_URL);

//  TODO - GET RID OF THIS, GOING A DIFFERENT ROUTE
        // final SpecificAvroSerializer<Payment> mySerializer = new SpecificAvroSerializer<>();
        // final boolean isKeySerde = false;
        // mySerializer.configure(
        //     Collections.singletonMap(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl),
        //     isKeySerde);

        final Properties props = new Properties();
        props.putAll(defaultConfig);

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, "io.confluent.kafka.serializers.subject.TopicNameStrategy");

//  TODO - GET RID OF THIS, GOING A DIFFERENT ROUTE
//        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 1);

        // TODO - IGNORE IF NOT CONFLUENT
        MonitoringInterceptorUtils.maybeConfigureInterceptorsProducer(props);

        try (final KafkaProducer<String, Payment> producer = new KafkaProducer<>(props)) {
            for ( Long lOrderId = 1L; lOrderId < 2e5; lOrderId++ ) {

                final int qty = (int)(lOrderId % 3) + 1;
                final double amt = qty * 5.0d;

                final Payment payment = new Payment("Payment:" + id(lOrderId), id(lOrderId), "CZK", amt);
                final ProducerRecord<String, Payment> record = new ProducerRecord<>("payments", payment.getId(), payment);
                producer.send(record);
                Thread.sleep(1000L);
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

    }

}

