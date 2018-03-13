package com.cisco.lms.nlp.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class TermRaiderUtils {

	@Autowired
	Environment env;

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

}
