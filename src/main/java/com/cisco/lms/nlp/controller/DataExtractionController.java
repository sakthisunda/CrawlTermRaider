package com.cisco.lms.nlp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.async.WebAsyncTask;
import org.springframework.web.multipart.MultipartFile;

import com.cisco.lms.nlp.helper.CrawlerConfiguation;
import com.cisco.lms.nlp.helper.CrawlerController;
import com.cisco.lms.nlp.helper.CsvToTurtleGenerator;
import com.cisco.lms.nlp.helper.NlpCrawler;
import com.cisco.lms.nlp.helper.TermRaiderHelper;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;

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

	@Autowired
	CrawlerConfiguation configuration;

	@Autowired
	CrawlerController crawlerController;

	@Autowired
	TermRaiderHelper termRaiderHelper;

	@RequestMapping(method = RequestMethod.POST, value = "/crawl/from-file", produces = { MediaType.APPLICATION_JSON_VALUE })
	public WebAsyncTask<ResponseEntity<Map<String, Object>>> crawl(@RequestParam("file") MultipartFile file) {

		Callable<ResponseEntity<Map<String, Object>>> callableResponseEntity = new Callable<ResponseEntity<Map<String, Object>>>() {
			@Override
			public ResponseEntity<Map<String, Object>> call() throws Exception {

				String fileContent = new String(file.getBytes());

				Map<String, Object> retJson = new HashMap<>();
				retJson.put("success", fileContent);
				return new ResponseEntity<>(retJson, HttpStatus.ACCEPTED);
			}

		};

		return new WebAsyncTask<>(600000L, threadPoolExecutor, callableResponseEntity);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/crawl", produces = { MediaType.APPLICATION_JSON_VALUE })
	public WebAsyncTask<ResponseEntity<Map<String, Object>>> crawl(@RequestBody Map<String, Object> bodyContent, @RequestParam(value = "depth", required = false) Integer depth) {

		Callable<ResponseEntity<Map<String, Object>>> callableResponseEntity = new Callable<ResponseEntity<Map<String, Object>>>() {
			@Override
			public ResponseEntity<Map<String, Object>> call() throws Exception {
				CompletableFuture.runAsync(() -> {
					try {
						List<String> urlList = (List<String>) bodyContent.get("rootUrl");
						System.out.println(urlList);
						CrawlConfig config = configuration.build();
						if (Optional.ofNullable(depth).isPresent()) {
							config.setMaxDepthOfCrawling(depth);
						}
						crawlerController.setConfiguration(config);
						crawlerController.crawl(urlList);
						termRaiderHelper.createTermBank(bodyContent);
					} catch (Exception ex) {
						LOG.error(" Exception happened while crawl/termraid:{}", ex);
						throw new RuntimeException(ex);
					}
				}, threadPoolExecutor);

				Map<String, Object> retJson = new HashMap<>();
				retJson.put("success", "Request accepted");
				return new ResponseEntity<>(retJson, HttpStatus.ACCEPTED);
			}

		};

		return new WebAsyncTask<>(600000L, threadPoolExecutor, callableResponseEntity);
	}

}