package org.daisy.dotify.formatter.impl.row;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.dotify.api.translator.UnsupportedMetricException;
import org.daisy.dotify.formatter.impl.segment.AnchorSegment;
import org.daisy.dotify.formatter.impl.segment.IdentifierSegment;
import org.daisy.dotify.formatter.impl.segment.MarkerSegment;

/**
 * Provides an aggregated braille translator result.
 * @author Bert Frees
 * @author Joel Håkansson
 */
class AggregatedBrailleTranslatorResult implements BrailleTranslatorResult {
	private final List<Object> results;
	private int currentIndex;
	private List<Marker> pendingMarkers;
	private List<String> pendingAnchors;
	private List<String> pendingIdentifiers;
	
	/**
	 * Provides a builder for an aggregated braille translator result.
	 */
	static class Builder {
		private final List<Object> results;
		Builder() {
			this.results = new ArrayList<>();
		}
		
		Builder(Builder template) {
			this.results = copyResults(template.results);
		}
		
		/**
		 * Adds a marker to the aggregated result.
		 * @param m the marker to add
		 */
		void add(MarkerSegment m) {
			results.add(m);
		}
		
		/**
		 * Adds an anchor segment to the aggregated result.
		 * @param as the anchor segement to add
		 */
		void add(AnchorSegment as) {
			results.add(as);
		}
		
		/**
		 * Adds an identifier segment to the aggregated result.
		 * @param as the identifier segment to add
		 */
		void add(IdentifierSegment is) {
			results.add(is);
		}
		
		/**
		 * Adds a braille translator result to the aggregated result.
		 * @param bts the translator result to add
		 */
		void add(BrailleTranslatorResult bts) {
			results.add(bts);
		}
		
		/**
		 * Builds an aggregated braille translator result based on the
		 * state of this builder.
		 * @return returns a new aggregated translator result
		 */
		AggregatedBrailleTranslatorResult build() {
			return new AggregatedBrailleTranslatorResult(this);
		}

	}
	
	private AggregatedBrailleTranslatorResult(Builder builder) {
		this.results = Collections.unmodifiableList(new ArrayList<>(builder.results));
		this.currentIndex = 0;
		this.pendingMarkers = new ArrayList<>();
		this.pendingAnchors = new ArrayList<>();
		this.pendingIdentifiers = new ArrayList<>();
	}
	
	private AggregatedBrailleTranslatorResult(AggregatedBrailleTranslatorResult template) {
		this.results = copyResults(template.results);
		this.currentIndex = template.currentIndex;
		this.pendingMarkers = new ArrayList<>(template.pendingMarkers);
		this.pendingAnchors = new ArrayList<>(template.pendingAnchors);
		this.pendingIdentifiers = new ArrayList<>(template.pendingIdentifiers);
	}
	
	private static List<Object> copyResults(List<?> inputs) {
		return inputs.stream().map(o->{
			if (o instanceof BrailleTranslatorResult) {
				BrailleTranslatorResult btr = ((BrailleTranslatorResult)o);
				return btr.copy();
			} else {
				return o;
			}
		}).collect(Collectors.toList());
	}

	@Override
	public String nextTranslatedRow(int limit, boolean force, boolean wholeWordsOnly) {
		String row = "";
		BrailleTranslatorResult current = computeNext();
		while (limit > row.length()) {
			row += current.nextTranslatedRow(limit - row.length(), force);
			current = computeNext();
			if (current == null) {
				break;
			}
		}
		return row;
	}

	private BrailleTranslatorResult computeNext() {
		while (currentIndex < results.size()) {
			Object o = results.get(currentIndex);
			if (o instanceof BrailleTranslatorResult) {
				BrailleTranslatorResult current = ((BrailleTranslatorResult)o);
				if (current.hasNext()) {
					return current;
				}
			} else if (o instanceof MarkerSegment) {
				pendingMarkers.add((MarkerSegment)o);
			} else if (o instanceof AnchorSegment) {
				pendingAnchors.add(((AnchorSegment)o).getReferenceID());
			} else if (o instanceof IdentifierSegment) {
				pendingIdentifiers.add(((IdentifierSegment)o).getName());
			} else {
				throw new RuntimeException("coding error");
			}
			currentIndex++;
		}
		return null;
	}

	@Override
	public String getTranslatedRemainder() {
		String remainder = "";
		for (int i = currentIndex; i < results.size(); i++) {
			Object o = results.get(i);
			if (o instanceof BrailleTranslatorResult) {
				remainder += ((BrailleTranslatorResult)o).getTranslatedRemainder();
			}
		}
		return remainder;
	}

	@Override
	public int countRemaining() {
		int remaining = 0;
		for (int i = currentIndex; i < results.size(); i++) {
			Object o = results.get(i);
			if (o instanceof BrailleTranslatorResult) {
				remaining += ((BrailleTranslatorResult)o).countRemaining();
			}
		}
		return remaining;
	}

	@Override
	public boolean hasNext() {
		return computeNext() != null;
	}
	
	List<Marker> getMarkers() {
		return Collections.unmodifiableList(pendingMarkers);
	}
	
	List<String> getAnchors() {
		return Collections.unmodifiableList(pendingAnchors);
	}
	
	List<String> getIdentifiers() {
		return Collections.unmodifiableList(pendingIdentifiers);
	}
	
	void clearPending() {
		pendingMarkers.clear();
		pendingAnchors.clear();
		pendingIdentifiers.clear();
	}

	@Override
	public boolean supportsMetric(String metric) {
		// since we cannot assume that the individual results of any metric can be added, we only support the following known cases
		if (METRIC_FORCED_BREAK.equals(metric) || METRIC_HYPHEN_COUNT.equals(metric)) {
			for (int i = 0; i <= currentIndex && i < results.size(); i++) {
				Object o = results.get(i);
				if (o instanceof BrailleTranslatorResult) {
					if (!((BrailleTranslatorResult)o).supportsMetric(metric)) {
						return false;
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public double getMetric(String metric) {
		// since we cannot assume that the individual results of any metric can be added, we only support the following known cases
		if (METRIC_FORCED_BREAK.equals(metric) || METRIC_HYPHEN_COUNT.equals(metric)) {
			int count = 0;
			for (int i = 0; i <= currentIndex && i < results.size(); i++) {
				Object o = results.get(i);
				if (o instanceof BrailleTranslatorResult) {
					count += ((BrailleTranslatorResult)o).getMetric(metric);
				}
			}
			return count;
		} else {
			throw new UnsupportedMetricException("Metric not supported: " + metric);
		}
	}

	@Override
	public BrailleTranslatorResult copy() {
		return new AggregatedBrailleTranslatorResult(this);
	}

}