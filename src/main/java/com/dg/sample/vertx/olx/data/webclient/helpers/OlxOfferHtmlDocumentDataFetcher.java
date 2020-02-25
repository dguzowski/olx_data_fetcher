package com.dg.sample.vertx.olx.data.webclient.helpers;

import com.dg.sample.vertx.olx.data.webclient.models.DocumentData;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.dg.sample.vertx.olx.data.utils.PropertyData.KEYWORD_PLACEHOLDER_PROP;

public final class OlxOfferHtmlDocumentDataFetcher implements HtmlAsyncDocumentDataFetcher<DocumentData> {

  private static final Logger LOGGER = LoggerFactory.getLogger(OlxOfferHtmlDocumentDataFetcher.class);

  private int port;
  private String host;
  private String path;
  private WebClient client;

  private OlxOfferHtmlDocumentDataFetcher(Vertx vertx, String host, String path, int port, boolean secure){
    this.host = host;
    this.path = path;
    this.port = port;
    this.client = WebClient.create(vertx, new WebClientOptions().setSsl(true));

  }

  @Override
  public Promise<DocumentData> getDocumentData(String keyword) {
    Promise<DocumentData> asyncResult = Promise.promise();
    String path = this.path.replaceFirst(KEYWORD_PLACEHOLDER_PROP, keyword);
    client.get(port, host, path)
      .send( resp -> handleOlxResponse(resp, asyncResult));
    return asyncResult;
  }

  private void handleOlxResponse(AsyncResult<HttpResponse<Buffer>> res, Promise<DocumentData> promise) {
    if (res.succeeded()) {
      LOGGER.info("Request succed!");
      HttpResponse<Buffer> result = res.result();
      String body = result.bodyAsString();
      promise.complete(new DocumentData(res.result().statusCode(), body));
    }
    else{
      promise.fail(res.cause());
    }
  }

  public static class Builder{
    private Integer port;
    private String host;
    private String path;
    private Vertx vertx;
    private boolean secure;

    public Builder setPort(Integer port) {
      this.port = port;
      return this;
    }

    public Builder setHost(String host) {
      this.host = host;
      return this;
    }

    public Builder setPath(String path) {
      this.path = path;
      return this;
    }

    public Builder setVertx(Vertx vertx) {
      this.vertx = vertx;
      return this;
    }

    public Builder setSecure(boolean secure) {
      this.secure = secure;
      return this;
    }

    public HtmlAsyncDocumentDataFetcher<DocumentData> build(){
      String cantBeNullMessage = " can't be null";
      Objects.requireNonNull(vertx, "vertx"+cantBeNullMessage);
      Objects.requireNonNull(host, "vertx"+cantBeNullMessage);
      Objects.requireNonNull(path, "vertx"+cantBeNullMessage);
      Objects.requireNonNull(port, "vertx"+cantBeNullMessage);
      return new OlxOfferHtmlDocumentDataFetcher(vertx,host,path,port,secure);
    }
  }


}
