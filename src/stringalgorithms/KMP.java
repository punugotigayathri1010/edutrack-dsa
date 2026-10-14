package stringalgorithms;

import java.util.ArrayList;
import java.util.List;

/**
 * Knuth-Morris-Pratt (KMP) pattern search - case-insensitive.
 *
 * <p>KMP never moves backwards in the text. It first builds a "longest proper
 * prefix which is also a suffix" (LPS) table for the pattern, and uses that table
 * to decide how far the pattern can safely slide after a mismatch.</p>
 *
 * <p>Time: O(n + m) for a text of length n and a pattern of length m.</p>
 */
public class KMP {

    private KMP() {
    }

    /**
     * Finds every position where {@code pattern} occurs in {@code text}.
     *
     * @return start indexes in increasing order (empty list for an empty pattern)
     */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty()) {
            return matches;
        }

        int[] lps = buildLpsTable(pattern);
        int textIndex = 0;
        int patternIndex = 0;

        while (textIndex < text.length()) {
            if (sameChar(text.charAt(textIndex), pattern.charAt(patternIndex))) {
                textIndex++;
                patternIndex++;

                if (patternIndex == pattern.length()) {
                    matches.add(textIndex - patternIndex);
                    patternIndex = lps[patternIndex - 1]; // keep going to find overlapping matches
                }
            } else if (patternIndex > 0) {
                patternIndex = lps[patternIndex - 1];     // slide the pattern, do not move in the text
            } else {
                textIndex++;
            }
        }
        return matches;
    }

    /**
     * lps[i] = length of the longest proper prefix of pattern[0..i] that is also
     * a suffix of pattern[0..i].
     */
    private static int[] buildLpsTable(String pattern) {
        int[] lps = new int[pattern.length()];
        int prefixLength = 0;
        int i = 1;

        while (i < pattern.length()) {
            if (sameChar(pattern.charAt(i), pattern.charAt(prefixLength))) {
                prefixLength++;
                lps[i] = prefixLength;
                i++;
            } else if (prefixLength > 0) {
                prefixLength = lps[prefixLength - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }
        return lps;
    }

    private static boolean sameChar(char a, char b) {
        return Character.toLowerCase(a) == Character.toLowerCase(b);
    }
}
