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
package org.daisy.braille.utils.impl.tools.embosser;

/**
 * Provides an object representation for each event in the EmbosserWriter interface.
 * The purpose is to add the possibility to buffer EmbosserWriter events for later use.
 *
 * @author Joel Håkansson
 */
public class EmbosserWriterEvent {
    enum EventType {
        OPEN_EVENT,
        WRITE_EVENT,
        NEW_LINE_EVENT,
        NEW_PAGE_EVENT,
        NEW_SECTION_AND_PAGE_EVENT,
        NEW_VOLUME_SECTION_AND_PAGE_EVENT,
        SET_ROWGAP_EVENT,
        CLOSE_EVENT
    }

    private final EventType t;

    private EmbosserWriterEvent(EventType t) {
        this.t = t;
    }

    /**
     * Gets the event type.
     *
     * @return returns the event
     */
    public EventType getEventType() {
        return t;
    }

    private static class DuplexEvent extends EmbosserWriterEvent {
        private final boolean duplex;

        public DuplexEvent(EventType t, boolean duplex) {
            super(t);
            this.duplex = duplex;
        }

        public boolean getDuplex() {
            return duplex;
        }
    }

    /**
     * Provides an object representation for an EmbosserWriter write event.
     *
     * @author Joel Håkansson
     */
    public static class WriteEvent extends EmbosserWriterEvent {
        private final String braille;

        /**
         * Creates a new write event.
         *
         * @param braille the braille characters
         */
        public WriteEvent(String braille) {
            super(EventType.WRITE_EVENT);
            this.braille = braille;
        }

        /**
         * Gets the braille characters.
         *
         * @return returns the braille characters
         */
        public String getBraille() {
            return braille;
        }
    }

    /**
     * Provides an object representation for an EmbosserWriter open event.
     *
     * @author Joel Håkansson
     */
    public static class OpenEvent extends DuplexEvent {

        /**
         * Creates a new OpenEvent with no contract.
         *
         * @param duplex true if using both sides of a sheet, false otherwise
         */
        public OpenEvent(boolean duplex) {
            super(EventType.OPEN_EVENT, duplex);
        }

    }

    /**
     * Provides an object representation for an EmbosserWriter close event.
     *
     * @author Joel Håkansson
     */
    public static class CloseEvent extends EmbosserWriterEvent {
        /**
         * Creates a new close event.
         */
        public CloseEvent() {
            super(EventType.CLOSE_EVENT);
        }
    }

    /**
     * Provides an object representation for an EmbosserWriter newSectionAndPage event.
     *
     * @author Joel Håkansson
     */
    public static class NewSectionAndPageEvent extends DuplexEvent {
        /**
         * Creates a new section and page event.
         *
         * @param duplex true if using both sides of a sheet, false otherwise
         */
        public NewSectionAndPageEvent(boolean duplex) {
            super(EventType.NEW_SECTION_AND_PAGE_EVENT, duplex);
        }
    }

    /**
     * Provides an object representation for an EmbosserWriter newVolumeSectionAndPage event.
     *
     * @author Joel Håkansson
     */
    public static class NewVolumeSectionAndPageEvent extends DuplexEvent {
        /**
         * Creates a new volume, section and page event.
         *
         * @param duplex true if using both sides of a sheet, false otherwise
         */
        public NewVolumeSectionAndPageEvent(boolean duplex) {
            super(EventType.NEW_VOLUME_SECTION_AND_PAGE_EVENT, duplex);
        }
    }

    /**
     * Provides an object representation for an EmbosserWriter newLine event.
     *
     * @author Joel Håkansson
     */
    public static class NewLineEvent extends EmbosserWriterEvent {
        /**
         * Creates a new new line event.
         */
        public NewLineEvent() {
            super(EventType.NEW_LINE_EVENT);
        }
    }

    /**
     * Provides an object representation for an EmbosserWriter newPage event.
     *
     * @author Joel Håkansson
     */
    public static class NewPageEvent extends EmbosserWriterEvent {
        /**
         * Creates a new page event.
         */
        public NewPageEvent() {
            super(EventType.NEW_PAGE_EVENT);
        }
    }

    /**
     * Provides an object representation for an EmbosserWriter setRowGap event.
     *
     * @author Joel Håkansson
     */
    public static class SetRowGapEvent extends EmbosserWriterEvent {
        private final int val;

        /**
         * Creates a new set row gap event.
         *
         * @param val the rowgap
         */
        public SetRowGapEvent(int val) {
            super(EventType.SET_ROWGAP_EVENT);
            this.val = val;
        }

        /**
         * Gets the row gap.
         *
         * @return returns the row gap
         */
        public int getRowGap() {
            return val;
        }
    }

}
