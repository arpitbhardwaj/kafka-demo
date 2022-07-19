package com.ab.kafkabasics.producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithKeys {

    private static final Logger log = LoggerFactory.getLogger(ProducerDemoWithKeys.class.getSimpleName());
    public static void main(String[] args) {
        log.info("I am a kafka producer");

        //create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG , "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG , StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG , StringSerializer.class.getName());

        //create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

        for (int i = 0; i < 10; i++) {
            //create a producer record
            ProducerRecord<String,String> producerRecord =
                    new ProducerRecord<>("demo_java", "id_"+i, "hello ProducerDemoWithKeys " + i);

            //send data - async operation
            producer.send(producerRecord, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    //executes every time a record is successfully sent or an exception thrown
                    if (exception == null){
                        log.info("Received metadata/ \n" +
                                "Topic: " + metadata.topic() + "\n" +
                                "Key: " + producerRecord.key() + "\n" +
                                "Partition: " + metadata.partition() + "\n" +
                                "Offset: " + metadata.offset() + "\n" +
                                "Timestamp: " + metadata.timestamp());
                    }else {
                        log.error("Error while producing: " + exception.getMessage(), exception);
                    }
                }
            });
        }

        //flush data - synchronous operation
        producer.flush();

        //close the producer
        producer.close();
    }
}
