package com.cisco.lms.nlp.helper;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

@Component
public class CsvToTurtleGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(CsvToTurtleGenerator.class);

	private String csvAbsoluteFileName;
	private String turtleAbsoluteFileName;

	@Autowired
	private TemplateTranformer templateTranformer;

	@Autowired
	CrawlerUtils utils;

	public String getCsvAbsoluteFileName() {
		return csvAbsoluteFileName;
	}

	public void setCsvAbsoluteFileName(String csvAbsoluteFileName) {
		this.csvAbsoluteFileName = csvAbsoluteFileName;
	}

	public String getTurtleAbsoluteFileName() {
		return turtleAbsoluteFileName;
	}

	public void setTurtleAbsoluteFileName(String turtleAbsoluteFileName) {
		this.turtleAbsoluteFileName = turtleAbsoluteFileName;
	}

	/**
	 * Saves the transformed csv to turtle file to turtleAbsoluteFileName
	 * Precondition: use setters for setting input(csv) file to output(turtle)
	 * full file path
	 * 
	 * @throws IOException
	 */
	public void saveTurtleFile(String category) throws IOException {

		try (FileWriter fw = new FileWriter(turtleAbsoluteFileName); CSVReader reader = new CSVReader(new FileReader(csvAbsoluteFileName), ',', '"', 1);) {
			String[] nextLine;

			String creationDate = utils.getZuluDate();
			while ((nextLine = reader.readNext()) != null) {

				if (DefaultConstants.MULTI_WORD.equalsIgnoreCase(nextLine[2]) && nextLine[0].split(" ").length == 2) {

					Map<String, Object> params = new HashMap<>();
					params.put("id", DigestUtils.md5Hex(nextLine[0]));
					params.put("weight", nextLine[5]);
					params.put("label", nextLine[0]);
					params.put("category", category);
					params.put("creationDate", creationDate);
					String triple = templateTranformer.tranformVelocityTemplate("termraider.vm", params);
					fw.write(triple);
					fw.flush();

				}
			}
		}

	}

}
