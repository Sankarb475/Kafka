Zookeeper:
================================
Kafka uses Zookeeper to store the metadata about kafka cluster. Broker and kafka topic details get stored in Zookeeper as well as the
consumer client details such as offset values. You can use zookeeper script placed in kafka distribution, but full version of zookeeper
is not really required for Kafka.

parameters to be set :
> tickTime=2000
> dataDir=/var/lib/zookeeper
> clientPort=2181

> zkServer.sh start  
-- this is how you start your zookeeper server when you have zookeeper installed separately
-- if youre using the zookeeper script comes with Kafka itself, you would do this:
zookeeper-server-start.sh config/zookeeper.properties

** A zookeeper server is called ensemble, and you should have odd numbers of nodes in an ensemble.

Kafka Broker
===============================
The nodes which run as kafka brokers has to have these config parameters updated (in server.properties file):

broker.id = <unique integer within a cluster>
zookeeper.conect = <IP_Address1>:<port = 2181>,<IP_Adress2>:<port = 2181>,.......

Optional ==> zookeeper.connection.timeout.ms=6000
# A comma seperated list of directories under which to store log files
log.dirs=/tmp/kafka-logs

Start the kafka broker:
> kafka-server-start.sh -daemon /usr/local/kafka/config/server.properties

Creating and verifying a topic::
for older version of kafka
> kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test   
for newer version of kafka
> kafka-topics.sh --create --bootstrap-server localhost:2181 --replication-factor 1 --partitions --topic test

Verifying the existing topic::
> kafka-topics.sh --zookeeper localhost:2181 --describe --topic test 


log.dirs ==> 
Kafka persists all messages to disk, and these log segments are stored in the directories specified in the log.dirs configuration. 
This is a comma-separated list of paths on the local system. If more than one path is specified, the broker will store partitions on
them in a “least-used” fashion with one partition’s log segments stored within the same path. Note that the broker will place a new 
partition in the path that has the least number of partitions currently stored in it, not the least amount of disk space
used in the following situations

log.retention.ms ==>
The message retention time for kafka, by default it is 168 hours that is 1 week.
We can configure it using this parameter.

log.retention.bytes ==>
This is another way to delete the records. So, this parameter defines the maximum size of a partition in Kafka topic.
If you have 8 partitions on a topic, and this parameter is set to 1 GB, your partition at max will be able to store 8 GB of data.

message.max.bytes ==>
This sets the maximum record size a producer can write to a kafka partition. By default it is 1 MB.


Listing out all the topics created ::
======================================
> bin/kafka-topics.sh --list --bootstrap-server localhost:9092

-- for standalone kafka, this will suffice. But for a Kafka cluster to be set up, we would need to set many config parameters.

*** broker.id ==> Each broker should have unique value for this within a single kafka cluster. By default it is set to 0. 

*** num.partitions ==> The num.partitions parameter determines how many partitions a new topic is created with, primarily when automatic 
topic creation is enabled (which is the default setting). This parameter defaults to one partition. Keep in mind that the number of
partitions for a topic can only be increased, never decreased.


Kafka Consumer and Producer set up::
==========================================
metadata.broker.list=PUBLIC_IP_ADDRESS_OF_FIRST_KAFKA_BROKER:9092, PUBLIC_IP_ADDRESS_OF_SECOND_KAFKA_BROKER:9092


Running Kafka in windows local 
=============================================
Step 1
========
First create a directory inside kafka/ as "data" and a directory "zookeeper" inside "data"
so you will end up having "/Kafka/data/zookeeper"

Then we need to set a parameter inside Kafka/config/zookeeper.properties.
dataDir=\Kafka\data\zookeeper

Step 2
========
Run the lightweight zookeeper server residing inside "Kafka/bin/windows/"

Open one CMD and run this below command:
>> .\bin\windows\zookeeper-server-start.bat config\zookeeper.properties


Step 3
========
Create another directory in "kafka" inside "/Kafka/data/"
and we need to update this parameter in server.properties

log.dirs=D:\App\kafka_2.11-2.4.1\data\kafka

Open another CMD and run the kafka broker using this below command:
>>.\bin\windows\kafka-server-start.bat .\config\server.properties





