package org.daisy.braille.utils.impl.provider.braillo;

import org.daisy.dotify.api.embosser.EmbosserFactoryProperties;
import org.daisy.dotify.api.embosser.EmbosserFeatures;
import org.daisy.dotify.api.paper.PageFormat;
import org.daisy.dotify.api.paper.Paper;
import org.daisy.dotify.api.paper.Paper.Type;
import org.daisy.dotify.api.table.TableCatalogService;

/**
 * Provides an Embosser for Braillo 600SF. This printer
 * is based on the Braillo SW with the same well-proven technology.
 * 
 * @author alra
 *
 */
public class Braillo650SFEmbosser extends AbstractBraillo440Embosser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3981473029360378026L;
	private static final String EMBOSSER_UNSUPPORTED_MESSAGE = "Unsupported value for saddle stitch.";

	public Braillo650SFEmbosser(TableCatalogService service, EmbosserFactoryProperties props) {
		super(service, props);
		saddleStitchEnabled = true;
	}

	@Override
	public void setFeature(String key, Object value) {
		if (EmbosserFeatures.SADDLE_STITCH.equals(key)) {
			try {
				saddleStitchEnabled = (Boolean)value;
				if (!saddleStitchEnabled) {
					saddleStitchEnabled = true;
					throw new IllegalArgumentException(EMBOSSER_UNSUPPORTED_MESSAGE);
				}
			} 
			catch (ClassCastException e) {
				throw new IllegalArgumentException(EMBOSSER_UNSUPPORTED_MESSAGE);
			}
		} 
		else {
			super.setFeature(key, value);
		}
	}

	@Override
	public boolean supportsPageFormat(PageFormat pageFormat) {
		return pageFormat.getPageFormatType() == PageFormat.Type.ROLL 
				&& pageFormat.asRollPaperFormat().getLengthAcrossFeed().asMillimeter() <= 330
				&& pageFormat.asRollPaperFormat().getLengthAlongFeed().asMillimeter() >= 417
				&& pageFormat.asRollPaperFormat().getLengthAlongFeed().asMillimeter() <= 585;
	}

	@Override
	public boolean supportsPaper(Paper paper) {
		return paper.getType() == Type.ROLL
				&& paper.asRollPaper().getLengthAcrossFeed().asMillimeter() <= 330;
	}

	@Override
	public boolean supportsPrintMode(PrintMode mode) {
		return mode == PrintMode.MAGAZINE;
	}
}