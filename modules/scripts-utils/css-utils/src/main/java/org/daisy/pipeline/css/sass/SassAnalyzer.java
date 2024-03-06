package org.daisy.pipeline.css.sass;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;

import org.daisy.pipeline.css.Medium;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.datatypes.DatatypeService;

/**
 * Analyzes a style sheet and extracts information about global variables.
 */
public class SassAnalyzer {

	private final org.daisy.pipeline.css.sass.impl.SassAnalyzer analyzer;

	public SassAnalyzer(Medium medium, URIResolver uriResolver, DatatypeRegistry datatypes) {
		analyzer = new org.daisy.pipeline.css.sass.impl.SassAnalyzer(medium, uriResolver, datatypes);
	}

	/**
	 * Get the variables declared within the given user style sheets and source document.
	 *
	 * @param userStylesheets {@code Source} objects must have absolute hierarchical URI as system ID
	 * @param sourceDocument {@code Source} object must have absolute hierarchical URI as system ID,
	 *                       or object may be {@code null}.
	 */
	public Result analyze(Iterable<Source> userStylesheets, Source sourceDocument) throws IOException {
		return new Result(analyzer.getVariableDeclarations(userStylesheets, sourceDocument));
	}

	public interface SassVariable {
		public String getName();
		public String getNiceName();
		public String getDescription();
		public String getValue();
		public boolean isDefault();
		public DatatypeService getType();
	}

	public static class Result {

		private final Collection<SassVariable> variables;

		private Result(Collection<? extends SassVariable> variables) {
			this.variables = Collections.unmodifiableCollection(variables);
		}

		public Collection<SassVariable> getVariables() {
			return variables;
		}
	}
}
