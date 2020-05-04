package org.daisy.dotify.formatter.impl.core;

import org.daisy.dotify.api.formatter.Condition;
import org.daisy.dotify.api.formatter.Field;
import org.daisy.dotify.api.formatter.FieldList;
import org.daisy.dotify.api.formatter.MarginRegion;
import org.daisy.dotify.api.formatter.NoField;
import org.daisy.dotify.api.formatter.PageTemplateBuilder;
import org.daisy.dotify.formatter.impl.search.DefaultContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * Specifies page objects such as header and footer
 * for the pages to which it applies.
 *
 * @author Joel Håkansson
 */
public class PageTemplate implements PageTemplateBuilder {
    private final Condition condition;
    private final List<FieldList> header;
    private final List<FieldList> footer;
    private final List<MarginRegion> leftMarginRegion;
    private final List<MarginRegion> rightMarginRegion;
    private final HashMap<Integer, Boolean> appliesTo;
    private final float defaultRowSpacing;
    private Float headerHeight;
    private Float footerHeight;
    private Integer flowIntoHeaderHeight;
    private Integer flowIntoFooterHeight;
    private Integer totalMarginRegion;

    PageTemplate(float rowSpacing) {
        this(null, rowSpacing);
    }

    /**
     * Create a new SimpleTemplate.
     *
     * @param condition  condition to evaluate.
     * @param rowSpacing the default row spacing
     */
    PageTemplate(Condition condition, float rowSpacing) {
        this.condition = condition;
        this.header = new ArrayList<>();
        this.footer = new ArrayList<>();
        this.leftMarginRegion = new ArrayList<>();
        this.rightMarginRegion = new ArrayList<>();
        this.appliesTo = new HashMap<>();
        this.defaultRowSpacing = rowSpacing;
    }

    @Override
    public void addToHeader(FieldList obj) {
        // reset the cached values
        headerHeight = null;
        flowIntoHeaderHeight = null;
        header.add(obj);
    }

    @Override
    public void addToFooter(FieldList obj) {
        // reset the cached values
        footerHeight = null;
        flowIntoFooterHeight = null;
        footer.add(obj);
    }


    /**
     * Gets header rows for a page using this Template. Each FieldList must
     * fit within a single row, i.e. the combined length of all resolved strings in each FieldList must
     * be smaller than the flow width. Keep in mind that text filters will be applied to the
     * resolved string, which could affect its length.
     *
     * @return returns a List of FieldList
     */
    public List<FieldList> getHeader() {
        return header;
    }

    public float getHeaderHeight() {
        if (headerHeight == null) {
            headerHeight = getHeight(header, defaultRowSpacing);
        }
        return headerHeight;
    }

    /**
     * Gets footer rows for a page using this Template. Each FieldList must
     * fit within a single row, i.e. the combined length of all resolved strings in each FieldList must
     * be smaller than the flow width. Keep in mind that text filters will be applied to the
     * resolved string, which could affect its length.
     *
     * @return returns a List of FieldList
     */
    public List<FieldList> getFooter() {
        return footer;
    }

    float getFooterHeight() {
        if (footerHeight == null) {
            footerHeight = getHeight(footer, defaultRowSpacing);
        }
        return footerHeight;
    }

    /**
     * Tests if this Template applies to this pagenum.
     *
     * @param pagenum the pagenum to test
     * @return returns true if the Template should be applied to the page
     */
    boolean appliesTo(int pagenum) {
        if (condition == null) {
            return true;
        }
        // keep a HashMap with calculated results
        if (appliesTo.containsKey(pagenum)) {
            return appliesTo.get(pagenum);
        }
        boolean applies = condition.evaluate(new DefaultContext.Builder(null).currentPage(pagenum).build());
        appliesTo.put(pagenum, applies);
        return applies;
    }

    @Override
    public void addToLeftMargin(MarginRegion margin) {
        totalMarginRegion = null;
        leftMarginRegion.add(margin);
    }

    @Override
    public void addToRightMargin(MarginRegion margin) {
        totalMarginRegion = null;
        rightMarginRegion.add(margin);
    }

    public List<MarginRegion> getLeftMarginRegion() {
        return leftMarginRegion;
    }

    public List<MarginRegion> getRightMarginRegion() {
        return rightMarginRegion;
    }

    public int validateAndAnalyzeHeader() {
        // do the analyzing lazily
        if (flowIntoHeaderHeight == null) {
            flowIntoHeaderHeight = validateAndAnalyzeHeaderFooter(true);
        }
        return flowIntoHeaderHeight;
    }

    public int validateAndAnalyzeFooter() {
        // do the analyzing lazily
        if (flowIntoFooterHeight == null) {
            flowIntoFooterHeight = validateAndAnalyzeHeaderFooter(false);
        }
        return flowIntoFooterHeight;
    }

    private int validateAndAnalyzeHeaderFooter(boolean header) {
        List<FieldList> rows;
        if (header) {
            rows = new ArrayList<>(getHeader());
            Collections.reverse(rows);
        } else {
            rows = getFooter();
        }
        int j = 0;
        int height = 0;
        for (FieldList row : rows) {
            int k = 0;
            boolean hasEmptyField = false;
            for (Field f : row.getFields()) {
                if (f instanceof NoField) {
                    if (hasEmptyField) {
                        throw new RuntimeException("At most one <field allow-text-flow=\"true\"/> allowed.");
                    } else {
                        hasEmptyField = true;
                    }
                }
                k++;
            }
            if (hasEmptyField) {
                if (k == 1) {
                    throw new RuntimeException(
                        "<field allow-text-flow=\"true\"/> does not make sense as single child."
                    );
                }
                float rowSpacing;
                if (row.getRowSpacing() != null) {
                    rowSpacing = row.getRowSpacing();
                } else {
                    rowSpacing = this.defaultRowSpacing;
                }
                if (rowSpacing != 1.0f) {
                    throw new RuntimeException(
                        "<field allow-text-flow=\"true\"/> only allowed when row-spacing is '1'."
                    );
                }
                if (height == j) {
                    height++;
                } else {
                    throw new RuntimeException("<field allow-text-flow=\"true\"/> only allowed if all "
                            + (header ? "<header/> below" : "<footer/> above")
                            + " have an <field allow-text-flow=\"true\"/> as well.");
                }
            }
            j++;
        }
        return height;
    }

    private static float getHeight(List<FieldList> list, float def) {
        return (float) list.stream().mapToDouble(f -> f.getRowSpacing() != null ? f.getRowSpacing() : def).sum();
    }

    public int getTotalMarginRegionWidth() {
        if (totalMarginRegion == null) {
            totalMarginRegion = getLeftMarginRegion().stream().mapToInt(mr -> mr.getWidth()).sum()
                    + getRightMarginRegion().stream().mapToInt(mr -> mr.getWidth()).sum();
        }
        return totalMarginRegion;
    }

}
