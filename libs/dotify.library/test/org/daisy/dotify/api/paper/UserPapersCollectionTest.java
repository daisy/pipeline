package org.daisy.dotify.api.paper;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * TODO: write java doc.
 */
public class UserPapersCollectionTest {

    @Test
    public void customSheetPaper() throws IOException {
        UserPapersCollection c = UserPapersCollection.getInstance();
        Paper px1 = c.addNewSheetPaper(
                "Custom paper 1",
                "My sheet paper",
                Length.newCentimeterValue(10),
                Length.newCentimeterValue(20)
        );
        boolean t1 = PaperCatalog.newInstance().get(px1.getIdentifier()) != null;
        c.remove(px1);
        //assert that paper is in collection
        assertTrue(t1);
        //assert that paper is not in collection
        assertTrue(PaperCatalog.newInstance().get(px1.getIdentifier()) == null);
    }

    @Test
    public void customTractorPaper() throws IOException {
        UserPapersCollection c = UserPapersCollection.getInstance();
        Paper px1 = c.addNewTractorPaper(
                "Custom paper 2",
                "My tractor",
                Length.newCentimeterValue(15),
                Length.newCentimeterValue(25)
        );
        boolean t1 = PaperCatalog.newInstance().get(px1.getIdentifier()) != null;
        c.remove(px1);
        //assert that paper is in collection
        assertTrue(t1);
        //assert that paper is not in collection
        assertTrue(PaperCatalog.newInstance().get(px1.getIdentifier()) == null);
    }

    @Test
    public void customRollPaper() throws IOException {
        UserPapersCollection c = UserPapersCollection.getInstance();
        Paper px1 = c.addNewRollPaper("Custom paper 3", "My roll paper", Length.newCentimeterValue(22));
        boolean t1 = PaperCatalog.newInstance().get(px1.getIdentifier()) != null;
        c.remove(px1);
        //assert that paper is in collection
        assertTrue(t1);
        //assert that paper is not in collection
        assertTrue(PaperCatalog.newInstance().get(px1.getIdentifier()) == null);
    }
}
