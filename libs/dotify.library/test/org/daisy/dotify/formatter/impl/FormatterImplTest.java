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
    public void testTwoBlocksWithoutExpression() throws TranslatorConfigurationException {
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
    public void testTwoBlocksWithDisplayWhenTrue() throws TranslatorConfigurationException {
        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
                "true",
                ExpressionFactoryMaker.newInstance().getFactory()
        );

        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            BlockProperties bb = new BlockProperties.Builder()
                    .displayWhen(condition)
                    .keep(FormattingTypes.Keep.PAGE)
                    .build();
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
    public void testTwoBlocksWithDisplayWhenFalse() throws TranslatorConfigurationException {
        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
                "false",
                ExpressionFactoryMaker.newInstance().getFactory()
        );

        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            BlockProperties bb = new BlockProperties.Builder()
                    .displayWhen(condition)
                    .keep(FormattingTypes.Keep.PAGE)
                    .build();
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
    public void testTwoBlocksWithDisplayWhenExpressionEvaluateToTrue() throws TranslatorConfigurationException {
        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
                "(= 1 1)",
                ExpressionFactoryMaker.newInstance().getFactory()
        );

        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            BlockProperties bb = new BlockProperties.Builder()
                    .displayWhen(condition)
                    .keep(FormattingTypes.Keep.PAGE)
                    .build();
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
    public void testTwoBlocksWithDisplayWhenExpressionEvaluateToFalse() throws TranslatorConfigurationException {
        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
            "(= 0 1)",
            ExpressionFactoryMaker.newInstance().getFactory()
        );
        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            BlockProperties bb = new BlockProperties.Builder()
                    .displayWhen(condition)
                    .keep(FormattingTypes.Keep.PAGE)
                    .build();
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
    public void testTwoBlocksWithDisplayWhenExpressionStartedPageNumberIsOne() throws TranslatorConfigurationException {
        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
                "(= $started-page-number 1)",
                ExpressionFactoryMaker.newInstance().getFactory(),
                OBFLVariable.STARTED_PAGE_NUMBER
        );
        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            BlockProperties bb = new BlockProperties.Builder()
                    .displayWhen(condition)
                    .keep(FormattingTypes.Keep.PAGE)
                    .build();
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
     * This test is now ignored because the code that handled changing the value of the top of the page
     * was changed and makes this test fail. The code we used changed the value when we had written the
     * first row group to the page which was deemed to be the incorrect solution to this problem and
     * therefore reverted. We will keep this test here for future reference if we implement this feature.
     *
     * @throws TranslatorConfigurationException
     */
    @Test
    @Ignore
    public void testTwoBlocksWithDisplayWhenAsTrue() throws TranslatorConfigurationException {
        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
                "$starts-at-top-of-page",
                ExpressionFactoryMaker.newInstance().getFactory(),
                OBFLVariable.STARTS_AT_TOP_OF_PAGE
        );
        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            BlockProperties bb = new BlockProperties.Builder()
                    .displayWhen(condition)
                    .keep(FormattingTypes.Keep.PAGE)
                    .build();
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
    public void testTwoBlocksWithDisplayWhenExpressionStartsAtTopOfPage() throws TranslatorConfigurationException {
        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
                "(! $starts-at-top-of-page)",
                ExpressionFactoryMaker.newInstance().getFactory(),
                OBFLVariable.STARTS_AT_TOP_OF_PAGE
        );
        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            BlockProperties bb = new BlockProperties.Builder()
                    .displayWhen(condition)
                    .keep(FormattingTypes.Keep.PAGE)
                    .build();
            BlockProperties nb = new BlockProperties.Builder().build();

            f.startBlock(bb);
            f.addChars("Testing1", tp);
            f.endBlock();
            f.startBlock(nb);
            f.addChars("Testing2", tp);
            f.endBlock();
            return null;
        });

        assertEquals("Testing2\n", res);
    }

    @Test
    public void testTwoBlocksWithDisplayWhenExpressionStartsAtTopOfPageMultiPage()
        throws TranslatorConfigurationException {

        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
                "(! $starts-at-top-of-page)",
                ExpressionFactoryMaker.newInstance().getFactory(),
                OBFLVariable.STARTS_AT_TOP_OF_PAGE
        );
        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            BlockProperties bb = new BlockProperties.Builder()
                    .displayWhen(condition)
                    .keep(FormattingTypes.Keep.PAGE)
                    .build();
            BlockProperties nb = new BlockProperties.Builder().build();

            f.startBlock(bb);
            f.addChars("Testing1", tp);
            f.endBlock();
            f.startBlock(nb);
            f.addChars("Testing2", tp);
            f.endBlock();

            for (int i = 0; i < 18; i++) {
                f.startBlock(nb);
                f.addChars(".", tp);
                f.endBlock();
            }

            f.startBlock(bb);
            f.addChars("Testing1", tp);
            f.endBlock();
            f.startBlock(bb);
            f.addChars("Testing2", tp);
            f.endBlock();
            f.startBlock(nb);
            f.addChars("Testing3", tp);
            f.endBlock();

            return null;
        });

        assertEquals("Testing2\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\nTesting2\nTesting3\n", res);
    }

    @Test
    public void testBlockFlowingOverTwoPages()
            throws TranslatorConfigurationException {

        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
                "(! $starts-at-top-of-page)",
                ExpressionFactoryMaker.newInstance().getFactory(),
                OBFLVariable.STARTS_AT_TOP_OF_PAGE
        );
        String res = testingFormatter((f1) -> {
            FormatterSequence f = f1.newSequence(new SequenceProperties.Builder("main").build());
            BlockProperties bb = new BlockProperties.Builder()
                    .displayWhen(condition)
                    .keep(FormattingTypes.Keep.PAGE)
                    .build();
            BlockProperties nb = new BlockProperties.Builder().build();

            f.startBlock(bb);
            f.addChars("Testing1", tp);
            f.endBlock();
            f.startBlock(nb);
            f.addChars("Testing2", tp);
            f.endBlock();

            for (int i = 0; i < 17; i++) {
                f.startBlock(nb);
                f.addChars(".", tp);
                f.endBlock();
            }

            f.startBlock(bb);
            f.addChars("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus facilisis elit id " +
                    "tellus lacinia fermentum. In sed arcu at eros scelerisque elementum quis ac velit.", tp);
            f.endBlock();
            f.startBlock(bb);
            f.addChars("Testing2", tp);
            f.endBlock();
            f.startBlock(nb);
            f.addChars("Testing3", tp);
            f.endBlock();

            return null;
        });

        assertEquals("Testing2\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n.\n" +
                "elit.⠀Vivamus⠀facilisis⠀elit⠀id⠀tellus⠀lacinia\n" +
                "fermentum.⠀In⠀sed⠀arcu⠀at⠀eros⠀scelerisque\n" +
                "elementum⠀quis⠀ac⠀velit.\n" +
                "Testing2\nTesting3\n", res);
    }


    @Test
    public void testBlocksWithMarkers()
            throws TranslatorConfigurationException {

        String loc = "und";

        TextProperties tp = new TextProperties.Builder(loc).hyphenate(false).build();
        final OBFLCondition condition = new OBFLCondition(
                "(! $starts-at-top-of-page)",
                ExpressionFactoryMaker.newInstance().getFactory(),
                OBFLVariable.STARTS_AT_TOP_OF_PAGE
        );

        BlockProperties bb = new BlockProperties.Builder()
                .displayWhen(condition)
                .keep(FormattingTypes.Keep.PAGE)
                .build();
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

        assertEquals("1⠤\n\nTesting2\n2⠤\nTesting2\n", res);
    }
}
