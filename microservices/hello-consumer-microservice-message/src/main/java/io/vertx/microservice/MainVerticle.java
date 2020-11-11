package io.vertx.microservice;

import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.core.RxHelper;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.eventbus.Message;
import rx.Single;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainVerticle extends AbstractVerticle {

  private final Logger logger = Logger.getLogger(MainVerticle.class.getName());

  @Override
  public void start(Promise<Void> promise) {
    logger.info("Starting Hello Consumer Microservice Message verticle.");

    vertx.createHttpServer()
      .requestHandler(
        req -> {
          EventBus bus = vertx.eventBus();
//          produceNoFailMessage(bus, req);
          produceFaultTolerance(bus, req);

        })
      .listen(8082, http -> {
        if(http.succeeded()) {
          logger.info("HTTP server started on port 8082");
          promise.complete();
        } else {
          logger.log(Level.SEVERE, http.cause().getMessage());
          promise.fail(http.cause());
        }
      });
  }

  private void produceNoFailMessage(EventBus bus, HttpServerRequest req) {
    Single<JsonObject> obs1 = bus.<JsonObject>rxSend("hello", "Luke").map(Message::body);
    Single<JsonObject> obs2 = bus.<JsonObject>rxSend("hello", "Leia").map(Message::body);
    Single<JsonObject> obs3 = bus.<JsonObject>rxSend("hello", "Darth Vader").map(Message::body);
    Single<JsonObject> obs4 = bus.<JsonObject>rxSend("hello", "Han Solo").map(Message::body);

    Single.zip(obs1, obs2, obs3, obs4, (luke, leia, darthVader, hanSolo) ->
      new JsonObject()
        .put("Luke", luke.getString("message") + " from " + luke.getString("served-by"))
        .put("Leia", leia.getString("message") + " from " + leia.getString("served-by"))
        .put("Darth Vader", darthVader.getString("message") + " from " + leia.getString("served-by"))
        .put("Han Solo", hanSolo.getString("message") + " from " + leia.getString("served-by"))
    ).subscribe(
      x -> req.response().end(x.encodePrettily()),
      t -> {
        logger.log(Level.SEVERE, t.getMessage());
        req.response().setStatusCode(500).end(t.getMessage());
      }
    );
  }

  private void produceFaultTolerance(EventBus bus, HttpServerRequest req) {
    Single<JsonObject> obs1 = bus.<JsonObject>rxSend("hello", "Luke").subscribeOn(RxHelper.scheduler(vertx)).timeout(3, TimeUnit.SECONDS).retry().map(Message::body);
    Single<JsonObject> obs2 = bus.<JsonObject>rxSend("hello", "Leia").subscribeOn(RxHelper.scheduler(vertx)).timeout(3, TimeUnit.SECONDS).retry().map(Message::body);
    Single<JsonObject> obs3 = bus.<JsonObject>rxSend("hello", "Darth Vader").subscribeOn(RxHelper.scheduler(vertx)).timeout(3, TimeUnit.SECONDS).retry().map(Message::body);
    Single<JsonObject> obs4 = bus.<JsonObject>rxSend("hello", "Han Solo").subscribeOn(RxHelper.scheduler(vertx)).timeout(3, TimeUnit.SECONDS).retry().map(Message::body);

    Single.zip(obs1, obs2, obs3, obs4, (luke, leia, darthVader, hanSolo) ->
      new JsonObject()
        .put("Luke", luke.getString("message") + " from " + luke.getString("served-by"))
        .put("Leia", leia.getString("message") + " from " + leia.getString("served-by"))
        .put("Darth Vader", darthVader.getString("message") + " from " + darthVader.getString("served-by"))
        .put("Han Solo", hanSolo.getString("message") + " from " + hanSolo.getString("served-by"))
    ).subscribe(
      x -> req.response().end(x.encodePrettily()),
      t -> {
        logger.log(Level.SEVERE, t.getMessage());
        req.response().setStatusCode(500).end(t.getMessage());
      }
    );
  }
}
