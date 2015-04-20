package org.daisy.dotify.impl.translator.sv_SE;

import static org.junit.Assert.assertEquals;

import org.daisy.dotify.impl.translator.sv_SE.CapitalizationMarkers;
import org.junit.Test;

public class CapitalizationMarkersTest {
	private final CapitalizationMarkers f;

	public CapitalizationMarkersTest() {
		f = new CapitalizationMarkers();
	}
	
	@Test
	public void testCaps001() {
		assertEquals("", "⠠Det första exemplet är ⠠⠠SAV, bara ett ord.", f.filter("Det första exemplet är SAV, bara ett ord."));
	}
	@Test
	public void testCaps002() {
		assertEquals("", "⠠Det andra exemplet är ⠠⠠⠠SAV VAS⠱, en ordgrupp.", f.filter("Det andra exemplet är SAV VAS, en ordgrupp."));
	}
	@Test
	public void testCaps003() {
		assertEquals("", "⠠⠠⠠LO/TCO / SACO⠱:s ⠠Brysselkontor", f.filter("LO/TCO / SACO:s Brysselkontor"));
	}
	@Test
	public void testCaps004() {
		assertEquals("", "⠠⠠⠠LO-TCO-SACO⠱:s ⠠Brysselkontor", f.filter("LO-TCO-SACO:s Brysselkontor"));
	}
	@Test
	public void testCaps005() {
		assertEquals("", "⠠⠠⠠LO/TCO⠱/⠠⠠SACO⠼5", f.filter("LO/TCO/SACO⠼5"));
	}
	@Test
	public void testCaps006() {
		assertEquals("", "aaa/⠠⠠⠠LO/TCO⠱/sas", f.filter("aaa/LO/TCO/sas"));
	}
	@Test
	public void testCaps007() {
		assertEquals("", "ddd⠠L⠠O/⠠⠠TCO⠼5", f.filter("dddLO/TCO⠼5"));
	}
	@Test
	public void testCaps008() {
		assertEquals("", "ddd⠠L⠠O ⠠⠠TCO⠼5", f.filter("dddLO TCO⠼5"));
	}
	@Test
	public void testCaps009() {
		assertEquals("", "⠠⠠LO/⠠⠠TCO⠼5⠱ddc", f.filter("LO/TCO⠼5⠱ddc"));
	}
	@Test
	public void testCaps010() {
		assertEquals("", "⠠⠠⠠LO-TCO⠱-", f.filter("LO-TCO-"));
	}
	@Test
	public void testCaps011() {
		assertEquals("", "⠠⠠⠠LO-/TCO⠱-kongressen", f.filter("LO-/TCO-kongressen"));
	}
	@Test
	public void testCaps012() {
		assertEquals("", "⠠⠠TCO⠱-", f.filter("TCO-"));
	}
	@Test
	public void testCaps013() {
		assertEquals("", "⠠⠠IKEA⠱s katalog", f.filter("IKEAs katalog"));		
	}
	@Test
	public void testCaps014() {
		assertEquals("", "⠠⠠IKEA⠼4⠱gf katalog", f.filter("IKEA⠼4⠱gf katalog"));
	}
	@Test
	public void testCaps015() {
		assertEquals("", "⠠Svenska ⠠⠠ISBN⠱-centralen",  f.filter("Svenska ISBN-centralen"));
	}
	@Test
	public void testCaps016() {
		assertEquals("", "⠠⠠⠠SYNSKADADES RIKSFÖRBUND⠱", f.filter("SYNSKADADES RIKSFÖRBUND"));
	}
	@Test
	public void testCaps017() {
		assertEquals("", "k⠠Wh, ⠠Jäm⠠O", f.filter("kWh, JämO"));
	}
	@Test
	public void testCaps018() {
		assertEquals("", "⠠Lösenord: o⠠Vb⠠E⠠Gj", f.filter("Lösenord: oVbEGj"));		
	}
	@Test
	public void testCaps019() {
		assertEquals("", "⠠Lösenord: o⠠Vb⠼4⠠E⠠G⠠F", f.filter("Lösenord: oVb⠼4EGF"));		
	}
	@Test
	public void testCaps020() {
		assertEquals("", "⠠Det är sant: ⠠⠠⠠DETTA ÄR ETT TEST⠱. ⠠Inget annat.", f.filter("Det är sant: DETTA ÄR ETT TEST. Inget annat."));
	}
	@Test
	public void testCaps021() {
		assertEquals("", "⠠Flera ⠠R ⠠O ⠠L ⠠I ⠠G ⠠A exempel.", f.filter("Flera R O L I G A exempel."));
	}
	@Test
	public void testCaps022() {
		assertEquals("", 
				"⠠Test av ⠠⠠STORA ⠠Bokstäver med flera ⠠⠠⠠ORD I RAD⠱, samt andra varianter, t.ex. lösenord som o⠠Ro⠠X⠠V⠠Q⠼5q och ⠠S ⠠P ⠠Ä ⠠R ⠠R ⠠A ⠠D text.",
				f.filter("Test av STORA Bokstäver med flera ORD I RAD, samt andra varianter, t.ex. lösenord som oRoXVQ⠼5q och S P Ä R R A D text."));
	}
	@Test
	public void testCaps023() {
		assertEquals("", "⠠Flera ⠠⠠⠠R-O-L-I-G-A⠱ exempel.", f.filter("Flera R-O-L-I-G-A exempel."));
	}
	@Test
	public void testCaps024() {
		assertEquals("", "⠠⠠SIFFROR ⠠I ⠼10 ⠠⠠GRUPPER.", f.filter("SIFFROR I ⠼10 GRUPPER."));
	}
	@Test
	public void testCaps025() {
		assertEquals("", "⠠⠠⠠SIFFROR I FLER ÄN⠱ ⠼10 ⠠⠠GRUPPER.", f.filter("SIFFROR I FLER ÄN ⠼10 GRUPPER."));
	}
	@Test
	public void testCaps026() {
		assertEquals("", "⠠Svenska ⠠⠠ISBN⠱-⠠Centralen", f.filter("Svenska ISBN-Centralen"));
	}
	@Test
	public void testCaps027() {
		assertEquals("", "⠠⠠SAV:::::⠠⠠VAS", f.filter("SAV:::::VAS"));
	}
	@Test
	public void testCaps028() {
		assertEquals("", "⠠⠠⠠SAV/VAS⠱", f.filter("SAV/VAS"));
	}
	@Test
	public void testCaps029() {
		assertEquals("", "⠠Um⠠U⠠B", f.filter("UmUB"));
	}
	@Test
	public void testCaps030() {
		assertEquals("", "(⠠⠠ISBN)", f.filter("(ISBN)"));
	}
	@Test
	public void testCaps031() {
		assertEquals("", "⠠T⠠Vå inledande versaler", f.filter("TVå inledande versaler"));
	}
	@Test
	public void testCaps032() {
		assertEquals("", "(vanligen förkortad ⠠⠠SFSV) –", f.filter("(vanligen förkortad SFSV) \u2013"));
	}
	@Test
	public void testCaps033() {
		assertEquals("", "⠠T.⠠⠠EX.", f.filter("T.EX."));
	}
	@Test
	public void testCaps034() {
		assertEquals("", "⠠⠠PC, men ⠠P⠠C-apparat.", f.filter("PC, men PC-apparat."));
	}
	@Test
	public void testCaps035() {
		assertEquals("", "⠠B. ⠠⠠⠠KORTA CITAT⠱", f.filter("B. KORTA CITAT"));
	}
	@Test
	public void testCaps036() {
		assertEquals("", "⠠⠠IV", f.filter("IV"));
	}
	/*
	@Test
	public void testCaps037() {
		assertEquals("", "", f.filter("STRINDBERG, AUGUST"));
	}*/
	@Test
	public void testCaps038() {
		assertEquals("", "⠠⠠GPS⠱-klocka", f.filter("GPS-klocka"));
	}
	/*
	@Test
	public void testCaps039() {
		assertEquals("", "", f.filter("CAN'T PLAY BINGO WITH NO LIGHTS!"));
	}*/
}
