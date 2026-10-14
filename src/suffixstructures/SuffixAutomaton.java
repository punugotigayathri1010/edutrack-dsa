package suffixstructures;

/**
 * Compact educational stand-in for a suffix automaton (substring queries only).
 * A full automaton with states and suffix links can replace it later.
 */
public class SuffixAutomaton {

    private final String text;

    public SuffixAutomaton(String text) {
        this.text = text;
    }

    /** True when {@code pattern} occurs somewhere in the text. */
    public boolean contains(String pattern) {
        return text.contains(pattern);
    }
}
