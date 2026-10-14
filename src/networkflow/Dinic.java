package networkflow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import networkflow.FlowResult.AugmentingStep;

/**
 * Dinic's maximum-flow algorithm.
 *
 * <p>Work happens in PHASES. Each phase runs a BFS that assigns every node a level
 * (its distance from the source) and then repeatedly finds paths that only go from a
 * level to the next one (a "blocking flow"). Phases continue until the sink can no
 * longer be reached. It is the fastest of the three algorithms for large networks:
 * O(V^2 * E) in general, and much faster on unit-capacity networks.</p>
 */
public class Dinic {

    /** One directed edge together with a link to its reverse edge. */
    private static class Edge {
        final int from;
        final int to;
        final int reverseIndex;      // index of the reverse edge inside adjacency.get(to)
        int remaining;               // capacity still available
        final int originalCapacity;  // 0 for reverse edges

        Edge(int from, int to, int reverseIndex, int capacity) {
            this.from = from;
            this.to = to;
            this.reverseIndex = reverseIndex;
            this.remaining = capacity;
            this.originalCapacity = capacity;
        }
    }

    private final List<List<Edge>> adjacency = new ArrayList<>();
    private final int[] level;              // BFS level of each node, -1 = not reached
    private final int[] nextEdgeToTry;      // per-node pointer so dead-end edges are skipped
    private final int nodeCount;
    private int phaseCount = 0;             // number of level graphs built so far

    public Dinic(int nodeCount) {
        this.nodeCount = nodeCount;
        this.level = new int[nodeCount];
        this.nextEdgeToTry = new int[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            adjacency.add(new ArrayList<Edge>());
        }
    }

    /** Adds a directed edge u -> v with the given capacity (and its zero-capacity reverse). */
    public void addEdge(int u, int v, int capacity) {
        Edge forward = new Edge(u, v, adjacency.get(v).size(), capacity);
        Edge backward = new Edge(v, u, adjacency.get(u).size(), 0);
        adjacency.get(u).add(forward);
        adjacency.get(v).add(backward);
    }

    /** Runs the algorithm on a {@link FlowNetwork} and records paths and phases. */
    public static FlowResult solve(FlowNetwork network, int source, int sink) {
        long startTime = System.nanoTime();

        Dinic dinic = new Dinic(network.size());
        for (int[] edge : network.edges()) {
            dinic.addEdge(edge[0], edge[1], edge[2]);
        }

        List<AugmentingStep> steps = new ArrayList<>();
        int totalFlow = dinic.run(source, sink, steps);
        int[][] flow = dinic.extractFlow();

        long elapsedMicros = (System.nanoTime() - startTime) / 1000;
        return new FlowResult("Dinic (level graph)", totalFlow, flow, steps, dinic.phaseCount, elapsedMicros);
    }

    /** Returns only the maximum flow value. */
    public int maxFlow(int source, int sink) {
        return run(source, sink, new ArrayList<AugmentingStep>());
    }

    private int run(int source, int sink, List<AugmentingStep> steps) {
        int totalFlow = 0;

        while (buildLevelGraph(source, sink)) {
            phaseCount++;
            Arrays.fill(nextEdgeToTry, 0);

            while (true) {
                List<Edge> pathEdges = new ArrayList<>();
                if (!findBlockingPath(source, sink, pathEdges)) {
                    break;   // this phase is finished
                }

                int amount = Integer.MAX_VALUE;
                for (Edge edge : pathEdges) {
                    amount = Math.min(amount, edge.remaining);
                }
                for (Edge edge : pathEdges) {
                    edge.remaining -= amount;
                    adjacency.get(edge.to).get(edge.reverseIndex).remaining += amount;
                }

                totalFlow += amount;
                steps.add(new AugmentingStep(nodesOnPath(pathEdges), amount, totalFlow, phaseCount));
            }
        }
        return totalFlow;
    }

    /** BFS from the source; returns true if the sink is reachable through edges with spare capacity. */
    private boolean buildLevelGraph(int source, int sink) {
        Arrays.fill(level, -1);
        level[source] = 0;

        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            int node = queue.remove();
            for (Edge edge : adjacency.get(node)) {
                if (edge.remaining > 0 && level[edge.to] < 0) {
                    level[edge.to] = level[node] + 1;
                    queue.add(edge.to);
                }
            }
        }
        return level[sink] >= 0;
    }

    /** DFS that only follows edges to the next level; fills {@code path} when it reaches the sink. */
    private boolean findBlockingPath(int node, int sink, List<Edge> path) {
        if (node == sink) {
            return true;
        }
        List<Edge> edges = adjacency.get(node);
        for (; nextEdgeToTry[node] < edges.size(); nextEdgeToTry[node]++) {
            Edge edge = edges.get(nextEdgeToTry[node]);
            if (edge.remaining > 0 && level[edge.to] == level[node] + 1) {
                path.add(edge);
                if (findBlockingPath(edge.to, sink, path)) {
                    return true;
                }
                path.remove(path.size() - 1);   // dead end: undo and try the next edge
            }
        }
        return false;
    }

    private List<Integer> nodesOnPath(List<Edge> pathEdges) {
        List<Integer> nodes = new ArrayList<>();
        nodes.add(pathEdges.get(0).from);
        for (Edge edge : pathEdges) {
            nodes.add(edge.to);
        }
        return nodes;
    }

    /** flow(u, v) = originalCapacity - remaining, for every real (non-reverse) edge. */
    private int[][] extractFlow() {
        int[][] flow = new int[nodeCount][nodeCount];
        for (List<Edge> edges : adjacency) {
            for (Edge edge : edges) {
                if (edge.originalCapacity > 0) {
                    flow[edge.from][edge.to] += edge.originalCapacity - edge.remaining;
                }
            }
        }
        return flow;
    }
}
