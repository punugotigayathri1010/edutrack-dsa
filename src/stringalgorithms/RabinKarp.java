package stringalgorithms;

import java.util.ArrayList;
import java.util.List;

/**
 * Rabin-Karp pattern search using a rolling hash.
 *
 * <p>A hash of the pattern is compared with a hash of every window of the text.
 * Hashes are updated in O(1) when the window slides one character, and a
 * character-by-character check is only done when the hashes are equal (to rule
 * out hash collisions). Useful for searching codes such as "CS301".</p>
 *
 * <p>Average time: O(n + m).</p>
 */
public class RabinKarp {

    private static final long BASE = 256;
    private static final long MODULUS = 1_000_000_007L;

    private RabinKarp() {
    }

    /** Returns the start index of every exact (case-sensitive) occurrence of {@code pattern}. */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        int patternLength = pattern.length();

        if (patternLength == 0 || patternLength > text.length()) {
            return matches;
        }

        // highestPower = BASE^(patternLength-1) mod MODULUS, used to remove the leading character.
        long highestPower = 1;
        for (int i = 0; i < patternLength - 1; i++) {
            highestPower = highestPower * BASE % MODULUS;
        }

        long patternHash = 0;
        long windowHash = 0;
        for (int i = 0; i < patternLength; i++) {
            patternHash = (patternHash * BASE + pattern.charAt(i)) % MODULUS;
            windowHash = (windowHash * BASE + text.charAt(i)) % MODULUS;
        }

        int lastStart = text.length() - patternLength;
        for (int start = 0; start <= lastStart; start++) {
            if (patternHash == windowHash && text.regionMatches(start, pattern, 0, patternLength)) {
                matches.add(start);
            }

            if (start < lastStart) {
                // Slide the window: drop text[start], add text[start + patternLength].
                long withoutLeading = (windowHash - text.charAt(start) * highestPower % MODULUS + MODULUS) % MODULUS;
                windowHash = (withoutLeading * BASE + text.charAt(start + patternLength)) % MODULUS;
            }
        }
        return matches;
    }
}
