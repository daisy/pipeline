package org.daisy.dotify.formatter.impl.page;

import org.daisy.dotify.common.text.StringTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Does positioning of header/footer fields, and layout of tables.
 */
class PaginatorTools {

    private static final Logger logger = Logger.getLogger(PaginatorTools.class.getCanonicalName());

    /**
     * Distribution modes.
     */
    enum DistributeMode {
        /**
         * Distribute so that the spaces between strings are kept equal.
         */
        EQUAL_SPACING,
        /**
         * Distribute so that the spaces between strings are kept equal, and
         * if needed the longest string is truncated in order to make the
         * resulting string fit in the provided space.
         */
        EQUAL_SPACING_TRUNCATE,
        /**
         * Distribute so that each cell is equally wide.
         */
        UNISIZE_TABLE_CELL
    }

    private PaginatorTools() {
    }

    /**
     * See distributeRetain.
     */
    private static List<String> distributeEqualSpacing(
        List<String> units,
        int width,
        String padding,
        boolean truncate
    ) throws PaginatorToolsException {
        if (units.size() == 1) {
            String unit = units.get(0);
            if (unit.codePointCount(0, unit.length()) > width) {
                if (truncate) {
                    unit = truncate(unit, width);
                } else {
                    throw new PaginatorToolsException(
                        "Text does not fit within provided space of " + width + ": " + unit
                    );
                }
            }
            List<String> ret = new ArrayList<String>();
            ret.add(unit);
            return ret;
        }
        int chunksLength = 0;
        for (String s : units) {
            chunksLength += s.codePointCount(0, s.length());
        }
        int totalSpace = width - chunksLength;
        if (totalSpace < 0) {
            if (truncate) {
                int longestChunkLength = 0;
                int longestChunk = 0;
                int chunk = 0;
                for (String s : units) {
                    int l = s.codePointCount(0, s.length());
                    if (l > longestChunkLength) {
                        longestChunkLength = l;
                        longestChunk = chunk;
                    }
                    chunk++;
                }
                if (longestChunkLength + totalSpace > 0) {
                    units.set(longestChunk,
                            truncate(units.get(longestChunk), longestChunkLength + totalSpace)
                    );
                } else {
                    throw new PaginatorToolsException(
                        "Text does not fit within provided space of " + width + ": " + units
                    );
                }
            } else {
                throw new PaginatorToolsException(
                    "Text does not fit within provided space of " + width + ": " + units + " (" +
                        units.stream().mapToInt(v -> v.length()).sum() + ")"
                );
            }
        }
        int parts = units.size() - 1;
        double target = totalSpace / (double) parts;
        int used = 0;
        int length = 0;
        List<String> ret = new ArrayList<String>();
        for (int i = 0; i < units.size(); i++) {
            String unit = units.get(i);
            if (i > 0) {
                int spacing = (int) Math.round(i * target) - used;
                used += spacing;
                ret.add(StringTools.fill(padding, spacing));
                length += spacing;
            }
            ret.add(unit);
            length += unit.length();
        }
        assert length == width;
        return ret;
    }

    private static String distributeTable(
        List<String> units,
        int width,
        String padding
    ) throws PaginatorToolsException {
        double target = width / (double) units.size();
        StringBuffer sb = new StringBuffer();
        int used = 0;
        for (int i = 0; i < units.size(); i++) {
            int spacing = (int) Math.round((i + 1) * target) - used;
            String cell = units.get(i);
            used += spacing;
            spacing -= cell.codePointCount(0, cell.length());
            if (spacing < 0) {
                throw new PaginatorToolsException("Text does not fit within cell: " + cell);
            }
            sb.append(cell);
            if (i < units.size() - 1) {
                sb.append(StringTools.fill(padding, spacing));
            }
        }
        return sb.toString();
    }

    private static String truncate(String s, int lengthInCodePoints) {
        if (s.codePointCount(0, s.length()) > lengthInCodePoints) {
            String truncated = s.substring(0, s.offsetByCodePoints(0, lengthInCodePoints));
            logger.log(Level.WARNING,
                       "Text does not fit within provided space of " + lengthInCodePoints + ": \"" + s + "\""
                       + ", trimming to \"" + truncated + "\"");
            return truncated;
        } else {
            return s;
        }
    }

    /**
     * Distribute <code>units</code> of text over <code>width</code> chars, separated by <code>padding</code> pattern
     * using distribution mode <code>mode</code>.
     *
     * @param units   the units of text to distribute
     * @param width   the width of the resulting string
     * @param padding the padding pattern to use as separator
     * @param mode    the distribution mode to use
     * @return returns a string of <code>width</code> chars
     * @throws PaginatorToolsException if distribution fails
     */
    static String distribute(
        List<String> units,
        int width,
        String padding,
        DistributeMode mode
    ) throws PaginatorToolsException {
        switch (mode) {
            case UNISIZE_TABLE_CELL:
                return distributeTable(units, width, padding);
            default:
                StringBuffer b = new StringBuffer();
                for (String s : distributeRetain(units, width, padding, mode)) {
                    b.append(s);
                }
                return b.toString();
        }
    }

    /**
     * @param units
     * @return a list of size 2*N-1, where N is the size of <code>units</code>. Element i of
     * <code>unit</code> corresponds with element 2*i in the returned list. Elements i*2+1 for
     * i=(0..N-1) is padding between units. The sum of the lengths of all strings equals
     * <code>width</code>.
     * @throws PaginatorToolsException if distribution fails
     */
    static List<String> distributeRetain(
        List<String> units,
        int width,
        String padding,
        DistributeMode mode
    ) throws PaginatorToolsException {
        switch (mode) {
            case EQUAL_SPACING:
                return distributeEqualSpacing(units, width, padding, false);
            case EQUAL_SPACING_TRUNCATE:
                return distributeEqualSpacing(units, width, padding, true);
            default:
                throw new IllegalArgumentException();
        }
    }

    static String distribute(Collection<TabStopString> units) {
        TreeSet<TabStopString> sortedUnits = new TreeSet<>();
        sortedUnits.addAll(units);
        StringBuffer sb = new StringBuffer();
        int used = 0;
        for (TabStopString t : sortedUnits) {
            used = sb.codePointCount(0, sb.length());
            if (used > t.getPosition()) {
                throw new RuntimeException("Cannot layout cell.");
            }
            int amount = t.getPosition() - used;
            switch (t.getAlignment()) {
                case LEFT:
                    //ok
                    break;
                case CENTER:
                    amount -= t.length() / 2;
                    break;
                case RIGHT:
                    amount -= t.length();
                    break;
            }
            sb.append(StringTools.fill(t.getPattern(), amount));
            sb.append(t.getText());
        }
        return sb.toString();
    }

}
