package com.cisco.lms.nlp.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.WebAsyncTask;

import com.cisco.lms.nlp.helper.CsvToTurtleGenerator;
import com.cisco.lms.nlp.helper.NlpCrawler;
import com.cisco.lms.nlp.helper.TermRaiderHelper;

@Controller
@RequestMapping("/")
public class DataExtractionController {

	private static final Logger LOG = LoggerFactory.getLogger(DataExtractionController.class);
	
	@Autowired
	@Qualifier("theApp")
	gate.CorpusController controller;

	@Autowired
	NlpCrawler nlpCrawler;

	@Autowired
	AsyncTaskExecutor threadPoolExecutor;

	@Autowired
	CsvToTurtleGenerator csvToTurtleGenerator;

	@Autowired
	Environment env;

	@Value(value = "classpath:termraider.vm")
	private Resource raiderTripleFile;

	@Autowired
	TermRaiderHelper termRaiderHelper;

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

				String url = (String) bodyContent.get("rootUrl");
				termRaiderHelper.createTermBank(url);
				Map<String, Object> retJson = new HashMap<>();
				retJson.put("success", "Request accepted");
				return new ResponseEntity<>(retJson, HttpStatus.ACCEPTED);
			}

		};

		return new WebAsyncTask<>(600000L, threadPoolExecutor, callableResponseEntity);

	}

}