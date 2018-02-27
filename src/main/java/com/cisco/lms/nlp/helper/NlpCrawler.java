package com.cisco.lms.nlp.helper;

import java.net.MalformedURLException;

import org.springframework.stereotype.Component;

import crawl.CrawlPR;
import gate.Corpus;
import gate.Factory;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;

@Component
public class NlpCrawler {

	private CrawlPR crawler;

	public NlpCrawler() {

		crawler = new CrawlPR();
		crawler.setKeywordsCaseSensitive(true);
		crawler.setConvertXmlTypes(true);
		crawler.setDomain(crawl.DomainMode.WEB);
		crawler.setDepth(1);
		crawler.setDfs(false);
		crawler.setMaxPageSize(100);
		crawler.setStopAfter(5);

	}

	public void execute() throws ExecutionException {

		crawler.execute();
	}

	public void setCorpus(String name) throws ResourceInstantiationException, MalformedURLException {
		Corpus corpus = Factory.newCorpus(name);
		//FeatureMap params = Factory.newFeatureMap();
		//params.put("sourceUrl", new URL(crawler.getRoot()));
		//params.put("preserveOriginalContent", new Boolean(true));
		//params.put("collectRepositioningInfo", new Boolean(true));
		//Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", params);
		//corpus.add(doc);

		crawler.setOutputCorpus(corpus);
	}

	public Corpus getOutputCorpus() {

		return crawler.getOutputCorpus();
	}

	public String getRootUrl() {
		return crawler.getRoot();
	}

	public void setRootUrl(String rootUrl) {
		crawler.setRoot(rootUrl);
	}

	public int getDepth() {
		return crawler.getDepth();
	}

	public void setDepth(int depth) {
		crawler.setDepth(depth);
	}

	public boolean getIsDepthFirst() {
		return crawler.getDfs();
	}

	public void setIsDepthFirst(boolean isDepthFirst) {
		crawler.setDfs(isDepthFirst);
	}

	public int getMaxPageSize() {
		return crawler.getMaxPageSize();
	}

	public void setMaxPageSize(int maxPageSize) {
		crawler.setMaxPageSize(maxPageSize);
	}

	public int getStopAfter() {
		return crawler.getStopAfter();
	}

	public void setStopAfter(int stopAfter) {
		crawler.setStopAfter(stopAfter);
	}

}
