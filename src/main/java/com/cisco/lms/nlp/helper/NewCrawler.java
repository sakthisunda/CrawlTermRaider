package com.cisco.lms.nlp.helper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class NewCrawler extends WebCrawler {

	Environment env;

	private static final Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg" + "|png|mp3|mp4|zip|gz))$");

	private String outputDir;

	
	public NewCrawler(Environment env) {
		this.env = env;
		setOutputDir(env.getProperty(DefaultConstants.OUTPUT_DIR_KEY));
	}
	
	public void setOutputDir(String outputDirName) {
		this.outputDir = outputDirName;
	}
	
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		String domain = url.getDomain();
		System.out.printf("****************Domain name:%s ***************", domain);
		if (FILTERS.matcher(href).matches()) {
			System.out.printf("Skipping URL:%s\n", url);
			return false;
		}
		return true;
		// && href.startsWith("http://www.ics.uci.edu/");
	}

	@Override
	public void visit(Page page)  {
		String url = page.getWebURL().getURL();
		System.out.printf("Visiting URL:%s\n", url);
		
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();
			
			try ( PrintWriter pw = new PrintWriter(outputDir + File.separator + url.replaceAll("[^\\p{L}\\p{Nd}]+", "_"))) {
				Document htmlDoc = Jsoup.parse(htmlParseData.getHtml());
				//pw.write(StringEscapeUtils.unescapeHtml(Jsoup.parse(htmlParseData.getHtml()),new Whitelist().addTags("p"))));
				pw.write(StringEscapeUtils.unescapeHtml(Jsoup.clean(htmlDoc.select("p").html(), Whitelist.none())));
			} catch(Exception ex) {
				System.out.println(ex.getMessage());
			}
			
			String html = htmlParseData.getHtml();
			Set<WebURL> links = htmlParseData.getOutgoingUrls();

			System.out.println("Text length: " + text.length());
			System.out.println("Html length: " + html.length());
			System.out.println("Number of outgoing links: " + links.size());
		}
	}

}
