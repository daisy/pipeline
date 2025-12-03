package org.daisy.pipeline.braille.pef.impl;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.base.CaseFormat;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import cz.vutbr.web.css.MediaQuery;

import org.daisy.dotify.api.embosser.Embosser;
import org.daisy.dotify.api.embosser.EmbosserFeatures;
import org.daisy.dotify.api.embosser.EmbosserProperties.PrintMode;
import org.daisy.dotify.api.embosser.EmbosserProvider;
import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.FileFormat;
import org.daisy.dotify.api.embosser.FileFormatProvider;
import org.daisy.dotify.api.embosser.PrintPage;
import org.daisy.dotify.api.paper.Area;
import org.daisy.dotify.api.paper.Length;
import org.daisy.dotify.api.paper.PageFormat;
import org.daisy.dotify.api.paper.RollPaperFormat;
import org.daisy.dotify.api.paper.SheetPaperFormat;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableFilter;
import org.daisy.dotify.api.paper.TractorPaperFormat;

import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.css.EmbossedMedium;
import org.daisy.pipeline.braille.css.EmbossedMedium.EmbossedMediumBuilder;
import org.daisy.pipeline.braille.pef.BrailleFileFormat;
import org.daisy.pipeline.braille.pef.TableRegistry;
import org.daisy.pipeline.css.Dimension;
import org.daisy.pipeline.css.Dimension.RelativeDimensionBase;
import org.daisy.pipeline.css.Dimension.Unit;
import org.daisy.pipeline.css.Medium;
import org.daisy.pipeline.css.Medium.MediumBuilder;
import org.daisy.pipeline.css.MediumProvider;

import org.osgi.framework.FrameworkUtil;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "org.daisy.pipeline.braille.pef.impl.FileFormatCatalog",
	service = { MediumProvider.class }
)
public class FileFormatCatalog implements MediumProvider {

	@Override
	public Iterable<EmbossedMedium> get(MediaQuery query) {
		try {
			return ((FileFormatBuilder)new FileFormatBuilder().parse(query)).get();
		} catch (IllegalArgumentException e) {
			// query is assumed to be validated, so an IllegalArgumentException should only happen
			// if a media type is provided that is not equal to "embossed"
			return empty;
		}
	}

	private class FileFormatBuilder extends EmbossedMediumBuilder {

		private Dimension width = null;
		private Dimension height = null;
		private Dimension pageWidth = null;
		private Dimension pageHeight = null;
		private Locale locale = null;
		private Locale docLocale = null;
		private String tableID = null;
		private String formatID = null;
		private String embosserID = null;
		private Boolean duplex = null;
		private Boolean saddleStitch = null;
		private Boolean zFolding = null;
		private Boolean blankLastPage = null;
		private Map<String,Object> otherFeatures = new HashMap<>();

		@Override
		protected Object parse(String feature, Object value) throws IllegalArgumentException {
			value = super.parse(feature, value);
			if ("width".equals(feature))
				width = (Dimension)value;
			else if ("height".equals(feature))
				height = (Dimension)value;
			else if ("device-width".equals(feature))
				pageWidth = (Dimension)value;
			else if ("device-height".equals(feature))
				pageHeight = (Dimension)value;
			else if ("-daisy-document-locale".equals(feature))
				docLocale = (Locale)value;
			else if ("-daisy-locale".equals(feature))
				locale = (Locale)value;
			else if ("-daisy-duplex".equals(feature))
				duplex = (Boolean)value;
			else if ("-daisy-table".equals(feature)) {
				if (!(value instanceof String))
					throw new IllegalArgumentException("Not an ident: " + value);
				tableID = (String)value;
			} else if ("-daisy-format".equals(feature)) {
				if (!(value instanceof String))
					throw new IllegalArgumentException("Not an ident: " + value);
				formatID = (String)value;
			} else if ("-daisy-embosser".equals(feature)) {
				if (!(value instanceof String))
					throw new IllegalArgumentException("Not an ident: " + value);
				embosserID = (String)value;
			} else if ("-daisy-saddle-stitch".equals(feature))
				saddleStitch = (Boolean)value;
			else if ("-daisy-z-folding".equals(feature))
				zFolding = (Boolean)value;
			else if ("-daisy-blank-last-page".equals(feature))
				blankLastPage = (Boolean)value;
			else
				otherFeatures.put(feature, value);
			return value;
		}

		// this method is assumed to be called only once
		public Iterable<EmbossedMedium> get() {
			if (type != null && type != Medium.Type.EMBOSSED)
				return empty;
			if (width != null && pageWidth != null) {
				logger.warn("query contains both width and device-width");
				return empty;
			}
			if (height != null && pageHeight != null) {
				logger.warn("query contains both height and device-height");
				return empty;
			}
			Iterable<Supplier<FileFormat>> format; {
				if (formatID != null) {
					if (embosserID != null) {
						logger.warn("query contains both -daisy-format and -daisy-embosser");
						return empty;
					}
					if ("pef".equalsIgnoreCase(formatID))
						format = Collections.singleton(() -> new PEFFileFormat());
					else
						format = Iterables.transform(
							Iterables.filter(
								fileFormatProviders,
								p -> Iterables.any(p.list(), props -> props.getIdentifier().equals(formatID))),
							p -> () -> p.newFactory(formatID));
				} else if (embosserID != null)
					format = Iterables.transform(
						Iterables.filter(
							embosserProviders,
							p -> Iterables.any(p.list(), props -> props.getIdentifier().equals(embosserID))),
						p -> () -> new EmbosserAsFileFormat(p.newFactory(embosserID)));
				else
					format = Lists.newArrayList(() -> new ConfigurableFileFormat(),
					                            () -> new PEFFileFormat());
			}
			Iterable<Table> table; {
				if (tableID != null) {
					Iterable<Table> t = tableRegistry.get(mutableQuery().add("id", tableID));
					try {
						// could be a locale
						t = Iterables.concat(
							tableRegistry.get(mutableQuery().add("locale", parseLocale(tableID).toLanguageTag())),
							t);
					} catch (IllegalArgumentException e) {
					}
					table = t;
				} else if (locale != null) {
					table = Iterables.concat(tableRegistry.get(mutableQuery().add("locale", locale.toLanguageTag())),
					                         Collections.singleton(null)); // for PEF
					locale = null;
				} else if (docLocale != null)
					table = Iterables.concat(tableRegistry.get(mutableQuery().add("locale", docLocale.toLanguageTag())),
					                         tableRegistry.get(mutableQuery().add("id", ConfigurableFileFormat.DEFAULT_TABLE)),
					                         Collections.singleton(null)); // for PEF
				else if (formatID == null && embosserID == null)
					table = Iterables.concat(tableRegistry.get(mutableQuery().add("id", ConfigurableFileFormat.DEFAULT_TABLE)),
					                         Collections.singleton(null)); // for PEF
				else
					table = Collections.singleton(null);
			}
			return Iterables.concat(
				Iterables.transform(
					format,
					new Function<Supplier<FileFormat>,Iterable<EmbossedMedium>>() {
						public Iterable<EmbossedMedium> apply(Supplier<FileFormat> fs) {
							return Iterables.filter(
								Iterables.transform(
									table,
									new Function<Table,EmbossedMedium>() {
										public EmbossedMedium apply(Table table) {
											FileFormat format = fs.get();
											Boolean duplex = FileFormatBuilder.this.duplex;
											if (duplex != null) {
												if (format.supportsDuplex())
													// try to enable/disable duplex
													try {
														format.setFeature(EmbosserFeatures.DUPLEX, duplex);
													} catch (IllegalArgumentException e) {
														// ignore, assume that duplex is always enabled (can not be disabled), and that
														// non-duplex PEFs will be handled correctly
													}
												else if (duplex == true)
													return null;
											} else {
												try {
													duplex = (Boolean)format.getFeature(EmbosserFeatures.DUPLEX);
												} catch (IllegalArgumentException e) {
												}
												if (duplex == null)
													duplex = false;
											}
											Boolean saddleStitch = FileFormatBuilder.this.saddleStitch;
											if (saddleStitch != null)
												try {
													format.setFeature(EmbosserFeatures.SADDLE_STITCH, saddleStitch);
												} catch (IllegalArgumentException e) {
													return null;
												}
											else if (format instanceof EmbosserAsFileFormat)
												try {
													saddleStitch = (Boolean)format.getFeature(EmbosserFeatures.SADDLE_STITCH);
												} catch (IllegalArgumentException e) {
													saddleStitch = null;
												}
											if (zFolding != null)
												if (format instanceof EmbosserAsFileFormat) {
													if (((EmbosserAsFileFormat)format).embosser.supportsZFolding())
														try {
															format.setFeature(EmbosserFeatures.Z_FOLDING, zFolding);
														} catch (IllegalArgumentException e) {
															return null;
														}
													else if (zFolding == true)
														return null;
												} else
													return null;
											if (table != null)
												if (format.supportsTable(table))
													try {
														format.setFeature(EmbosserFeatures.TABLE, table);
													} catch (IllegalArgumentException e) {
														return null;
													}
												else
													return null;
											else
												// this is meant for PEFFileFormat or other formats/embossers that have a table set by default
												// in case of a format that needs the setting, newEmbosserWriter() below will fail
												try {
													table = (Table)format.getFeature(EmbosserFeatures.TABLE);
												} catch (IllegalArgumentException e) {
												}
											if (locale != null)
												if (table != null) {
													if (!Iterables.any(
														    tableRegistry.get(mutableQuery().add("locale", locale.toLanguageTag())),
														    Predicates.equalTo(table))) {
														logger.warn("Table " + table + " not compatible with locale " + locale);
														return null;
													}
												} else
													return null;
											for (Map.Entry<String,Object> f : otherFeatures.entrySet()) {
												String k = f.getKey();
												if (k.startsWith("-daisy-"))
													k = k.substring("-daisy-".length());
												try {
													format.setFeature(CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, k),
													                  f.getValue());
												} catch (IllegalArgumentException e) {
													return null;
												}
											}
											// sheets-multiple-of-two and blank-last-page are handled in px:pef-store
											Boolean blankLastPage = FileFormatBuilder.this.blankLastPage;
											if (blankLastPage == null)
												blankLastPage = false;
											if (format instanceof EmbosserAsFileFormat) {
												Embosser embosser = ((EmbosserAsFileFormat)format).embosser;
												Double cellWidth, cellHeight;
												try {
													cellWidth = getCellWidth(embosser);
													cellHeight = getCellHeight(embosser);
												} catch (IllegalArgumentException e) {
													return null;
												}
												RelativeDimensionBase fontSize = new RelativeDimensionBase() {
													public double getCh() { return cellWidth; }
													public double getEm() { return cellHeight; }};
												PageFormat pageFormat = null; {
													// it is complicated to compute the page format from the printable area, so don't support it for now
													if (width != null) {
														// device-width == null (see assertion above)
														logger.warn(
															"width is not supported when query also contains -daisy-embosser, use device-width instead");
														return null;
													}
													if (height != null) {
														// device-height == null (see assertion above)
														logger.warn(
															"height is not supported when query also contains -daisy-embosser, use device-height instead");
														return null;
													}
													if (pageWidth == null || pageHeight == null) {
														PageFormat p = getPageFormat(embosser);
														if (p != null) {
															if (pageWidth == null && pageHeight == null)
																pageFormat = p;
															PrintPage page = embosser.getPrintPage(p);
															if (pageWidth == null)
																pageWidth = new Dimension(page.getWidth(), Unit.MM);
															if (pageHeight == null)
																pageHeight = new Dimension(page.getHeight(), Unit.MM);
														} else {
															if (pageWidth == null)
																pageWidth = new Dimension(210, Unit.MM); // A4
															if (pageHeight == null)
																pageHeight = new Dimension(297, Unit.MM); // A4
														}
													}
													if (pageFormat == null) {
														// we don't know the supported paper types or the print direction, so try all combinations
														Length w = Length.newMillimeterValue(pageWidth.toUnit(Unit.MM, fontSize).getValue().doubleValue());
														Length h = Length.newMillimeterValue(pageHeight.toUnit(Unit.MM, fontSize).getValue().doubleValue());
														if (!Boolean.TRUE.equals(saddleStitch) && (
															    // try sheet paper
															    setPageFormat(embosser, new SheetPaperFormat(w, h), w, h)
															    // try roll paper, upright
															    || setPageFormat(embosser, new RollPaperFormat(w, h), w, h)
															    // try roll paper, sideways
															    || setPageFormat(embosser, new RollPaperFormat(h, w), w, h)
															    // try tractor paper, upright
															    || setPageFormat(embosser, new TractorPaperFormat(w, h), w, h)
															    // try tractor paper, sideways
															    || setPageFormat(embosser, new TractorPaperFormat(h, w), w, h)))
															;
														else if (!Boolean.FALSE.equals(saddleStitch)) {
															// try magazine mode
															Length doubleWidth = Length.newMillimeterValue(2 * w.asMillimeter());
															// note that it would be better to check for magazine mode using
															// `embosser.get(SADDLE_STITCH)', but this does not work for all embossers
															if (setPageFormat(embosser, new SheetPaperFormat(doubleWidth, h), w, h)
															    || setPageFormat(embosser, new RollPaperFormat(doubleWidth, h), w, h)
															    || setPageFormat(embosser, new RollPaperFormat(h, doubleWidth), w, h)
															    || setPageFormat(embosser, new TractorPaperFormat(doubleWidth, h), w, h)
															    || setPageFormat(embosser, new TractorPaperFormat(h, doubleWidth), w, h))
																;
															else
																return null;
														} else
															return null;
														pageFormat = getPageFormat(embosser);
													}
												}
												// verify that newEmbosserWriter() works (will be used in PEF2TextStep)
												try {
													if (!(format instanceof PEFFileFormat))
														format.newEmbosserWriter(new OutputStream() { public void write(int b) {}});
												} catch (Throwable e) {
													logger.debug("file format misconfigured", e);
													return null;
												}
												return new BrailleFileFormat(
													format, embosser.getPrintableArea(pageFormat),
													pageWidth, pageHeight, cellWidth, cellHeight,
													duplex, blankLastPage, false,
													FileFormatBuilder.this);
											} else {
												double cellWidth = 6;
												double cellHeight = 10;
												RelativeDimensionBase fontSize = new RelativeDimensionBase() {
													public double getCh() { return cellWidth; }
													public double getEm() { return cellHeight; }};
												if (width == null && pageWidth == null)
													width = pageWidth = new Dimension(40, Unit.CH);
												else if (width == null)
													width = pageWidth;
												else if (pageWidth == null)
													pageWidth = width;
												else
													; // can not happen, see assertion above
												if (height == null && pageHeight == null)
													height = pageHeight = new Dimension(25, Unit.EM);
												else if (height == null)
													height = pageHeight;
												else if (pageHeight == null)
													pageHeight = height;
												else
													; // can not happen, see assertion above
												// verify that newEmbosserWriter() works (will be used in PEF2TextStep)
												try {
													if (!(format instanceof PEFFileFormat))
														format.newEmbosserWriter(new OutputStream() { public void write(int b) {}});
												} catch (Throwable e) {
													logger.debug("file format misconfigured", e);
													return null;
												}
												return new BrailleFileFormat(
													format,
													new Area(
														width.toUnit(Unit.MM, fontSize).getValue().doubleValue(),
														height.toUnit(Unit.MM, fontSize).getValue().doubleValue(),
														0, 0),
													pageWidth, pageHeight, cellWidth, cellHeight,
													duplex, blankLastPage, true,
													FileFormatBuilder.this);
											}
										}
									}
								),
								Predicates.notNull()
							);
						}
					}
				)
			);
		}

		@Override
		public EmbossedMedium build() {
			throw new UnsupportedOperationException(); // call get() instead
		}
	}

	/**
	 * Get the embosser's cell width through reflection
	 *
	 * @throw IllegalArgumentException if the embosser does not extend <code>AbstractEmbosser</code>
	 */
	private static double getCellWidth(Embosser embosser) throws IllegalArgumentException {
		try {
			Method m = null; {
				Class<?> c = embosser.getClass();
				while (c != null) {
					try {
						m = c.getDeclaredMethod("getCellWidth");
						break;
					} catch (NoSuchMethodException e) {
						c = c.getSuperclass();
					}
				}
			}
			m.setAccessible(true);
			return (double)m.invoke(embosser);
		} catch (IllegalAccessException | InvocationTargetException | SecurityException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Get the embosser's cell height through reflection
	 *
	 * @throw IllegalArgumentException if the embosser does not extend <code>AbstractEmbosser</code>
	 */
	private static double getCellHeight(Embosser embosser) throws IllegalArgumentException {
		try {
			Method m = null; {
				Class c = embosser.getClass();
				while (c != null) {
					try {
						m = c.getDeclaredMethod("getCellHeight");
						break;
					} catch (NoSuchMethodException e) {
						c = c.getSuperclass();
					}
				}
			}
			m.setAccessible(true);
			return (double)m.invoke(embosser);
		} catch (IllegalAccessException | InvocationTargetException | SecurityException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Get the embosser's page format
	 */
	private static PageFormat getPageFormat(Embosser embosser) {
		return (PageFormat)embosser.getFeature(EmbosserFeatures.PAGE_FORMAT);
	}
	
	/**
	 * Set embosser's page format if it matches the requested page dimensions
	 */
	private static boolean setPageFormat(Embosser embosser, PageFormat format, Length width, Length height) {
		if (embosser.supportsPageFormat(format)) {
			PrintPage page = embosser.getPrintPage(format);
			if (page.getWidth() == width.asMillimeter() && page.getHeight() == height.asMillimeter()) {
				embosser.setFeature(EmbosserFeatures.PAGE_FORMAT, format);
				return true;
			}
		}
		return false;
	}

	private final static Iterable<EmbossedMedium> empty = Optional.<EmbossedMedium>absent().asSet();
	
	private final List<FileFormatProvider> fileFormatProviders = new ArrayList<FileFormatProvider>();
	
	@Reference(
		name = "FileFormatProvider",
		unbind = "-",
		service = FileFormatProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addFileFormatProvider(FileFormatProvider provider) {
		if (!OSGiHelper.inOSGiContext())
			provider.setCreatedWithSPI();
		fileFormatProviders.add(provider);
	}
	
	private final List<EmbosserProvider> embosserProviders = new ArrayList<EmbosserProvider>();
	
	@Reference(
		name = "EmbosserProvider",
		unbind = "-",
		service = EmbosserProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addEmbosserProvider(EmbosserProvider provider) {
		if (!OSGiHelper.inOSGiContext())
			provider.setCreatedWithSPI();
		embosserProviders.add(provider);
	}
	
	private TableRegistry tableRegistry;
	
	@Reference(
		name = "TableRegistry",
		unbind = "-",
		service = TableRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindTableRegistry(TableRegistry registry) {
		tableRegistry = registry;
	}
	
	private static class EmbosserAsFileFormat implements FileFormat {
		
		private final Embosser embosser;
		
		public EmbosserAsFileFormat(Embosser embosser) {
			this.embosser = embosser;
		}

		@Override
		public Object getProperty(String key) {
			return embosser.getProperty(key);
		}

		@Override
		public Object getFeature(String key) {
			return embosser.getFeature(key);
		}

		@Override
		public void setFeature(String key, Object value) {
			embosser.setFeature(key, value);
		}

		@Override
		public String getIdentifier() {
			return embosser.getIdentifier();
		}

		@Override
		public String getDisplayName() {
			return embosser.getDisplayName();
		}

		@Override
		public String getDescription() {
			return embosser.getDescription();
		}

		@Override
		public boolean supports8dot() {
			return embosser.supports8dot();
		}

		@Override
		public boolean supportsDuplex() {
			return embosser.supportsDuplex();
		}

		@Override
		public boolean supportsVolumes() {
			return false;
		}

		@Override
		public String getFileExtension() {
			return ".brf";
		}

		@Override
		public boolean supportsTable(Table table) {
			return embosser.supportsTable(table);
		}

		@Override
		public TableFilter getTableFilter() {
			return embosser.getTableFilter();
		}

		@Override
		public EmbosserWriter newEmbosserWriter(OutputStream os) {
			return embosser.newEmbosserWriter(os);
		}

		@Override
		public String toString() {
			return embosser.toString();
		}
	}
	
	private static abstract class OSGiHelper {
		static boolean inOSGiContext() {
			try {
				return FrameworkUtil.getBundle(OSGiHelper.class) != null;
			} catch (NoClassDefFoundError e) {
				return false;
			}
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(FileFormatCatalog.class);
}
