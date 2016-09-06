package ch.sbs.pipeline.braille.impl;

import java.io.File;
import java.net.URL;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.daisy.pipeline.braille.common.util.URIs.asURI;
import static org.daisy.pipeline.braille.common.util.URLs.asURL;
import org.daisy.pipeline.braille.liblouis.LiblouisTablePath;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.ComponentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "ch.sbs.pipeline.braille.impl.TablePath",
	service = {
		LiblouisTablePath.class
	},
	property = {
		"identifier:String=http://www.sbs.ch/pipeline/liblouis/tables/",
		"path:String=/tables"
	}
)

public class TablePath extends LiblouisTablePath {
	
	private final static String WHITELIST_BASE_PROPERTY = "ch.sbs.whitelist.base";
	private final static Pattern WHITELIST_NAME_PATTERN = Pattern.compile("^sbs-de-(g1|g2|g2-place|g2-name)-white(-(?<docid>.*))?\\.mod$");
	
	private File whitelistBase;
	private URL emptyTable;
	
	@Activate
	protected void activate(ComponentContext context, Map<?,?> properties) throws Exception {
		super.activate(context, properties);
		String whitelistBasePath = System.getProperty(WHITELIST_BASE_PROPERTY);
		whitelistBase = whitelistBasePath == null ? null : new File(whitelistBasePath);
		emptyTable = resolve(asURI("_empty"));
	}
	
	public URL resolve(URI resource) {
		URL resolved = super.resolve(resource);
		if (resolved == null) {
			URI relativePath = resource;
			if (relativePath.isAbsolute())
				relativePath = getIdentifier().relativize(resource);
			if (!relativePath.isAbsolute()) {
				String fileName = relativePath.toString();
				Matcher m = WHITELIST_NAME_PATTERN.matcher(fileName);
				if (m.matches()) {
					if (whitelistBase != null) {
						File file = new File(whitelistBase, fileName);
						if (file.exists())
							return asURL(file);
						else {
							logger.error("Whitelist " + fileName + " could not be found in " + whitelistBase);
							return null; }}
					else {
						String docid = m.group("docid");
						if (docid != null) {
							logger.error("Requested whitelist for document " + docid
							             + ", but no ch.sbs.whitelist.base was specified.");
							return null; }
						else {
							logger.warn("No ch.sbs.whitelist.base was specified. Skipping " + fileName);
							return emptyTable; }}}}}
		return resolved;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(TablePath.class);
	
}
