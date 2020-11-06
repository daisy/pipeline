package org.daisy.pipeline.maven.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import com.google.common.base.Optional;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.xpath.XPathFactoryImpl;

import org.xml.sax.InputSource;

abstract class utils {
	
	static abstract class URLs {
		
		static URI asURI(File file) {
			return file.toURI();
		}
		
		static URI asURI(URL url) {
			try {
				return url.toURI();
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException(e);
			}
		}
		
		static URI asURI(String url) {
			try {
				return new URI(url);
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException(e);
			}
		}
		
		static URI relativize(URI base, URI child) {
			try {
				if (base.isOpaque() || child.isOpaque()
				    || !Optional.fromNullable(base.getScheme()).or("").equalsIgnoreCase(Optional.fromNullable(child.getScheme()).or(""))
				    || !Optional.fromNullable(base.getAuthority()).equals(Optional.fromNullable(child.getAuthority())))
					return child;
				else {
					String bp = base.normalize().getPath();
					String cp = child.normalize().getPath();
					String relativizedPath;
					if (cp.startsWith("/")) {
						String[] bpSegments = bp.split("/", -1);
						String[] cpSegments = cp.split("/", -1);
						int i = bpSegments.length - 1;
						int j = 0;
						while (i > 0) {
							if (bpSegments[j].equals(cpSegments[j])) {
								i--;
								j++; }
							else
								break; }
						relativizedPath = "";
						while (i > 0) {
							relativizedPath += "../";
							i--; }
						while (j < cpSegments.length) {
							relativizedPath += cpSegments[j] + "/";
							j++; }
						relativizedPath = relativizedPath.substring(0, relativizedPath.length() - 1); }
					else
						relativizedPath = cp;
					if (relativizedPath.isEmpty())
						relativizedPath = "./";
					return new URI(null, null, relativizedPath, child.getQuery(), child.getFragment()); }}
			catch (URISyntaxException e) {
				throw new RuntimeException(e); }
		}
	}
	
	static abstract class XML {
		
		private static XPath xpath;
		static {
			XPathFactoryImpl xpathFactory = new XPathFactoryImpl();
			// to make messages not end up on stderr
			xpathFactory.getConfiguration().setErrorListener(new ErrorListener() {
					public void error(TransformerException exception) {}
					public void fatalError(TransformerException exception) {}
					public void warning(TransformerException exception) {}
				});
			xpath = xpathFactory.newXPath();
		}
		
		static Object evaluateXPath(File context, String expression, final Map<String,String> namespaces, Class<?> type) {
			return evaluateXPath(URLs.asURI(context), expression, namespaces, type);
		}
		
		static Object evaluateXPath(URI context, String expression, final Map<String,String> namespaces, Class<?> type) {
			try {
				if (namespaces != null)
					xpath.setNamespaceContext(
						new NamespaceContext() {
							public String getNamespaceURI(String prefix) {
								return namespaces.get(prefix); }
							public String getPrefix(String namespaceURI) {
								for (String prefix : namespaces.keySet())
									if (namespaces.get(prefix).equals(namespaceURI))
										return prefix;
								return null; }
							public Iterator<String> getPrefixes(String namespaceURI) {
								List<String> prefixes = new ArrayList<String>();
								for (String prefix : namespaces.keySet())
									if (namespaces.get(prefix).equals(namespaceURI))
										prefixes.add(prefix);
								return prefixes.iterator(); }});
				else
					xpath.setNamespaceContext(null);
				XPathExpression expr = xpath.compile(expression);
				InputSource source = new InputSource(context.toURL().openStream());
				if (type.equals(Boolean.class))
					return expr.evaluate(source, XPathConstants.BOOLEAN);
				if (type.equals(String.class))
					return expr.evaluate(source, XPathConstants.STRING);
				if (type.equals(Integer.class))
					return ((Double)expr.evaluate(source, XPathConstants.NUMBER)).intValue();
				if (type.equals(Long.class))
					return ((Double)expr.evaluate(source, XPathConstants.NUMBER)).longValue();
				else
					throw new RuntimeException("Cannot evaluate to a " + type.getName());
			} catch (Exception e) {
				throw new RuntimeException("Exception occured during XPath evaluation", e);
			}
		}
		
		static void transform(File input, File output, URI xslt, Map<String,Object> params, URIResolver uriResolver) {
			try {
				TransformerFactoryImpl factory = new TransformerFactoryImpl();
				if (uriResolver != null)
					factory.getConfiguration().setURIResolver(uriResolver);
				Transformer transformer = factory.newTransformer(new StreamSource(xslt.toURL().openStream(), xslt.toString()));
				if (params != null)
					for (String p : params.keySet())
						transformer.setParameter(p, params.get(p));
				transformer.transform(new StreamSource(input), new StreamResult(output));
			} catch (Exception e) {
				throw new RuntimeException("Exception occured during XSLT transformation", e);
			}
		}
	}
}
