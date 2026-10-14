package suffixstructures;

/**
 * SA-IS entry point.
 *
 * <p>Educational placeholder: it currently returns the result of the (correct)
 * prefix-doubling builder in {@link SuffixArray}. A full linear-time SA-IS
 * implementation can replace the body of {@link #build} later without changing any caller.</p>
 */
public class SAIS {

    private SAIS() {
    }

    public static int[] build(String text) {
        return SuffixArray.build(text);
    }
}
