package com.dg.sample.vertx.olx.data.webclient.helpers;

import com.dg.sample.vertx.olx.data.test.utils.TestUtils;
import com.dg.sample.vertx.olx.data.webclient.models.DocumentData;
import com.dg.sample.vertx.olx.data.webclient.models.OfferData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class OlxOfferHtmlDocumentDataParserTest {

  private OlxOfferHtmlDocumentDataParser parser;

  @BeforeEach
  public void setUpParser(){
    parser = new OlxOfferHtmlDocumentDataParser();
  }

  @Test
  public void shouldParseCorrectDocumentData(){
    //given
    DocumentData data = new DocumentData(200, TestUtils.getOlxHtmlStubbedData());

    //when
    List<OfferData> offersData = parser.parseDocumentData(data);

    //then
    Assertions.assertEquals(44, offersData.size());
  }

  @Test
  public void shouldReturnEmptyListWhenNoOffersCanBeFound(){
    //given
    DocumentData data = new DocumentData(200, "<html></html>");

    //when
    List<OfferData> offersData = parser.parseDocumentData(data);

    //then
    Assertions.assertEquals(0, offersData.size());
  }

}
