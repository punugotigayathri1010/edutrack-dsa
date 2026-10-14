package dynamicprogramming;

/**
 * Optimal binary search tree: given how often each (sorted) key is searched,
 * computes the lowest possible total search cost of a BST built from those keys.
 * Frequently used keys end up near the root.
 *
 * <p>Time: O(n^3), space: O(n^2).</p>
 */
public class OptimalBST {

    private OptimalBST() {
    }

    /** @param frequency access counts of the keys in sorted key order */
    public static int minCost(int[] frequency) {
        int n = frequency.length;
        if (n == 0) {
            return 0;
        }

        // prefixSum[i] = frequency[0] + ... + frequency[i-1], for fast range sums
        int[] prefixSum = new int[n + 1];
        for (int i = 0; i < n; i++) {
            prefixSum[i + 1] = prefixSum[i] + frequency[i];
        }

        // cost[i][j] = best cost of a BST that contains keys i..j
        int[][] cost = new int[n][n];

        for (int length = 1; length <= n; length++) {
            for (int i = 0; i + length - 1 < n; i++) {
                int j = i + length - 1;
                int rangeSum = prefixSum[j + 1] - prefixSum[i];
                cost[i][j] = Integer.MAX_VALUE;

                // Try every key r as the root of the subtree that holds keys i..j.
                for (int root = i; root <= j; root++) {
                    int leftCost = (root > i) ? cost[i][root - 1] : 0;
                    int rightCost = (root < j) ? cost[root + 1][j] : 0;
                    cost[i][j] = Math.min(cost[i][j], leftCost + rightCost + rangeSum);
                }
            }
        }
        return cost[0][n - 1];
    }
}
