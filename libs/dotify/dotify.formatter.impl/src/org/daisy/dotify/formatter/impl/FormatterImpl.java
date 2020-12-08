package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.api.formatter.ContentCollection;
import org.daisy.dotify.api.formatter.Formatter;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.formatter.FormatterSequence;
import org.daisy.dotify.api.formatter.LayoutMasterBuilder;
import org.daisy.dotify.api.formatter.LayoutMasterProperties;
import org.daisy.dotify.api.formatter.SequenceProperties;
import org.daisy.dotify.api.formatter.TableOfContents;
import org.daisy.dotify.api.formatter.TransitionBuilder;
import org.daisy.dotify.api.formatter.VolumeTemplateBuilder;
import org.daisy.dotify.api.formatter.VolumeTemplateProperties;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;
import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.formatter.impl.common.Volume;
import org.daisy.dotify.formatter.impl.common.WriterHandler;
import org.daisy.dotify.formatter.impl.page.BlockSequence;
import org.daisy.dotify.formatter.impl.page.RestartPaginationException;
import org.daisy.dotify.formatter.impl.sheet.VolumeImpl;
import org.daisy.dotify.formatter.impl.volume.VolumeTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <p>Provides an implementation of the {@link Formatter} API.</p>
 *
 * <p>Is used for creating a paged document. Uses {@link
 * org.daisy.dotify.formatter.impl.VolumeProvider} to produce {@link
 * org.daisy.dotify.formatter.impl.common.Volume} objects from a list of {@link BlockSequence},
 * which are provided through {@link #newSequence(SequenceProperties)}, and populated through
 * methods like {@link BlockSequence#startBlock(BlockProperties)}, {@link
 * BlockSequence#addChars(CharSequence, TextProperties)}, etc. The resuling paged document is
 * collected through a {@link org.daisy.dotify.api.writer.PagedMediaWriter}.</p>
 *
 * @author Joel Håkansson
 */
class FormatterImpl implements Formatter {
    private static final int MAX_ITERATIONS = 200;

    private final Stack<VolumeTemplate> volumeTemplates;
    private final Logger logger;

    private boolean unopened;
    private final Stack<BlockSequence> blocks;

    private final LazyFormatterContext context;

    /**
     * Creates a new formatter.
     *
     * @param translatorFactory a braille translator factory maker service
     * @param tbf               a text border factory maker service
     * @param locale            a locale
     * @param mode              a braille mode
     */
    FormatterImpl(
        BrailleTranslatorFactoryMakerService translatorFactory,
        TextBorderFactoryMakerService tbf,
        String locale,
        String mode
    ) {
        this(translatorFactory, tbf, FormatterConfiguration.with(locale, mode).build());
    }

    /**
     * Creates a new formatter.
     *
     * @param translatorFactory a braille translator factory maker service
     * @param tbf               a text border factory maker service
     * @param config            the configuration
     */
    FormatterImpl(
        BrailleTranslatorFactoryMakerService translatorFactory,
        TextBorderFactoryMakerService tbf,
        FormatterConfiguration config
    ) {
        this.context = new LazyFormatterContext(translatorFactory, tbf, config);
        this.blocks = new Stack<>();
        this.unopened = true;
        this.volumeTemplates = new Stack<>();

        this.logger = Logger.getLogger(this.getClass().getCanonicalName());
    }


    @Override
    public FormatterConfiguration getConfiguration() {
        return context.getFormatterContext().getConfiguration();
    }

    @Override
    public void setConfiguration(FormatterConfiguration config) {
        //TODO: we require unopened at the moment due to limitations in the implementation
        if (!unopened) {
            throw new IllegalStateException("Configuration must happen before use.");
        }
        context.setConfiguration(config);
    }

    @Override
    public FormatterSequence newSequence(SequenceProperties p) {
        unopened = false;
        BlockSequence currentSequence = new BlockSequence(
            context.getFormatterContext(),
            p,
            context.getFormatterContext().getMasters().get(p.getMasterName())
        );
        blocks.push(currentSequence);
        return currentSequence;
    }

    @Override
    public LayoutMasterBuilder newLayoutMaster(String name,
                                               LayoutMasterProperties properties) {
        unopened = false;
        return context.getFormatterContext().newLayoutMaster(name, properties);
    }

    @Override
    public VolumeTemplateBuilder newVolumeTemplate(VolumeTemplateProperties props) {
        unopened = false;
        VolumeTemplate template = new VolumeTemplate(
            context.getFormatterContext(),
            props.getCondition(),
            props.getSplitterMax()
        );
        volumeTemplates.push(template);
        return template;
    }

    @Override
    public TableOfContents newToc(String tocName) {
        unopened = false;
        return context.getFormatterContext().newTableOfContents(tocName);
    }

    @Override
    public ContentCollection newCollection(String collectionId) {
        unopened = false;
        return context.getFormatterContext().newContentCollection(collectionId);
    }

    @Override
    public void write(PagedMediaWriter writer) {
        unopened = false;
        try (WriterHandler wh = new WriterHandler(writer)) {
            wh.write(getVolumes());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to close resource.", e);
        }
    }

    private Iterable<? extends Volume> getVolumes() {
        VolumeProvider volumeProvider = new VolumeProvider(blocks, volumeTemplates, context);

        ArrayList<VolumeImpl> ret;

        /*
         * Inside this loop a result is created. The volume provider does all the work, and this loop
         * simply controls the number of iterations and adds the content to the result. If
         * the volume provider indicates that something is wrong with its result, another iteration
         * is attempted. The reason that more than one iteration may be needed, is because of
         * references. For example, in order to produce a TOC, information about the volume and page
         * number for each item is typically requested before that page has been produced.
         * Therefore, these values are recorded on the first iteration and then used in the second
         * and so on. But even subsequent iterations may contain errors, due to different
         * constraints being activated when the dynamic text changes, which leads to different
         * volume breaks etc.
         *
         * The maximum number of iterations below is a balance between giving up when in fact a
         * solution could be found and pointless iterations when in fact no solution will ever be
         * found. When no solution is found, this is either because of an error in the input OBFL,
         * or because of a bug in the code. In other words, in theory this should never happen.
         *
         * Experience and a solid understanding of the algorithm should be involved when changing
         * this value permanently. It should therefore not be parameterized.  Having that said,
         * changing this value temporarily could be useful for debugging purposes.
         */
        for (int j = 1; j <= MAX_ITERATIONS; j++) {
            try {
                ret = new ArrayList<>();
                volumeProvider.prepare();
                while (volumeProvider.hasNext()) {
                    ret.add(volumeProvider.nextVolume());
                }

                if (volumeProvider.done()) {
                    //everything fits
                    return ret;
                }

            } catch (RestartPaginationException e) {
                // don't count this round, simply restart
                j--;
            }
        }
        throw new RuntimeException("Failed to complete volume division.");
    }

    @Override
    public TransitionBuilder getTransitionBuilder() {
        return context.getFormatterContext().getTransitionBuilder();
    }

}
