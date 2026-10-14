package suffixstructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Text-analysis operations built on top of the suffix array and the LCP array.
 *
 * <ul>
 *   <li>{@link #countDistinctSubstrings} - how many different substrings a text has;</li>
 *   <li>{@link #findRepeatedPhrases} - phrases that occur more than once in one document;</li>
 *   <li>{@link #compare} - shared phrases and a similarity score for two documents.</li>
 * </ul>
 *
 * <p>All methods expect text that has already been normalised (see {@code TextUtil.normalise}).</p>
 */
public class SuffixArrayAnalysis {

    /** Character that cannot appear in normal text; it glues two documents together. */
    private static final char SEPARATOR = '\u0001';

    private SuffixArrayAnalysis() {
    }

    // ------------------------------------------------------------------
    // Result types
    // ------------------------------------------------------------------

    /** A phrase together with every position where it occurs. */
    public static class Phrase {
        public final String text;
        public final List<Integer> positions;

        public Phrase(String text, List<Integer> positions) {
            this.text = text;
            this.positions = positions;
        }
    }

    /** Result of comparing two documents. */
    public static class Comparison {
        public final int lengthA;
        public final int lengthB;
        /** Characters of A that belong to a phrase shared with B (phrase length >= minLength). */
        public final int coveredA;
        public final int coveredB;
        /** (coveredA + coveredB) / (lengthA + lengthB), between 0.0 and 1.0. */
        public final double similarity;
        public final String longestCommon;
        public final int longestPositionA;
        public final int longestPositionB;
        public final List<String> sharedPhrases;

        Comparison(int lengthA, int lengthB, int coveredA, int coveredB, String longestCommon,
                   int longestPositionA, int longestPositionB, List<String> sharedPhrases) {
            this.lengthA = lengthA;
            this.lengthB = lengthB;
            this.coveredA = coveredA;
            this.coveredB = coveredB;
            int totalLength = lengthA + lengthB;
            this.similarity = totalLength == 0 ? 0.0 : (double) (coveredA + coveredB) / totalLength;
            this.longestCommon = longestCommon;
            this.longestPositionA = longestPositionA;
            this.longestPositionB = longestPositionB;
            this.sharedPhrases = sharedPhrases;
        }
    }

    // ------------------------------------------------------------------
    // Single-document operations
    // ------------------------------------------------------------------

    /**
     * Counts the different substrings of a text of length {@code n}.
     *
     * <p>There are n(n+1)/2 substrings in total, and every LCP value corresponds to
     * one duplicate that must be removed: distinct = n(n+1)/2 - sum(lcp).</p>
     */
    public static long countDistinctSubstrings(int n, int[] lcp) {
        return (long) n * (n + 1) / 2 - LCP.sum(lcp);
    }

    /**
     * Finds the longest substring that occurs at least twice (the maximum LCP value).
     *
     * @return the phrase, or {@code null} when nothing repeats
     */
    public static Phrase longestRepeatedSubstring(String text, int[] suffixArray, int[] lcp) {
        int bestLength = 0;
        int bestRank = -1;

        for (int rank = 0; rank < lcp.length; rank++) {
            if (lcp[rank] > bestLength) {
                bestLength = lcp[rank];
                bestRank = rank;
            }
        }
        if (bestRank < 0) {
            return null;
        }

        String phrase = text.substring(suffixArray[bestRank], suffixArray[bestRank] + bestLength);
        return new Phrase(phrase, SuffixArray.search(text, suffixArray, phrase));
    }

    /**
     * Lists repeated phrases, longest first.
     *
     * <p>Candidates come from the LCP array: two neighbouring suffixes with LCP = L share
     * a phrase of length L. A candidate is dropped when a longer reported phrase already
     * contains it and occurs equally often, so the list does not fill up with fragments.</p>
     *
     * @param minLength shortest phrase (in characters, after trimming) worth reporting
     * @param limit     maximum number of phrases to return
     */
    public static List<Phrase> findRepeatedPhrases(String text, int[] suffixArray, int[] lcp,
                                                   int minLength, int limit) {
        // 1. Collect distinct candidate phrases from the LCP array.
        Set<String> candidates = new LinkedHashSet<>();
        for (int rank = 0; rank < lcp.length; rank++) {
            if (lcp[rank] < minLength) {
                continue;
            }
            String phrase = text.substring(suffixArray[rank], suffixArray[rank] + lcp[rank]).trim();
            if (phrase.length() >= minLength && containsLetterOrDigit(phrase)) {
                candidates.add(phrase);
            }
        }

        // 2. Find every occurrence of each candidate with the suffix array.
        List<Phrase> phrases = new ArrayList<>();
        for (String candidate : candidates) {
            phrases.add(new Phrase(candidate, SuffixArray.search(text, suffixArray, candidate)));
        }

        // 3. Longest first; ties broken by number of occurrences.
        Collections.sort(phrases, new Comparator<Phrase>() {
            @Override
            public int compare(Phrase a, Phrase b) {
                if (a.text.length() != b.text.length()) {
                    return Integer.compare(b.text.length(), a.text.length());
                }
                return Integer.compare(b.positions.size(), a.positions.size());
            }
        });

        // 4. Keep only phrases that are not fully explained by a longer one.
        List<Phrase> kept = new ArrayList<>();
        for (Phrase phrase : phrases) {
            if (kept.size() >= limit) {
                break;
            }
            if (!isExplainedByLongerPhrase(phrase, kept)) {
                kept.add(phrase);
            }
        }
        return kept;
    }

    private static boolean isExplainedByLongerPhrase(Phrase candidate, List<Phrase> longerPhrases) {
        for (Phrase longer : longerPhrases) {
            boolean containsIt = longer.text.contains(candidate.text);
            boolean sameFrequency = longer.positions.size() == candidate.positions.size();
            if (containsIt && sameFrequency) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsLetterOrDigit(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetterOrDigit(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Two-document comparison
    // ------------------------------------------------------------------

    /**
     * Compares two documents using ONE suffix array built over {@code a + SEPARATOR + b}.
     *
     * <p>In the sorted order, suffixes that share a long prefix sit next to each other.
     * For every position of A, the longest text that also occurs in B is found by walking
     * to the nearest suffix that belongs to B and taking the smallest LCP value on the way
     * (the classic "matching statistics" idea). Because the separator occurs only once,
     * no match can run across the boundary between the documents.</p>
     *
     * <p>Time: suffix-array construction plus a scan that is fast for normal documents.</p>
     *
     * @param minLength shortest shared phrase that counts towards the similarity score
     */
    public static Comparison compare(String a, String b, int minLength) {
        String combined = a + SEPARATOR + b;
        int[] suffixArray = SuffixArray.build(combined);
        int[] lcp = Kasai.build(combined, suffixArray);
        int boundary = a.length();      // index of the separator inside "combined"

        int[] matchInA = new int[a.length()];      // longest text starting here that also occurs in B
        int[] partnerInB = new int[a.length()];    // where that text starts in B
        int[] matchInB = new int[b.length()];
        int[] partnerInA = new int[b.length()];

        for (int rank = 0; rank < suffixArray.length; rank++) {
            int position = suffixArray[rank];
            if (position < boundary) {
                int[] best = longestMatchWithOtherDocument(rank, true, suffixArray, lcp, boundary);
                matchInA[position] = best[0];
                partnerInB[position] = best[1] - boundary - 1;
            } else if (position > boundary) {
                int[] best = longestMatchWithOtherDocument(rank, false, suffixArray, lcp, boundary);
                matchInB[position - boundary - 1] = best[0];
                partnerInA[position - boundary - 1] = best[1];
            }
        }

        // Longest common substring = largest matching statistic.
        int longestLength = 0;
        int longestPositionA = -1;
        int longestPositionB = -1;
        for (int i = 0; i < matchInA.length; i++) {
            if (matchInA[i] > longestLength) {
                longestLength = matchInA[i];
                longestPositionA = i;
                longestPositionB = partnerInB[i];
            }
        }
        String longestCommon = longestLength == 0 ? "" : a.substring(longestPositionA, longestPositionA + longestLength);

        return new Comparison(a.length(), b.length(),
                countCoveredCharacters(matchInA, minLength),
                countCoveredCharacters(matchInB, minLength),
                longestCommon, longestPositionA, longestPositionB,
                collectSharedPhrases(a, matchInA, minLength, 5));
    }

    /**
     * Looks up and down from {@code rank} for the nearest suffix that belongs to the other
     * document and returns {longest common prefix length, start position of that suffix
     * inside the combined text}.
     */
    private static int[] longestMatchWithOtherDocument(int rank, boolean fromA, int[] suffixArray,
                                                       int[] lcp, int boundary) {
        int bestLength = 0;
        int bestPartner = -1;

        // Walk towards smaller ranks.
        int running = Integer.MAX_VALUE;
        for (int k = rank - 1; k >= 0 && running > bestLength; k--) {
            running = Math.min(running, lcp[k]);
            if (belongsToOtherDocument(suffixArray[k], fromA, boundary)) {
                if (running > bestLength) {
                    bestLength = running;
                    bestPartner = suffixArray[k];
                }
                break;
            }
        }

        // Walk towards larger ranks.
        running = Integer.MAX_VALUE;
        for (int k = rank; k < lcp.length && running > bestLength; k++) {
            running = Math.min(running, lcp[k]);
            if (belongsToOtherDocument(suffixArray[k + 1], fromA, boundary)) {
                if (running > bestLength) {
                    bestLength = running;
                    bestPartner = suffixArray[k + 1];
                }
                break;
            }
        }
        return new int[] {bestLength, bestPartner};
    }

    private static boolean belongsToOtherDocument(int position, boolean fromA, int boundary) {
        return fromA ? position > boundary : position < boundary;
    }

    /** Counts characters that are part of at least one shared phrase of length >= minLength. */
    private static int countCoveredCharacters(int[] matchLengths, int minLength) {
        boolean[] covered = new boolean[matchLengths.length];
        for (int start = 0; start < matchLengths.length; start++) {
            if (matchLengths[start] >= minLength) {
                for (int i = start; i < start + matchLengths[start]; i++) {
                    covered[i] = true;
                }
            }
        }
        int count = 0;
        for (boolean isCovered : covered) {
            if (isCovered) {
                count++;
            }
        }
        return count;
    }

    /** Extracts the longest distinct shared phrases (skipping ones inside a longer phrase). */
    private static List<String> collectSharedPhrases(String a, int[] matchInA, int minLength, int limit) {
        Set<String> found = new LinkedHashSet<>();
        for (int start = 0; start < matchInA.length; start++) {
            int length = matchInA[start];
            boolean insideEarlierMatch = start > 0 && matchInA[start - 1] > length;
            if (length >= minLength && !insideEarlierMatch) {
                String phrase = a.substring(start, start + length).trim();
                if (phrase.length() >= minLength && containsLetterOrDigit(phrase)) {
                    found.add(phrase);
                }
            }
        }

        List<String> phrases = new ArrayList<>(found);
        Collections.sort(phrases, new Comparator<String>() {
            @Override
            public int compare(String x, String y) {
                return Integer.compare(y.length(), x.length());
            }
        });

        List<String> kept = new ArrayList<>();
        for (String phrase : phrases) {
            if (kept.size() >= limit) {
                break;
            }
            boolean inside = false;
            for (String longer : kept) {
                if (longer.contains(phrase)) {
                    inside = true;
                    break;
                }
            }
            if (!inside) {
                kept.add(phrase);
            }
        }
        return kept;
    }
}
