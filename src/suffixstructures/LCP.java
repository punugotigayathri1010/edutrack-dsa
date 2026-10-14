package suffixstructures;

/**
 * Longest-common-prefix (LCP) array of a suffix array.
 *
 * <p>{@code lcp[i]} is the number of characters shared by the sorted suffixes at ranks
 * i and i+1. Large values point to repeated text, which is what the document
 * similarity features are built on. The array is computed with {@link Kasai}.</p>
 */
public class LCP {

    private LCP() {
    }

    /** Builds the LCP array (length n - 1) for {@code text} and its suffix array. */
    public static int[] build(String text, int[] suffixArray) {
        return Kasai.build(text, suffixArray);
    }

    /** Returns the largest value in the LCP array, or 0 when it is empty. */
    public static int maximum(int[] lcp) {
        int best = 0;
        for (int value : lcp) {
            best = Math.max(best, value);
        }
        return best;
    }

    /** Returns the sum of all LCP values. */
    public static long sum(int[] lcp) {
        long total = 0;
        for (int value : lcp) {
            total += value;
        }
        return total;
    }
}
