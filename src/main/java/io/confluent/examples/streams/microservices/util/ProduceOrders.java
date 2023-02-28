package io.confluent.examples.streams.microservices.util;

import io.confluent.examples.streams.avro.microservices.Order;
import io.confluent.examples.streams.utils.MonitoringInterceptorUtils;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;

//  TODO - AZURE SCHEMA REGISTRY
//import com.microsoft.azure.schemaregistry.kafka.avro.KafkaAvroSerializer;
//import com.microsoft.azure.schemaregistry.kafka.avro.KafkaAvroSerializerConfig;
//import com.microsoft.azure.schemaregistry.kafka.avro.AbstractKafkaSchemaSerDeConfig;
//import io.netty.handler.codec.DefaultHeaders;       // Attempt to correct a bug

//  TODO - AZURE SCHEMA REGISTRY AUTHENTICATION
// import com.azure.core.credential.TokenCredential;
// import com.azure.identity.ClientSecretCredentialBuilder;
// import com.azure.identity.ManagedIdentityCredentialBuilder;

//import org.apache.avro.generic.GenericRecord;
//import org.apache.avro.specific.SpecificRecord;

//import com.microsoft.azure.schemaregistry.kafka.avro.SpecificAvroSerializer;
//import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;

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
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Map.Entry;

import static io.confluent.examples.streams.avro.microservices.OrderState.CREATED;
import static io.confluent.examples.streams.avro.microservices.Product.UNDERPANTS;
import static io.confluent.examples.streams.microservices.domain.beans.OrderId.id;
import static io.confluent.examples.streams.microservices.util.MicroserviceUtils.*;

public class ProduceOrders {

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
        // final SpecificAvroSerializer<Order> mySerializer = new SpecificAvroSerializer<>();
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
        //  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 1);

        // TODO - IGNORE IF NOT CONFLUENT
        MonitoringInterceptorUtils.maybeConfigureInterceptorsProducer(props);

        for ( final Map.Entry<Object, Object> e : props.entrySet() ) {
            System.out.println( String.format("%s: %s", e.getKey(), e.getValue()));
        }

        // AZURE SCHEMA REGISTRY
        // TODO - IGNORE IF NOT AZURE SCHEMA REGISTRY
        // final TokenCredential credential;
        // if (props.getProperty("use.managed.identity.credential").equals("true")) {
        //     credential = new ManagedIdentityCredentialBuilder().build();
        // } else {
        //     credential = new ClientSecretCredentialBuilder()
        //             .tenantId(props.getProperty("tenant.id"))
        //             .clientId(props.getProperty("client.id"))
        //             .clientSecret(props.getProperty("client.secret"))
        //             .build();
        // }
        // props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_CREDENTIAL_CONFIG, credential);

        // Schema Registry configs
        props.put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, "true");
//        props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
//        props.put(KafkaAvroSerializerConfig.SCHEMA_GROUP_CONFIG, schemaGroup);

    try (final KafkaProducer<String, Order> producer = new KafkaProducer<String, Order>(props)) {
            for ( Long lOrderId = 1L; lOrderId < 2e5; lOrderId++ ) {
                final String orderId = id(lOrderId);
                final int qty = (int)(lOrderId % 3) + 1;
                final long customerId = ( lOrderId / 2L ) + 1L;
                final Order order = new Order(orderId, customerId, CREATED, UNDERPANTS, qty, 5.00d);
                final ProducerRecord<String, Order> record = new ProducerRecord<String, Order>("orders", order.getId(), order);
                producer.send(record);
                Thread.sleep(750L);
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

}

