package com.cisco.lms.nlp.helper;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

@Component
public class CsvToTurtleGenerator {

	@Value(value = "classpath:termraider.vm")
	private Resource raiderTripleFile;

	private MessageFormat frequencyTermTriple;

	private String csvAbsoluteFileName;
	private String turtleAbsoluteFileName;

	/**
	 * Reads triple template file and keep that in String Helps in reusing the
	 * template; not reading from disk all the time
	 * 
	 * @throws IOException
	 */
	@PostConstruct
	public void init() throws IOException {

		byte[] b = new byte[1024];
		int byteSize = raiderTripleFile.getInputStream().read(b);
		frequencyTermTriple = new MessageFormat(new String(b, 0, byteSize));

	}

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
	public void saveTurtleFile() throws IOException {

		try (FileWriter fw = new FileWriter(turtleAbsoluteFileName); CSVReader reader = new CSVReader(new FileReader(csvAbsoluteFileName), ',', '"', 1);) {
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {

				if ("multiword".equalsIgnoreCase(nextLine[2])) {

					if (nextLine[0].split(" ").length == 2) {

						Object[] testArgs = { DigestUtils.md5Hex(nextLine[0]), new Integer(nextLine[5]), nextLine[0] };
						fw.write(String.format("%s\n", frequencyTermTriple.format(testArgs)));
						fw.flush();

					}
				}
			}
		}

	}

}
