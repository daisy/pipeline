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
package org.daisy.braille.utils.impl.provider;

import org.daisy.dotify.api.paper.Length;
import org.daisy.dotify.api.paper.Paper;
import org.daisy.dotify.api.paper.PaperProvider;
import org.daisy.dotify.api.paper.RollPaper;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * TODO: write java doc.
 */
@Component
public class RollPaperProvider implements PaperProvider {
    enum PaperSize {
        W21CM,
        W24CM,
        W28CM,
        W33CM;

        private final String identifier;

        PaperSize() {
            this.identifier = "org_daisy.RollPaperProvider.PaperSize." + this.toString();
        }

        String getIdentifier() {
            return identifier;
        }
    }

    private final Collection<Paper> papers;

    public RollPaperProvider() {
        List<Paper> tmp = new ArrayList<Paper>();
        tmp.add(new RollPaper("21 cm wide", "", PaperSize.W21CM.getIdentifier(), Length.newCentimeterValue(21)));
        tmp.add(new RollPaper("24 cm wide", "", PaperSize.W24CM.getIdentifier(), Length.newCentimeterValue(24)));
        tmp.add(new RollPaper("28 cm wide", "", PaperSize.W28CM.getIdentifier(), Length.newCentimeterValue(28)));
        tmp.add(new RollPaper("33 cm wide", "", PaperSize.W33CM.getIdentifier(), Length.newCentimeterValue(33)));
        this.papers = Collections.unmodifiableCollection(tmp);
    }

    @Override
    public Collection<Paper> list() {
        return papers;
    }

}
