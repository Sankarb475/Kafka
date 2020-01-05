1) How to increase the number of partitions of a topic?
==>
Kafka only lets you increase the number of partitions, you cannot decrease it.

Kafka has this alter statement which lets you do that::
> kafka-topics.sh --alter --zookeeper localhost:2182 --topic test --patitions 5

** Using AdminUtils you can increase the number of partitions from your producer
> AdminUtils.addPartitions()

======================================================================================================================================
2) how to dynamically create a kafka topic from the producer?
==>
If you set this property to true in our server.properties file before starting your kafka broker:
-- auto.create.topics.enable
kafka will automatically create a topic when you send a message to a non existing topic. The partition number will be defined by the 
default settings in this same file.
num.partitions ==> this parameter will define the number of partitions to be created.

======================================================================================================================================
3) Why the change from zookeeper to bootstrap-server for any topic related operations and kafka consumer?
==>
Before 0.10.0 kafka version, consumer used to store the offset value with Zookeeper, so consumer had to communicate with the Zookeeper
to fetch/update the offset value.
But now, Kafka consumer stores the offset value in the __consumer_offset topic on the same Kafka server (in kafka broker). So consumer
doesnt need need to connect to Zookeeper, bootstrap-server is nothing but one of the kafka broker in the kafka cluster.

** so anything which has to do with topic creationg/alteration and consumer, we'd have to use bootstrap-server.
======================================================================================================================================

