package utils;

import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.Pipeline2Logger.LEVEL;
import play.Logger;

/**
 * Implementation of a logger for the Pipeline 2 Client Library. 
 * @author jostein
 */
@SuppressWarnings("unused")
public class Pipeline2PlayLogger extends Pipeline2Logger {
	
	private LEVEL level = LEVEL.INFO;

	@Override
	public void setLevel(LEVEL level) {
		if (level != null)
			this.level = level;
	}
	
	@Override
	public boolean logsLevel(LEVEL level) {
		return this.level.ordinal() <= level.ordinal();
	}
	
	@Override
	public void debug(String message) { Logger.debug("[WS clientlib] "+message); }
	public void debug(String message, Exception e) { Logger.debug("[WS clientlib] "+message, e); }

	@Override
	public void error(String message) { Logger.error("[WS clientlib] "+message); }
	public void error(String message, Exception e) { Logger.error("[WS clientlib] "+message, e); }

	@Override
	public void fatal(String message) { Logger.error("[WS clientlib] "+message); }
	public void fatal(String message, Exception e) { Logger.error("[WS clientlib] "+message, e); }

	@Override
	public void info(String message) { Logger.info("[WS clientlib] "+message); }
	public void info(String message, Exception e) { Logger.info("[WS clientlib] "+message, e); }

	@Override
	public void trace(String message) { Logger.trace("[WS clientlib] "+message); }
	public void trace(String message, Exception e) { Logger.trace("[WS clientlib] "+message, e); }

	@Override
	public void warn(String message) { Logger.warn("[WS clientlib] "+message); }
	public void warn(String message, Exception e) { Logger.warn("[WS clientlib] "+message, e); }

	@Override
	public LEVEL getLevel() {
		return level;
	}

}
