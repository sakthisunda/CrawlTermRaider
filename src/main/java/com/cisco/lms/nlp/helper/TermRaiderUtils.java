package com.cisco.lms.nlp.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class TermRaiderUtils {

	@Autowired
	Environment env;

	private static final String ZULU_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	private Path outputFolder;

	public String urlToFolderName(String url) {
		if (url == null)
			return url;
		return url.replaceAll("[^\\p{L}\\p{Nd}]+", "_");
	}

	public void setOutputFolder(String url) throws IOException {
		String outputDir = env.getProperty(DefaultConstants.OUTPUT_DIR_KEY) + File.separator + urlToFolderName(url);
		outputFolder = Files.createDirectories(Paths.get(outputDir));
	}

	public Path getOutputFolder() {
		return outputFolder;
	}

	public String getZuluDate() {
		Date date = new Date();
		java.text.DateFormat df = new java.text.SimpleDateFormat(ZULU_FORMAT);
		df.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
		return df.format(date);
	}

}
