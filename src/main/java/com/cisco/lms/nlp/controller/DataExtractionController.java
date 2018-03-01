package com.cisco.lms.nlp.controller;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.WebAsyncTask;

import com.cisco.lms.nlp.helper.NlpCrawler;

import au.com.bytecode.opencsv.CSVReader;
import gate.Corpus;
import gate.termraider.bank.AbstractTermbank;
import gate.termraider.output.CsvGenerator;
import gate.util.LanguageAnalyserDocumentProcessor;

@Controller
@RequestMapping("/")
public class DataExtractionController {

	private static final Logger LOG = LoggerFactory.getLogger(DataExtractionController.class);

	@Value("${gate.file.folder}")
	private String gateFileFolder;

	@Value("${raider.output.dir}")
	private String raiderOutputDir;

	@Autowired
	LanguageAnalyserDocumentProcessor processor;

	@Autowired
	@Qualifier("theApp")
	gate.CorpusController controller;

	@Autowired
	NlpCrawler nlpCrawler;

	@Autowired
	AsyncTaskExecutor threadPoolExecutor;

	@Autowired
	Environment env;

	@RequestMapping(method = RequestMethod.GET, value = "/crawl", produces = { MediaType.APPLICATION_JSON_VALUE })
	public WebAsyncTask<ResponseEntity<Map<String, String>>> crawl() {

		Callable<ResponseEntity<Map<String, String>>> callableResponseEntity = new Callable<ResponseEntity<Map<String, String>>>() {
			@Override
			public ResponseEntity<Map<String, String>> call() throws Exception {
				Map<String, String> retJson = new HashMap<>();
				retJson.put("success", "Request Successful.");
				return new ResponseEntity<>(retJson, HttpStatus.ACCEPTED);
			}

		};

		return new WebAsyncTask<>(600000L, threadPoolExecutor, callableResponseEntity);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/crawl", produces = { MediaType.APPLICATION_JSON_VALUE })
	public WebAsyncTask<ResponseEntity<Map<String, Object>>> crawl(@RequestBody Map<String, Object> bodyContent) {

		Callable<ResponseEntity<Map<String, Object>>> callableResponseEntity = new Callable<ResponseEntity<Map<String, Object>>>() {
			@Override
			public ResponseEntity<Map<String, Object>> call() throws Exception {

				String outputDir = env.getProperty("raider.output.dir");
				String url = (String) bodyContent.get("rootUrl");
				nlpCrawler.setRootUrl(url);
				nlpCrawler.setCorpus("saksunda");
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

				CSVReader reader = new CSVReader(new FileReader(outputDir + "\\frequency.csv"), ',', '"', 1);
				String[] nextLine;

				try (FileWriter fw = new FileWriter(outputDir + "\\frequency.ttl")) {

					while ((nextLine = reader.readNext()) != null) {

						if ("multiword".equalsIgnoreCase(nextLine[2])) {
							if (nextLine[0].split(" ").length == 2) {
								fw.write(String.format("%s %d\n", nextLine[0], Integer.parseInt(nextLine[5])));
								fw.flush();
							}
						}

					}
				}

				Map<String, Object> retJson = new HashMap<>();
				retJson.put("success", corpus.getDocumentNames());

				return new ResponseEntity<>(retJson, HttpStatus.ACCEPTED);
			}

		};

		return new WebAsyncTask<>(600000L, threadPoolExecutor, callableResponseEntity);

	}

}