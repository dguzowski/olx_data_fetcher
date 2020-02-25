package com.dg.sample.vertx.olx.data.webclient.helpers;

import com.dg.sample.vertx.olx.data.webclient.models.DocumentData;
import com.dg.sample.vertx.olx.data.webclient.models.OfferData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.stream.Collectors;

public class OlxOfferHtmlDocumentDataParser implements HtmlDocumentDataParser<DocumentData, List<OfferData>> {
  @Override
  public List<OfferData> parseDocumentData(DocumentData documentData) {
    return parseHtmlResults(documentData.getBody());
  }

  private List<OfferData> parseHtmlResults(String htmlDocument){
    Document document = Jsoup.parse(htmlDocument);
    Elements offersElements = document.select("tr.wrap > td.offer > div.offer-wrapper > table");
    return offersElements.stream()
      .map(this::mapElementsToOfferData)
      .collect(Collectors.toList());
  }

  private OfferData mapElementsToOfferData(Element element) {
    long id = Long.valueOf(element.attr("data-id"));
    String name = element.selectFirst("tbody > tr > td.title-cell > div > h3 > a > strong").html();
    String price = element.selectFirst("tbody > tr > td.td-price > div > p.price > strong").html();
    return new OfferData(id, name, price);
  }
}
