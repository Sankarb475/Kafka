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
-- if youre using th e

** A zookeeper server is called ensemble, and you should have odd numbers of nodes in an ensemble.

Kafka Broker
===============================
Start the kafka broker:
> kafka-server-start.sh -daemon /usr/local/kafka/config/server.properties

Creating and verifying a topic::
> kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test

Verifying the existing topic::
> kafka-topics.sh --zookeeper localhost:2181 --describe --topic test 

-- for standalone kafka, this will suffice. But for a Kafka cluster to be set up, we would need to set many config parameters.

*** broker.id ==> Each broker should have unique value for this within a single kafka cluster. By default it is set to 0. 

*** num.partitions ==> The num.partitions parameter determines how many partitions a new topic is created with, primarily when automatic 
topic creation is enabled (which is the default setting). This parameter defaults to one partition. Keep in mind that the number of
partitions for a topic can only be increased, never decreased.
