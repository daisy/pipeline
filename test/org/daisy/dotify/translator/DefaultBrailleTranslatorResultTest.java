package org.daisy.dotify.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.api.translator.UnsupportedMetricException;
import org.daisy.dotify.common.text.BreakPointHandler;
import org.junit.Test;

public class DefaultBrailleTranslatorResultTest {
	private final static String INPUT_STR1 = "aaaaaaaaaaa bbb";
	private final static String DUMMY_METRIC = "dummy-metric";
	
	@Test
	public void testSupportsMetrics_01() throws TranslationException {
		//setup
		BrailleTranslatorResult btr = newTranslatorResult(INPUT_STR1);
		//test
		assertTrue(btr.supportsMetric(BrailleTranslatorResult.METRIC_FORCED_BREAK));
	}
	
	@Test
	public void testSupportsMetrics_02() throws TranslationException {
		//setup
		BrailleTranslatorResult btr = newTranslatorResult(INPUT_STR1);
		//test
		assertFalse(btr.supportsMetric(DUMMY_METRIC));
	}
	
	@Test
	public void testMetricForcedBreak_01() throws TranslationException {
		//setup
		BrailleTranslatorResult btr = newTranslatorResult(INPUT_STR1);
		//test
		assertEquals(0, btr.getMetric(BrailleTranslatorResult.METRIC_FORCED_BREAK), 0);
		btr.nextTranslatedRow(5, true);
		assertEquals(1, btr.getMetric(BrailleTranslatorResult.METRIC_FORCED_BREAK), 0);
		btr.nextTranslatedRow(6, true);
		assertEquals(1, btr.getMetric(BrailleTranslatorResult.METRIC_FORCED_BREAK), 0);
	}
	
	@Test(expected=UnsupportedMetricException.class)
	public void testUnsupportedMetric_01() throws TranslationException {
		//setup
		BrailleTranslatorResult btr = newTranslatorResult(INPUT_STR1);
		//test
		btr.getMetric(DUMMY_METRIC);
	}

	private static BrailleTranslatorResult newTranslatorResult(String str) {
		return new DefaultBrailleTranslatorResult(new BreakPointHandler(str), null);
	}

}
