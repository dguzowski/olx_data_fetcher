package com.dg.sample.vertx.olx.data;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestMainVerticle.class);
  private static Properties props = new Properties();

  @BeforeAll
  static void initProperties() throws IOException {
    InputStream resource = TestMainVerticle.class.getResourceAsStream("/app.properties");
    props.load(resource);
  }

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    JsonObject config = JsonObject.mapFrom(props);
    vertx.deployVerticle(new HttpServerVerticle(), new DeploymentOptions().setConfig(config),testContext.succeeding(id -> testContext.completeNow()));
  }

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
      testContext.completeNow();
    });
  }

}
