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

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Strings;

public final class TestResults {

	public final static class Builder {
		private String name = null;
		private long runs = 0;
		private long failures = 0;
		private long errors = 0;
		private long skipped = 0;
		private String time = "";
		private Map<String, String> failuresDetails = new HashMap<String, String>();
		private Map<String, String> errorsDetails = new HashMap<String, String>();

		public Builder(String name) {
			this.name = name;
		}

		public Builder addRuns(long runs) {
			this.runs = runs;
			return this;
		}

		public Builder addRun() {
			this.runs++;
			return this;
		}

		public Builder addFailures(long failures) {
			this.failures = failures;
			return this;
		}

		public Builder addFailure() {
			this.failures++;
			return this;
		}

		public Builder addErrors(long errors) {
			this.errors = errors;
			return this;
		}

		public Builder addError() {
			this.errors++;
			return this;
		}

		public Builder addSkipped(long skipped) {
			this.skipped = skipped;
			return this;
		}

		public Builder addSkipped() {
			this.skipped++;
			return this;
		}

		public Builder time(String time) {
			this.time = time;
			return this;
		}

		public Builder failuresDetails(Map<String, String> failuresDetails) {
			this.failuresDetails.putAll(failuresDetails);
			return this;
		}

		public Builder addFailureDetail(String name, String detail) {
			this.failuresDetails.put(name, detail);
			return this;
		}

		public Builder setErrorsDetails(Map<String, String> errorsDetails) {
			this.errorsDetails.putAll(errorsDetails);
			return this;
		}

		public Builder addErrorDetail(String name, String detail) {
			this.errorsDetails.put(name, detail);
			return this;
		}

		public Builder addSubResults(TestResults results) {
			this.runs += results.runs;
			this.errors += results.errors;
			this.failures += results.failures;
			this.skipped += results.skipped;

			for (Map.Entry<String, String> detail : results.failuresDetails
					.entrySet()) {
				this.failuresDetails.put(
						new StringBuilder().append("[").append(results.name)
								.append("] ").append(detail.getKey())
								.toString(), detail.getValue());
			}
			return this;
		}

		public TestResults build() {
			return new TestResults(name, runs, failures, errors, skipped, time,
					failuresDetails, errorsDetails);
		}
	}
	
	public static final String NEWLINE = System.getProperty("line.separator");
	public static final String INDENT = "  ";

	private final String name;
	private final long runs;
	private final long failures;
	private final long errors;
	private final long skipped;
	private final String time;
	private final Map<String, String> failuresDetails;
	private final Map<String, String> errorsDetails;

	private TestResults(String name, long runs, long failures, long errors,
			long skipped, String time, Map<String, String> failuresDetails,
			Map<String, String> errorsDetails) {
		this.name = name;
		this.runs = runs;
		this.failures = failures;
		this.errors = errors;
		this.skipped = skipped;
		this.time = time;
		this.failuresDetails = failuresDetails;
		this.errorsDetails = errorsDetails;
	}

	public String getName() {
		return name;
	}

	public long getRuns() {
		return runs;
	}

	public long getErrors() {
		return errors;
	}

	public long getFailures() {
		return failures;
	}

	public long getSkipped() {
		return skipped;
	}

	public Map<String, String> getFailureDetails() {
		return null;
	}

	public Map<String, String> getErrorsDetails() {
		return null;
	}

	public String toString() {
		return appendSummary(new StringBuilder(), true).toString();
	}

	public String toDetailedString() {
		StringBuilder sb = new StringBuilder();
		sb.append(NEWLINE).append("Results:").append(NEWLINE).append(NEWLINE);
		if (failures > 0) {
			sb.append("Failed tests:").append(NEWLINE);
			for (Map.Entry<String, String> detail : failuresDetails.entrySet()) {
				sb.append(INDENT).append(detail.getKey());
				if (!Strings.isNullOrEmpty(detail.getValue()))
					sb.append(": ").append(detail.getValue());
				sb.append(NEWLINE);
			}
			sb.append(NEWLINE);
		}
		if (errors > 0) {
			sb.append("Tests in error:").append(NEWLINE);
			for (Map.Entry<String, String> detail : errorsDetails.entrySet()) {
				sb.append(INDENT).append(detail.getKey());
				if (!Strings.isNullOrEmpty(detail.getValue()))
					sb.append(": ").append(detail.getValue());
				sb.append(NEWLINE);
			}
			sb.append(NEWLINE);
		}
		return appendSummary(sb, false).append(NEWLINE).toString();
	}

	private StringBuilder appendSummary(StringBuilder sb, boolean detailed) {
		sb.append("Tests run: ").append(runs);
		sb.append(", Failures: ").append(failures);
		sb.append(", Errors: ").append(errors);
		sb.append(", Skipped: ").append(skipped);
		if (detailed) {
			sb.append(", Time elapsed: ").append(time);
			if (failures > 0 || errors > 0) {
				sb.append(" <<< FAILURE!");
			}
		}
		return sb;
	}
}
