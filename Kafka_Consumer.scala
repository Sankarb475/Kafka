Kafka Consumer
=====================================================================================================================================
Applications that need to read data from Kafka use a KafkaConsumer to subscribe to Kafka topics and receive messages from these topics.


Consumers and Consumer Groups
=====================================================
If your producer is writing at a rate more than the rate at which your consumer can consume the records. your application may fall 
farther and farther behind, unable to keep up with the rate of incoming messages. Thus we need to scale up the consumer rate of 
consumption. We do that by increasing the number of consumer instances, so the incoming load gets distributed among the instances. 

Suppose if the producer is writing at a speed of 1 GB per minute, and your consumer instance can consume the data at a rate 50 MB per 
minute, then we would need to 20 such consumer instances to keep up with the producer with no backlog.

-- So since all the 20 consumers would be consuming from the same topic and complete the consumption, they should be binded by something
which is consumer group, so consumer instances part of a consumer group distributes the load amongst themselves, and owns one or more
partition. 

-- If we can have multiple consumer groups consuming from the same topic, the groups would be independently consuming from the topic.

Suppose we have a topic with 4 partitions, and a consumer group with one consumer instance, then the consumer will be consuming from all 
these 4 partitions, and now if you add another consumer instance into the group, the work load will be distributed by the group 
coordinator and each instance will own only two partition. But if you keep adding and end up having 4 instances and each instance will 
consume from only one partition. Now if you add another instance, it will remain idle.
--thus, number of consumer instances <= number of partitions

-- Each consumer instance is called the owner/leader of the partition from which it consumes the messages.

Consumer groups and Partition rebalance
===================================================
When 
1) we add a new consumer to the consumer group
2) we add new partitions to the topic
3) consumer crashes or shuts down

The ownership of partitions changes and an entire reassignment of partitions to the consumer occurs - this is called a Rebalance.
Moving partition ownership from one consumer to another is called a rebalance.

-- rebalance is important because it allows for high availability and scaling up.
-- but rebalance is not desirable because
1) during rebalance consumer cant consume messages, so it is the unavailability of the entire consumer group.
2) when partitions are moved from one consumer to another, the consumer loses its current state; if it was caching any data, it 
will need to refresh its caches—slowing down the application until the consumer sets up its state again.

Group Coordinator
==================================================
The way consumers maintain membership in a consumer group and ownership of the partition assigned to them is by sending heartbeats to
a kafka broker designated as "GROUP COORDINATOR".
A consumer will be called alive if -
1) it sends heartbeats to the group coordinator on a regular interval
2) it is consuming/processing the messages from the partition 

-- Heartbeats are sent when the consumer polls (i.e., retrieves records) and when it commits records it has consumed.

-- If the consumer stops sending heartbeats for long enough, its session will time out and the group coordinator will consider it dead 
and trigger a rebalance. If a consumer crashed and stopped processing messages, it will take the group coordinator a few seconds 
without heartbeats to decide it is dead and trigger the rebalance. During those seconds, no messages will be processed from the 
partitions owned by the dead consumer. When closing a consumer cleanly, the consumer will notify the group coordinator that it is 
leaving, and the group coordinator will trigger a rebalance immediately, reducing the gap in processing.


Creating a Kafka Consumer
==================================================
1) First you start with creating a properties object which will hold all the important parameters for you consumer.

The mandatory parameters for a Kafka consumer :
a) Bootstrap servers
b) consumer group (group id) - optional 
c) key deserializer
d) Value deserializer

import java.util.Properties

val props = new Properties()
props.put("bootstrap.servers", "localhost:9094")
props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
props.put("auto.offset.reset", "latest")
props.put("group.id", "consumer-group")

2) Creating a Kafka consumer instance.

import org.apache.kafka.clients.consumer.KafkaConsumer
val consumer : KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)

3) Subscribe to topic/topics

val topic = "my-topic"
consumer.subscribe(util.Arrays.asList(topic))

or 

val: List[String] topics = List("my_topic_partition","my_topic_partition")
consumer.subscribe(topics)

-- we could use regular expression as well to define the topics
consumer.subscribe("test.*"); ==> in java

4) Start consuming data : The Poll loop
At the heart of the consumer API is a simple loop for polling the server for more data. Once the consumer subscribes to topics, the poll 
loop handles all details of coordination, partition rebalances, heartbeats, and data fetching, leaving the developer with a clean API 
that simply returns available data from the assigned partitions.


try {
    while (true) {
      val record = consumer.poll(1000).asScala
      for (data <- record.iterator)
        println(data.value())
    }
}
finally{
  consumer.close()
}

-- this is an infinite loop


Kafka Poll
=========================================
Consumers must keep polling Kafka or they will be considered dead and the partitions they are consuming will be handed to another 
consumer in the group to continue consuming. The parameter we pass, poll(), is a timeout interval and controls how long poll() will 
block if data is not available in the consumer buffer. If this is set to 0, poll() will return immediately; otherwise, it will wait 
for the specified number of milliseconds for data to arrive from the broker.

poll() returns a list of records.
Each record contains : the topic name, the partition name from where the record has come, offset value, key and value of the record.

Always close() the consumer before exiting. This will close the network connections and sockets. It will also trigger a rebalance 
immediately rather than wait for the group coordinator to discover that the consumer stopped sending heartbeats and is likely dead, 
which will take longer and therefore result in a longer period of time in which consumers can’t consume messages from a subset of the 
partitions.

poll() does a lot of work - 
1) The very first it is being called, it finds the GroupCoordinator
2) Joins the consumer group and receives a partition assignment
3) If a rebalance is triggered, it will be handled inside the poll loop as well.
4) the heartbeats that keep consumers alive are sent from within the poll loop.

*** You can’t have multiple consumers that belong to the same group in one thread and you can’t have multiple threads safely use the 
same consumer. One consumer per thread is the rule. To run multiple consumers in the same group in one application, you will need to run 
each in its own thread.


Consumer Configurations
===============================================
fetch.min.bytes ==> 
The minimum size of the batch requires for the broker to be able to sent it out to the Consumer. If the combined size of the messages
doesnt cross this value, broker will wait for additional records to come.

fetch.max.wait.ms ==> 500 ms
Tells the broker how long to wait before it sends out one batch to the Consumer. By default, Kafka will wait up to 500 ms.

*** so between the above two parameter broker waits for any one of them to complete, so that it can send out the batch. 
If you set fetch.max.wait.ms to 100 ms and fetch.min.bytes to 1 MB, Kafka will recieve a fetch request from the consumer and will 
respond with data either when it has 1 MB of data to return or after 100 ms, whichever happens first.

max.partition.fetch.bytes ==> 1 MB
This property controls the maximum number of bytes the server will return per partition. This is the maximum size of a single record 
which can be sent over the network to the consumer.

session.timeout.ms ==> 3 seconds
The amount of time a consumer can be out of contact with the brokers (no heartbeats sent) while still considered alive defaults to 3 
seconds.

heartbeat.interval.ms ==> 
controls how frequently the KafkaConsumer poll() method will send a heartbeat to the group coordinator.

*** if heartbeat.interval.ms > session.timeout.ms ==> then the broker will always assume that the consumer is dead. So this is a MUST :
heartbeat.interval.ms < session.timeout.ms

auto.offset.reset ==> "latest"
The default is “latest,” which means that lacking a valid offset, the consumer will start reading from the newest records (records that
were written after the consumer started running). The alternative is “earliest,” which means that lacking a valid offset, the consumer 
will read all the data in the partition, starting from the very beginning.


enable.auto.commit => false
Whether the offsets are automatically updated. If enabled each "auto.commit.interval.ms" interval, the offsets will be updated.

max.poll.records ==> 
This controls the maximum number of records that a single call to poll() will return.

Commits and Offsets
==============================================
One of Kafka’s unique characteristics is that it does not track acknowledgments from consumers the way many JMS queues do. Instead, it 
allows consumers to use Kafka to track their position (offset) in each partition.

We call the action of updating the current position in the partition a commit.

How does a consumer commit an offset? 
It produces a message to Kafka, to a special __consumer_offsets topic, with the committed offset for each partition. As long as all your
consumers are up, running, and churning away, this will have no impact. However, if a consumer crashes or a new consumer joins the 
consumer group, this will trigger a rebalance. After a rebalance, each consumer may be assigned a new set of partitions than the one it 
processed before. In order to know where to pick up the work, the consumer will read the latest committed offset of each partition and 
continue from there.
If the committed offset is smaller than the offset of the last message the client processed, the messages between the last processed 
offset and the committed offset will be processed twice.

Automatic Commit ==>
The easiest way to commit offsets is to allow the consumer to do it for you. If you configure enable.auto.commit=true, then every five 
seconds the consumer willcommit the largest offset your client received from poll(). The five-second interval is the default and is 
controlled by setting auto.commit.interval.ms. Just like everything else in the consumer, the automatic commits are driven by the poll 
loop. Whenever you poll, the consumer checks if it is time to commit, and if it is, it will commit the offsets it returned in the last 
poll.

Consider that, by default, automatic commits occur every five seconds. Suppose that we are three seconds after the most recent commit 
and a rebalance is triggered. After the rebalancing, all consumers will start consuming from the last offset committed. In this case, 
the offset is three seconds old, so all the events that arrived in those three seconds will be processed twice. It is possible to 
configure the commit interval to commit more frequently and reduce the window in which records will be duplicated, but it is impossible 
to completely eliminate them.

Commit Current Offset ==> 
commitSync 
=========================================
By setting auto.commit.offset=false, offsets will only be committed when the application explicitly chooses to do so. The simplest and 
most reliable of the commit APIs is commitSync(). This API will commit the latest offset returned by poll() and return once the offset 
is committed, throwing an exception if commit fails for some reason.


while (true) {
  val record = consumer.poll(1000).asScala
  for (data <- record.iterator)
    println(data.value())
}
try{
    consumer.commitSync()   
}
catch{
    case IOException ==> {
        print("IO error")
    }
}

-- Once we are done “processing” all the records in the current batch, we call commitSync to commit the last offset in the batch, before 
polling for additional messages.
commitSync retries committing as long as there is no error that can’t be recovered. If this happens, there is not much we can do except 
log an error.

Asynchronous Commit ::
==============================
One problem with commitSync is that until unless processing is done, the application is blocked, also if broker doesnt respond to the 
commit request application is blocked.
Another option is the asynchronous commit API. Instead of waiting for the broker to respond to a commit, we just send the request and 
continue on.

while (true) {
  val record = consumer.poll(1000).asScala
  for (data <- record.iterator)
    println(data.value())
  consumer.commitAsync()                            //Commit the last offset and carry on.
}

-- during a rebalance, it might cause a bigger rebalance.

Combining a sync and async commit 
==========================================

try{
    while (true) {
      val record = consumer.poll(1000).asScala
      for (data <- record.iterator)
        println(data.value())
       consumer.commitAsync()
    }
}
catch{
    case .....
    ....
}
finally {
    try {
        consumer.commitSync();
    } finally {
        consumer.close();
    }
}

While everything is fine, we use commitAsync. It is faster, and if one commit fails, the next commit will serve as a retry.
But if we are closing, there is no “next commit.” We call commitSync(), because it will retry until it succeeds or suffers unrecoverable
failure.

Commit Specified Offset
================================
sync and aync both lets you commit when one complete batch is done processing. What if you want to commit in between, because your batch 
size is big and you dont want to reprocess the whole batch if a rebalance is triggered in between.

int count = 0;
while (true) {
ConsumerRecords<String, String> records = consumer.poll(100);
for (ConsumerRecord<String, String> record : records)
{
System.out.printf("topic = %s, partition = %s, offset = %d,
customer = %s, country = %s\n",
record.topic(), record.partition(), record.offset(),
record.key(), record.value());
currentOffsets.put(new TopicPartition(record.topic(),
record.partition()), new
OffsetAndMetadata(record.offset()+1, "no metadata"));
if (count % 1000 == 0)
consumer.commitAsync(currentOffsets, null);
count++;
}
}
 
Consuming Records with Specific Offsets
====================================================
This has to be updated/populated with Scala code.





Standalone Consumer: Why and How to Use a Consumer Without a Group
========================================================================================
Sometimes you know you have a single consumer that always needs to read data from all the partitions in a topic, or from a specific 
partition in a topic. In this case, there is no reason for groups or rebalances— just assign the consumer-specific topic and/or 
partitions, consume messages, and commit offsets on occasion.

When you know exactly which partitions the consumer should read, you don’t subscribe to a topic—instead, you assign yourself a few 
partitions. A consumer can either subscribe to topics (and be part of a consumer group), or assign itself partitions, but not both at 
the same time.

The scala code for this has to be found out and populated here.
