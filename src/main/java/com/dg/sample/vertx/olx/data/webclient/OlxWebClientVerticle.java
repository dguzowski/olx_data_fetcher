package com.dg.sample.vertx.olx.data.webclient;

import com.dg.sample.vertx.olx.data.utils.PropertyData;
import com.dg.sample.vertx.olx.data.webclient.helpers.HtmlAsyncDocumentDataFetcher;
import com.dg.sample.vertx.olx.data.webclient.helpers.HtmlDocumentDataParser;
import com.dg.sample.vertx.olx.data.webclient.helpers.OlxOfferHtmlDocumentDataFetcher;
import com.dg.sample.vertx.olx.data.webclient.helpers.OlxOfferHtmlDocumentDataParser;
import com.dg.sample.vertx.olx.data.webclient.models.DocumentData;
import com.dg.sample.vertx.olx.data.webclient.models.OfferData;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OlxWebClientVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(OlxWebClientVerticle.class);

  private HtmlAsyncDocumentDataFetcher<DocumentData> fetcher;
  private HtmlDocumentDataParser<DocumentData, List<OfferData>> parser;

  private OlxWebClientVerticle(HtmlAsyncDocumentDataFetcher<DocumentData> fetcher, HtmlDocumentDataParser<DocumentData, List<OfferData>> parser) {
    this.fetcher = fetcher;
    this.parser = parser;
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    String olxWebClientChannel = config().getString(PropertyData.OLX_OFFER_DATA_QUEUE_PROP, "olx.offer.data");
    getVertx().eventBus().consumer(olxWebClientChannel, this::onMessage);
    LOGGER.info("Olx Web Client has started and waiting for messages on: {} queue", olxWebClientChannel);
    startPromise.complete();
  }

  private void onMessage(Message<JsonObject> tMessage) {
    String keyword = tMessage.body().getString("keyword", "keyword");
    fetcher.getDocumentData(keyword).future()
      .onSuccess( result -> handleSuccessfulOlxResponse(result, tMessage))
      .onFailure( result -> handleFailureOlxResponse(result, tMessage));
  }

  private void handleSuccessfulOlxResponse(DocumentData documentData, Message<JsonObject> tMessage) {
    List<OfferData> offers = parser.parseDocumentData(documentData);
    LOGGER.debug("Olx offers data retrieval operation succeed. Remote raw data: \n{}",documentData.getBody());
    tMessage.reply(convertToJsonObject(offers));
  }

  private JsonObject convertToJsonObject(List<OfferData> offers) {
    JsonObject offersJson = new JsonObject();
    offersJson.put("offers", (JsonArray) offers.stream()
      .map(OfferData::toJsonObject)
      .collect(JsonArray::new, (a,b) -> a.add(b), (a1, a2) -> a1.addAll(a2)));
    return offersJson;
  }

  private void handleFailureOlxResponse(Throwable cause, Message<JsonObject> tMessage) {
    LOGGER.error("Olx offers data retrieval operation failed: {}", cause.getMessage());
    tMessage.fail(500, cause.getMessage());
  }

  public static OlxWebClientVerticle getInstance(Vertx vertx){
    HtmlAsyncDocumentDataFetcher<DocumentData> documentFetcher = new OlxOfferHtmlDocumentDataFetcher.Builder()
        .setHost(PropertyData.getApplicationProperties().getProperty(PropertyData.OLX_ENDPOINT_URL_PROP))
        .setPort(Integer.parseInt(PropertyData.getApplicationProperties().getProperty(PropertyData.OLX_ENDPOINT_PORT_PROP)))
        .setPath(PropertyData.getApplicationProperties().getProperty(PropertyData.OLX_ENDPOINT_PATH_PROP))
        .setSecure(true)
        .setVertx(vertx)
        .build();
      HtmlDocumentDataParser<DocumentData, List<OfferData>> parser = new OlxOfferHtmlDocumentDataParser();
      return new OlxWebClientVerticle(documentFetcher, parser);
    }

    public static OlxWebClientVerticle getInstance(HtmlAsyncDocumentDataFetcher<DocumentData> documentDataFetcher, HtmlDocumentDataParser<DocumentData, List<OfferData>> documentDataParser){
        return new OlxWebClientVerticle(documentDataFetcher, documentDataParser);
    }
}

