package org.daisy.pipeline.tts.aws.impl;

import java.util.Map;
import java.util.Optional;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Component;

/**
 * @author mmartida - original author
 * @author Nicolas Pavie - update for pipeline-modules 1.15+
 */
@Component(
	name = "aws-tts-service",
	service = { TTSService.class }
)
public class AWSPollyTTSService implements TTSService {

	private static final Property AWS_ACCESS_KEY = Properties.getProperty(
		"org.daisy.pipeline.tts.aws.accesskey",
		true,
		"Access key for Amazon Polly speech engine",
		true,
		null
	);

	private static final Property AWS_SECRET_KEY = Properties.getProperty(
		"org.daisy.pipeline.tts.aws.secretkey",
		true,
		"Secret key for Amazon Polly speech engine",
		true,
		null);

	private static final Property AWS_REGION = Properties.getProperty(
		"org.daisy.pipeline.tts.aws.region",
		true,
		"Region for Amazon Polly speech engine",
		true,
		null);

	private static final Property AWS_PRIORITY = Properties.getProperty(
		"org.daisy.pipeline.tts.aws.priority",
		true,
		"Priority of Amazon voices relative to voices of other engines",
		false,
		"15");

	@Override
	public AWSPollyTTSEngine newEngine(Map<String,String> properties)
			throws ServiceDisabledException, SynthesisException {

		String accessKey = AWS_ACCESS_KEY.getValue(properties);
		if (accessKey == null || "".equals(accessKey))
			throw new ServiceDisabledException("Property not set: " + AWS_ACCESS_KEY.getName());
		String secretKey = AWS_SECRET_KEY.getValue(properties);
		if (secretKey == null || "".equals(secretKey))
			throw new ServiceDisabledException("Property not set: " + AWS_SECRET_KEY.getName());
		String region = AWS_REGION.getValue(properties);
		if (region == null || "".equals(region))
			throw new ServiceDisabledException("Property not set: " + AWS_REGION.getName());
		int priority = getPropertyAsInt(properties, AWS_PRIORITY).get();
		return new AWSPollyTTSEngine(this, accessKey, secretKey, region, priority);
	}

	private static Optional<Integer> getPropertyAsInt(Map<String,String> properties, Property prop)
			throws SynthesisException {

		String str = prop.getValue(properties);
		if (str != null) {
			try {
				return Optional.of(Integer.valueOf(str));
			} catch (NumberFormatException e) {
				throw new SynthesisException(str + " is not a valid a value for property " + prop.getName());
			}
		}
		return Optional.empty();
	}

	@Override
	public String getName() {
		return "aws";
	}

	@Override
	public String getDisplayName() {
		return "Amazon";
	}
}
