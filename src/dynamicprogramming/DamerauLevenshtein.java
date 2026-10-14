package dynamicprogramming;

/**
 * Damerau-Levenshtein distance: like Levenshtein, but swapping two neighbouring
 * characters ("teh" -> "the") counts as ONE edit. This matches typical typing mistakes.
 * Comparison ignores upper/lower case.
 *
 * <p>Time and space: O(n * m).</p>
 */
public class DamerauLevenshtein {

    private DamerauLevenshtein() {
    }

    public static int distance(String a, String b) {
        int n = a.length();
        int m = b.length();
        int[][] table = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) {
            table[i][0] = i;
        }
        for (int j = 0; j <= m; j++) {
            table[0][j] = j;
        }

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                char currentA = Character.toLowerCase(a.charAt(i - 1));
                char currentB = Character.toLowerCase(b.charAt(j - 1));
                int cost = (currentA == currentB) ? 0 : 1;

                table[i][j] = Math.min(
                        Math.min(table[i - 1][j] + 1, table[i][j - 1] + 1),
                        table[i - 1][j - 1] + cost);

                // Transposition of two adjacent characters.
                if (i > 1 && j > 1) {
                    char previousA = Character.toLowerCase(a.charAt(i - 2));
                    char previousB = Character.toLowerCase(b.charAt(j - 2));
                    if (currentA == previousB && previousA == currentB) {
                        table[i][j] = Math.min(table[i][j], table[i - 2][j - 2] + 1);
                    }
                }
            }
        }
        return table[n][m];
    }
}
