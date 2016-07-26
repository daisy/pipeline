package org.daisy.dotify.writer.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.daisy.dotify.api.writer.MediaTypes;
import org.daisy.dotify.api.writer.PagedMediaWriterFactory;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryService;

import aQute.bnd.annotation.component.Component;

@Component
public class PEFMediaWriterFactoryService implements
		PagedMediaWriterFactoryService {
	
	private final static List<String> mediaTypes;
	static {
		mediaTypes = new ArrayList<>();
		mediaTypes.add(MediaTypes.PEF_MEDIA_TYPE);
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
		return new PEFMediaWriterFactory();
	}

	@Override
	public void setCreatedWithSPI() {
	}

}
