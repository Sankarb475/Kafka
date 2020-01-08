Kafka ==> "distributed commit log"
Message/record ==> An array of bytes.

Kafka broker is a node(kafka server) on the Kafka cluster, its use is to persist and replicate the data. 
A Kafka Producer pushes the message into the message container called the Kafka Topic. 
Whereas a Kafka Consumer pulls the message from the Kafka Topic.

Replication factor :: 
for a replication factor of N, you will need at least N brokers and you will store N copies of the data, meaning you will need N times 
as much disk space. But it allows you to lose N-1 brokers while still being able to read and write data to the topic reliably.

Kafka Learning
========================================================================================================================
Kafka has four core API's

1) Producer API : Allows an application to publish messages to kafka topic(s).

2) Consumer API : allows application to receive messages from one or more kafka topics.

3) Streams API : Allows an application to receive messages from one or more kafka topics and process those and write to another topic
                 as output stream.

4) Connector API : allows building and running reusable producers or consumers that connect Kafka topics to existing applications or 
                   data systems. For example, a connector to a relational database might capture every change to a table.



-- In kafka, clients and servers communicates through languagae agnostic TCP protocols. Kafka client is one which consumes record
from a kafka cluster. Kafka is run as a cluster on one or more servers that can span multiple datacenters.
-- Each record consists of a key, a value, and a timestamp.

Topics and Logs
===================================
A topic is a category to which records are publsihed. Topics are always multi-subscriber. Topics are stored into partitions.
-- Each partition is an ordered, immutable sequence of records that is continually appended to a structured commit log.
-- The records in the partitions are each assigned a sequential id number called the offset that uniquely identifies each record 
within the partition.

-- The Kafka cluster durably persists all published records—whether or not they have been consumed—using a configurable retention period.
** Kafka's performance is effectively constant with respect to data size so storing data for a long time is not a problem.

-- the only metadata retained on a per-consumer basis is the offset or position of that consumer in the log. This offset is controlled 
by the consumer: normally a consumer will advance its offset linearly as it reads records, but, in fact, since the position is 
controlled by the consumer it can consume records in any order it like.

-- This combination of features means that Kafka consumers are very cheap—they can come and go without much impact on the cluster or 
on other consumers.

The partitions in the log serves several purposes: 
1) Scalability :: They allow the log to scale beyond a size that will fit on a single server.
Each partition would have a maximum size limit, so after it reaches the maximum value, the next messages will get
stored into a different partition.

2) Distributions :: The partitions of the log are distributed over the servers in kafka cluster with each server handing data and 
requests for a share of the partition. Each partition is replicated across a configurable number of servers for fault tolerance.
Each partition has one server which acts as the leader and zero or more which act as followers. 
The leader handles all read and write requests for the partition while the followers passively replicate the leader. If leader fails one
of the follower picks up the responsibility of a leader.
LOAD BALANCING : Each server acts as a leader for one of the partitions and followerfor other partitions, so load is well balanced.

3) Kafka partitions and record consumptions:
===============================================
kafka partitions are a way to scale up the rate of input records coming in. Lets assume your topic has 4 partitions. 
So, if you consumer group has one consumer instance consuming from that topic, so one consumer instance is receiving messages from 
4 partitions. Now, if number of partition keeps growing, a single consumer instance will not be able to keep the load.
Now if you create another consumer instance in your consumer group, each consumer instance will consume records from 2 partitions at
any point of time. Now if you add two more consumer instances then each instance will receive records from one partition only.
So the load will be distributed among these consumer instances.
-- so this is the way we scale up input volume 
-- But we should never have number of consumer instances more than the number of partitions or few instances will be idle.
-- If one instance dies, the partition attached to that, will be distributed to the other instances.
-- This is done by Kafka protocol dynamically.

** you create a new consumer group for each application that needs all the messages from one or more topics. You add consumers to an 
existing consumer group to scale the reading and processing of messages from the topics, so each additional consumer in a group will 
only get a subset of the messages.

*** while paritiions speed up the processing at consumer side, it violates message ordering guarantees

Producer 
=============
pulishes data to kafka topics. The producer is responsible for assigning a particular record to a partition on a topic.
This can be done in a round-robin fashion simply to balance load or it can be done according to some semantic partition function.

Consumers
=============
Consumer label themselves with a consumer group name. Each record published to a topic is delivered to one consumer instance within
one consumer group.
-- If all the consumer instances have different consumer groups, then each record will be broadcast to all the consumer processes.
-- If all the consumer instances have the same consumer group, then the records will effectively be load balanced over the consumer 
instances.

Rebalance
======================
when a consumer dies or a consumer is added to a consumer group, or a new partition is added to the topic, the consumers are being 
reshuffled to adjust the recent changes. This is called rebalancing.

During a rebalance, consumers can’t consume messages, so a rebalance is basically a short window of unavailability of the entire consumer 
group. We always need to make sure we are avoiding rebalancing as much as possible.

*** The way consumers maintain membership in a consumer group and ownership of the partitions assigned to them is by sending heartbeats 
to a Kafka broker designated as the GROUP COORDINATOR (this broker can be different for different consumer groups).


Kafka broker
==========================
Kafka broker is a single kafka server(node) in a kafka cluster. The broker receives messages from producer, assigns offset and commits 
to persistent storgae on disk. 
It also services consumers, responds to fetch request for partitions and sends them the records writtent to disk.
-- A single broker can easily handle thousands of partitions and millions of messages per second.
-- The partitions of each topic are distributed in these brokers.

Within a cluster of brokers, one broker gets selected as the cluster controller(automatically elected from the live members). The 
controller is responsible for the administrative work in the cluster - assigning partitions to the brokers and monitoring the brokers.
A partition is owned by a single broker, and it is called the leader of that partition. A partition gets assigned to multiple brokers
in a cluster maintaining replication, if a leader of a partition fails , automatically the follower broker becomes the leader of that 
partition. 

-- Leader of a partition is responsible for replicating the data written to that partition into the other brokers.

RETENTIONs are two types:
1) Based on a time (default 7 days) after that time, messages will be discarded.
2) Based on topic size (default 1 GB), if crosses that size, older messages will be discarded out.

-- Individual topics can also be configured with their own retention settings.

*** We can have multiple Kafka clusters as input grows in multiple data centers. Replication mechanism between different data centers 
are handled by a tool called MirroMaker. MirrorMaker is simply a Kafka consumer and producer, linked together with a queue. Messages 
are consumed from one Kafka cluster and produced for another.


Replication fundamentals
==================================================
A partition is stored on a single disk. Kafka guarantees order of events within a partition and a partition can be either online 
(available) or offline (unavailable). Each partition can have multiple replicas, one of which is a designated leader. All events are 
produced to and consumed from the leader replica. Other replicas just need to stay in sync with the leader and replicate all the recent
events on time. If the leader becomes unavailable, one of the in-sync replicas becomes the new leader.

A replica is considered in-sync if it is the leader for a partition, or if it is a follower that:
1) Has an active session with Zookeeper—meaning, it sent a heartbeat to Zookeeper in the last 6 seconds (configurable).
2) Fetched messages from the leader in the last 10 seconds (configurable).
3) Fetched the most recent messages from the leader in the last 10 seconds. That is, it isn’t enough that the follower is still getting
messages from the leader; it must have almost no lag.

An in-sync replica that is slightly behind can slow down producers and consumers, since they wait for all the in-sync replicas to get 
the message before it is committed. Once a replica falls out of sync, we no longer wait for it to get messages. It is still behind, but 
now there is no performance impact. The catch is that with fewer in-sync replicas, the effective replication factor of the partition is
lower and therefore there is a higher risk for downtime or data loss.

