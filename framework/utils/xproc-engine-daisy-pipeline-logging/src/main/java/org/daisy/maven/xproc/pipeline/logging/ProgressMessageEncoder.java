package org.daisy.maven.xproc.pipeline.logging;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.core.pattern.PatternLayoutEncoderBase;

import org.slf4j.helpers.MessageFormatter;

public class ProgressMessageEncoder extends PatternLayoutEncoderBase<ILoggingEvent> {
	
	@Override
	public void start() {
		PatternLayout patternLayout = new PatternLayout() {
			@Override
			public Map<String,String> getEffectiveConverterMap() {
				Map<String,String> map = new HashMap<String,String>(super.getEffectiveConverterMap());
				map.put("progress", ProgressConverter.class.getName());
				map.put("indent", IndentConverter.class.getName());
				map.put("m", MessageConverter.class.getName());
				map.put("msg", MessageConverter.class.getName());
				map.put("message", MessageConverter.class.getName());
				return map;
			}
		};
		patternLayout.setContext(context);
		patternLayout.setPattern(getPattern());
		patternLayout.setOutputPatternAsHeader(outputPatternAsHeader);
		patternLayout.start();
		this.layout = patternLayout;
		super.start();
	}
	
	public static class MessageConverter extends ClassicConverter {
		public String convert(ILoggingEvent event) {
			Object[] args = event.getArgumentArray();
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					if (args[i] instanceof FlattenedProgressMessage) {
						args[i] = ((FlattenedProgressMessage)args[i]).getText();
					}
				}
				return MessageFormatter.arrayFormat(event.getMessage(), args).getMessage();
			}
			return event.getFormattedMessage();
		}
	}
	
	public static class ProgressConverter extends ClassicConverter {
		public String convert(ILoggingEvent event) {
			FlattenedProgressMessage message = getProgressMessageFromArgumentArray(event.getArgumentArray());
			if (message != null) {
				BigDecimal progress = message.getProgress();
				if (progress != null)
					return progress.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN)
					               .setScale(0, RoundingMode.HALF_UP)
					               .toPlainString() + "%";
			}
			return "";
		}
	}
	
	public static class IndentConverter extends ClassicConverter {
		
		private String baseOffset;
		private String childIndicator;
		
		@Override
		public void start() {
			List<String> options = getOptionList();
			if (options != null && options.size() >= 1 && options.size() <= 2) {
				baseOffset = options.get(0);
				childIndicator = options.size() == 2 ? options.get(1) : null;
			}
		}

		public String convert(ILoggingEvent event) {
			FlattenedProgressMessage message = getProgressMessageFromArgumentArray(event.getArgumentArray());
			if (message != null) {
				return indent(message.getDepth());
			} else {
				return "";
			}
		}
		
		private String indent(int depth) {
			if (depth > 0) {
				StringBuilder indent = new StringBuilder();
				if (childIndicator != null)
					depth--;
				for (int i = 0; i < depth; i++)
					indent.append(baseOffset);
				if (childIndicator != null)
					indent.append(childIndicator);
				return indent.toString(); }
			else
				return "";
		}
	}
	
	private static FlattenedProgressMessage getProgressMessageFromArgumentArray(Object[] args) {
		if (args == null) {
			return null;
		} else {
			FlattenedProgressMessage message = null;
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof FlattenedProgressMessage) {
					if (message != null) {
						return null;
					}
					message = (FlattenedProgressMessage)args[i];
				}
			}
			return message;
		}
	}
}
