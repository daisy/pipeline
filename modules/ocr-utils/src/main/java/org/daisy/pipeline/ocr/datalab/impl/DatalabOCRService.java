package org.daisy.pipeline.ocr.datalab.impl;

import java.io.File;
import java.math.BigDecimal;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.daisy.common.file.Resource;
import org.daisy.common.messaging.MessageAppender;
import org.daisy.common.messaging.MessageBuilder;
import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.pipeline.common.rest.Request;
import org.daisy.pipeline.common.rest.Response;
import static org.daisy.pipeline.ocr.impl.OCRServiceHelper.getMetadataFromPDF;
import static org.daisy.pipeline.ocr.impl.OCRServiceHelper.getImageFromBase64String;
import static org.daisy.pipeline.ocr.impl.OCRServiceHelper.linkFootnotes;
import static org.daisy.pipeline.ocr.impl.OCRServiceHelper.markdownToHTML;
import org.daisy.pipeline.ocr.impl.OCRServiceHelper.Metadata;
import org.daisy.pipeline.ocr.OCRProcessor;
import org.daisy.pipeline.ocr.OCRService;
import org.daisy.pipeline.script.ScriptOption;

import org.json.JSONException;
import org.json.JSONObject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "datalab",
	service = { OCRService.class }
)
public class DatalabOCRService implements OCRService {

	private static final URL USER_HEALTH_ENDPOINT;
	private static final URL CONVERT_ENDPOINT;

	static {
		// this is a hidden parameter, it is meant to be used in tests only
		String BASE_URL = Properties.getProperty("org.daisy.pipeline.ocr.datalab.address",
		                                         false,
		                                         "Base URL of Datalab API",
		                                         false,
		                                         "https://www.datalab.to/api/v1")
			.getValue();
		try {
			USER_HEALTH_ENDPOINT = new URL(BASE_URL + "/user_health");
			CONVERT_ENDPOINT = new URL(BASE_URL + "/convert");
		} catch (MalformedURLException e) {
			throw new IllegalStateException("coding error", e);
		}
	}

	/* package private for tests */
	static final Property DATALAB_APIKEY = Properties.getProperty("org.daisy.pipeline.ocr.datalab.apikey",
	                                                              true,
	                                                              "API key for Datalab",
	                                                              true,
	                                                              null);

	@Override
	public String getName() {
		return "datalab";
	}

	@Override
	public String getDisplayName() {
		return "Datalab";
	}

	@Override
	public String getDescription() {
		return "AI-based online service";
	}

	@Override
	public Iterable<ScriptOption> getOptions() {
		List<ScriptOption> options = new ArrayList<>();
		// FIXME: the "paginate" option of the /convert API does not seem to work anymore
		// options.add(CommonOptions.INCLUDE_PAGE_NUMBERS);
		return options;
	}

	private String cacheKey = null;
	private List<OCRProcessor> modelsCache = null;

	@Override
	public Collection<OCRProcessor> getAvailableProcessors(Map<String,String> properties)
			throws ServiceDisabledException {
		String apiKey = DATALAB_APIKEY.getValue(properties);
		if (apiKey == null || "".equals(apiKey))
			throw new ServiceDisabledException("Property not set: " + DATALAB_APIKEY.getName());
		synchronized (this) {
			if (apiKey.equals(cacheKey))
				return modelsCache;
		}
		try {
			Request request = new Request(USER_HEALTH_ENDPOINT)
				.addHeader("X-API-Key", apiKey)
				.addHeader("User-Agent", "curl/8.0");
			Response response = request.send();
			switch (response.status) {
			case 200: // success
				if (response.body == null)
					throw new IllegalStateException("expected a response body");
				logger.debug(response.body);
				List<OCRProcessor> models = new ArrayList<>();
				models.add(new DatalabOCRModel(apiKey, ProcessingMode.BALANCED, properties));
				models.add(new DatalabOCRModel(apiKey, ProcessingMode.FAST, properties));
				models.add(new DatalabOCRModel(apiKey, ProcessingMode.ACCURATE, properties));
				synchronized (this) {
					modelsCache = Collections.unmodifiableList(models);
					cacheKey = apiKey;
				}
				return models;
			case 401: // Unauthorized (invalid API key)
			case 403: // Forbidden
			default:
			}
			throw raiseApiError(response, request);
		} catch (IOException|InterruptedException|RuntimeException e) {
			throw new ServiceDisabledException("Failed to establish a connection to the server", e);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(DatalabOCRService.class);

	private enum ProcessingMode {

		FAST("Prioritizes processing speed while maintaining good results."),
		BALANCED("The best trade-off between speed and accuracy."),
		ACCURATE("Prioritizes the highest-quality results for complex documents.");

		private final String desc;

		ProcessingMode(String desc) {
			this.desc =desc;
		}

		String getName() {
			return toString().toLowerCase();
		}

		String getDescription() {
			return desc;
		}
	};

	private class DatalabOCRModel implements OCRProcessor {

		private final String apiKey;
		private final ProcessingMode mode;
		private final Map<String,String> properties;

		private DatalabOCRModel(String apiKey, ProcessingMode mode, Map<String,String> properties) {
			this.apiKey = apiKey;
			this.mode = mode;
			this.properties = properties;
		}

		@Override
		public String getName() {
			return mode.getName();
		}

		@Override
		public String getDisplayName() {
			return mode.getName() + " mode";
		}

		@Override
		public String getDescription() {
			return mode.getDescription();
		}

		@Override
		public Collection<Resource> run(Resource input, Map<String,Iterable<String>> options, MessageAppender messages, File resultDir) {
			// for now only support PDF
			if (!(input.getMediaType().isPresent() ? ("application/pdf".equals(input.getMediaType().get())
			                                          || "application/x-pdf".equals(input.getMediaType().get()))
			                                       : input.getPath().toString().endsWith(".pdf")))
				throw new UnsupportedOperationException("Only supports PDF");

			// options
			boolean includePageNumbers = OCRProcessor.getBooleanOption(options, CommonOptions.INCLUDE_PAGE_NUMBERS);

			try {
				URL checkUrl; {
					JSONObject requestBody; {
						try {
							requestBody = new JSONObject()
								.put("output_format", "markdown")
								.put("mode", mode.getName())
								.put("paginate", includePageNumbers);
						} catch (JSONException e) {
							throw new IllegalStateException("coding error");
						}
					}
					Request request = new Request(CONVERT_ENDPOINT)
						.setMethod("POST")
						.addHeader("X-API-Key", apiKey)
						.addHeader("User-Agent", "curl/8.0")
						.setFormDataContent("file", input)
						.setFormDataContent("data", requestBody.toString());
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
						try {
							JSONObject json = new JSONObject(response.body);
							String url = json.getString("request_check_url");
							if (url == null)
								throw new RuntimeException("missing request_check_url");
							try {
								checkUrl = new URL(url);
							} catch (MalformedURLException e) {
								throw new RuntimeException("invalid URL: " + url);
							}
						} catch (JSONException | RuntimeException e) {
							throw new IllegalStateException("could not parse response", e);
						}
						break;
					default:
						throw raiseApiError(response, request);
					}
				}

				// poll request_check_url
				Resource markdown = null;
				Set<Resource> images = new HashSet<>(); {
					Request request = new Request(checkUrl)
						.setMethod("GET")
						.addHeader("X-API-Key", apiKey)
						.addHeader("User-Agent", "curl/8.0");
					try (MessageAppender _m = messages != null ? messages.append(new MessageBuilder().withProgress(new BigDecimal(.4))) : null) {
						int retry = 30;
						poll: while (retry-- > 0) {
							Response response = request.send();
							switch (response.status) {
								case 200: // success
									if (response.body == null)
										throw new IllegalStateException("expected a response body");
									logger.debug(response.body);
									try {
										JSONObject json = new JSONObject(response.body);
										String status = json.getString("status");
										if (status == null)
											throw new RuntimeException("missing status");
										if ("complete".equals(status)) {
											String markdownContent = json.getString("markdown");
											if (markdownContent == null)
												throw new RuntimeException("missing markdown");
											markdownContent = linkFootnotes(markdownContent);
											if (includePageNumbers)
												markdownContent = String.join(
													"\n\n<hr role='doc-pagebreak'/>\n\n",
													markdownContent.split("\\{\\d+\\}------------------------------------------------"));
											markdownContent = "_This document was converted from PDF using AI._\n\n" + markdownContent;
											markdown = Resource.load(markdownContent.getBytes(StandardCharsets.UTF_8),
											                         URI.create("index.md"),
											                         "text/markdown");
											JSONObject imageData = json.getJSONObject("images");
											if (imageData != null)
												for (String id : JSONObject.getNames(imageData))
													images.add(getImageFromBase64String(imageData.getString(id), URI.create(id), null));
											break poll;
										}
									} catch (JSONException | RuntimeException e) {
										throw new IllegalStateException("could not parse response", e);
									}
									break;
								default:
									throw raiseApiError(response, request);
							}
							request.cancel();
							Thread.sleep(2000);
						}
					}
				}

				// get metadata from PDF
				Metadata metadata = getMetadataFromPDF(input, logger);

				// convert the markdown to HTML
				File tempDir = new File(resultDir, "tmp");
				resultDir = new File(resultDir, "result");
				tempDir.mkdirs();
				try (MessageAppender xprocMessages = messages != null ? messages.append(new MessageBuilder().withProgress(new BigDecimal(.3))) : null) {
					return markdownToHTML(xprocEngine, xprocMessages, logger, properties, tempDir, resultDir, markdown, metadata,
					                      images, null, null, null, null);
				}
			} catch (IOException|InterruptedException|RuntimeException e) {
				throw new RuntimeException("OCR conversion could not be performed", e);
			}
		}
	}

	private static RuntimeException raiseApiError(Response response, Request request) {
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
