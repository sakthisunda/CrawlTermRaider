package com.cisco.lms.nlp.helper;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.CrawlController;


@Component
public class TermRaiderCrawlerFactory implements CrawlController.WebCrawlerFactory<NewCrawler> {     

	@Autowired
	Environment env;
	
	@Override
    public NewCrawler newInstance() throws IOException {
        return new NewCrawler(env);
    }
} 