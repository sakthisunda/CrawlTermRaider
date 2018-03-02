package com.cisco.lms.nlp.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import gate.Corpus;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.termraider.bank.AbstractTermbank;
import gate.termraider.output.CsvGenerator;
import gate.util.GateException;

@Component
public class TermRaiderHelper {
	

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
	
	
    public void createTermBank(String roolUrl) throws GateException, IOException, ExecutionException, ResourceInstantiationException {
    	
    	nlpCrawler.setRootUrl(roolUrl);
		nlpCrawler.setCorpus("termBank");
		nlpCrawler.execute();

		Corpus corpus = nlpCrawler.getOutputCorpus();
		controller.init();
		controller.setCorpus(corpus);
		controller.execute();

		Corpus outCorpus = controller.getCorpus();
		System.out.println(outCorpus.getFeatures());

		AbstractTermbank termbank = (AbstractTermbank) outCorpus.getFeatures().get("tfidfTermbank");
		AbstractTermbank hyponymytermbank = (AbstractTermbank) outCorpus.getFeatures().get("hyponymyTermbank");
		AbstractTermbank annotationtermbank = (AbstractTermbank) outCorpus.getFeatures().get("annotationTermbank");

		System.out.println("frquencyTermBank:" + termbank);
		System.out.println("hyponymyTermbank:" + hyponymytermbank);
		System.out.println("annotationTermbank:" + annotationtermbank);

		
		String outputDir = env.getProperty("raider.output.dir");
		File fPath = new File(outputDir + "\\frequency.csv");
		if (!fPath.exists())
			Files.createFile(fPath.toPath());

		File gPath = new File(outputDir + "\\generic.csv");
		if (!gPath.exists())
			Files.createFile(gPath.toPath());

		File aPath = new File(outputDir + "\\annotation.csv");
		if (!aPath.exists())
			Files.createFile(aPath.toPath());

		CsvGenerator.generateAndSaveCsv(termbank, 0, fPath);
		CsvGenerator.generateAndSaveCsv(hyponymytermbank, 0, gPath);
		CsvGenerator.generateAndSaveCsv(annotationtermbank, 0, aPath);
		
		csvToTurtleGenerator.setCsvAbsoluteFileName(outputDir + "\\frequency.csv");
		csvToTurtleGenerator.setTurtleAbsoluteFileName(outputDir + "\\frequency.ttl");
		csvToTurtleGenerator.saveTurtleFile();
    	
    }

}
