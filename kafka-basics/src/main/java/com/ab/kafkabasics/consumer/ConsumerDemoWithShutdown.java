package com.ab.kafkabasics.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerDemoWithShutdown {

    private static final Logger log = LoggerFactory.getLogger(ConsumerDemoWithShutdown.class.getSimpleName());
    public static void main(String[] args) {
        log.info("I am a kafka consumer");

        //create consumer properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG , "127.0.0.1:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG , StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG , StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG , "my-second-app");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG , "earliest");

        //create the consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);

        //get a reference to the current thread
        final Thread mainThread = Thread.currentThread();

        //adding the shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                log.info("Detected shutdown..");
                consumer.wakeup();

                //join the main thread to allow the execution of the code in the main thread
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        try {
            //subscribe consumer to topic
            consumer.subscribe(Arrays.asList("demo_java"));

            //poll new message - async operation
            while (true) {
                log.info("Polling");
                ConsumerRecords<String,String> records =
                        consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> record:
                        records) {
                    log.info("key: " + record.key() + " value: " + record.value());
                    log.info("Partition: " + record.partition() + " offset: " + record.offset());
                }
            }
        }catch (WakeupException e){
            log.info("Wake up Exception");
        }catch (Exception e){
            log.error("Unexpected exception");
        }finally {
            consumer.close(); //this will also commit the offsets if required
            log.info("Consumer is gracefully closed");
        }

    }
}
