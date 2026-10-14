package suffixstructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Suffix array: the starting positions of all suffixes of a text, listed in
 * alphabetical order of the suffixes.
 *
 * <p>Example for "banana": suffixes sorted are a, ana, anana, banana, na, nana,
 * so the suffix array is [5, 3, 1, 0, 4, 2].</p>
 *
 * <p>This class offers two operations:</p>
 * <ul>
 *   <li>{@link #build} - prefix-doubling construction in O(n log^2 n);</li>
 *   <li>{@link #search} - find all occurrences of a pattern with binary search
 *       in O(m log n + k) for a pattern of length m and k matches.</li>
 * </ul>
 */
public class SuffixArray {

    private SuffixArray() {
    }

    // ------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------

    /**
     * Builds the suffix array with the prefix-doubling technique.
     *
     * <p>Round k sorts suffixes by their first 2k characters. Each suffix is described
     * by a pair (rank of its first k characters, rank of the next k characters), and
     * ranks from the previous round give those pairs cheaply. After about log2(n)
     * rounds every suffix has a unique rank.</p>
     */
    public static int[] build(String text) {
        int n = text.length();
        if (n == 0) {
            return new int[0];
        }

        Integer[] order = new Integer[n];   // suffix start positions, being sorted
        int[] rank = new int[n];            // current rank of each suffix
        int[] nextRank = new int[n];

        for (int i = 0; i < n; i++) {
            order[i] = i;
            rank[i] = text.charAt(i);       // round 0: rank by the first character
        }

        for (int length = 1; ; length <<= 1) {
            final int currentLength = length;
            final int[] currentRank = rank;

            Comparator<Integer> byPair = (a, b) -> {
                if (currentRank[a] != currentRank[b]) {
                    return Integer.compare(currentRank[a], currentRank[b]);
                }
                int secondA = (a + currentLength < n) ? currentRank[a + currentLength] : -1;
                int secondB = (b + currentLength < n) ? currentRank[b + currentLength] : -1;
                return Integer.compare(secondA, secondB);
            };

            Arrays.sort(order, byPair);

            // Give equal pairs the same new rank, and increase the rank otherwise.
            nextRank[order[0]] = 0;
            for (int i = 1; i < n; i++) {
                boolean differs = byPair.compare(order[i - 1], order[i]) < 0;
                nextRank[order[i]] = nextRank[order[i - 1]] + (differs ? 1 : 0);
            }

            int[] swap = rank;
            rank = nextRank;
            nextRank = swap;

            boolean allRanksUnique = rank[order[n - 1]] == n - 1;
            if (allRanksUnique) {
                break;
            }
        }

        int[] suffixArray = new int[n];
        for (int i = 0; i < n; i++) {
            suffixArray[i] = order[i];
        }
        return suffixArray;
    }

    // ------------------------------------------------------------------
    // Pattern search
    // ------------------------------------------------------------------

    /**
     * Finds the block of suffix-array ranks whose suffixes start with {@code pattern}.
     *
     * @return {@code {low, high}} where ranks low..high-1 match (empty block when low == high)
     */
    public static int[] findRange(String text, int[] suffixArray, String pattern) {
        if (pattern.isEmpty()) {
            return new int[] {0, 0};
        }
        int low = firstRankNotBefore(text, suffixArray, pattern, false);
        int high = firstRankNotBefore(text, suffixArray, pattern, true);
        return new int[] {low, high};
    }

    /**
     * Returns the start position of every occurrence of {@code pattern}, sorted ascending.
     */
    public static List<Integer> search(String text, int[] suffixArray, String pattern) {
        int[] range = findRange(text, suffixArray, pattern);

        List<Integer> positions = new ArrayList<>();
        for (int rank = range[0]; rank < range[1]; rank++) {
            positions.add(suffixArray[rank]);
        }
        Collections.sort(positions);
        return positions;
    }

    /**
     * Binary search over ranks.
     *
     * @param strictlyAfter false: first rank whose suffix is >= pattern (block start);
     *                      true: first rank whose suffix is > every string starting with pattern (block end)
     */
    private static int firstRankNotBefore(String text, int[] suffixArray, String pattern, boolean strictlyAfter) {
        int low = 0;
        int high = suffixArray.length;

        while (low < high) {
            int middle = (low + high) >>> 1;
            int comparison = comparePrefix(text, suffixArray[middle], pattern);

            boolean goRight = strictlyAfter ? (comparison <= 0) : (comparison < 0);
            if (goRight) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
    }

    /**
     * Compares the first pattern.length() characters of the suffix that starts at
     * {@code start} with the pattern.
     *
     * @return negative if the suffix sorts before the pattern, 0 if it starts with the
     *         pattern, positive if it sorts after
     */
    private static int comparePrefix(String text, int start, String pattern) {
        int length = pattern.length();
        for (int i = 0; i < length; i++) {
            if (start + i >= text.length()) {
                return -1;   // suffix is shorter than the pattern, so it sorts first
            }
            char suffixChar = text.charAt(start + i);
            char patternChar = pattern.charAt(i);
            if (suffixChar != patternChar) {
                return suffixChar < patternChar ? -1 : 1;
            }
        }
        return 0;
    }
}
