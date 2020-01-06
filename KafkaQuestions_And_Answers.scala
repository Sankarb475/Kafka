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
4) how to see the leader of a topic in a multi-broker kafka cluster
==>
> bin/kafka-topics.sh --describe --bootstrap-server localhost:9092 --topic <topic-name>
Topic: my-replicated-topic PartitionCount:5 ReplicationFactor:3 Configs:
Topic: my-replicated-topic Partition: 0 Leader: 2 Replicas: 2,3,1 Isr: 2,3,1
Topic: my-replicated-topic Partition: 1 Leader: 3 Replicas: 3,2,1 Isr: 3,2,1
Topic: my-replicated-topic Partition: 2 Leader: 1 Replicas: 1,3,2 Isr: 1,3,2
Topic: my-replicated-topic Partition: 3 Leader: 2 Replicas: 2,3,1 Isr: 2,3,1
Topic: my-replicated-topic Partition: 4 Leader: 3 Replicas: 3,1,2 Isr: 3,1,2


The first line describes the topic itself, so it has 5 partitions (details of those are being provided in subsequent rows) and each
partition has 3 replicas.

here,
leader represents the broker.id of that broker which is the leader of that partition.
Partition:0 Leader:2 ==> means broker 2 is the leader of partition 1

Replicas, represent the broker.id's where that partition is being stored, since RF is 3, you will see 3 broker.id's.

ISR ==> set of "in-sync replicas". This is the subset of the replicas list that is currently alive and caught-up to the leader.

======================================================================================================================================
5) List of frequently used commands in Kafka?
==>
Purging a topic::
=================
There is'nt any single command to delete all the data from a topic. What we do is, we change the retention period to seconds, and Kafka
deletes all the data, and once that is done, we reconfigure the retention period back to default.
retention.ms=1000

Use this to change the retention period:
$ ./bin/kafka-topics.sh --alter --zookeeper localhost:2181 --topic my-topic --config retention.ms=1000

Use this to reconfigure it back to normal (wait for a couple of minutes after the initial change):
$ ./bin/kafka-topics.sh --alter --zookeeper localhost:2181 --topic my-topic --delete-config retention.ms

Deleting a topic::
==================
$ ./bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic my-topic

** remind you in newer version of kafka "--zookeeper" has been changed to "--bootstrap-server"

======================================================================================================================================
6) Stopping a Kafka broker::
==>
In Linux ::
> ps aux | grep server-1.properties
7564 ttys002    0:15.91 /System/Library/Frameworks/JavaVM.framework/Versions/1.8/Home/bin/java...
> kill -9 7564

In Windows::
> wmic process where "caption = 'java.exe' and commandline like '%server-1.properties%'" get processid
ProcessId
6016
> taskkill /pid 6016 /f



