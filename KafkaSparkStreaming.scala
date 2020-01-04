The key.serializer and value.serializer instruct how to turn the key and value objects the user provides with their ProducerRecord into 
bytes. You can use the included ByteArraySerializer or StringSerializer for simple string or byte types.
 
Kafka and Spark streaming integration ::
=====================================================================================================================
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

val kafkaParams = Map[String, Object](
  "bootstrap.servers" -> "localhost:9092,anotherhost:9092",
  "key.deserializer" -> classOf[StringDeserializer],
  "value.deserializer" -> classOf[StringDeserializer],
  "group.id" -> "use_a_separate_group_id_for_each_stream",
  "auto.offset.reset" -> "latest",
  "enable.auto.commit" -> (false: java.lang.Boolean)
)

val topics = Array("topicA", "topicB")
val stream = KafkaUtils.createDirectStream[String, String](
  streamingContext,
  PreferConsistent,
  Subscribe[String, String](topics, kafkaParams)
)

stream.map(record => (record.key, record.value))
=======================================================================================================================
LocationStrategy => allows a direct kafka input dstream to request spark executors to execute kafka consumer as close topic leaders 
of topic partitions as possible.
  PreferBrokers : Use when executors are on the same nodes as your Kafka brokers.
  PreferConsistent : Use in most cases as it consistently distributes partitions across all executors.
  PreferFixed :

ConsumerStrategy => ConsumerStrategy is a contract to create Kafka Consumers in a Spark Streaming application that allows for their 
custom configuration after the consumers have been created.
  Assign, Subscribe, and SubscribePattern 
  
 
