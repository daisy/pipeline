/*
 * Braille Utils (C) 2010-2011 Daisy Consortium 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com_braillo;

import org.daisy.braille.api.embosser.EmbosserFeatures;
import org.daisy.braille.api.factory.FactoryProperties;
import org.daisy.braille.api.paper.PageFormat;
import org.daisy.braille.api.paper.Paper;
import org.daisy.braille.api.paper.Paper.Type;
import org.daisy.braille.api.table.TableCatalogService;

public class Braillo440SFEmbosser extends AbstractBraillo440Embosser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3642071434258027472L;

	public Braillo440SFEmbosser(TableCatalogService service, FactoryProperties props) {
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
            		throw new IllegalArgumentException("Unsupported value for saddle stitch.");
            	}
            } catch (ClassCastException e) {
            	throw new IllegalArgumentException("Unsupported value for saddle stitch.");
            }
		} else {
			super.setFeature(key, value);
		}
	}

	public boolean supportsPageFormat(PageFormat pageFormat) {
		return pageFormat.getPageFormatType() == PageFormat.Type.ROLL 
				&& pageFormat.asRollPaperFormat().getLengthAcrossFeed().asMillimeter() <= 330
				&& pageFormat.asRollPaperFormat().getLengthAlongFeed().asMillimeter() >= 417
				&& pageFormat.asRollPaperFormat().getLengthAlongFeed().asMillimeter() <= 585;
	}

	public boolean supportsPaper(Paper paper) {
		return paper.getType() == Type.ROLL
			&& paper.asRollPaper().getLengthAcrossFeed().asMillimeter() <= 330;
	}
	
	@Override
	public boolean supportsPrintMode(PrintMode mode) {
		return mode == PrintMode.MAGAZINE;
	}

}
