package io.vertx.microservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

import java.util.logging.Logger;

public class MainVerticle extends AbstractVerticle {

  private final Logger logger = Logger.getLogger(MainVerticle.class.getName());

  @Override
  public void start(Promise<Void> startPromise) {
    logger.info("Starting Hello Microservice Message verticle.");

    // Receive message from the address 'hello'
    vertx.eventBus().<String>consumer("hello", message -> {
      //      consumeNoFailMessage(message);
      consumeWithFailMessage(message);
    });

    startPromise.complete();
  }

  public void consumeNoFailMessage(Message<String> message) {
    JsonObject json = new JsonObject().put("served-by", this.toString());

    if(message.body().isEmpty()) {
      message.reply(json.put("message", "Hello"));
    } else {
      message.reply(json.put("message", "Hello " + message.body()));
    }
  }

  public void consumeWithFailMessage(Message<String> message) {
    // Randomly selects one of three strategies:
    // (1) reply with an explicit failure,
    // (2) forget to reply (leading to a timeout on the consumer side), or
    // (3) send the correct result.
    // It simulates reply in a timely fashion to the user
    double chaos = Math.random();
    JsonObject json = new JsonObject().put("served-by", this.toString());

    if(chaos < 0.6) {
      // Normal Behavior
      consumeNoFailMessage(message);
    } else if (chaos < 0.9) {
      message.reply(json.put("message", "Hello " + message.body()));
      message.fail(500, "message processing failure");
    } else {
      // Just do not reply, leading to a timeout on the consumer side.
      logger.info("Not replying");
    }
  }
}
