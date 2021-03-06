package com.corp.concepts.reactive.rxjava.runner;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.corp.concepts.reactive.rxjava.observer.ResponseObserver;
import com.corp.concepts.reactive.rxjava.service.CoinPriceService;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

@Component
public class AppRunner implements ApplicationRunner {

	@Value("${custom.property.interval.msecs}")
	private int intervalInMsecs;
	@Value("${custom.property.crypto.name}")
	private String cryptoName;
	@Value("${custom.property.crypto.curr}")
	private String cryptoCurr;
	@Value("${custom.property.message.topic}")
	private String topicName;

	private CoinPriceService coinPriceService;
	private SimpMessagingTemplate template;

	public AppRunner(CoinPriceService coinPriceService, SimpMessagingTemplate template) {
		this.coinPriceService = coinPriceService;
		this.template = template;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		Observable.interval(intervalInMsecs, TimeUnit.MILLISECONDS, Schedulers.io())
				.map(tick -> coinPriceService.getPrice(cryptoName + "-" + cryptoCurr))
				.subscribe(new ResponseObserver(template, topicName));
	}

}
