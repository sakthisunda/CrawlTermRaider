package com.cisco.lms.nlp.helper;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class NewCrawler extends WebCrawler {

	@Autowired
	Environment env;

	@Autowired
	TermRaiderUtils termRaiderUtils;

	private static final Logger LOG = LoggerFactory.getLogger(NewCrawler.class);

	private static final Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4" + "|wav|avi|mov|mpeg|ram|m4v" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

	private String outputDir;

	private Set<String> domains = new HashSet<>();

	public NewCrawler(Environment env) {
		this.env = env;
	}

	public void setOutputDir(String outputDirName) {
		this.outputDir = outputDirName;
	}

	public void setAllowedDomains(List<String> urlList) {

		urlList.forEach(url -> {
			WebURL u = new WebURL();
			u.setURL(url);
			domains.add(u.getDomain());
		});

		LOG.debug(" **** Allowed Domains : {}", domains);
	}

	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {

		String href = url.getURL().toLowerCase();
		String domain = url.getDomain();
		LOG.debug("****************Domain name: {} ***************", domain);

		if (FILTERS.matcher(href).matches() || !domains.contains(domain)) {
			return false;
		}

		return true;

	}

	@Override
	public void visit(Page page) {

		String url = page.getWebURL().getURL();
		LOG.debug("Visiting URL:{}", url);
		ParseData data = page.getParseData();
		LOG.debug(String.format("%s is instance of %s", url, data.getClass().getTypeName()));
		String extension = url.substring(url.lastIndexOf("."));
		String fileName = url.substring(0, url.lastIndexOf(".")).replaceAll("[^\\p{L}\\p{Nd}]+", "_") + extension;

		if (data instanceof HtmlParseData) {

			HtmlParseData htmlParseData = (HtmlParseData) data;
			String text = htmlParseData.getText();

			try (PrintWriter pw = new PrintWriter(outputDir + File.separator + fileName)) {
				Document htmlDoc = Jsoup.parse(htmlParseData.getHtml());
				pw.write(StringEscapeUtils.unescapeHtml(Jsoup.clean(htmlDoc.select("p").html(), Whitelist.none())));
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			String html = htmlParseData.getHtml();
			Set<WebURL> links = htmlParseData.getOutgoingUrls();


		} else if (data instanceof BinaryParseData) {

			try {
				Files.write(Paths.get(outputDir + File.separator + fileName), page.getContentData());
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
	}
}
