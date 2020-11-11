package io.vertx.sample;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;

public class MyFirstRXVerticle extends AbstractVerticle {
    HttpServer server = vertx.createHttpServer();
    server.requestStream().toObservable()
        .subscribe(req ->
            req.response()
            .end("Hello from Vert.x from " + Thread.currentThread().getName())
        );

    server
    .rxListen(8080)
    .subscribe()

}
