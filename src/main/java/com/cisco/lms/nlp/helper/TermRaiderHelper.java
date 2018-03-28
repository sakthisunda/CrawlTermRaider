package com.cisco.lms.nlp.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import gate.Corpus;
import gate.Factory;
import gate.termraider.bank.AbstractTermbank;
import gate.termraider.output.CsvGenerator;
import gate.util.GateException;

@Component
@Scope(value = "prototype")
public class TermRaiderHelper {

	private static final Logger LOG = LoggerFactory.getLogger(TermRaiderHelper.class);

	@Autowired
	@Qualifier("theApp")
	gate.CorpusController controller;

	@Autowired
	CsvToTurtleGenerator csvToTurtleGenerator;

	@Value(value = "classpath:termraider.vm")
	private Resource raiderTripleFile;

	@Autowired
	Environment env;

	@Autowired
	TermRaiderUtils utils;

	private Path outputFolder;

	private Random newRandom = new Random();

	public TermRaiderHelper() {
		LOG.info(" Instance of TermRaiderHelper created");

	}

	public void createTermBank(Map<String, Object> bodyContent) throws Exception {

		// Remove all non-word characters
		LOG.debug("Term raider starts working on the documents");
		List<String> urlList = (List<String>) bodyContent.get("rootUrl");
		setOutputFolder(urlList.get(0));

		// Initialize term raider and execute after adding the corpus of
		// documents
		controller.init();
		controller.setCorpus(getOuputCorpus());
		controller.execute();
		controller.cleanup();

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
		csvToTurtle(frequencyFileName, (String) bodyContent.get("category"));

	}

	private AbstractTermbank getTermBank(Corpus corpus, String bankType) {
		return (AbstractTermbank) corpus.getFeatures().get(bankType);
	}

	private void saveTermBank(AbstractTermbank termBank, String outputFileName) throws GateException, IOException {
		Files.createDirectories(this.getOutputFolder());
		CsvGenerator.generateAndSaveCsv(termBank, 0, Paths.get(this.getOutputFolder() + File.separator + outputFileName).toFile());
	}

	private boolean createFile(String fileName) throws IOException {
		Files.createDirectories(this.getOutputFolder());
		return Paths.get(this.getOutputFolder() + File.separator + fileName).toFile().createNewFile();
	}

	private void csvToTurtle(String fileName, String category) throws IOException {
		Files.createDirectories(this.getOutputFolder());
		csvToTurtleGenerator.setCsvAbsoluteFileName(this.getOutputFolder() + File.separator + fileName);
		int extensionPos = fileName.lastIndexOf('.');
		String ttlFile = fileName.substring(0, extensionPos).concat(".ttl");
		csvToTurtleGenerator.setTurtleAbsoluteFileName(this.getOutputFolder() + File.separator + ttlFile);
		csvToTurtleGenerator.saveTurtleFile(category);
	}

	public Corpus getOuputCorpus() throws Exception {

		Files.createDirectories(this.getOutputFolder());
		String corpusName = "raiderCorpus-" + newRandom.nextInt(5000);
		Corpus corpus = Factory.newCorpus(corpusName);

		LOG.info("*** Collecting output from : {} for corpus {} creation", this.getOutputFolder(), corpusName);
		Arrays.stream(this.getOutputFolder().toFile().listFiles()).filter(File::isFile).forEach(file -> {
			try {
				LOG.debug("Iterating **** {}", file.getAbsolutePath());
				corpus.add(Factory.newDocument(file.toURI().toURL(), "UTF-8"));
			} catch (Exception ex) {
				LOG.debug("Error while adding {} to corpus:{}", file, ex);
			}
		});

		return corpus;

	}

	public void setOutputFolder(String url) throws IOException {
		String outputDir = env.getProperty(DefaultConstants.OUTPUT_DIR_KEY) + File.separator + utils.urlToFolderName(url);
		outputFolder = Files.createDirectories(Paths.get(outputDir));
	}

	public Path getOutputFolder() {
		return outputFolder;
	}
}
