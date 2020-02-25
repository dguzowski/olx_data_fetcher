package com.dg.sample.vertx.olx.data.httpserver;

import com.dg.sample.vertx.olx.data.test.utils.TestUtils;
import com.dg.sample.vertx.olx.data.utils.PropertyData;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.VertxImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Collections;

class HttpServerVerticleTest {

private HttpServerVerticle serverVerticle;

  @BeforeEach
  public void setUpServerVerticle(){
    serverVerticle = new HttpServerVerticle();
  }

  @Test
  public void shouldReturnJsonDataOnSuccess() throws Exception {
    //given
    MockContext ctx = MockContext.getInstance(serverVerticle)
      .mockServerConfigOperations()
      .mockRouteConfigOperations()
      .mockConfig()
      .mockRoutingContextOperations()
      .mockEventBusOperations();

    ArgumentCaptor<Handler<RoutingContext>> getDataHandlerCaptor = ArgumentCaptor.forClass(Handler.class);
   ArgumentCaptor<Handler<AsyncResult<Message<JsonObject>>>> eventBusResponseHandlerCaptor = ArgumentCaptor.forClass(Handler.class);

    Buffer expectedResponse = new JsonObject().put("response", "success").toBuffer();

    ctx.setExpectedResponse(expectedResponse)
      .mockEventBusResponseAsSuccessful();

    //when
    serverVerticle.start(ctx.getPromiseMock());
    Mockito.verify(ctx.getRouteMock()).handler(getDataHandlerCaptor.capture());
    Mockito.verify(ctx.getServerMock()).listen(Mockito.anyInt(), Mockito.any(Handler.class));
    Handler<RoutingContext> getDataHandler = getDataHandlerCaptor.getValue();
   getDataHandler.handle(ctx.getRoutingContextMock());

    Mockito.verify(ctx.getEventBusMock()).request(Mockito.anyString(), Mockito.any(JsonObject.class), eventBusResponseHandlerCaptor.capture());
    Handler<AsyncResult<Message<JsonObject>>> eventBusResponseHandler = eventBusResponseHandlerCaptor.getValue();

    eventBusResponseHandler.handle(ctx.getEventBusResponseMock());

    //then
    Mockito.verify(ctx.getResponseMock()).setStatusCode(200);
    Mockito.verify(ctx.getResponseMock()).end(expectedResponse);
  }

  @Test
  public void shouldReturnServerErrorCodeOnFailure() throws Exception {
    //given
    MockContext ctx = MockContext.getInstance(serverVerticle)
      .mockServerConfigOperations()
      .mockRouteConfigOperations()
      .mockConfig()
      .mockRoutingContextOperations()
      .mockEventBusOperations()
      .mockEventBusResponseAsFailure();

    ArgumentCaptor<Handler<RoutingContext>> getDataHandlerCaptor = ArgumentCaptor.forClass(Handler.class);
  ArgumentCaptor<Handler<AsyncResult<Message<JsonObject>>>> eventBusResponseHandlerCaptor = ArgumentCaptor.forClass(Handler.class);


    //when
    serverVerticle.start(ctx.getPromiseMock());
    Mockito.verify(ctx.getRouteMock()).handler(getDataHandlerCaptor.capture());
    Mockito.verify(ctx.getServerMock()).listen(Mockito.anyInt(), Mockito.any(Handler.class));
    Handler<RoutingContext> getDataHandler = getDataHandlerCaptor.getValue();

    getDataHandler.handle(ctx.getRoutingContextMock());

    Mockito.verify(ctx.getEventBusMock()).request(Mockito.anyString(), Mockito.any(JsonObject.class), eventBusResponseHandlerCaptor.capture());
    Handler<AsyncResult<Message<JsonObject>>> eventBusResponseHandler = eventBusResponseHandlerCaptor.getValue();

    eventBusResponseHandler.handle(ctx.getEventBusResponseMock());

    //then
    Mockito.verify(ctx.getResponseMock()).setStatusCode(500);
  }

  @Test
  public void shouldHandleSuccesfulServerStart() throws Exception {
    //given
    MockContext ctx = MockContext.getInstance(serverVerticle)
      .mockServerConfigOperations()
      .mockRouteConfigOperations()
      .mockConfig();

    ArgumentCaptor<Handler<AsyncResult<HttpServer>>> serverLaunchHandlerCaptor = ArgumentCaptor.forClass(Handler.class);

    AsyncResult<HttpServer> result = Mockito.mock(AsyncResult.class);
    Mockito.when(result.succeeded()).thenReturn(true);
    Mockito.doNothing().when(ctx.getPromiseMock()).complete();


    //when
    serverVerticle.start(ctx.getPromiseMock());
    Mockito.verify(ctx.getRouteMock()).handler(Mockito.any(Handler.class));
    Mockito.verify(ctx.getServerMock()).listen(Mockito.anyInt(), serverLaunchHandlerCaptor.capture());
    Handler<AsyncResult<HttpServer>> serverLaunchHandler = serverLaunchHandlerCaptor.getValue();

    serverLaunchHandler.handle(result);

    //then
    Mockito.verify(ctx.getPromiseMock()).complete();
  }

  @Test
  public void shouldHandleFailureOnServerStart() throws Exception {
    //given
    MockContext ctx = MockContext.getInstance(serverVerticle)
      .mockServerConfigOperations()
      .mockRouteConfigOperations()
      .mockConfig();

    ArgumentCaptor<Handler<AsyncResult<HttpServer>>> serverLaunchHandlerCaptor = ArgumentCaptor.forClass(Handler.class);

    Throwable throwable = Mockito.mock(Throwable.class);
    AsyncResult<HttpServer> result = Mockito.mock(AsyncResult.class);
    Mockito.when(result.succeeded()).thenReturn(false);
    Mockito.when(result.cause()).thenReturn(throwable);
    Mockito.doNothing().when(ctx.getPromiseMock()).fail(throwable);


    //when
    serverVerticle.start(ctx.getPromiseMock());
    Mockito.verify(ctx.getRouteMock()).handler(Mockito.any(Handler.class));
    Mockito.verify(ctx.getServerMock()).listen(Mockito.anyInt(), serverLaunchHandlerCaptor.capture());
    Handler<AsyncResult<HttpServer>> serverLaunchHandler = serverLaunchHandlerCaptor.getValue();

    serverLaunchHandler.handle(result);

    //then
    Mockito.verify(ctx.getPromiseMock()).fail(throwable);
  }

  private static class MockContext{
    private Promise promiseMock;
    private Vertx vertxMock;
    private Context contextMock;
    private EventBus eventBusMock;
    private HttpServer serverMock;
    private Router routerMock;
    private Route routeMock;
    private RoutingContext routingContextMock;
    private AsyncResult<Message<JsonObject>> eventBusResponseMock;
    private HttpServerResponse responseMock;
    private Buffer expectedResponse;

    private MockContext(Promise promise, Vertx vertx, Context context,
                        EventBus eventBus, HttpServer server, Router router, Route route,
                        RoutingContext routingContext, AsyncResult<Message<JsonObject>> eventBusResponse,
                        HttpServerResponse response) {
      this.promiseMock = promise;
      this.vertxMock = vertx;
      this.contextMock = context;
      this.eventBusMock = eventBus;
      this.serverMock = server;
      this.routerMock = router;
      this.routeMock = route;
      this.routingContextMock = routingContext;
      this.eventBusResponseMock = eventBusResponse;
      this.responseMock = response;
    }

    public static MockContext getInstance(HttpServerVerticle verticle) throws NoSuchFieldException, IllegalAccessException {
      return new MockContext(Mockito.mock(Promise.class),
        TestUtils.setUpMockFieldOnObject(verticle, VertxImpl.class, "vertx"),
        TestUtils.setUpMockFieldOnObject(verticle, Context.class, "context"),
        Mockito.mock(EventBus.class),
        Mockito.mock(HttpServer.class),
        TestUtils.setUpMockFieldOnObject(verticle, Router.class, "router"),
        Mockito.mock(Route.class),
        Mockito.mock(RoutingContext.class),
        Mockito.mock(AsyncResult.class),
        Mockito.mock(HttpServerResponse.class)
      );
    }

    public Promise getPromiseMock() {
      return promiseMock;
    }

    public Vertx getVertxMock() {
      return vertxMock;
    }

    public HttpServer getServerMock() {
      return serverMock;
    }

    public Router getRouterMock() {
      return routerMock;
    }

    public Route getRouteMock() {
      return routeMock;
    }

    public Context getContextMock() {
      return contextMock;
    }

    public EventBus getEventBusMock() {
      return eventBusMock;
    }

    public RoutingContext getRoutingContextMock() {
      return routingContextMock;
    }

    public AsyncResult<Message<JsonObject>> getEventBusResponseMock() {
      return eventBusResponseMock;
    }

    public HttpServerResponse getResponseMock() {
      return responseMock;
    }

    public Buffer getExpectedResponse() {
      return expectedResponse;
    }

    public MockContext setExpectedResponse(Buffer expectedResponse) {
      this.expectedResponse = expectedResponse;
      return this;
    }

    public MockContext mockServerConfigOperations(){
      Mockito.when(vertxMock.createHttpServer())
        .thenReturn(serverMock);
      Mockito.when(serverMock.requestHandler(routerMock))
        .thenReturn(serverMock);
      Mockito.when(serverMock.listen(Mockito.anyInt(), Mockito.any(Handler.class)))
        .thenReturn(serverMock);
      return this;
    }

    public MockContext mockRouteConfigOperations(){
      Mockito.when(routerMock.getRoutes())
        .thenReturn(Collections.emptyList());
      Mockito.when(routerMock.get(Mockito.anyString()))
        .thenReturn(routeMock);
      Mockito.when(routeMock.handler(Mockito.any(Handler.class)))
        .thenReturn(routeMock);
      return this;
    }

    public MockContext mockRoutingContextOperations(){
      Mockito.when(routingContextMock.pathParam("keyword"))
        .thenReturn("keyword");
      Mockito.when(routingContextMock.response())
        .thenReturn(responseMock);
      return this;
    }

    public MockContext mockConfig(){
      Mockito.when(contextMock.config())
        .thenReturn(new JsonObject().put(PropertyData.OLX_OFFER_DATA_QUEUE_PROP,
          PropertyData.getApplicationProperties().get(PropertyData.OLX_OFFER_DATA_QUEUE_PROP))
          .put(PropertyData.SERVER_PORT_PROPERTY_PROP,
            PropertyData.getApplicationProperties().get(PropertyData.SERVER_PORT_PROPERTY_PROP)));
      return this;
    }

    public MockContext mockEventBusOperations() {
      Mockito.when(vertxMock.eventBus())
        .thenReturn(eventBusMock);
      Mockito.when(eventBusMock.request(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class)))
        .thenReturn(eventBusMock);
      return this;
    }

    public MockContext mockEventBusResponseAsSuccessful(){
      Message<JsonObject> resultMock = Mockito.mock(Message.class);
      JsonObject resultBodyMock = Mockito.mock(JsonObject.class);

      Mockito.when(eventBusResponseMock.succeeded())
        .thenReturn(true);
      Mockito.when(eventBusResponseMock.result())
        .thenReturn(resultMock);
      Mockito.when(resultMock.body())
        .thenReturn(resultBodyMock);
      Mockito.when(resultBodyMock.toBuffer())
        .thenReturn(getExpectedResponse());
      return mockHttpServerSuccessfulResponse();
    }

    public MockContext mockEventBusResponseAsFailure(){
      Throwable ex = Mockito.mock(Throwable.class);
      Mockito.when(ex.getMessage())
        .thenReturn("Exception message");

      Mockito.when(eventBusResponseMock.succeeded())
        .thenReturn(false);
      Mockito.when(eventBusResponseMock.cause())
        .thenReturn(ex);
      return mockHttpServerFailureResponse();
    }

    private MockContext mockHttpServerSuccessfulResponse(){
      Mockito.when(responseMock.setStatusCode(Mockito.anyInt()))
        .thenReturn(responseMock);
      Mockito.doNothing().when(responseMock).end(Mockito.any(Buffer.class));
      return this;
    }

    private MockContext mockHttpServerFailureResponse(){
      Mockito.when(responseMock.setStatusCode(Mockito.anyInt()))
        .thenReturn(responseMock);
      Mockito.doNothing().when(responseMock).end();
      return this;
    }

  }
}
