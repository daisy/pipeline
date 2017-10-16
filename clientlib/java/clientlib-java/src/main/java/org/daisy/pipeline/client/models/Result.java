package org.daisy.pipeline.client.models;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.utils.Files;
import org.daisy.pipeline.client.utils.XML;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Result implements Comparable<Result> {

	public String href;
	public String file;
	public String mimeType;
	public String name;
	public String from;
	public String nicename;
	public Long size;

	// convenience variables
	public String filename;
	public String prettyRelativeHref;
	public String relativeHref;

	private static Pattern filenamePattern = Pattern.compile(".*/");
	private static Pattern idxPattern = Pattern.compile("^/?idx/[^/]+/");
	private static Pattern relativeHrefPattern = Pattern.compile("^.*?/jobs/[^/]+/result(.*)$");

	public static Result parseResultXml(Node resultNode) throws Pipeline2Exception {
		String parentHref = XPath.selectText("../@href", resultNode, XPath.dp2ns); // can be from /job/@href, /job/results/@href or /job/results/result/@href
		return parseResultXml(resultNode, parentHref);
	}

	public static Result parseResultXml(Node resultNode, String base) throws Pipeline2Exception {
		Result item = new Result();

		item.href = XPath.selectText("@href", resultNode, XPath.dp2ns);
		item.file= XPath.selectText("@file", resultNode, XPath.dp2ns);
		item.mimeType = XPath.selectText("@mime-type", resultNode, XPath.dp2ns);
		item.name = XPath.selectText("@name", resultNode, XPath.dp2ns);
		item.from = XPath.selectText("@from", resultNode, XPath.dp2ns);
		item.nicename = XPath.selectText("@nicename", resultNode, XPath.dp2ns);
		
		if ("results".equals(resultNode.getNodeName())) {
			// all results
			item.filename = "results.zip";
			
		} else if (item.from != null && !"".equals(item.from)) {
			// option or port
			item.filename = (item.name != null && !"".equals(item.name) ? item.name : item.from) + ".zip";
			
		} else {
			// single file
			item.filename = item.href == null ? null : filenamePattern.matcher(item.href).replaceAll("");
		}

		if (base != null) {
			if (base.length() >= item.href.length()) {
				item.prettyRelativeHref = "";
			} else {
				item.prettyRelativeHref = item.href == null ? null : item.href.substring(base.length() + 1);
				Matcher m = idxPattern.matcher(item.prettyRelativeHref);
				item.prettyRelativeHref = m.replaceFirst("");
			}
		}
		Matcher prettyRelativeHrefMatcher = relativeHrefPattern.matcher(item.href);
		if (prettyRelativeHrefMatcher.matches()) {
			item.relativeHref = prettyRelativeHrefMatcher.group(1);
			if (item.relativeHref.length() > 0) {
				item.relativeHref = item.relativeHref.substring(1); // remove "/" at start of string
			}
		}

		String sizeText = XPath.selectText("@size", resultNode, XPath.dp2ns);
		if (sizeText != null && sizeText.length() > 0) {
			item.size = Long.parseLong(sizeText);
		} else {
			item.size = 0L;
			List<Node> leafNodes = XPath.selectNodes(".//d:result[not(*)]", resultNode, XPath.dp2ns);
			for (Node leafNode : leafNodes) {
				String childSize = XPath.selectText("@size", leafNode, XPath.dp2ns);
				item.size += childSize == null ? 0 : Long.parseLong(childSize);
			}
		}

		return item;
	}

	public int compareTo(Result other) {
		return href.compareTo(other.href);
	}

	public void toXml(Element resultElement) {
		if (href != null) {
		    resultElement.setAttribute("href", href);
		}
		if (file != null) {
		    resultElement.setAttribute("file", file);
		}
		if (mimeType != null) {
		    resultElement.setAttribute("mime-type", mimeType);
		}
		if (name != null) {
		    resultElement.setAttribute("name", name);
		}
		if (from != null) {
		    resultElement.setAttribute("from", from);
		}
		if (size != null) {
		    resultElement.setAttribute("size", size+"");
		}
	}

	/**
	 * Get the result as a File object if possible; null otherwise.
	 * 
	 * @return The file
	 */
	public File asFile() {
		if (file == null) {
			return null;

		} else {
			URI fileUri;
			try {
				fileUri = new URI(file);
				File f = new File(fileUri);
				if (f.isFile()) {
					return f;

				} else {
					return null;
				}
			} catch (URISyntaxException e) {
				Pipeline2Logger.logger().error("Could not parse file: URL", e);
				return null;
			}
		}
	}

	/**
	 * Get the result as a String object if possible; null otherwise.
	 * 
	 * @return The text in the file
	 */
	public String asText() {
		File f = asFile();
		if (f == null) {
			return null;

		} else {
			return Files.read(f);
		}
	}

	/**
	 * Get the result as a Document object if possible; null otherwise.
	 * 
	 * @return The XML in the file
	 */
	public Document asXml() {
		String text = asText();
		if (text == null) {
			return null;

		} else {
			return XML.getXml(text);
		}
	}

}
