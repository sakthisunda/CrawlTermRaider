package com.cisco.lms.nlp.helper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

@Component
public class CrawlerProxy {

	@Autowired
	Environment env;

	@Autowired
	private Provider<TermRaiderCrawlerFactory> termRaiderCrawlerFactoryProvider;

	private static final Logger LOG = LoggerFactory.getLogger(CrawlerProxy.class);
	private int numberOfCrawlers = 25;
	private CrawlConfig config;

	public void setConfiguration(CrawlConfig configuration) {

		this.config = configuration;

	}

	public void crawl(List<String> seedUrls, boolean... isTaggingEnabled) throws Exception {

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

		TermRaiderCrawlerFactory factory = termRaiderCrawlerFactoryProvider.get();
		Optional.ofNullable(seedUrls).orElseGet(() -> Collections.<String>emptyList()).forEach(url -> {
			controller.addSeed(url); // This is mandatory
			factory.addSeed(url);
		});

		LOG.info("Seed Urls:{}", seedUrls);
		factory.setOutputDir(seedUrls.get(0)); // output folders created based

		// Set tagging
		if (isTaggingEnabled.length > 0) {
			factory.setTagging(isTaggingEnabled[0]);
		}

		controller.start(factory, numberOfCrawlers);

	}

}
