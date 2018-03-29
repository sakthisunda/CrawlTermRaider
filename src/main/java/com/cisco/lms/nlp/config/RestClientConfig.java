package com.cisco.lms.nlp.config;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.cisco.lms.nlp.helper.TopicsServiceProxy;

@Configuration
@ComponentScan(basePackages = { "com.cisco.lms.nlp.helper" })
public class RestClientConfig {

	@Value("${rest.client.connectionTimeoutMillis}")
	private int restClientConnectionTimeoutMillis;

	@Value("${rest.client.readTimeoutMillis}")
	private int restClientReadTimeoutMillis;

	@Value("${rest.client.maxConnectionsPerHost}")
	private int restClientMaxConnectionsPerHost;

	@Value("${rest.client.maxTotalConnections}")
	private int restClientMaxTotalConnections;

	@Bean
	public HttpClient getHttpClient() {
		RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectTimeout(restClientConnectionTimeoutMillis).setSocketTimeout(restClientReadTimeoutMillis).setConnectionRequestTimeout(restClientConnectionTimeoutMillis).build();
		return HttpClientBuilder.create().setMaxConnPerRoute(restClientMaxConnectionsPerHost).setMaxConnTotal(restClientMaxTotalConnections).setDefaultRequestConfig(defaultRequestConfig).setRetryHandler(new DefaultHttpRequestRetryHandler(3, true)).build();
	}

	@Bean
	public ClientHttpRequestFactory getClientHttpRequestFactory() {

		return new HttpComponentsClientHttpRequestFactory(getHttpClient());

	}
	
	@Bean
	public TopicsServiceProxy topicsService()  {

		return new TopicsServiceProxy();
	}

	@Bean
	public RestTemplate getRestTemplate() {
		RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		return restTemplate;
	}

}
