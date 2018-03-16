package com.cisco.lms.nlp.helper;

import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.EscapeTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("VelocityTemplateTranformer")
public class TemplateTranformer {

	@Autowired
	private VelocityEngine velocityEngine;

	public TemplateTranformer() {

	}

	/**
	 * @return the velocityEngine
	 */
	public VelocityEngine getVelocityEngine() {
		return velocityEngine;
	}

	/**
	 * @param velocityEngine
	 *            the velocityEngine to set
	 */
	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}

	public String tranformVelocityTemplate(String velocityTemplateName, Map<String, Object> mapData) {
		VelocityContext velocityContext = new VelocityContext(mapData);
		velocityContext.put("esc", new EscapeTool());
		velocityContext.put("StringUtils", new StringUtils());
		StringWriter writer = new StringWriter();
		velocityEngine.mergeTemplate(velocityTemplateName, "utf-8", velocityContext, writer);
		return writer.toString();
	}

	public String transformStringTemplate(String template, String logTag, Map<String, Object> mapData) {
		VelocityContext velocityContext = new VelocityContext(mapData);
		velocityContext.put("esc", new EscapeTool());
		velocityContext.put(Integer.class.getSimpleName(), Integer.class);
		StringWriter writer = new StringWriter();
		velocityEngine.evaluate(velocityContext, writer, logTag, template);
		return writer.toString();

	}

}
