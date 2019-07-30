package org.daisy.pipeline.tts.synthesize.calabash.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.tts.Voice.MarkSupport;
import org.daisy.pipeline.tts.VoiceInfo;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.VoiceInfo.UnknownLanguage;
import org.daisy.pipeline.tts.config.ConfigProperties;
import org.daisy.pipeline.tts.config.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceConfigExtension implements ConfigReader.Extension, ConfigProperties {

	private Logger Logger = LoggerFactory.getLogger(VoiceConfigExtension.class);

	@Override
	public boolean parseNode(XdmNode node, URI documentURI) {
		String name = node.getNodeName().getLocalName();
		if ("voice".equalsIgnoreCase(name)) {
			String lang = node.getAttributeValue(new QName(null, "lang"));
			if (lang == null)
				node.getAttributeValue(new QName("http://www.w3.org/XML/1998/namespace",
				        "lang"));
			String vengine = node.getAttributeValue(new QName(null, "engine"));
			String vname = node.getAttributeValue(new QName(null, "name"));
			String priority = node.getAttributeValue(new QName(null, "priority"));
			Gender gender = Gender.of(node.getAttributeValue(new QName(null, "gender")));
			String marks = node.getAttributeValue(new QName(null, "marks"));
			MarkSupport markSupport = MarkSupport.DEFAULT;
			if ("true".equalsIgnoreCase(marks)) {
				markSupport = markSupport.MARK_SUPPORTED;
			} else if ("false".equalsIgnoreCase(marks)) {
				markSupport = MarkSupport.MARK_NOT_SUPPORTED;
			}

			if (priority == null)
				priority = "5";
			if (lang == null || vengine == null || vname == null || gender == null) {
				Logger.warn("Config file invalid near " + node.toString());
			} else {
				try {
					mVoices.add(new VoiceInfo(vengine, vname, markSupport, lang, gender, Float
					        .valueOf(priority)));
				} catch (NumberFormatException e) {
					Logger.warn("Error while converting config file's priority " + priority
					        + " to float.");
				} catch (UnknownLanguage e) {
					Logger.warn("Unknown language in config file: " +lang);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void setParentReader(ConfigReader cr) {
		mConfigReader = cr;
	}

	public Collection<VoiceInfo> getVoiceDeclarations() {
		return mVoices;
	}

	@Override
	public Map<String, String> getStaticProperties() {
		return mConfigReader.getStaticProperties();
	}

	@Override
	public Map<String, String> getDynamicProperties() {
		return mConfigReader.getStaticProperties();
	}

	@Override
	public Map<String, String> getAllProperties() {
		return mConfigReader.getAllProperties();
	}

	private ConfigReader mConfigReader;
	private Collection<VoiceInfo> mVoices = new ArrayList<VoiceInfo>();
}
