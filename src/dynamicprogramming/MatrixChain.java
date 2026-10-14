package dynamicprogramming;

/**
 * Matrix-chain multiplication: finds the cheapest bracketing for multiplying a
 * chain of matrices. Matrix i has dimensions p[i] x p[i+1].
 *
 * <p>Time: O(n^3), space: O(n^2).</p>
 */
public class MatrixChain {

    private MatrixChain() {
    }

    /**
     * @param p dimension array of length (number of matrices + 1)
     * @return the minimum number of scalar multiplications (0 for fewer than two matrices)
     */
    public static long minMultiplications(int[] p) {
        int matrixCount = p.length - 1;
        if (matrixCount <= 0) {
            return 0;
        }

        // cost[i][j] = cheapest way to multiply matrices i..j
        long[][] cost = new long[matrixCount][matrixCount];

        for (int chainLength = 2; chainLength <= matrixCount; chainLength++) {
            for (int i = 0; i + chainLength - 1 < matrixCount; i++) {
                int j = i + chainLength - 1;
                cost[i][j] = Long.MAX_VALUE;

                // Try every place k where the chain can be split into (i..k) and (k+1..j).
                for (int k = i; k < j; k++) {
                    long splitCost = cost[i][k] + cost[k + 1][j] + (long) p[i] * p[k + 1] * p[j + 1];
                    cost[i][j] = Math.min(cost[i][j], splitCost);
                }
            }
        }
        return cost[0][matrixCount - 1];
    }
}
