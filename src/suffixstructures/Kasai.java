package suffixstructures;

/**
 * Kasai's algorithm: builds the LCP array from a text and its suffix array in O(n).
 *
 * <p>Convention used in this project: {@code lcp[i]} is the length of the longest
 * common prefix of the suffixes at suffix-array ranks i and i+1, so the array has
 * length n - 1.</p>
 *
 * <p>Trick: if suffix i shares h characters with its neighbour, then suffix i+1
 * shares at least h-1 characters with its own neighbour. So h only ever decreases
 * by one per step, which gives the linear running time.</p>
 */
public class Kasai {

    private Kasai() {
    }

    public static int[] build(String text, int[] suffixArray) {
        int n = text.length();
        int[] lcp = new int[Math.max(0, n - 1)];
        if (n <= 1) {
            return lcp;
        }

        // rank[p] = position of suffix p inside the suffix array
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[suffixArray[i]] = i;
        }

        int matched = 0;   // characters shared with the next suffix in sorted order
        for (int position = 0; position < n; position++) {
            int currentRank = rank[position];

            if (currentRank == n - 1) {
                matched = 0;   // last suffix has no "next" neighbour
                continue;
            }

            int neighbour = suffixArray[currentRank + 1];
            while (position + matched < n
                    && neighbour + matched < n
                    && text.charAt(position + matched) == text.charAt(neighbour + matched)) {
                matched++;
            }

            lcp[currentRank] = matched;
            if (matched > 0) {
                matched--;
            }
        }
        return lcp;
    }
}
