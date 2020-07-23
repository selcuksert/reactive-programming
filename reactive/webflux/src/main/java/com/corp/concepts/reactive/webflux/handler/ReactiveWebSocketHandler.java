package com.corp.concepts.reactive.webflux.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.corp.concepts.reactive.webflux.service.CurrencyService;

import reactor.core.publisher.Mono;

@Component
public class ReactiveWebSocketHandler implements WebSocketHandler {
	private CurrencyService currencyService;

	public ReactiveWebSocketHandler(CurrencyService currencyService) {
		this.currencyService = currencyService;
	}

	@Override
	public Mono<Void> handle(WebSocketSession webSocketSession) {
		return webSocketSession.send(currencyService.getCryptoPrice().map(webSocketSession::textMessage));
	}

}
