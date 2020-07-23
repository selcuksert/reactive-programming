package com.corp.concepts.reactive.webflux.configuration;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class WebSocketConfig {

	@Value("${custom.property.socket.addr}")
	private String socketAddr;

	private WebSocketHandler webSocketHandler;

	public WebSocketConfig(WebSocketHandler webSocketHandler) {
		this.webSocketHandler = webSocketHandler;
	}

	@Bean
	public SimpleUrlHandlerMapping simpleUrlHandlerMapping() {
		log.info("Init SimpleUrlHandlerMapping");
		Properties mappings = new Properties();
		mappings.put(socketAddr, webSocketHandler);

		SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
		handlerMapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
		handlerMapping.setMappings(mappings);
		return handlerMapping;
	}

	@Bean
	public WebSocketHandlerAdapter handlerAdapter() {
		return new WebSocketHandlerAdapter();
	}

	@Bean
	public WebSocketService webSocketService() {
		return new HandshakeWebSocketService(new ReactorNettyRequestUpgradeStrategy());
	}

}
