package org.daisy.pipeline.ocr.impl;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stax.StAXResult;

import com.google.common.base.Supplier;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.Matrix;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.xml.XmpParsingException;
import org.apache.xmpbox.xml.DomXmpParser;

import org.daisy.common.file.Resource;
import org.daisy.common.file.URLs;
import org.daisy.common.messaging.MessageAppender;
import org.daisy.common.saxon.SaxonBuffer;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcErrorException;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.pipeline.fileset.Fileset;

import org.slf4j.Logger;

public class OCRServiceHelper {

	public static class Metadata {
		public String title = null;
		public String author = null;
		public Locale language = null;
	}

	public static Metadata getMetadataFromPDF(Resource input, Logger logger) {
		Metadata result = new Metadata();
		try (PDDocument pdf = PDDocument.load(input.read())) {
			PDDocumentInformation info = pdf.getDocumentInformation();
			result.title = info.getTitle();
			result.author = info.getAuthor();
			if ("unknown".equalsIgnoreCase(result.author))
				result.author = null;
			PDMetadata metadata = pdf.getDocumentCatalog().getMetadata();
			if (metadata != null)
				try (InputStream s = metadata.createInputStream()) {
					DublinCoreSchema dc = new DomXmpParser().parse(s).getDublinCoreSchema();
					if (result.title == null)
						result.title = dc.getTitle();
					List<String> languages = dc.getLanguages();
					if (languages != null && languages.size()> 0)
						result.language = new Locale.Builder().setLanguageTag(languages.get(0)).build();
				} catch (XmpParsingException e) {
					logger.debug("Failed to extract XMP metadata from PDF", e);
				}
		} catch (IOException e) {
			logger.warn("Failed to extract metadata from PDF", e);
		}
		return result;
	}

	private static final Base64.Decoder base64Decoder = Base64.getDecoder();
	private static final Pattern DATA_URL = Pattern.compile("data:(image/[^;]+);base64,(.+)=*");

	public static Resource getImageFromDataURL(String data, URI path) throws IllegalArgumentException {
		Matcher m = DATA_URL.matcher(data);
		if (!m.matches())
			throw new IllegalArgumentException("unexpected image data URL");
		return Resource.load(base64Decoder.decode(m.group(2)),
		                     path,
		                     m.group(1));
	}

	private static final URI markdownToHTML
		= URLs.asURI(URLs.getResourceFromJAR("/xml/markdown-to-html.xpl", OCRServiceHelper.class));

	public static List<Resource> markdownToHTML(XProcEngine xprocEngine,
	                                            MessageAppender messageAppender,
	                                            Logger logger,
	                                            Map<String,String> properties,
	                                            File tempDir,
	                                            File resultDir,
	                                            Resource markdown,
	                                            Metadata metadata,
	                                            Set<Resource> images,
	                                            Map<String,String> imageShortDescriptions,
	                                            Map<String,String> imagesTextContent,
	                                            Map<String,Integer> imageWidths,
	                                            List<String> replaceImages)
			throws IOException {
		Map<String,String> metadataMap = new HashMap<>(); {
			if (metadata.title != null)
				metadataMap.put("title", metadata.title);
			if (metadata.author != null)
				metadataMap.put("author", metadata.author);
			if (metadata.language != null)
				metadataMap.put("language", metadata.language.toLanguageTag());
		}
		markdown = markdown.copy(URLs.resolve(URLs.asURI(tempDir), markdown.getPath())).store();
		for (Resource image : images)
			image.copy(URLs.resolve(URLs.asURI(tempDir), image.getPath())).store();
		SaxonBuffer buffer = new SaxonBuffer();
		XProcInput.Builder xprocInput = new XProcInput.Builder()
			.withOption(new QName("source"), markdown.getPath())
			.withOption(new QName("metadata"), metadataMap)
			.withOption(new QName("result-dir"), URLs.asURI(resultDir))
			.withOption(new QName("image-descriptions"), imageShortDescriptions != null ? imageShortDescriptions : Collections.emptyMap())
			.withOption(new QName("image-text-content"), imagesTextContent != null ? imagesTextContent : Collections.emptyMap())
			.withOption(new QName("image-sizes"), imageWidths != null ? imageWidths : Collections.emptyMap())
			.withOption(new QName("replace-images"), replaceImages != null ? replaceImages : Collections.emptyList());
		try {
			xprocEngine
				.load(markdownToHTML)
				.run(
					xprocInput.build(),
					() -> messageAppender,
					properties)
				.writeTo(
					new XProcOutput.Builder()
					               .withOutput(
					                   "result",
					                   () -> new StAXResult(buffer.asOutput().asXMLStreamWriter()))
					               .build());
		} catch (XProcErrorException e) {
			logger.error("XProc error:\n" + e.toString());
			throw new RuntimeException("XProc error happened. Please see detailed log for more info.", e);
		}
		buffer.done();
		try {
			return Fileset.unmarshall(buffer.asInput().asXMLStreamReader());
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	public static class ImageInfo {
		BufferedImage image;
		Rectangle2D bounds;
	}

	// Resolution of produced images if taken from rendered pages. (If taken directly from
	// source images, the original resulotion is used.)
	// Beware: this variable is specifically set to match Mistral's coordinate system. If
	// you change it, the returned image coordinates need to be processed differently.
	public static final float IMAGE_DPI = 200f;

	public static List<ImageInfo> getImageInfo(PDPage page) throws IOException {
		List<ImageInfo> images = new ArrayList<>();
		new PDFGraphicsStreamEngine(page) {
			@Override
			public void drawImage(PDImage pdImage) throws IOException {
				Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
				float x = ctm.getTranslateX();
				float y = ctm.getTranslateY();
				float w = ctm.getScalingFactorX();
				float h = ctm.getScalingFactorY();
				ImageInfo info = new ImageInfo();
				info.image = pdImage.getImage();
				info.bounds = new Rectangle2D.Float(x, y, w, h);
				images.add(info);
			}
			@Override public void strokePath() {}
			@Override public void fillPath(int windingRule) {}
			@Override public void clip(int windingRule) {}
			@Override public void moveTo(float x, float y) {}
			@Override public void lineTo(float x, float y) {}
			@Override public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {}
			@Override public Point2D getCurrentPoint() { return null; }
			@Override public void closePath() {}
			@Override public void endPath() {}
			@Override public void shadingFill(COSName shadingName) {}
			@Override public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException {}
			@Override public void fillAndStrokePath(int windingRule) throws IOException {}
		}.processPage(page);
		return images;
	}

	public static BufferedImage extractRegion(PDPage page,
	                                          Supplier<BufferedImage> renderedPage,
	                                          List<ImageInfo> images,
	                                          Rectangle2D region)
			throws IOException {

		// try direct image crop
		if (images != null)
			for (ImageInfo info : images) {

				// convert region to coordinate system of source image
				double scaleX = info.image.getWidth() / info.bounds.getWidth();
				double scaleY = info.image.getHeight() / info.bounds.getHeight();
				double x = (region.getX() * 72f / IMAGE_DPI - info.bounds.getX()) * scaleX;
				double y = (region.getY() * 72f / IMAGE_DPI - info.bounds.getY()) * scaleY;
				double w = region.getWidth() * 72f / IMAGE_DPI * scaleX;
				double h = region.getHeight() * 72f / IMAGE_DPI * scaleY;
				if (x >= 0
				    && y >= 0
				    && x + w <= info.image.getWidth()
				    && y + h <= info.image.getHeight())
					return info.image.getSubimage((int)x, (int)y, (int)w, (int)h);
			}

		// otherwise render full page (in 200 dpi) and crop
		return renderedPage.get().getSubimage((int)region.getX(),
		                                      (int)region.getY(),
		                                      (int)region.getWidth(),
		                                      (int)region.getHeight());
	}
}
