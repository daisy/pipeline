package org.daisy.dotify.graphics;

import static org.junit.Assert.assertEquals;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.List;

import org.junit.Test;

public class BrailleGraphicsTest {
	final BufferedImage lineImage;
	final BufferedImage borderImage;
	final BrailleGraphics sixDotGraphics;
	final BrailleGraphics eightDotGraphics;
	
	public BrailleGraphicsTest() {
		lineImage = new BufferedImage(20, 15, BufferedImage.TYPE_BYTE_BINARY);
		{
			Graphics2D g = (Graphics2D)lineImage.getGraphics();
			g.drawLine(0, 0, 20, 15);
		}
		borderImage = new BufferedImage(30, 12, BufferedImage.TYPE_BYTE_BINARY);
		{
			Graphics2D g = (Graphics2D)borderImage.getGraphics();
			int x1 = 1;
			int y1 = 2;
			int x2 = 28;
			int y2 = 9;
			Stroke s = new BasicStroke();
			g.setStroke(s);
			g.drawLine(x1, y1-1, x2+1, y1-1);
			s = new BasicStroke(2);
			g.setStroke(s);
			g.drawLine(x2+1, y1, x2+1, y2);
			s = new BasicStroke(1);
			g.setStroke(s);
			g.drawLine(x2+1, y2, x1, y2);
			g.drawLine(x1, y2, x1, y1-1);
		}
		sixDotGraphics = new BrailleGraphics(false);
		eightDotGraphics = new BrailleGraphics(true);
		//writeImages(new File("C:\\"));
	}
	/*
	private void writeImages(File p) {
	  	try {
	  		ImageIO.write(lineImage, "jpg", new File(p, "line.jpg"));
			ImageIO.write(borderImage, "jpg", new File(p, "border.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	@Test
	public void testGraphicsEightDot() {
		String[] exp = new String[]{"⠑⠤⡀⠀⠀⠀⠀⠀⠀⠀",
									"⠀⠀⠈⠒⢄⠀⠀⠀⠀⠀",
									"⠀⠀⠀⠀⠀⠉⠢⣀⠀⠀",
									"⠀⠀⠀⠀⠀⠀⠀⠀⠑⠤"};
		
		
		List<String> res = eightDotGraphics.renderGraphics(lineImage.getData());
		assertEquals(res.size(), exp.length);
		for (int i = 0; i< exp.length; i++) {
			assertEquals("Assert that render is correct.", exp[i], res.get(i));
		}
	}
	
	@Test
	public void testGraphicsSixDot() {
		String[] exp = new String[]{"⠑⠤⠀⠀⠀⠀⠀⠀⠀⠀",
									"⠀⠀⠑⠤⠀⠀⠀⠀⠀⠀",
									"⠀⠀⠀⠀⠑⠤⠀⠀⠀⠀",
									"⠀⠀⠀⠀⠀⠀⠑⠤⠀⠀",
									"⠀⠀⠀⠀⠀⠀⠀⠀⠑⠤"};
		
		
		List<String> res = sixDotGraphics.renderGraphics(lineImage.getData());
		assertEquals(res.size(), exp.length);
		for (int i = 0; i< exp.length; i++) {
			assertEquals("Assert that render is correct.", exp[i], res.get(i));
		}
	}
	
	@Test
	public void testGraphicsEightToSixDot() {
		String[] exp = new String[]{"⠑⠤⠀⠀⠀⠀⠀⠀⠀⠀",
									"⠀⠀⠈⠒⠄⠀⠀⠀⠀⠀",
									"⠀⠀⠀⠀⠀⠉⠢⠀⠀⠀",
									"⠀⠀⠀⠀⠀⠀⠀⠀⠑⠤"};
		
		
		List<String> res = BrailleGraphics.sixDotFilter(eightDotGraphics.renderGraphics(lineImage.getData()));
		assertEquals(res.size(), exp.length);
		for (int i = 0; i< exp.length; i++) {
			assertEquals("Assert that render is correct.", exp[i], res.get(i));
		}
	}

	@Test
	public void testDrawBorder() {
		String[] exp = new String[]{"⠰⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠶",
		"⠸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠿",
		"⠸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠿",
		"⠈⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉"};
		List<String> res = sixDotGraphics.renderGraphics(borderImage.getData());
		assertEquals(res.size(), exp.length);
		for (int i = 0; i< exp.length; i++) {
			assertEquals("Assert that render is correct.", exp[i], res.get(i));
		}
	}

}
