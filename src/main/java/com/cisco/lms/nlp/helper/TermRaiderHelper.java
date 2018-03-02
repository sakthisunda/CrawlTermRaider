package com.cisco.lms.nlp.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
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

	public void createTermBank(String rootUrl) throws GateException, IOException {

		nlpCrawler.setRootUrl(rootUrl);
		nlpCrawler.setCorpus("termBank");
		nlpCrawler.execute();

		Corpus corpus = nlpCrawler.getOutputCorpus();
		controller.init();
		controller.setCorpus(corpus);
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
		String outputDir = env.getProperty(DefaultConstants.OUTPUT_DIR_KEY);
		Path outputDirPath = Files.createDirectories(Paths.get(outputDir));
		CsvGenerator.generateAndSaveCsv(termBank, 0, Paths.get(outputDirPath + File.separator + outputFileName).toFile());
	}

	private boolean createFile(String fileName) throws IOException {
		String outputDir = env.getProperty(DefaultConstants.OUTPUT_DIR_KEY);
		Path outputDirPath = Files.createDirectories(Paths.get(outputDir));
		return Paths.get(outputDirPath + File.separator + fileName).toFile().createNewFile();
	}

	private void csvToTurtle(String fileName) throws IOException {
		String outputDir = env.getProperty(DefaultConstants.OUTPUT_DIR_KEY);
		Path outputDirPath = Files.createDirectories(Paths.get(outputDir));
		csvToTurtleGenerator.setCsvAbsoluteFileName(outputDirPath + File.separator + fileName);
		int extensionPos = fileName.lastIndexOf('.');
		String ttlFile = fileName.substring(0, extensionPos).concat(".ttl");
		csvToTurtleGenerator.setTurtleAbsoluteFileName(outputDir + File.separator + ttlFile);
		csvToTurtleGenerator.saveTurtleFile();
	}
}
