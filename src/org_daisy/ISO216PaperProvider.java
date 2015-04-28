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
package org_daisy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.daisy.braille.tools.Length;
import org.daisy.paper.Paper;
import org.daisy.paper.PaperProvider;
import org.daisy.paper.SheetPaper;

import aQute.bnd.annotation.component.Component;

@Component
public class ISO216PaperProvider implements PaperProvider {
	public static final double INCH_IN_MM = 25.4;
	enum PaperSize {
		A3,
		A4,
                A5,
                B3,
                B4,
                B5
	};

	private final Collection<Paper> papers;
	
	public ISO216PaperProvider() {
		ArrayList<Paper> tmp = new ArrayList<Paper>();
		tmp.add(new SheetPaper("A3", "297 mm x 420 mm", PaperSize.A3, 
				Length.newMillimeterValue(297d),
				Length.newMillimeterValue(420d)));
		tmp.add(new SheetPaper("A4", "210 mm x 297 mm", PaperSize.A4, 
				Length.newMillimeterValue(210d),
				Length.newMillimeterValue(297d)));
        tmp.add(new SheetPaper("A5", "148 mm x 210 mm", PaperSize.A5, 
        		Length.newMillimeterValue(148d), 
        		Length.newMillimeterValue(210d)));
        tmp.add(new SheetPaper("B3", "353 mm x 500 mm", PaperSize.B3, 
        		Length.newMillimeterValue(353d), 
        		Length.newMillimeterValue(500d)));
        tmp.add(new SheetPaper("B4", "250 mm x 353 mm", PaperSize.B4, 
        		Length.newMillimeterValue(250d), 
        		Length.newMillimeterValue(353d)));
        tmp.add(new SheetPaper("B5", "176 mm x 250 mm", PaperSize.B5, 
        		Length.newMillimeterValue(176d), 
        		Length.newMillimeterValue(250d)));
        this.papers = Collections.unmodifiableCollection(tmp);
	}

	//jvm1.6@Override
	public Collection<Paper> list() {
		return papers;
	}
}
