package nl.dedicon.pipeline.braille.symbolslist;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import nl.dedicon.pipeline.braille.calabash.impl.SymbolsListStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dedicon BRL
 * Maps BRL characters to Unicode Braille characters
 * 
 * Based on Wim Berden's dediconbrl.php
 * Modifications:
 *   - added missing characters so that all uppercase/lowercase combinations are present
 *   - reordered so that an uppercase character always precedes the lowercase variant
 *   - changed Ú mapping from 0x2839 to 0x283E
 * 
 * @author Paul Rambags
 */
public class DediconBrl {

    private static final Map<Character, Character> brlToUnicodeMap = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(SymbolsListStep.class);

    private static void map (char brlChar, int unicodeChar) {
        brlToUnicodeMap.put(brlChar, (char)unicodeChar);
    }
    
    static {
        map(' ', 0x2800);
        map('1', 0x2801);
        map('A', 0x2801);
        map('a', 0x2801);	
        map(',', 0x2802);
        map('2', 0x2803);
        map('B', 0x2803);
        map('b', 0x2803);
        map('\'', 0x2804);
        map('K', 0x2805);
        map('k', 0x2805);
        map(';', 0x2806);
        map('L', 0x2807);
        map('l', 0x2807);
        map('&', 0x2808);
        map('3', 0x2809);
        map('C', 0x2809);
        map('c', 0x2809);
        map('9', 0x280A);
        map('I', 0x280A);
        map('i', 0x280A);
        map('6', 0x280B);
        map('F', 0x280B);
        map('f', 0x280B);
        map('/', 0x280C);
        map('ì', 0x280C);
        map('í', 0x280C);
        map('M', 0x280D);
        map('m', 0x280D);
        map('S', 0x280E);
        map('s', 0x280E);
        map('P', 0x280F);
        map('p', 0x280F);
        map('|', 0x2810);
        map('5', 0x2811);
        map('E', 0x2811);
        map('e', 0x2811);
        map(':', 0x2812);
        map('8', 0x2813);
        map('H', 0x2813);
        map('h', 0x2813);
        map('*', 0x2814);
        map('O', 0x2815);
        map('o', 0x2815);
        map('!', 0x2816);
        map('+', 0x2816);
        map('¡', 0x2816);
        map('R', 0x2817);
        map('r', 0x2817);
        map(']', 0x2818);
        map('4', 0x2819);
        map('D', 0x2819);
        map('d', 0x2819);
        map('0', 0x281A);
        map('J', 0x281A);
        map('j', 0x281A);
        map('7', 0x281B);
        map('G', 0x281B);
        map('g', 0x281B);
        map('Ä', 0x281C);
        map('ä', 0x281C);
        map('Å', 0x281C);
        map('å', 0x281C);
        map('N', 0x281D);
        map('n', 0x281D);
        map('T', 0x281E);
        map('t', 0x281E);
        map('Q', 0x281F);
        map('q', 0x281F);
        map('@', 0x2820);
        map('\\', 0x2821);
        map('Â', 0x2821);
        map('â', 0x2821);
        map('?', 0x2822);
        map('¿', 0x2822);
        map('Ê', 0x2823);
        map('ê', 0x2823);
        map('-', 0x2824);
        map('U', 0x2825);
        map('u', 0x2825);
        map('<', 0x2826);
        map('}', 0x2826);
        map('V', 0x2827);
        map('v', 0x2827);
        map('{', 0x2828);
        map('Î', 0x2829);
        map('î', 0x2829);
        map('Ö', 0x282A);
        map('ö', 0x282A);
        map('Ë', 0x282B);
        map('ë', 0x282B);
        map('Ò', 0x282C);
        map('ò', 0x282C);
        map('Ó', 0x282C);
        map('ó', 0x282C);
        map('X', 0x282D);
        map('x', 0x282D);
        map('È', 0x282E);
        map('è', 0x282E);
        map('ß', 0x282E);
        map('#', 0x282F);
        map('Ç', 0x282F);
        map('ç', 0x282F);
        map('~', 0x2830);
        map('Û', 0x2831);
        map('û', 0x2831);
        map('$', 0x2832);
        map('.', 0x2832);
        map('Ü', 0x2833);
        map('ü', 0x2833);
        map('"', 0x2834);
        map('>', 0x2834);
        map('Z', 0x2835);
        map('z', 0x2835);
        map('(', 0x2836);
        map(')', 0x2836);
        map('=', 0x2836);
        map('À', 0x2837);
        map('à', 0x2837);
        map('Á', 0x2837);
        map('á', 0x2837);
        map('[', 0x2838);
        map('Ô', 0x2839);
        map('ô', 0x2839);
        map('W', 0x283A);
        map('w', 0x283A);
        map('Ï', 0x283B);
        map('ï', 0x283B);
        map('Ñ', 0x283B);
        map('ñ', 0x283B);
        map('%', 0x283C);
        map('Y', 0x283D);
        map('y', 0x283D);
        map('Ù', 0x283E);
        map('ù', 0x283E);
        map('Ú', 0x283E);
        map('ú', 0x283E);
        map('É', 0x283F);
        map('é', 0x283F);
    }

    /**
     * Convert BRL characters to unicode braille, if necessary.
     * 
     * Sometimes a BRL character must be converted to a unicode braille character.
     * For instance, the pipeline would convert BRL character ~ to (5 26) but it should be converted to (56) according to the table above. 
     * Case information gets lost when converting to unicode braille.
     * Therefore, as much as possible we let the pipeline take care of the conversion to unicode braille.
     * 
     * @param braille Symbol replacement
     * @return certain BRL characters replaced by Unicode braille characters
     */
    public static String convert(String braille) {
        StringWriter result = new StringWriter();
        for (int i = 0; i < braille.length(); i++) {
            char brailleChar = braille.charAt(i);
            if (Character.isLetterOrDigit(brailleChar)) {
                // let the pipeline convert this BRL character to unicode braille
                // and take care of capitalization
                result.append(brailleChar);
            } else {
                Character unicodeChar = brlToUnicodeMap.get(brailleChar);
                if (unicodeChar != null) {
                    result.append(unicodeChar);
                } else {
                    result.append(brailleChar);
                    if (!Utils.isBraille(brailleChar)) {
                        logger.warn("Unknown BRL character: '{}'", brailleChar);
                    }
                }
            }
        }
        return result.toString();
    }
}
