package org.daisy.dotify.api.factory;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.daisy.dotify.api.factory.FactoryProperties.ComparatorBuilder.SortOrder;
import org.daisy.dotify.api.factory.FactoryProperties.ComparatorBuilder.SortProperty;
import org.junit.Test;
import org.mockito.Mockito;

@SuppressWarnings("javadoc")
public class FactoryPropertiesTest {

	@Test
	public void testFactoryPropertes_01() {
		FactoryProperties a = Mockito.mock(FactoryProperties.class);
		Mockito.when(a.getIdentifier()).thenReturn("a");
		FactoryProperties b = Mockito.mock(FactoryProperties.class);
		Mockito.when(b.getIdentifier()).thenReturn("b");
		FactoryProperties c = Mockito.mock(FactoryProperties.class);
		Mockito.when(c.getIdentifier()).thenReturn("c");
		List<FactoryProperties> props = Arrays.asList(a, b, c);
		Comparator<FactoryProperties> comp = FactoryProperties.newComparatorBuilder()
				.sortBy(SortProperty.IDENTIFIER)
				.sortOrder(SortOrder.DOWN)
				.build();
		Collections.sort(props, comp);
		assertEquals("c", props.get(0).getIdentifier());
	}
	
	@Test
	public void testFactoryPropertes_02() {
		FactoryProperties a = Mockito.mock(FactoryProperties.class);
		Mockito.when(a.getIdentifier()).thenReturn("a");
		FactoryProperties b = Mockito.mock(FactoryProperties.class);
		Mockito.when(b.getIdentifier()).thenReturn("b");
		FactoryProperties c = Mockito.mock(FactoryProperties.class);
		Mockito.when(c.getIdentifier()).thenReturn("c");
		List<FactoryProperties> props = Arrays.asList(a, b, c);
		FactoryProperties.ComparatorBuilder builder = FactoryProperties.newComparatorBuilder();
		Comparator<FactoryProperties> comp = builder.sortBy(SortProperty.IDENTIFIER).sortOrder(SortOrder.UP).build();
		// This tests that changing the builder after the comparator has been built doesn't affect it.
		builder.sortOrder(SortOrder.DOWN);
		Collections.sort(props, comp);
		assertEquals("a", props.get(0).getIdentifier());
		// Sorting based on the current configuration of the builder. 
		Collections.sort(props, builder.build());
		assertEquals("c", props.get(0).getIdentifier());
	}
}
