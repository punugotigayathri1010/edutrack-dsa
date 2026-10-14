package stringalgorithms;

/**
 * Z-function: z[i] is the length of the longest substring that starts at i and
 * is also a prefix of the whole string.
 *
 * <p>A large z value means the beginning of the text repeats at position i, which
 * makes the Z-function handy for spotting repeated phrases.</p>
 *
 * <p>Time: O(n).</p>
 */
public class ZFunction {

    private ZFunction() {
    }

    /** Computes the Z-array of {@code s} (z[0] is left as 0). */
    public static int[] zArray(String s) {
        int n = s.length();
        int[] z = new int[n];

        // [left, right] is the right-most segment that matches a prefix of s.
        int left = 0;
        int right = 0;

        for (int i = 1; i < n; i++) {
            // Reuse work from the segment we already know about.
            if (i <= right) {
                z[i] = Math.min(right - i + 1, z[i - left]);
            }

            // Extend the match character by character.
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) {
                z[i]++;
            }

            // Remember the segment that reaches furthest to the right.
            if (i + z[i] - 1 > right) {
                left = i;
                right = i + z[i] - 1;
            }
        }
        return z;
    }
}
