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

import org.daisy.braille.tools.Length;
import org.daisy.paper.Paper;
import org.daisy.paper.PaperProvider;
import org.daisy.paper.RollPaper;

public class RollPaperProvider implements PaperProvider {
	enum PaperSize {
		W21CM,
		W24CM,
		W28CM,
		W33CM
	}
	
	private final ArrayList<Paper> papers;
	
	public RollPaperProvider() {
		papers = new ArrayList<Paper>();
		papers.add(new RollPaper("21 cm wide", "", PaperSize.W21CM, Length.newCentimeterValue(21)));
		papers.add(new RollPaper("24 cm wide", "", PaperSize.W24CM, Length.newCentimeterValue(24)));
		papers.add(new RollPaper("28 cm wide", "", PaperSize.W28CM, Length.newCentimeterValue(28)));
		papers.add(new RollPaper("33 cm wide", "", PaperSize.W33CM, Length.newCentimeterValue(33)));
	}

	public Collection<Paper> list() {
		return papers;
	}

}
