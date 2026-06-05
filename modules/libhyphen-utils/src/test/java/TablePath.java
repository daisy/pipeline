import java.util.Map;

import org.daisy.pipeline.braille.libhyphen.LibhyphenTablePath;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "table-path",
	service = { LibhyphenTablePath.class },
	property = {
		"identifier:String=http://test/tables/",
		"path:String=/tables"
	}
)
public class TablePath extends LibhyphenTablePath {
	
	@Activate
	public void activate(Map<?,?> properties) {
		super.activate(properties, TablePath.class);
	}
}
