package com.corp.concepts.reactive.rxjava.config;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
	@Value("${custom.property.proxy.enabled}")
	private boolean proxyEnabled;

	@Value("${custom.property.proxy.host}")
	private String proxyHost;

	@Value("${custom.property.proxy.port}")
	private int proxyPort;

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		RestTemplate template = builder.build();

		if (proxyEnabled) {
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

			Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
			requestFactory.setProxy(proxy);

			template.setRequestFactory(requestFactory);
		}

		return template;
	}

}
