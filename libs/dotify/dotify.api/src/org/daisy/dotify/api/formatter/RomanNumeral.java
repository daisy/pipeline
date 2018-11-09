package org.daisy.dotify.api.formatter;

/**
 * Provides a static method for converting binary integers to Roman numbers.
 * <p>http://leepoint.net/notes-java/examples/components/romanNumerals/romanNumeral.html</p>
 * <p>Illustrates: Static inner value class, StringBuffer, throw exceptions.</p>
 * <p>2006-12-29 - Placed in public domain.</p>
 * @author Fred Swartz
 *
 */
class RomanNumeral {
 //================================================================ constant
 // This could be alternatively be done with parallel arrays.
 // Another alternative would be Pair<Integer, String>
 static final RomanValue[] ROMAN_VALUE_TABLE = {
     new RomanValue(1000, "M"),
     new RomanValue( 900, "CM"),
     new RomanValue( 500, "D"),
     new RomanValue( 400, "CD"),
     new RomanValue( 100, "C"),
     new RomanValue(  90, "XC"),
     new RomanValue(  50, "L"),
     new RomanValue(  40, "XL"),
     new RomanValue(  10, "X"),
     new RomanValue(   9, "IX"),
     new RomanValue(   5, "V"),
     new RomanValue(   4, "IV"),
     new RomanValue(   1, "I")
 };
 
 //============================================================== int2roman
 public static String int2roman(int n) {
     if (n >= 4000  || n < 1) {
         throw new NumberFormatException("Numbers must be in range 1-3999");
     }
     StringBuffer result = new StringBuffer(10);
     
     //... Start with largest value, and work toward smallest.
     for (RomanValue equiv : ROMAN_VALUE_TABLE) {
         //... Remove as many of this value as possible (maybe none).
         while (n >= equiv.intVal) {
             n -= equiv.intVal;            // Subtract value.
             result.append(equiv.romVal);  // Add roman equivalent.
         }
     }
     return result.toString();
 }
 
 ///////////////////////////////////////////////////////// inner value class
 private static class RomanValue {
     //============================================================== fields
     //... No need to make this fields private because they are
     //    used only in this private value class.
     int    intVal;     // Integer value.
     String romVal;     // Equivalent roman numeral.
     
     //========================================================= constructor
     RomanValue(int dec, String rom) {
         this.intVal = dec;
         this.romVal = rom;
     }
 }

}
