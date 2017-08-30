package org.daisy.pipeline.client.filestorage;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.daisy.pipeline.client.models.Argument;

public class JobValidator {
	
	// Compile some patterns for validating certain XML types
	private static final String NameStartChar = "[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF"+
												"\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD"+
												"\\u10000-\\uEFFFF]";
    private static final String NameChar = NameStartChar+"\\-\\.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040";
    private static final String Name = "["+NameStartChar+"]["+NameChar+"]*";
    private static final String BaseChar = "\\u0041-\\u005A\\u0061-\\u007A\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u00FF\\u0100-\\u0131"+
    										"\\u0134-\\u013E\\u0141-\\u0148\\u014A-\\u017E\\u0180-\\u01C3\\u01CD-\\u01F0\\u01F4-\\u01F5"+
											"\\u01FA-\\u0217\\u0250-\\u02A8\\u02BB-\\u02C1\\u0386\\u0388-\\u038A\\u038C\\u038E-\\u03A1"+
											"\\u03A3-\\u03CE\\u03D0-\\u03D6\\u03DA\\u03DC\\u03DE\\u03E0\\u03E2-\\u03F3\\u0401-\\u040C"+
											"\\u040E-\\u044F\\u0451-\\u045C\\u045E-\\u0481\\u0490-\\u04C4\\u04C7-\\u04C8\\u04CB-\\u04CC"+
											"\\u04D0-\\u04EB\\u04EE-\\u04F5\\u04F8-\\u04F9\\u0531-\\u0556\\u0559\\u0561-\\u0586"+
											"\\u05D0-\\u05EA\\u05F0-\\u05F2\\u0621-\\u063A\\u0641-\\u064A\\u0671-\\u06B7\\u06BA-\\u06BE"+
											"\\u06C0-\\u06CE\\u06D0-\\u06D3\\u06D5\\u06E5-\\u06E6\\u0905-\\u0939\\u093D\\u0958-\\u0961"+
											"\\u0985-\\u098C\\u098F-\\u0990\\u0993-\\u09A8\\u09AA-\\u09B0\\u09B2\\u09B6-\\u09B9"+
											"\\u09DC-\\u09DD\\u09DF-\\u09E1\\u09F0-\\u09F1\\u0A05-\\u0A0A\\u0A0F-\\u0A10\\u0A13-\\u0A28"+
											"\\u0A2A-\\u0A30\\u0A32-\\u0A33\\u0A35-\\u0A36\\u0A38-\\u0A39\\u0A59-\\u0A5C\\u0A5E"+
											"\\u0A72-\\u0A74\\u0A85-\\u0A8B\\u0A8D\\u0A8F-\\u0A91\\u0A93-\\u0AA8\\u0AAA-\\u0AB0"+
											"\\u0AB2-\\u0AB3\\u0AB5-\\u0AB9\\u0ABD\\u0AE0\\u0B05-\\u0B0C\\u0B0F-\\u0B10\\u0B13-\\u0B28"+
											"\\u0B2A-\\u0B30\\u0B32-\\u0B33\\u0B36-\\u0B39\\u0B3D\\u0B5C-\\u0B5D\\u0B5F-\\u0B61"+
											"\\u0B85-\\u0B8A\\u0B8E-\\u0B90\\u0B92-\\u0B95\\u0B99-\\u0B9A\\u0B9C\\u0B9E-\\u0B9F"+
											"\\u0BA3-\\u0BA4\\u0BA8-\\u0BAA\\u0BAE-\\u0BB5\\u0BB7-\\u0BB9\\u0C05-\\u0C0C\\u0C0E-\\u0C10"+
											"\\u0C12-\\u0C28\\u0C2A-\\u0C33\\u0C35-\\u0C39\\u0C60-\\u0C61\\u0C85-\\u0C8C\\u0C8E-\\u0C90"+
											"\\u0C92-\\u0CA8\\u0CAA-\\u0CB3\\u0CB5-\\u0CB9\\u0CDE\\u0CE0-\\u0CE1\\u0D05-\\u0D0C"+
											"\\u0D0E-\\u0D10\\u0D12-\\u0D28\\u0D2A-\\u0D39\\u0D60-\\u0D61\\u0E01-\\u0E2E\\u0E30"+
											"\\u0E32-\\u0E33\\u0E40-\\u0E45\\u0E81-\\u0E82\\u0E84\\u0E87-\\u0E88\\u0E8A\\u0E8D"+
											"\\u0E94-\\u0E97\\u0E99-\\u0E9F\\u0EA1-\\u0EA3\\u0EA5\\u0EA7\\u0EAA-\\u0EAB\\u0EAD-\\u0EAE"+
											"\\u0EB0\\u0EB2-\\u0EB3\\u0EBD\\u0EC0-\\u0EC4\\u0F40-\\u0F47\\u0F49-\\u0F69\\u10A0-\\u10C5"+
											"\\u10D0-\\u10F6\\u1100\\u1102-\\u1103\\u1105-\\u1107\\u1109\\u110B-\\u110C\\u110E-\\u1112"+
											"\\u113C\\u113E\\u1140\\u114C\\u114E\\u1150\\u1154-\\u1155\\u1159\\u115F-\\u1161\\u1163"+
											"\\u1165\\u1167\\u1169\\u116D-\\u116E\\u1172-\\u1173\\u1175\\u119E\\u11A8\\u11AB"+
											"\\u11AE-\\u11AF\\u11B7-\\u11B8\\u11BA\\u11BC-\\u11C2\\u11EB\\u11F0\\u11F9\\u1E00-\\u1E9B"+
											"\\u1EA0-\\u1EF9\\u1F00-\\u1F15\\u1F18-\\u1F1D\\u1F20-\\u1F45\\u1F48-\\u1F4D\\u1F50-\\u1F57"+
											"\\u1F59\\u1F5B\\u1F5D\\u1F5F-\\u1F7D\\u1F80-\\u1FB4\\u1FB6-\\u1FBC\\u1FBE\\u1FC2-\\u1FC4"+
											"\\u1FC6-\\u1FCC\\u1FD0-\\u1FD3\\u1FD6-\\u1FDB\\u1FE0-\\u1FEC\\u1FF2-\\u1FF4\\u1FF6-\\u1FFC"+
											"\\u2126\\u212A-\\u212B\\u212E\\u2180-\\u2182\\u3041-\\u3094\\u30A1-\\u30FA\\u3105-\\u312C"+
											"\\uAC00-\\uD7A3";
    private static final String Ideographic = "\\u4E00-\\u9FA5\\u3007\\u3021-\\u3029";
    private static final String Letter = BaseChar+Ideographic;
    private static final String Digit = "\\u0030-\\u0039\\u0660-\\u0669\\u06F0-\\u06F9\\u0966-\\u096F\\u09E6-\\u09EF\\u0A66-\\u0A6F"+
											"\\u0AE6-\\u0AEF\\u0B66-\\u0B6F\\u0BE7-\\u0BEF\\u0C66-\\u0C6F\\u0CE6-\\u0CEF\\u0D66-\\u0D6F"+
											"\\u0E50-\\u0E59\\u0ED0-\\u0ED9\\u0F20-\\u0F29";
    private static final String CombiningChar = "\\u0300-\\u0345\\u0360-\\u0361\\u0483-\\u0486\\u0591-\\u05A1\\u05A3-\\u05B9"+
											"\\u05BB-\\u05BD\\u05BF\\u05C1-\\u05C2\\u05C4\\u064B-\\u0652\\u0670\\u06D6-\\u06DC"+
											"\\u06DD-\\u06DF\\u06E0-\\u06E4\\u06E7-\\u06E8\\u06EA-\\u06ED\\u0901-\\u0903\\u093C"+
											"\\u093E-\\u094C\\u094D\\u0951-\\u0954\\u0962-\\u0963\\u0981-\\u0983\\u09BC\\u09BE\\u09BF"+
											"\\u09C0-\\u09C4\\u09C7-\\u09C8\\u09CB-\\u09CD\\u09D7\\u09E2-\\u09E3\\u0A02\\u0A3C\\u0A3E"+
											"\\u0A3F\\u0A40-\\u0A42\\u0A47-\\u0A48\\u0A4B-\\u0A4D\\u0A70-\\u0A71\\u0A81-\\u0A83\\u0ABC"+
											"\\u0ABE-\\u0AC5\\u0AC7-\\u0AC9\\u0ACB-\\u0ACD\\u0B01-\\u0B03\\u0B3C\\u0B3E-\\u0B43"+
											"\\u0B47-\\u0B48\\u0B4B-\\u0B4D\\u0B56-\\u0B57\\u0B82-\\u0B83\\u0BBE-\\u0BC2\\u0BC6-\\u0BC8"+
											"\\u0BCA-\\u0BCD\\u0BD7\\u0C01-\\u0C03\\u0C3E-\\u0C44\\u0C46-\\u0C48\\u0C4A-\\u0C4D"+
											"\\u0C55-\\u0C56\\u0C82-\\u0C83\\u0CBE-\\u0CC4\\u0CC6-\\u0CC8\\u0CCA-\\u0CCD\\u0CD5-\\u0CD6"+
											"\\u0D02-\\u0D03\\u0D3E-\\u0D43\\u0D46-\\u0D48\\u0D4A-\\u0D4D\\u0D57\\u0E31\\u0E34-\\u0E3A"+
											"\\u0E47-\\u0E4E\\u0EB1\\u0EB4-\\u0EB9\\u0EBB-\\u0EBC\\u0EC8-\\u0ECD\\u0F18-\\u0F19\\u0F35"+
											"\\u0F37\\u0F39\\u0F3E\\u0F3F\\u0F71-\\u0F84\\u0F86-\\u0F8B\\u0F90-\\u0F95\\u0F97"+
											"\\u0F99-\\u0FAD\\u0FB1-\\u0FB7\\u0FB9\\u20D0-\\u20DC\\u20E1\\u302A-\\u302F\\u3099\\u309A";
    private static final String Extender = "\\u00B7\\u02D0\\u02D1\\u0387\\u0640\\u0E46\\u0EC6\\u3005"+
											"\\u3031-\\u3035\\u309D-\\u309E\\u30FC-\\u30FE";
    private static final String NCNameChar = Letter+Digit+"\\.\\-_"+CombiningChar+Extender;
	private static final String NCName = "["+Letter+"_]["+NCNameChar+"]*";
	private static final String ID = Name;
	private static final String IDREF = Name;
	//private static final String ENTITY = Name;
	//private static final String Nmtoken = "["+NameChar+"]+";
	private static final String QName = "(["+NCName+"]:)?"+NCName;
	private static final String language = "[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*";
	private static final Pattern languagePattern = Pattern.compile(language, Pattern.CANON_EQ);
	private static final Pattern namePattern = Pattern.compile(Name, Pattern.CANON_EQ);
	private static final Pattern ncnamePattern = Pattern.compile(NCName, Pattern.CANON_EQ);
	private static final Pattern idPattern = Pattern.compile(ID, Pattern.CANON_EQ);
	private static final Pattern idrefPattern = Pattern.compile(IDREF, Pattern.CANON_EQ);
	//private static final Pattern entityPattern = Pattern.compile(ENTITY, Pattern.CANON_EQ);
	//private static final Pattern nmtokenPattern = Pattern.compile(Nmtoken, Pattern.CANON_EQ);
	private static final Pattern qnamePattern = Pattern.compile(QName, Pattern.CANON_EQ);
	private static final String timezone = "([+-](0\\d|1[0-4]):[0-5]\\d|Z)";
	private static final String year = "-?\\d\\d\\d\\d+";
	private static final String month = "(0[1-9]|1[012])";
	private static final String day = "(0[1-9]|[12]\\d|3[01])";
	private static final String date = year+"-"+month+"-"+day;
	private static final String time = "(24:00:00(\\.0+)?|([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(\\.\\d+)?)";
	private static final Pattern datePattern = Pattern.compile("^"+date+"("+timezone+")?$"); // unless dateIsYear0000
	private static final Pattern timePattern = Pattern.compile("^"+time+"("+timezone+")?$");
	private static final Pattern dateTimePattern = Pattern.compile("^"+date+"T"+time+"("+timezone+")?$"); // unless dateIsYear0000
	private static final Pattern dateIsYear0000 = Pattern.compile("^-?0000.*$");
	private static final Pattern durationPattern = Pattern.compile("^-?P(\\d+Y)?(\\d+M)?(\\d+D)?T(\\d+H)?(\\d+M)?(\\d+S(\\.\\d+))?$");
	private static final Pattern durationPatternAtLeastOne = Pattern.compile("^.*P\\d.*[^T]$"); // additional restriction to durationPattern
	private static final Pattern gYearMonthPattern = Pattern.compile("^"+year+"-"+month+"("+timezone+")?$");
	private static final Pattern gYearPattern = Pattern.compile("^"+year+"("+timezone+")?$");
	private static final Pattern gMonthDayPattern = Pattern.compile("^--"+month+"-"+day+"("+timezone+")?$");
	private static final Pattern gDayPattern = Pattern.compile("^---"+day+"("+timezone+")?$");
	private static final Pattern gMonthPattern = Pattern.compile("^--"+month+"("+timezone+")?$");
	
	/**
	 * Check if the input or option is filled out properly.
	 * 
	 * For instance, if the input or option is not a sequence, there must be exactly one value defined.
	 * 
	 * @param arg The argument to validate
	 * @param context The job context, needed if you want to validate that files in file arguments exist on disk
	 * @return a message describing the error, or null if there is no error
	 */
	public static synchronized String validate(Argument arg, JobStorage context) {
		String beginText = "The "+arg.getKind()+" '"+(arg.getNicename() == null || "".equals(arg.getNicename()) ? arg.getName() : arg.getNicename())+"'";

		// is required ?
		if (!arg.isDefined() && arg.getRequired()) {
			return beginText+" is required.";
		}

		// default value
		if (!arg.isDefined() && !arg.getRequired()) {
			return null;
		}

		// is sequence ?
		if (!arg.getSequence() && arg.size() != 1) {
			return beginText+" must have exactly one value, "+(arg.size() == 0 ? "no value" : arg.size()+" values")+" are currently given.";
		}

		String type = arg.getType();
		if (type != null) {
			String[] typeSplit = type.split(":");
			type = typeSplit[typeSplit.length-1];
			
			for (String value : arg.getAsList()) {
				if (value == null) {
					return beginText+" contains a undefined value.";
				}
				
				switch (arg.getType()) {

				case "string":
					// nothing to validate, all strings are strings
					break;

				case "boolean":
					if (!"true".equals(value) && !"false".equals(value)) {
						return beginText+" must be either 'true' or 'false'.";
					}
					break;

				case "float":
					try {
						new Float(value);
					} catch (NumberFormatException e) {
						return beginText+" must be a floating point number";
					}
					break;

				case "double":
				case "decimal":
					try {
						new Double(value);
					} catch (NumberFormatException e) {
						return beginText+" must be a decimal number";
					}
					break;

				case "positiveInteger":
				case "nonNegativeInteger":
					try {
						long l = new Long(value);
						if (l < 0L) {
							return beginText+" must be a positive integer";
						}
					} catch (NumberFormatException e) {
						return beginText+" must be a positive integer";
					}
					break;

				case "nonPositiveInteger":
				case "negativeInteger":
					try {
						long l = new Long(value);
						if (l > 0L) {
							return beginText+" must be a negative integer";
						}
					} catch (NumberFormatException e) {
						return beginText+" must be a negative integer";
					}
					break;

				case "integer":
				case "int":
				case "long":
					try {
						new Long(value);
					} catch (NumberFormatException e) {
						return beginText+" must be a integer";
					}
					break;

					
				case "language":
					if (!languagePattern.matcher(value).matches()) {
						return beginText+" must be a valid language code according to RFC 3066.";
					}
					break;

				case "ID":
					if (!idPattern.matcher(value).matches()) {
						return beginText+" must be a valid XML ID.";
					}
					break;

				case "IDREF":
					if (!idrefPattern.matcher(value).matches()) {
						return beginText+" must be a valid XML IDREF.";
					}
					break;

				case "Name":
					if (!namePattern.matcher(value).matches()) {
						return beginText+" must be a valid XML Name.";
					}
					break;

				case "NCName":
					if (!ncnamePattern.matcher(value).matches()) {
						return beginText+" must be a valid XML NCName.";
					}
					break;

				case "QName":
					if (!qnamePattern.matcher(value).matches()) {
						return beginText+" must be a valid XML QName.";
					}
					break;
					
				case "ENTITY":
					if (!qnamePattern.matcher(value).matches()) {
						return beginText+" must be a valid XML ENTITY.";
					}
					break;
					
				case "NMTOKEN":
					if (!qnamePattern.matcher(value).matches()) {
						return beginText+" must be a valid XML NMTOKEN.";
					}
					break;
					
				case "date":
					if (!datePattern.matcher(value).matches()) {
						return beginText+" must be a valid ISO 8601 date string; for instance \"2015-08-25\".";
					}
					if (dateIsYear0000.matcher(value).matches()) {
						return beginText+" must be a valid ISO 8601 date string; year 0 is not allowed.";
					}
					break;
					
				case "time":
					if (!timePattern.matcher(value).matches()) {
						return beginText+" must be a valid ISO 8601 time string; for instance \"07:47:13\".";
					}
					break;
				
				case "dateTime":
					if (!dateTimePattern.matcher(value).matches()) {
						return beginText+" must be a valid ISO 8601 date-time; for instance \"2015-08-25T07:47:13\".";
					}
					if (dateIsYear0000.matcher(value).matches()) {
						return beginText+" must be a valid ISO 8601 date string; year 0 is not allowed.";
					}
					break;
				
				case "duration":
					if (!durationPattern.matcher(value).matches() || !durationPatternAtLeastOne.matcher(value).matches()) {
						return beginText+" must be a valid ISO 8601 duration string; for instance \"P2DT3H20M\".";
					}
					break;
				
				case "gYearMonth":
					if (!gYearMonthPattern.matcher(value).matches()) {
						return beginText+" must be a valid year-month; for instance \"2015-08\".";
					}
					break;
				
				case "gYear":
					if (!gYearPattern.matcher(value).matches()) {
						return beginText+" must be a valid year; for instance \"2015\".";
					}
					break;
					
				
				case "gMonthDay":
					if (!gMonthDayPattern.matcher(value).matches()) {
						return beginText+" must be a valid month-day; for instance \"08-25\".";
					}
					break;
				
				case "gDay":
					if (!gDayPattern.matcher(value).matches()) {
						return beginText+" must be a valid day number from 01 to 31.";
					}
					break;
				
				case "gMonth":
					if (!gMonthPattern.matcher(value).matches()) {
						return beginText+" must be a valid month number from 01 to 12.";
					}
					break;
					
				case "anyURI":
					try {
						URI uri = new URI(value);
						
						if (arg.getOutput() == null) { // input type => validate that it refers to context files
							if (uri.getScheme() != null || value.startsWith("/")) {
								return beginText+" must be a relative URI";
							}
							if (context != null && !context.existsInContext(value)) {
								return beginText+" must refer to a directory that exists in the job context.";
							}
//							if (directory.isDirectory()) { // only validate presence of context files if job is stored to disk
//								File targetFile = new File(new File(directory, "context"), value);
//								if (!targetFile.exists()) {
//									return beginText+" must refer to a file or directory that exists in the job context.";
//								}
//							}
							
						} else { // output type => validate that the URI is an absolute file: URI
							if ("file".equals(uri.getScheme())) {
								return beginText+" must be an absolute file: URI"+(value.startsWith("/") ? "; it must start with 'file:/' and not simply '/'" : "");
							}
							File targetFile = new File(uri);
							if (!(targetFile.isDirectory() || targetFile.getParentFile().isDirectory())) {
								return beginText+" must refer to a directory that exists or a file in a directory that exists.";
							}
							
						}
						
					} catch (URISyntaxException e) {
						return beginText+" must be a URI.";
					}
					break;
					
				case "anyDirURI":
					try {
						URI uri = new URI(value);
						
						if (arg.getOutput() == null) { // input type => validate that it refers to context files
							if (uri.getScheme() != null || value.startsWith("/")) {
								return beginText+" must be a relative URI";
							}
							if (context != null && !context.isDirectoryInContext(value)) {
								return beginText+" must refer to a directory that exists in the job context.";
							}
//							if (directory.isDirectory()) { // only validate presence of context files if job is stored to disk
//								File targetFile = new File(new File(directory, "context"), value);
//								if (!targetFile.exists()) {
//									return beginText+" must refer to a directory that exists in the job context.";
//								}
//							}
							
						} else { // output type => validate that the URI is an absolute file: URI
							if (!"file".equals(uri.getScheme())) {
								return beginText+" must be an absolute file: URI"+(value.startsWith("/") ? "; it must start with 'file:/' and not simply '/'" : "");
							}
							File targetFile = new File(uri);
							if (!targetFile.isDirectory()) {
								return beginText+" must refer to a directory that exists.";
							}
							
						}
						
					} catch (URISyntaxException e) {
						return beginText+" must be a URI.";
					}
					break;
					
				case "anyFileURI":
					try {
						URI uri = new URI(value);
						
						if (arg.getOutput() == null) { // input type => validate that it refers to context files
							if (uri.getScheme() != null || value.startsWith("/")) {
								return beginText+" must be a relative URI";
							}
							if (context != null && !context.isFileInContext(value)) {
								return beginText+" must refer to a directory that exists in the job context.";
							}
//							if (directory.isDirectory()) { // only validate presence of context files if job is stored to disk
//								File targetFile = new File(new File(directory, "context"), value);
//								if (!targetFile.exists()) {
//									return beginText+" must refer to a file that exists in the job context.";
//								}
//							}
							
							// NOTE: We could validate mediatype here, but let's not. It would probably not be reliable enough, and performance could be an issue.
							
						} else { // output type => validate that the URI is an absolute file: URI
							if ("file".equals(uri.getScheme())) {
								return beginText+" must be an absolute file: URI"+(value.startsWith("/") ? "; it must start with 'file:/' and not simply '/'" : "");
							}
							File targetFile = new File(uri);
							if (!targetFile.getParentFile().isDirectory()) {
								return beginText+" must refer to a file in a directory that exists.";
							}
							
						}
						
					} catch (URISyntaxException e) {
						return beginText+" must be a URI.";
						}
						break;
					}
				}
			}
			
			return null;
		}
}
