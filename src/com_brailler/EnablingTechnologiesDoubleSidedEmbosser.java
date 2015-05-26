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

import org.daisy.braille.embosser.EmbosserFeatures;
import org.daisy.braille.table.TableCatalogService;
import org.daisy.paper.PageFormat;
import org.daisy.paper.PrintPage;

import com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType;

/**
 *
 * @author Bert Frees
 */
public class EnablingTechnologiesDoubleSidedEmbosser extends EnablingTechnologiesEmbosser {

    /**
	 * 
	 */
	private static final long serialVersionUID = 160751373667707902L;

	public EnablingTechnologiesDoubleSidedEmbosser(TableCatalogService service, EmbosserType props) {

        super(service, props);

        switch (type) {
            case ET:
            case JULIET_PRO:
            case JULIET_PRO_60:
            case JULIET_CLASSIC:
            case BOOKMAKER:
            case BRAILLE_EXPRESS_100:
            case BRAILLE_EXPRESS_150:
            case BRAILLE_PLACE:
                break;
            default:
                throw new IllegalArgumentException("Unsupported embosser type");
        }

        duplexEnabled = true;
    }

    @Override
    public boolean supportsDuplex() {
        return true;
    }

    @Override
    public Object getFeature(String key) {

        if (EmbosserFeatures.DUPLEX.equals(key)) {
            return duplexEnabled;
        } else {
            return super.getFeature(key);
        }
    }

    @Override
    public void setFeature(String key, Object value) {

        if (EmbosserFeatures.DUPLEX.equals(key)) {
            try {
                duplexEnabled = (Boolean)value;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Unsupported value for duplex.");
            }
        } else {
            super.setFeature(key, value);
        }
    }

	public boolean supportsZFolding() {
		return false;
	}
	
	@Override
	public boolean supportsPrintMode(PrintMode mode) {
		return PrintMode.REGULAR == mode;
	}
	
	@Override	
	public PrintPage getPrintPage(PageFormat pageFormat) {
		return new PrintPage(pageFormat);
	}
}
