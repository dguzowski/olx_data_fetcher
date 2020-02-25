package com.dg.sample.vertx.olx.data.webclient.models;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OfferDataTest {

  @Test
  public void shouldMapToJsonObject(){

    //given
    long id = 123L;
    String title = "Offer";
    String price = "1234 zł";

    OfferData offer = new OfferData(id, title, price);

    //when
    JsonObject json = offer.toJsonObject();

    //then
    Assertions.assertEquals(Long.toString(id),json.getString("id"));
    Assertions.assertEquals(title, json.getString("name"));
    Assertions.assertEquals(price, json.getString("price"));
  }

}
