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
package org.daisy.braille.embosser;

import org.daisy.braille.embosser.EmbosserProperties.PrintMode;
import org.daisy.paper.Dimensions;
import org.daisy.paper.Length;
import org.daisy.paper.PageFormat;
import org.daisy.paper.RollPaperFormat;
import org.daisy.paper.SheetPaperFormat;
import org.daisy.paper.TractorPaperFormat;

/**
 *
 * @author Bert Frees
 * @author Joel Håkansson
 */
public class PrintPage implements Dimensions {

    /**
     *  Direction of print
     */
    public enum PrintDirection {
        /**
         *  Direction of embosser head is equal to direction of feeding paper
         */
        UPRIGHT,
        /**
         *  Direction of embosser head is opposite to direction of feeding paper
         */
        SIDEWAYS
    }

    /**
	 * The shape of the paper
	 */
	public enum Shape {
		/**
		 *  Represents portrait shape, that is to say that getWidth()<getHeight()
		 */
		PORTRAIT,
		/**
		 *  Represents landscape shape, that is to say that getWidth>getHeight()
		 */
		LANDSCAPE,
		/**
		 *  Represents square shape, that is to say that getWidth()==getHeight()
		 */
		SQUARE
	}

    private final PageFormat inputPage;
    private final PrintDirection direction;
    private final PrintMode mode;

    public PrintPage(PageFormat inputPage,
                     PrintDirection direction,
                     PrintMode mode) {

        this.inputPage = inputPage;
        this.direction = direction;
        this.mode = mode;
    }

    public PrintPage(PageFormat inputPage) {
        this(inputPage, PrintDirection.UPRIGHT, PrintMode.REGULAR);
    }
    
    public Length getLengthAcrossFeed() {
    	switch (inputPage.getPageFormatType()) {
	    	case SHEET: {
	    		switch (direction) {
		    		case SIDEWAYS:
		    			return ((SheetPaperFormat)inputPage).getPageHeight();
		    		case UPRIGHT: default:
		    			return ((SheetPaperFormat)inputPage).getPageWidth();
	    		}
	    	}
	    	case ROLL:
	    		return ((RollPaperFormat)inputPage).getLengthAcrossFeed();
	    	case TRACTOR:
	    		return ((TractorPaperFormat)inputPage).getLengthAcrossFeed();
	    	default:
	    		throw new RuntimeException("Coding error");
    	}
    }

    public Length getLengthAlongFeed() {
    	switch (inputPage.getPageFormatType()) {
	    	case SHEET: {
	    		switch (direction) {
		    		case SIDEWAYS:
		    			return ((SheetPaperFormat)inputPage).getPageWidth();
		    		case UPRIGHT: default:
		    			return ((SheetPaperFormat)inputPage).getPageHeight();
	    		}
	    	}
	    	case ROLL:
	    		return ((RollPaperFormat)inputPage).getLengthAlongFeed();
	    	case TRACTOR:
	    		return ((TractorPaperFormat)inputPage).getLengthAlongFeed();
	    	default:
	    		throw new RuntimeException("Coding error");
		}
    }

    public double getWidth() {
        double width;

        switch (direction) {
            case SIDEWAYS:
                width = getLengthAlongFeed().asMillimeter();
                break;
            case UPRIGHT:
            default:
                width = getLengthAcrossFeed().asMillimeter();
        }

        switch (mode) {
            case MAGAZINE:
                return width/2;
            case REGULAR:
            default:
                return width;
        }
    }

    public double getHeight() {
        switch (direction) {
            case SIDEWAYS:
                return getLengthAcrossFeed().asMillimeter();
            case UPRIGHT:
            default:
                return getLengthAlongFeed().asMillimeter();
        }
    }
    
    public Shape getShape() {
		if (getWidth()<getHeight()) {
			return Shape.PORTRAIT;
		} else if (getWidth()>getHeight()) {
			return Shape.LANDSCAPE;
		} else {
			return Shape.SQUARE;
		}
    }
}
