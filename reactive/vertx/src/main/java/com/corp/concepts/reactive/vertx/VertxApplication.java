package com.corp.concepts.reactive.vertx;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.corp.concepts.reactive.vertx.verticle.CoinPriceVerticle;

import io.vertx.rxjava.core.Vertx;


@SpringBootApplication
public class VertxApplication {

	@Autowired
	private CoinPriceVerticle coinPriceVerticle;

	public static void main(String[] args) {
		SpringApplication.run(VertxApplication.class, args);
	}

	@PostConstruct
	public void deployVerticles() {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(coinPriceVerticle);
	}
}
