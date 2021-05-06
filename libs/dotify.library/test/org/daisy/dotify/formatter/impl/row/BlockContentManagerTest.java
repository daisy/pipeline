package org.daisy.dotify.formatter.impl.row;

import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.formatter.Leader;
import org.daisy.dotify.api.formatter.Position;
import org.daisy.dotify.api.formatter.TextProperties;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMaker;
import org.daisy.dotify.api.translator.TextBorderFactoryMaker;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.api.translator.TranslatorType;
import org.daisy.dotify.formatter.impl.core.FormatterContext;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.segment.LeaderSegment;
import org.daisy.dotify.formatter.impl.segment.NewLineSegment;
import org.daisy.dotify.formatter.impl.segment.Segment;
import org.daisy.dotify.formatter.impl.segment.TextSegment;
import org.junit.Test;

import java.util.Stack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class BlockContentManagerTest {

    @Test
    public void testHangingIndent() throws TranslatorConfigurationException {
        //setup
        FormatterContext c = new FormatterContext(
            BrailleTranslatorFactoryMaker.newInstance(),
            TextBorderFactoryMaker.newInstance(),
            FormatterConfiguration.with("sv-SE", TranslatorType.UNCONTRACTED.toString()).build()
        );
        Stack<Segment> segments = new Stack<>();
        for (int i = 0; i < 6; i++) {
            segments.push(
                new TextSegment("... ", new TextProperties.Builder("sv-SE").build())
            );
        }
        RowDataProperties rdp = new RowDataProperties.Builder().firstLineIndent(1).textIndent(3).build();
        CrossReferenceHandler refs = mock(CrossReferenceHandler.class);
        DefaultContext context = createContext();
        AbstractBlockContentManager m = new BlockContentManager(
            null,
            10,
            segments,
            rdp,
            refs,
            context,
            c
        );

        //test
        assertEquals("⠀⠄⠄⠄⠀⠄⠄⠄", m.getNext().get().getChars());
        assertEquals("⠀⠀⠀⠄⠄⠄⠀⠄⠄⠄", m.getNext().get().getChars());
        assertEquals("⠀⠀⠀⠄⠄⠄⠀⠄⠄⠄", m.getNext().get().getChars());
        assertFalse(m.hasNext());
    }

    @Test
    public void testLeader() throws TranslatorConfigurationException {
        //setup
        FormatterContext c = new FormatterContext(
            BrailleTranslatorFactoryMaker.newInstance(),
            TextBorderFactoryMaker.newInstance(),
            FormatterConfiguration.with("sv-SE", TranslatorType.UNCONTRACTED.toString()).build()
        );
        Stack<Segment> segments = new Stack<>();
        segments.push(new LeaderSegment(
            new Leader.Builder()
                .align(org.daisy.dotify.api.formatter.Leader.Alignment.RIGHT)
                .pattern(" ")
                .position(new Position(1.0, true))
                .build()
        ));
        segments.push(
            new TextSegment("...", new TextProperties.Builder("sv-SE").build())
        );

        RowDataProperties rdp = new RowDataProperties.Builder().firstLineIndent(1).textIndent(3).build();
        CrossReferenceHandler refs = mock(CrossReferenceHandler.class);
        DefaultContext context = createContext();
        AbstractBlockContentManager m = new BlockContentManager(null, 10, segments, rdp, refs, context, c);

        //test
        assertEquals("⠀⠀⠀⠀⠀⠀⠀⠄⠄⠄", m.getNext().get().getChars());
        assertFalse(m.hasNext());
    }

    @Test
    public void testNewLine() throws TranslatorConfigurationException {
        //setup
        FormatterContext c = new FormatterContext(
            BrailleTranslatorFactoryMaker.newInstance(),
            TextBorderFactoryMaker.newInstance(),
            FormatterConfiguration.with("sv-SE", TranslatorType.UNCONTRACTED.toString()).build()
        );
        Stack<Segment> segments = new Stack<>();
        segments.push(new TextSegment("... ... ...", new TextProperties.Builder("sv-SE").build()));
        segments.push(new NewLineSegment());
        segments.push(new TextSegment("...", new TextProperties.Builder("sv-SE").build()));
        segments.push(new NewLineSegment());
        segments.push(new TextSegment("...", new TextProperties.Builder("sv-SE").build()));

        RowDataProperties rdp = new RowDataProperties.Builder().firstLineIndent(1).textIndent(3).build();
        CrossReferenceHandler refs = mock(CrossReferenceHandler.class);
        DefaultContext context = createContext();
        AbstractBlockContentManager m = new BlockContentManager(null, 10, segments, rdp, refs, context, c);

        //test
        assertEquals("⠀⠄⠄⠄⠀⠄⠄⠄", m.getNext().get().getChars());
        RowImpl r = m.getNext().get();
        assertEquals("⠀⠀⠀⠄⠄⠄", r.getLeftMargin().getContent() + r.getChars());
        r = m.getNext().get();
        assertEquals("⠀⠀⠀⠄⠄⠄", r.getLeftMargin().getContent() + r.getChars());
        r = m.getNext().get();
        assertEquals("⠀⠀⠀⠄⠄⠄", r.getLeftMargin().getContent() + r.getChars());
        assertFalse(m.hasNext());
    }


    private static DefaultContext createContext() {
        CrossReferenceHandler crh = new CrossReferenceHandler();
        crh.setVolumeCount(1);
        return new DefaultContext.Builder(crh).currentVolume(1).build();
    }

}
