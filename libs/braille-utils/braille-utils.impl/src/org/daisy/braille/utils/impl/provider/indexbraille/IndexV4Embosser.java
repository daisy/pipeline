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
package org.daisy.braille.utils.impl.provider.indexbraille;

import java.io.OutputStream;

import org.daisy.dotify.api.embosser.EmbosserFactoryException;
import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.PrintPage;
import org.daisy.dotify.api.embosser.UnsupportedPaperException;
import org.daisy.dotify.api.paper.Length;
import org.daisy.dotify.api.paper.PageFormat;
import org.daisy.dotify.api.table.TableCatalogService;
import org.daisy.dotify.api.table.TableFilter;
import org.daisy.braille.utils.impl.provider.indexbraille.IndexEmbosserProvider.EmbosserType;
import org.daisy.braille.utils.impl.tools.embosser.BufferedEmbosserWriter;

/**
 * Provides an implementation for Index V4/V5 embossers.
 */
public class IndexV4Embosser extends IndexEmbosser {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3888325825465502071L;
	static final String TABLE6DOT = "org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US";
	static final TableFilter tableFilter = (object) -> {
		return object == null?false:object.getIdentifier().equals(TABLE6DOT);
	};

	public IndexV4Embosser(TableCatalogService service, EmbosserType props) {
		super(service, props);

		setTable = service.newTable(TABLE6DOT);
		duplexEnabled = true;

		switch (type) {
		case INDEX_BASIC_D_V4:
		case INDEX_BASIC_D_V5:
		case INDEX_FANFOLD_V5:
			maxCellsInWidth = 49;
			break;
		case INDEX_EVEREST_D_V4:
		case INDEX_EVEREST_D_V5:
		case INDEX_BRAILLE_BOX_V4:
		case INDEX_BRAILLE_BOX_V5:
			maxCellsInWidth = 48;
			break;
		default:
			throw new IllegalArgumentException("Unsupported embosser type");
		}

		maxNumberOfCopies = 10000;
		maxMarginInner = 10;
		maxMarginOuter = 10;
		maxMarginTop = 10;
	}
	
	@Override
	public boolean supportsDuplex() {
		return true;
	}

	@Override
	public boolean supports8dot() {
		return true;
	}

	@Override
	public TableFilter getTableFilter() {
		return tableFilter;
	}

	@Override
	public boolean supportsPrintPage(PrintPage dim) {
		if (dim==null) {
			return false;
		}
		if (type == EmbosserType.INDEX_BRAILLE_BOX_V4 || type == EmbosserType.INDEX_BRAILLE_BOX_V5) {
			Length across = dim.getLengthAcrossFeed();
			Length along = dim.getLengthAlongFeed();
			if (saddleStitchEnabled) {
				return (across.asMillimeter() == 297 && along.asMillimeter() == 420) ||
						(across.asInches()     == 11  && along.asInches()     == 17);
			} else {
				return (across.asMillimeter() == 297 && along.asMillimeter() == 210) ||
						(across.asInches()     == 11  && along.asInches()     == 8.5) ||
						(across.asInches()     == 11  && along.asInches()     == 11.5);
			}
		} else {
			return super.supportsPrintPage(dim);
		}
	}

	@Override
	public EmbosserWriter newEmbosserWriter(OutputStream os) {

		PageFormat page = getPageFormat();
		if (!supportsPageFormat(page)) {
			throw new IllegalArgumentException(new UnsupportedPaperException("Unsupported paper"));
		}

		int cellsInWidth = (int)Math.floor(getPrintArea(page).getWidth()/getCellWidth());

		if (cellsInWidth > maxCellsInWidth) {
			throw new IllegalArgumentException(new UnsupportedPaperException("Unsupported paper"));
		}
		if (numberOfCopies > maxNumberOfCopies || numberOfCopies < 1) {
			throw new IllegalArgumentException(new EmbosserFactoryException("Invalid number of copies: " + numberOfCopies + " is not in [1, 10000]"));
		}

		return new BufferedEmbosserWriter(new IndexContractEmbosserWriter(os,
					setTable.newBrailleConverter(),
					new IndexV4Header.Builder(getPrintableArea(page))
						.cellWidth(getCellWidth())
						.cellHeight(getCellHeight())
						.marginTop(marginTop)
						.duplex(duplexEnabled)
						.zFolding(zFoldingEnabled)
						.numberOfCopies(numberOfCopies)
						.saddleStitch(saddleStitchEnabled)));
	}

}
