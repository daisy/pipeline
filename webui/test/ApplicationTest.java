import org.junit.*;

import static org.junit.Assert.*;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest {

    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertEquals(2, a);
    }

    @Test
    public void renderTemplate() {
//        Content html = views.html.index.render("Your new application is ready.");
//        assertEquals("text/html", contentType(html));
//        assertTrue(contentAsString(html).contains("Your new application is ready."));
    }


}
