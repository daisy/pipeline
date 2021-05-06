package org.daisy.dotify.formatter.impl.row;

import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.formatter.TextProperties;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMaker;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMakerService;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.common.text.IdentityFilter;
import org.daisy.dotify.formatter.impl.core.FormatterContext;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class SegmentProcessorTest {
    private static final Logger logger = Logger.getLogger(SegmentProcessorTest.class.getCanonicalName());

    final String loc = "und";
    final String mode = "bypass";
    final RowDataProperties rdp = new RowDataProperties.Builder().build();
    final FormatterConfiguration conf = new FormatterConfiguration.Builder(loc, mode).build();
    final DefaultMarkerProcessor mp = new DefaultMarkerProcessor.Builder()
            .addDictionary("em", (str, ta) -> new Marker("x", "y"))
            .addDictionary("strong", (str, ta) -> new Marker("6", "7"))
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

        SegmentProcessor sp = new SegmentProcessor(
            "",
            segments,
            100,
            new CrossReferenceHandler(),
            null,
            100,
            rdp.getMargins(),
            fc,
            rdp
        );
        logger.info(sp.getNext(LineProperties.DEFAULT).get().getChars());
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
        SegmentProcessor sp = new SegmentProcessor(
            "",
            segments,
            100,
            new CrossReferenceHandler(),
            null,
            100,
            rdp.getMargins(),
            fc,
            rdp
        );
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
        SegmentProcessor sp = new SegmentProcessor(
            "",
            segments,
            100,
            new CrossReferenceHandler(),
            null,
            100,
            rdp.getMargins(),
            fc,
            rdp
        );
        assertEquals("abcxdefy", sp.getNext(LineProperties.DEFAULT).get().getChars());
    }

}
