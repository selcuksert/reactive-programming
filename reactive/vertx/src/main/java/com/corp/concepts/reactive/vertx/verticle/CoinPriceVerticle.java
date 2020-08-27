package com.corp.concepts.reactive.vertx.verticle;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.reactivex.Single;
import io.reactivex.functions.BooleanSupplier;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.http.ServerWebSocket;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import io.vertx.reactivex.ext.web.handler.FaviconHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CoinPriceVerticle extends AbstractVerticle {

	@Value("${custom.property.web.server.port}")
	private int serverPort;
	@Value("${custom.property.coin.price.host}")
	private String coinPriceServiceHost;
	@Value("${custom.property.coin.price.uri}")
	private String coinPriceServiceUri;
	@Value("${custom.property.socket.addr}")
	private String socketAddr;
	@Value("${custom.property.crypto.name}")
	private String cryptoName;
	@Value("${custom.property.crypto.curr}")
	private String cryptoCurr;
	@Value("${custom.property.interval.msecs}")
	private int intervalInMsecs;

	private WebClient webClient;

	@Override
	public void start() throws Exception {
		webClient = WebClient.create(vertx);

		Router router = Router.router(vertx);

		router.route().handler(StaticHandler.create("static")).handler(FaviconHandler.create("static/favicon.ico"));

		router.get("/info/currpair").handler(this::getCurrPair);

		vertx.createHttpServer().requestHandler(router).webSocketHandler(handler -> {
			if (!handler.path().equals(socketAddr)) {
				handler.reject();
			}

			handler.closeHandler(closeHandler -> log.info("WebSocket closed"));

			coinPriceWsHandler(handler);
		}).listen(serverPort);

	}

	private void getCurrPair(RoutingContext rc) {
		rc.response().end(cryptoName + "-" + cryptoCurr + " (frequency: " + intervalInMsecs + " msecs)");
	}

	private void coinPriceWsHandler(ServerWebSocket ws) {
		log.info("WebSocket opened");
		HttpRequest<JsonObject> requestPrice = webClient
				.get(443, coinPriceServiceHost, coinPriceServiceUri + cryptoName + "-" + cryptoCurr + "/buy").ssl(true)
				.putHeader(HttpHeaderNames.ACCEPT.toString(), HttpHeaderValues.APPLICATION_JSON.toString())
				.as(BodyCodec.jsonObject());

		Single<JsonObject> priceResp = requestPrice.rxSend().subscribeOn(RxHelper.scheduler(vertx))
				.map(HttpResponse::body);

		priceResp.toObservable().subscribeOn(RxHelper.scheduler(vertx)).map(price -> {
			return new JsonObject().put("data", price.getJsonObject("data")).put("timestamp",
					Calendar.getInstance().getTimeInMillis());
		})
		.delay(intervalInMsecs, TimeUnit.MILLISECONDS)
		.repeatUntil(new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() throws Exception {
				return ws.isClosed();
			}
		}).subscribe(result -> {
			if (!ws.isClosed()) {
				ws.writeTextMessage(result.encode());
			}
		}, error -> {
			log.error("Error: ", error.getMessage());
		});
	}
}
