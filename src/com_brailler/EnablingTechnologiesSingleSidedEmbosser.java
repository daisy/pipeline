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
package com_brailler;

import org.daisy.braille.table.TableCatalogService;
import org.daisy.paper.PageFormat;
import org.daisy.paper.PrintPage;

import com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType;

/**
 *
 * @author Bert Frees
 */
public class EnablingTechnologiesSingleSidedEmbosser extends EnablingTechnologiesEmbosser {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3602582375923051565L;

	public EnablingTechnologiesSingleSidedEmbosser(TableCatalogService service, EmbosserType props) {

        super(service, props);

        switch (type) {
            case ROMEO_ATTACHE:
            case ROMEO_ATTACHE_PRO:
            case ROMEO_25:
            case ROMEO_PRO_50:
            case ROMEO_PRO_LE_NARROW:
            case ROMEO_PRO_LE_WIDE:
            case THOMAS:
            case THOMAS_PRO:
            case MARATHON:
                break;
            default:
                throw new IllegalArgumentException("Unsupported embosser type");
        }

        duplexEnabled = false;
    }

    public boolean supportsDuplex() {
        return false;
    }

	public boolean supportsZFolding() {
		return false;
	}

	//jvm1.6@Override
	public boolean supportsPrintMode(PrintMode mode) {
		return PrintMode.REGULAR == mode;
	}

	//jvm1.6Override	
	public PrintPage getPrintPage(PageFormat pageFormat) {
		return new PrintPage(pageFormat);
	}

}
