package org.daisy.dotify.impl.system.common;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;
import org.daisy.dotify.api.tasks.TaskGroupSpecification.Type;
import org.daisy.dotify.consumer.tasks.TaskGroupFactoryMaker;
import org.junit.Test;
public class DotifyTaskSystemTest {

	public DotifyTaskSystemTest() {
		// TODO Auto-generated constructor stub
	}
	
	@Test
	public void notAnActualTest() {
		Set<TaskGroupSpecification> specs = TaskGroupFactoryMaker.newInstance().listSupportedSpecifications();
		Map<String, List<TaskGroupSpecification>> byInput = DotifyTaskSystem.byInput(specs);
		//DotifyTaskSystem.getPathSpecifications("dtbook", "pef", "sv-SE", new HashMap<String, Object>(), specs);
		DotifyTaskSystem.listSpecs(System.out, byInput);
	}
	
	@Test
	public void testPath_01() {
		TaskGroupSpecification spec = new TaskGroupSpecification("dtbook", "pef", "sv-SE");
		TaskGroupFactoryMaker tgf = TaskGroupFactoryMaker.newInstance();
		List<TaskGroupSpecification> specs = DotifyTaskSystem.getPath(tgf, spec, new HashMap<String, Object>());
		assertEquals(3, specs.size());
		List<TaskGroup> tasks = new ArrayList<>();
		for (TaskGroupSpecification s : specs) {
			if (s.getType()==Type.CONVERT) {
				TaskGroup g = tgf.newTaskGroup(s);
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
		TaskGroupSpecification spec = new TaskGroupSpecification("epub", "pef", "sv-SE");
		TaskGroupFactoryMaker tgf = TaskGroupFactoryMaker.newInstance();
		List<TaskGroupSpecification> specs = DotifyTaskSystem.getPath(tgf, spec, new HashMap<String, Object>());
		assertEquals(4, specs.size());
		List<TaskGroup> tasks = new ArrayList<>();
		for (TaskGroupSpecification s : specs) {
			if (s.getType()==Type.CONVERT) {
				TaskGroup g = tgf.newTaskGroup(s);
				tasks.add(g);
				System.out.println(g.getName());
			}
		}
		assertEquals(3, tasks.size());
		assertEquals("org.daisy.dotify.impl.input.epub.Epub3InputManager", tasks.get(0).getName());
		assertEquals("XMLInputManager", tasks.get(1).getName());
		assertEquals("Layout Engine", tasks.get(2).getName());
	}
	
	static Map<String, List<TaskGroupSpecification>> inputs = new HashMap<>();	
	static String loc = "sv-SE";
	static {
		inputs.put("A", buildSpecs(loc, "A", "B", "C"));
		inputs.put("B", buildSpecs(loc, "B", "D"));
		inputs.put("C", buildSpecs(loc, "C", "D", "E"));
		inputs.put("D", buildSpecs(loc, "D", "E", "G"));
		inputs.put("E", buildSpecs(loc, "E", "F"));
	}
	
	@Test
	public void testPath_03() {
		List<TaskGroupSpecification> ret = DotifyTaskSystem.getPathSpecifications("A", "E", loc, inputs);
		assertEquals(2, ret.size());
		assertEquals("A -> C (sv-SE)", asString(ret.get(0)));
		assertEquals("C -> E (sv-SE)", asString(ret.get(1)));
	}
	
	@Test
	public void testPath_04() {
		List<TaskGroupSpecification> ret = DotifyTaskSystem.getPathSpecifications("A", "F", loc, inputs);
		assertEquals(3, ret.size());
		assertEquals("A -> C (sv-SE)", asString(ret.get(0)));
		assertEquals("C -> E (sv-SE)", asString(ret.get(1)));
		assertEquals("E -> F (sv-SE)", asString(ret.get(2)));
	}
	
	@Test
	public void testPath_05() {
		List<TaskGroupSpecification> ret = DotifyTaskSystem.getPathSpecifications("A", "G", loc, inputs);
		assertEquals(3, ret.size());
		assertEquals("A -> B (sv-SE)", asString(ret.get(0)));
		assertEquals("B -> D (sv-SE)", asString(ret.get(1)));
		assertEquals("D -> G (sv-SE)", asString(ret.get(2)));
	}
	
	private static List<TaskGroupSpecification> buildSpecs(String locale, String input, String ... outputs) {
		List<TaskGroupSpecification> specs = new ArrayList<>();
		for (String r : outputs) {
			specs.add(new TaskGroupSpecification(input, r, locale));
		}
		return specs;
	}
	
	private static String asString(TaskGroupSpecification spec) {
		return spec.getInputFormat() + " -> " + spec.getOutputFormat() + " (" + spec.getLocale() + ")";
	}

}
