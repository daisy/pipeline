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
import org.daisy.dotify.api.paper.TractorPaper;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * TODO: write java doc.
 */
@Component
public class TractorPaperProvider implements PaperProvider {
    public static final double INCH_IN_MM = 25.4;

    /**
     * TODO: write java doc.
     */
    enum PaperSize {
        W210MM_X_H10INCH,
        W210MM_X_H11INCH,
        W210MM_X_H12INCH,
        W240MM_X_H12INCH,
        W280MM_X_H12INCH;
        private final String identifier;

        PaperSize() {
            this.identifier = "org_daisy.TractorPaperProvider.PaperSize." + this.toString();
        }

        String getIdentifier() {
            return identifier;
        }
    }

    private final Collection<Paper> papers;

    public TractorPaperProvider() {
        List<Paper> tmp = new ArrayList<Paper>();
        tmp.add(
            new TractorPaper(
                "210 mm x 10 inch",
                "Tractor paper: 210 mm wide (excluding paper guides)",
                PaperSize.W210MM_X_H10INCH.getIdentifier(),
                Length.newMillimeterValue(210d),
                Length.newInchValue(10)
            )
        );
        tmp.add(
            new TractorPaper(
                "210 mm x 11 inch",
                "Tractor paper: 210 mm wide (excluding paper guides)",
                PaperSize.W210MM_X_H11INCH.getIdentifier(),
                Length.newMillimeterValue(210d),
                Length.newInchValue(11)
            )
        );
        tmp.add(
            new TractorPaper(
                "210 mm x 12 inch",
                "Tractor paper: 210 mm wide (excluding paper guides)",
                PaperSize.W210MM_X_H12INCH.getIdentifier(),
                Length.newMillimeterValue(210d),
                Length.newInchValue(12)
            )
        );
        tmp.add(
            new TractorPaper(
                "240 mm x 12 inch",
                "Tractor paper: 240 mm wide (excluding paper guides)",
                PaperSize.W240MM_X_H12INCH.getIdentifier(),
                Length.newMillimeterValue(240d),
                Length.newInchValue(12)
            )
        );
        tmp.add(
            new TractorPaper(
                "280 mm x 12 inch",
                "Tractor paper: 280 mm wide (excluding paper guides)",
                PaperSize.W280MM_X_H12INCH.getIdentifier(),
                Length.newMillimeterValue(280d),
                Length.newInchValue(12)
            )
        );
        this.papers = Collections.unmodifiableCollection(tmp);
    }

    @Override
    public Collection<Paper> list() {
        return papers;
    }

}
