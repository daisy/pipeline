import org.daisy.braille.api.table.BrailleConverter;
import org.daisy.pipeline.braille.pef.impl.NabccEightDotTableProvider;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class NabccEightDotTableProviderTest {
	
	@Test
	public void testNabccEightDotBrailleConverter() {
		BrailleConverter converter = new NabccEightDotTableProvider().newFactory(NabccEightDotTableProvider.IDENTIFIER).newBrailleConverter();
		assertEquals("⠋⠕⠕⠃⠁⠗", converter.toBraille("foobar"));
		assertEquals("foobar", converter.toText("⠋⠕⠕⠃⠁⠗"));
	}
}
