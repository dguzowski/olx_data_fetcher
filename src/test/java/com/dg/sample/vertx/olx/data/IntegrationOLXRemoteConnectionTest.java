package com.dg.sample.vertx.olx.data;

import com.dg.sample.vertx.olx.data.httpserver.HttpServerVerticle;
import com.dg.sample.vertx.olx.data.webclient.OlxWebClientVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


@ExtendWith(VertxExtension.class)
public class IntegrationOLXRemoteConnectionTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationOLXRemoteConnectionTest.class);

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(new HttpServerVerticle(), OlxWebClientVerticle.getInstance(vertx)), id -> testContext.completeNow());
  }


  //enable to interact with running server
  @Disabled
  @Test
  void runServer(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
    testContext.awaitCompletion(1, TimeUnit.DAYS);
  }


  @Disabled
  @Test
  void run(Vertx vertx, VertxTestContext testContext) {
    //given
    WebClient client = WebClient.create(vertx);

    //when
    HttpRequest<Buffer> localhost = client.get(8080, "localhost", "/offers/olx/hyundai");

    //then
    localhost.send( res -> {
      Assertions.assertTrue(res.succeeded());
      JsonObject data = res.result().bodyAsJsonObject();
      JsonArray offers = data.getJsonArray("offers");
      Assertions.assertTrue(offers.getList().size() > 0 );
      LOGGER.info("\n{}", data.encodePrettily());
      testContext.completeNow();
    });
  }

}
