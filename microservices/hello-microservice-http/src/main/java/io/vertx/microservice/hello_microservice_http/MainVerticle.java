package io.vertx.microservice.hello_microservice_http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> promise) throws Exception {

    Router router = Router.router(vertx);
    router.get("/").handler(this::hello);
    router.get("/:name").handler(this::hello);

    vertx.createHttpServer().requestHandler(router)
      .listen(8080, http -> {
        if (http.succeeded()) {
          promise.complete();
          System.out.println("HTTP server started on port 8080");
        } else {
          promise.fail(http.cause());
        }
      });
  }

  private void hello(RoutingContext rc) {
    String message = "hello";

    if(rc.pathParam("name") != null) {
      message += " " + rc.pathParam(("name"));
    }

    JsonObject json = new JsonObject().put("message", message);
    rc.response()
      .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
      .end(json.encode());
  }
}
