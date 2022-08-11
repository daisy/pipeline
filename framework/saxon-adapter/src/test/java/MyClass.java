import java.util.HashMap;
import java.util.Map;

import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;

import org.osgi.service.component.annotations.Component;

public class MyClass {

	@Component(
		name = "MyClass",
		service = { ExtensionFunctionProvider.class }
	)
	public static class Provider extends ReflexiveExtensionFunctionProvider {
		public Provider() {
			super(MyClass.class);
		}
	}

	private final String value;

	public MyClass(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public MyClass uppercase() {
		return new MyClass(value.toUpperCase());
	}

	public Map<String,MyClass> wrapInMap() {
		Map<String,MyClass> map = new HashMap<>();
		map.put("result", this);
		return map;
	}
}
