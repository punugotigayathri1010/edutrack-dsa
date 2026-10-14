package networkflow;

import java.util.List;

/**
 * The residual graph used by Ford-Fulkerson and Edmonds-Karp.
 *
 * <p>residual(u, v) says how much more flow can still be pushed from u to v.
 * Pushing flow along u -> v lowers residual(u, v) and raises residual(v, u), which
 * lets later paths "undo" earlier decisions.</p>
 */
class ResidualGraph {

    private final FlowNetwork network;
    private final int[][] residual;

    ResidualGraph(FlowNetwork network) {
        this.network = network;
        int n = network.size();
        this.residual = new int[n][n];
        for (int from = 0; from < n; from++) {
            for (int to = 0; to < n; to++) {
                residual[from][to] = network.capacity(from, to);
            }
        }
    }

    int size() {
        return residual.length;
    }

    int residual(int from, int to) {
        return residual[from][to];
    }

    /** The smallest residual capacity on a path = how much flow it can carry. */
    int bottleneck(List<Integer> path) {
        int smallest = Integer.MAX_VALUE;
        for (int i = 0; i + 1 < path.size(); i++) {
            smallest = Math.min(smallest, residual[path.get(i)][path.get(i + 1)]);
        }
        return smallest;
    }

    /** Pushes {@code amount} units of flow along the path. */
    void augment(List<Integer> path, int amount) {
        for (int i = 0; i + 1 < path.size(); i++) {
            int from = path.get(i);
            int to = path.get(i + 1);
            residual[from][to] -= amount;
            residual[to][from] += amount;
        }
    }

    /**
     * Converts the final residual graph into flow per edge:
     * flow(u, v) = capacity(u, v) - residual(u, v) for every original edge.
     */
    int[][] extractFlow() {
        int n = size();
        int[][] flow = new int[n][n];
        for (int from = 0; from < n; from++) {
            for (int to = 0; to < n; to++) {
                if (network.capacity(from, to) > 0) {
                    flow[from][to] = Math.max(0, network.capacity(from, to) - residual[from][to]);
                }
            }
        }
        return flow;
    }
}
