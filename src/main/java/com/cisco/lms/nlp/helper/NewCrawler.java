package com.cisco.lms.nlp.helper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class NewCrawler extends WebCrawler {

	@Autowired
	Environment env;

	@Autowired
	TermRaiderUtils termRaiderUtils;

	private static final Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg" + "|png|mp3|mp4|zip|gz))$");

	private String outputDir;

	public NewCrawler(Environment env) throws IOException {
		this.env = env;
	}

	public void setOutputDir(String outputDirName) throws IOException {
		this.outputDir = outputDirName;
	}

	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {

		String href = url.getURL().toLowerCase();
		String domain = url.getDomain();
		System.out.printf("****************Domain name:%s ***************", domain);
		if (FILTERS.matcher(href).matches()) {
			return false;
		}
		return true;

	}

	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();
		System.out.printf("Visiting URL:%s\n", url);

		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();

			try (PrintWriter pw = new PrintWriter(outputDir + File.separator + url.replaceAll("[^\\p{L}\\p{Nd}]+", "_"))) {
				Document htmlDoc = Jsoup.parse(htmlParseData.getHtml());
				pw.write(StringEscapeUtils.unescapeHtml(Jsoup.clean(htmlDoc.select("p").html(), Whitelist.none())));
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			String html = htmlParseData.getHtml();
			Set<WebURL> links = htmlParseData.getOutgoingUrls();

			System.out.println("Text length: " + text.length());
			System.out.println("Html length: " + html.length());
			System.out.println("Number of outgoing links: " + links.size());
		}
	}
}
