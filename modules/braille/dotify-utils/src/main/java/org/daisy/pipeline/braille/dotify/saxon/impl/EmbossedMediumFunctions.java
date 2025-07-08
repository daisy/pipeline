package org.daisy.pipeline.braille.dotify.saxon.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;
import org.daisy.dotify.api.paper.Area;
import org.daisy.pipeline.braille.common.util.Strings;
import org.daisy.pipeline.braille.css.xpath.Style;
import org.daisy.pipeline.braille.pef.BrailleFileFormat;
import org.daisy.pipeline.css.Dimension;
import org.daisy.pipeline.css.Dimension.RelativeDimensionBase;
import org.daisy.pipeline.css.Dimension.Unit;
import org.daisy.pipeline.css.Medium;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbossedMediumFunctions {

	/**
	 * Adjust a page style so that it represents the printable area of the page (or, depending on
	 * the width and height that was specified, an area within the printable area).
	 */
	public static Style getPrintablePageStyle(Optional<BrailleFileFormat> medium, Optional<Style> pageStyle) {
		BrailleFileFormat m = medium.orElse(null);
		if (m != null && !pageStyle.isPresent()) {
			Area printable = m.getPrintableArea();
			return Style.get(parseStyle(String.format("@page { size: %d %d }",
			                                          (int)Math.floor(mmToCh(m, printable.getWidth())),
			                                          (int)Math.floor(mmToEm(m, printable.getHeight())))),
			                 "@page").get();
		} else if (m != null && !m.isOverflowAllowed()) { // && pageStyle.isPresent()
			Area printable = m.getPrintableArea();
			double printableWidth = printable.getWidth();
			double printableHeight = printable.getHeight();
			double innerUnprintable = printable.getOffsetX();
			double outerUnprintable = m.getDeviceWidth().toUnit(Unit.MM, m).getValue().doubleValue()
				- printableWidth - innerUnprintable;
			double topUnprintable = printable.getOffsetY();
			double bottomUnprintable = m.getDeviceHeight().toUnit(Unit.MM, m).getValue().doubleValue()
				- printableHeight - topUnprintable;
			Optional<Double[]> size = Style.get(pageStyle, "size")
				.map(x -> {
					String[] s = x.toString().trim().split("\\s+");
					Dimension w = parseDimension(s[0], true).toUnit(Unit.MM, m);
					Dimension h = parseDimension(s[1], false).toUnit(Unit.MM, m);
					return new Double[]{w.getValue().doubleValue(),
					                    h.getValue().doubleValue()}; });
			// It is assumed that if :left and :right styles exist, they have fully (except for the size property)
			// inherited from the main style (which happened in px:css-cascade). We ensure the same is true for the
			// returned style.
			Optional<Style> frontStyle = Style.get(pageStyle, "&:right");
			frontStyle = frontStyle.isPresent() ? frontStyle : pageStyle;
			Optional<Style> backStyle = Style.get(pageStyle, "&:left");
			backStyle = backStyle.isPresent() ? backStyle : pageStyle;
			// adapt margins of front and back pages
			Margin frontMarginSpecified = null;
			Margin backMarginSpecified = null;
			Margin frontMarginAdjusted = null;
			Margin backMarginAdjusted = null;
			List<Optional<Style>> frontMarginContentSpecified = null;
			List<Optional<Style>> backMarginContentSpecified = null;
			double adjustedWidth = printableWidth;
			double adjustedHeight = printableHeight;
			for (boolean frontSide : new Boolean[]{true, false}) {
				Optional<Style> style = frontSide ? frontStyle : backStyle;
				Margin specified = Margin.fromStyle(style, m);
				if (frontSide)
					frontMarginSpecified = specified;
				else
					backMarginSpecified = specified;
				// Normalize margin content by replacing white space with padding, and determine the minimum required
				// margin size to contain the margin content. Note that if a margin size was specified that can not
				// contain the specified content, the content will extend into the page area (and the formatter will
				// flow the text around it if possible). This could result in a minimum required size that is smaller
				// than 0.
				List<Optional<Style>> normalizedContent = new ArrayList<>();
				Margin minimum = specified; {
					double left = -9999.0;
					double right = -9999.0;
					double top = -9999.0;
					double bottom = -9999.0;
					for (String at : new String[]{"@left"}) {
						Optional<Style> area = Style.get(style, at);
						Padding padding = Padding.fromStyle(area, m);
						if (padding.right > 0 || padding.top > 0 || padding.bottom > 0)
							throw new IllegalArgumentException(
								String.format("No padding other than padding-left supported on %s, but got: %s", at, padding));
						Optional<Style> ws = Style.get(area, "white-space");
						boolean pre = ws.filter(x -> "pre-wrap".equals(x.toString())).isPresent();
						Iterator<Object> content = Style.iterate(Style.get(area, "content"));
						List<Optional<Style>> trimmed = new ArrayList<>();
						int leadingBlanks = 0; {
							content: while (content.hasNext()) {
								Object c = content.next();
								if (c instanceof String) {
									String s = (String)c;
									s = s.replace("\u200b", "");
									if (pre)
										s = s.replaceAll("[\\s\u2800]", "\u00a0");
									else
										s = s.replaceAll("[\\s\u2800]+", " ");
									Iterator<String> words = Arrays.asList(s.split("((?<=[ \u00a0]))")).iterator();
									while  (words.hasNext()) {
										String w = words.next();
										if (!w.matches("[ \u00a0]*")) {
											trimmed.add(asContentList(w + Strings.join(words, "")));
											break content;
										} else
											leadingBlanks += w.length();
									}
								} else {
									trimmed.add(Optional.of((Style)c));
									break;
								}
							}
							while (content.hasNext()) {
								Object c = content.next();
								if (c instanceof String)
									trimmed.add(asContentList((String)c));
								else
									trimmed.add(Optional.of((Style)c));
							}
						}
						padding = new Padding(0, 0, 0, padding.left + emToMm(m, leadingBlanks));
						area = Style.remove(area, ImmutableList.of("content",
						                                           "padding-left",
						                                           "padding-right",
						                                           "padding-top",
						                                           "padding-bottom").iterator());
						area = mergeStyle(area, padding.toStyle(m));
						area = Style.put(area, "content", mergeStyle(trimmed));
						normalizedContent.add(Style.of(at, area));
						left = Math.max(left, specified.left - padding.left);
					}
					for (String at : new String[]{"@right"}) {
						Optional<Style> area = Style.get(style, at);
						Padding padding = Padding.fromStyle(area, m);
						if (padding.left > 0 || padding.top > 0 || padding.bottom > 0)
							throw new IllegalArgumentException(
								String.format("No padding other than padding-right supported on %s, but got: %s", at, padding));
						Optional<Style> ws = Style.get(area, "white-space");
						boolean pre = ws.filter(x -> "pre-wrap".equals(x.toString())).isPresent();
						Iterator<Object> content = reverse(Style.iterate(Style.get(area, "content"))).iterator();
						List<Optional<Style>> trimmed = new ArrayList<>();
						int trailingBlanks = 0; {
							content: while (content.hasNext()) {
								Object c = content.next();
								if (c instanceof String) {
									String s = (String)c;
									s = s.replace("\u200b", "");
									if (pre)
										s = s.replaceAll("[\\s\u2800]", "\u00a0");
									else
										s = s.replaceAll("[\\s\u2800]+", " ");
									Iterator<String> words = reverse(s.split("((?<=[ \u00a0]))")).iterator();
									while (words.hasNext()) {
										String w = words.next();
										if (!w.matches("[ \u00a0]*")) {
											trimmed.add(asContentList(Strings.join(reverse(words), "") + w));
											break content;
										} else
											trailingBlanks += w.length();
									}
								} else {
									trimmed.add(Optional.of((Style)c));
									break;
								}
							}
							while (content.hasNext()) {
								Object c = content.next();
								if (c instanceof String)
									trimmed.add(asContentList((String)c));
								else
									trimmed.add(Optional.of((Style)c));
							}
						}
						padding = new Padding(0, padding.right + emToMm(m, trailingBlanks), 0, 0);
						area = Style.remove(area, ImmutableList.of("content",
						                                           "padding-left",
						                                           "padding-right",
						                                           "padding-top",
						                                           "padding-bottom").iterator());
						area = mergeStyle(area, padding.toStyle(m));
						area = Style.put(area, "content", mergeStyle(reverse(trimmed)));
						normalizedContent.add(Style.of(at, area));
						right = Math.max(right, specified.right - padding.right);
					}
					for (String at : new String[]{"@top-left", "@top-center", "@top-right"}) {
						Optional<Style> area = Style.get(style, at);
						Padding padding = Padding.fromStyle(area, m);
						if (padding.left > 0 || padding.right > 0 || padding.bottom > 0)
							throw new IllegalArgumentException(
								String.format("No padding other than padding-top supported on %s, but got: %s", at, padding));
						Optional<Style> ws = Style.get(area, "white-space");
						boolean preLine = ws.filter(x -> "pre-wrap".equals(x.toString()) ||
						                            "pre-line".equals(x.toString())).isPresent();
						boolean pre = preLine && ws.filter(x -> "pre-wrap".equals(x.toString())).isPresent();
						Iterator<Object> content = Style.iterate(Style.get(area, "content"));
						List<Optional<Style>> trimmed = new ArrayList<>();
						int leadingBlanks = 0; {
							String line = "";
							content: while (content.hasNext()) {
								Object c = content.next();
								if (c instanceof String) {
									String s = (String)c;
									s = s.replace("\u200b", "");
									if (preLine)
										s = s.replaceAll("\n", "\u2028");
									Iterator<String> lines = Arrays.asList(s.split("((?<=\u2028))")).iterator();
									while (lines.hasNext()) {
										String l = lines.next();
										if (!l.matches("[\\s\u00a0\u2028\u2800]*")) {
											trimmed.add(asContentList(line + l + Strings.join(lines, "")));
											line = "";
											break content;
										}
										if (l.endsWith("\u2028")) {
											leadingBlanks++;
											line = "";
										} else
											line = line + l;
									}
								} else {
									trimmed.add(asContentList(line));
									line = "";
									trimmed.add(Optional.of((Style)c));
									break;
								}
							}
							if (!line.isEmpty())
								if (pre || line.contains("\u00a0"))
									leadingBlanks++;
								else
									trimmed.add(asContentList(line));
							while (content.hasNext()) {
								Object c = content.next();
								if (c instanceof String)
									trimmed.add(asContentList((String)c));
								else
									trimmed.add(Optional.of((Style)c));
							}
						}
						padding = new Padding(padding.top + emToMm(m, leadingBlanks), 0, 0, 0);
						area = Style.remove(area, ImmutableList.of("content",
						                                           "padding-left",
						                                           "padding-right",
						                                           "padding-top",
						                                           "padding-bottom").iterator());
						area = mergeStyle(area, padding.toStyle(m));
						area = Style.put(area, "content", mergeStyle(trimmed));
						normalizedContent.add(Style.of(at, area));
						top = Math.max(top, specified.top - padding.top);
					}
					for (String at : new String[]{"@bottom-left", "@bottom-center", "@bottom-right"}) {
						Optional<Style> area = Style.get(style, at);
						Padding padding = Padding.fromStyle(area, m);
						if (padding.left > 0 || padding.right > 0 || padding.top > 0)
							throw new IllegalArgumentException(
								String.format("No padding other than padding-bottom supported on %s, but got: %s", at, padding));
						Optional<Style> ws = Style.get(area, "white-space");
						boolean preLine = ws.filter(x -> "pre-wrap".equals(x.toString()) ||
						                            "pre-line".equals(x.toString())).isPresent();
						boolean pre = preLine && ws.filter(x -> "pre-wrap".equals(x.toString())).isPresent();
						Iterator<Object> content = reverse(Style.iterate(Style.get(area, "content"))).iterator();
						List<Optional<Style>> trimmed = new ArrayList<>();
						int trailingBlanks = 0; {
							String line = "";
							content: while (content.hasNext()) {
								Object c = content.next();
								if (c instanceof String) {
									String s = (String)c;
									s = s.replace("\u200b", "");
									if (preLine)
										s = s.replaceAll("\n", "\u2028");
									Iterator<String> lines = reverse(s.split("((?<=\u2028))")).iterator();
									while (lines.hasNext()) {
										String l = lines.next();
										if (l.endsWith("\u2028")) {
											if (!line.isEmpty() && (pre || line.contains("\u00a0") || line.contains("\u2028")))
												trailingBlanks++;
											line = "";
										}
										if (!l.matches("[\\s\u00a0\u2028\u2800]*")) {
											trimmed.add(asContentList(Strings.join(reverse(lines), "") + l + line));
											line = "";
											break content;
										}
										line = l + line;
									}
								} else {
									trimmed.add(asContentList(line));
									line = "";
									trimmed.add(Optional.of((Style)c));
									break;
								}
							}
							if (!line.isEmpty())
								if (pre || line.contains("\u00a0") || line.contains("\u2028"))
									trailingBlanks++;
								else
									trimmed.add(asContentList(line));
							while (content.hasNext()) {
								Object c = content.next();
								if (c instanceof String)
									trimmed.add(asContentList((String)c));
								else
									trimmed.add(Optional.of((Style)c));
							}
						}
						padding = new Padding(0, 0, padding.bottom + emToMm(m, trailingBlanks), 0);
						area = Style.remove(area, ImmutableList.of("content",
						                                           "padding-left",
						                                           "padding-right",
						                                           "padding-top",
						                                           "padding-bottom").iterator());
						area = mergeStyle(area, padding.toStyle(m));
						area = Style.put(area, "content", mergeStyle(reverse(trimmed)));
						normalizedContent.add(Style.of(at, area));
						bottom = Math.max(bottom, specified.bottom - padding.bottom);
					}
					minimum = new Margin(top, right, bottom, left);
				}
				if (frontSide)
					frontMarginContentSpecified = normalizedContent;
				else
					backMarginContentSpecified = normalizedContent;
				// First move boundaries of page box to boundaries of printable area, changing margins (possibly making
				// them negative), but keeping size of page area equal.
				double left = specified.left;
				double right = specified.right;
				double top = specified.top;
				double bottom = specified.bottom;
				if (size.isPresent()) {
					double w = size.get()[0];
					double h = size.get()[1];
					if (frontSide) {
						left -= innerUnprintable;
						right += (adjustedWidth - w + innerUnprintable);
					} else {
						left -= outerUnprintable;
						right += (adjustedWidth - w + outerUnprintable);
					}
					top -= topUnprintable;
					bottom += (adjustedHeight - h + topUnprintable);
				} else {
					if (frontSide) {
						left -= innerUnprintable;
						right -= outerUnprintable;
					} else {
						right -= innerUnprintable;
						left -= outerUnprintable;
					}
					top -= topUnprintable;
					bottom -= bottomUnprintable;
				}
				// Then adjust/redistribute margins so that content does not overflow (more than with the specified
				// margins), while keeping page size equal.
				if (left + right < minimum.left + minimum.right || top + bottom < minimum.top + minimum.bottom)
					logger.warn(
						String.format(
							"Page area may not exceed %f mm x %f mm. " +
							"Adjusting page dimensions to fit content within the available space",
							adjustedWidth,
							adjustedHeight));
				else if (left < minimum.left || right < minimum.right)
					logger.warn(
						String.format(
							"% margin must be at least %f mm. " +
							"Adjusting page dimensions to fit content within the available space",
							(left < minimum.left) == frontSide ? "Inner" : "Outer",
							innerUnprintable));
				else if (top < minimum.top || bottom < minimum.bottom)
					logger.warn(
						String.format(
							"% margin must be at least %f mm. " +
							"Adjusting page dimensions to fit content within the available space",
							top < minimum.top ? "Top" : "Bottom",
							innerUnprintable));
				if (left < minimum.left) {
					if (size.isPresent())
						right -= (minimum.left - left);
					left = minimum.left;
				}
				if (right < minimum.right) {
					right = minimum.right;
					if (size.isPresent())
						left -= Math.min(minimum.right - right, left - minimum.left);
				}
				if (top < minimum.top) {
					if (size.isPresent())
						bottom -= (minimum.top - top);
					top = minimum.top;
				}
				if (bottom < minimum.bottom) {
					if (size.isPresent())
						top -= Math.min(minimum.bottom - bottom, top - minimum.top);
					bottom = minimum.bottom;
				}
				Margin adjusted = new Margin(top, right, bottom, left);
				if (frontSide)
					frontMarginAdjusted = adjusted;
				else
					backMarginAdjusted = adjusted;

			}
			// in case a page size was specified, match the adjusted page size to it as much as possible,
			// leaving as much freedom as possible for the PEF handler to position the content on the page
			if (size.isPresent()) {
				double minWidth = 0;
				double minHeight = 0;
				for (boolean frontSide : new Boolean[]{true, false}) {
					Optional<Style> style = frontSide ? frontStyle : backStyle;
					Margin specified = frontSide ? frontMarginSpecified : backMarginSpecified;
					Margin adjusted = frontSide ? frontMarginAdjusted : backMarginAdjusted;
					double rightUnprintable = frontSide ? outerUnprintable : innerUnprintable;
					minWidth = Math.max(
						minWidth,
						adjustedWidth - Math.max(0, adjusted.right + rightUnprintable - specified.right));
					minHeight = Math.max(
						minHeight,
						adjustedHeight - Math.max(0, adjusted.bottom + bottomUnprintable - specified.bottom));
				}
				if (minWidth < adjustedWidth || minHeight < adjustedHeight) {
					for (boolean frontSide : new Boolean[]{true, false}) {
						Optional<Style> style = frontSide ? frontStyle : backStyle;
						Margin adjusted = frontSide ? frontMarginAdjusted : backMarginAdjusted;
						adjusted = new Margin(adjusted.top,
						                      adjusted.right - (adjustedWidth - minWidth),
						                      adjusted.bottom - (adjustedHeight - minHeight),
						                      adjusted.left);
						if (frontSide)
							frontMarginAdjusted = adjusted;
						else
							backMarginAdjusted = adjusted;
					}
					adjustedWidth = minWidth;
					adjustedHeight = minHeight;
				}
			}
			// update page style
			Iterable<String> handledKeys = ImmutableList.of(
				"size",
				"margin-left", "margin-right", "margin-top", "margin-bottom",
				"&:left", "&:right",
				"@top-left", "@top-center", "@top-right",
				"@bottom-left", "@bottom-center", "@bottom-right",
				"@left", "@right"
			);
			for (boolean frontSide : new Boolean[]{true, false}) {
				Optional<Style> style = frontSide ? frontStyle : backStyle;
				Margin specified = frontSide ? frontMarginSpecified : backMarginSpecified;
				Margin adjusted = frontSide ? frontMarginAdjusted : backMarginAdjusted;
				List<Optional<Style>> specifiedContent = frontSide ? frontMarginContentSpecified : backMarginContentSpecified;
				// trim or pad margin content
				List<Optional<Style>> adjustedContent;
				if (adjusted.equals(specified))
					adjustedContent = specifiedContent;
				else {
					adjustedContent = new ArrayList<>();
					for (Optional<Style> s : specifiedContent)
						if (s.isPresent()) {
							String at = Style.keys(s).iterator().next();
							if (at.startsWith("@top-") && adjusted.top != specified.top ||
							    at.startsWith("@bottom-") && adjusted.bottom != specified.bottom ||
							    at.equals("@left") && adjusted.left != specified.left ||
							    at.equals("@right") && adjusted.right != specified.right) {
								Optional<Style> area = Style.get(s, at);
								Padding padding = Padding.fromStyle(area, m);
								area = Style.remove(area, ImmutableList.of("padding-left",
								                                           "padding-right",
								                                           "padding-top",
								                                           "padding-bottom").iterator());
								if (at.startsWith("@top-"))
									padding = new Padding(padding.top + adjusted.top - specified.top, padding.right, padding.bottom, padding.left);
								else if (at.startsWith("@bottom-"))
									padding = new Padding(padding.top, padding.right, padding.bottom + adjusted.bottom - specified.bottom, padding.left);
								else if (at.equals("@left"))
									padding = new Padding(padding.top, padding.right, padding.bottom, padding.left + adjusted.left - specified.left);
								else
									padding = new Padding(padding.top, padding.right + adjusted.right - specified.right , padding.bottom, padding.left);
								area = mergeStyle(area, padding.toStyle(m));
								s = Style.of(at, area);
							}
							adjustedContent.add(s);
						}
				}
				// negative margins can not actually be accomplished (content will extend into the page area anyway)
				if (adjusted.top < 0 || adjusted.bottom < 0 || adjusted.left < 0 || adjusted.right < 0)
					adjusted = new Margin(Math.max(0, adjusted.top),
					                      Math.max(0, adjusted.right),
					                      Math.max(0, adjusted.bottom),
					                      Math.max(0, adjusted.left));
				style = Style.remove(style, handledKeys.iterator());
				style = mergeStyle(adjusted.toStyle(m), mergeStyle(adjustedContent), style);
				if (frontSide)
					frontStyle = style;
				else
					backStyle = style;
			}
			List<Optional<Style>> s = new ArrayList<>();
			s.add(Style.get(parseStyle(String.format("@page { size: %d %d }",
			                                         (int)Math.floor(mmToCh(m, adjustedWidth)),
			                                         (int)Math.floor(mmToEm(m, adjustedHeight)))),
			                "@page"));
			s.add(Style.remove(pageStyle, handledKeys.iterator()));
			if (frontStyle.isPresent() && !"".equals(frontStyle.get().toString())) {
				if (backStyle.isPresent() && !"".equals(backStyle.get().toString())) {
					s.add(frontStyle);
					if (!frontStyle.get().toString().equals(backStyle.get().toString()))
						s.add(Style.of("&:left", backStyle));
				} else
					s.add(Style.of("&:right", frontStyle));
			} else if (backStyle.isPresent() && !"".equals(backStyle.get().toString()))
				s.add(Style.of("&:left", backStyle));
			return mergeStyle(s).get();
		} else if (!pageStyle.isPresent()) // m == null
			return Style.get(parseStyle("@page { size: 40 25 }"), "@page").get();
		else if (m != null) { // pageStyle.isPresent() && m.isOverFlowAllowed()
			Optional<Double[]> size = Style.get(pageStyle, "size")
				.map(x -> {
						String[] s = x.toString().trim().split("\\s+");
						Dimension w = parseDimension(s[0], true).toUnit(Unit.MM, m);
						Dimension h = parseDimension(s[1], false).toUnit(Unit.MM, m);
						return new Double[]{w.getValue().doubleValue(),
						                    h.getValue().doubleValue()}; });
			if (size.isPresent())
				return pageStyle.get();
			else {
				Area printable = m.getPrintableArea();
				return mergeStyle(
					Style.get(parseStyle(String.format("@page { size: %d %d }",
					                                   (int)Math.floor(mmToCh(m, printable.getWidth())),
					                                   (int)Math.floor(mmToEm(m, printable.getHeight())))),
					          "@page"),
					pageStyle
				).get();
			}
		} else // pageStyle.isPresent()
			return pageStyle.get();
	}

	private static Dimension parseDimension(String dimension, boolean horizontal) throws IllegalArgumentException {
		try {
			return Dimension.parse(dimension);
		} catch (IllegalArgumentException e) {
			try {
				int i = Integer.parseInt(dimension);
				return new Dimension(i, horizontal ? Unit.CH : Unit.EM);
			} catch (NumberFormatException ee) {
				throw e;
			}
		}
	}

	private static Optional<Style> parseStyle(String style) {
		return Style.parse(Optional.of(style));
	}

	private static Optional<Style> asContentList(String style) {
		if (style.isEmpty())
			return Optional.empty();
		else
			return Style.get(
				Style.get(
					parseStyle(String.format("@page { content: '%s' }", style.replaceAll("\n", "\\\\A ").replaceAll("'", "\\\\27 "))),
					"@page"),
				"content");
	}

	private static Optional<Style> mergeStyle(Iterable<Optional<Style>> styles) {
		List<Object> list = new ArrayList<>();
		for (Optional<Style> s : styles)
			if (s.isPresent())
				list.add(s.get());
		return Style.merge(list.iterator());
	}

	private static Optional<Style> mergeStyle(Optional<Style>... styles) {
		List<Object> list = new ArrayList<>();
		for (Optional<Style> s : styles)
			if (s.isPresent())
				list.add(s.get());
		return Style.merge(list.iterator());
	}

	private static <T> Iterable<T> iterable(Iterator<T> iterator) {
		return Lists.newArrayList(iterator);
	}

	private static <T> Iterable<T> reverse(Iterator<T> iterator) {
		List<T> list = Lists.newArrayList(iterator);
		Collections.reverse(list);
		return list;
	}

	private static <T> Iterable<T> reverse(Iterable<T> iterable) {
		List<T> list = Lists.newArrayList(iterable);
		Collections.reverse(list);
		return list;
	}

	private static <T> Iterable<T> reverse(T[] array) {
		List<T> list = Lists.newArrayList(array);
		Collections.reverse(list);
		return list;
	}

	private static double mmToCh(RelativeDimensionBase medium, double mm) {
		return new Dimension(mm, Unit.MM).toUnit(Unit.CH, medium).getValue().doubleValue();
	}

	private static double mmToEm(RelativeDimensionBase medium, double mm) {
		return new Dimension(mm, Unit.MM).toUnit(Unit.EM, medium).getValue().doubleValue();
	}

	private static double emToMm(RelativeDimensionBase medium, double mm) {
		return new Dimension(mm, Unit.EM).toUnit(Unit.MM, medium).getValue().doubleValue();
	}

	private static class Margin {

		private final static Margin NONE = new Margin(0.0, 0.0, 0.0, 0.0);
		final double left;
		final double right;
		final double top;
		final double bottom;

		Margin(double top, double right, double bottom, double left) {
			this.left = left;
			this.right = right;
			this.top = top;
			this.bottom = bottom;
		}

		@Override
		public String toString() {
			return String.format("margin: %dmm %dmm %dmm %dmm", (int)top, (int)right, (int)bottom, (int)left);
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Margin))
				return false;
			Margin that = (Margin)other;
			return this.left == that.left &&
				this.right == that.right &&
				this.top == that.top &&
				this.bottom == that.bottom;
		}

		Optional<Style> toStyle(RelativeDimensionBase medium) {
			List<String> s = new ArrayList<>();
			if (left > 0)
				s.add(String.format("margin-left: %d", (int)Math.round(mmToCh(medium, left))));
			if (right > 0)
				s.add(String.format("margin-right: %d", (int)Math.round(mmToCh(medium, right))));
			if (top > 0)
				s.add(String.format("margin-top: %d", (int)Math.round(mmToEm(medium, top))));
			if (bottom > 0)
				s.add(String.format("margin-bottom: %d", (int)Math.round(mmToEm(medium, bottom))));
			return Style.get(parseStyle(String.format("@page { %s }", Strings.join(s, "; "))), "@page");
		}

		static Margin fromStyle(Optional<Style> style, RelativeDimensionBase medium) {
			if (!style.isPresent())
				return NONE;
			double left = Style.get(style, "margin-left")
				.map(x -> parseDimension(x.toString(), true).toUnit(Unit.MM, medium).getValue().doubleValue())
				.orElse(0.0);
			double right = Style.get(style, "margin-right")
				.map(x -> parseDimension(x.toString(), true).toUnit(Unit.MM, medium).getValue().doubleValue())
				.orElse(0.0);
			double top = Style.get(style, "margin-top")
				.map(x -> parseDimension(x.toString(), false).toUnit(Unit.MM, medium).getValue().doubleValue())
				.orElse(0.0);
			double bottom = Style.get(style, "margin-bottom")
				.map(x -> parseDimension(x.toString(), false).toUnit(Unit.MM, medium).getValue().doubleValue())
				.orElse(0.0);
			return new Margin(top, right, bottom, left);
		}
	}

	private static class Padding {

		private final static Padding NONE = new Padding(0.0, 0.0, 0.0, 0.0);
		final double left;
		final double right;
		final double top;
		final double bottom;

		Padding(double top, double right, double bottom, double left) {
			this.left = left;
			this.right = right;
			this.top = top;
			this.bottom = bottom;
		}

		@Override
		public String toString() {
			return String.format("padding: %dmm %dmm %dmm %dmm", (int)top, (int)right, (int)bottom, (int)left);
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Padding))
				return false;
			Padding that = (Padding)other;
			return this.left == that.left &&
				this.right == that.right &&
				this.top == that.top &&
				this.bottom == that.bottom;
		}

		Optional<Style> toStyle(RelativeDimensionBase medium) {
			List<String> s = new ArrayList<>();
			if (left > 0)
				s.add(String.format("padding-left: %d", (int)Math.round(mmToCh(medium, left))));
			if (right > 0)
				s.add(String.format("padding-right: %d", (int)Math.round(mmToCh(medium, right))));
			if (top > 0)
				s.add(String.format("padding-top: %d", (int)Math.round(mmToEm(medium, top))));
			if (bottom > 0)
				s.add(String.format("padding-bottom: %d", (int)Math.round(mmToEm(medium, bottom))));
			return Style.get(parseStyle(String.format("@page { %s }", Strings.join(s, "; "))), "@page");
		}

		static Padding fromStyle(Optional<Style> style, RelativeDimensionBase medium) {
			if (!style.isPresent())
				return NONE;
			double left = Style.get(style, "padding-left")
				.map(x -> parseDimension(x.toString(), true).toUnit(Unit.MM, medium).getValue().doubleValue())
				.orElse(0.0);
			double right = Style.get(style, "padding-right")
				.map(x -> parseDimension(x.toString(), true).toUnit(Unit.MM, medium).getValue().doubleValue())
				.orElse(0.0);
			double top = Style.get(style, "padding-top")
				.map(x -> parseDimension(x.toString(), false).toUnit(Unit.MM, medium).getValue().doubleValue())
				.orElse(0.0);
			double bottom = Style.get(style, "padding-bottom")
				.map(x -> parseDimension(x.toString(), false).toUnit(Unit.MM, medium).getValue().doubleValue())
				.orElse(0.0);
			return new Padding(top, right, bottom, left);
		}
	}

	@Component(
		name = "EmbossedMediumFunctions",
		service = { ExtensionFunctionProvider.class }
	)
	public static class Provider extends ReflexiveExtensionFunctionProvider {
		public Provider() {
			super(EmbossedMediumFunctions.class);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(EmbossedMediumFunctions.class);

}
