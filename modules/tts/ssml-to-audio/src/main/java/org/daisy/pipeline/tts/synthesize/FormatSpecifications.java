package org.daisy.pipeline.tts.synthesize;

import net.sf.saxon.s9api.QName;

public interface FormatSpecifications {

	static final String AudioNS = "http://www.daisy.org/ns/pipeline/data";
	static final String LogNS = "http://www.daisy.org/ns/pipeline/data";
	static final String SSMLNS = "http://www.w3.org/2001/10/synthesis";
	static final String MarkDelimiter = "___";

	public static final QName ClipTag = new QName(AudioNS, "clip");
	public static final QName OutputRootTag = new QName(AudioNS, "audio-clips");

	public static final QName Audio_attr_clipBegin = new QName("", "clipBegin");
	public static final QName Audio_attr_clipEnd = new QName("", "clipEnd");
	public static final QName Audio_attr_src = new QName("", "src");
	public static final QName Audio_attr_id = new QName("", "idref");

	public static final QName SentenceTag = new QName(SSMLNS, "s");
	public static final QName TokenTag = new QName(SSMLNS, "token");
	public static final QName Sentence_attr_id = new QName("", "id");
	public static final QName Sentence_attr_select1 = new QName("voice-selector1");
	public static final QName Sentence_attr_select2 = new QName("voice-selector2");
	public static final QName Sentence_attr_gender = new QName("voice-gender");
	public static final QName Sentence_attr_age = new QName("voice-age");
	public static final QName Sentence_attr_lang = new QName(
	        "http://www.w3.org/XML/1998/namespace", "lang");

	public static final QName LogRootTag = new QName(LogNS, "log");
	public static final QName LogErrorTag = new QName(LogNS, "error");
	public static final QName LogTextTag = new QName(LogNS, "text");
	public static final QName LogSsmlTag = new QName(LogNS, "ssml");
	public static final QName LogInpTag = new QName(LogNS, "input");
	public static final QName Log_attr_code = new QName("", "key");
	public static final QName Log_attr_file = new QName("", "file");
	public static final QName Log_attr_id = new QName("", "id");
	public static final QName Log_attr_begin = new QName("", "begin");
	public static final QName Log_attr_end = new QName("", "end");
	public static final QName Log_attr_selected_voice = new QName("", "selected-voice");
	public static final QName Log_attr_actual_voice = new QName("", "actual-voice");
	public static final QName Log_attr_timeout = new QName("", "timeout");
}
