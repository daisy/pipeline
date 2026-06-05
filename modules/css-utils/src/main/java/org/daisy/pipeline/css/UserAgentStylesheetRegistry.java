package org.daisy.pipeline.css;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import static com.google.common.collect.Iterables.any;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "UserAgentStylesheetRegistry",
	service = { UserAgentStylesheetRegistry.class }
)
public class UserAgentStylesheetRegistry {

	/**
	 * Return the user agent style sheet(s) for the given stylesheet types, content type
	 * and medium. If there are multiple style sheets, their order is undetermined.
	 */
	public Collection<URL> get(Collection<String> types, Collection<String> contentTypes, Collection<Medium> media) {
		Collection<URL> filtered = new ArrayList<>();
		for (UserAgentStylesheet s : stylesheets)
			if (types.contains(s.getType()))
				if (any(contentTypes, s::matchesContentType))
					if (any(media, s::matchesMedium))
						filtered.add(s.getURL());
		return filtered;
	}

	private Collection<UserAgentStylesheet> stylesheets = new ArrayList<>();

	@Reference(
		name = "UserAgentStylesheet",
		unbind = "-",
		service = UserAgentStylesheet.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	protected void addStylesheet(UserAgentStylesheet stylesheet) {
		stylesheets.add(stylesheet);
	}
}
