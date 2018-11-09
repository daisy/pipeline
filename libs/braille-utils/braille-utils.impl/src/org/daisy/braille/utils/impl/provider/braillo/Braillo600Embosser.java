package org.daisy.braille.utils.impl.provider.braillo;

import org.daisy.dotify.api.embosser.EmbosserFactoryProperties;
import org.daisy.dotify.api.paper.PageFormat;
import org.daisy.dotify.api.paper.Paper;
import org.daisy.dotify.api.table.TableCatalogService;

/**
 * Provides an Embosser for Braillo 600. This printer
 * is based on the Braillo 400S with the same well-proven technology.
 * 
 * @author alra
 *
 */
public class Braillo600Embosser extends AbstractBraillo200Embosser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1849647807065499461L;

	/**
	 * Creates a new Braillo 600 embosser.
	 * @param service the table catalog
	 * @param props the embosser properties
	 */
	public Braillo600Embosser(TableCatalogService service, EmbosserFactoryProperties props) {
		super(service, props);
	}

	@Override
	public boolean supportsPageFormat(PageFormat pageFormat) {
		return pageFormat.getPageFormatType() == PageFormat.Type.TRACTOR
				&& pageFormat.asTractorPaperFormat().getLengthAcrossFeed().asMillimeter() >= 140
				&& pageFormat.asTractorPaperFormat().getLengthAcrossFeed().asMillimeter() <= 330
				&& pageFormat.asTractorPaperFormat().getLengthAlongFeed().asInches() >= 4
				&& pageFormat.asTractorPaperFormat().getLengthAlongFeed().asInches() <= 14;
	}

	@Override
	public boolean supportsPaper(Paper paper) {
		return paper.getType() == Paper.Type.ROLL 
				&& paper.asRollPaper().getLengthAcrossFeed().asMillimeter() >= 140
				&& paper.asRollPaper().getLengthAcrossFeed().asMillimeter() <= 330;
	}	
}