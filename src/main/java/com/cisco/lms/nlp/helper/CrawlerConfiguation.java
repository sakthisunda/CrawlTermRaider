package com.cisco.lms.nlp.helper;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;

@Component
public class CrawlerConfiguation {
	
	@Autowired
    Environment env;
	
	private static String crawlStorageFolder = "/opt/crawler";

	private CrawlerConfiguation() {
		super();
	}
	
	public CrawlConfig build() throws Exception {
		Files.createDirectories(Paths.get(crawlStorageFolder));
		CrawlConfig config = new CrawlConfig();
		config.setPolitenessDelay(1000);
	    config.setMaxDepthOfCrawling( env.getProperty("crawler.depth") != null ? Integer.valueOf(env.getProperty("crawler.depth")) : 1);
		config.setMaxPagesToFetch(1000);
		config.setIncludeBinaryContentInCrawling(false);  
		config.setCrawlStorageFolder(crawlStorageFolder);
		config.setFollowRedirects(false);
		System.out.println(config.toString());
		return config;		
		
	}
	
}
