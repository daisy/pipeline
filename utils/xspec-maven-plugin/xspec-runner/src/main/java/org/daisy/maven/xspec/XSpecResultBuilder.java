/**
 * Copyright (C) 2013 The DAISY Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.daisy.maven.xspec;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.maven.xspec.TestResults.Builder;

public final class XSpecResultBuilder {

	private XSpecResultBuilder() {
	};

	public static TestResults fromReport(String name, XdmNode xdmResult,
			XPathCompiler xpathCompiler, String time) {
		Builder builder = new TestResults.Builder(name).time(time);

		try {
			builder.addRuns(
					((XdmAtomicValue) xpathCompiler.evaluateSingle(
							"count(//test)", xdmResult)).getLongValue())
					.addFailures(
							((XdmAtomicValue) xpathCompiler.evaluateSingle(
									"count(//test[@successful = 'false'])",
									xdmResult)).getLongValue())
					.addSkipped(
							((XdmAtomicValue) xpathCompiler.evaluateSingle(
									"count(//test[@pending = 'true'])",
									xdmResult)).getLongValue());
			for (XdmItem xdmItem : xpathCompiler
					.evaluate(
							"//test[@successful='false']/string-join(ancestor-or-self::*/label,' ')",
							xdmResult)) {
				builder.addFailureDetail(xdmItem.getStringValue(), null);
			}
		} catch (SaxonApiException e) {
			throw new IllegalStateException(e);
		}
		return builder.build();
	}

	public static TestResults fromException(String name,
			SaxonApiException exception, String time) {
		return new TestResults.Builder(name).time(time).addError()
				.addErrorDetail("XSpec runtime error", exception.getMessage())
				.build();
	}

}
