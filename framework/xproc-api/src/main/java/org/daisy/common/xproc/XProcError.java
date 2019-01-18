package org.daisy.common.xproc;

import javax.xml.transform.SourceLocator;

public abstract class XProcError {
	
	public abstract String getCode();
	
	public abstract String getMessage();
	
	public abstract XProcError getCause();
	
	public abstract SourceLocator[] getLocation();
	
	// adapted from java.lang.Throwable
	private String printEnclosedLocation(SourceLocator[] enclosingLocation) {
		StringBuilder s = new StringBuilder();
		if (getCode() != null) {
			s.append("[").append(getCode()).append("]");
			if (getMessage() != null)
				s.append(" ").append(getMessage());
		} else
			s.append(getMessage());
		SourceLocator[] loc = getLocation();
		int m = loc.length - 1;
		int n = enclosingLocation.length - 1;
		while (m >= 0 && n >=0 && loc[m].equals(enclosingLocation[n])) {
			m--;
			n--;
		}
		int inCommon = loc.length - 1 - m;
		for (int i = 0; i <= m; i++)
			s.append("\n\tat " + loc[i]);
		if (inCommon != 0)
			s.append("\n\t... " + inCommon + " more"); // in Logback: "... X common frames omitted"
		XProcError cause = getCause();
		if (cause != null) {
			s.append("\nCaused by: ");
			s.append(cause.printEnclosedLocation(loc));
		}
		return s.toString();
	}
	
	@Override
	public String toString() {
		return printEnclosedLocation(new SourceLocator[]{});
	}
}
