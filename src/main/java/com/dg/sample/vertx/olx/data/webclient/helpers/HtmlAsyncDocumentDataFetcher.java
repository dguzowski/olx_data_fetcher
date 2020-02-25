package com.dg.sample.vertx.olx.data.webclient.helpers;

import io.vertx.core.Promise;

public interface HtmlAsyncDocumentDataFetcher<T>{
  Promise<T> getDocumentData(String keyword);
}
