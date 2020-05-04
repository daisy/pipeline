package org.daisy.dotify.formatter.impl.obfl;

import org.daisy.dotify.api.formatter.Context;
import org.daisy.dotify.api.obfl.ExpressionFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Write java doc.
 * TODO: Remove the defaults from the OBFL specification. See https://github.com/mtmse/obfl/issues/13
 */
public abstract class OBFLExpressionBase {

    public static final String
            PAGE_NUMBER_VARIABLE_NAME = "page",
            VOLUME_NUMBER_VARIABLE_NAME = "volume",
            VOLUME_COUNT_VARIABLE_NAME = "volumes",
            STARTED_VOLUME_NUMBER_VARIABLE_NAME = "started-volume-number",
            STARTED_PAGE_NUMBER_VARIABLE_NAME = "started-page-number",
            STARTED_VOLUME_FIRST_CONTENT_PAGE_NUMBER_VARIABLE_NAME = "started-volume-first-content-page",
            SHEET_COUNT_VARIABLE_NAME = "sheets-in-document",
            VOLUME_SHEET_COUNT_VARIABLE_NAME = "sheets-in-volume";

    protected final ExpressionFactory ef;
    protected final String exp;

    private String
            pageNumberVariableName = null,
            volumeNumberVariableName = null,
            volumeCountVariableName = null,
            metaVolumeNumberVariableName = null,
            metaPageNumberVariableName = null,
            sheetCountVariableName = null,
            volumeSheetCountVariableName = null;

    /**
     * @param exp The expression string
     * @param ef The expression factory
     * @param variables The variables (zero or more) that may be used within the expression.
     * <p>Note that whether a variable will actually be assigned a value is not
     * determined by this object. This depends on the context in which the
     * expression is used.</p>
     * <p>The variables {@link OBFLVariable#STARTED_PAGE_NUMBER} and {@link
     * OBFLVariable#STARTED_VOLUME_FIRST_CONTENT_PAGE_NUMBER} cannot both be
     * used in the same expression. At most one "<i>meta</i>" page number is
     * available at any particular place in the OBFL. Which of the two is
     * included in the <code>variables</code> argument does not affect the value
     * that will be assigned, only the variable name. The value is determined by
     * the context.</p>
     */
    public OBFLExpressionBase(String exp, ExpressionFactory ef, OBFLVariable... variables) {
        this.ef = ef;
        this.exp = exp;
        final Set<OBFLVariable> variablesSeen = new HashSet<>();
        for (OBFLVariable v : variables) {
            switch (v) {
            case PAGE_NUMBER:
                this.pageNumberVariableName = PAGE_NUMBER_VARIABLE_NAME;
                break;
            case VOLUME_NUMBER:
                this.volumeNumberVariableName = VOLUME_NUMBER_VARIABLE_NAME;
                break;
            case VOLUME_COUNT:
                this.volumeCountVariableName = VOLUME_COUNT_VARIABLE_NAME;
                break;
            case STARTED_VOLUME_NUMBER:
                this.metaVolumeNumberVariableName = STARTED_VOLUME_NUMBER_VARIABLE_NAME;
                break;
            case STARTED_PAGE_NUMBER:
                if (variablesSeen.contains(OBFLVariable.STARTED_VOLUME_FIRST_CONTENT_PAGE_NUMBER)) {
                    throw new IllegalArgumentException(
                        "STARTED_PAGE_NUMBER and STARTED_VOLUME_FIRST_CONTENT_PAGE_NUMBER " +
                        "may not both be used in the same expression.");
                }
                this.metaPageNumberVariableName = STARTED_PAGE_NUMBER_VARIABLE_NAME;
                break;
            case STARTED_VOLUME_FIRST_CONTENT_PAGE_NUMBER:
                if (variablesSeen.contains(OBFLVariable.STARTED_PAGE_NUMBER)) {
                    throw new IllegalArgumentException(
                        "STARTED_PAGE_NUMBER and STARTED_VOLUME_FIRST_CONTENT_PAGE_NUMBER " +
                        "may not both be used in the same expression.");
                }
                this.metaPageNumberVariableName = STARTED_VOLUME_FIRST_CONTENT_PAGE_NUMBER_VARIABLE_NAME;
                break;
            case SHEET_COUNT:
                this.sheetCountVariableName = SHEET_COUNT_VARIABLE_NAME;
                break;
            case VOLUME_SHEET_COUNT:
                this.volumeSheetCountVariableName = VOLUME_SHEET_COUNT_VARIABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException(); // coding error
            }
            variablesSeen.add(v);
        }
    }

    protected Map<String, String> buildArgs(Context context) {
        HashMap<String, String> variables = new HashMap<>();
        if (pageNumberVariableName != null) {
            variables.put(pageNumberVariableName, "" + context.getCurrentPage());
        }
        if (volumeNumberVariableName != null) {
            // Passing a default value for the case the current volume is not known. This is the
            // case during the preparation phase of the VolumeProvider. If we wouldn't pass a value,
            // the evaluation of an expression with "$volume" would fail. Passing the value "??"
            // would not work because $volume is expected to be a number, so e.g. arithmetic
            // operations can be applied to it.
            variables.put(
                volumeNumberVariableName,
                context.getCurrentVolume() == null ? "0" : ("" + context.getCurrentVolume())
            );
        }
        if (volumeCountVariableName != null) {
            variables.put(volumeCountVariableName, "" + context.getVolumeCount());
        }
        // The meta variables below are only available in a meta-context. If
        // they are used incorrectly in a context where the meta-context is
        // unavailable, then they evaluate to null, which may result for
        // instance in the literal text "null" to appear in the content, without
        // warning or error.
        // TODO: Fix this issue.
        if (metaVolumeNumberVariableName != null) {
            variables.put(metaVolumeNumberVariableName, "" + context.getMetaVolume());
        }
        if (metaPageNumberVariableName != null) {
            variables.put(metaPageNumberVariableName, "" + context.getMetaPage());
        }
        if (sheetCountVariableName != null) {
            variables.put(sheetCountVariableName, "" + context.getSheetsInDocument());
        }
        if (volumeSheetCountVariableName != null) {
            variables.put(volumeSheetCountVariableName, "" + context.getSheetsInVolume());
        }
        return variables;
    }

}
