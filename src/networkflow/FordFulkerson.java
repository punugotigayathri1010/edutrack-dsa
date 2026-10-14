package networkflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import networkflow.FlowResult.AugmentingStep;

/**
 * Ford-Fulkerson maximum flow using DEPTH-first search to find augmenting paths.
 *
 * <p>Repeat: find any path from source to sink that still has spare capacity, push the
 * bottleneck amount along it, until no such path exists. The flow found is then maximum.
 * With a DFS the number of paths can be large for big capacities, but for the small
 * unit-capacity networks in EduTrack it is fast.</p>
 */
public class FordFulkerson {

    private FordFulkerson() {
    }

    /** Runs the algorithm and records every augmenting path. */
    public static FlowResult solve(FlowNetwork network, int source, int sink) {
        long startTime = System.nanoTime();

        ResidualGraph graph = new ResidualGraph(network);
        List<AugmentingStep> steps = new ArrayList<>();
        int totalFlow = 0;

        while (true) {
            List<Integer> path = findPathWithDfs(graph, source, sink);
            if (path == null) {
                break;   // no augmenting path left => flow is maximum
            }
            int amount = graph.bottleneck(path);
            graph.augment(path, amount);
            totalFlow += amount;
            steps.add(new AugmentingStep(path, amount, totalFlow, 0));
        }

        long elapsedMicros = (System.nanoTime() - startTime) / 1000;
        return new FlowResult("Ford-Fulkerson (DFS)", totalFlow, graph.extractFlow(), steps, 0, elapsedMicros);
    }

    /** Convenience method that only returns the flow value of a capacity matrix. */
    public static int maxFlow(int[][] capacity, int source, int sink) {
        return solve(FlowNetwork.fromMatrix(capacity), source, sink).getMaxFlow();
    }

    /** Returns a source-to-sink path with spare capacity, or null if none exists. */
    private static List<Integer> findPathWithDfs(ResidualGraph graph, int source, int sink) {
        int[] parent = new int[graph.size()];
        boolean[] visited = new boolean[graph.size()];
        parent[source] = -1;

        if (!dfs(graph, source, sink, visited, parent)) {
            return null;
        }

        List<Integer> path = new ArrayList<>();
        for (int node = sink; node != -1; node = parent[node]) {
            path.add(node);
        }
        Collections.reverse(path);
        return path;
    }

    private static boolean dfs(ResidualGraph graph, int node, int sink, boolean[] visited, int[] parent) {
        visited[node] = true;
        if (node == sink) {
            return true;
        }
        for (int next = 0; next < graph.size(); next++) {
            if (!visited[next] && graph.residual(node, next) > 0) {
                parent[next] = node;
                if (dfs(graph, next, sink, visited, parent)) {
                    return true;
                }
            }
        }
        return false;
    }
}
