package org.daisy.braille.utils.impl.provider.indexbraille;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.daisy.dotify.api.embosser.EmbosserCatalog;
import org.daisy.dotify.api.embosser.EmbosserFeatures;
import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.paper.PageFormat;
import org.daisy.dotify.api.paper.PaperCatalog;
import org.daisy.dotify.api.paper.SheetPaper;
import org.daisy.dotify.api.paper.SheetPaperFormat;
import org.daisy.dotify.api.paper.TractorPaper;
import org.daisy.dotify.api.paper.TractorPaperFormat;
import org.daisy.braille.utils.pef.FileCompare;
import org.daisy.braille.utils.pef.PEFConverterFacade;
import org.daisy.braille.utils.pef.PEFHandler;
import org.daisy.braille.utils.pef.UnsupportedWidthException;
import org.daisy.dotify.common.io.FileIO;
import org.junit.Test;
import org.xml.sax.SAXException;

@SuppressWarnings("javadoc")
public class IndexV4EmbosserTest {
	
	private static EmbosserCatalog ec = EmbosserCatalog.newInstance();
	private static IndexV4Embosser everest = (IndexV4Embosser)ec.get("com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_EVEREST_D_V5");
	private static IndexV4Embosser basic_d = (IndexV4Embosser)ec.get("com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BASIC_D_V5");

	private static PaperCatalog pc = PaperCatalog.newInstance();
	private static PageFormat a3 = new SheetPaperFormat((SheetPaper)pc.get("org_daisy.ISO216PaperProvider.PaperSize.A3"), SheetPaperFormat.Orientation.DEFAULT);
	private static PageFormat _280mm_12inch = new TractorPaperFormat((TractorPaper)pc.get("org_daisy.TractorPaperProvider.PaperSize.W280MM_X_H12INCH"));

	@Test
	public void testDuplex() {
		assertTrue("Assert that duplex is supported for " + basic_d.getDisplayName(), basic_d.supportsDuplex());
		assertTrue("Assert that duplex is supported for " + everest.getDisplayName(), everest.supportsDuplex());
	}

	@Test
	public void test8dot() {
		assertTrue("Assert that 8-dot is supported", basic_d.supports8dot());
		assertTrue("Assert that 8-dot is supported", everest.supports8dot());
	}

	@Test
	public void testAligning() {

		assertTrue("Assert that aligning is supported", basic_d.supportsAligning());
		assertTrue("Assert that aligning is supported", everest.supportsAligning());
	}

	@Test
	public void testEmbosserWriter() throws IOException,
	ParserConfigurationException,
	SAXException,
	UnsupportedWidthException {

		File prn1 = File.createTempFile("test_indexv3_", ".prn");
		File prn2 = File.createTempFile("test_indexv3_", ".prn");
		File pef =  File.createTempFile("test_indexv3_", ".pef");

		FileCompare fc = new FileCompare();
		PEFHandler.Builder builder;
		EmbosserWriter w;

		basic_d.setFeature(EmbosserFeatures.PAGE_FORMAT, _280mm_12inch);
		everest.setFeature(EmbosserFeatures.PAGE_FORMAT, a3);

		// Single sided on a double sided printer

		basic_d.setFeature(EmbosserFeatures.Z_FOLDING, false);
		w = basic_d.newEmbosserWriter(new FileOutputStream(prn1));
		builder = new PEFHandler.Builder(w)
				.range(null)
				.align(PEFHandler.Alignment.INNER)
				.offset(0)
				.topOffset(0);
		FileIO.copy(this.getClass().getResourceAsStream("resource-files/single_sided.pef"), new FileOutputStream(pef));
		FileIO.copy(this.getClass().getResourceAsStream("resource-files/basic_d_v5_single_sided.prn"), new FileOutputStream(prn2));
		new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
		try (InputStream is2 = new FileInputStream(prn2)) {
			assertTrue("Assert that the contents of the file is as expected.",
					fc.compareBinary(new FileInputStream(prn1), is2)
					);
		}

		// Z-folding, single sided

		basic_d.setFeature(EmbosserFeatures.Z_FOLDING, true);
		w = basic_d.newEmbosserWriter(new FileOutputStream(prn1));
		builder = new PEFHandler.Builder(w)
				.range(null)
				.align(PEFHandler.Alignment.INNER)
				.offset(0)
				.topOffset(0);

		FileIO.copy(this.getClass().getResourceAsStream("resource-files/basic_d_v5_zfolding_single_sided.prn"), new FileOutputStream(prn2));
		new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
		try (InputStream is2 = new FileInputStream(prn2)) {
			assertTrue("Assert that the contents of the file is as expected.",
					fc.compareBinary(new FileInputStream(prn1), is2)
					);
		}

		// Double sided

		basic_d.setFeature(EmbosserFeatures.Z_FOLDING, false);
		w = basic_d.newEmbosserWriter(new FileOutputStream(prn1));
		builder = new PEFHandler.Builder(w)
				.range(null)
				.align(PEFHandler.Alignment.INNER)
				.offset(0)
				.topOffset(0);

		FileIO.copy(this.getClass().getResourceAsStream("resource-files/double_sided.pef"), new FileOutputStream(pef));
		FileIO.copy(this.getClass().getResourceAsStream("resource-files/basic_d_v5_double_sided.prn"), new FileOutputStream(prn2));
		new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
		try (InputStream is2 = new FileInputStream(prn2)) {
			assertTrue("Assert that the contents of the file is as expected.",
					fc.compareBinary(new FileInputStream(prn1), is2)
					);
		}

		// Z-folding, double sided

		basic_d.setFeature(EmbosserFeatures.Z_FOLDING, true);
		w = basic_d.newEmbosserWriter(new FileOutputStream(prn1));
		builder = new PEFHandler.Builder(w)
				.range(null)
				.align(PEFHandler.Alignment.INNER)
				.offset(0)
				.topOffset(0);

		FileIO.copy(this.getClass().getResourceAsStream("resource-files/basic_d_v5_zfolding_double_sided.prn"), new FileOutputStream(prn2));
		new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
		try (InputStream is2 = new FileInputStream(prn2)) {
			assertTrue("Assert that the contents of the file is as expected.",
					fc.compareBinary(new FileInputStream(prn1), is2)
					);
		}

		// Everest

		w = everest.newEmbosserWriter(new FileOutputStream(prn1));
		builder = new PEFHandler.Builder(w)
				.range(null)
				.align(PEFHandler.Alignment.INNER)
				.offset(0)
				.topOffset(0);

		FileIO.copy(this.getClass().getResourceAsStream("resource-files/everest_v5_double_sided.prn"), new FileOutputStream(prn2));
		new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
		try (InputStream is2 = new FileInputStream(prn2)) {
			assertTrue("Assert that the contents of the file is as expected.",
					fc.compareBinary(new FileInputStream(prn1), is2)
					);
		}

		prn1.deleteOnExit();
		prn2.deleteOnExit();
		pef.deleteOnExit();
	}

	@Test
	public void testEmbosserWriterBasic8Dot() throws IOException, ParserConfigurationException, SAXException, UnsupportedWidthException, URISyntaxException {
		File pef = new File(this.getClass().getResource("resource-files/8-dot-chart.pef").toURI());
		String refPath = "resource-files/basic_d_v5_8-dot-chart.prn";
		File prn1 = File.createTempFile(this.getClass().getCanonicalName(), ".prn");
		prn1.deleteOnExit();

		FileCompare fc = new FileCompare();
		PEFHandler.Builder builder;
		EmbosserWriter w;
		basic_d.setFeature(EmbosserFeatures.PAGE_FORMAT, _280mm_12inch);
		basic_d.setFeature(EmbosserFeatures.Z_FOLDING, false);
		w = basic_d.newEmbosserWriter(new FileOutputStream(prn1));
		builder = new PEFHandler.Builder(w)
				.range(null)
				.align(PEFHandler.Alignment.INNER)
				.offset(0)
				.topOffset(0);
		new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
		try (InputStream ref = this.getClass().getResourceAsStream(refPath)) {
			assertTrue("Assert that the contents of the file is as expected.", fc.compareBinary(new FileInputStream(prn1), ref));
		}
	}
	
	@Test
	public void testEmbosserWriterEverest8Dot() throws IOException, ParserConfigurationException, SAXException, UnsupportedWidthException, URISyntaxException {
		File pef = new File(this.getClass().getResource("resource-files/8-dot-chart.pef").toURI());
		String refPath = "resource-files/everest_v5_8-dot-chart.prn";
		File prn1 = File.createTempFile(this.getClass().getCanonicalName(), ".prn");
		prn1.deleteOnExit();

		FileCompare fc = new FileCompare();
		PEFHandler.Builder builder;
		EmbosserWriter w;
		everest.setFeature(EmbosserFeatures.PAGE_FORMAT, a3);
		w = everest.newEmbosserWriter(new FileOutputStream(prn1));
		builder = new PEFHandler.Builder(w)
				.range(null)
				.align(PEFHandler.Alignment.INNER)
				.offset(0)
				.topOffset(0);
		new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
		try (InputStream ref = this.getClass().getResourceAsStream(refPath)) {
			assertTrue("Assert that the contents of the file is as expected.", fc.compareBinary(new FileInputStream(prn1), ref));
		}
	}

	@Test
	public void testTableFilter() {
		assertTrue(IndexV4Embosser.tableFilter.accept(new FactoryProperties(){

			@Override
			public String getIdentifier() {
				return IndexV4Embosser.TABLE6DOT;
			}

			@Override
			public String getDisplayName() {
				return null;
			}

			@Override
			public String getDescription() {
				return null;
			}
		}));
		assertFalse(IndexV4Embosser.tableFilter.accept(new FactoryProperties() {
			
			@Override
			public String getIdentifier() {
				return "no-match";
			}
			
			@Override
			public String getDisplayName() {
				return null;
			}
			
			@Override
			public String getDescription() {
				return null;
			}
		}));
	}

}
