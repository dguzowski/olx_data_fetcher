package com.dg.sample.vertx.olx.data.webclient;

import com.dg.sample.vertx.olx.data.test.utils.TestUtils;
import com.dg.sample.vertx.olx.data.utils.PropertyData;
import com.dg.sample.vertx.olx.data.webclient.helpers.HtmlAsyncDocumentDataFetcher;
import com.dg.sample.vertx.olx.data.webclient.helpers.HtmlDocumentDataParser;
import com.dg.sample.vertx.olx.data.webclient.models.DocumentData;
import com.dg.sample.vertx.olx.data.webclient.models.OfferData;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.FutureFactoryImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

class OlxWebClientVerticleTest {

  private OlxWebClientVerticle verticleUnderTest;

  private HtmlAsyncDocumentDataFetcher<DocumentData> fetcherMock;
  private HtmlDocumentDataParser<DocumentData, List<OfferData>> parserMock;

  @BeforeEach
  public void setUpVerticle(){
    fetcherMock = Mockito.mock(HtmlAsyncDocumentDataFetcher.class);
    parserMock = Mockito.mock(HtmlDocumentDataParser.class);
    verticleUnderTest = OlxWebClientVerticle.getInstance(fetcherMock, parserMock);
  }

  @Test
  public void shouldSendBackJsonDataWithOffers() throws Exception {
    //GIVEN
    MockContext ctx = MockContext.initContext(verticleUnderTest);
    ctx.mockConfig()
      .mockDocumentFetchingOperation()
      .mockEventBusOperations()
      .mockMessageOperations()
      .mockDocumentFetchingOperation()
      .mockPromiseOperations()
      .mockFutureOperations();

    //expected data from parser
    List<OfferData> resultList = LongStream
      .range(1, 10)
      .mapToObj(i ->  new OfferData(i , "title "+i, "1"+i+" zł"))
      .collect(Collectors.toList());

    ctx.setParsingResult(resultList);

    JsonObject resultJson = expectedResultByParsingResult(resultList);

    //queue id,  and result captors
    ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Handler<Message<JsonObject>>> messageHandlerCaptor = ArgumentCaptor.forClass(Handler.class);
    ArgumentCaptor<JsonObject> jsonResponseCaptor = ArgumentCaptor.forClass(JsonObject.class);

    // future's onSuccess, onFailure and onComplete handler captors
    ArgumentCaptor<Handler<DocumentData>> onSuccessHandlerCaptor = ArgumentCaptor.forClass(Handler.class);

    //WHEN
    verticleUnderTest.start(Promise.promise());

    //capture handler method listening for bus events and invoke
    Mockito.verify(ctx.getEventBusMock()).consumer(idCaptor.capture(), messageHandlerCaptor.capture());
    messageHandlerCaptor.getValue().handle(ctx.getMessageMock());

    //capture on Success handler and invoke
    Mockito.verify(ctx.getDocumentDataFutureMock()).onSuccess(onSuccessHandlerCaptor.capture());
    onSuccessHandlerCaptor.getValue().handle(Mockito.mock(DocumentData.class));

    //capture response to message;
    Mockito.verify(ctx.getMessageMock()).reply(jsonResponseCaptor.capture());


    //THEN
    Assertions.assertEquals( PropertyData.getApplicationProperties().get(PropertyData.OLX_OFFER_DATA_QUEUE_PROP),
      idCaptor.getValue());
    Assertions.assertEquals( resultJson, jsonResponseCaptor.getValue());
  }

  @Test
  public void shouldSetFailureOnMessageWithExceptionCauseWhenCantFetchData() throws Exception {
    //GIVEN
    MockContext ctx = MockContext.initContext(verticleUnderTest)
      .setRealFuture();
    Throwable throwable = Mockito.mock(Throwable.class);
    ctx.mockConfig()
      .mockDocumentFetchingOperation()
      .mockEventBusOperations()
      .mockMessageOperations()
      .mockDocumentFetchingOperation()
      .mockPromiseOperations();

    //queue id,  and result captors
    ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Handler<Message<JsonObject>>> messageHandlerCaptor = ArgumentCaptor.forClass(Handler.class);
    ArgumentCaptor<Integer> errorIdCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<String> errorMessageCaptor = ArgumentCaptor.forClass(String.class);

    // future's onSuccess, onFailure and onComplete handler captors
    //ArgumentCaptor<Handler<DocumentData>> onSuccessHandlerCaptor = ArgumentCaptor.forClass(Handler.class);
    ArgumentCaptor<Handler<Throwable>> onFailureHandlerCaptor = ArgumentCaptor.forClass(Handler.class);
    //ArgumentCaptor<Handler<AsyncResult<DocumentData>>> onCompleteHandlerCaptor = ArgumentCaptor.forClass(Handler.class);


    //WHEN
    verticleUnderTest.start(Promise.promise());

    //capture handler method listening for bus events and invoke
    Mockito.verify(ctx.getEventBusMock()).consumer(idCaptor.capture(), messageHandlerCaptor.capture());
   Mockito.when(throwable.getMessage()).thenReturn("Exception message");

    messageHandlerCaptor.getValue().handle(ctx.getMessageMock());
    ctx.getDocumentDataFutureMock().fail(throwable);

    //capture response to message;
    Mockito.verify(ctx.getMessageMock()).fail(errorIdCaptor.capture(), errorMessageCaptor.capture());

    //THEN
    Assertions.assertEquals( PropertyData.getApplicationProperties().get(PropertyData.OLX_OFFER_DATA_QUEUE_PROP),
      idCaptor.getValue());
    Assertions.assertEquals( 500, errorIdCaptor.getValue());
    Assertions.assertEquals("Exception message", errorMessageCaptor.getValue());
  }

  private JsonObject expectedResultByParsingResult(List<OfferData> parsingResult){
    return new JsonObject().put("offers",
      parsingResult.stream()
        .map(OfferData::toJsonObject)
        .collect(JsonArray::new, ( JsonArray a ,JsonObject b) -> a.add(b), (a1, a2) -> a1.addAll(a2)));
  }

  private static class MockContext {
    private Vertx vertxMock;
    private Context contextMock;
    private EventBus eventBusMock;
    private Message<JsonObject> messageMock;
    private Promise<DocumentData> documentDataPromiseMock;
    private Future<DocumentData> documentDataFutureMock;
    private HtmlAsyncDocumentDataFetcher<DocumentData> fetcherMock;
    private HtmlDocumentDataParser<DocumentData, List<OfferData>> parserMock;

    private MockContext(OlxWebClientVerticle verticleUnderTest, HtmlAsyncDocumentDataFetcher<DocumentData> fetcherMock,
                        HtmlDocumentDataParser<DocumentData, List<OfferData>> parserMock) throws NoSuchFieldException, IllegalAccessException {

      vertxMock = TestUtils.setUpMockFieldOnObject(verticleUnderTest, Vertx.class, "vertx");
      contextMock = TestUtils.setUpMockFieldOnObject(verticleUnderTest, Context.class, "context");
      eventBusMock = Mockito.mock(EventBus.class);
      messageMock = Mockito.mock(Message.class);
      documentDataPromiseMock = Mockito.mock(Promise.class);
      documentDataFutureMock = Mockito.mock(Future.class);
      this.fetcherMock = fetcherMock;
      this.parserMock = parserMock;
    }

    public Vertx getVertxMock() {
      return vertxMock;
    }

    public Context getContextMock() {
      return contextMock;
    }

    public EventBus getEventBusMock() {
      return eventBusMock;
    }

    public Message<JsonObject> getMessageMock() {
      return messageMock;
    }

    public Promise<DocumentData> getDocumentDataPromiseMock() {
      return documentDataPromiseMock;
    }

    public Future<DocumentData> getDocumentDataFutureMock() {
      return documentDataFutureMock;
    }

    public HtmlAsyncDocumentDataFetcher<DocumentData> getFetcherMock() {
      return fetcherMock;
    }

    public HtmlDocumentDataParser<DocumentData, List<OfferData>> getParserMock() {
      return parserMock;
    }

    public MockContext mockConfig(){
      Mockito.when(contextMock.config())
        .thenReturn(new JsonObject().put(PropertyData.OLX_OFFER_DATA_QUEUE_PROP,
          PropertyData.getApplicationProperties().get(PropertyData.OLX_OFFER_DATA_QUEUE_PROP)));
      return this;
    }

    public MockContext mockPromiseOperations(){
      Mockito.when(documentDataPromiseMock.future())
        .thenReturn(documentDataFutureMock);
      return this;
    }

    public MockContext mockFutureOperations(){
      Mockito.when(documentDataFutureMock.onSuccess(Mockito.any(Handler.class)))
        .thenReturn(documentDataFutureMock);

      Mockito.when(documentDataFutureMock.onFailure(Mockito.any(Handler.class)))
        .thenReturn(documentDataFutureMock);

      Mockito.when(documentDataFutureMock.onComplete(Mockito.any(Handler.class)))
        .thenReturn(documentDataFutureMock);
      return this;
    }

    public MockContext mockEventBusOperations(){
      Mockito.when(vertxMock.eventBus()).thenReturn(eventBusMock);
      Mockito.when(eventBusMock.<JsonObject>consumer(Mockito.anyString(), Mockito.any(Handler.class)))
        .thenReturn(Mockito.mock(MessageConsumer.class));
      return this;
    }

    public MockContext mockMessageOperations(){
      Mockito.when(messageMock.address())
        .thenReturn(PropertyData.getApplicationProperties()
          .getProperty(PropertyData.OLX_OFFER_DATA_QUEUE_PROP));
      Mockito.doNothing().when(messageMock).reply(Mockito.any(JsonObject.class));
      Mockito.doNothing().when(messageMock).fail(Mockito.anyInt(), Mockito.anyString());
      JsonObject requestMessageMock = Mockito.mock(JsonObject.class);
      Mockito.when(messageMock.body()).thenReturn(requestMessageMock);
      Mockito.when(requestMessageMock.getString("keyword", "keyword"))
        .thenReturn("keyword");
      return this;
    }

    public MockContext mockDocumentFetchingOperation(){
      Mockito.when(fetcherMock.getDocumentData("keyword"))
        .thenReturn(documentDataPromiseMock);
      return this;
    }

    public MockContext setParsingResult(List<OfferData> parsingResult){
      Mockito.when(parserMock.parseDocumentData(Mockito.any(DocumentData.class)))
        .thenReturn(parsingResult);
      return this;
    }



    public static MockContext initContext(OlxWebClientVerticle verticleUnderTest) throws NoSuchFieldException, IllegalAccessException {
      HtmlAsyncDocumentDataFetcher<DocumentData> fetcherMock = TestUtils.getFieldValue(verticleUnderTest, "fetcher");
      HtmlDocumentDataParser<DocumentData, List<OfferData>> parserMock = TestUtils.getFieldValue(verticleUnderTest, "parser");
      return new MockContext(verticleUnderTest, fetcherMock, parserMock);
    }

    public MockContext setFutureSpy() {
      documentDataFutureMock = Mockito.spy(new FutureFactoryImpl().future());
      return this;
    }

    public MockContext setRealFuture() {
      documentDataFutureMock = new FutureFactoryImpl().future();
      return this;
    }
  }


}
