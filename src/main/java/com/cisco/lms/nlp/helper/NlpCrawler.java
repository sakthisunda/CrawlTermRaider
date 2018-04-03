package com.cisco.lms.nlp.helper;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.url.WebURL;

@Component
@Scope(value = "prototype")
public class NlpCrawler extends WebCrawler {

	@Autowired
	CrawlerUtils crawlerUtils;

	@Autowired
	TopicProcessor topicProcessor;

	private static final Logger LOG = LoggerFactory.getLogger(NlpCrawler.class);

	private static final Pattern FILTERS = Pattern.compile(".*(\\.(svg|css|js|bmp|gif|ico|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4" + "|wav|avi|mov|mpeg|ram|m4v" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	private static final String[] exclude = { "ar_ae", "bg_bg", "bs_ba", "cs_cz", "da_dk", "de_at", "de_ch", "de_de", "el_gr", "en_be", "en_ph", "en_za", "es_ar", "es_mx", "es_bz", "es_cl", "es_bo", "es_co", "es_cr", "es_es", "es_gt", "es_hn", "es_ni", "es_pa", "es_pe", "es_py", "es_sv", "es_uy", "es_ve", "fi_fi", "fr_ca", "fr_be", "fr_ch", "fr_fr", "hr_hr",
			"hu_hu", "it_it", "ja_jp", "ko_kr", "nl_be", "nl_nl", "no_no", "pl_pl", "pt_br", "pt_pt", "ro_ro", "ru_kz", "ru_ru", "ru_ua", "sk_sk", "sl_si", "sr_rs", "sv_se", "th_th", "tr_tr", "vi_vn", "zh_cn", "zh_hk", "zh_tw" };

	private TermRaiderCrawlerFactory factory;

	private Set<String> domains = new HashSet<>();

	public NlpCrawler() {
		LOG.info(" *********************************** Crawler Instance Created ****************************");
	}

	public void setFactory(TermRaiderCrawlerFactory factory) {

		this.factory = factory;
	}

	public void setAllowedDomains(List<String> urlList) {

		urlList.forEach(url -> {
			WebURL u = new WebURL();
			u.setURL(url);
			domains.add(u.getDomain());
		});

		LOG.info(" **** Allowed Domains for {}: {}", this.getClass().getName(), domains);
	}

	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {

		String href = url.getURL().toLowerCase();
		String domain = url.getDomain();

		if (FILTERS.matcher(href).matches() || !domains.contains(domain)) {
			LOG.info("Skipping file: {} on  {} ***************", href, domain);
			return false;
		}

		for (String lang : exclude) {

			if (href.contains(lang)) {
				LOG.info("Skipping file: {} for invalid language ***************", href);
				return false;
			}
		}

		LOG.info("Qualified for crawling url : {} on  {} ***************", href, domain);

		return true;

	}

	@Override
	public void visit(Page page) {

		String url = page.getWebURL().getURL();
		LOG.debug("Visiting URL:{}", url);
		ParseData data = page.getParseData();
		LOG.info(String.format("%s is instance of %s", url, data.getClass().getTypeName()));
		String fileName = url.replaceAll("[^\\p{L}\\p{Nd}]+", "_");

		if (factory.isTaggingEnabled()) {
			topicProcessor.process(page, data, factory.getOutputDir() + File.separator + fileName + ".ttl");
			return;
		}

		if (data instanceof HtmlParseData) {

			crawlerUtils.writeHtmlFilteredData(data, factory.getOutputDir() + File.separator + fileName + ".html");

		} else if (data instanceof BinaryParseData) {

			String path = factory.getOutputDir() + File.separator + fileName;
			crawlerUtils.writeBinaryData(url, page.getContentData(), path);

		} else {

			LOG.warn("******** Skipping url {} with data type: {} ********", url, data.getClass().getTypeName());

		}

	}
}
