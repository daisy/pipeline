package org.daisy.pipeline.epub.ace;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.daisy.common.shell.BinaryFinder;

public class AceFinder {

	private static Optional<Ace> ace = null;

	public static Ace get() throws NoSuchElementException {
		if (ace == null)
			try {
				ace = BinaryFinder.find("ace").map(f -> new Ace(new File(f)));
			} catch (RuntimeException e) {
				Ace.LOGGER.debug("Ace could not be initialized", e);
				ace = Optional.empty();
			}
		return ace.get();
	}
}
