package io.vertx.sample;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> promise) throws Exception {
    vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x from " + Thread.currentThread().getName());
    }).listen(8888, http -> {
      if (http.succeeded()) {
        promise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        promise.fail(http.cause());
      }
    });
  }
}
