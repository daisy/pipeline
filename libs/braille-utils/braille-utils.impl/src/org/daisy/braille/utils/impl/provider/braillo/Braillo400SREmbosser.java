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
package org.daisy.braille.utils.impl.provider.braillo;

import org.daisy.dotify.api.embosser.EmbosserFactoryProperties;
import org.daisy.dotify.api.paper.PageFormat;
import org.daisy.dotify.api.paper.Paper;
import org.daisy.dotify.api.table.TableCatalogService;

public class Braillo400SREmbosser extends AbstractBraillo200Embosser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4824785073677884484L;

	public Braillo400SREmbosser(TableCatalogService service, EmbosserFactoryProperties props) {
		super(service, props);
	}

	@Override
	public boolean supportsPageFormat(PageFormat pageFormat) {
		return pageFormat.getPageFormatType() == PageFormat.Type.ROLL
				&& pageFormat.asRollPaperFormat().getLengthAcrossFeed().asMillimeter() >= 140
				&& pageFormat.asRollPaperFormat().getLengthAcrossFeed().asMillimeter() <= 330
				&& pageFormat.asRollPaperFormat().getLengthAlongFeed().asInches() >= 4
				&& pageFormat.asRollPaperFormat().getLengthAlongFeed().asInches() <= 14;
	}

	@Override
	public boolean supportsPaper(Paper paper) {
		return paper.getType() == Paper.Type.ROLL 
				&& paper.asRollPaper().getLengthAcrossFeed().asMillimeter() >= 140
				&& paper.asRollPaper().getLengthAcrossFeed().asMillimeter() <= 330;
	}


}
