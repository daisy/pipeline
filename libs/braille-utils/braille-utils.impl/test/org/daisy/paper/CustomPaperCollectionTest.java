package org.daisy.paper;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.daisy.braille.api.paper.CustomPaperCollection;
import org.daisy.braille.api.paper.Length;
import org.daisy.braille.api.paper.Paper;
import org.daisy.braille.consumer.paper.PaperCatalog;
import org.junit.Test;
public class CustomPaperCollectionTest {
	
	@Test
	public void customSheetPaper() throws IOException {
		CustomPaperCollection c = CustomPaperCollection.getInstance();
		Paper px1 = c.addNewSheetPaper("Custom paper 1", "My sheet paper", Length.newCentimeterValue(10), Length.newCentimeterValue(20));
		boolean t1 =  PaperCatalog.newInstance().get(px1.getIdentifier())!=null;
		c.remove(px1);
		//assert that paper is in collection
		assertTrue(t1);
		//assert that paper is not in collection
		assertTrue(PaperCatalog.newInstance().get(px1.getIdentifier())==null);
	}
	
	@Test
	public void customTractorPaper() throws IOException {
		CustomPaperCollection c = CustomPaperCollection.getInstance();
		Paper px1 = c.addNewTractorPaper("Custom paper 2", "My tractor", Length.newCentimeterValue(15), Length.newCentimeterValue(25));
		boolean t1 =  PaperCatalog.newInstance().get(px1.getIdentifier())!=null;
		c.remove(px1);
		//assert that paper is in collection
		assertTrue(t1);
		//assert that paper is not in collection
		assertTrue(PaperCatalog.newInstance().get(px1.getIdentifier())==null);
	}
	
	@Test
	public void customRollPaper() throws IOException {
		CustomPaperCollection c = CustomPaperCollection.getInstance();
		Paper px1 = c.addNewRollPaper("Custom paper 3", "My roll paper", Length.newCentimeterValue(22));
		boolean t1 =  PaperCatalog.newInstance().get(px1.getIdentifier())!=null;
		c.remove(px1);
		//assert that paper is in collection
		assertTrue(t1);
		//assert that paper is not in collection
		assertTrue(PaperCatalog.newInstance().get(px1.getIdentifier())==null);
	}

	/*
	public static void main(String[] args) throws IOException {
		for (Paper p : PaperCatalog.newInstance().list()) {
			System.out.println(p.getDisplayName() + " " +p.getIdentifier() + " " + p);
		}
	}*/

}
