package org.daisy.pipeline.braille.pef.impl;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Optional;

import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.factory.AbstractFactory;
import org.daisy.dotify.api.table.BrailleConverter;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableProvider;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "org.daisy.pipeline.braille.pef.impl.NabccEightDotTableProvider",
	service = { TableProvider.class }
)
public class NabccEightDotTableProvider implements TableProvider {
	
	public final static String IDENTIFIER = "org.daisy.pipeline.braille.pef.impl.NabccEightDotTable";
	private final Table table = new NabccEightDotTable();
	
	@SuppressWarnings("serial")
	private static class NabccEightDotTable extends AbstractFactory implements Table {
		
		private NabccEightDotTable() {
			super("NABCC 8-dot [louis:format]", "", IDENTIFIER);
		}
		
		public BrailleConverter newBrailleConverter() {
			return new NabccEightDotBrailleConverter();
		}
		
		public void setFeature(String key, Object value) {
			throw new IllegalArgumentException("Unknown feature: " + key);
		}
		
		public Object getFeature(String key) {
			throw new IllegalArgumentException("Unknown feature: " + key);
		}
		
		public Object getProperty(String key) {
			return null;
		}
	}
	
	public Collection<FactoryProperties> list() {
		return Optional.of((FactoryProperties)table).asSet();
	}
	
	public Table newFactory(String identifier) {
		return IDENTIFIER.equals(identifier) ? table : null;
	}
	
	private static class NabccEightDotBrailleConverter implements BrailleConverter {
		
		private static final Map<Character,Character> b2t = new HashMap<Character,Character>();
		private static final Map<Character,Character> t2b = new HashMap<Character,Character>();
		
		static {
			char[] tableDef = {(char)32,  (char)97,  (char)49,  (char)98,  (char)39,  (char)107, (char)50,  (char)108,
			                   (char)96,  (char)99,  (char)105, (char)102, (char)47,  (char)109, (char)115, (char)112,
			                   (char)34,  (char)101, (char)51,  (char)104, (char)57,  (char)111, (char)54,  (char)114,
			                   (char)126, (char)100, (char)106, (char)103, (char)62,  (char)110, (char)116, (char)113,
			                   (char)44,  (char)42,  (char)53,  (char)60,  (char)45,  (char)117, (char)56,  (char)118,
			                   (char)46,  (char)37,  (char)123, (char)36,  (char)43,  (char)120, (char)33,  (char)38,
			                   (char)59,  (char)58,  (char)52,  (char)124, (char)48,  (char)122, (char)55,  (char)40,
			                   (char)95,  (char)63,  (char)119, (char)125, (char)35,  (char)121, (char)41,  (char)61,
			                   (char)186, (char)65,  (char)185, (char)66,  (char)180, (char)75,  (char)178, (char)76,
			                   (char)64,  (char)67,  (char)73,  (char)70,  (char)247, (char)77,  (char)83,  (char)80,
			                   (char)168, (char)69,  (char)179, (char)72,  (char)167, (char)79,  (char)182, (char)82,
			                   (char)94,  (char)68,  (char)74,  (char)71,  (char)187, (char)78,  (char)84,  (char)81,
			                   (char)184, (char)215, (char)175, (char)171, (char)173, (char)85,  (char)174, (char)86,
			                   (char)183, (char)164, (char)91,  (char)162, (char)177, (char)88,  (char)161, (char)165,
			                   (char)181, (char)166, (char)172, (char)92,  (char)176, (char)90,  (char)169, (char)188,
			                   (char)127, (char)191, (char)87,  (char)93,  (char)163, (char)89,  (char)190, (char)189,
			                   (char)170, (char)129, (char)226, (char)130, (char)230, (char)139, (char)234, (char)140,
			                   (char)128, (char)131, (char)137, (char)134, (char)248, (char)141, (char)147, (char)144,
			                   (char)227, (char)133, (char)238, (char)136, (char)242, (char)143, (char)224, (char)146,
			                   (char)158, (char)132, (char)138, (char)135, (char)229, (char)142, (char)148, (char)145,
			                   (char)240, (char)225, (char)251, (char)233, (char)254, (char)149, (char)236, (char)150,
			                   (char)241, (char)237, (char)155, (char)253, (char)231, (char)152, (char)246, (char)228,
			                   (char)245, (char)250, (char)244, (char)156, (char)249, (char)154, (char)232, (char)239,
			                   (char)159, (char)243, (char)151, (char)157, (char)255, (char)153, (char)252, (char)235,
			                   (char)160, (char)1,   (char)194, (char)2,   (char)198, (char)11,  (char)202, (char)12,
			                   (char)0,   (char)3,   (char)9,   (char)6,   (char)216, (char)13,  (char)19,  (char)16,
			                   (char)195, (char)5,   (char)206, (char)8,   (char)210, (char)15,  (char)192, (char)18,
			                   (char)30,  (char)4,   (char)10,  (char)7,   (char)197, (char)14,  (char)20,  (char)17,
			                   (char)208, (char)193, (char)219, (char)201, (char)222, (char)21,  (char)204, (char)22,
			                   (char)209, (char)205, (char)27,  (char)221, (char)199, (char)24,  (char)214, (char)196,
			                   (char)213, (char)218, (char)212, (char)28,  (char)217, (char)26,  (char)200, (char)207,
			                   (char)31,  (char)211, (char)23,  (char)29,  (char)223, (char)25,  (char)220, (char)203 };
			int i = 0;
			char b;
			for (char t : tableDef) {
				b = (char)(0x2800+i);
				t2b.put(t, b);
				b2t.put(b, t);
				i++;
			}
		}
		
		public String toBraille(String text) {
			StringBuffer buf = new StringBuffer();
			Character b;
			for (char t : text.toCharArray()) {
				b = t2b.get(t);
				if (b == null)
					throw new IllegalArgumentException("Character '" + t + "' (0x" + Integer.toHexString((int)(t)) + ") not found.");
				buf.append(b); }
			return buf.toString();
		}
		
		public String toText(String braille) {
			StringBuffer buf = new StringBuffer();
			Character t;
			for (char b : braille.toCharArray()) {
				t = b2t.get(b);
				if (t == null)
					throw new IllegalArgumentException("Braille pattern '" + b + "' (0x" + Integer.toHexString((int)(b)) + ") not found.");
				buf.append(t); }
			return buf.toString();
		}
		
		private static final Charset charset = Charset.forName("ISO-8859-1");
		
		public Charset getPreferredCharset() {
			return charset;
		}
		
		public boolean supportsEightDot() {
			return true;
		}
	}
}
