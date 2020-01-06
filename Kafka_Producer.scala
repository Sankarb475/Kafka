Kafka Producer sequential steps (code using Scala)
==============================================================================================
1) first we create a Properties object and push all the properties needed for the producer to run.

import java.util.Properties

val props = new Properties()
props.put("bootstrap.servers", "broker1:9092,broker2:9092")
props.put("client.id", "ScalaProducerExample")
props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

2) We create a KafkaProducer object, which will facilitate the whole process

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
val producer = new KafkaProducer[String, String](props)


2) We start producing messages to Kafka by creating a ProducerRecord, which must include the TOPIC(topic) we want to send the record to 
and a VALUE(msg). Optionally, we can also specify a key and/or a partition. Once we send the ProducerRecord, the first thing the producer 
will do is serialize the key and value objects to ByteArrays so they can be sent over the network.
We use the send() function to send the records.

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

val record = new ProducerRecord[String, String](topic, msg)
producer.send(record)

3) Next, the data is sent to a partitioner. If we specified a partition in the ProducerRecord, the partitioner doesn’t do anything and 
simply returns the partition we specified. If we didn’t, the partitioner will choose a partition for us, usually based on the 
ProducerRecord key. Once a partition is selected, the producer knows which topic and partition the record will go to. It then adds the 
record to a batch of records that will also be sent to the same topic and partition. A separate thread is responsible for sending those 
batches of records to the appropriate Kafka brokers.


4) When the broker receives the messages, it sends back a response. If the messages were successfully written to Kafka, it will return 
a RecordMetadata object with the topic, partition, and the offset of the record within the partition. If the broker failed to write the 
messages, it will return an error. When the producer receives an error, it may retry sending the message a few more times before giving
up and returning an error.


-- Since we plan on using strings for message key and value, we use the built-in StringSerializer.



There are three primary methods of sending messages::
====================================================================
1) Fire-and-forget ==>
We send a message to the server and don’t really care if it arrives succesfully or not. Most of the time, it will arrive successfully, 
since Kafka is highly available and the producer will retry sending messages automatically. However, some messages will get lost using 
this method.

import java.io.IOException  

val topic = "my-topic"
val msg = "This is to be sent over Kafka network
val producer = new KafkaProducer[String, String](props)
val record = new ProducerRecord[String, String](topic, msg)
try{
    producer.send(record)
}
catch{
    case IOException => {
        print("Error")
    }
}

2) Synchronous send ==> 
We send a message, the send() method returns a Future object, and we use get() to wait on the future and see if the send() was successful
or not.










