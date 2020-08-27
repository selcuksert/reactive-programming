package com.corp.concepts.reactive.vertx.verticle;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.circuitbreaker.CircuitBreaker;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.RxHelper;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.client.HttpRequest;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.codec.BodyCodec;
import io.vertx.rxjava.ext.web.handler.FaviconHandler;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;
import rx.Single;

@Component
@Slf4j
public class CoinPriceVerticle extends AbstractVerticle {

	@Value("${custom.property.web.server.port}")
	private int serverPort;

	@Value("${custom.property.coin.price.host}")
	private String coinPriceServiceHost;

	@Value("${custom.property.coin.price.uri}")
	private String coinPriceServiceUri;

	private WebClient webClient;

	private CircuitBreaker circuit;

	@Override
	public void start() throws Exception {
		webClient = WebClient.create(vertx);

		Router router = Router.router(vertx);

		router.route().handler(StaticHandler.create("static")).handler(FaviconHandler.create("static/favicon.ico"));

		router.get("/price/:base/:curr").handler(this::getCoinPrice);

		vertx.createHttpServer().requestHandler(router).listen(serverPort);

		this.circuit = CircuitBreaker.create("coin-circuit", vertx,
				new CircuitBreakerOptions().setFallbackOnFailure(true) // Call the fallback on failures
						.setTimeout(3000) // Set the operation timeout
						.setMaxFailures(3) // Number of failures before switching to the 'open' state
						.setResetTimeout(5000) // Time before attempting to reset the circuit breaker
		);

	}

	private void getCoinPrice(RoutingContext rc) {
		circuit.rxExecuteWithFallback(future -> {
			HttpRequest<JsonObject> requestPrice = webClient
					.get(443, coinPriceServiceHost,
							coinPriceServiceUri + rc.pathParam("base") + "-" + rc.pathParam("curr") + "/buy")
					.ssl(true)
					.putHeader(HttpHeaderNames.ACCEPT.toString(), HttpHeaderValues.APPLICATION_JSON.toString())
					.as(BodyCodec.jsonObject());

			HttpRequest<JsonObject> requestTime = webClient.get(443, coinPriceServiceHost, "/v2/time").ssl(true)
					.putHeader(HttpHeaderNames.ACCEPT.toString(), HttpHeaderValues.APPLICATION_JSON.toString())
					.as(BodyCodec.jsonObject());

			Single<JsonObject> priceResp = requestPrice.rxSend()
					.subscribeOn(RxHelper.scheduler(vertx)) // use Vert.x event loop
					.map(HttpResponse::body);
			Single<JsonObject> timeResp = requestTime.rxSend()
					.subscribeOn(RxHelper.scheduler(vertx)) // use Vert.x event loop
					.map(HttpResponse::body);

			// Merge price and time API responses
			Single.zip(priceResp, timeResp, (price, time) -> {
				return new JsonObject().put("data", price.getJsonObject("data")).put("timestamp",
						time.getJsonObject("data").getLong("epoch"));
			}).subscribe(future::complete, future::fail);
		}, fallback -> new JsonObject().put("error", "[" + circuit.state().name() + "] " + fallback.getMessage()))
				.subscribe(result -> {
					rc.response().putHeader(HttpHeaderNames.CONTENT_TYPE.toString(),
							HttpHeaderValues.APPLICATION_JSON.toString()).end(result.encode());
				}, error -> {
					log.error("Error: ", error.getCause());
					rc.response().putHeader(HttpHeaderNames.CONTENT_TYPE.toString(),
							HttpHeaderValues.APPLICATION_JSON.toString()).end(error.getMessage());
				});
	}
}
