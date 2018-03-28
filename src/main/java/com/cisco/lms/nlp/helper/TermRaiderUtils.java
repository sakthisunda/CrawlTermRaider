package com.cisco.lms.nlp.helper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.ParseData;

@Component
public class TermRaiderUtils {

	@Autowired
	Environment env;

	private static final Logger LOG = LoggerFactory.getLogger(TermRaiderUtils.class);

	private static final String ZULU_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	
	public String urlToFolderName(String url) {
		if (url == null)
			return url;
		return url.replaceAll("[^\\p{L}\\p{Nd}]+", "_");
	}

	

	public String getZuluDate() {
		Date date = new Date();
		java.text.DateFormat df = new java.text.SimpleDateFormat(ZULU_FORMAT);
		df.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
		return df.format(date);
	}

	public void writeHtmlFilteredData(ParseData data, String fileName) {

		HtmlParseData htmlParseData = (HtmlParseData) data;

		try (PrintWriter pw = new PrintWriter(fileName)) {
			Document htmlDoc = Jsoup.parse(htmlParseData.getHtml());
			pw.write(StringEscapeUtils.unescapeHtml(Jsoup.clean(htmlDoc.select("p").html(), Whitelist.none())));
		} catch (Exception ex) {
			LOG.info("Exception parsing Html data:{}", ex);
		}

	}

	public void writeBinaryData(String url, byte[] data, String filePath) {

		try {

			String extension = url.substring(url.lastIndexOf('.'));
			String path = null;

			if (extension.indexOf('/') == -1) {
				path = filePath + extension;
			} else {
				LOG.info(" Adding .txt extension for path:{}", path);
				path = filePath + ".txt";

			}

			Files.write(Paths.get(path), data);

		} catch (Exception ex) {
			LOG.info("Exception parsing binary data:{}", ex);
		}

	}

}
