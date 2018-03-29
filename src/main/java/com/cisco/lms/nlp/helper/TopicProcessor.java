package com.cisco.lms.nlp.helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.ParseData;

@Component
public class TopicProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(TopicProcessor.class);

	@Autowired
	Environment env;

	@Autowired
	CrawlerUtils crawlerUtils;

	@Autowired
	TopicsServiceProxy topicsServiceProxy;

	@Autowired
	private TemplateTranformer templateTranformer;

	public TopicProcessor() {

		LOG.info("TopicProcessor instance got created");
	}

	public void process(Page page, ParseData data, String fileName) {

		try {

			LOG.info("Topic Processor processing the contents of:{}", page.getWebURL());
			persistData(page, data, fileName);

		} catch (IOException ex) {

			LOG.error("Error while topic processing {} with {}", page.getWebURL().getURL(), ex);

		}

	}

	public String[] getTopics(ParseData data) throws IOException {

		return topicsServiceProxy.getTopicsFromText(crawlerUtils.getHtmlFilteredData(data));

	}

	public String[] getTopics(String data) throws IOException {

		return topicsServiceProxy.getTopicsFromText(data);

	}

	public void persistData(Page page, ParseData data, String fileName) throws IOException {

		Map<String, Object> model = new HashMap<>();
		model.put("id", DigestUtils.md5Hex(page.getWebURL().getURL()));
		String title = "unknown";
		if (data instanceof HtmlParseData) {
			title = crawlerUtils.getTitle(crawlerUtils.getHtmlData(data));
			model.put("topics", getTopics(data));
		} else {
			model.put("topics", getTopics(new String(page.getContentData())));
		}

		model.put("title", title);
		model.put("creationDate", crawlerUtils.getZuluDate());
		model.put("url", page.getWebURL().getURL());
		String triple = templateTranformer.tranformVelocityTemplate("urlMetadata.vm", model);
		LOG.info(" Generated Triple:\n{}", triple);
		crawlerUtils.writeTTL(triple, fileName);

	}

}