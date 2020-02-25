package com.dg.sample.vertx.olx.data;

import com.dg.sample.vertx.olx.data.httpserver.HttpServerVerticle;
import com.dg.sample.vertx.olx.data.utils.PropertyData;
import com.dg.sample.vertx.olx.data.webclient.OlxWebClientVerticle;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import java.util.Optional;
import java.util.Properties;

public class MainVerticle extends AbstractVerticle {

  private static final Properties PROPERTIES = PropertyData.getApplicationProperties();
  private Verticle webServerVerticle;
  private Verticle webClientVerticle;

  public MainVerticle() {
    //getVertx() returns null when running using exec plugin
    Vertx vertx = Optional.ofNullable(getVertx())
      .orElse(Vertx.vertx());
    this.webClientVerticle = OlxWebClientVerticle.getInstance(vertx);
    this.webServerVerticle = new HttpServerVerticle();
  }

  public MainVerticle(Verticle webServerVerticle, Verticle webClientVerticle) {
    this.webServerVerticle = webServerVerticle;
    this.webClientVerticle = webClientVerticle;
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Promise<String> olxWebClientDeployment = Promise.promise();
    vertx.deployVerticle(this.webClientVerticle, new DeploymentOptions().setConfig(JsonObject.mapFrom(PROPERTIES)),olxWebClientDeployment);

    olxWebClientDeployment.future().compose(id -> {
      Promise<String> httpVerticleDeployment = Promise.promise();
      vertx.deployVerticle(this.webServerVerticle,
        new DeploymentOptions().setConfig(JsonObject.mapFrom(PROPERTIES)),
        httpVerticleDeployment);

      return httpVerticleDeployment.future();

    }).setHandler(ar -> {
      if (ar.succeeded()) {
        startPromise.complete();
      } else {
        startPromise.fail(ar.cause());
      }
    });
  }
}
