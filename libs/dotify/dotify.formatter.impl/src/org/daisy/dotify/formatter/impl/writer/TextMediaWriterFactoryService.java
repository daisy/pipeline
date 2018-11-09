package org.daisy.dotify.formatter.impl.writer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.daisy.dotify.api.writer.MediaTypes;
import org.daisy.dotify.api.writer.PagedMediaWriterFactory;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryService;
import org.osgi.service.component.annotations.Component;

/**
 * Provides a paged media writer factory for plain text.
 * @author Joel Håkansson
 */
@Component
public class TextMediaWriterFactoryService implements
		PagedMediaWriterFactoryService {
	
	private static final List<String> mediaTypes;
	static {
		mediaTypes = new ArrayList<>();
		mediaTypes.add(MediaTypes.TEXT_MEDIA_TYPE);
	}

	@Override
	public boolean supportsMediaType(String mediaType) {
		for (String l : mediaTypes) {
			if (l.equalsIgnoreCase(mediaType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Collection<String> listMediaTypes() {
		return mediaTypes;
	}

	@Override
	public PagedMediaWriterFactory newFactory(String mediaType) {
		return new TextMediaWriterFactory();
	}

	@Override
	public void setCreatedWithSPI() {
	}

}
