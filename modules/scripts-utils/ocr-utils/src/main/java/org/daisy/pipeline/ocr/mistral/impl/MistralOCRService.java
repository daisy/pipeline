package org.daisy.pipeline.ocr.mistral.impl;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stax.StAXResult;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.xml.XmpParsingException;
import org.apache.xmpbox.xml.DomXmpParser;

import org.daisy.common.file.Resource;
import org.daisy.common.file.URLs;
import org.daisy.common.messaging.MessageAppender;
import org.daisy.common.messaging.MessageBuilder;
import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.common.saxon.SaxonBuffer;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcErrorException;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.pipeline.common.rest.Request;
import org.daisy.pipeline.common.rest.Response;
import org.daisy.pipeline.fileset.Fileset;
import org.daisy.pipeline.ocr.OCRProcessor;
import org.daisy.pipeline.ocr.OCRService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "mistral-ocr",
	service = { OCRService.class }
)
public class MistralOCRService implements OCRService {

	private static final URL MODELS_ENDPOINT;
	private static final URL FILES_ENDPOINT;
	private static final URL OCR_ENDPOINT;

	static {
		// this is a hidden parameter, it is meant to be used in tests only
		String BASE_URL = Properties.getProperty("org.daisy.pipeline.ocr.mistral.address",
		                                         false,
		                                         "Base URL of Mistral API",
		                                         false,
		                                         "https://api.mistral.ai/v1")
			.getValue();
		try {
			MODELS_ENDPOINT = new URL(BASE_URL + "/models");
			FILES_ENDPOINT = new URL(BASE_URL + "/files");
			OCR_ENDPOINT = new URL(BASE_URL + "/ocr");
		} catch (MalformedURLException e) {
			throw new IllegalStateException("coding error", e);
		}
	}

	/* package private for tests */
	static final Property MISTRAL_APIKEY = Properties.getProperty("org.daisy.pipeline.ocr.mistral.apikey",
	                                                              true,
	                                                              "API key for Mistral OCR",
	                                                              true,
	                                                              null);

	@Override
	public String getName() {
		return "mistral-ocr";
	}

	@Override
	public String getDisplayName() {
		return "Mistral OCR";
	}

	@Override
	public String getDescription() {
		return "AI-based online service";
	}

	private String cacheKey = null;
	private List<OCRProcessor> modelsCache = null;
	private static final Base64.Decoder base64Decoder = Base64.getDecoder();
	private static final Pattern DATA_URL = Pattern.compile("data:(image/[^;]+);base64,(.+)=*");

	@Override
	public Collection<OCRProcessor> getAvailableProcessors(Map<String,String> properties)
			throws ServiceDisabledException {
		String apiKey = MISTRAL_APIKEY.getValue(properties);
		if (apiKey == null || "".equals(apiKey))
			throw new ServiceDisabledException("Property not set: " + MISTRAL_APIKEY.getName());
		synchronized (this) {
			if (apiKey.equals(cacheKey))
				return modelsCache;
		}
		try {
			Request request = new Request(MODELS_ENDPOINT)
				.addHeader("Authorization", "Bearer " + apiKey)
				.addHeader("Content-Type", "application/json; utf-8");
			Response response = request.send();
			switch (response.status) {
			case 200: // success
				if (response.body == null)
					throw new IllegalStateException("expected a response body");
				logger.debug(response.body);
				try {
					JSONObject json = new JSONObject(response.body);
					JSONArray data = json.getJSONArray("data");
					if (data != null) {
						List<OCRProcessor> models = new ArrayList<>();
						for (int k = 0; k < data.length(); k++) {
							JSONObject model = data.getJSONObject(k);
							JSONObject capabilities = model.getJSONObject("capabilities");
							if (capabilities != null && capabilities.getBoolean("ocr")) {
								String id = model.getString("id");
								if (id != null) {
									String desc = model.getString("description");
									models.add(new MistralOCRModel(apiKey, id, desc, properties));
								}
							}
						}
						synchronized (this) {
							modelsCache = Collections.unmodifiableList(models);
							cacheKey = apiKey;
						}
						return models;
					}
				} catch (JSONException e) {
				}
				throw new IllegalStateException("could not parse response");
			case 401: // Unauthorized (invalid API key)
			default:
			}
			throw raiseError(response, request);
		} catch (IOException|InterruptedException|RuntimeException e) {
			throw new ServiceDisabledException("Failed to establish a connection to the server", e);
		}
	}

	private static final URI markdownToHTML = URLs.asURI(URLs.getResourceFromJAR("/xml/mistral/markdown-to-html.xpl", MistralOCRService.class));
	private static final Logger logger = LoggerFactory.getLogger(MistralOCRService.class);
	// Resolution of produced images if taken from rendered pages. (If taken directly from
	// source images, the original resulotion is used.)
	// Beware: this variable is specifically set to match Mistral's coordinate system. If
	// you change it, the returned image coordinates need to be processed differently.
	private static final float IMAGE_DPI = 200f;

	private class MistralOCRModel implements OCRProcessor {

		private final String apiKey;
		private final String modelName;
		private final String description;
		private final Map<String,String> properties;

		private MistralOCRModel(String apiKey, String modelName, String description, Map<String,String> properties) {
			this.apiKey = apiKey;
			this.modelName = modelName;
			this.description = description != null ? description : "";
			this.properties = properties;
		}

		@Override
		public String getName() {
			return modelName;
		}

		@Override
		public String getDisplayName() {
			return modelName;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public Collection<Resource> run(Resource input, Map<String,Iterable<String>> options, MessageAppender messages, File resultDir) {
			// for now only support PDF
			if (!(input.getMediaType().isPresent() ? ("application/pdf".equals(input.getMediaType().get())
			                                          || "application/x-pdf".equals(input.getMediaType().get()))
			                                       : input.getPath().toString().endsWith(".pdf")))
				throw new UnsupportedOperationException("Only supports PDF");

			boolean includeImageBase64 = true; // for some reason, extracting the images from the PDF based on the
			                                   // bounding boxes is not reliable enough

			// metadata
			String title = null;
			String author = null;
			Locale language = null;

			// first try to get metadata from PDF directly
			try (PDDocument pdf = PDDocument.load(input.read())) {
				PDDocumentInformation info = pdf.getDocumentInformation();
				title = info.getTitle();
				author = info.getAuthor();
				if ("unknown".equalsIgnoreCase(author))
					author = null;
				PDMetadata metadata = pdf.getDocumentCatalog().getMetadata();
				if (metadata != null)
					try (InputStream s = metadata.createInputStream()) {
						DublinCoreSchema dc = new DomXmpParser().parse(s).getDublinCoreSchema();
						if (title == null)
							title = dc.getTitle();
						if (language == null) {
							List<String> languages = dc.getLanguages();
							if (languages != null && languages.size()> 0)
								language = new Locale.Builder().setLanguageTag(languages.get(0)).build();
						}
					} catch (XmpParsingException e) {
						logger.debug("Failed to extract XMP metadata from PDF", e);
					}
			} catch (IOException e) {
				logger.warn("Failed to extract metadata from PDF", e);
			}
			try {
				String fileId = uploadFile(input);
				JSONObject bboxAnnotationSchema; {
					try {
						bboxAnnotationSchema = new JSONObject()
							.put("name", "bbox_annotation")
							.put("strict", true)
							.put(
								"schema",
								new JSONObject()
								    .put("type", "object")
								    .put("title", "BBOXAnnotation")
								    .put("properties",
								         new JSONObject()
								         .put("short_description",
								              new JSONObject().put("title", "Short_Description")
								                              .put("description", "A short description in English describing the image.")
								                              .put("type", "string"))
								         .put("text_content",
								              new JSONObject().put("title", "Text_Content")
								                              .put("description",
								                                   "The full literal text content of the image, as well-structured HTML."
								                                   + " Must include only the exact text present within the image."
								                                   + " Prefer lists over headings to convey structure."
								                                   + " If the image contains no text, return an empty string.")
								                              .put("type", "string"))
								         .put("functional_index",
								              new JSONObject().put("title", "Functional_Index")
								                              .put("description",
								                                   "A number, between 0 and 10, indicating the functional value of the non-textual"
								                                   + " part of the image, where 0 means that it is purely decorative, and 10 means"
								                                   + " that the image contains important non-textual content.")
								                              .put("type", "integer")))
								    .put("required", new JSONArray().put("text_content")
								                                    .put("functional_index")
								                                    .put("short_description"))
								    .put("additionalProperties", false));
					} catch (JSONException e) {
						throw new IllegalStateException("coding error");
					}
				}
				JSONObject docAnnotationSchema; {
					try {
						docAnnotationSchema = new JSONObject()
							.put("name", "document_annotation")
							.put("strict", true)
							.put(
								"schema",
								new JSONObject()
								    .put("type", "object")
								    .put("title", "DocumentAnnotation")
								    .put("properties",
								         new JSONObject()
								         .put("title",
								              new JSONObject().put("title", "Document title")
								                              .put("type", "string"))
								         .put("author",
								              new JSONObject().put("title", "Document author")
								                              .put("type", "string"))
								         .put("language",
								              new JSONObject().put("title", "Language")
								                              .put("type", "string"))
								         )
								    .put("required", new JSONArray().put("title")
								                                    .put("author")
								                                    .put("language"))
								    .put("additionalProperties", false));
					} catch (JSONException e) {
						throw new IllegalStateException("coding error");
					}
				}
				JSONObject requestBody; {
					try {
						requestBody = new JSONObject()
							.put("model", modelName)
							.put("document", new JSONObject().put("type", "file")
							                                 .put("file_id", fileId))
							.put("extract_header", true)
							.put("extract_footer", true)
							.put("bbox_annotation_format",
							     new JSONObject().put("type", "json_schema")
							                     .put("json_schema", bboxAnnotationSchema))
							.put("include_image_base64", includeImageBase64);
						if (title == null || author == null || language == null)
							requestBody = requestBody.put("document_annotation_format",
							                              new JSONObject().put("type", "json_schema")
							                                              .put("json_schema", docAnnotationSchema));
					} catch (JSONException e) {
						throw new IllegalStateException("coding error");
					}
				}
				Request request = new Request(OCR_ENDPOINT)
					.setMethod("POST")
					.addHeader("Authorization", "Bearer " + apiKey)
					.addHeader("Content-Type", "application/json; utf-8")
					.setContent(requestBody.toString());
				Response response; {
					try (MessageAppender _m = messages != null ? messages.append(new MessageBuilder().withProgress(new BigDecimal(.3))) : null) {
						response = request.send();
					}
				}
				switch (response.status) {
				case 200: // success
					if (response.body == null)
						throw new IllegalStateException("expected a response body");
					logger.debug(response.body);
					Resource markdown = null;
					List<Map<String,Rectangle2D>> imageBoxes = null;
					List<Map<String,String>> imageData = null;
					Map<String,String> imageShortDescriptions = null;
					Map<String,String> imagesTextContent = null;
					List<String> replaceImages = null;
					try {
						JSONObject json = new JSONObject(response.body);
						if (title == null || author == null || language == null) {
							JSONObject docAnnotation = new JSONObject(json.getString("document_annotation"));
							if (title == null)
								title = docAnnotation.isNull("title") ? null : docAnnotation.getString("title");
							if (author == null) {
								author = docAnnotation.isNull("author") ? null : docAnnotation.getString("author");
								if ("unknown".equalsIgnoreCase(author))
									author = null;
							}
							if (language == null)
								language = docAnnotation.isNull("language")
									? null
									: new Locale.Builder().setLanguageTag(docAnnotation.getString("language")).build();
						}
						JSONArray pages = json.getJSONArray("pages");
						if (pages == null)
							throw new RuntimeException("missing pages");
						List<String> markdownPages = new ArrayList<>();
						imageShortDescriptions = new HashMap<>();
						imagesTextContent = new HashMap<>();
						replaceImages = new ArrayList<>();
						imageBoxes = new ArrayList<>();
						if (includeImageBase64)
							imageData = new ArrayList<>();
						for (int p = 0; p < pages.length(); p++) {
							JSONObject pageJson = pages.getJSONObject(p);
							String md = pageJson.getString("markdown");
							if (md == null)
								throw new RuntimeException("missing markdown");
							markdownPages.add(md);
							JSONArray images = pageJson.getJSONArray("images");
							imageBoxes.add(images.length() > 0 ? new HashMap<>() : null);
							if (includeImageBase64)
								imageData.add(images.length() > 0 ? new HashMap<>() : null);
							for (int i = 0; i < images.length(); i++) {
								JSONObject img = images.getJSONObject(i);
								String id = img.getString("id");
								if (id == null)
									throw new RuntimeException("missing image id");
								JSONObject annotation = new JSONObject(img.getString("image_annotation"));
								String desc = annotation.getString("short_description");
								if (desc == null)
									throw new RuntimeException("missing image description");
								imageShortDescriptions.put(id, desc);
								String textContent = annotation.getString("text_content");
								if (textContent != null && !"".equals(textContent) && !"null".equals(textContent))
									imagesTextContent.put(id, textContent);
								Integer functionalIndex = annotation.getInt("functional_index");
								if (functionalIndex != null && functionalIndex <= 3)
									replaceImages.add(id);
								Integer topLeftX = getInteger(img, "top_left_x").get();
								Integer topLeftY = getInteger(img, "top_left_y").get();
								Integer bottomRightX = getInteger(img, "bottom_right_x").get();
								Integer bottomRightY = getInteger(img, "bottom_right_y").get();
								imageBoxes.get(imageBoxes.size() - 1)
								          .put(id, new Rectangle2D.Float(topLeftX,
								                                         topLeftY,
								                                         bottomRightX - topLeftX,
								                                         bottomRightY - topLeftY));
								if (includeImageBase64) {
									String data = img.getString("image_base64");
									if (data == null)
										throw new RuntimeException("missing image data");
									imageData.get(imageData.size() - 1)
									         .put(id, data);
								}
							}
						}

						// <hr> and not <br> because <br> is wrapped in <p>
						String markdownContent = String.join("\n\n<hr role='doc-pagebreak'/>\n\n", markdownPages);
						markdownContent = "_This document was converted from PDF using AI._\n\n" + markdownContent;
						markdown = Resource.load(markdownContent.getBytes(StandardCharsets.UTF_8),
						                         URI.create("index.md"),
						                         "text/markdown");
					} catch (JSONException|RuntimeException e) {
						throw new IllegalStateException("could not parse response", e);
					}
					Set<Resource> images = new HashSet<>();
					Map<String,Integer> imageWidths = new HashMap<>(); {
						try (MessageAppender progress = messages != null
						         ? messages.append(new MessageBuilder().withProgress(new BigDecimal(.5)))
						         : null) {
							BigDecimal portion = progress != null
								? BigDecimal.ONE.divide(new BigDecimal(imageBoxes.size()), MathContext.DECIMAL128)
								: null;
							try (PDDocument pdf = PDDocument.load(input.read())) {
								for (int p = 0; p < imageBoxes.size(); p++) {
									Map<String,Rectangle2D> boxes = imageBoxes.get(p);
									if (boxes != null) {
										for (String id : boxes.keySet())
											imageWidths.put(id, (int)boxes.get(id).getWidth());
										if (includeImageBase64) {
											for (String id : boxes.keySet()) {
												Matcher m = DATA_URL.matcher(imageData.get(p).get(id));
												if (!m.matches())
													throw new IllegalArgumentException("unexpected image data URL");
												images.add(Resource.load(base64Decoder.decode(m.group(2)),
												                         URI.create(id),
												                         m.group(1)));
											}
										} else {
											// FIXME: extractRegion appears to be unreliable when it makes use of the source images
											PDPage page = pdf.getPage(p);
											List<ImageInfo> pdfImages = null; //getImageInfo(page);
											int pageIndex = p;
											Supplier<BufferedImage> renderedPage = Suppliers.memoize(
												() -> {
													try {
														return new PDFRenderer(pdf).renderImageWithDPI(pageIndex, IMAGE_DPI); }
													catch (IOException e) {
														throw new UncheckedIOException(e); }});
											for (String id : boxes.keySet()) {
												ByteArrayOutputStream data = new ByteArrayOutputStream();
												ImageIO.write(extractRegion(page, renderedPage, pdfImages, boxes.get(id)),
												              "png", // FIXME: should match file extension
												              data);
												images.add(Resource.load(data.toByteArray(), URI.create(id), "image/png"));
											}
										}
									}
									if (progress != null)
										progress.append(new MessageBuilder().withProgress(portion)).close();
								}
							}
						}
					}
					// convert the markdown to HTML
					Map<String,String> metadata = new HashMap<>(); {
						if (title != null)
							metadata.put("title", title);
						if (author != null)
							metadata.put("author", author);
						if (language != null)
							metadata.put("language", language.toLanguageTag());
					}
					File tempDir = new File(resultDir, "tmp");
					resultDir = new File(resultDir, "result");
					tempDir.mkdirs();
					markdown = markdown.copy(URLs.resolve(URLs.asURI(tempDir), markdown.getPath())).store();
					for (Resource image : images)
						image.copy(URLs.resolve(URLs.asURI(tempDir), image.getPath())).store();
					List<Resource> htmlFileset = new ArrayList<>();
					SaxonBuffer buffer = new SaxonBuffer();
					try (MessageAppender xprocMessages = messages != null ? messages.append(new MessageBuilder().withProgress(new BigDecimal(.2))) : null) {
						xprocEngine
							.load(markdownToHTML)
							.run(
								new XProcInput.Builder()
								              .withOption(new QName("source"), markdown.getPath())
								              .withOption(new QName("metadata"), metadata)
								              .withOption(new QName("result-dir"), URLs.asURI(resultDir))
								              .withOption(new QName("image-descriptions"), imageShortDescriptions)
								              .withOption(new QName("image-text-content"), imagesTextContent)
								              .withOption(new QName("image-sizes"), imageWidths)
								              .withOption(new QName("replace-images"), replaceImages)
								              .build(),
								() -> xprocMessages,
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
					htmlFileset.addAll(Fileset.unmarshall(buffer.asInput().asXMLStreamReader()));
					return htmlFileset;
				case 422: // Unprocessable Content
				case 500: // Internal Server Error
				default:
				}
				throw raiseError(response, request);
			} catch (IOException|InterruptedException|XMLStreamException|RuntimeException e) {
				throw new RuntimeException("OCR conversion could not be performed", e);
			}
		}

		// file size may not exceed 512 MB
		private String uploadFile(Resource file) {
			try {
				Request request = new Request(FILES_ENDPOINT)
					.setMethod("POST")
					.addHeader("Authorization", "Bearer " + apiKey)
					.setFormDataContent("file", file)
					.setFormDataContent("purpose", "ocr");
				Response response = request.send();
				switch (response.status) {
				case 200: // success
					if (response.body == null)
						throw new IllegalStateException("expected a response body");
					logger.debug(response.body);
					try {
						JSONObject json = new JSONObject(response.body);
						String id = json.getString("id");
						if (id != null)
							return id;
					} catch (JSONException e) {
					}
					throw new IllegalStateException("could not parse response");
				default:
				}
				throw raiseError(response, request);
			} catch (IOException|InterruptedException|RuntimeException e) {
				throw new RuntimeException("failed to upload file", e);
			}
		}
	}

	private static Optional<Integer> getInteger(JSONObject json, String key) {
		Object o = json.opt(key);
		if (o == null)
			return Optional.empty();
		else if (o instanceof String)
			return Optional.of(Integer.parseInt((String)o));
		else if (o instanceof Integer)
			return Optional.of((Integer)o);
		else
			throw new IllegalArgumentException("JSONObject[\"" + key + "\"] can not be converted to an integer");
	}

	static class ImageInfo {
		BufferedImage image;
		Rectangle2D bounds;
	}

	private static List<ImageInfo> getImageInfo(PDPage page) throws IOException {
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

	private static BufferedImage extractRegion(PDPage page,
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

	private static RuntimeException raiseError(Response response, Request request) {
		String message = "Response code " + response.status + " from " + request.getConnection().getURL();
		Throwable cause = response.exception;
		try {
			JSONObject errorJson = null; {
				if (response.error != null) {
					logger.debug(response.error);
					errorJson = new JSONObject(response.error);
					String detail = errorJson.getString("detail");
					if (detail != null) {
						message = message + ": " + detail;
						cause = null;
					} else {
						detail = errorJson.getString("message");
						if (detail != null) {
							message = message + ": " + detail;
							cause = null;
						}
					}
				}
			}
		} catch (JSONException e) {
		}
		return new RuntimeException(message, cause);
	}

	private XProcEngine xprocEngine = null;

	@Reference(
		name = "XProcEngine",
		unbind = "-",
		service = XProcEngine.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setXProcEngine(XProcEngine engine) {
		xprocEngine = engine;
	}
}
