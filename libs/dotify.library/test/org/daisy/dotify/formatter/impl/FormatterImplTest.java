package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.api.formatter.BlockProperties;
import org.daisy.dotify.api.formatter.Context;
import org.daisy.dotify.api.formatter.DynamicContent;
import org.daisy.dotify.api.formatter.Field;
import org.daisy.dotify.api.formatter.FieldList;
import org.daisy.dotify.api.formatter.Formatter;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.formatter.FormatterSequence;
import org.daisy.dotify.api.formatter.FormattingTypes;
import org.daisy.dotify.api.formatter.LayoutMasterProperties;
import org.daisy.dotify.api.formatter.MarkerReference;
import org.daisy.dotify.api.formatter.MarkerReferenceField;
import org.daisy.dotify.api.formatter.PageTemplateBuilder;
import org.daisy.dotify.api.formatter.SequenceProperties;
import org.daisy.dotify.api.formatter.TextProperties;
import org.daisy.dotify.api.obfl.ExpressionFactoryMaker;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMakerService;
import org.daisy.dotify.api.translator.TextAttribute;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.api.writer.MetaDataItem;
import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.api.writer.PagedMediaWriterException;
import org.daisy.dotify.api.writer.Row;
import org.daisy.dotify.api.writer.SectionProperties;
import org.daisy.dotify.common.text.IdentityFilter;
import org.daisy.dotify.formatter.impl.obfl.OBFLCondition;
import org.daisy.dotify.formatter.impl.obfl.OBFLVariable;
import org.daisy.dotify.formatter.impl.row.RowImpl;
import org.daisy.dotify.translator.DefaultBrailleFilter;
import org.daisy.dotify.translator.DefaultMarkerProcessor;
import org.daisy.dotify.translator.Marker;
import org.daisy.dotify.translator.SimpleBrailleTranslator;
import org.daisy.dotify.translator.impl.DefaultBrailleFinalizer;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class FormatterImplTest {

    public String testingFormatter(Function<Formatter, Object> fi) throws TranslatorConfigurationException {
        String loc = "und";
        String mode = "bypass";

        DefaultMarkerProcessor mp = new DefaultMarkerProcessor.Builder()
                .addDictionary("em", (String str, TextAttribute attributes) -> new Marker("1>", "<1"))
                .addDictionary("strong", (String str, TextAttribute attributes) -> new Marker("2>", "<2"))
                .build();

        SimpleBrailleTranslator trr = new SimpleBrailleTranslator(
                new DefaultBrailleFilter(new IdentityFilter(), loc, mp, null),
                new DefaultBrailleFinalizer(), mode);
        BrailleTranslatorFactoryMakerService sr = Mockito.mock(BrailleTranslatorFactoryMakerService.class);
        Mockito.when(sr.newTranslator(loc, mode)).thenReturn(trr);

        final Formatter f1 = new FormatterImpl(
                sr,
                null,
                new FormatterConfiguration.Builder(loc, mode).hyphenate(false).build());
        f1.newLayoutMaster("main", new LayoutMasterProperties.Builder(50, 20).build());

        fi.apply(f1);

        StringBuilder sb = new StringBuilder();
        f1.write(new PagedMediaWriter() {
            @Override
            public void close() throws IOException {
            }

            @Override
            public void prepare(List<MetaDataItem> meta) {
            }

            @Override
            public void open(OutputStream os) throws PagedMediaWriterException {
            }

            @Override
            public void newVolume(SectionProperties props) {
            }

            @Override
            public void newSection(SectionProperties props) {
            }

            @Override
            public void newRow(Row row) {
                if (row instanceof RowImpl && ((RowImpl) row).isInvisible()) {
                    return;
                }
                sb.append(row.getChars() + "\n");
            }

            @Override
            public void newRow() {
            }

            @Override
            public void newPage() { }
        });
        return sb.toString();
    }

    @Test
    public void testConnectedStyles() throws TranslatorConfigurationException {
        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        DynamicContent exp = new DynamicContent() {
            String str = "b";

            @Override
            public String render(Context context) {
                return str;
            }

            @Override
            public String render() {
                return render(new Context() {
                });
            }
        };

        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            f.startBlock(new BlockProperties.Builder().build());
            f.startStyle("em");
            f.addChars("a", tp);
            f.startStyle("strong");
            f.insertEvaluate(exp, tp);
            f.endStyle();
            f.addChars("c", tp);
            f.endStyle();
            f.endBlock();
            return null;
        });

        assertEquals("1>a2>b<2c<1\n", res);
    }


    @Test
    public void testBlocksWithoutDisplayWhen() throws TranslatorConfigurationException {
        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();

        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            f.startBlock(new BlockProperties.Builder().build());
            f.addChars("Testing1", tp);
            f.endBlock();
            f.startBlock(new BlockProperties.Builder().build());
            f.addChars("Testing2", tp);
            f.endBlock();
            return null;
        });

        assertEquals("Testing1\nTesting2\n", res);
    }

    @Test
    public void testDisplayWhenTrue() throws TranslatorConfigurationException {
        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
                "true",
                ExpressionFactoryMaker.newInstance().getFactory()
        );

        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            BlockProperties bb = new BlockProperties.Builder().displayWhen(condition).build();
            f.startBlock(bb);
            f.addChars("Testing1", tp);
            f.endBlock();
            f.startBlock(new BlockProperties.Builder().build());
            f.addChars("Testing2", tp);
            f.endBlock();
            return null;
        });

        assertEquals("Testing1\nTesting2\n", res);
    }

    @Test
    public void testDisplayWhenFalse() throws TranslatorConfigurationException {
        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
                "false",
                ExpressionFactoryMaker.newInstance().getFactory()
        );

        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            BlockProperties bb = new BlockProperties.Builder().displayWhen(condition).build();
            f.startBlock(bb);
            f.addChars("Testing1", tp);
            f.endBlock();
            f.startBlock(new BlockProperties.Builder().build());
            f.addChars("Testing2", tp);
            f.endBlock();
            return null;
        });

        assertEquals("Testing2\n", res);
    }

    @Test
    public void testDisplayWhenExpressionEvaluateToTrue() throws TranslatorConfigurationException {
        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
                "(= 1 1)",
                ExpressionFactoryMaker.newInstance().getFactory()
        );

        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            BlockProperties bb = new BlockProperties.Builder().displayWhen(condition).build();
            f.startBlock(bb);
            f.addChars("Testing1", tp);
            f.endBlock();
            f.startBlock(new BlockProperties.Builder().build());
            f.addChars("Testing2", tp);
            f.endBlock();
            return null;
        });

        assertEquals("Testing1\nTesting2\n", res);
    }

    @Test
    public void testDisplayWhenExpressionEvaluateToFalse() throws TranslatorConfigurationException {
        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
            "(= 0 1)",
            ExpressionFactoryMaker.newInstance().getFactory()
        );
        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            BlockProperties bb = new BlockProperties.Builder().displayWhen(condition).build();
            f.startBlock(bb);
            f.addChars("Testing1", tp);
            f.endBlock();
            f.startBlock(new BlockProperties.Builder().build());
            f.addChars("Testing2", tp);
            f.endBlock();
            return null;
        });

        assertEquals("Testing2\n", res);
    }

    @Test
    public void testDisplayWhenStartedPageNumberIsOne() throws TranslatorConfigurationException {
        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
                "(= $started-page-number 1)",
                ExpressionFactoryMaker.newInstance().getFactory(),
                OBFLVariable.STARTED_PAGE_NUMBER
        );
        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            BlockProperties bb = new BlockProperties.Builder().displayWhen(condition).build();
            f.startBlock(bb);
            f.addChars("Testing1", tp);
            f.endBlock();
            f.startBlock(new BlockProperties.Builder().build());
            f.addChars("Testing2", tp);
            f.endBlock();
            return null;
        });

        assertEquals("Testing2\n", res);
    }


    /**
     * This test is ignored because the current implementation makes certain assumptions which are
     * not met when display-when="$starts-at-top-of-page". The OBFL parser enforces that the
     * assumptions are met. We keep this test here for future reference if we implement this
     * feature.
     *
     * @throws TranslatorConfigurationException
     */
    @Test
    @Ignore
    public void testDisplayWhenStartsAtTopOfPage() throws TranslatorConfigurationException {
        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
                "$starts-at-top-of-page",
                ExpressionFactoryMaker.newInstance().getFactory(),
                OBFLVariable.STARTS_AT_TOP_OF_PAGE
        );
        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            BlockProperties bb = new BlockProperties.Builder().displayWhen(condition).build();
            f.startBlock(bb);
            f.addChars("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus facilisis elit id " +
                    "tellus lacinia fermentum. In sed arcu at eros scelerisque elementum quis ac velit.", tp);
            f.endBlock();
            return null;
        });

        assertEquals("Lorem⠀ipsum⠀dolor⠀sit⠀amet,⠀consectetur⠀adipiscing\n" +
                "elit.⠀Vivamus⠀facilisis⠀elit⠀id⠀tellus⠀lacinia\n" +
                "fermentum.⠀In⠀sed⠀arcu⠀at⠀eros⠀scelerisque\n" +
                "elementum⠀quis⠀ac⠀velit.\n", res);
    }

    @Test
    public void testDisplayWhenDoesNotStartAtTopOfPage() throws TranslatorConfigurationException {

        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
                "(! $starts-at-top-of-page)",
                ExpressionFactoryMaker.newInstance().getFactory(),
                OBFLVariable.STARTS_AT_TOP_OF_PAGE
        );
        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            BlockProperties bb = new BlockProperties.Builder().displayWhen(condition).build();
            BlockProperties kb = new BlockProperties.Builder()
                    .displayWhen(condition)
                    .keep(FormattingTypes.Keep.PAGE)
                    .build();
            BlockProperties nb = new BlockProperties.Builder().build();

            // The first block is not rendered because of the "display-when" condition which
            // evaluates to false at the top of the page.
            f.startBlock(bb);
            f.addChars("Testing1", tp);
            f.endBlock();
            // This (unconditional) block becomes is the first row of the page
            f.startBlock(nb);
            f.addChars("Testing2", tp);
            f.endBlock();
            // This block with "display-when" condition is rendered and becomes is the third row of the page
            f.startBlock(bb);
            f.addChars("Testing3", tp);
            f.endBlock();
            // We render another 17 rows so that we end up on the 19th row of the page (of 20 rows).
            for (int i = 0; i < 17; i++) {
                f.startBlock(nb);
                f.addChars(".", tp);
                f.endBlock();
            }
            // The next block takes up 4 rows and is split over two pages. It is rendered in its entirety, also
            // the part at the top of the next page, even though it has a "display-when" condition.
            f.startBlock(bb);
            f.addChars("Testing4", tp);
            f.newLine();
            f.addChars("Testing4", tp);
            f.newLine();
            f.addChars("Testing4", tp);
            f.newLine();
            f.addChars("Testing4", tp);
            f.endBlock();
            // We render another 17 rows so that we end up on the 19th (last) row of the page.
            for (int i = 0; i < 17; i++) {
                f.startBlock(nb);
                f.addChars(".", tp);
                f.endBlock();
            }
            // The next block takes up 4 rows so would normally be split over two pages, but thanks
            // to the "keep" property it is moved to the second page, where it is not rendered
            // because of the "display-when" condition.
            f.startBlock(kb);
            f.addChars("Testing4", tp);
            f.newLine();
            f.addChars("Testing4", tp);
            f.newLine();
            f.addChars("Testing4", tp);
            f.newLine();
            f.addChars("Testing4", tp);
            f.endBlock();
            // The next block is also not rendered because it also has the "display-when" condition
            // and we are still at the top of the page.
            f.startBlock(bb);
            f.addChars("Testing2", tp);
            f.endBlock();
            // This block becomes is the first row of the second page
            f.startBlock(nb);
            f.addChars("Testing3", tp);
            f.endBlock();

            return null;
        });

        assertEquals("Testing2\nTesting3\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\nTesting4\nTesting4\n" +
                     "Testing4\nTesting4\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n" +
                     "Testing3\n",
                     res);
    }


    @Test
    public void testBlockWithMarkersAndDisplayWhenDoesNotStartAtTopOfPage()
            throws TranslatorConfigurationException {

        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
                "(! $starts-at-top-of-page)",
                ExpressionFactoryMaker.newInstance().getFactory(),
                OBFLVariable.STARTS_AT_TOP_OF_PAGE
        );

        BlockProperties bb = new BlockProperties.Builder().displayWhen(condition).build();
        BlockProperties nb = new BlockProperties.Builder().build();

        String res = testingFormatter((f1) -> {
            final OBFLCondition tempCondition = new OBFLCondition(
                    "true",
                    ExpressionFactoryMaker.newInstance().getFactory()
            );

            PageTemplateBuilder ptb = f1.newLayoutMaster("test", new LayoutMasterProperties.Builder(10, 2).build())
            .newTemplate(tempCondition);

            List<Field> list = new ArrayList<>();
            list.add(
                new MarkerReferenceField("pagenum-turn",
                MarkerReference.MarkerSearchDirection.FORWARD,
                MarkerReference.MarkerSearchScope.PAGE_CONTENT)
            );
            ptb.addToHeader(new FieldList.Builder(list).build());


            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("test").build());
            f.startBlock(bb);
            f.insertMarker(new org.daisy.dotify.api.formatter.Marker("pagenum", "1"));
            f.insertMarker(new org.daisy.dotify.api.formatter.Marker("pagenum-turn", "1-"));
            f.addChars("Testing1", tp);
            f.endBlock();
            f.startBlock(nb);
            f.addChars("Testing2", tp);
            f.endBlock();
            f.startBlock(nb);
            f.insertMarker(new org.daisy.dotify.api.formatter.Marker("pagenum", "2"));
            f.insertMarker(new org.daisy.dotify.api.formatter.Marker("pagenum-turn", "2-"));
            f.addChars("Testing2", tp);
            f.endBlock();

            return null;
        });

        assertEquals("1⠤\nTesting2\n2⠤\nTesting2\n", res);
    }
}
