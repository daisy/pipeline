/*
 * LimSee3
 * A cross-platform multimedia authoring tool
 *
 * Copyright (C) INRIA. All rights reserved.
 * For details on use and redistribution please refer to [$HOME/Licence.txt].
 */
package org.daisy.dotify.common.xml;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Contains utility methods related to URI creation and manipulation.
 * <p/>
 * </p>
 *
 * @author Romain Deltour
 * @see URI
 */
final class URIUtils {
    
    /**
     * URI encoded values 0-255, for convenience
     */
    private final static String[] hex = { "%00", "%01", "%02", "%03", "%04", "%05",
        "%06", "%07", "%08", "%09", "%0A", "%0B", "%0C", "%0D", "%0E",
        "%0F", "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
        "%18", "%19", "%1A", "%1B", "%1C", "%1D", "%1E", "%1F", "%20",
        "%21", "%22", "%23", "%24", "%25", "%26", "%27", "%28", "%29",
        "%2A", "%2B", "%2C", "%2D", "%2E", "%2F", "%30", "%31", "%32",
        "%33", "%34", "%35", "%36", "%37", "%38", "%39", "%3A", "%3B",
        "%3C", "%3D", "%3E", "%3F", "%40", "%41", "%42", "%43", "%44",
        "%45", "%46", "%47", "%48", "%49", "%4A", "%4B", "%4C", "%4D",
        "%4E", "%4F", "%50", "%51", "%52", "%53", "%54", "%55", "%56",
        "%57", "%58", "%59", "%5A", "%5B", "%5C", "%5D", "%5E", "%5F",
        "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67", "%68",
        "%69", "%6A", "%6B", "%6C", "%6D", "%6E", "%6F", "%70", "%71",
        "%72", "%73", "%74", "%75", "%76", "%77", "%78", "%79", "%7A",
        "%7B", "%7C", "%7D", "%7E", "%7F", "%80", "%81", "%82", "%83",
        "%84", "%85", "%86", "%87", "%88", "%89", "%8A", "%8B", "%8C",
        "%8D", "%8E", "%8F", "%90", "%91", "%92", "%93", "%94", "%95",
        "%96", "%97", "%98", "%99", "%9A", "%9B", "%9C", "%9D", "%9E",
        "%9F", "%A0", "%A1", "%A2", "%A3", "%A4", "%A5", "%A6", "%A7",
        "%A8", "%A9", "%AA", "%AB", "%AC", "%AD", "%AE", "%AF", "%B0",
        "%B1", "%B2", "%B3", "%B4", "%B5", "%B6", "%B7", "%B8", "%B9",
        "%BA", "%BB", "%BC", "%BD", "%BE", "%BF", "%C0", "%C1", "%C2",
        "%C3", "%C4", "%C5", "%C6", "%C7", "%C8", "%C9", "%CA", "%CB",
        "%CC", "%CD", "%CE", "%CF", "%D0", "%D1", "%D2", "%D3", "%D4",
        "%D5", "%D6", "%D7", "%D8", "%D9", "%DA", "%DB", "%DC", "%DD",
        "%DE", "%DF", "%E0", "%E1", "%E2", "%E3", "%E4", "%E5", "%E6",
        "%E7", "%E8", "%E9", "%EA", "%EB", "%EC", "%ED", "%EE", "%EF",
        "%F0", "%F1", "%F2", "%F3", "%F4", "%F5", "%F6", "%F7", "%F8",
        "%F9", "%FA", "%FB", "%FC", "%FD", "%FE", "%FF" };
    
    /**
     * <p>
     * The different parts of a URI.
     * </p>
     */
    private enum UriPart {
        SCHEME(2, SCHEME_CHAR),
        SSP(-1, SSP_CHAR),
        AUTHORITY(4, AUTHORITY_CHAR),
        USERINFO(2, USERINFO_CHAR),
        HOST(3, REG_HOST_CHAR),
        PORT(7, PORT_CHAR),
        PATH(5, PATH_CHAR),
        QUERY(7, QUERY_CHAR),
        FRAGMENT(9, FRAGMENT_CHAR);
        /**
         * The index of the capturing group representing this part in the regex pattern.
         */
        private final int mGroup;
        /**
         * The set of characters allowed in this part a URI.
         */
        private final String mChars;

        UriPart(int group, String chars) {
            mGroup = group;
            mChars = chars;
        }
    }

    /**
     * <p>
     * Represents possible values for URI schemes. Only enumerates schemes that are
     * supported in the application.
     * </p>
     */
    enum Scheme {
        /**
         * The scheme for HyperText Transfer Protocol URIs.
         */
        HTTP,
        /**
         * The scheme for host-specific file names URIs.
         */
        FILE;

        /**
         * <p>
         * Whether the specified string represent this scheme.
         * </p>
         *
         * @param str the string to test
         * @return <code>true</code> if and only if <code>str</code> is equal to this scheme's string
         *         (case insensitive)
         */
        boolean isSchemeFor(String str) {
            return toString().equalsIgnoreCase(str);
        }

        /**
         * <p>
         * Returns the lowercase name of this enum constant, as contained in the
         * declaration.
         * </p>
         *
         * @return the lowercase name of this enum constant
         */
        @Override
		public String toString() {
            return super.toString().toLowerCase();
        }

        /**
         * <p>
         * Returns the scheme represented by the specified string.
         * </p>
         *
         * @param str The string to test.
         * @return the scheme represented by <code>str</code> (ignoring case) or <code>null</code>
         *         if not found.
         */
        static Scheme getScheme(String str) {
            for (Scheme scheme : EnumSet.allOf(Scheme.class)) {
                if (scheme.isSchemeFor(str)) {
                    return scheme;
                }
            }
            return null;
        }
    }

    /**
     * The URI regular expression (taken from <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC3986</a>).
     */
    private static final Pattern URI_PATTERN = Pattern.compile("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?");
    /**
     * The URI Authority regular expression.
     */
    private static final Pattern AUTHORITY_PATTERN = Pattern.compile("(([^?#]*)@)?((\\[[^?#]*\\])|([^?#:]*))(:([0-9]*))?");
    /**
     * The regular expression matching IP Hosts (syntax only, not validating illegal characters).
     */
    private static final String IPHOST_REGEX = "(.+\\..+\\..+\\..+)|(\\[.*\\])";
    /**
     * The set of digital characters.
     */
    private static final String DIGIT = "0123456789";
    /**
     * The set of alphabetical characters.
     */
    private static final String ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * The set of unreserved characters allowed in a URI.
     */
    private static final String UNRESERVED = ALPHA + DIGIT + "-._~";
    /**
     * The set of subcomponent delimiters reserved characters allowed in a URI.
     */
    private static final String SUB_DELIMS = "!$&'()*+,;=";
    /**
     * The set of characters allowed in the user info of a URI (as specified in <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC3986</a>).
     */
    private static final String SCHEME_CHAR = ALPHA + DIGIT + "+-.";
    /**
     * The set of characters allowed in the scheme specific part of a URI (as specified in <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC3986</a>).
     */
    private static final String SSP_CHAR = UNRESERVED + '%' + SUB_DELIMS + ":@/?";
    /**
     * The set of characters allowed in the authority of a URI (as specified in <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC3986</a>).
     */
    private static final String AUTHORITY_CHAR = UNRESERVED + '%' + SUB_DELIMS + ":@[]";
    /**
     * The set of characters allowed in the user info of a URI (as specified in <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC3986</a>).
     */
    private static final String USERINFO_CHAR = UNRESERVED + '%' + SUB_DELIMS + ':';
    /**
     * The set of characters allowed in the reg-name host of a URI (as specified in <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC3986</a>).
     */
    private static final String REG_HOST_CHAR = UNRESERVED + '%' + SUB_DELIMS;
    /**
     * The set of characters allowed in the port of a URI (as specified in <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC3986</a>).
     */
    private static final String PORT_CHAR = DIGIT;
    /**
     * The set of characters allowed in the path of a URI (as specified in <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC3986</a>).
     */
    private static final String PATH_CHAR = UNRESERVED + '%' + SUB_DELIMS + ":@/";
    /**
     * The set of characters allowed in the query of a URI (as specified in <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC3986</a>).
     */
    private static final String QUERY_CHAR = PATH_CHAR + '?';
    /**
     * The set of characters allowed in the fragment of a URI (as specified in <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC3986</a>).
     */
    private static final String FRAGMENT_CHAR = PATH_CHAR + '?';

    // Avoids instanciation
    private URIUtils() {
    }

    /**
     * <p>
     * Tries to construct a URI by parsing the specified string.
     * </p>
     * <p>
     * It differs from the {@link URI#URI(String)} method (and thus from
     * <a href="http://www.ietf.org/rfc/rfc3986.txt">the RFC 3986</a>) in that
     * non encoded characters are tolerated in the URI parts.
     * </p>
     * <p>
     * For instance the string <code>"file:///c:/Program Files/"</code> will
     * be accepted and encoded in <code>"file:///c:/Program%20Files/"</code>
     * </p>
     * <p>
     * Actually, the specified string is first parsed syntaxically only and
     * matched with the regular expression specified in Appendix B of
     * <a href="http://www.ietf.org/rfc/rfc3986.txt">the RFC 3986</a>, and
     * then a multi-argument URI constructor is called, thus implicitly
     * encoding the URI parts when required.
     * </p>
     * <p>
     * Note: As specified in <a href="http://www.ietf.org/rfc/rfc3986.txt">the RFC 3986</a>,
     * registry-based naming authorities are now defined within the <code>host</code> subpart
     * therefore the <code>authority</code> is always parsed as a server-based authority.
     * </p>
     * <p>
     * Note: Whereas it is specified in <a href="http://www.ietf.org/rfc/rfc3986.txt">the RFC 3986</a>
     * that percent-encoded subdelimiters should not be decoded before processing of the URI, this
     * method explicitly decodes percent-encoded slashes (%2F) found in the path part of the URI,
     * as a slash character is anyway a delimiter for a path, encoded or not.
     * </p>
     *
     * @param spec the string to be parsed into a URI.
     * @return the URI specified in the specified string.
     * @throws URISyntaxException    if the specified string violates RFC 3986, as augmented by the above deviations.
     * @throws NullArgumentException if <code>spec</code> is <code>null</code>.
     * @see URI#URI(String)
     */
    static URI createURI(String spec) throws URISyntaxException {
        if (spec == null) {
            throw new IllegalArgumentException("spec");
        }
        boolean isHierarchical;
        boolean isIPHost = false;
        String scheme;
        String authority;
        String userInfo = null;
        String host = null;
        int port = -1;
        String path;
        String query;
        String fragment;
        Matcher uriMatcher = URI_PATTERN.matcher(spec);
        if (uriMatcher.matches()) {
            scheme = uriMatcher.group(UriPart.SCHEME.mGroup);
            authority = uriMatcher.group(UriPart.AUTHORITY.mGroup);
            path = uriMatcher.group(UriPart.PATH.mGroup);
            query = uriMatcher.group(UriPart.QUERY.mGroup);
            fragment = uriMatcher.group(UriPart.FRAGMENT.mGroup);
            isHierarchical = (scheme == null) || (authority != null) || ((path != null) && (path.startsWith("/")));
            if (isHierarchical && authority != null && authority.length() != 0)
            { // We parse the authority to conform the modification in RFC 3986.
                Matcher authMatcher = AUTHORITY_PATTERN.matcher(authority);
                if (authMatcher.matches()) {
                    userInfo = authMatcher.group(UriPart.USERINFO.mGroup);
                    host = authMatcher.group(UriPart.HOST.mGroup);
                    if (host == null || host.length() == 0) {
                        throw new URISyntaxException(spec, " authority does not specify a host");
                    }
                    isIPHost = host.matches(IPHOST_REGEX);
                    String portStr = authMatcher.group(UriPart.PORT.mGroup);
                    try {
                        port = ((portStr == null) || portStr.length() == 0) ? -1 : Integer.parseInt(portStr);
                    } catch (NumberFormatException e) {
                        URISyntaxException use = new URISyntaxException(spec, " port part contains non digit chars");
                        use.initCause(e);
                        throw use;
                    }
                } else {
                    throw new URISyntaxException(spec, " authority does not match the URI regular expression");
                }
            }
            StringBuilder newSpec = new StringBuilder();
            try {
                if (scheme != null) {
                    newSpec.append(scheme).append(':');
                }
                if (isHierarchical && authority != null) {
                    newSpec.append("//");
                    if (authority.length() != 0) {
                        if (userInfo != null) {
                            newSpec.append(encode(decode(userInfo, "%"), UriPart.USERINFO)).append('@');
                        }
                        if (isIPHost) {
                            newSpec.append(host);
                        } else {
                            newSpec.append(encode(decode(host, "%"), UriPart.HOST));
                        }
                        if (port != -1) {
                            newSpec.append(':').append(port);
                        }
                    }
                }
                if (path != null) {
                    newSpec.append(encodePath(decode(path, "%")));
                }
                if (query != null) {
                    newSpec.append('?').append(encode(decode(query, "%"), UriPart.QUERY));
                }
                if (fragment != null) {
                    newSpec.append('#').append(encode(decode(fragment, "%"), UriPart.FRAGMENT));
                }
            } catch (IllegalArgumentException e) {
                URISyntaxException use = new URISyntaxException(spec, " contains illegal characters");
                use.initCause(e);
                throw use;
            }
            return new URI(newSpec.toString());
        } else {
            throw new URISyntaxException(spec, "does not match the URI regular expression");
        }
    }

    /**
     * <p>
     * Returns a new string resulting from decoding all percent-encoded
     * characters in the specified string.
     * </p>
     *
     * @param str the string to decode (can be <code>null</code>).
     * @return the decoded string or <code>null</code> if the specified string was <code>null</code>.
     * @throws IllegalArgumentException if the specified string contains invalid percent-encoded characters.
     * @see #decode(String, String)
     */
    static String decode(String str) {
        return decode(str, null);
    }

    /**
     * <p>
     * Returns a new string resulting from decoding all percent-encoded
     * characters in the specified string except for the chars present in
     * <code>skipChars</code>.
     * </p>
     *
     * @param str       the string to decode (can be <code>null</code>).
     * @param skipChars the chars that will not be decoded.
     * @return the decoded string or <code>null</code> if the specified string was <code>null</code>.
     * @throws IllegalArgumentException if the specified string contains invalid percent-encoded characters.
     */
	static String decode(String str, String skipChars) {
		// LE 2008-10-01: rewritten to support multi byte characters
		if (str == null) {
			return null;
		}
		try {
			// We first encode '+' to ensure it won't be decoded as a space
			String res = URLDecoder.decode(str.replace("+", "%2B"), "UTF-8");
			if (skipChars != null && skipChars.length() > 0) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < res.length(); i++) {
					char c = res.charAt(i);
					if (skipChars.indexOf(c) != -1) {
						sb
								.append(URLEncoder.encode(String.valueOf(c),
										"UTF-8"));
					} else {
						sb.append(c);
					}
				}
				return sb.toString();
			} else {
				return res;
			}
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("shoudn't happen", e);
		}
	}

    /**
     * <p>
     * Encode in the specified string all the characters not allowed in the specified URI part.
     * </p>
     * <p>
     * Replaces illegal characters by their percent-encoding representation
     * ("%" + hexadecimal code) in the UTF-8 encoding.
     * </p>
     *
     * @param str  the string to percent-encode.
     * @param part the URI part reference.
     * @return the encoded string (for URI part illegal characters) or <code>null</code> if the specified string was <code>null</code>.
     */
    private static String encode(String str, UriPart part) {
        // LE 2008-10-01: support for non us-ascii chars added
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (part.mChars.indexOf(c) == -1) {
                if ((int) c <= 127) {
                    sb.append(hex[c]);
                } else if (!Character.isISOControl(c) && !Character.isSpaceChar(c)) {
                    // Remove this if clause to also encode characters in the 'other' category
                    sb.append(c);
                } else if (c <= 0x07FF) { // non-ASCII <= 0x7FF
                    sb.append(String.valueOf(hex[0xc0 | (c >> 6)] + 
                            hex[0x80 | (c & 0x3F)]));
                } else { // 0x7FF < c <= 0xFFFF
                    sb.append(String.valueOf(hex[0xe0 | (c >> 12)] + 
                            hex[0x80 | ((c >> 6) & 0x3F)] + 
                            hex[0x80 | (c & 0x3F)]));
                }                
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * <p>
     * Return a string where all colon characters found in the first
     * segment of the specified path are percent encoded.
     * </p>
     * <p>
     * This can be really useful when trying to create a new relative URI from this path, to
     * prevent it for being parsed as an opaque URI with a scheme.
     * </p>
     * <p>
     * See the following example for a better understanding of this method:
     * <blockquote>
     * <code>&lt;b:b:b/b:b?b:b&gt;</code> is rewritten as <code>&lt;b%3ab%3ab/b:b?b:b&gt;</code><br/>
     * </blockquote>
     * </p>
     *
     * @param path the path to encode
     * @return a new string where the colons of the first segment are percent-encoded,
     *         or <code>null</code> if the specified path is null.
     */
    static String encodeColon(CharSequence path) {
        if (path == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(path.length());
        boolean stop = false;
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (!stop && (c == ':')) {
                sb.append("%3A");
            } else {
                stop = stop || (c == '?') || (c == '/');
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * <p>
     * Return a string where all illegal characters and all colon characters found in the first
     * segment of the specified path are percent encoded.
     * </p>
     * <p>
     * This method is acts as calling sucessively {@link #encode(String, UriPart)}
     * and {@link #encodeColon(CharSequence)} on <code>path</code>.
     * </p>
     *
     * @param path the path to encode
     * @return a new string where the colons of the first segment and other illegal characters
     *         are percent-encoded or <code>null</code> if the specified path is null.
     */
    static String encodePath(String path) {
        return encodeColon(encode(path, UriPart.PATH));
    }

    /**
     * <p>
     * Tries to normalize the specified <code>file</code> URI by following a set of rules that are
     * considered common practice in the major implementations (mostly browsers).
     * </p>
     * <p>
     * The <em>normalization</em> is specified by the following rules:
     * </p>
     * <ul>
     * <li>If the URI specifies a non-empty host different to <code>localhost</code>, or denotes
     * a UNC path, the resulting URI will have the form:
     * <blockquote><pre>file://host/path</pre></blockquote>
     * <p>Note that if the authority is empty but the path starts with exactly two slashes, the
     * URI is written so the authority is the first path segment and the path is the other path
     * segements. This is to conform with MS Internet Explorer interpretation of
     * <code>&lt;file://host/share/path&gt;</code> as the UNC name <code>\\host\share\path</code>
     * whereas <code>File.toURI()</code> method returns <code>&lt;file:////host/share/path&gt;</code>
     * for the same UNC name.</p>
     * <p>Examples of normalizations following this rules are:</p>
     * <blockquote>
     * <code>&lt;file://host/path&gt;</code> is unchanged.<br/>
     * <code>&lt;file://host&gt;</code> is rewritten as <code>&lt;file://host/&gt;</code><br/>
     * <code>&lt;file:////host/share/path&gt;</code> is rewritten as <code>&lt;file://host/share/path&gt;</code><br/>
     * </blockquote>
     * </li>
     * <li>If the URI specifies an absolute file path, the resulting URI will have the form:
     * <blockquote><pre>file:///path</pre></blockquote>
     * <p>Note that the path may specify a Windows drive letter:</p>
     * <blockquote><pre>file:///c:/path</pre></blockquote>
     * <p>Note also regardless of how many slashes start the scheme specific part, if a drive letter
     * is specified jsut after, the URI will be recognized as an absolute file path.</p>
     * <p>Examples of normalizations following this rules are:</p>
     * <blockquote>
     * <code>&lt;file:/dir/path&gt;</code> is rewritten as <code>&lt;file:///dir/path&gt;</code><br/>
     * <code>&lt;file:c:/dir/file.tmp&gt;</code> is rewritten as <code>&lt;file:///c:/dir/file.tmp&gt;</code><br/>
     * <code>&lt;file:/c:/dir/file.tmp&gt;</code> is rewritten as <code>&lt;file:///c:/dir/file.tmp&gt;</code><br/>
     * <code>&lt;file://localhost/c:/dir/file.tmp&gt;</code> is rewritten as <code>&lt;file:///c:/dir/file.tmp&gt;</code><br/>
     * <code>&lt;file://c:/dir/file.tmp&gt;</code> is rewritten as <code>&lt;file:///c:/dir/file.tmp&gt;</code><br/>
     * <code>&lt;file:////&gt;</code> is rewritten as <code>&lt;file:///&gt;</code>
     * </blockquote>
     * </li>
     * </ul>
     * <li>If the URI specifies a relative file path, the resulting URI will have the form:
     * <blockquote><pre>rel-path</pre></blockquote>
     * <p>Note that colon charachters (<code>':'</code>) possibly present in the first segment
     * of a path are percent encoded (<code>'%3A'</code>) so that they shall not be interpreted
     * as scheme separator.</p>
     * <p>Note also that a path specifying a drive letter without a slash is considered a
     * Windows relative path (the drive lettre is kept in the resulting path):</p>
     * <blockquote><code>c%3Apath</code> is the normal form of <code>file:///c:path</code></blockquote>
     * <p>Examples of normalizations following this rules are:</p>
     * <blockquote>
     * <code>&lt;file://///dir/path&gt;</code> is rewritten as <code>&lt;file:///dir/path&gt;</code><br/>
     * <code>&lt;file:dir/file.tmp&gt;</code> is rewritten as <code>&lt;dir/file.tmp&gt;</code><br/>
     * <code>&lt;file:dir:dir/path&gt;</code> is rewritten as <code>&lt;dir%3Adir/path&gt;</code><br/>
     * <code>&lt;file:///c:dir/file.tmp&gt;</code> is rewritten as <code>&lt;c%3Adir/file.tmp&gt;</code><br/>
     * <code>&lt;file:///c:&gt;</code> is rewritten as <code>&lt;c%3A&gt;</code>
     * </blockquote>
     * </li>
     * </ul>
     * <p>
     * Note that the current <code>file</code> scheme specification is defined in section 3.10 of RFC 1738,
     * but is quite unprecise and useless. The normal form used here is actually roughly inspired on the
     * <a href="http://offset.skew.org/wiki/URI/File_scheme">wiki preparing a <code>file</code> scheme
     * draft</a> that will be submitted to the IETF in future.
     * </p>
     *
     * @param uri the <code>file</code> URI to normalize.
     * @return a URI equivalent to the interpretation of the specified <code>file</code> URI as described above.
     * @throws NullArgumentException    if <code>uri</code> is <code>null</code>.
     * @throws IllegalArgumentException if <code>uri</code> is not a <code>file</code> URI.
     * @see java.io.File
     */
    static URI normalizeFileURI(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri");
        }
        if (!Scheme.FILE.isSchemeFor(uri.getScheme())) {
            throw new IllegalArgumentException("The specified URI is not a file URI!");
        }
        String ssp = uri.getRawSchemeSpecificPart();
        String query = uri.getRawQuery();
        StringBuilder pathSb = new StringBuilder(ssp.length());
        boolean isNormal = true;
        int slashCount = 0;
        // Construct the path part :
        // - decoded encoded slashes (%2F)
        // - count starting slashes
        // - if not in normal form, set isNormal to false
        int i = 0;
        boolean stop = false;
        boolean isStartingSlash = true;
        while (!stop && i < ssp.length()) {
            char c = ssp.charAt(i);
            if (ssp.length() > i + 2 && c == '%' && ssp.charAt(i + 1) == '2' && (ssp.charAt(i + 2) == 'F'
                                                                                 || ssp.charAt(i + 2) == 'f')) {
                isNormal = false;
                i += 2;
                c = '/';
            }
            if (c == '/') {
                slashCount = (isStartingSlash) ? ++slashCount : slashCount;
                if (slashCount == 4) {
                    isNormal = false;
                }
                if (slashCount < 4) {
                    pathSb.append('/');
                }
            } else if (c == '?') {
                stop = true;
                if (slashCount == 0) {// the query must be stored as it is not recognized by this opaque URI
                    query = ssp.substring(i + 1);
                }
            } else {
                isStartingSlash = false;
                if (slashCount == 1) {
                    isNormal = false;
                    pathSb.append("//");
                    slashCount = 3;
                }
                if (slashCount == 4) {
                    isNormal = false;
                    pathSb.deleteCharAt(0);
                    slashCount = 2;
                }
                if (slashCount > 4) {
                    isNormal = false;
                    slashCount = 3;
                }
                pathSb.append(c);
            }
            i++;
        }
        // Remove possible "localhost" host
        if (slashCount == 2 && pathSb.length() > 10 && "localhost".equals(pathSb.substring(2, 11))) {//
            isNormal = false;
            pathSb.delete(2, 11);
            if (pathSb.length() == 2) {// add trailing slash if the uri jsut contained the host <file://localhost>
                pathSb.append('/');
            }
            slashCount = 3;
        }
        // Process possible drive letter
        if (pathSb.length() >= slashCount + 4
            && (pathSb.charAt(slashCount + 3) == 'A' || pathSb.charAt(slashCount + 3) == 'a')
            && pathSb.charAt(slashCount + 2) == '3'
            && pathSb.charAt(slashCount + 1) == '%'
            && Character.isLetter(pathSb.charAt(slashCount))) {// decode colon if any
            isNormal = false;
            pathSb.replace(slashCount + 1, slashCount + 4, ":");
        }
        if (pathSb.length() >= slashCount + 2
            && pathSb.charAt(slashCount + 1) == ':'
            && Character.isLetter(pathSb.charAt(slashCount))) {// a drive letter has been found
            char driveLetter = pathSb.charAt(slashCount);
            if (Character.isUpperCase(driveLetter)) {// upper case drive letter made lower case
                isNormal = false;
                pathSb.setCharAt(slashCount, Character.toLowerCase(driveLetter));
            }
            if (pathSb.length() < slashCount + 3 || pathSb.charAt(slashCount + 2) != '/')
            {// relative path with drive letter
                isNormal = false;
                pathSb.delete(0, slashCount);//remove starting slashes
                slashCount = 0;
            } else if (slashCount != 3) {// <file:c:/path> or <file://c:/path> form
                isNormal = false;
                pathSb.replace(0, slashCount, "///");
                slashCount = 3;
            }
        }
        // Process the different slashCount values
        if (slashCount == 0) {// <file:rel-path> form
            isNormal = false;
        }
        if (slashCount == 2) {// <file://host/path> or <file://c:path> form
            if (pathSb.length() == 2) {// the path is empty - same result as empty path with three slashes <file:///>
                isNormal = false;
                pathSb.append('/');
                slashCount = 3;
            } else {// UNC file (<file://unc-path> or <file://host/path> form)
                if (pathSb.substring(2).lastIndexOf('/') == -1) {// host <file://host> is made <file://host/>
                    isNormal = false;
                    pathSb.append('/');
                }
            }
        }
        if (isNormal) {
            return uri;
        } else {
            // Note: must not use multi-arg constructor as it percent encode '%' chars used in percent-encoding
            StringBuilder newSpec = new StringBuilder(pathSb.length() + 4);
            if (slashCount == 0) {
                newSpec.append(encodeColon(pathSb));
            } else {
                newSpec.append(Scheme.FILE.toString()).append(':').append(pathSb);
            }
            if (query != null) {
                newSpec.append('?').append(query);
            }
            if (uri.getRawFragment() != null) {
                newSpec.append('#').append(uri.getRawFragment());
            }
            try {
                return new URI(newSpec.toString());
            } catch (URISyntaxException e) {
                throw new Error("The URI spec should be well formed.", e); // Shouldn't happen
            }
        }
    }

	/**
	 * Resolves the second URI argument against the first URI argument.
	 * 
	 * <p>
	 * This method overrides the {@link URI#resolve(URI)} method only for
	 * <code>file</code> URIs representing UNC paths (i.e. of the form
	 * <code>file:////server/path</code> or <code>file://server/path</code>).
	 * </p>
	 * <p>
	 * The problem with the {@link URI#resolve(URI)} method was for
	 * round-tripping with {@link File} objects: for a file at the UNC Location
	 * <code>//server/file</code>, the {@link File#toURI()} method returns
	 * <code>file:////server/file</code>. Now, if this latter URI is used for
	 * resolution or is normalized, it results in <code>file:/server/file</code>
	 * , which doesn't give the original file object with the constructor
	 * {@link File#File(URI)}.
	 * </p>
	 * <p>
	 * To circumvent this round-tripping problem, this method resolves UNC-like
	 * file URIs with an empty host and a path starting with two slashes (like
	 * <code>file:////server/path</code>).
	 * </p>
	 * 
	 * @param reference
	 *            the reference URI against which the second argument will be
	 *            resolved
	 * @param uri
	 *            the URI to resolve
	 * @return the resulting URI
	 */
	static URI resolve(URI reference, URI uri) {
		if (Scheme.FILE.isSchemeFor(reference.getScheme())
				&& (reference.getHost() != null || reference.getPath()
						.startsWith("//"))) {
			URI resolved = normalizeFileURI(reference).resolve(uri);
			try {
				return new URI("file://" + resolved.toString().substring(5));
			} catch (URISyntaxException e) {
				throw new IllegalStateException("shouldn't happen: "
						+ e.getMessage(), e);
			}

		} else {
			return reference.resolve(uri);
		}
	}

	/**
	 * Resolves the second URI argument against the first URI argument.
	 * 
	 * <p>
	 * This method overrides the {@link URI#resolve(URI)} method only for
	 * <code>file</code> URIs representing UNC paths (i.e. of the form
	 * <code>file:////server/path</code> or <code>file://server/path</code>).
	 * </p>
	 * 
	 * @param the
	 *            reference URI against which the second argument will be
	 *            resolved
	 * @param str
	 *            the URI to resolve
	 * @return the resulting URI
	 * @see #resolve(URI, URI)
	 */
	static URI resolve(URI ref, String str) {
		if (Scheme.FILE.isSchemeFor(ref.getScheme())
				&& (ref.getHost() != null || ref.getPath().startsWith("//"))) {
			URI resolved = normalizeFileURI(ref).resolve(str);
			try {
				return new URI("file://" + resolved.toString().substring(5));
			} catch (URISyntaxException e) {
				throw new IllegalStateException("shouldn't happen: "
						+ e.getMessage(), e);
			}

		} else {
			return ref.resolve(str);
		}
	}
}
