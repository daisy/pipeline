package org.daisy.dotify.hyphenator.impl;

import static org.junit.Assert.assertEquals;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import org.daisy.dotify.api.hyphenator.HyphenatorFactory;
import org.daisy.dotify.api.hyphenator.HyphenatorInterface;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class SwedishHyphenation2Test {
	private final HyphenatorInterface hyph_sv_SE;
	public SwedishHyphenation2Test() throws HyphenatorConfigurationException {
		HyphenatorInterface h2;
		try {
			String locale = "sv-SE";
			HyphenatorFactory hf = new LatexHyphenatorFactory(LatexHyphenatorCore.getInstance());
			//if this is set to 3 (using only patterns) several tests will fail 
			//hf.setFeature(HyphenationFeatures.HYPHENATION_ACCURACY, 3);
			h2 = hf.newHyphenator(locale);
		} catch (HyphenatorConfigurationException e) {
			h2 = null;
		}
		hyph_sv_SE = h2;
	}
	@Test
	public void testCompoundWord_Sv_027() throws HyphenatorConfigurationException {
		assertEquals("olycks­falls­för­säk­ring­ar­nas", hyph_sv_SE.hyphenate("olycksfallsförsäkringarnas"));
	}
	@Test
	public void testWord_Sv_028() throws HyphenatorConfigurationException {
		assertEquals("ox­hu­vud", hyph_sv_SE.hyphenate("oxhuvud"));
	}
	@Test
	public void testCompoundWord_Sv_029() throws HyphenatorConfigurationException {
		assertEquals("chat­pro­gram­met", hyph_sv_SE.hyphenate("chatprogrammet"));
	}
	@Test
	public void testWord_Sv_030() throws HyphenatorConfigurationException {
		assertEquals("pap­pers­ark", hyph_sv_SE.hyphenate("pappersark"));
	}
	@Test
	@Ignore
	public void testWord_Sv_031() throws HyphenatorConfigurationException {
		assertEquals("skol­orkes­terns", hyph_sv_SE.hyphenate("skolorkesterns"));
	}
	@Test
	public void testWord_Sv_032() throws HyphenatorConfigurationException {
		assertEquals("nit­ton­åring­ens", hyph_sv_SE.hyphenate("nittonåringens"));
	}
	@Test
	@Ignore
	public void testCompoundWord_Sv_033() throws HyphenatorConfigurationException {
		assertEquals("blom­odlingspurt­seger", hyph_sv_SE.hyphenate("blomodlingspurtseger"));
	}
	@Test
	public void testWord_Sv_034() throws HyphenatorConfigurationException {
		assertEquals("Un­der­sö­ka", hyph_sv_SE.hyphenate("Undersöka"));
	}
	@Test
	public void testWord_Sv_035() throws HyphenatorConfigurationException {
		assertEquals("kam­rat­skap", hyph_sv_SE.hyphenate("kamratskap"));
	}
	@Test
	public void testWord_Sv_036() throws HyphenatorConfigurationException {
		assertEquals("trap­pan", hyph_sv_SE.hyphenate("trappan"));
	}
	@Test
	@Ignore
	public void testCompoundWord_Sv_037() throws HyphenatorConfigurationException {
		//compound possibilities: sprita-uran, sprit-auran
		assertEquals("sprit­au­ran", hyph_sv_SE.hyphenate("spritauran"));
	}
	@Test
	@Ignore
	public void testCompoundWord_Sv_038() throws HyphenatorConfigurationException {
		assertEquals("Stu­dent­åren", hyph_sv_SE.hyphenate("Studentåren"));
	}
	@Test
	@Ignore
	public void testCompoundWord_Sv_039() throws HyphenatorConfigurationException {
		assertEquals("ba­ry­ton­smatt­ran­det", hyph_sv_SE.hyphenate("barytonsmattrandet"));
	}
	@Test
	public void testCompoundWord_Sv_040() throws HyphenatorConfigurationException {
		assertEquals("maj­stång", hyph_sv_SE.hyphenate("majstång"));
	}
	@Test
	public void testWord_Sv_041() throws HyphenatorConfigurationException {
		assertEquals("av­sli­ten", hyph_sv_SE.hyphenate("avsliten"));
	}
	@Test
	public void testWord_Sv_042() throws HyphenatorConfigurationException {
		assertEquals("ho­nom", hyph_sv_SE.hyphenate("honom"));
	}
	@Test
	public void testCompoundWord_Sv_043() throws HyphenatorConfigurationException {
		assertEquals("Upp­hovs­rätts­la­gen", hyph_sv_SE.hyphenate("Upphovsrättslagen"));
	}
	@Test
	public void testWord_Sv_044() throws HyphenatorConfigurationException {
		assertEquals("Trans­port", hyph_sv_SE.hyphenate("Transport"));
	}
	@Test
	public void testWord_Sv_045() throws HyphenatorConfigurationException {
		assertEquals("eds­brotts", hyph_sv_SE.hyphenate("edsbrotts"));
	}
	@Test
	public void testWord_Sv_046() throws HyphenatorConfigurationException {
		assertEquals("för­fal­lo­tid", hyph_sv_SE.hyphenate("förfallotid"));
	}
	@Test
	public void testWord_Sv_047() throws HyphenatorConfigurationException {
		assertEquals("dår­skap", hyph_sv_SE.hyphenate("dårskap"));
	}
	@Test
	public void testCompoundWord_Sv_048() throws HyphenatorConfigurationException {
		assertEquals("upp­hovs­rätts­la­gar", hyph_sv_SE.hyphenate("upphovsrättslagar"));
	}
	@Test
	public void testWord_Sv_049() throws HyphenatorConfigurationException {
		assertEquals("runt­om", hyph_sv_SE.hyphenate("runtom"));
	}
	@Test
	public void testCompoundWord_Sv_050() throws HyphenatorConfigurationException {
		assertEquals("ori­gi­nal­för­lag", hyph_sv_SE.hyphenate("originalförlag"));
	}
	@Test
	public void testWord_Sv_051() throws HyphenatorConfigurationException {
		assertEquals("för­sla­va", hyph_sv_SE.hyphenate("förslava"));
	}
	@Test
	public void testCompoundWord_Sv_052() throws HyphenatorConfigurationException {
		assertEquals("sam­hälls­ste­gen", hyph_sv_SE.hyphenate("samhällsstegen"));
	}
	@Test
	@Ignore
	public void testCompoundWord_Sv_053() throws HyphenatorConfigurationException {
		assertEquals("kan­vas­tält", hyph_sv_SE.hyphenate("kanvastält"));
	}
	@Test
	public void testWord_Sv_054() throws HyphenatorConfigurationException {
		assertEquals("för­strött", hyph_sv_SE.hyphenate("förstrött"));
	}
	@Test
	public void testCompoundWord_Sv_055() throws HyphenatorConfigurationException {
		//compound possibilities: as-kungen, ask-ungen
		assertEquals("ask­ung­en", hyph_sv_SE.hyphenate("askungen"));
	}
	@Test
	public void testWord_Sv_056() throws HyphenatorConfigurationException {
		assertEquals("önska­des", hyph_sv_SE.hyphenate("önskades"));
	}
	@Test
	public void testWord_Sv_057() throws HyphenatorConfigurationException {
		assertEquals("han­nahs", hyph_sv_SE.hyphenate("hannahs"));
	}
	@Test
	public void testCompoundWord_058() {
		assertEquals("alp­land­skap", hyph_sv_SE.hyphenate("alplandskap"));
	}
	@Test
	public void testWord_059() {
		assertEquals("land­skap", hyph_sv_SE.hyphenate("landskap"));
	}
	@Test
	@Ignore
	public void testCompoundWord_060() {
		assertEquals("in­di­ka­tors­lam­pa", hyph_sv_SE.hyphenate("indikatorslampa"));
	}
	@Test
	public void testCompoundWord_061() {
		//compound possibilities: bil-drulle, bild-rulle
		assertEquals("bildrul­le", hyph_sv_SE.hyphenate("bildrulle"));
	}
	@Test
	public void testCompoundWord_062() {
		assertEquals("skriv­bords­lam­pa", hyph_sv_SE.hyphenate("skrivbordslampa"));
	}
	@Test
	@Ignore
	public void testCompoundWord_063() {
		assertEquals("kon­tors­lam­pa", hyph_sv_SE.hyphenate("kontorslampa"));
	}
	@Test
	@Ignore
	public void testCompoundWord_064() {
		assertEquals("Fot­ang­lar", hyph_sv_SE.hyphenate("Fotanglar"));
	}
	@Test
	@Ignore
	public void testCompoundWord_065() {
		assertEquals("ko­kos­prick­ar", hyph_sv_SE.hyphenate("kokosprickar"));
	}
	@Test
	@Ignore
	public void testCompoundWord_066() {
		//compound possibilities: bomb-attrapp, bombat-trapp
		assertEquals("Bomb­att­rapp", hyph_sv_SE.hyphenate("Bombattrapp"));
	}
	@Test
	@Ignore
	public void testCompoundWord_067() {
		//non-standard hyphenation example: rygg-grej
		assertEquals("Ryggrej", hyph_sv_SE.hyphenate("Ryggrej"));
	}
	@Test
	@Ignore
	public void testCompoundWord_068() {
		assertEquals("Grek­land", hyph_sv_SE.hyphenate("Grekland"));
	}
	@Test
	@Ignore
	public void testCompoundWord_069() {
		//uppstöt not in dictionary
		assertEquals("Slem­upp­stöt", hyph_sv_SE.hyphenate("Slemuppstöt"));
	}
	@Test
	@Ignore
	public void testCompoundWord_070() {
		//compound possibilities: tax-ikväll, taxi-kväll
		assertEquals("Taxi­kväll", hyph_sv_SE.hyphenate("Taxikväll"));
	}
	@Test
	@Ignore
	public void testCompoundWord_071() {
		//compound possibilities: bomulls-toppar, bomull-stoppar
		assertEquals("Bom­ulls­topp­ar", hyph_sv_SE.hyphenate("Bomullstoppar"));
	}
	@Test
	@Ignore
	public void testCompoundWord_072() {
		assertEquals("Sko­snö­re", hyph_sv_SE.hyphenate("Skosnöre"));
	}
	@Test
	@Ignore
	public void testCompoundWord_073() {
		assertEquals("Ar­rest­rum­met", hyph_sv_SE.hyphenate("Arrestrummet"));
	}
	
	@Test
	@Ignore
	public void testCompoundWord_074() {
		assertEquals("Skit­snygg", hyph_sv_SE.hyphenate("Skitsnygg"));
	}
	
	@Test
	@Ignore
	public void testCompoundWord_075() {
		assertEquals("Yo­ga­shorts", hyph_sv_SE.hyphenate("Yogashorts"));
	}
	
	@Test
	@Ignore
	public void testCompoundWord_076() {
		assertEquals("Hand­slag", hyph_sv_SE.hyphenate("Handslag"));
	}
	
	@Test
	@Ignore
	public void testCompoundWord_077() {
		assertEquals("Ihop­pres­sa­de", hyph_sv_SE.hyphenate("Ihoppressade"));
	}
	
	@Test
	@Ignore
	public void testCompoundWord_078() {
		assertEquals("jeans­kjol", hyph_sv_SE.hyphenate("jeanskjol"));
	}
	
	@Test
	public void testCompoundWord_079() {
		assertEquals("pull­over", hyph_sv_SE.hyphenate("pullover"));
	}
	
	@Test
	@Ignore
	public void testCompoundWord_080() {
		assertEquals("hår­sling­or", hyph_sv_SE.hyphenate("hårslingor"));
	}
	@Test
	@Ignore
	public void testCompoundWord_081() {
		assertEquals("bil­olycka", hyph_sv_SE.hyphenate("bilolycka"));
	}
	@Test
	@Ignore
	public void testCompoundWord_082() {
		assertEquals("röd­svar­ta", hyph_sv_SE.hyphenate("rödsvarta"));
	}
	
	@Test
	@Ignore
	public void testCompoundWord_083() {
		assertEquals("kri­mi­nal­gen­ren", hyph_sv_SE.hyphenate("kriminalgenren"));
	}
	@Test
	@Ignore
	public void testCompoundWord_084() {
		assertEquals("makt­oba­lan­sen", hyph_sv_SE.hyphenate("maktobalansen"));
	}
	@Test
	@Ignore
	public void testCompoundWord_085() {
		//Non-standard: Upp-plockad
		assertEquals("Upplockad", hyph_sv_SE.hyphenate("Upplockad"));
	}
	@Test
	@Ignore
	public void testCompoundWord_086() {
		assertEquals("Mät­en­het", hyph_sv_SE.hyphenate("Mätenhet"));
	}
	
	@Test
	@Ignore
	public void testCompoundWord_087() {
		assertEquals("Scorpia­stil", hyph_sv_SE.hyphenate("Scorpiastil"));
	}
	@Test
	@Ignore
	public void testCompoundWord_088() {
		assertEquals("Gym­pa­skor", hyph_sv_SE.hyphenate("Gympaskor"));
	}
	@Test
	@Ignore
	public void testCompoundWord_089() {
		//Non-standard: Till-låtelse
		assertEquals("Tillåtelse", hyph_sv_SE.hyphenate("Tillåtelse"));
	}
	@Test
	@Ignore
	public void testCompoundWord_090() {
		assertEquals("Pi­stol­ar­men", hyph_sv_SE.hyphenate("Pistolarmen"));
	}
	@Test
	@Ignore
	public void testCompoundWord_091() {
		assertEquals("Scorpiarapport", hyph_sv_SE.hyphenate("Scorpiarapport"));
	}
	@Test
	public void testCompoundWord_092() {
		assertEquals("Brook­land", hyph_sv_SE.hyphenate("Brookland"));
	}
	@Test
	@Ignore
	public void testCompoundWord_093() {
		assertEquals("Iphone", hyph_sv_SE.hyphenate("Iphone"));
	}
	@Test
	@Ignore
	public void testCompoundWord_094() {
		assertEquals("Lat­ex­täck­ta", hyph_sv_SE.hyphenate("Latextäckta"));
	}
	@Test
	@Ignore
	public void testCompoundWord_095() {
		assertEquals("Bant­nings­pil­ler", hyph_sv_SE.hyphenate("Bantningspiller"));
	}
	@Test
	@Ignore
	public void testCompoundWord_096() {
		assertEquals("Tin­tin­al­bum", hyph_sv_SE.hyphenate("Tintinalbum"));
	}
	@Test
	@Ignore
	public void testCompoundWord_097() {
		assertEquals("Lycko­bring­an­de", hyph_sv_SE.hyphenate("Lyckobringande"));
	}
	@Test
	@Ignore
	public void testCompoundWord_098() {
		assertEquals("Hjälp­pro­gram­men", hyph_sv_SE.hyphenate("Hjälpprogrammen"));
	}
	@Test
	@Ignore
	public void testCompoundWord_099() {
		assertEquals("Jakt­week­end", hyph_sv_SE.hyphenate("Jaktweekend"));
	}
	@Test
	@Ignore
	public void testCompoundWord_100() {
		assertEquals("Regattaskrud", hyph_sv_SE.hyphenate("Regattaskrud"));
	}
	@Test
	@Ignore
	public void testCompoundWord_101() {
		assertEquals("Arab­re­pu­blik", hyph_sv_SE.hyphenate("Arabrepublik"));
	}
	@Test
	@Ignore
	public void testCompoundWord_102() {
		assertEquals("Bröl­lops­tår­ta", hyph_sv_SE.hyphenate("Bröllopstårta"));
	}
	@Test
	@Ignore
	public void testCompoundWord_103() {
		assertEquals("Pors­lins­ugg­lor", hyph_sv_SE.hyphenate("Porslinsugglor"));
	}
	@Test
	@Ignore
	public void testCompoundWord_104() {
		assertEquals("Spe­ci­al­av­del­ning", hyph_sv_SE.hyphenate("Specialavdelning"));
	}
	@Test
	@Ignore
	public void testCompoundWord_105() {
		assertEquals("Stu­dent­upp­lopp", hyph_sv_SE.hyphenate("Studentupplopp"));
	}
	@Test
	@Ignore
	public void testCompoundWord_106() {
		assertEquals("Köks­vas­ken", hyph_sv_SE.hyphenate("Köksvasken"));
	}
	@Test
	@Ignore
	public void testCompoundWord_107() {
		assertEquals("Bermuda­shorts", hyph_sv_SE.hyphenate("Bermudashorts"));
	}
	@Test
	@Ignore
	public void testCompoundWord_108() {
		assertEquals("Kom­mu­nist­ag­ent", hyph_sv_SE.hyphenate("Kommunistagent"));
	}
	@Test
	@Ignore
	public void testCompoundWord_109() {
		assertEquals("Ope­ra­käl­la­ren", hyph_sv_SE.hyphenate("Operakällaren"));
	}
	@Test
	@Ignore
	public void testCompoundWord_110() {
		assertEquals("Snobb­ung­do­mar", hyph_sv_SE.hyphenate("Snobbungdomar"));
	}
	
	@Test
	@Ignore
	public void testCompoundWord_111() {
		assertEquals("Mjölk­span­nen", hyph_sv_SE.hyphenate("Mjölkspannen"));
	}
	@Test
	@Ignore
	public void testCompoundWord_112() {
		assertEquals("Melanderhjelms", hyph_sv_SE.hyphenate("Melanderhjelms"));
	}
	@Test
	@Ignore
	public void testCompoundWord_113() {
		assertEquals("Sorg­slö­ja", hyph_sv_SE.hyphenate("Sorgslöja"));
	}
	@Test
	@Ignore
	public void testCompoundWord_114() {
		assertEquals("Mar­mor­trap­pan", hyph_sv_SE.hyphenate("Marmortrappan"));
	}
	@Test
	@Ignore
	public void testCompoundWord_115() {
		assertEquals("Sömn­druck­en", hyph_sv_SE.hyphenate("Sömndrucken"));
	}
	@Test
	@Ignore
	public void testCompoundWord_116() {
		assertEquals("Fat­tig­sa­na­to­ri­um", hyph_sv_SE.hyphenate("Fattigsanatorium"));
	}
	@Test
	public void testCompoundWord_117() {
		assertEquals("Tvärs­över", hyph_sv_SE.hyphenate("Tvärsöver"));
	}
	@Test
	@Ignore
	public void testCompoundWord_118() {
		assertEquals("Na­tio­nal­in­sam­ling", hyph_sv_SE.hyphenate("Nationalinsamling"));
	}
	@Test
	@Ignore
	public void testCompoundWord_119() {
		assertEquals("Fri­vak­ten", hyph_sv_SE.hyphenate("Frivakten"));
	}
	@Test
	@Ignore
	public void testCompoundWord_120() {
		assertEquals("Högt­upp­i­från", hyph_sv_SE.hyphenate("Högtuppifrån"));
	}
	@Test
	@Ignore
	public void testCompoundWord_121() {
		assertEquals("Tjej­ak­tigt", hyph_sv_SE.hyphenate("Tjejaktigt"));
	}
	@Test
	@Ignore
	public void testCompoundWord_122() {
		assertEquals("Skit­ro­man­tiskt", hyph_sv_SE.hyphenate("Skitromantiskt"));
	}
	@Test
	@Ignore
	public void testCompoundWord_123() {
		assertEquals("Sår­yt­an", hyph_sv_SE.hyphenate("Sårytan"));
	}
	@Test
	@Ignore
	public void testCompoundWord_124() {
		assertEquals("Ham­mars­lund", hyph_sv_SE.hyphenate("Hammarslund"));
	}
	@Test
	@Ignore
	public void testCompoundWord_125() {
		assertEquals("Guld­sling­a­de", hyph_sv_SE.hyphenate("Guldslingade"));
	}

	/*
Reg-nig
Sh-annon
Sh-add
*/
}