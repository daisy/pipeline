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
package se_tpb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.daisy.braille.api.paper.Length;
import org.daisy.braille.api.paper.Paper;
import org.daisy.braille.api.paper.PaperProvider;
import org.daisy.braille.api.paper.SheetPaper;

import aQute.bnd.annotation.component.Component;

@Component
public class FA44PaperProvider implements PaperProvider {
	public static final double INCH_IN_MM = 25.4;
	enum PaperSize {
		FA44,
		//FA44_LEGACY
	};

	private final Collection<Paper> papers;
	
	public FA44PaperProvider() {
		ArrayList<Paper> tmp = new ArrayList<Paper>();
		tmp.add(new SheetPaper("FA44", "261 mm x 297 mm", PaperSize.FA44, 
				Length.newMillimeterValue(261d),
				Length.newMillimeterValue(297d)));
		//papers.add(new DefaultPaper("FA44 (legacy)", "252 mm x 297 mm", PaperSize.FA44_LEGACY, 252d, 297d));
		this.papers = Collections.unmodifiableCollection(tmp);
	}

	@Override
	public Collection<Paper> list() {
		return papers;
	}

}
