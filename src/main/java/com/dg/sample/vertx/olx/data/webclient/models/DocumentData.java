package com.dg.sample.vertx.olx.data.webclient.models;

public class DocumentData {
  private int statusCode;
  private String body;

  public DocumentData(int statusCode, String body){
    this.statusCode = statusCode;
    this.body = body;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getBody() {
    return body;
  }
}
