package com.corp.concepts.reactive.akka.service;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.corp.concept.reactive.models.CoinBaseResponse;

@Service
public class CoinPriceService {
	@Value("${custom.property.coin.service.uri}")
	private String serviceUri;

	private RestTemplate restTemplate;

	public CoinPriceService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public CoinBaseResponse getPrice(String currPair) {
		long timestamp = Calendar.getInstance().getTimeInMillis();

		CoinBaseResponse response = restTemplate.getForObject(serviceUri, CoinBaseResponse.class, currPair);
		response.setTimestamp(timestamp);

		return response;
	}

}
