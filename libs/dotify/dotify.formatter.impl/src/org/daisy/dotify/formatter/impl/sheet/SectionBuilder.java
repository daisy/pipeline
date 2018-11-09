package org.daisy.dotify.formatter.impl.sheet;

import java.util.List;
import java.util.Stack;

import org.daisy.dotify.api.writer.SectionProperties;
import org.daisy.dotify.formatter.impl.page.PageImpl;
import org.daisy.dotify.formatter.impl.writer.Section;

public class SectionBuilder {
    private Stack<Section> ret = new Stack<Section>();
    private SectionProperties currentProps = null;
    private int sheets = 0;

    public void addSheet(Sheet s) {
        sheets++;
        /* We're using object identity here to communicate requests for 
         * new sections. It is left over from a previous cleanup, and 
         * might not be very intuitive, but it have to do for now.
         * Please improve if you wish.
         */
        if (ret.isEmpty() || currentProps!=s.getSectionProperties()) {
            currentProps = s.getSectionProperties();
            ret.add(new SectionImpl(currentProps));
        }
        SectionImpl sect = ((SectionImpl)ret.peek()); 
        for (PageImpl p : s.getPages()) {
        	sect.addPage(p);
        }
    }
    
    List<Section> getSections() {
        return ret;
    }
    
    int getSheetCount() {
        return sheets;
    }

}