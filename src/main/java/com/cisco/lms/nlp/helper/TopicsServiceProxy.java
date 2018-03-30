package com.cisco.lms.nlp.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;


public class TopicsServiceProxy {

	private static final Logger LOG = LoggerFactory.getLogger(TopicsServiceProxy.class);

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	Environment env;

	public String[] getTopicsFromText(String text) throws JsonProcessingException, IOException {

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);

		Map<String, String> postParameters = new HashMap<String, String>();
		postParameters.put("text", text);

		HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(postParameters, headers);

		LOG.debug("Sending request to get topics for : {} from the Gate Topics service", text);

		long startTime = System.currentTimeMillis();

		String response = restTemplate.postForObject(env.getProperty("topics.base.service.URL") + "/from-text", requestEntity, String.class);

		LOG.debug("PERF STATS: total time took to process request \n: " + (System.currentTimeMillis() - startTime));

		return parseTopicServiceResponse(response);

	}

	public String[] getTopicsFromFile(File file) throws JsonProcessingException, IOException {

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<String, Object>();
		postParameters.add("file", new FileSystemResource(file));

		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(postParameters, headers);

		LOG.debug("Sending request to get topics from the Gate Topics service");

		long startTime = System.currentTimeMillis();

		String response = restTemplate.postForObject(env.getProperty("topics.base.service.URL") + "/from-file", requestEntity, String.class);

		LOG.debug("PERF STATS: total time took to process request \n: " + (System.currentTimeMillis() - startTime));

		return parseTopicServiceResponse(response);

	}

	private String[] parseTopicServiceResponse(String serviceResponse) throws JsonProcessingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectReader reader = mapper.readerFor(Map.class); // or JsonNode.class
		Map<String, Object> json = reader.readValue(serviceResponse);

		@SuppressWarnings("unchecked")
		ArrayList<String> topics = (ArrayList<String>) json.get("topics");

		if (topics == null) {
			LOG.error("error got from the Gate Topics service ==>" + serviceResponse);
			throw new IOException("Topics service returned error");
		}
		LOG.debug("parseTopicServiceResponse :: topics parsed <" + topics.toString() + ">");
		return (String[]) topics.toArray(new String[topics.size()]);

	}

}
