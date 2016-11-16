package org.daisy.pipeline.maven.plugin;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.google.common.base.Optional;

abstract class utils {
	
	// TODO: use org.daisy.pipeline.braille.common.util.URIs.asURI
	static URI asURI(Object o) {
		if (o == null)
			return null;
		try {
			if (o instanceof String)
				return new URI((String)o);
			if (o instanceof File)
				return ((File)o).toURI();
			if (o instanceof URL) {
				URL url = (URL)o;
				if (url.getProtocol().equals("jar"))
					return new URI("jar:" + new URI(null, url.getAuthority(), url.getPath(), url.getQuery(), url.getRef()).toASCIIString());
				String authority = (url.getPort() != -1) ?
					url.getHost() + ":" + url.getPort() :
					url.getHost();
				return new URI(url.getProtocol(), authority, url.getPath(), url.getQuery(), url.getRef()); }
			if (o instanceof URI)
				return (URI)o; }
		catch (Exception e) {}
		throw new RuntimeException("Object can not be converted to URI: " + o);
	}
	
	static URI relativize(URI base, URI child) {
		try {
			if (base.isOpaque() || child.isOpaque()
			    || !Optional.fromNullable(base.getScheme()).or("").equalsIgnoreCase(Optional.fromNullable(child.getScheme()).or(""))
			    || !Optional.fromNullable(base.getAuthority()).equals(Optional.fromNullable(child.getAuthority())))
				return child;
			else {
				String bp = base.normalize().getPath();
				String cp = child.normalize().getPath();
				String relativizedPath;
				if (cp.startsWith("/")) {
					String[] bpSegments = bp.split("/", -1);
					String[] cpSegments = cp.split("/", -1);
					int i = bpSegments.length - 1;
					int j = 0;
					while (i > 0) {
						if (bpSegments[j].equals(cpSegments[j])) {
							i--;
							j++; }
						else
							break; }
					relativizedPath = "";
					while (i > 0) {
						relativizedPath += "../";
						i--; }
					while (j < cpSegments.length) {
						relativizedPath += cpSegments[j] + "/";
						j++; }
					relativizedPath = relativizedPath.substring(0, relativizedPath.length() - 1); }
				else
					relativizedPath = cp;
				if (relativizedPath.isEmpty())
					relativizedPath = "./";
				return new URI(null, null, relativizedPath, child.getQuery(), child.getFragment()); }}
		catch (URISyntaxException e) {
			throw new RuntimeException(e); }
	}
}
