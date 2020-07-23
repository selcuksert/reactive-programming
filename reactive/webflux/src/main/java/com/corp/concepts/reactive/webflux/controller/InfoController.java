package com.corp.concepts.reactive.webflux.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/info")
public class InfoController {

	@Value("${custom.property.crypto.name}")
	private String cryptoName;
	@Value("${custom.property.crypto.curr}")
	private String cryptoCurr;
	@Value("${custom.property.interval.msecs}")
	private int intervalInMsecs;

	@GetMapping("/currpair")
	public Mono<String> getCurrPair() {
		return Mono.just(cryptoName + "-" + cryptoCurr + " (frequency: " + intervalInMsecs + " msecs)");
	}

}
