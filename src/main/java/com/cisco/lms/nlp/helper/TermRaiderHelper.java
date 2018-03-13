package com.cisco.lms.nlp.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import gate.Corpus;
import gate.Factory;
import gate.termraider.bank.AbstractTermbank;
import gate.termraider.output.CsvGenerator;
import gate.util.GateException;

@Component
public class TermRaiderHelper {

	private static final Logger LOG = LoggerFactory.getLogger(TermRaiderHelper.class);

	@Autowired
	@Qualifier("theApp")
	gate.CorpusController controller;

	@Autowired
	NlpCrawler nlpCrawler;

	@Autowired
	CsvToTurtleGenerator csvToTurtleGenerator;

	@Value(value = "classpath:termraider.vm")
	private Resource raiderTripleFile;

	@Autowired
	Environment env;

	@Autowired
	TermRaiderUtils utils;

	public void createTermBank(String rootUrl) throws Exception {

		// Remove all non-word characters
		String basePath = utils.urlToFolderName(rootUrl);
		utils.setOutputFolder(rootUrl);

		// Initialize term raider and execute after adding the copus of
		// documents
		controller.init();
		controller.setCorpus(getOuputCorpus());
		controller.execute();

		Corpus outCorpus = controller.getCorpus();
		LOG.debug("temraider corpus generated:{}", outCorpus.getFeatures());

		AbstractTermbank frequencyTermbank = getTermBank(outCorpus, "tfidfTermbank");
		AbstractTermbank hyponymytermbank = getTermBank(outCorpus, "hyponymyTermbank");
		AbstractTermbank annotationtermbank = getTermBank(outCorpus, "annotationTermbank");

		LOG.debug("frquencyTermBank:{}", frequencyTermbank);
		LOG.debug("hyponymyTermbank:{}", hyponymytermbank);
		LOG.debug("annotationTermbank:{}", annotationtermbank);

		// create all termbank file names

		String frequencyFileName = new SimpleDateFormat("'frequency'yyyy-MM-dd-HH-mm'.csv'").format(new Date());
		String hyponymFileName = new SimpleDateFormat("'generic'yyyy-MM-dd-HH-mm'.csv'").format(new Date());
		String annotateFileName = new SimpleDateFormat("'annotate'yyyy-MM-dd-HH-mm'.csv'").format(new Date());

		// Create csv files
		createFile(frequencyFileName);
		createFile(hyponymFileName);
		createFile(annotateFileName);

		// save term banks in created csv files
		saveTermBank(frequencyTermbank, frequencyFileName);
		saveTermBank(hyponymytermbank, hyponymFileName);
		saveTermBank(annotationtermbank, annotateFileName);

		// Turtle conversion
		csvToTurtle(frequencyFileName);

	}

	private AbstractTermbank getTermBank(Corpus corpus, String bankType) {
		return (AbstractTermbank) corpus.getFeatures().get(bankType);
	}

	private void saveTermBank(AbstractTermbank termBank, String outputFileName) throws GateException, IOException {
		Files.createDirectories(utils.getOutputFolder());
		CsvGenerator.generateAndSaveCsv(termBank, 0, Paths.get(utils.getOutputFolder() + File.separator + outputFileName).toFile());
	}

	private boolean createFile(String fileName) throws IOException {
		Files.createDirectories(utils.getOutputFolder());
		return Paths.get(utils.getOutputFolder() + File.separator + fileName).toFile().createNewFile();
	}

	private void csvToTurtle(String fileName) throws IOException {
		Files.createDirectories(utils.getOutputFolder());
		csvToTurtleGenerator.setCsvAbsoluteFileName(utils.getOutputFolder() + File.separator + fileName);
		int extensionPos = fileName.lastIndexOf('.');
		String ttlFile = fileName.substring(0, extensionPos).concat(".ttl");
		csvToTurtleGenerator.setTurtleAbsoluteFileName(utils.getOutputFolder() + File.separator + ttlFile);
		csvToTurtleGenerator.saveTurtleFile();
	}

	public Corpus getOuputCorpus() throws Exception {

		Files.createDirectories(utils.getOutputFolder());
		Corpus corpus = Factory.newCorpus("raiderCorpus");

		Arrays.stream(utils.getOutputFolder().toFile().listFiles()).filter(File::isFile).forEach(file -> {
			try {
				System.out.printf("Iterating **** %s\n", file.getAbsolutePath());
				corpus.add(Factory.newDocument(file.toURI().toURL(), "UTF-8"));
			} catch (Exception ex) {
				System.out.printf("Error while adding %s to corpus\n", ex.getMessage());
			}
		});

		return corpus;

	}
}
