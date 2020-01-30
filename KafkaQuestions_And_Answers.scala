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
kafka will automatically create a topic:
• When a producer starts writing messages to the topic
• When a consumer starts reading messages from the topic
• When any client requests metadata for the topic

send a message to a non existing topic. 
The partition number will be defined by the default settings in this same file.
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

======================================================================================================================================
7) How to choose the number of partitions?
==>
This depends on the following properties:
1) throughput you expect to achieve for the topic
2) maximum throughput you expect to achieve when consuming from a single partition
==> You will always have, at most, one consumer reading from a partition, so if you know that your slower consumer writes the data to 
a database and this database never handles more than 50 MB per second from each thread writing to it, then you know you are limited to
50 MB throughput when consuming from a partition.
3) Avoid overestimating, as each partition uses memory and other resources on the broker and will increase the time for leader elections

*** If you have some estimate regarding the target throughput of the topic and the expected throughput of the consumers, you can divide 
the target throughput by the expected consumer throughput and derive the number of partitions this way. So
if I want to be able to write and read 1 GB/sec from a topic, and I know each consumer can only process 50 MB/s, then I know I need
at least 20 partitions. This way, I can have 20 consumers reading from the topic and achieve 1 GB/sec.
 
partitions = target throughput of the topic / expected consumer throughput 

--if you do not have those values handy, it is advisable to have a partition that limits the size of the partition on the disk to less
than 6 GB per day of retention often gives satisfactory results.

** You measure the throughout that you can achieve on a single partition for production (call it p) and consumption (call it c). Let’s 
say your target throughput is t. Then you need to have at least max(t/p, t/c) partitions.

======================================================================================================================================
8) Adverse effect of having more partitions?
==>
Although more partition lead to higher throughpu, thses are the adverse effects:
1) More Partitions Requires More Open File Handles
2) More Partitions May Increase Unavailability
3) More Partitions May Increase End-to-end Latency
4) More Partitions May Require More Memory In the Client

======================================================================================================================================
9) How to increase the maximum size of a record in Kafka?
==>
By default maximum size is 1 MB.

You need to adjust three (or four) properties:
Broker: you need to increase properties message.max.bytes and replica.fetch.max.bytes. 
        condition to be satisfied :
            message.max.bytes <= replica.fetch.max.bytes
        -- if replica.fetch.max.bytes is less than message.max.bytes then although producer will be able to write to the leader, leader
           will not be able to replicate the messages to replicas.
Producer: Increase max.request.size to send the larger message.
Consumer: Increase max.partition.fetch.bytes to receive larger messages.

-- we need to add the broker properties in server.properties.

======================================================================================================================================
10) what are the uses of Kafka keys?
==>
Keys are mostly useful/necessary If you require that messages with the same key (for instance, a unique id) are always seen in the 
correct order, attaching a key to messages will ensure messages with the same key always go to the same partition in a topic. Kafka 
guarantees order within a partition, but not across partitions in a topic, so alternatively not providing a key - which will result in 
round-robin distribution across partitions - will not maintain such order.

Keys are used to determine the partition within a log to which a message get's appended to. While the value is the actual payload of 
the message.

Specifying the key so that all messages on the same key go to the same partition is very important for proper ordering of message 
processing if you will have multiple consumers in a consumer group on a topic.
Without a key, two messages on the same key could go to different partitions and be processed by different consumers in the group out 
of order.

======================================================================================================================================
11) What will happen if I try to send a record with the producer api to a broker and the log of the topic got full before the retention 
period? Will my message get dropped? Or will kafka free some space from the old messages and add mine?
==>
By default kafka has no size limit to its partition. It has a parameter 
log.retention.bytes (log.retention.bytes: The maximum size of the log before deleting it), the value of which is -1 by default, which
means theres no size limit to it.

But, if you have put a size limit to it, then Kafka has another property (values - delete/compact)
cleanup.policy (The default cleanup policy for segments beyond the retention window) - by default "delete"
-- this means "The delete policy will discard old segments when their retention time or size limit has been reached."

======================================================================================================================================
12) How can I know if a topic is getting full and logs are being deleted before being consumed? Is there a way to monitor or expose a 
metric when a topic is getting full?
==>
We can partition size using the below command :

> /bin/kafka-log-dirs.sh --describe --bootstrap-server : --topic-list

-- We will need to develop a script that will run above script for fetching current size of topic, and take conditional action based 
on the size.

======================================================================================================================================
13) When you might end up processing same records more than once?
==>
During a rebalance, If the committed offset is smaller than the offset of the last message the client processed, the messages between 
the last processed offset and the committed offset will be processed twice.

======================================================================================================================================
14) How does a consumer commit an offset?
==> 
It produces a message to Kafka, to a special __consumer_offsets topic, with the committed offset for each partition. As long as all your
consumers are up, running, and churning away, this will have no impact. However, if a consumer crashes or a new consumer joins the 
consumer group, this will trigger a rebalance. After a rebalance, each consumer may be assigned a new set of partitions than the one it 
processed before. In order to know where to pick up the work, the consumer will read the latest committed offset of each partition and 
continue from there.

======================================================================================================================================
15) How to only subscribe to a particular partition of a topic and not the whole topic?
How to force a consumer to read a specific partition in kafka?
==>
Using "assign" rather than using "subscribe"

>>> # manually assign the partition list for the consumer
>>> from kafka import TopicPartition
>>> consumer = KafkaConsumer(bootstrap_servers='localhost:1234')
>>> consumer.assign([TopicPartition('foobar', 2)])
>>> msg = next(consumer)

-- subscriber needs group id while assign doesnt

