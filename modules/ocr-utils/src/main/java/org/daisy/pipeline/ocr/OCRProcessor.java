package org.daisy.pipeline.ocr;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.daisy.common.file.Resource;
import org.daisy.common.messaging.MessageAppender;

public interface OCRProcessor {

	/**
	 * Unique ID of the processor
	 */
	String getName();

	/**
	 * Nice name of the processor, for displaying in user interfaces.
	 */
	String getDisplayName();

	/**
	 * Description of the processor, for displaying in user interfaces.
	 */
	String getDescription();

	/**
	 * Run a OCR conversion on a given input.
	 *
	 * @param input      The input resource, such as a PDF file
	 * @param options    Values for the options specified by {@link OCRService#getOptions}.
	 * @param messages   {@link MessageAppender} for passing messages and progress information
	 *                   during the conversion.
	 * @param resultDir  The directory in which to store results and temporary files. Assumed to
	 *                   be empty.
	 * @return The output fileset as a set of {@code Resource} objects. Must contain
	 *         exactly one file with media type "application/xhtml+xml" or "text/html", plus any
	 *         number of other resources.
	 */
	Collection<Resource> run(Resource input,
	                         Map<String,Iterable<String>> options,
	                         MessageAppender messages,
	                         File resultDir);

}
