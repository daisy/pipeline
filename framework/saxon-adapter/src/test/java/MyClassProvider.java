import java.util.HashMap;
import java.util.Map;

import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "MyClassProvider",
	service = { ExtensionFunctionProvider.class }
)
public class MyClassProvider extends ReflexiveExtensionFunctionProvider {

	public MyClassProvider() {
		super(MyClass.class);
	}

	public class MyClass {

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
}
