package networkflow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import networkflow.FlowResult.AugmentingStep;

/**
 * Edmonds-Karp maximum flow: Ford-Fulkerson where every augmenting path is the
 * SHORTEST one (found with breadth-first search).
 *
 * <p>Choosing shortest paths guarantees at most O(V * E) augmentations, no matter what the
 * capacities are.</p>
 */
public class EdmondsKarp {

    private EdmondsKarp() {
    }

    /** Runs the algorithm and records every augmenting path. */
    public static FlowResult solve(FlowNetwork network, int source, int sink) {
        long startTime = System.nanoTime();

        ResidualGraph graph = new ResidualGraph(network);
        List<AugmentingStep> steps = new ArrayList<>();
        int totalFlow = 0;

        while (true) {
            List<Integer> path = findShortestPath(graph, source, sink);
            if (path == null) {
                break;
            }
            int amount = graph.bottleneck(path);
            graph.augment(path, amount);
            totalFlow += amount;
            steps.add(new AugmentingStep(path, amount, totalFlow, 0));
        }

        long elapsedMicros = (System.nanoTime() - startTime) / 1000;
        return new FlowResult("Edmonds-Karp (BFS)", totalFlow, graph.extractFlow(), steps, 0, elapsedMicros);
    }

    /** Convenience method that only returns the flow value of a capacity matrix. */
    public static int maxFlow(int[][] capacity, int source, int sink) {
        return solve(FlowNetwork.fromMatrix(capacity), source, sink).getMaxFlow();
    }

    /** BFS over edges with spare capacity; returns the shortest path or null. */
    private static List<Integer> findShortestPath(ResidualGraph graph, int source, int sink) {
        int[] parent = new int[graph.size()];
        Arrays.fill(parent, -1);
        parent[source] = source;

        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(source);

        while (!queue.isEmpty() && parent[sink] == -1) {
            int node = queue.remove();
            for (int next = 0; next < graph.size(); next++) {
                if (parent[next] == -1 && graph.residual(node, next) > 0) {
                    parent[next] = node;
                    queue.add(next);
                }
            }
        }

        if (parent[sink] == -1) {
            return null;
        }

        List<Integer> path = new ArrayList<>();
        for (int node = sink; node != source; node = parent[node]) {
            path.add(node);
        }
        path.add(source);
        Collections.reverse(path);
        return path;
    }
}
