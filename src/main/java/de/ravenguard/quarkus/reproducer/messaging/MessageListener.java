package de.ravenguard.quarkus.reproducer.messaging;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.transactions.KafkaTransactions;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.reactive.messaging.*;

@ApplicationScoped
public class MessageListener {
  @Channel("out")
  @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 500)
  KafkaTransactions<Integer> txProducer;

  @Channel("out2")
  @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 500)
  KafkaTransactions<Integer> txProducer2;

  @Channel("out3")
  Emitter<Integer> integerEmitter;

  /**
   * Failing Listener using Kafka Transaction and Mutiny.
   *
   * @param message Message from Kafka Topic
   * @return Uni for Completion
   */
  @Incoming("in")
  @Retry(delay = 1000)
  public Uni<Void> emitInTransaction(Message<Integer> message) {
    return txProducer.withTransactionAndAck(message, emitter ->
        Uni.createFrom()
            .item(message)
            .map(Message::getPayload)
            .chain(payload -> {
              emitter.send(payload + 1);
              return Uni.createFrom().voidItem();
            })
    );
  }

  /**
   * Successful Listener with Kafka Transaction and without Mutiny.
   *
   * @param message Message from Kafka Topic
   * @return Uni for Completion
   */
  @Incoming("in2")
  @Retry(delay = 1000)
  public Uni<Void> emitInTransaction2(Message<Integer> message) {
    return txProducer2.withTransactionAndAck(message, emitter -> {
          emitter.send(message.getPayload() + 1);
          return Uni.createFrom().voidItem();
        }
    );
  }

  /**
   * Successful Listener without Kafka Transactions and with Mutiny.
   *
   * @param message Message from Kafka Topic
   * @return Uni for Completion
   */
  @Incoming("in3")
  @Retry(delay = 1000)
  public Uni<Void> emitInTransaction3(Message<Integer> message) {
    return Uni.createFrom()
        .item(message)
        .map(Message::getPayload)
        .flatMap(i -> Uni.createFrom().item(i + 1))
        .chain(payload -> {
          integerEmitter.send(payload);
          return Uni.createFrom().voidItem();
        });
  }
}