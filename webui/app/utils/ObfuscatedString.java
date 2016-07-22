/*
 * ObfuscatedString.java
 *
 * Created on 20. Mai 2006, 11:44
 */
/*
 * Copyright 2006 Schlichtherle IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//package de.schlichtherle.util;
package utils;

import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import play.Logger;

/**
 * Adapted for use with the DAISY Pipeline 2 Web UI by Jostein Austvik Jacobsen.
 * The only thing exposed by this class are now the static methods
 * "obfuscate" and "unobfuscate". They now both take a string as an argument
 * and gives a string in return.
 * 
 * 
 * 
 * Original documentation:
 * 
 * A utility class used to replace string literals in Java source code with an
 * obfuscated representation of the string.
 * Client applications should use this class to implement the
 * {@link de.schlichtherle.license.LicenseParam},
 * {@link de.schlichtherle.license.KeyStoreParam}
 * and {@link de.schlichtherle.license.CipherParam} interfaces in order to
 * make it considerably hard (although still not impossible) for a reverse
 * engineer to find these string literals while providing comparably fast
 * operation and minimum memory footprint.
 * <p>
 * To use this class you need to provide the string literal to obfuscate as a
 * parameter to the static {@link #obfuscate} method.
 * Its return value is a string which contains the Java code which you should
 * substitute for the string literal in the client application's source code.
 * <p>
 * Please note that obfuscation is <em>not</em> equal to encryption:
 * In contrast to the obfuscation provided by this class, encryption is
 * comparably slow and expensive in terms of resources - no matter what
 * algorithm is actually used.
 * More importantly, encrypting string literals in Java code does not really
 * increase the privacy of these strings compared to obfuscation as long as
 * the encryption key is still placed in the Java code itself and tracing the
 * calls to the JVM is possible.
 * Hence, obfuscation is selected in favour of encryption. 
 * <p>
 * In order to provide a reasonable level of security for your application,
 * you should <em>always</em> obfuscate the <em>application code</em> too,
 * including this class.
 * Otherwise, a reverse engineer could simply use the UNIX "strings" utility
 * to search for all usages of this class, which would render its use
 * completely pointless!
 * In case you're looking for a free Java code obfuscation tool for this task,
 * please consider ProGuard, available and usable for free at
 * <a href="http://proguard.sourceforge.net">http://proguard.sourceforge.net</a>.
 * <p>
 * This class is designed to be thread safe.
 *
 * @author Christian Schlichtherle
 */
public final class ObfuscatedString {

    //
    // Regular string constants:
    // These can't be obfuscated using this class because it would cause an
    // illegal recursion in the class initializer.
    // Still we want these strings to be obfuscated in a minimal way in order
    // to prevent a reverse engineer from locating the obfuscated version
    // of this class (created with e.g. ProGuard) by searching for these
    // strings.
    // This is not a 100% secure protection, but it's supposed to make
    // the life of a reverse engineer a little bit harder.
    //

    private static final String UTF8
            = new String(new char[] { '\u0055', '\u0054', '\u0046', '\u0038' }); // => "UTF8"

    /**
     * Decodes a long value from eight bytes in little endian order,
     * beginning at index <code>off</code>.
     * This is the inverse of {@link #toBytes(long, byte[], int)}.
     * If less than eight bytes are remaining in the array,
     * only these low order bytes are processed and the complementary high
     * order bytes of the returned value are set to zero.
     *
     * @param bytes The array containing the bytes to decode in little endian
     *        order.
     * @param off The offset of the bytes in the array.
     *
     * @return The decoded long value.
     */
    private static final long toLong(final byte[] bytes, int off) {
        long l = 0;

        final int end = Math.min(bytes.length, off + 8);
        for (int i = end; --i >= off; ) {
            l <<= 8;
            l |= bytes[i] & 0xFF;
        }
        
        return l;
    }
    
//    /**
//      * Convenience wrapper for toLong(byte[] bytes, int off).
//      */
//    private static final long toLong(String hex) {
//        final byte[] bytes;
//        try {
//            bytes = hex.getBytes(UTF8);
//        } catch (UnsupportedEncodingException ex) {
//            throw new AssertionError(ex); // UTF8 is always supported
//        }
//        return toLong(bytes, 0);
//    }

    /**
     * Encodes a long value to eight bytes in little endian order,
     * beginning at index <code>off</code>.
     * This is the inverse of {@link #toLong(byte[], int)}.
     * If less than eight bytes are remaining in the array,
     * only these low order bytes of the long value are processed and the
     * complementary high order bytes are ignored.
     *
     * @param l The long value to encode.
     * @param bytes The array which holds the encoded bytes upon return.
     * @param off The offset of the bytes in the array.
     */
    private static final void toBytes(long l, byte[] bytes, int off) {
        final int end = Math.min(bytes.length, off + 8);
        for (int i = off; i < end; i++) {
            bytes[i] = (byte) l;
            l >>= 8;
        }
    }

    /**
     * Returns a obfuscated string representation of the input string.
     * 
     * Obfuscation is performed by encoding the given string into UTF8 and then
     * XOR-ing a sequence of pseudo random numbers to it in order to prevent
     * attacks based on character probability.
     * The result is encoded into an array of longs.
     * The sequence of pseudo random numbers is seeded with a 48 bit random
     * number in order to provide a non-deterministic result.
     * Hence, two subsequent calls with the same string will produce equal
     * results by a chance of 1/(2<sup>48</sup>-1) (0 isn't used as a seed) only!
     * 
     * @param text The string to obfuscate. This may not contain null characters.
     *
     * @return The obfuscated string representation of the input text.
     *
     * @throws IllegalArgumentException If <code>text</code> contains a null
     *         character.
     */
    public static String obfuscate(final String text) {
        // Any string literal used in this method is represented as an
        // ObfuscatedString unless it's no longer than two characters.
        // This should help to prevent location of this class in the obfuscated
        // code generated by ProGuard.
        
        if (text.indexOf(0) != -1) {
            throw new IllegalArgumentException(new ObfuscatedString(new long[] {
                0x241005931110FC70L, 0xDCD925A88EAD9F37L, 0x19ADA1C861E2A85DL,
                0x9A5948E700FCAD8AL, 0x2E11C83A72441DE2L
            }).toString()); // => "Null characters are not allowed!";
        }

        // Obtain the string as a sequence of UTF-8 encoded bytes.
        final byte[] encoded;
        try {
            encoded = text.getBytes(UTF8);
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex); // UTF8 is always supported
        }
        
        // Create and seed a Pseudo Random Number Generator (PRNG) with a
        // random long number generated by another PRNG.
        //   Note that using a PRNG to generate a seed for itself wouldn't make
        // it behave deterministically because each subsequent call to
        // setSeed() SUPPLEMENTS, rather than replaces, the existing seed.
        long seed;
        Random prng = new Random(); // randomly seeded
        do {
            seed = prng.nextLong(); // seed strength is effectively 48 bits
        } while (seed == 0); // setSeed(0) could cause issues
        prng = new Random(seed);

        // Append the seed as the first element of the encoded array of longs.
        List<Long> longList = new ArrayList<Long>();
        longList.add(seed);

        final int length = encoded.length;
        for (int i = 0; i < length; i += 8) {
            final long key = prng.nextLong();
            // Compute the value of the next array element as an obfuscated
            // version of the next eight bytes of the UTF8 encoded string.
            final long obfuscated = toLong(encoded, i) ^ key;
            longList.add(obfuscated);
        }
        
        String obfuscatedString = "";
        for (int i = 0; i < longList.size(); i++)
            obfuscatedString += longList.get(i) + (i < longList.size()-1 ? " " : "");
        return obfuscatedString;
    }

    /** The clear text string. */
    private final String s;
    
    /**
     * Returns the original text.
     * 
     * @param obfuscated The obfuscated text.
     * @return
     */
    public static String unobfuscate(String obfuscated) {
        String[] splitString = obfuscated.split(" ");
        long[] obfuscatedLongArray = new long[splitString.length];
        try {
	        for (int i = 0; i < splitString.length; i++)
	            obfuscatedLongArray[i] = Long.parseLong(splitString[i]);
        } catch (NumberFormatException e) {
        	// Create new exception to avoid listing the unobfuscated value in logs etc
        	Logger.error("Unable to parse unobfuscated string", new NumberFormatException("Unable to parse unobfuscated string"));
        	return null;
        } catch (Exception e) {
        	// Catch all exceptions and create a new generic exception to avoid listing the unobfuscated value in logs etc
        	Logger.error("Unable to parse unobfuscated string", new Exception(e.getClass().getName()+" occured while parsing the unobfuscated string"));
        	return null;
        }
        return new ObfuscatedString(obfuscatedLongArray).toString();
    }
    
    /**
     * Decodes an obfuscated string from its representation as an array of
     * longs.
     *
     * @param obfuscated The obfuscated representation of the string.
     *
     * @throws NullPointerException If <code>obfuscated</code> is
     *         <code>null</code>.
     * @throws ArrayIndexOutOfBoundsException If the provided array does not
     *         contain at least one element.
     */
    private ObfuscatedString(final long[] obfuscated) {
        final int length = obfuscated.length;

        // The original UTF8 encoded string was probably not a multiple
        // of eight bytes long and is thus actually shorter than this array.
        final byte[] encoded = new byte[8 * (length - 1)];

        // Obtain the seed and initialize a new PRNG with it.
        final long seed = obfuscated[0];
        final Random prng = new Random(seed);

        // De-obfuscate.
        for (int i = 1; i < length; i++) {
            final long key = prng.nextLong();
            toBytes(obfuscated[i] ^ key, encoded, 8 * (i - 1));
        }

        // Decode the UTF-8 encoded byte array into a string.
        // This will create null characters at the end of the decoded string
        // in case the original UTF8 encoded string was not a multiple of
        // eight bytes long.
        final String decoded;
        try {
            decoded = new String(encoded, UTF8);
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex); // UTF-8 is always supported
        }
        
        // Cut off trailing null characters in case the original UTF8 encoded
        // string was not a multiple of eight bytes long.
        final int i = decoded.indexOf(0);
        s = i != -1 ? decoded.substring(0, i) : decoded;
    }

    /**
     * Returns the original string which was obfuscated by the
     * {@link #obfuscate} method.
     */
    public String toString() {
        return s;
    }
}
