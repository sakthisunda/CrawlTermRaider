package com.cisco.lms.nlp.helper;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import gate.Corpus;
import gate.Factory;

@Component
public class CrawlerController {

	@Autowired
	Environment env;

	@Autowired
	TermRaiderCrawlerFactory factory;

	private int numberOfCrawlers = 7;
	private CrawlConfig config;

	public void setConfiguration(CrawlConfig configuration) {
		
		this.config = configuration;
		
	}

	public void crawl(String rootUrl) throws Exception {

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		controller.addSeed(rootUrl);
		controller.start(factory, numberOfCrawlers);

	}
	

}
