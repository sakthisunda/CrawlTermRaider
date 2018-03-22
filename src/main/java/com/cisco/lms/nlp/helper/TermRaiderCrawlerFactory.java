package com.cisco.lms.nlp.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.CrawlController;

@Component
@Scope(value = "prototype")
public class TermRaiderCrawlerFactory implements CrawlController.WebCrawlerFactory<NewCrawler> {
	
	private static final Logger LOG = LoggerFactory.getLogger(TermRaiderCrawlerFactory.class);

	@Autowired
	Environment env;

	@Autowired
	TermRaiderUtils utils;

	private Path outputDir;

	private List<String> urlList = new ArrayList<>();
	
	public TermRaiderCrawlerFactory() {
		LOG.info(" *********************************** Crawler Factory ****************************");
	}

	public void setOutputDir(String url) throws IOException {

		outputDir = Files.createDirectories(Paths.get(env.getProperty(DefaultConstants.OUTPUT_DIR_KEY) + File.separator + utils.urlToFolderName(url)));
	}

	public Path getOutputDir() {

		return outputDir;
	}

	public void addSeed(String url) {
		urlList.add(url);
	}

	@Override
	public NewCrawler newInstance() throws IOException {
		NewCrawler crawler = new NewCrawler(env);
		crawler.setOutputDir(getOutputDir().toString());
		crawler.setAllowedDomains(urlList);
		return crawler;
	}
}