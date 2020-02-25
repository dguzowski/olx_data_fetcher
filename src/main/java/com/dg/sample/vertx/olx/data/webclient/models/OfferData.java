package com.dg.sample.vertx.olx.data.webclient.models;

import io.vertx.core.json.JsonObject;

public class OfferData {
  private Long offerId;
  private String offerTitle;
  private String offerPrice;

  public OfferData(Long offerId, String offerTitle, String offerPrice) {
    this.offerId = offerId;
    this.offerTitle = offerTitle;
    this.offerPrice = offerPrice;
  }

  public long getOfferId() {
    return offerId;
  }

  public String getOfferTitle() {
    return offerTitle;
  }

  public String getOfferPrice() {
    return offerPrice;
  }

  public JsonObject toJsonObject(){
    JsonObject json = new JsonObject();
    json.put("id", this.offerId.toString());
    json.put("name", this.offerTitle);
    json.put("price", this.offerPrice);
    return json;
  }
}
