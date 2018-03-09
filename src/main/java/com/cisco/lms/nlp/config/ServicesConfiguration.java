/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cisco.lms.nlp.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang.StringUtils;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ComponentScan(basePackages = { "com.cisco.lms.nlp.helper" })
@ImportResource({ "classpath:/spring-gate.xml" })
@PropertySource("classpath:term-raider.properties")
@EnableAsync(proxyTargetClass = true)
public class ServicesConfiguration implements AsyncConfigurer {

	@Autowired
	Environment env;

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Override
	@Bean
	public AsyncTaskExecutor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		try {
			String corePoolSize = StringUtils.defaultIfBlank(env.getProperty("gate.thread.corePoolSize"), "500");
			executor.setCorePoolSize(Integer.parseInt(corePoolSize));
			String maxPoolSize = StringUtils.defaultIfBlank(env.getProperty("gate.thread.maxPoolSize"), "1000");
			executor.setMaxPoolSize(Integer.parseInt(maxPoolSize));
			String maxQueueSize = StringUtils.defaultIfBlank(env.getProperty("gate.thread.maxQueueSize"), "1000");
			executor.setQueueCapacity(Integer.parseInt(maxQueueSize));
			executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
			executor.setThreadNamePrefix("TermRaider-");
			executor.initialize();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new SimpleAsyncUncaughtExceptionHandler();
	}

}