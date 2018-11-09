package org.daisy.dotify.formatter.impl.obfl;

import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.formatter.DynamicRenderer;
import org.daisy.dotify.api.formatter.RenderingScenario;

public class XMLDataRenderer implements DynamicRenderer {
	private final List<RenderingScenario> data;

	public XMLDataRenderer(List<RenderingScenario> data) {
		this.data = new ArrayList<>(data);
	}

	@Override
	public Iterable<RenderingScenario> getScenarios() {
		return data;
	}

}
