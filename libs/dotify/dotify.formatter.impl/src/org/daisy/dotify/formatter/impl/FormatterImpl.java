package org.daisy.dotify.formatter.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.daisy.dotify.api.translator.MarkerProcessorFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;
import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.formatter.impl.page.BlockSequence;
import org.daisy.dotify.formatter.impl.page.RestartPaginationException;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
import org.daisy.dotify.formatter.impl.sheet.VolumeImpl;
import org.daisy.dotify.formatter.impl.volume.TableOfContentsImpl;
import org.daisy.dotify.formatter.impl.volume.VolumeTemplate;
import org.daisy.dotify.formatter.impl.writer.Volume;
import org.daisy.dotify.formatter.impl.writer.WriterHandler;


/**
 * Breaks flow into rows, page related block properties are left to next step
 * @author Joel Håkansson
 */
class FormatterImpl implements Formatter {

	private final HashMap<String, TableOfContentsImpl> tocs;
	private final Stack<VolumeTemplate> volumeTemplates;
	private final Logger logger;

	private boolean unopened;
	private final Stack<BlockSequence> blocks;
	
	private final LazyFormatterContext context;

	/**
	 * Creates a new formatter.
	 * @param translatorFactory a braille translator factory maker service
	 * @param tbf a text border factory maker service
	 * @param mpf a marker processor factory maker service
	 * @param locale a locale
	 * @param mode a braille mode
	 */
	FormatterImpl(BrailleTranslatorFactoryMakerService translatorFactory, TextBorderFactoryMakerService tbf, MarkerProcessorFactoryMakerService mpf, String locale, String mode) {
		this.context = new LazyFormatterContext(translatorFactory, tbf, mpf, FormatterConfiguration.with(locale, mode).build());
		this.blocks = new Stack<>();
		this.unopened = true;
		this.tocs = new HashMap<>();
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
		BlockSequence currentSequence = new BlockSequence(context.getFormatterContext(), p, context.getFormatterContext().getMasters().get(p.getMasterName()));
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
		VolumeTemplate template = new VolumeTemplate(context.getFormatterContext(), tocs, props.getCondition(), props.getSplitterMax());
		volumeTemplates.push(template);
		return template;
	}

	@Override
	public TableOfContents newToc(String tocName) {
		unopened = false;
		TableOfContentsImpl toc = new TableOfContentsImpl(context.getFormatterContext());
		tocs.put(tocName, toc);
		return toc;
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
		CrossReferenceHandler crh = new CrossReferenceHandler();
		VolumeProvider volumeProvider = new VolumeProvider(blocks, volumeTemplates, context, crh);

		ArrayList<VolumeImpl> ret;

		int maxIterations = 50;
		for (int j=1;j<=maxIterations;j++) {
			try {
				ret = new ArrayList<>();
				volumeProvider.prepare();
				for (int i=1;i<= crh.getVolumeCount();i++) {
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
