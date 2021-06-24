Kafka Configurations
======================================
kafka-env.sh
---------------------
-- Set KAFKA specific environment variables here like JAVA_HOME, LOG_DIR
-- Add kafka sink to classpath and related depenencies
like - export CLASSPATH=$CLASSPATH:/usr/lib/ambari-metrics-kafka-sink/ambari-metrics-kafka-sink.jar


server.properties
---------------------
zookeeper.connect
-- give details of the IP:PORT of your zookeeper servers

port
-- your kafka brokers will be connecting through this port

-- all offset related parameters are configured here
offset.metadata.max.bytes=4096
offsets.commit.required.acks=-1
offsets.commit.timeout.ms=5000
offsets.load.buffer.size=5242880
offsets.retention.check.interval.ms=600000
offsets.retention.minutes=86400000
offsets.topic.compression.codec=0
offsets.topic.num.partitions=50
offsets.topic.replication.factor=3
offsets.topic.segment.bytes=104857600

other important parameters - 
auto.create.topics.enable=true
auto.leader.rebalance.enable=true
compression.type=producer
controlled.shutdown.enable=true
controlled.shutdown.max.retries=3
default.replication.factor=1
delete.topic.enable=true


num.partitions (default is 1)	
-- The default number of partitions per topic if a partition count isn't given at topic creation time.

message.max.bytes (default is 1 MB)
-- The maximum size of a message that the broker can receive from producer. It is important that this property be in 
sync with the maximum fetch size your consumers use or else an unruly producer will be able to publish 
messages too large for consumers to consume

replica.fetch.max.bytes (default is 1 MB or 1048576)
-- Maximum size of data that a broker can replicate. This has to be larger than message.max.bytes, or a broker will 
accept messages and fail to replicate them. Leading to potential data loss.

max.message.bytes 
-- this is the largest size of the message the broker will allow to be appended to the topic

fetch.message.max.bytes 
-- this will determine the largest size of a message that can be fetched by the consumer.



Each arriving message at the Kafka broker is written into a segment file. The catch here is that this data is not written to the disk directly. 
It is buffered first. The below two properties define when data will be flushed to disk. Very large flush intervals may lead to latency spikes 
when the flush happens and a very small flush interval may lead to excessive seeks.

log.flush.interval.messages: Threshold for message count that is once reached all messages are flushed to the disk.

log.flush.interval.ms: Periodic time interval after which all messages will be flushed into the disk.


consumer.properties
--------------------------------------------
[kafka@nvmbdprp011112 config]$ cat consumer.properties

key.serializer	
-- org.apache.kafka.common.serialization.Serialize

value.serializer
-- org.apache.kafka.common.serialization.Serialize

# consumer group id
group.id=test-consumer-group

bootstrap-servers
-- A list of host/port pairs to use for establishing the initial connection to the Kafka cluster. Data will be load balanced over all servers 
irrespective of which servers are specified here for bootstrapping—this list only impacts the initial hosts used to discover the full set 
of servers. This list should be in the form host1:port1,host2:port2,.... Since these servers are just used for the initial connection to 
discover the full cluster membership (which may change dynamically), this list need not contain the full set of servers (you may want more 
than one, though, in case a server is down). If no server in this list is available sending data will fail until on becomes available.


fetch.min.bytes
-- The minimum amount of data the server should return for a fetch request. If insufficient data is available the request will wait for 
that much data to accumulate before answering the request. The default setting of 1 byte means that fetch requests are answered as 
soon as a single byte of data is available or the fetch request times out waiting for data to arrive. Setting this to something greater 
than 1 will cause the server to wait for larger amounts of data to accumulate which can improve server throughput a bit at the cost 
of some additional latency.

max.partition.fetch.bytes	
-- The maximum amount of data per-partition the server will return. Records are fetched in batches by the consumer. If the first record
 batch in the first non-empty partition of the fetch is larger than this limit, the batch will still be returned to ensure that the 
 consumer can make progress. The maximum record batch size accepted by the broker is defined via message.max.bytes (broker config) or 
 max.message.bytes (topic config). See fetch.max.bytes for limiting the consumer request size.

auto.offset.reset
-- What to do when there is no initial offset in ZooKeeper or if an offset is out of range:
* smallest : automatically reset the offset to the smallest offset
* largest : automatically reset the offset to the largest offset
* anything else: throw exception to the consumer


fetch.message.max.bytes 
-- this will determine the largest size of a message that can be fetched by the consumer.

client.id => group.id
-- The client id is a user-specified string sent in each request to help trace calls. 
It should logically identify the application making the request.



producer.properties
--------------------------------------------
key.serializer	
-- org.apache.kafka.common.serialization.Serialize

value.serializer
-- org.apache.kafka.common.serialization.Serialize

bootstrap.servers	
-- A list of host/port pairs to use for establishing the initial connection to the Kafka cluster

max.request.size	
-- The maximum size of a request in bytes. This setting will limit the number of record batches 
the producer will send in a single request to avoid sending huge requests. This is also effectively 
a cap on the maximum record batch size. Note that the server has its own cap on record batch size 
which may be different from this.


batch.size	
-- The producer will attempt to batch records together into fewer requests whenever multiple records 
are being sent to the same partition. This helps performance on both the client and the server. 
This configuration controls the default batch size in bytes.
-- Requests sent to brokers will contain multiple batches, one for each partition with data available to be sent.


zookeeper.properties
---------------------------
[kafka@nvmbdprp011112 config]$ cat zookeeper.properties

# the directory where the snapshot is stored.
dataDir=/tmp/zookeeper
# the port at which the clients will connect
clientPort=2181
# disable the per-ip limit on the number of connections since this is a non-production config
maxClientCnxns=0



Creating a kafka topic 
=====================================
/usr/hdp/3.1.0.28-1/kafka/bin/kafka-topics.sh --create --zookeeper 10.143.172.204 --replication-factor 2 --partitions 5 --topic practice

[kafka@nvmbdprp011112 root]$ /usr/hdp/3.1.0.28-1/kafka/bin/kafka-topics.sh --create --zookeeper 10.143.172.204 --replication-factor 2 --partitions 5 --topic practice
Created topic "practice".


finding all the topics present
-- /usr/hdp/3.1.0.28-1/kafka/bin/kafka-topics.sh --list --zookeeper 10.143.172.204


find the details of a particular kafka topic
-- /usr/hdp/3.1.0.28-1/kafka/bin/kafka-topics.sh --describe --zookeeper 10.143.172.204 --topic practice

[kafka@nvmbdprp011112 root]$ /usr/hdp/3.1.0.28-1/kafka/bin/kafka-topics.sh --describe --zookeeper 10.143.172.204 --topic practice
Topic:practice  PartitionCount:5        ReplicationFactor:2     Configs:
        Topic: practice Partition: 0    Leader: 1004    Replicas: 1004,1001     Isr: 1004,1001
        Topic: practice Partition: 1    Leader: 1001    Replicas: 1001,1002     Isr: 1001,1002
        Topic: practice Partition: 2    Leader: 1002    Replicas: 1002,1003     Isr: 1002,1003
        Topic: practice Partition: 3    Leader: 1003    Replicas: 1003,1004     Isr: 1003,1004
        Topic: practice Partition: 4    Leader: 1004    Replicas: 1004,1002     Isr: 1004,1002


find kafka configs 
----------------------------------------
ps aux | grep kafka | grep -i "server.properties"


checking kafka version
---------------------------------------
[kafka@nvmbdprp011112 kafka]$ ./bin/kafka-topics.sh --version
2.0.0.3.1.0.314-3 (Commit:669a8e0ec53221c0)


how to find the broker list
----------------------------------------
[kafka@nvmbdprp011112 kafka]$ ./bin/kafka-broker-api-versions.sh --bootstrap-server 10.143.168.76:6667 | grep 6667
bdpdata1458.jio.com:6667 (id: 1001 rack: null) -> (
nvmbdprp011112.jio.com:6667 (id: 1002 rack: null) -> (
nvmbdprp011111.jio.com:6667 (id: 1004 rack: null) -> (
bdpdata1459.jio.com:6667 (id: 1003 rack: null) -> (




connect from Spark Streaming
==========================================
spark-shell --master yarn --executor-memory 5g --num-executors 10 --executor-cores 4 --driver-memory 3G --jars ojdbc6-11.2.0.2.0.jar ,spark-streaming-kafka-0-10_2.11-2.3.2.jar ,spark-sql-kafka-0-10_2.11-2.3.2.jar ,kafka-clients-2.0.0.jar ,encryptionmodule_2.11-1.0.jar ,ojdbc7-12.1.0.2.jar ,kafka-clients-0.10.0.2.0.2.2-2.jar ,module_2.11-2.0.jar


import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import java.util.Properties

//import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.functions.concat
import org.apache.spark.sql.functions.when
import org.apache.spark.sql.functions.lit
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent


// val conf = new SparkConf().set("spark.streaming.kafka.consumer.cache.enabled", "false")
// val sc = new SparkContext(conf)
// val spark = SparkSession.builder().config(conf).getOrCreate()
spark.conf.set("spark.streaming.kafka.consumer.cache.enabled",false)


	  
val props = new Properties()
props.put("bootstrap.servers", "10.143.168.76:6667")
props.put("client.id", "testing")
props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")


val ssc = new StreamingContext(spark.sparkContext, Seconds(1))
val topicsSet = "practice"


val ds = df
  .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
  .writeStream
  .format("kafka")
  .option("kafka.bootstrap.servers", "10.143.168.76:6667")
  .option("topic", "practice")
  .start()

// Write key-value data from a DataFrame to Kafka using a topic specified in the data
val ds = df
  .selectExpr("topic", "CAST(key AS STRING)", "CAST(value AS STRING)")
  .writeStream
  .format("kafka")
  .option("kafka.bootstrap.servers", "10.143.168.76:6667")
  .start()




