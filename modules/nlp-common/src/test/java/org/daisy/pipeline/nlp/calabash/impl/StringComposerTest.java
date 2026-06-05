package org.daisy.pipeline.nlp.calabash.impl;

import java.util.Arrays;
import java.util.List;

import org.daisy.pipeline.nlp.calabash.impl.StringComposer.SentencePointer;
import org.daisy.pipeline.nlp.calabash.impl.StringComposer.TextPointer;
import org.daisy.pipeline.nlp.lexing.LexResultPrettyPrinter;
import org.daisy.pipeline.nlp.lexing.LexService.Sentence;
import org.daisy.pipeline.nlp.lexing.LexService.TextBoundaries;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

;

public class StringComposerTest {
	static StringComposer sc;
	static LexResultPrettyPrinter lexPrinter;
	static SegmentsPrettyPrinter segmentsPrinter;

	@BeforeClass
	static public void setUp() {
		sc = new StringComposer();
		lexPrinter = new LexResultPrettyPrinter();
		lexPrinter.enableTrimming(true);
		segmentsPrinter = new SegmentsPrettyPrinter();
	}

	static private void checkBoundaries(TextPointer pointer, String[] segments) {
		Assert.assertFalse(segments[pointer.firstSegment].length() == pointer.firstIndex);
		Assert.assertFalse(0 == pointer.lastIndex);
	}

	public void check(Sentence[] sentences, String[] segments, boolean[] lexHelper) {
		List<Sentence> sents = Arrays.asList(sentences);
		List<String> segs = Arrays.asList(segments);

		if (lexHelper == null) {
			lexHelper = new boolean[segments.length];
			Arrays.fill(lexHelper, false);
		}
		List<SentencePointer> pointers = sc.makePointers(sents, segs, lexHelper);

		String expected = lexPrinter.convert(sents, sc.concat(segs));
		String actual = segmentsPrinter.convert(pointers, segs);

		Assert.assertEquals(expected, actual);

		//check that no pointer refers to segment left/right bounds
		for (SentencePointer p : pointers) {
			checkBoundaries(p.boundaries, segments);
			if (p.content != null)
				for (TextPointer wp : p.content) {
					checkBoundaries(wp, segments);
				}
		}
	}

	@Test
	public void oneSegment() {
		String[] segments = new String[]{
			"one segment"
		};

		Sentence s = new Sentence();
		s.boundaries = new TextBoundaries();
		s.boundaries.left = 1;
		s.boundaries.right = 4;

		check(new Sentence[]{
			s
		}, segments, null);
	}

	@Test
	public void twoSegments() {
		String[] segments = new String[]{
		        "one segment", "two segments"
		};

		Sentence s = new Sentence();
		s.boundaries = new TextBoundaries();
		s.boundaries.left = 1;
		s.boundaries.right = segments[0].length() + 3;

		check(new Sentence[]{
			s
		}, segments, null);
	}

	@Test
	public void middleNull() {
		String[] segments = new String[]{
		        "one segment", null, "two segments"
		};

		Sentence s = new Sentence();
		s.boundaries = new TextBoundaries();
		s.boundaries.left = 1;
		s.boundaries.right = segments[0].length() + 3;

		check(new Sentence[]{
			s
		}, segments, null);
	}

	@Test
	public void leftNull() {
		String[] segments = new String[]{
		        null, "one segment", "two segments"
		};

		Sentence s = new Sentence();
		s.boundaries = new TextBoundaries();
		s.boundaries.left = 1;
		s.boundaries.right = segments[1].length() + 3;

		check(new Sentence[]{
			s
		}, segments, null);
	}

	@Test
	public void rightNull() {
		String[] segments = new String[]{
		        "one segment", "two segments", null
		};

		Sentence s = new Sentence();
		s.boundaries = new TextBoundaries();
		s.boundaries.left = 1;
		s.boundaries.right = segments[0].length() + 3;

		check(new Sentence[]{
			s
		}, segments, null);
	}

	@Test
	public void twoSentences() {
		String[] segments = new String[]{
		        "one segment", "two segments"
		};

		Sentence s1 = new Sentence();
		s1.boundaries = new TextBoundaries();
		s1.boundaries.left = 1;
		s1.boundaries.right = segments[0].length() + 2;

		Sentence s2 = new Sentence();
		s2.boundaries = new TextBoundaries();
		s2.boundaries.left = s1.boundaries.right + 2;
		s2.boundaries.right = segments[1].length() + segments[0].length() - 1;

		check(new Sentence[]{
		        s1, s2
		}, segments, null);
	}

	@Test
	public void pointerToEnd1() {
		String[] segments = new String[]{
		        "one segment", "two segments"
		};

		Sentence s1 = new Sentence();
		s1.boundaries = new TextBoundaries();
		s1.boundaries.left = 1;
		s1.boundaries.right = segments[0].length() + 2;

		Sentence s2 = new Sentence();
		s2.boundaries = new TextBoundaries();
		s2.boundaries.left = s1.boundaries.right + 2;
		s2.boundaries.right = segments[1].length() + segments[0].length();

		check(new Sentence[]{
		        s1, s2
		}, segments, null);
	}

	@Test
	public void intersection1() {
		String[] segments = new String[]{
		        "one segment", "two segments"
		};

		Sentence s1 = new Sentence();
		s1.boundaries = new TextBoundaries();

		for (int shift = -1; shift <= 1; ++shift) {
			s1.boundaries.left = 0;
			s1.boundaries.right = segments[0].length() + shift;

			check(new Sentence[]{
				s1
			}, segments, null);
		}
	}

	@Test
	public void intersection2() {
		String[] segments = new String[]{
		        "one segment", "two segments"
		};

		Sentence s1 = new Sentence();
		s1.boundaries = new TextBoundaries();

		Sentence s2 = new Sentence();
		s2.boundaries = new TextBoundaries();

		for (int shift = -1; shift <= 1; ++shift) {
			s1.boundaries.left = 0;
			s1.boundaries.right = segments[0].length() + shift;
			s2.boundaries.left = s1.boundaries.right;
			s2.boundaries.right = segments[1].length() + segments[0].length();

			check(new Sentence[]{
			        s1, s2
			}, segments, null);
		}
	}

	@Test
	public void withWords() {
		String[] segments = new String[]{
		        "one segment", "two segments"
		};

		Sentence s = new Sentence();
		s.boundaries = new TextBoundaries();
		s.boundaries.left = 1;
		s.boundaries.right = segments[0].length() + segments[1].length();

		for (int shift1 = 0; shift1 <= 1; ++shift1) {
			for (int shift2 = 0; shift2 <= 1; ++shift2) {
				TextBoundaries w1 = new TextBoundaries();
				w1.left = s.boundaries.left + 1;
				w1.right = w1.left + 1 + shift1;

				TextBoundaries w2 = new TextBoundaries();
				w2.left = w1.right + shift2;
				w2.right = w2.left + (1 + shift1) * (1 + shift2);

				s.words = Arrays.asList(new TextBoundaries[]{
				        w1, w2
				});
				check(new Sentence[]{
					s
				}, segments, null);
			}
		}

	}
}
