# olx_data_fetcher

Launch using " mvn clean install exec:java"
#
Eventually by launching com.dg.sample.vertx.olx.data.IntegrationOLXRemoteConnectionTest.runServer()
#
It was hard to write verticles unit tests. A lot of mocking. Much of it repeatable.
Maybe it's possible to extract common mock context with repetable mocking operations for all verticles.
However I've reached quite high code coverage
#
Another steps would be introducing proxy for OlxWebClient verticle to turn it into service and hide eventBus.
Next might be introducing swagger to get nice and convenient UI to test API
#
test by invoking GET localhost:8080/offers/olx/{keyword}
