package org.daisy.pipeline.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.daisy.pipeline.datatypes.ValidationResult;
import org.daisy.pipeline.job.JobResources;

public class BoundScript {

	private final Script script;
	private final ScriptInput input;

	public static class Builder {

		private final Script script;
		private final ScriptInput.Builder input;

		public Builder(Script script) {
			this(script, null);
		}

		public Builder(Script script, JobResources resources) {
			this.script = script;
			this.input = new ScriptInput.Builder(resources);
		}

		/**
		 * Put a single document on the specified input port. All documents that are put on a port
		 * form a sequence.
		 *
		 * @throws IllegalArgumentException if the script does not have the specified port, or the
		 *         port does not accept a sequence of documents and multiple documents are supplied.
		 * @throws FileNotFoundException if the URI can not be resolved to a document.
		 */
		public Builder withInput(String port, URI source) throws IllegalArgumentException, FileNotFoundException {
			checkInputPort(port);
			input.withInput(port, source);
			return this;
		}

		/**
		 * Put a single document on the specified input port. All documents that are put on a port
		 * form a sequence.
		 *
		 * @throws IllegalArgumentException if the script does not have the specified port, or the
		 *         port does not accept a sequence of documents and multiple documents are supplied.
		 * @throws FileNotFoundException if <code>source</code> does not exist.
		 */
		public Builder withInput(String port, File source) throws IllegalArgumentException, FileNotFoundException {
			checkInputPort(port);
			input.withInput(port, source);
			return this;
		}

		/**
		 * Put a single document on the specified input port. All documents that are put on a port
		 * form a sequence.
		 *
		 * @throws IllegalArgumentException if the script does not have the specified port, or the
		 *         port does not accept a sequence of documents and multiple documents are supplied.
		 * @throws FileNotFoundException if the URL can not be resolved to a document.
		 */
		public Builder withInput(String port, URL source) throws FileNotFoundException {
			checkInputPort(port);
			input.withInput(port, source);
			return this;
		}

		/**
		 * Put a single document on the specified input port. All documents that are put on a port
		 * form a sequence.
		 *
		 * @throws IllegalArgumentException if the script does not have the specified port, or the
		 *         port does not accept a sequence of documents and multiple documents are supplied.
		 */
		public Builder withInput(String port, InputStream source) {
			checkInputPort(port);
			input.withInput(port, source);
			return this;
		}

		/**
		 * Put a single document on the specified input port. All documents that are put on a port
		 * form a sequence.
		 *
		 * @throws IllegalArgumentException if the script does not have the specified port, the port
		 *         does not accept a sequence of documents and multiple documents are supplied, or
		 *         if <code>source</code> is not a {@link SAXSource} and has an empty system ID.
		 * @throws FileNotFoundException if <code>source</code> is not a {@link SAXSource} and the
		 *         system ID can not be resolved to a document.
		 */
		public Builder withInput(String port, Source source) throws IllegalArgumentException, FileNotFoundException {
			checkInputPort(port);
			input.withInput(port, source);
			return this;
		}

		/**
		 * Set a single value for an option. All values that are set on an option form a sequence.
		 *
		 * @throws IllegalArgumentException if the script does not have the specified option, the
		 *         option does not accept a sequence of values and multiple values are supplied, or
		 *         the value is not valid according to the option type.
		 */
		public Builder withOption(String name, String value) throws FileNotFoundException {
			ScriptOption option = script.getOption(name);
			if (option == null)
				throw new IllegalArgumentException(
					String.format("Option '%s' is not recognized by script '%s'", name, script.getId()));
			if (!option.isSequence() && input.options.containsKey(name))
				throw new IllegalArgumentException(
					String.format("Option '%s' of script '%s' does not accept a sequence of values", name, script.getId()));
			ValidationResult valid = option.getType().validate(value);
			if (!valid.isValid())
				throw new IllegalArgumentException(
					String.format("Value '%s' not accepted for option '%s' of script '%s'%s",
					              value, name, script.getId(),
					              valid.getMessage().isPresent() ? ("\n" + valid.getMessage().get()) : ""));
			input.withOption(name, value);
			return this;
		}

		private void checkInputPort(String port) throws IllegalArgumentException {
			ScriptPort p = script.getInputPort(port);
			if (p == null)
				throw new IllegalArgumentException(
					String.format("Input '%s' is not recognized by script '%s'", port, script.getId()));
			if (!p.isSequence() && input.inputs.containsKey(port))
				throw new IllegalArgumentException(
					String.format("Input '%s' of script '%s' does not accept a sequence of documents", port, script.getId()));
		}

		/**
		 * @throws IllegalArgumentException if no documents were supplied for a required inputs or
		 *         no values were supplied for a required option.
		 */
		public BoundScript build() throws IllegalArgumentException {
			for (ScriptPort p : script.getInputPorts()) {
				if (!input.inputs.containsKey(p.getName()) && p.isRequired())
					throw new IllegalArgumentException(
						String.format("Required input '%s' of script '%s' not specified", p.getName(), script.getId()));
			}
			for (ScriptOption o : script.getOptions()) {
				if (!input.options.containsKey(o.getName()))
					// note that when the option is not required we don't set the default value
					// because the script is expected to automatically use the default value
					if (o.isRequired())
						throw new IllegalArgumentException(
							String.format("Required option '%s' of script '%s' not specified", o.getName(), script.getId()));
			}
			return new BoundScript(script, input.build());
		}
	}

	private BoundScript(Script script, ScriptInput input) {
		this.script = script;
		this.input = input;
	}

	public Script getScript() {
		return script;
	}

	public ScriptInput getInput() {
		return input;
	}
}
