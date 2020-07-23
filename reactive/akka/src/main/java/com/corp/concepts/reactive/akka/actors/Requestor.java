package com.corp.concepts.reactive.akka.actors;

import com.corp.concept.reactive.models.CoinBaseResponse;
import com.corp.concepts.reactive.akka.service.CoinPriceService;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Requestor extends AbstractActor {

	private final ActorRef printerActor;
	private final CoinPriceService coinPriceService;

	public Requestor(ActorRef printerActor, CoinPriceService coinPriceService) {
		this.printerActor = printerActor;
		this.coinPriceService = coinPriceService;
	}

	public static Props props(ActorRef printerActor, CoinPriceService coinPriceService) {
		return Props.create(Requestor.class, () -> new Requestor(printerActor, coinPriceService));
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(GetCryptoPrice.class, crypto -> {
			CoinBaseResponse response = coinPriceService.getPrice(crypto.currPair);
			log.debug("currPair: {} | response: {}", crypto.currPair, response);
			printerActor.tell(new Sender.CryptoPrice(response), getSelf());
		}).build();
	}

	public static class GetCryptoPrice {
		public final String currPair;

		public GetCryptoPrice(String currPair) {
			this.currPair = currPair;
		}
	}

}
