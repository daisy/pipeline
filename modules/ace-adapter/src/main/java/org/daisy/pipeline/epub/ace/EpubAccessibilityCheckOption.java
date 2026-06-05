package org.daisy.pipeline.epub.ace;

import java.util.ListResourceBundle;
import java.util.NoSuchElementException;

import org.osgi.framework.Version;

public class EpubAccessibilityCheckOption extends ListResourceBundle {

	private static final String latestAceVersion = "1.4.6";

	private final Object[][] resources;

	public EpubAccessibilityCheckOption() {
		super();
		String name = "Enable accessibility check";
		String desc = "Check the compliance to the EPUB accessibility specification using the [DAISY Ace](https://daisy.github.io/ace) tool.";
		try {
			Ace ace = AceFinder.get();
			try {
				Version version = Version.parseVersion(ace.getVersion());
				desc += String.format("\n\nAce version %s was found on your system.", version);
				if (version.compareTo(Version.parseVersion(latestAceVersion)) < 0)
					desc += String.format(" A newer version is available. Check [how to update Ace]("
					                      + "https://daisy.github.io/ace/getting-started/installation/#install-or-update-ace).");
			} catch (IllegalArgumentException e) {
			}
		} catch (NoSuchElementException e) {
			desc += "\n\nThis option can currently not be used because Ace was not found on your system.";
			desc += " Check [how to install Ace](https://daisy.github.io/ace/getting-started/installation/) on your system.";
		}
		desc += "\n\nThis option is only available for zipped EPUBs.";
		resources = new Object[][]{
			{ "epub3-validator.accessibility-check.name", name },
			{ "epub3-validator.accessibility-check.desc", desc }
		};
	}

	@Override
	protected Object[][] getContents() {
		return resources;
	}
}
