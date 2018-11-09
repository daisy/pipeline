package org.daisy.dotify.formatter.impl.page;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.daisy.dotify.api.formatter.CompoundField;
import org.daisy.dotify.api.formatter.CurrentPageField;
import org.daisy.dotify.api.formatter.Field;
import org.daisy.dotify.api.formatter.FieldList;
import org.daisy.dotify.api.formatter.MarkerReferenceField;
import org.daisy.dotify.api.formatter.NoField;
import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.formatter.impl.core.FormatterContext;
import org.daisy.dotify.formatter.impl.core.LayoutMaster;
import org.daisy.dotify.formatter.impl.core.PageTemplate;
import org.daisy.dotify.formatter.impl.core.PaginatorException;
import org.daisy.dotify.formatter.impl.row.RowImpl;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
import org.daisy.dotify.formatter.impl.search.PageDetails;

class FieldResolver implements PageShape {
	private static final Pattern softHyphen = Pattern.compile("\u00ad");
	private final LayoutMaster master;
	private final FormatterContext fcontext;
	private final CrossReferenceHandler crh;
	private final PageDetails detailsTemplate;

	FieldResolver(LayoutMaster master, FormatterContext fcontext, CrossReferenceHandler crh, PageDetails detailsTemplate) {
		this.master = master;
		this.fcontext = fcontext;
		this.crh = crh;
		this.detailsTemplate = detailsTemplate;
	}
	
    List<RowImpl> renderFields(PageDetails p, List<FieldList> fields, BrailleTranslator translator) throws PaginatorException {
        ArrayList<RowImpl> ret = new ArrayList<>();
		for (FieldList row : fields) {
            ret.add(renderField(p, row, translator));
		}
		return ret;
	}
    
    RowImpl renderField(PageDetails p, FieldList field, BrailleTranslator translator) throws PaginatorException {
    	try {
            return new RowImpl.Builder(distribute(p, field, master.getFlowWidth(), fcontext.getSpaceCharacter()+"", translator))
            		.rowSpacing(field.getRowSpacing())
            		.build();
        } catch (PaginatorToolsException e) {
            throw new PaginatorException("Error while rendering header", e);
		}
    }
    
    private List<String> resolveField(PageDetails p, FieldList chunks, int width, String padding, BrailleTranslator translator) throws PaginatorToolsException {
		ArrayList<String> chunkF = new ArrayList<>();
		for (Field f : chunks.getFields()) {
			DefaultTextAttribute.Builder b = new DefaultTextAttribute.Builder(null);
            String resolved = softHyphen.matcher(resolveField(f, p, b)).replaceAll("");
			Translatable.Builder tr = Translatable.text(fcontext.getConfiguration().isMarkingCapitalLetters()?resolved:resolved.toLowerCase()).
										hyphenate(false);
			if (resolved.length()>0) {
				tr.attributes(b.build(resolved.length()));
			}
			try {
				chunkF.add(translator.translate(tr.build()).getTranslatedRemainder());
			} catch (TranslationException e) {
				throw new PaginatorToolsException(e);
			}
		}
		return chunkF;
    }
	
    private String distribute(PageDetails p, FieldList chunks, int width, String padding, BrailleTranslator translator) throws PaginatorToolsException {
    	List<String> chunkF = resolveField(p, chunks, width, padding, translator);
        return PaginatorTools.distribute(chunkF, width, padding,
                fcontext.getConfiguration().isAllowingTextOverflowTrimming()?
                PaginatorTools.DistributeMode.EQUAL_SPACING_TRUNCATE:
                PaginatorTools.DistributeMode.EQUAL_SPACING
            );
	}
	
	/*
	 * Note that the result of this function is not constant because getPageInSequenceWithOffset(),
	 * getPageInVolumeWithOffset() and shouldAdjustOutOfBounds() are not constant.
	 */
	private String resolveField(Field field, PageDetails p, DefaultTextAttribute.Builder b) {
		if (field instanceof NoField) {
			return "";
		}
		String ret;
		DefaultTextAttribute.Builder b2 = new DefaultTextAttribute.Builder(field.getTextStyle());
		if (field instanceof CompoundField) {
			ret = resolveCompoundField((CompoundField)field, p, b2);
		} else if (field instanceof MarkerReferenceField) {
			ret = crh.findMarker(p.getPageId(), (MarkerReferenceField)field);
		} else if (field instanceof CurrentPageField) {
			ret = resolveCurrentPageField((CurrentPageField)field, p);
		} else {
			ret = field.toString();
		}
		if (ret.length()>0) {
			b.add(b2.build(ret.length()));
		}
		return ret;
	}

	private String resolveCompoundField(CompoundField f, PageDetails p, DefaultTextAttribute.Builder b) {
		return f.stream().map(f2 -> resolveField(f2, p, b)).collect(Collectors.joining());
	}
	
	private static String resolveCurrentPageField(CurrentPageField f, PageDetails p) {
		int pagenum = p.getPageNumber();
		return f.getNumeralStyle().format(pagenum);
	}

	@Override
	public int getWidth(int pagenum, int rowOffset) {
		while (true) {
			// Iterates until rowOffset is less than the height of the page.
			// Since each page could potentially have a different flow height we cannot
			// simply divide, we have to retrieve the page template for each page
			// and look at the actual value...
			PageTemplate p = master.getTemplate(pagenum);
			int flowHeight = master.getFlowHeight(p);
			if (rowOffset>flowHeight) {
				if (flowHeight<=0) {
					throw new RuntimeException("Error in code.");
				}
				// subtract the height of the page we're on
				rowOffset-=flowHeight;
				// move to the next page
				pagenum++;
			} else {
				break;
			}
		}
		return getWidth(detailsTemplate.with(pagenum-1), rowOffset);
	}

	int getWidth(PageDetails details, int rowOffset) {
		PageTemplate p = master.getTemplate(details.getPageNumber());
		int flowHeader = p.validateAndAnalyzeHeader();
		int flowFooter = p.validateAndAnalyzeFooter();
		if (flowHeader+flowFooter>0) {
			int flowHeight = master.getFlowHeight(p);
			rowOffset = rowOffset % flowHeight;
			if (rowOffset<flowHeader) {
				//this is a shared row
				int start = p.getHeader().size()-flowHeader;
				return getAvailableForNoField(details, p.getHeader().get(start+rowOffset));
			} else if (rowOffset>=flowHeight-flowFooter) {
				//this is a shared row
				int rowsLeftOnPage = flowHeight-rowOffset;
				return getAvailableForNoField(details, p.getFooter().get(flowFooter-rowsLeftOnPage));
			} else {
				return master.getFlowWidth();
			}
		} else {
			return master.getFlowWidth();
		}
	}

	private int getAvailableForNoField(PageDetails details, FieldList list) {
		try {
			List<String> parts = resolveField(details, list, master.getFlowWidth(), fcontext.getSpaceCharacter()+"", fcontext.getDefaultTranslator());
			int size = parts.stream().mapToInt(str -> str.length()).sum();
			return master.getFlowWidth()-size;
		} catch (PaginatorToolsException e) {
			throw new RuntimeException("", e);
		}
	}
}
