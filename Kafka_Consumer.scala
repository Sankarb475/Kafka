Kafka Consumer
=====================================================================================================================================
Applications that need to read data from Kafka use a KafkaConsumer to subscribe to Kafka topics and receive messages from these topics.


Consumers and Consumer Groups
=====================================================
If your producer is writing at a rate more than the rate at which your consumer can consume the records. your application may fall farther
and farther behind, unable to keep up with the rate of incoming messages. Thus we need to scale up the consumer rate of consumption.
We do that by increasing the number of consumer instances, so the incoming load gets distributed among the instances. 

Suppose if the producer is writing at a speed of 1 GB per minute, and your consumer instance can consume the data at a rate 50 MB per 
minute, then we would need to 20 such consumer instances to keep up with the producer with no backlog.

-- So since all the 20 consumers would be consuming from the same topic and complete the consumption, they should be binded by something
which is consumer group, so consumer instances part of a consumer group distributes the load amongst themselves, and owns one or more
partition. 

-- If we can have multiple consumer groups consuming from the same topic, the groups would be independently consuming from the topic.

Suppose we have a topic with 4 partitions, and a consumer group with one consumer instance, then the consumer will be consuming from all 
these 4 partitions, and now if you add another consumer instance into the group, the work load will be distributed by the group coordinator
and each instance will own only two partition. But if you keep adding and end up having 4 instances and each instance will consume from 
only one partition. Now if you add another instance, it will remain idle.
--thus, number of consumer instances <= number of partitions

-- Each consumer instance is called the owner/leader of the partition from which it consumes the messages.

Consumer groups and Partition rebalance
===================================================
