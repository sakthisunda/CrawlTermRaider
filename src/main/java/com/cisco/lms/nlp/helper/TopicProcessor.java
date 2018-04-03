package com.cisco.lms.nlp.helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
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

	@Async
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
		} else if (data instanceof BinaryParseData) {

			PdfReader reader = new PdfReader(page.getContentData());
			title = reader.getInfo().get("title");
			StringBuilder builder = new StringBuilder();
			for (int pageNum = 1; pageNum <= reader.getNumberOfPages(); pageNum++) {
				builder.append(PdfTextExtractor.getTextFromPage(reader, pageNum));
			}

			model.put("topics", getTopics(builder.toString()));
		}

		model.put("title", title);
		model.put("creationDate", crawlerUtils.getZuluDate());
		model.put("url", page.getWebURL().getURL());
		String triple = templateTranformer.tranformVelocityTemplate("urlMetadata.vm", model);
		LOG.debug(" Generated Triple:\n{}", triple);
		crawlerUtils.writeTTL(triple, fileName);

	}

}