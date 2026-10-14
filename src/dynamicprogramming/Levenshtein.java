package dynamicprogramming;

/**
 * Levenshtein (edit) distance: the minimum number of single-character
 * insertions, deletions and substitutions needed to turn one string into another.
 * Comparison ignores upper/lower case. Used to suggest corrections for mistyped searches.
 *
 * <p>Time and space: O(n * m).</p>
 */
public class Levenshtein {

    private Levenshtein() {
    }

    public static int distance(String a, String b) {
        int n = a.length();
        int m = b.length();

        // table[i][j] = distance between the first i chars of a and the first j chars of b
        int[][] table = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) {
            table[i][0] = i;              // delete all i characters
        }
        for (int j = 0; j <= m; j++) {
            table[0][j] = j;              // insert all j characters
        }

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                boolean same = Character.toLowerCase(a.charAt(i - 1)) == Character.toLowerCase(b.charAt(j - 1));
                int substitution = table[i - 1][j - 1] + (same ? 0 : 1);
                int deletion = table[i - 1][j] + 1;
                int insertion = table[i][j - 1] + 1;
                table[i][j] = Math.min(substitution, Math.min(deletion, insertion));
            }
        }
        return table[n][m];
    }
}
