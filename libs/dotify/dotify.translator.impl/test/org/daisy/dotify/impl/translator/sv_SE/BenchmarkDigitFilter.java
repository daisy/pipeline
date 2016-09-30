package org.daisy.dotify.impl.translator.sv_SE;

import org.daisy.dotify.impl.translator.sv_SE.DigitFilter.Algorithm;

public class BenchmarkDigitFilter {

	public static void main(String[] args) {
		doFilterCompareTest("1.2");
		doFilterCompareTest("Eftersom allt resande är en miljöpåverkande faktor bör alltid telefonkonferens/videokonferens övervägas i första hand. Möjligheten till att resa med tåg bör avvägas vid varje tjänsteresa, då miljöpåverkan skiljer sig avsevärt mellan tåg och flygresor. Vid resa mellan t.ex. Stockholm – Göteborg tur och retur, blir koldioxidutsläppen blivit 143 kg med flyg, men endast 0,0019 kg med tåg.");
		doFilterCompareTest("Av hälsoskäl bör resande ske under arbetstid. Övernattning ska övervägas om resandet medför extremt tidig avfärd eller extremt sen ankomst till bostad (före klockan 06.00 eller efter klockan 23.00.) Grunden är att resor planeras i samråd mellan chef och resenär. Planering bör ske i god tid för att ekonomiskt mest fördelaktiga biljetter kan beställas. Dessutom är det viktigt att arbetstidslagen uppfylls, ALFA kap. 4. Vid restid före klockan 06.00 avresedagen eller efter klockan 18.00 hemresedagen utgår ersättning för restid i form av plusflex (mertid). Förutom skattefria ersättningar vid tjänsteresor enligt Utlandsreseförordningen, har MTM ett lokalt avtal med skattepliktig ersättning för tjänsteresor.");
		doFilterCompareTest("En stor del av MTM:s verksamhet är utåtriktad med ett omfattande resande som följd.");
	}
	
	private static void doFilterCompareTest(String inp) {
		DigitFilter  d1 = new DigitFilter(Algorithm.REGEX);
		DigitFilter  d2 = new DigitFilter(Algorithm.SPECIALIZED);
		
		doFilterTest(inp, d1);
		doFilterTest(inp, d2);
	}
	
	private static void doFilterTest(String inp, DigitFilter d) {
		long t = System.currentTimeMillis();
		for (int i = 0; i<5000; i++) {
			d.filter(inp);
		}
		System.out.println(d.getAlgorithm() + " -> " + (System.currentTimeMillis()-t) + "\t" + d.filter(inp));
	}

}
