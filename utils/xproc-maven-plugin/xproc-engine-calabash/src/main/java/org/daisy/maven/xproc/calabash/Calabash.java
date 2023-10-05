package org.daisy.maven.xproc.calabash;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Consumer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import static com.google.common.base.Objects.equal;
import com.google.common.collect.Iterators;

import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritableDocument;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XPipeline;
import com.xmlcalabash.util.Input;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;

import org.daisy.maven.xproc.api.XProcEngine;
import org.daisy.maven.xproc.api.XProcExecutionException;

import org.xml.sax.InputSource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "org.daisy.maven.xproc.calabash.Calabash",
	service = { XProcEngine.class }
)
public class Calabash implements XProcEngine {
	
	private URIResolver nextURIResolver = null;
	private String nextCatalogFile = null;
	private Object nextConfig = null; // XdmNode|Consumer<XProcConfiguration>
	private XdmNode nextDefaultConfig = null;
	
	@Reference(
		name = "URIResolver",
		unbind = "-",
		service = URIResolver.class,
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.STATIC
	)
	protected void setURIResolver(URIResolver uriResolver) {
		nextURIResolver = uriResolver;
	}
	
	public void setCatalog(URL catalogFile) {
		if (catalogFile != null)
			nextCatalogFile = catalogFile.toString();
		else
			nextCatalogFile = null;
	}
	
	public void setCatalog(File catalogFile) {
		if (catalogFile != null && catalogFile.exists())
			nextCatalogFile = catalogFile.getPath();
		else
			nextCatalogFile = null;
	}
	
	// Settings from a custom calabash.xml
	public void setConfiguration(File configFile) {
		if (configFile == null)
			nextConfig = null;
		else
			try {
				XProcConfiguration config = new XProcConfiguration("he", false);
				nextConfig = config.getProcessor().newDocumentBuilder()
					.build(new SAXSource(new InputSource(new FileReader(configFile)))); }
			catch (FileNotFoundException e) {
				throw new IllegalArgumentException("Config file does not exist", e); }
			catch (SaxonApiException e) {
					throw new RuntimeException(e); }
	}

	public void setConfiguration(Consumer<XProcConfiguration> config) {
		nextConfig = config;
	}
	
	// Settings to be always applied
	public void setDefaultConfiguration(Reader defaultConfig) {
		XProcConfiguration config = new XProcConfiguration("he", false);
		try {
			nextDefaultConfig = config.getProcessor().newDocumentBuilder()
				.build(new SAXSource(new InputSource(defaultConfig))); }
		catch (SaxonApiException e) {
			throw new RuntimeException(e); }
	}
	
	private XProcRuntime currentRuntime = null;
	private URIResolver currentURIResolver = null;
	private String currentCatalogFile = null;
	private Object currentConfig = null; // XdmNode|Consumer<XProcConfiguration>
	private XdmNode currentDefaultConfig = null;
	
	protected XProcRuntime runtime() {
		
		// A new runtime needs to be created for every run because of
		// a bug in XMLCalabash with p:iteration-position().
		currentRuntime = null;
		currentURIResolver = null;
		currentCatalogFile = null;
		currentConfig = null;
		currentDefaultConfig = null;
		
		System.setProperty("com.xmlcalabash.config.user", "false");
		System.setProperty("com.xmlcalabash.config.local", "false");
		if (currentRuntime == null || !equal(nextConfig, currentConfig) || !equal(nextDefaultConfig, currentDefaultConfig)) {
			XProcConfiguration config = new XProcConfiguration("he", false);
			if (nextDefaultConfig != null)
				config.parse(nextDefaultConfig);
			if (nextConfig != null)
				if (nextConfig instanceof XdmNode)
					config.parse((XdmNode)nextConfig);
				else
					((Consumer)nextConfig).accept(config);
			if (config.saxonConfig != null) {
				// because the above does not properly set Saxon configuration from "saxon-config" setting
				config = new XProcConfiguration(config.saxonConfig);
				if (nextDefaultConfig != null)
					config.parse(nextDefaultConfig);
				if (nextConfig != null)
					if (nextConfig instanceof XdmNode)
						config.parse((XdmNode)nextConfig);
					else
						((Consumer)nextConfig).accept(config);
			}
			currentRuntime = new XProcRuntime(config); }
		if (nextURIResolver == null)
			nextURIResolver = simpleURIResolver();
		if (!equal(nextCatalogFile, currentCatalogFile) || nextURIResolver != currentURIResolver) {
			if (nextCatalogFile != null) {
				CatalogManager catalogManager = new CatalogManager();
				// catalogManager.debug.setDebug(5);
				catalogManager.setUseStaticCatalog(false);
				catalogManager.setCatalogFiles(nextCatalogFile);
				currentRuntime.setURIResolver(fallingBackURIResolver(jarURIResolver(), nextURIResolver, new CatalogResolver(catalogManager))); }
			else
				currentRuntime.setURIResolver(fallingBackURIResolver(jarURIResolver(), nextURIResolver)); }
		currentURIResolver = nextURIResolver;
		currentCatalogFile = nextCatalogFile;
		currentConfig = nextConfig;
		currentDefaultConfig = nextDefaultConfig;
		return currentRuntime;
	}
	
	public void run(String pipeline,
	                Map<String,List<String>> inputs,
	                Map<String,String> outputs,
	                Map<String,String> options,
	                Map<String,Map<String,String>> parameters)
			throws XProcExecutionException {
		run(pipeline, inputs, outputs, options, parameters, null);
	}
	
	public void run(String pipeline,
	                Map<String,List<String>> inputs,
	                Map<String,String> outputs,
	                Map<String,String> options,
	                Map<String,Map<String,String>> parameters,
	                Map<String,?> context)
			throws XProcExecutionException {
		XProcRuntime runtime = runtime();
		try {
			XPipeline xpipeline = runtime.load(new Input(pipeline));
			if (inputs != null)
				for (String port : inputs.keySet())
					for (String document : inputs.get(port))
						xpipeline.writeTo(port, runtime.parse(document, null));
			if (options != null)
				for (String name : options.keySet())
					xpipeline.passOption(new QName("", name), new RuntimeValue(options.get(name)));
			if (parameters != null)
				for (String port : parameters.keySet())
					for (String name : parameters.get(port).keySet())
						xpipeline.setParameter(port, new QName("", name), new RuntimeValue(parameters.get(port).get(name)));
			xpipeline.run();
			if (outputs != null)
				for (String port : xpipeline.getOutputs())
					if (outputs.containsKey(port)) {
						String outFile = new URI(outputs.get(port)).getPath();
						new File(outFile).getParentFile().mkdirs();
						WritableDocument wdoc = new WritableDocument(
							runtime,
							outFile,
							xpipeline.getSerialization(port),
							new FileOutputStream(outFile));
						ReadablePipe rpipe = xpipeline.readFrom(port);
						while (rpipe.moreDocuments())
							wdoc.write(rpipe.read()); }}
		catch (Throwable e) {
			throw new XProcExecutionException("Calabash failed to execute XProc", e); }
	}
		
	private static URIResolver simpleURIResolver() {
		return new URIResolver() {
			public Source resolve(String href, String base) throws TransformerException {
				if (href.startsWith("http:"))
					return null;
				try {
					URI uri;
					if (base != null)
						uri = new URI(base).resolve(new URI(href));
					else
						uri = new URI(href);
					return new SAXSource(new InputSource(uri.toASCIIString())); }
				catch (URISyntaxException e) {
					throw new TransformerException(e); }
			}
		};
	}
	
	private static URIResolver jarURIResolver() {
		return new URIResolver() {
			public Source resolve(String href, String base) throws TransformerException {
				try {
					if (!href.startsWith("http:") && base != null && base.startsWith("jar:file:"))
						return new SAXSource(new InputSource(new URL(new URL(base), href).toString())); }
				catch (MalformedURLException e) {}
				return null;
			}
		};
	}
	
	private static URIResolver fallingBackURIResolver(final URIResolver... resolvers) {
		return new URIResolver() {
			public Source resolve(String href, String base) throws TransformerException {
				Source source = null;
				Iterator<URIResolver> iterator = Iterators.forArray(resolvers);
				while (iterator.hasNext()) {
					source = iterator.next().resolve(href, base);
					if (source != null)
						break; }
				return source;
			}
		};
	}
}
