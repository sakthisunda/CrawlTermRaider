package com.cisco.lms.nlp.helper;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;

@Component
public class CrawlerConfiguation {

	@Autowired
	Environment env;

	private static String crawlStorageFolder = "/opt/crawler";

	private static final Logger LOG = LoggerFactory.getLogger(CrawlerConfiguation.class);

	private CrawlerConfiguation() {
		super();
	}

	public CrawlConfig build() throws Exception {
		Files.createDirectories(Paths.get(crawlStorageFolder));
		CrawlConfig config = new CrawlConfig();
		config.setPolitenessDelay(1000);
		config.setMaxDepthOfCrawling(env.getProperty("crawler.depth") != null ? Integer.valueOf(env.getProperty("crawler.depth")) : 1);
		config.setMaxPagesToFetch(1000);
		config.setIncludeBinaryContentInCrawling(false);
		config.setCrawlStorageFolder(crawlStorageFolder);
		config.setFollowRedirects(false);
		LOG.debug("Configuration:{}", config.toString());
		return config;

	}

}
