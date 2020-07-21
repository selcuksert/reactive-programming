package com.corp.concepts.reactive.rxjava.configuration;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import javax.net.ssl.SSLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.ProxyProvider;

@Configuration
public class WebClientConfig {
	@Value("${custom.property.proxy.enabled}")
	private boolean proxyEnabled;

	@Value("${custom.property.proxy.host}")
	private String proxyHost;

	@Value("${custom.property.proxy.port}")
	private int proxyPort;

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
	}

	@Bean
	public ObjectWriter objectWriter(ObjectMapper objectMapper) {
		return objectMapper.writerWithDefaultPrettyPrinter();
	}

	@Bean
	public RouterFunction<ServerResponse> htmlRouter(@Value("classpath:/public/index.html") Resource html) {
		return route(GET("/"), request -> ok().contentType(MediaType.TEXT_HTML).bodyValue(html));
	}

	@Bean
	public WebClient webClient() throws SSLException {
		HttpClient httpClient = null;

		if (proxyEnabled) {
			httpClient = HttpClient.create().tcpConfiguration(tcpClient -> tcpClient
					.proxy(proxy -> proxy.type(ProxyProvider.Proxy.HTTP).host(proxyHost).port(proxyPort)));
		} else {
			httpClient = HttpClient.create();
		}

		ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
		WebClient client = WebClient.builder().clientConnector(connector).build();

		return client;
	}
}
