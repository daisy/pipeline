package org.daisy.dotify.formatter.impl.row;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.formatter.TextProperties;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMaker;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMakerService;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.common.text.IdentityFilter;
import org.daisy.dotify.formatter.impl.core.FormatterContext;
import org.daisy.dotify.formatter.impl.segment.AnchorSegment;
import org.daisy.dotify.formatter.impl.segment.Segment;
import org.daisy.dotify.formatter.impl.segment.Style;
import org.daisy.dotify.formatter.impl.segment.TextSegment;
import org.daisy.dotify.translator.DefaultBrailleFilter;
import org.daisy.dotify.translator.DefaultMarkerProcessor;
import org.daisy.dotify.translator.Marker;
import org.daisy.dotify.translator.SimpleBrailleTranslator;
import org.daisy.dotify.translator.impl.DefaultBrailleFinalizer;
import org.junit.Test;
import org.mockito.Mockito;

@SuppressWarnings("javadoc")
public class SegmentProcessorTest {
	final String loc = "und";
	final String mode = "bypass";
	final RowDataProperties rdp = new RowDataProperties.Builder().build();
	final FormatterConfiguration conf = new FormatterConfiguration.Builder(loc, mode).build();
	final DefaultMarkerProcessor mp = new DefaultMarkerProcessor.Builder()
			.addDictionary("em", (str, ta)->new Marker("x", "y"))
			.addDictionary("strong", (str, ta)->new Marker("6", "7"))
			.build();
	final SimpleBrailleTranslator trr = new SimpleBrailleTranslator(
			new DefaultBrailleFilter(new IdentityFilter(), loc, mp, null),
			new DefaultBrailleFinalizer(), mode);

	@Test
	public void testTextNoProcessor() {
		Segment t;
		TextProperties tp = new TextProperties.Builder("und").hyphenate(false).build();
		List<Segment> segments = new ArrayList<>();
		List<Segment> expecteds = new ArrayList<>();
		t = new TextSegment("abc", tp, true);
		segments.add(t);
		expecteds.add(t);
		Style s = new Style("em");
		segments.add(s);
		t = new TextSegment("def", tp, true);
		s.add(t);
		expecteds.add(t);
		t = new TextSegment("ghi", tp, true);
		segments.add(t);
		expecteds.add(t);
		FormatterContext fc = new FormatterContext(BrailleTranslatorFactoryMaker.newInstance(), null, conf);

		SegmentProcessor sp = new SegmentProcessor("", segments, 100, null, null, 100, rdp.getMargins(), fc, rdp);
		System.out.println(sp.getNext(LineProperties.DEFAULT).get().getChars());
	}

	@Test
	public void testTextWithProcessor_01() throws TranslatorConfigurationException {
		Segment t;
		TextProperties tp = new TextProperties.Builder("und").hyphenate(false).build();
		List<Segment> segments = new ArrayList<>();		
		t = new TextSegment("abc", tp, true);
		segments.add(t);
		Style s = new Style("em");
		segments.add(s);
		t = new TextSegment("def", tp, true);
		s.add(t);
		t = new TextSegment("ghi", tp, true);
		segments.add(t);
		

		BrailleTranslatorFactoryMakerService sr = Mockito.mock(BrailleTranslatorFactoryMakerService.class);
		Mockito.when(sr.newTranslator(loc, mode)).thenReturn(trr);
		FormatterContext fc = new FormatterContext(sr, null, conf);
		SegmentProcessor sp = new SegmentProcessor("", segments, 100, null, null, 100, rdp.getMargins(), fc, rdp);
		assertEquals("abcxdefyghi", sp.getNext(LineProperties.DEFAULT).get().getChars());
	}

	@Test
	public void testTextWithProcessor_02() throws TranslatorConfigurationException {

		Segment t;
		TextProperties tp = new TextProperties.Builder("und").hyphenate(false).build();
		List<Segment> segments = new ArrayList<>();
		List<Segment> expecteds = new ArrayList<>();
		t = new TextSegment("abc", tp, true);
		segments.add(t);
		expecteds.add(new TextSegment("abcxdefy", tp, true));
		Style s = new Style("em");
		segments.add(s);
		t = new TextSegment("def", tp, true);
		s.add(t);
		t = new AnchorSegment("ref-id");
		s.add(t);
		expecteds.add(t);

		BrailleTranslatorFactoryMakerService sr = Mockito.mock(BrailleTranslatorFactoryMakerService.class);
		Mockito.when(sr.newTranslator(loc, mode)).thenReturn(trr);
		FormatterContext fc = new FormatterContext(sr, null, conf);
		SegmentProcessor sp = new SegmentProcessor("", segments, 100, null, null, 100, rdp.getMargins(), fc, rdp);
		assertEquals("abcxdefy", sp.getNext(LineProperties.DEFAULT).get().getChars());
	}
	/*
	@Test
	public void testDynamicWithProcessor_01() {
		Context context = Mockito.mock(Context.class);
		Segment t;
		TextProperties tp = new TextProperties.Builder("und").build();
		List<Segment> segments = new ArrayList<>();
		List<Segment> expecteds = new ArrayList<>();
		t = new TextSegment("abc", tp);
		segments.add(t);
		expecteds.add(t);
		Style s = new Style("em");
		segments.add(s);
		DynamicContent dc = Mockito.mock(DynamicContent.class);
		t = new Evaluate(dc, tp);
		s.add(t);
		expecteds.add(new Evaluate(dc, tp, new MarkerValue("x", "y")));
		t = new TextSegment("ghi", tp);
		segments.add(t);
		expecteds.add(t);
		MarkerProcessor mp = new DefaultMarkerProcessor.Builder().addDictionary("em", (str, ta)->new Marker("x", "y")).build();
		List<Segment> actuals = SegmentProcessor.processStyles(segments, mp, context);
		assertEquals(expecteds, actuals);
	}
	
	@Test
	public void testDynamicWithProcessor_02() {
		Context context = Mockito.mock(Context.class);
		Segment t;
		TextProperties tp = new TextProperties.Builder("und").build();
		List<Segment> segments = new ArrayList<>();
		List<Segment> expecteds = new ArrayList<>();
		t = new TextSegment("abc", tp);
		segments.add(t);
		expecteds.add(t);
		Style s = new Style("em");
		Style s1 = new Style("strong");
		segments.add(s);
		s.add(s1);
		DynamicContent dc = Mockito.mock(DynamicContent.class);
		t = new Evaluate(dc, tp);
		s1.add(t);
		expecteds.add(new Evaluate(dc, tp, new MarkerValue("x6", "7")));
		t = new TextSegment("ghi", tp);
		s.add(t);
		expecteds.add(new TextSegment("ghiy", tp));

		List<Segment> actuals = SegmentProcessor.processStyles(segments, mp, context);
		assertEquals(expecteds, actuals);
	}
	
	@Test
	public void testDynamicWithProcessor_03() {
		Context context = Mockito.mock(Context.class);
		Segment t;
		TextProperties tp = new TextProperties.Builder("und").build();
		List<Segment> segments = new ArrayList<>();
		List<Segment> expecteds = new ArrayList<>();
		t = new TextSegment("abc", tp);
		segments.add(t);
		expecteds.add(t);
		Style s = new Style("em");
		Style s1 = new Style("strong");
		segments.add(s);
		s.add(s1);
		DynamicContent dc = Mockito.mock(DynamicContent.class);
		t = new PageNumberReference("id", NumeralStyle.ALPHA);
		s1.add(t);
		expecteds.add(new PageNumberReference("id", NumeralStyle.ALPHA, new MarkerValue("x6", "7")));
		t = new TextSegment("ghi", tp);
		s.add(t);
		expecteds.add(new TextSegment("ghiy", tp));
		MarkerProcessor mp = new DefaultMarkerProcessor.Builder()
				.addDictionary("em", (str, ta)->new Marker("x", "y"))
				.addDictionary("strong", (str, ta)->new Marker("6", "7"))
				.build();
		List<Segment> actuals = SegmentProcessor.processStyles(segments, mp, context);
		assertEquals(expecteds, actuals);
	}
*/
}
