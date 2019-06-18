package org.daisy.dotify.api.translator;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

@SuppressWarnings("javadoc")
public class TranslatableWithContextTest {

	@Test
	public void testNewContext() {
		List<ResolvableText> items = new ArrayList<>();
		ResolvableText a = Mockito.mock(ResolvableText.class);
		items.add(a);
		ResolvableText b = Mockito.mock(ResolvableText.class);
		items.add(b);
		ResolvableText c = Mockito.mock(ResolvableText.class);
		items.add(c);
		{
			TextWithContext context = TranslatableWithContext.newContext(items, 0);
			assertEquals(0, context.getPrecedingText().size());
			assertEquals(2, context.getFollowingText().size());
			assertEquals(1, context.getTextToTranslate().size());
		}
		{
			TextWithContext context = TranslatableWithContext.newContext(items, 1);
			assertEquals(1, context.getPrecedingText().size());
			assertEquals(1, context.getFollowingText().size());
			assertEquals(1, context.getTextToTranslate().size());
		}
		{
			TextWithContext context = TranslatableWithContext.newContext(items, 2);
			assertEquals(2, context.getPrecedingText().size());
			assertEquals(0, context.getFollowingText().size());
			assertEquals(1, context.getTextToTranslate().size());
		}
		{
			TextWithContext context = TranslatableWithContext.newContext(items, 0, 2);
			assertEquals(0, context.getPrecedingText().size());
			assertEquals(1, context.getFollowingText().size());
			assertEquals(2, context.getTextToTranslate().size());
		}
		{
			TextWithContext context = TranslatableWithContext.newContext(items, 1, 3);
			assertEquals(1, context.getPrecedingText().size());
			assertEquals(0, context.getFollowingText().size());
			assertEquals(2, context.getTextToTranslate().size());
		}
		{
			TextWithContext context = TranslatableWithContext.newContext(items, 1, 1);
			assertEquals(1, context.getPrecedingText().size());
			assertEquals(2, context.getFollowingText().size());
			assertEquals(0, context.getTextToTranslate().size());
		}
	}
}
