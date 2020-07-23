package com.corp.concepts.reactive.rxjava.observer;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.corp.concept.reactive.models.CoinBaseResponse;

import io.reactivex.observers.DefaultObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResponseObserver extends DefaultObserver<CoinBaseResponse> {
	private SimpMessagingTemplate template;
	private String topicName;

	public ResponseObserver(SimpMessagingTemplate template, String topicName) {
		this.template = template;
		this.topicName = topicName;
	}

	@Override
	public void onNext(CoinBaseResponse response) {
		template.convertAndSend(topicName, response);
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
