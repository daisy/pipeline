package org.daisy.pipeline.word_to_dtbook.impl;

import java.util.List;

public class PageStylesValidator {

	protected PageStyle _lastStyle = PageStyle.Empty;

	public ValidationResult ValidateParagraph(List<PageStyle> paragraphStyles) {
		ValidationResult result = new ValidationResult();
		if (paragraphStyles.size() != 0) {
			if (paragraphStyles.size() > 1) {
				result = new ValidationResult(
					"More than one page style applied to paragraph, please applay only"
					+ "one style from Frontmatter (DAISY), Bodymatter (DAISY) and Rearmatter (DAISY)");
				_lastStyle = paragraphStyles.get(paragraphStyles.size() - 1);
			} else {
				PageStyle currentStyle = paragraphStyles.get(0);
				switch (_lastStyle) {
				case Empty:
					if (currentStyle == PageStyle.Rearmatter) {
						result = new ValidationResult(
							"Rearmatter (Daisy) can be applied only after Bodymetter (DAISY) style");
					}
					break;
				case Frontmatter:
					if (currentStyle == PageStyle.Frontmatter) {
							result = new ValidationResult(
								"Frontmatter (DAISY) style should be applied only one time");
					} else if (currentStyle == PageStyle.Rearmatter) {
						result = new ValidationResult(
							"Rearmatter (DAISY) style can be applied only after Bodymatter (DAISY) style");
					}
					break;
				case Bodymatter:
					if (currentStyle == PageStyle.Frontmatter) {
						result = new ValidationResult(
							"Frontmatter (DAISY) style can not be applied after Bodymatter (DAISY) style");
					} else if (currentStyle == PageStyle.Bodymatter) {
						result = new ValidationResult(
							"Bodymatter (DAISY) style should be applied only one time");
					}
					break;
				case Rearmatter:
					result = new ValidationResult(
						"Frontmatter (DAISY), Bodymatter (DAISY) and Rearmatter (DAISY) styles "
						+ "can not be applied after Rearmatter (DAISY) style");
					break;
				default:
					throw new RuntimeException();
				}
				_lastStyle = currentStyle;
			}
		}
		return result;
	}

	public ValidationResult ValidateLastStyle() {
		if (_lastStyle == PageStyle.Frontmatter)
			return new ValidationResult(
				"Frontmatter (DAISY) can not be applied without Bodymatter (DAISY) style ");
		return new ValidationResult();
	}

	public static PageStyle GetPageStyle(String style) {
		if (style.startsWith("Frontmatter"))
			return PageStyle.Frontmatter;
		else if (style.startsWith("Bodymatter"))
			return PageStyle.Bodymatter;
		else if (style.startsWith("Rearmatter"))
			return PageStyle.Rearmatter;
		return PageStyle.Empty;
	}
}
