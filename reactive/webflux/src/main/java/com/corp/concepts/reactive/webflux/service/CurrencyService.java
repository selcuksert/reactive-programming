package com.corp.concepts.reactive.webflux.service;

import java.time.Duration;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.corp.concept.reactive.models.CoinBaseResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class CurrencyService {

	@Value("${custom.property.coin.service.uri}")
	private String serviceUri;

	@Value("${custom.property.interval.msecs}")
	private int intervalInMsecs;

	@Value("${custom.property.crypto.name}")
	private String cryptoName;
	
	@Value("${custom.property.crypto.curr}")
	private String cryptoCurr;

	private WebClient webClient;
	private ObjectMapper json;

	public CurrencyService(WebClient webClient, ObjectMapper objectMapper) {
		this.webClient = webClient;
		this.json = objectMapper;
	}

	public Flux<String> getCryptoPrice() {
		Flux<CoinBaseResponse> eventFlux = webClient.get().uri(serviceUri, cryptoName + "-" + cryptoCurr).accept(MediaType.APPLICATION_JSON)
				.exchange().flatMap(response -> response.bodyToMono(CoinBaseResponse.class)).repeat();

		Flux<String> intervalFlux = Flux.interval(Duration.ofMillis(intervalInMsecs)).zipWith(eventFlux,
				(time, event) -> {
					try {
						event.setTimestamp(Calendar.getInstance().getTimeInMillis());
						return json.writeValueAsString(event);
					} catch (JsonProcessingException e) {
						log.error("Error:", e);
						return null;
					}
				});

		return intervalFlux;
	}

}
