package org.daisy.dotify.formatter.test;

import java.io.IOException;

import org.daisy.dotify.api.engine.FormatterEngineMaker;
import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.writer.MediaTypes;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMaker;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class TakenFromDP2Test extends AbstractFormatterEngineTest {
	
	@Test
	public void testPageNumber() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/page-number-input.obfl",
		        "resource-files/dp2/page-number-expected.pef", false);
	}
	@Ignore // issue with cross-referencing between sequences
	        // (https://github.com/joeha480/dotify/issues/97)
	@Test
	public void testPageNumberReferenceOtherSequence() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/page-number-reference-other-sequence-input.obfl",
		        "resource-files/dp2/page-number-reference-other-sequence-expected.pef", false);
	}
	@Test
	public void testPageNumberFollowsLeader() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/page-number-follows-leader-input.obfl",
		        "resource-files/dp2/page-number-follows-leader-expected.pef", false);
	}
	@Test
	public void testNestedBlocksWithBorders() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/nested-blocks-with-borders-input.obfl",
		        "resource-files/dp2/nested-blocks-with-borders-expected.pef", false);
	}
	@Test
	public void testBorderAlignCenter() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/border-align-center-input.obfl",
		        "resource-files/dp2/border-align-center-expected.pef", false);
	}
	@Test
	public void testVerticalPositionAlignBefore() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/vertical-position-align-before-input.obfl",
		        "resource-files/dp2/vertical-position-align-before-expected.pef", false);
	}
	@Test
	public void testVerticalPositionAlignBeforeNestedBlocks() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/vertical-position-align-before-nested-blocks-input.obfl",
		        "resource-files/dp2/vertical-position-align-before-nested-blocks-expected.pef", false);
	}
	@Test
	public void testLayoutMasterRowSpacing() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/layout-master-row-spacing-input.obfl",
		        "resource-files/dp2/layout-master-row-spacing-expected.pef", false);
	}
	@Test
	public void testBlockRowSpacing() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/block-row-spacing-input.obfl",
		        "resource-files/dp2/block-row-spacing-expected.pef", false);
	}
	@Test
	public void testRowSpacingAndMargins() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/row-spacing-and-margins-input.obfl",
		        "resource-files/dp2/row-spacing-and-margins-expected.pef", false);
	}
	@Test
	public void testRowSpacingAndCollapsingMargins() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/row-spacing-and-collapsing-margins-input.obfl",
		        "resource-files/dp2/row-spacing-and-collapsing-margins-expected.pef", false);
	}
	@Test
	public void testHeaderRowSpacing() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/header-row-spacing-input.obfl",
		        "resource-files/dp2/header-row-spacing-expected.pef", false);
	}
	@Test
	public void testKeepAll() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/keep-all-input.obfl",
		        "resource-files/dp2/keep-all-expected.pef", false);
	}
	@Test
	public void testKeepWithNext() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/keep-with-next-input.obfl",
		        "resource-files/dp2/keep-with-next-expected.pef", false);
	}
	@Test
	public void testKeepSeveralBlocks() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/keep-several-blocks-input.obfl",
		        "resource-files/dp2/keep-several-blocks-expected.pef", false);
	}
	@Test
	public void testKeepWithFollowingSiblingOfParent() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/keep-with-following-sibling-of-parent-input.obfl",
		        "resource-files/dp2/keep-with-following-sibling-of-parent-expected.pef", false);
	}
	@Test
	public void testPreAndPostContent() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/pre-and-post-content-input.obfl",
		        "resource-files/dp2/pre-and-post-content-expected.pef", false);
	}
	@Test
	public void testPreContentUseWhen() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/pre-content-use-when-input.obfl",
		        "resource-files/dp2/pre-content-use-when-expected.pef", false);
	}
	@Test
	public void testKeepContentWithBorder() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/keep-content-with-border-input.obfl",
		        "resource-files/dp2/keep-content-with-border-expected.pef", false);
	}
	@Test
	public void testOrphansWidowsCountingAndBorders() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/orphans-widows-counting-and-borders-input.obfl",
		        "resource-files/dp2/orphans-widows-counting-and-borders-expected.pef", false);
	}
	@Test
	public void testPageBreakingAndBorders() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/page-breaking-and-borders-input.obfl",
		        "resource-files/dp2/page-breaking-and-borders-expected.pef", false);
	}
	@Ignore // see https://github.com/joeha480/dotify/issues/117
	@Test
	public void testWhiteSpaceNormalizationAroundMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/white-space-normalization-around-marker-input.obfl",
		        "resource-files/dp2/white-space-normalization-around-marker-expected.pef", false);
	}
	@Ignore // white space around comment is dropped
	@Test
	public void testCommentInWhiteSpace() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/comment-in-white-space-input.obfl",
		        "resource-files/dp2/comment-in-white-space-expected.pef", false);
	}
	@Test
	public void testMarkerReferenceSequenceBackward() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/marker-reference-sequence-backward-input.obfl",
		        "resource-files/dp2/marker-reference-sequence-backward-expected.pef", false);
	}
	@Test
	public void testMarkerReferencePageForwardBackward() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/marker-reference-page-forward-backward-input.obfl",
		        "resource-files/dp2/marker-reference-page-forward-backward-expected.pef", false);
	}
	@Test
	public void testMarkerReferencePageContentForwardBackward() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/marker-reference-page-content-forward-backward-input.obfl",
		        "resource-files/dp2/marker-reference-page-content-forward-backward-expected.pef", false);
	}
	@Test
	public void testMarkerReferenceAcrossSequenceWorkaround() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/marker-reference-across-sequence-workaround-input.obfl",
		        "resource-files/dp2/marker-reference-across-sequence-workaround-expected.pef", false);
	}
	@Test
	public void testMarkerReferenceSpreadAcrossSequence() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/marker-reference-spread-across-sequence-input.obfl",
		        "resource-files/dp2/marker-reference-spread-across-sequence-expected.pef", false);
	}
	@Test
	public void testMarkerReferenceSpreadAcrossVolume() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/marker-reference-spread-across-volume-input.obfl",
		        "resource-files/dp2/marker-reference-spread-across-volume-expected.pef", false);
	}
	@Test
	public void testMarkerReferenceStartOffsetFirstPage() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/marker-reference-start-offset-first-page-input.obfl",
		        "resource-files/dp2/marker-reference-start-offset-first-page-expected.pef", false);
	}
	@Test
	public void testMarkerReferencePageFirstWorkaround() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/marker-reference-page-first-workaround-input.obfl",
		        "resource-files/dp2/marker-reference-page-first-workaround-expected.pef", false);
	}
	@Test
	public void testMarkerReferencePageStartWorkaround() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/marker-reference-page-start-workaround-input.obfl",
		        "resource-files/dp2/marker-reference-page-start-workaround-expected.pef", false);
	}
	@Test
	public void testMarkerReferenceSpreadStartWorkaround() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/marker-reference-spread-start-workaround-input.obfl",
		        "resource-files/dp2/marker-reference-spread-start-workaround-expected.pef", false);
	}
	@Ignore // position of middle field of header/footer depends on width of fields in
	        // left and right corner, so centering is not perfect
	@Test
	public void testFooterMiddleFieldCentering() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/footer-middle-field-centering-input.obfl",
		        "resource-files/dp2/footer-middle-field-centering-expected.pef", false);
	}
	@Test
	public void testVariousPageWidths() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/various-page-widths-input.obfl",
		        "resource-files/dp2/various-page-widths-expected.pef", false);
	}
	@Test
	public void testCurrentPageVariousPositions() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/current-page-various-positions-input.obfl",
		        "resource-files/dp2/current-page-various-positions-expected.pef", false);
	}
	@Test
	public void testCurrentPageVariousNumberFormats() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/current-page-various-number-formats-input.obfl",
		        "resource-files/dp2/current-page-various-number-formats-expected.pef", false);
	}
	@Test
	public void testPageNumberBackwardReference() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/page-number-backward-reference-input.obfl",
		        "resource-files/dp2/page-number-backward-reference-expected.pef", false);
	}
	@Test
	public void testPageNumberReferenceSpan() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/page-number-reference-span-input.obfl",
		        "resource-files/dp2/page-number-reference-span-expected.pef", false);
	}
	@Test
	public void testPreHyphenation() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/pre-hyphenation-input.obfl",
		        "resource-files/dp2/pre-hyphenation-expected.pef", false);
	}
	@Test
	public void testCollapsingMarginsAdjacentBlocks() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/collapsing-margins-adjacent-blocks-input.obfl",
		        "resource-files/dp2/collapsing-margins-adjacent-blocks-expected.pef", false);
	}
	@Test
	public void testCollapsingMarginsEmptyBlock() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/collapsing-margins-empty-block-input.obfl",
		        "resource-files/dp2/collapsing-margins-empty-block-expected.pef", false);
	}
	@Test
	public void testRowSpacingNestedBlocks() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/row-spacing-nested-blocks-input.obfl",
		        "resource-files/dp2/row-spacing-nested-blocks-expected.pef", false);
	}
	@Test
	public void testSingleLineHeaderEmpty() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/single-line-header-empty-input.obfl",
		        "resource-files/dp2/single-line-header-empty-expected.pef", false);
	}
	@Test
	public void testMultiLineHeaderEmpty() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/multi-line-header-empty-input.obfl",
		        "resource-files/dp2/multi-line-header-empty-expected.pef", false);
	}
	@Test
	public void testSingleLineHeader() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/single-line-header-input.obfl",
		        "resource-files/dp2/single-line-header-expected.pef", false);
	}
	@Test
	public void testMultiLineHeaderPartlyEmpty() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/multi-line-header-partly-empty-input.obfl",
		        "resource-files/dp2/multi-line-header-partly-empty-expected.pef", false);
	}
	@Test
	public void testHeaderVaryHeightLeftRightPages() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/header-vary-height-left-right-pages-input.obfl",
		        "resource-files/dp2/header-vary-height-left-right-pages-expected.pef", false);
	}
	@Test
	public void testEmptyBlock() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/empty-block-input.obfl",
		        "resource-files/dp2/empty-block-expected.pef", false);
	}
	@Test
	public void testEmptyBlockBorder() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/empty-block-border-input.obfl",
		        "resource-files/dp2/empty-block-border-expected.pef", false);
	}
	@Test
	public void testSingleLineFooterEmpty() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/single-line-footer-empty-input.obfl",
		        "resource-files/dp2/single-line-footer-empty-expected.pef", false);
	}
	@Test
	public void testMultiLineFooterEmpty() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/multi-line-footer-empty-input.obfl",
		        "resource-files/dp2/multi-line-footer-empty-expected.pef", false);
	}
	@Test
	public void testSingleLineFooter() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/single-line-footer-input.obfl",
		        "resource-files/dp2/single-line-footer-expected.pef", false);
	}
	@Test
	public void testMultiLineFooterPartlyEmpty() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/multi-line-footer-partly-empty-input.obfl",
		        "resource-files/dp2/multi-line-footer-partly-empty-expected.pef", false);
	}
	@Test
	public void testMultiLineFooter() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/multi-line-footer-input.obfl",
		        "resource-files/dp2/multi-line-footer-expected.pef", false);
	}
	@Test
	public void testPreContent() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/pre-content-input.obfl",
		        "resource-files/dp2/pre-content-expected.pef", false);
	}
	@Test
	public void testSheetsInVolumeMax() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/sheets-in-volume-max-input.obfl",
		        "resource-files/dp2/sheets-in-volume-max-expected.pef", false);
	}
	@Test
	public void testVolumeTemplateVariousSheetsInVolumeMax() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/volume-template-various-sheets-in-volume-max-input.obfl",
		        "resource-files/dp2/volume-template-various-sheets-in-volume-max-expected.pef", false);
	}
	@Test
	public void testSheetCalculationInitialPageNumber() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/sheet-calculation-initial-page-number-input.obfl",
		        "resource-files/dp2/sheet-calculation-initial-page-number-expected.pef", false);
	}
	@Test
	public void testMarkerReferenceSpreadInitialPageNumber() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/marker-reference-spread-initial-page-number-input.obfl",
		        "resource-files/dp2/marker-reference-spread-initial-page-number-expected.pef", false);
	}
	@Test
	public void testSheetsInVolume() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/sheets-in-volume-input.obfl",
		        "resource-files/dp2/sheets-in-volume-expected.pef", false);
	}
	@Test
	public void testTocSequenceInPostContent() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/toc-sequence-in-post-content-input.obfl",
		        "resource-files/dp2/toc-sequence-in-post-content-expected.pef", false);
	}
	@Test
	public void testBlockUnderline() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/block-underline-input.obfl",
		        "resource-files/dp2/block-underline-expected.pef", false);
	}
	@Test
	public void testBlockUnderlineStyles() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/block-underline-styles-input.obfl",
		        "resource-files/dp2/block-underline-styles-expected.pef", false);
	}
	@Ignore // undefined behavior
	public void testBlockUnderlineWithChildBlock() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/block-underline-with-child-block-input.obfl",
		        "resource-files/dp2/block-underline-with-child-block-expected.pef", false);
	}
	@Ignore // see https://github.com/joeha480/dotify/issues/194
	@Test
	public void testMarkerIndicatorBeforeBlock() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/marker-indicator-before-block-input.obfl",
		        "resource-files/dp2/marker-indicator-before-block-expected.pef", false);
	}
	@Ignore // see https://github.com/joeha480/dotify/issues/195
	@Test
	public void testMarginRegionAndBorders() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/margin-region-and-borders-input.obfl",
		        "resource-files/dp2/margin-region-and-borders-expected.pef", false);
	}
	@Test
	public void testRowSpacingAndFooter() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/row-spacing-and-footer-input.obfl",
		        "resource-files/dp2/row-spacing-and-footer-expected.pef", false);
	}
	@Test
	public void testManualVolumeBreakingTOC() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/manual-volume-breaking-toc-input.obfl",
		        "resource-files/dp2/manual-volume-breaking-toc-expected.pef", false);
	}

	@Test
	public void testManualAndAutomaticVolumeBreaking() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/manual-and-automatic-volume-breaking-input.obfl",
		        "resource-files/dp2/manual-and-automatic-volume-breaking-expected.pef", false);
	}
	@Test
	public void testMarginRegionAndBorder() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/margin-region-and-border-input.obfl",
		        "resource-files/dp2/margin-region-and-border-expected.pef", false);
	}
	@Test // see https://github.com/nlbdev/pipeline/issues/147
	public void testTableIssue147() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/table-issue-147-input.obfl",
		        "resource-files/dp2/table-issue-147-expected.pef", false);
	}
	@Test
	public void testVolumeBreakingBetweenBlocks() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/volume-breaking-between-blocks-input.obfl",
		        "resource-files/dp2/volume-breaking-between-blocks-expected.pef", false);
	}
	@Ignore // see https://github.com/brailleapps/dotify.formatter.impl/issues/46
	@Test
	public void testVolumeBreakingBetweenWords() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF(
			FormatterEngineMaker.newInstance().getFactory().newFormatterEngine(
				new FormatterConfiguration.Builder("sv-SE", BrailleTranslatorFactory.MODE_UNCONTRACTED)
				.allowsEndingVolumeOnHyphen(false)
				.build(),
				PagedMediaWriterFactoryMaker.newInstance().newPagedMediaWriter(MediaTypes.PEF_MEDIA_TYPE)),
			"resource-files/dp2/volume-breaking-between-words-input.obfl",
			"resource-files/dp2/volume-breaking-between-words-expected.pef",
			null
		);
	}
	@Test
	public void testBreakBeforeSheet() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/break-before-sheet-input.obfl",
		        "resource-files/dp2/break-before-sheet-expected.pef", false);
	}
}
