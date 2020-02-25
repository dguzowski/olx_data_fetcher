package com.dg.sample.vertx.olx.data.webclient.helpers;

public interface HtmlDocumentDataParser <P, R>{
  R parseDocumentData(P documentData);
}
