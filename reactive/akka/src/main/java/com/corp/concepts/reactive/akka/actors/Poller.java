package com.corp.concepts.reactive.akka.actors;

import java.time.Duration;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Poller extends AbstractActorWithTimers {

	private static final Object TICK_KEY = "TickKey";
	private final ActorRef requestorActor;
	private final String cryptoName;
	private final int interval;

	public Poller(ActorRef requestorActor, String cryptoName, int interval) {
		this.requestorActor = requestorActor;
		this.cryptoName = cryptoName;
		this.interval = interval;
		getTimers().startSingleTimer(TICK_KEY, new FirstTick(), Duration.ofMillis(interval));
	}

	public static Props props(String cryptoName, int interval, ActorRef requestorActor) {
		return Props.create(Poller.class, () -> new Poller(requestorActor, cryptoName, interval));
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(FirstTick.class, message -> {
			getTimers().startTimerAtFixedRate(TICK_KEY, new Tick(), Duration.ofMillis(interval));
		}).match(Tick.class, message -> requestorActor.tell(new Requestor.GetCryptoPrice(cryptoName), getSelf()))
				.build();
	}

	private static final class FirstTick {

	}

	private static final class Tick {

	}

}
