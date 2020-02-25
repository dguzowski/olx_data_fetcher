package com.dg.sample.vertx.olx.data.httpserver;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dg.sample.vertx.olx.data.utils.PropertyData.OLX_OFFER_DATA_QUEUE_PROP;
import static com.dg.sample.vertx.olx.data.utils.PropertyData.SERVER_PORT_PROPERTY_PROP;

public class HttpServerVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

  private Router router;

  public HttpServerVerticle(){
    this.router = Router.router(vertx);
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    String serverPort = this.config().getString(SERVER_PORT_PROPERTY_PROP, "8080");

    HttpServer server = vertx.createHttpServer();

    router.get("/offers/olx/:keyword")
      .handler(this::handleOlxSearch);

    server.requestHandler(router).listen(Integer.valueOf(serverPort), http -> {
      if (http.succeeded()) {
        startPromise.complete();
        LOGGER.info("HTTP server started on port {}", serverPort);
      } else {
        LOGGER.error("Cannot start server on port {}", serverPort);
        startPromise.fail(http.cause());
      }
    });
  }

  private void handleOlxSearch(RoutingContext routingContext) {
    String keyword = routingContext.pathParam("keyword");
    JsonObject request = new JsonObject();
    request.put("keyword", keyword);
    String destination = config().getString(OLX_OFFER_DATA_QUEUE_PROP, "olx.data.fetcher.queue");
    getVertx().eventBus().<JsonObject>request(destination, request, reply -> this.handleReply(reply, routingContext));
  }

  private void handleReply(AsyncResult<Message<JsonObject>> reply, RoutingContext rctx) {
    if(reply.succeeded()){
      JsonObject offers = reply.result().body();
      LOGGER.debug("Request succed! Data: \n{}",  offers.encodePrettily());
      rctx.response()
        .setStatusCode(200)
        .end(offers.toBuffer());
    }
    else{
      LOGGER.error("Error when invoking olx endpoint {}", reply.cause());
      rctx.response()
        .setStatusCode(500)
        .end();
    }
  }

}
