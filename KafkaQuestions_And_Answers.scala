1) How to increase the number of partitions of a topic?
==>
Kafka only lets you increase the number of partitions, you cannot decrease it.

> kafka-topics.sh --alter --zookeeper localhost:2182 --topic test --patitions 5

** Using AdminUtils you can increase the number of partitions from your producer
> AdminUtils.addPartitions()

======================================================================================================================================
2) 
