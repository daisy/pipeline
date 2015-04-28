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

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import org.daisy.braille.embosser.FileFormat;
import org.daisy.braille.embosser.FileFormatProvider;

/**
 *
 * @author Bert Frees
 */
public class BrailleEditorsFileFormatProvider implements FileFormatProvider {

    public static enum FileType { BRF, BRL, BRA };

    private final Map<FileType,FileFormat> map;

    public BrailleEditorsFileFormatProvider() {
            map = new HashMap<FileType,FileFormat>();
            map.put(FileType.BRF, new BrailleEditorsFileFormat("BRF (Braille Formatted)", "Duxbury Braille file",      FileType.BRF));
            map.put(FileType.BRA, new BrailleEditorsFileFormat("BRA",                     "Spanish Braille file",      FileType.BRA));
          //map.put(FileType.BRL, new BrailleEditorsFileFormat("BRL",                     "MicroBraille Braille file", FileType.BRL));
    }

    public Collection<FileFormat> list() {
        return map.values();
    }
}
