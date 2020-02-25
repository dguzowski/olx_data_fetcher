package com.dg.sample.vertx.olx.data;

import com.dg.sample.vertx.olx.data.httpserver.HttpServerVerticle;
import com.dg.sample.vertx.olx.data.test.utils.TestUtils;
import com.dg.sample.vertx.olx.data.webclient.OlxWebClientVerticle;
import com.dg.sample.vertx.olx.data.webclient.helpers.HtmlAsyncDocumentDataFetcher;
import com.dg.sample.vertx.olx.data.webclient.helpers.OlxOfferHtmlDocumentDataParser;
import com.dg.sample.vertx.olx.data.webclient.models.DocumentData;
import io.vertx.core.Promise;
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
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;


@ExtendWith(VertxExtension.class)
public class IntegrationOLXStubTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationOLXStubTest.class);

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    OlxWebClientVerticle webClient = createOlxWebClientWithStubbedOlxData();
    HttpServerVerticle webServer = new HttpServerVerticle();
    vertx.deployVerticle(new MainVerticle(webServer, webClient), id -> testContext.completeNow());
  }

  private OlxWebClientVerticle createOlxWebClientWithStubbedOlxData(){
    return OlxWebClientVerticle.getInstance(getOlxDocumentDataFetcherStub(), new OlxOfferHtmlDocumentDataParser());
  }

  private HtmlAsyncDocumentDataFetcher<DocumentData> getOlxDocumentDataFetcherStub(){
    HtmlAsyncDocumentDataFetcher<DocumentData> dataFetcherMock = Mockito.mock(HtmlAsyncDocumentDataFetcher.class);
    DocumentData data = new DocumentData(200, TestUtils.getOlxHtmlStubbedData());
    Promise<DocumentData> promise = Promise.promise();
    promise.complete(data);
    Mockito.when(dataFetcherMock.getDocumentData(Mockito.anyString()))
      .thenReturn(promise);
    return dataFetcherMock;
  }

  //enable to interact with running server
  @Disabled
  @Test
  void runServer(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
    testContext.awaitCompletion(1, TimeUnit.DAYS);
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
      Assertions.assertEquals(44, offers.getList().size());
      LOGGER.info("\n{}",data.encodePrettily());
      testContext.completeNow();
    });
  }
}
