package org.daisy.braille.utils.pef;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

class BenchmarkPEFBook {

	public static void main(String[] args) throws IOException {
		File f = File.createTempFile("test", ".tmp");
		f.deleteOnExit();
		int a1=0, a2=0;
		final int x = 100;
		try {
			for (int i=0; i<x; i++) {
				Map<String, String> defaults = new HashMap<>();
				defaults.put(PEFGenerator.KEY_VOLUMES, ""+(3+(int)(Math.random()*10)));
				defaults.put(PEFGenerator.KEY_SPV, ""+(1+(int)(Math.random()*5)));
				defaults.put(PEFGenerator.KEY_PPV, ""+(20+(int)(Math.random()*20)));
				defaults.put(PEFGenerator.KEY_EIGHT_DOT, ((int)(Math.random()*2)>=1?"true":"false"));
				defaults.put(PEFGenerator.KEY_ROWS, ""+(20+(int)(Math.random()*10)));
				defaults.put(PEFGenerator.KEY_COLS, ""+(25+(int)(Math.random()*10)));
				defaults.put(PEFGenerator.KEY_DUPLEX, ((int)(Math.random()*2)>=1?"true":"false"));
				System.out.print(defaults + " ");

				PEFGenerator pg = new PEFGenerator(defaults);
				pg.generateTestBook(f);
				try {
					long t1 = System.currentTimeMillis();
					PEFBook p1 = XPathPEFBook.load(f.toURI());
					long t2 = System.currentTimeMillis();
					PEFBook p2 = StaxPEFBook.loadStax(f.toURI(), true);
					long t3 = System.currentTimeMillis();
					a1+=(t2-t1);
					a2+=(t3-t2);
					System.out.println("" + (t2-t1) + " " + (t3-t2));
					if (!p1.equals(p2)) {
						System.err.println("Not equal: " + f);
						System.exit(0);
					}
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				f.delete();
			}
			System.out.println("Average Xpath: " + a1/x+ ", Stax: " +a2/x);
		} finally {
			f.delete();
		}
	}

}
