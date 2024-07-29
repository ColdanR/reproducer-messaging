package de.ravenguard.quarkus.reproducer.messaging;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.ConsumerTask;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
public class MessageListenerTest {

  @InjectKafkaCompanion
  KafkaCompanion companion;

  @Test
  void testEmitInTransaction() {
    companion.produceIntegers().usingGenerator(i -> new ProducerRecord<>("in", i));

    ConsumerTask<String, Integer> orders = companion.consumeIntegers().fromTopics("out", 10);
    orders.awaitCompletion();
    assertEquals(10, orders.count());
  }

  @Test
  void testEmitInTransaction2() {
    companion.produceIntegers().usingGenerator(i -> new ProducerRecord<>("in2", i));

    ConsumerTask<String, Integer> orders = companion.consumeIntegers().fromTopics("out2", 10);
    orders.awaitCompletion();
    assertEquals(10, orders.count());
  }

  @Test
  void testEmitInTransaction3() {
    companion.produceIntegers().usingGenerator(i -> new ProducerRecord<>("in3", i));

    ConsumerTask<String, Integer> orders = companion.consumeIntegers().fromTopics("out3", 10);
    orders.awaitCompletion();
    assertEquals(10, orders.count());
  }
}