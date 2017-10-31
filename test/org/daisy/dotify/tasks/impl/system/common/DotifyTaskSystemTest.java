package org.daisy.dotify.tasks.impl.system.common;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.daisy.streamline.api.tasks.TaskGroup;
import org.daisy.streamline.api.tasks.TaskGroupActivity;
import org.daisy.streamline.api.tasks.TaskGroupFactoryMaker;
import org.daisy.streamline.api.tasks.TaskGroupInformation;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class DotifyTaskSystemTest {

	@Test
	public void testPath_01() {
		TaskGroupInformation spec = TaskGroupInformation.newConvertBuilder("dtbook", "pef").build();
		TaskGroupFactoryMaker tgf = TaskGroupFactoryMaker.newInstance();
		List<TaskGroupInformation> specs = DotifyTaskSystem.getPath(tgf, spec, "sv-SE");
		assertEquals(2, specs.size());
		List<TaskGroup> tasks = new ArrayList<>();
		for (TaskGroupInformation s : specs) {
			if (s.getActivity()==TaskGroupActivity.CONVERT) {
				TaskGroup g = tgf.newTaskGroup(s.toSpecificationBuilder("sv-SE").build());
				tasks.add(g);
				System.out.println(g.getName());
			}
		}
		assertEquals(2, tasks.size());
		assertEquals("XMLInputManager", tasks.get(0).getName());
		assertEquals("Layout Engine", tasks.get(1).getName());
	}
	
	@Test
	public void testPath_02() {
		TaskGroupInformation spec = TaskGroupInformation.newConvertBuilder("epub", "pef").build();
		TaskGroupFactoryMaker tgf = TaskGroupFactoryMaker.newInstance();
		List<TaskGroupInformation> specs = DotifyTaskSystem.getPath(tgf, spec, "sv-SE");
		assertEquals(3, specs.size());
		List<TaskGroup> tasks = new ArrayList<>();
		for (TaskGroupInformation s : specs) {
			if (s.getActivity()==TaskGroupActivity.CONVERT) {
				TaskGroup g = tgf.newTaskGroup(s.toSpecificationBuilder("sv-SE").build());
				tasks.add(g);
				System.out.println(g.getName());
			}
		}
		assertEquals(3, tasks.size());
		assertEquals("org.daisy.dotify.tasks.impl.input.epub.Epub3InputManager", tasks.get(0).getName());
		assertEquals("XMLInputManager", tasks.get(1).getName());
		assertEquals("Layout Engine", tasks.get(2).getName());
	}
	
	static Map<String, List<TaskGroupInformation>> inputs = new HashMap<>();
	static Map<String, List<TaskGroupInformation>> inputsE = new HashMap<>();
	static String loc = "sv-SE";
	static {
		inputs.put("A", buildSpecs(loc, "A", false, "B", "C"));
		inputs.put("B", buildSpecs(loc, "B", false, "D"));
		inputs.put("C", buildSpecs(loc, "C", false, "D", "E"));
		inputs.put("D", buildSpecs(loc, "D", false, "E", "G"));
		inputs.put("E", buildSpecs(loc, "E", false, "F"));
		inputsE.put("A", buildSpecs(loc, "A", true, "B", "C"));
		inputsE.put("B", buildSpecs(loc, "B", true, "D"));
		inputsE.put("C", buildSpecs(loc, "C", true, "D", "E"));
		inputsE.put("D", buildSpecs(loc, "D", true, "E", "G"));
		inputsE.put("E", buildSpecs(loc, "E", true, "F"));
		List<TaskGroupInformation> sp = new ArrayList<>();
		sp.add(TaskGroupInformation.newEnhanceBuilder("G").build());
		inputsE.put("G", sp);
	}
	
	@Test
	public void testPath_03() {
		List<TaskGroupInformation> ret = DotifyTaskSystem.getPathSpecifications("A", "E", inputs);
		assertEquals(2, ret.size());
		assertEquals("A -> C (sv-SE)", asString(ret.get(0)));
		assertEquals("C -> E (sv-SE)", asString(ret.get(1)));
	}
	
	@Test
	public void testPath_04() {
		List<TaskGroupInformation> ret = DotifyTaskSystem.getPathSpecifications("A", "F", inputs);
		assertEquals(3, ret.size());
		assertEquals("A -> C (sv-SE)", asString(ret.get(0)));
		assertEquals("C -> E (sv-SE)", asString(ret.get(1)));
		assertEquals("E -> F (sv-SE)", asString(ret.get(2)));
	}
	
	@Test
	public void testPath_05() {
		List<TaskGroupInformation> ret = DotifyTaskSystem.getPathSpecifications("A", "G", inputs);
		assertEquals(3, ret.size());
		assertEquals("A -> B (sv-SE)", asString(ret.get(0)));
		assertEquals("B -> D (sv-SE)", asString(ret.get(1)));
		assertEquals("D -> G (sv-SE)", asString(ret.get(2)));
	}
	
	@Test
	public void testPathEnhance_01() {
		List<TaskGroupInformation> ret = DotifyTaskSystem.getPathSpecifications("A", "G", inputsE);
		assertEquals(7, ret.size());
		assertEquals("A -> A (sv-SE)", asString(ret.get(0)));
		assertEquals("A -> B (sv-SE)", asString(ret.get(1)));
		assertEquals("B -> B (sv-SE)", asString(ret.get(2)));
		assertEquals("B -> D (sv-SE)", asString(ret.get(3)));
		assertEquals("D -> D (sv-SE)", asString(ret.get(4)));
		assertEquals("D -> G (sv-SE)", asString(ret.get(5)));
		assertEquals("G -> G (sv-SE)", asString(ret.get(6)));
	}
	
	private static List<TaskGroupInformation> buildSpecs(String locale, String input, boolean withEnhance, String ... outputs) {
		List<TaskGroupInformation> specs = new ArrayList<>();
		for (String r : outputs) {
			specs.add(TaskGroupInformation.newConvertBuilder(input, r).build());
		}
		if (withEnhance) {
			specs.add(TaskGroupInformation.newEnhanceBuilder(input).build());
		}
		return specs;
	}
	
	private static String asString(TaskGroupInformation spec) {
		return spec.getInputFormat() + " -> " + spec.getOutputFormat() + " (sv-SE)";
	}

}
