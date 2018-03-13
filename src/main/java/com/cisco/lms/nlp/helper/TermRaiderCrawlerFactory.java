package com.cisco.lms.nlp.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.CrawlController;

@Component
public class TermRaiderCrawlerFactory implements CrawlController.WebCrawlerFactory<NewCrawler> {

	@Autowired
	Environment env;

	@Autowired
	TermRaiderUtils utils;

	private Path outputDir;

	public void setOutputDir(String url) throws IOException {

		outputDir = Files.createDirectories(Paths.get(env.getProperty(DefaultConstants.OUTPUT_DIR_KEY) + File.separator + utils.urlToFolderName(url)));
	}

	public Path getOutputDir() throws IOException {

		return outputDir;
	}

	@Override
	public NewCrawler newInstance() throws IOException {
		NewCrawler crawler = new NewCrawler(env);
		crawler.setOutputDir(getOutputDir().toString());
		return crawler;
	}
}