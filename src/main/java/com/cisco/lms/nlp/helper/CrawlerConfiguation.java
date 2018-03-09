package com.cisco.lms.nlp.helper;

import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;

@Component
public class CrawlerConfiguation {
	
	private static String crawlStorageFolder = "/opt/crawler";

	private CrawlerConfiguation() {
		super();
	}

	
	public static CrawlConfig build() {
		
		CrawlConfig config = new CrawlConfig();
		config.setPolitenessDelay(1000);
		config.setMaxDepthOfCrawling(0);
		config.setMaxPagesToFetch(1000);
		config.setIncludeBinaryContentInCrawling(false);  
		config.setCrawlStorageFolder(crawlStorageFolder);
		config.setFollowRedirects(false);
		System.out.println(config.toString());
		return config;
		
		
	}
	
}
