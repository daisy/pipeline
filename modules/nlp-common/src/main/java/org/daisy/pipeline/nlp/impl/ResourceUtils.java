package org.daisy.pipeline.nlp.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import com.google.common.base.Charsets;

public class ResourceUtils {
	public static Collection<String> readLines(String directory, String filename)
	        throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceUtils.class
		        .getResourceAsStream("/" + directory + "/" + filename), Charsets.UTF_8));

		ArrayList<String> res = new ArrayList<String>();
		String line = reader.readLine();
		while (line != null) {
			String str = line.trim();
			if (str.length() > 0)
				res.add(str);
			line = reader.readLine();
		}

		return res;
	}
}
