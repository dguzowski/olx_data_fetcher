package com.dg.sample.vertx.olx.data.webclient.helpers;

import com.dg.sample.vertx.olx.data.test.utils.TestUtils;
import com.dg.sample.vertx.olx.data.webclient.OlxWebClientVerticle;
import com.dg.sample.vertx.olx.data.webclient.models.DocumentData;
import io.netty.handler.codec.http.HttpStatusClass;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OlxOfferHtmlDocumentDataFetcherTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(OlxOfferHtmlDocumentDataFetcherTest.class);
  private OlxOfferHtmlDocumentDataFetcher fetcherUnderTest;

  @BeforeEach
  public void setUpFetcher(){
    this.fetcherUnderTest = (OlxOfferHtmlDocumentDataFetcher) new OlxOfferHtmlDocumentDataFetcher.Builder()
      .setHost("some.host")
      .setPort(1)
      .setVertx(Mockito.mock(Vertx.class))
      .setPath("/random/path")
      .setSecure(false)
      .build();
  }

  private WebClient setUpWebClientMock() throws NoSuchFieldException, IllegalAccessException {
    return TestUtils.setUpMockFieldOnObject(this.fetcherUnderTest, WebClient.class, "client");
  }


  @Test
  public void shouldReturnOlxDocumentDataOnSuccess() throws NoSuchFieldException, IllegalAccessException {
    //given
    WebClient mockClient = setUpWebClientMock();
    HttpRequest<Buffer> request = Mockito.mock(HttpRequest.class);
    when(mockClient.get(any(Integer.class), any(String.class), any(String.class)))
      .thenReturn(request);
    AsyncResult<HttpResponse<Buffer>> response = Mockito.mock(AsyncResult.class);
    HttpResponse<Buffer> result = Mockito.mock(HttpResponse.class);
    when(response.succeeded()).thenReturn(true);
    when(response.result()).thenReturn(result);
    when(result.statusCode()).thenReturn(200);
    when(result.bodyAsString()).thenReturn(TestUtils.getOlxHtmlStubbedData());
    Mockito.doNothing().when(request).send(any(Handler.class));
    ArgumentCaptor<Handler<AsyncResult<HttpResponse<Buffer>>>> handlerArgumentCaptor  = ArgumentCaptor.forClass(Handler.class);

    //when
    Promise<DocumentData> promise = this.fetcherUnderTest.getDocumentData("keyword");
    verify(request).send(handlerArgumentCaptor.capture());
    Handler<AsyncResult<HttpResponse<Buffer>>> handler = handlerArgumentCaptor.getValue();
    handler.handle(response);

    //then
    promise.future()
      .onComplete( res ->{
        DocumentData data = res.result();
        Assertions.assertEquals(200, data.getStatusCode());
        Assertions.assertFalse(data.getBody().isEmpty());
      });

  }

  @Test
  public void shouldReturnCauseOnException() throws NoSuchFieldException, IllegalAccessException {
    //given
    WebClient mockClient = setUpWebClientMock();
    HttpRequest<Buffer> request = Mockito.mock(HttpRequest.class);
    when(mockClient.get(any(Integer.class), any(String.class), any(String.class)))
      .thenReturn(request);
    AsyncResult<HttpResponse<Buffer>> response = Mockito.mock(AsyncResult.class);
    HttpResponse<Buffer> result = Mockito.mock(HttpResponse.class);
    when(response.succeeded()).thenReturn(false);
    when(response.cause()).thenReturn(Mockito.mock(Exception.class));
    Mockito.doNothing().when(request).send(any(Handler.class));
    ArgumentCaptor<Handler<AsyncResult<HttpResponse<Buffer>>>> handlerArgumentCaptor  = ArgumentCaptor.forClass(Handler.class);

    //when
    Promise<DocumentData> promise = this.fetcherUnderTest.getDocumentData("keyword");
    verify(request).send(handlerArgumentCaptor.capture());
    Handler<AsyncResult<HttpResponse<Buffer>>> handler = handlerArgumentCaptor.getValue();
    handler.handle(response);

    //then
    promise.future()
      .onFailure( ex -> {
        Assertions.assertTrue( ex instanceof Exception);
      });

  }

}
