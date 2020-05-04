package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.api.formatter.BlockProperties;
import org.daisy.dotify.api.formatter.Context;
import org.daisy.dotify.api.formatter.DynamicContent;
import org.daisy.dotify.api.formatter.Formatter;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.formatter.FormatterSequence;
import org.daisy.dotify.api.formatter.LayoutMasterProperties;
import org.daisy.dotify.api.formatter.SequenceProperties;
import org.daisy.dotify.api.formatter.TextProperties;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMakerService;
import org.daisy.dotify.api.translator.TextAttribute;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.api.writer.MetaDataItem;
import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.api.writer.PagedMediaWriterException;
import org.daisy.dotify.api.writer.Row;
import org.daisy.dotify.api.writer.SectionProperties;
import org.daisy.dotify.common.text.IdentityFilter;
import org.daisy.dotify.translator.DefaultBrailleFilter;
import org.daisy.dotify.translator.DefaultMarkerProcessor;
import org.daisy.dotify.translator.Marker;
import org.daisy.dotify.translator.SimpleBrailleTranslator;
import org.daisy.dotify.translator.impl.DefaultBrailleFinalizer;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class FormatterImplTest {

    @Test
    public void testConnectedStyles() throws TranslatorConfigurationException {
        String loc = "und";
        String mode = "bypass";
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

        DefaultMarkerProcessor mp = new DefaultMarkerProcessor.Builder()
                .addDictionary("em", (String str, TextAttribute attributes) -> new Marker("1>", "<1"))
                .addDictionary("strong", (String str, TextAttribute attributes) -> new Marker("2>", "<2"))
                .build();

        SimpleBrailleTranslator trr = new SimpleBrailleTranslator(
                new DefaultBrailleFilter(new IdentityFilter(), loc, mp, null),
                new DefaultBrailleFinalizer(), mode);
        BrailleTranslatorFactoryMakerService sr = Mockito.mock(BrailleTranslatorFactoryMakerService.class);
        Mockito.when(sr.newTranslator(loc, mode)).thenReturn(trr);

        Formatter f1 = new FormatterImpl(
                sr,
                null,
                new FormatterConfiguration.Builder(loc, mode).hyphenate(false).build());
        f1.newLayoutMaster("main", new LayoutMasterProperties.Builder(50, 20).build());

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
                sb.append(row.getChars());
            }

            @Override
            public void newRow() {
            }

            @Override
            public void newPage() {
            }
        });
        assertEquals("1>a2>b<2c<1", sb.toString());
    }

}
