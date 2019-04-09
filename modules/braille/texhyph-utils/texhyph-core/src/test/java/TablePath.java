import java.util.Map;

import org.daisy.pipeline.braille.tex.TexHyphenatorTablePath;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "table-path",
	service = { TexHyphenatorTablePath.class },
	property = {
		"identifier:String=http://test/tables/",
		"path:String=/tables"
	}
)
public class TablePath extends TexHyphenatorTablePath {
	
	@Activate
	public void activate(Map<?,?> properties) {
		super.activate(properties, TablePath.class);
	}
}
