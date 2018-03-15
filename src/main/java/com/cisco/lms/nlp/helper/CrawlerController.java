package com.cisco.lms.nlp.helper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

@Component
public class CrawlerController {

	@Autowired
	Environment env;

	@Autowired
	TermRaiderUtils utils;	
	
	@Autowired
    private Provider<TermRaiderCrawlerFactory> termRaiderCrawlerFactoryProvider;

	private int numberOfCrawlers = 10;
	private CrawlConfig config;

	public void setConfiguration(CrawlConfig configuration) {

		this.config = configuration;

	}

	public void crawl(List<String> seedUrls) throws Exception {

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		
		TermRaiderCrawlerFactory factory = termRaiderCrawlerFactoryProvider.get();
		Optional.ofNullable(seedUrls).orElseGet(() -> Collections.<String>emptyList()).forEach( url -> {
			controller.addSeed(url);
			factory.addSeed(url);
		});
	
		System.out.println(" Seed Urls:" + seedUrls);
		factory.setOutputDir(seedUrls.get(0)); // output folders created based on first seed url	

		controller.start(factory, numberOfCrawlers);

	}

}
