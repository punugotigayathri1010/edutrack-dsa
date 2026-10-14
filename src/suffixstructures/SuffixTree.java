package suffixstructures;

/**
 * Compact educational stand-in for a suffix tree.
 *
 * <p>It answers the same question a suffix tree does - "is this string a substring
 * of the text?" - but is backed by a suffix array instead of explicit tree nodes,
 * so the prototype stays small and easy to read.</p>
 */
public class SuffixTree {

    private final String text;
    private final int[] suffixArray;

    public SuffixTree(String text) {
        this.text = text;
        this.suffixArray = SuffixArray.build(text);
    }

    /** True when {@code pattern} occurs somewhere in the text. */
    public boolean contains(String pattern) {
        if (pattern.isEmpty()) {
            return true;   // the empty string is a substring of every text
        }
        int[] range = SuffixArray.findRange(text, suffixArray, pattern);
        return range[0] < range[1];
    }
}
