package com.dg.sample.vertx.olx.data;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

  private static final String SERVER_PORT_PROPERTY = "server.port";
  private static final String OLX_ENDPOINT_URL = "olx.endpoint.url";
  private static final String OLX_ENDPOINT_PATH = "olx.endpoint.path";
  private static final String OLX_ENDPOINT_PORT = "olx.endpoint.port";
  private static final String KEYWORD_PLACEHOLDER = "\\$\\{keyword}";
  //private static final String OLX_DATA_FETCHER_SERVICE_PROPERTY = "olx.data.fetcher.queue";

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    String serverPort = this.config().getString(SERVER_PORT_PROPERTY, "8080");

    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);
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
    WebClient client = WebClient.create(vertx, new WebClientOptions().setSsl(true));
    String olxEndpointUrl = config().getString(OLX_ENDPOINT_URL, "www.olx.pl");
    String olxEndpointPath = config().getString(OLX_ENDPOINT_PATH, "/oferty/q-${keyword}")
      .replaceFirst(KEYWORD_PLACEHOLDER, keyword);
    Integer olxPort = Integer.valueOf(config().getString(OLX_ENDPOINT_PORT, "443"));
    client.get(olxPort, olxEndpointUrl, olxEndpointPath)
      .send(res -> {
        if (res.succeeded()) {
          LOGGER.info("Request succed!");
          HttpResponse<Buffer> result = res.result();
          String body = result.bodyAsString();
          JsonObject resultBody = HTMLOlxSearchResultParser.parseHtmlResults(body);
          routingContext.response()
            .setStatusCode(200)
            .end(resultBody.toBuffer());
        }
        else{
          LOGGER.error("Error when invoking olx endpoint {}", res.cause());
          routingContext.response()
            .setStatusCode(500)
            .end();
        }
      });
  }

  static class  HTMLOlxSearchResultParser{

    public static JsonObject parseHtmlResults(String htmlDocument){
      Document document = Jsoup.parse(htmlDocument);
      Elements offersElements = document.select("tr.wrap > td.offer > div.offer-wrapper > table");
      JsonArray offersArray = offersElements.stream()
        .map( HTMLOlxSearchResultParser::MapElementToJSONObject)
        .collect(JsonArray::new, (a, o) -> a.add(o), (a1, a2) -> a1.addAll(a2) );
      JsonObject offers = new JsonObject();
      offers.put("offers", offersArray);
      return offers;
    }

    private static JsonObject MapElementToJSONObject(Element element) {
      JsonObject offerEntry = new JsonObject();
      offerEntry.put("id", element.attr("data-id"));
      offerEntry.put("name", element.selectFirst("tbody > tr > td.title-cell > div > h3 > a > strong").html());
      offerEntry.put("price", element.selectFirst("tbody > tr > td.td-price > div > p.price > strong").html());
      return offerEntry;
    }
  }


}
