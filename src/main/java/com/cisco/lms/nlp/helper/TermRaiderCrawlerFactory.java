package com.cisco.lms.nlp.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.CrawlController;


@Component
public class TermRaiderCrawlerFactory implements CrawlController.WebCrawlerFactory<NewCrawler> {     

	@Autowired
	Environment env;
	
	@Override
    public NewCrawler newInstance() throws Exception {
        return new NewCrawler(env);
    }
} 