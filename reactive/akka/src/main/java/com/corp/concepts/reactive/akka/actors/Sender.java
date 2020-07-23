package com.corp.concepts.reactive.akka.actors;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.corp.concept.reactive.models.CoinBaseResponse;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class Sender extends AbstractActor {
	private SimpMessagingTemplate template;
	private String topicName;

	public Sender(SimpMessagingTemplate template, String topicName) {
		this.template = template;
		this.topicName = topicName;
	}

	public static Props props(SimpMessagingTemplate template, String topicName) {
		return Props.create(Sender.class, () -> new Sender(template, topicName));
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(
				CryptoPrice.class, 
				message -> template.convertAndSend(topicName, message.response))
				.build();
	}

	public static class CryptoPrice {
		public final CoinBaseResponse response;

		public CryptoPrice(CoinBaseResponse response) {
			this.response = response;
		}
	}

}
