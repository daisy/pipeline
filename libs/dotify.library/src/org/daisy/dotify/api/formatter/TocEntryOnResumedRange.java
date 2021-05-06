package org.daisy.dotify.api.formatter;

import org.daisy.dotify.api.obfl.ObflParserException;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Defines a range of toc-entry-on-resumed elements.</p>
 *
 * <p>This class stores ranges of the form [startRefId,endRefId) or [startRefId,)</p>
 *
 * @author Paul Rambags
 */
public class TocEntryOnResumedRange {

    /**
     * <p>Pattern for parsing the range attribute</p>
     *
     * <p>This pattern is taken from the
     * <a href="https://github.com/mtmse/obfl/blob/master/src/validation/obfl.rng">OBFL specification</a>
     * and slightly modified so that it returns two groups.
     * The pattern matches only if the range is in one of these forms:
     * [startRefId,endRefId] (unsupported) or [startRefId,endRefId) or [startRefId,)
     * and when it matches, it returns two groups: the first one is startRefId and
     * the second one is endRefId followed by either a ']' or a ')' character.</p>
     */
    private static final Pattern PATTERN = Pattern.compile(
        "^\\[([^,\\[\\]\\)]+),([^,\\[\\]\\)]+\\]|[^,\\[\\]\\)]*\\))$"
    );

    /* the startRefId refers to the start of the first block in the range. May not be null */
    private final String startRefId;
    /* the endRefId refers to the start of the last block in the range. It may be absent. */
    private final Optional<String> endRefId;

    public TocEntryOnResumedRange(String range) throws ObflParserException {
        // parse the range
        String startId = null;
        String endId = null;
        Matcher m = PATTERN.matcher(range);
        if (m.find()) {
            startId = m.group(1).trim();
            endId = m.group(2);
            if (endId.endsWith("]")) {
                throw new UnsupportedOperationException(
                    String.format(
                        "Found range %s. Ranges in the form [startRefId,endRefId] are unsupported. " +
                        "Please use this form: [startRefId,endRefId)",
                        range
                    )
                );
            }
            endId = endId.substring(0, endId.length() - 1).trim();
            if (endId.length() == 0) {
                endId = null;
            }
        }
        if (startId == null) {
            throw new ObflParserException(String.format("Could not parse this range: %s", range));
        }

        this.startRefId = startId;
        this.endRefId = Optional.ofNullable(endId);
    }

    /**
     * Get the start ref-id of the range.
     *
     * @return the start ref-id of the range, not null
     */
    public String getStartRefId() {
        return startRefId;
    }

    /**
     * Get the end ref-id of the range.
     *
     * @return the end ref-id of the range
     */
    public Optional<String> getEndRefId() {
        return endRefId;
    }
}
