package com.corp.concepts.reactive.rxjava.observer;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.corp.concept.reactive.models.CoinBaseResponse;

import io.reactivex.observers.DefaultObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResponseObserver extends DefaultObserver<CoinBaseResponse> {
	private SimpMessagingTemplate template;

	public ResponseObserver(SimpMessagingTemplate template) {
		this.template = template;
	}

	@Override
	public void onNext(CoinBaseResponse response) {
		template.convertAndSend("/topic/price", response);
	}

	@Override
	public void onError(Throwable e) {
		log.error("Error:", e);

	}

	@Override
	public void onComplete() {
		log.info("Completed");
	}

}
