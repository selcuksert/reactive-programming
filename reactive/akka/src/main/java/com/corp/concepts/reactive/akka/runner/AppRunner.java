package com.corp.concepts.reactive.akka.runner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.corp.concepts.reactive.akka.actors.Poller;
import com.corp.concepts.reactive.akka.actors.Requestor;
import com.corp.concepts.reactive.akka.actors.Sender;
import com.corp.concepts.reactive.akka.service.CoinPriceService;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

@Component
public class AppRunner implements ApplicationRunner {

	@Value("${custom.property.interval.msecs}")
	private int intervalInMsecs;
	@Value("${custom.property.message.topic}")
	private String topicName;
	@Value("${custom.property.crypto.name}")
	private String cryptoName;
	@Value("${custom.property.crypto.curr}")
	private String cryptoCurr;

	private CoinPriceService coinPriceService;
	private SimpMessagingTemplate template;

	public AppRunner(CoinPriceService coinPriceService, SimpMessagingTemplate template) {
		this.coinPriceService = coinPriceService;
		this.template = template;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		final ActorSystem system = ActorSystem.create("cryptoprice");
		final ActorRef sender = system.actorOf(Sender.props(template, topicName), "sender");

		final ActorRef requestor = system.actorOf(Requestor.props(sender, coinPriceService), "requestor");

		system.actorOf(Poller.props(cryptoName + "-" + cryptoCurr, intervalInMsecs, requestor),
				cryptoName + "-" + cryptoCurr + "-actor");
	}

}
