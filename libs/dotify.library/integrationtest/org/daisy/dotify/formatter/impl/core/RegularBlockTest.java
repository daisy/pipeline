package org.daisy.dotify.formatter.impl.core;

import org.daisy.dotify.api.formatter.BlockProperties;
import org.daisy.dotify.api.formatter.Context;
import org.daisy.dotify.api.formatter.DynamicContent;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.formatter.TextProperties;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMakerService;
import org.daisy.dotify.api.translator.TextAttribute;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.common.text.IdentityFilter;
import org.daisy.dotify.formatter.impl.row.AbstractBlockContentManager;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.translator.DefaultBrailleFilter;
import org.daisy.dotify.translator.DefaultMarkerProcessor;
import org.daisy.dotify.translator.Marker;
import org.daisy.dotify.translator.SimpleBrailleTranslator;
import org.daisy.dotify.translator.impl.DefaultBrailleFinalizer;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * TODO: Write java doc.
 */
public class RegularBlockTest {

    @Test
    public void testConnectedStyles() throws TranslatorConfigurationException {
        String loc = "sv-SE";
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
                .addDictionary(
                    "em",
                    (String str, TextAttribute attributes) -> new Marker("1>", "<1")
                )
                .addDictionary(
                    "strong",
                    (String str, TextAttribute attributes) -> new Marker("2>", "<2")
                )
                .build();

        SimpleBrailleTranslator trr = new SimpleBrailleTranslator(
                new DefaultBrailleFilter(new IdentityFilter(), loc, mp, null),
                new DefaultBrailleFinalizer(), mode);
        BrailleTranslatorFactoryMakerService sr = Mockito.mock(BrailleTranslatorFactoryMakerService.class);
        Mockito.when(sr.newTranslator(loc, mode)).thenReturn(trr);

        FormatterContext fc = new FormatterContext(
                sr,
                null,
                new FormatterConfiguration.Builder(loc, mode).build());

        FormatterCoreImpl f = new FormatterCoreImpl(fc);
        f.startBlock(new BlockProperties.Builder().build());
        f.startStyle("em");
        f.addChars("a", tp);
        f.startStyle("strong");
        f.insertEvaluate(exp, tp);
        f.endStyle();
        f.addChars("c", tp);
        f.endStyle();
        f.endBlock();
        List<Block> b = f.getBlocks(null, null, null);
        Block bl = b.get(0);
        AbstractBlockContentManager bcm = bl.getBlockContentManager(
            new BlockContext.Builder(
                new DefaultContext.Builder(new CrossReferenceHandler()).build()
            ).flowWidth(30).formatterContext(fc).build()
        );
        StringBuilder sb = new StringBuilder();
        while (bcm.hasNext()) {
            bcm.getNext().ifPresent(v -> sb.append(v.getChars()));
        }
        assertEquals("1>a2>b<2c<1", sb.toString());
    }
}
