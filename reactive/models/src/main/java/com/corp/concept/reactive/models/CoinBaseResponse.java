package com.corp.concept.reactive.models;

import lombok.Data;

@Data
public class CoinBaseResponse {

	private Long timestamp;
	private CoinData data;

	@Data
	public class CoinData {
		private String base;
		private String currency;
		private String amount;
	}
}
