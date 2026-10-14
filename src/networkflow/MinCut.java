package networkflow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Minimum cut derived from a finished maximum flow (max-flow min-cut theorem).
 *
 * <p>After the flow is maximum, the nodes that can still be reached from the source
 * through edges with spare capacity form the SOURCE side of the cut. The original
 * edges that leave this side are saturated, and their total capacity equals the
 * maximum flow. They are the "bottleneck" of the network.</p>
 */
public class MinCut {

    private final Set<Integer> sourceSide;
    private final List<int[]> cutEdges;   // each entry is {from, to, capacity}
    private final int capacity;

    private MinCut(Set<Integer> sourceSide, List<int[]> cutEdges, int capacity) {
        this.sourceSide = sourceSide;
        this.cutEdges = cutEdges;
        this.capacity = capacity;
    }

    /** Computes the minimum cut of {@code network} for a finished flow {@code result}. */
    public static MinCut compute(FlowNetwork network, FlowResult result, int source) {
        int n = network.size();

        // 1. Find everything still reachable from the source in the residual graph.
        Set<Integer> reachable = new LinkedHashSet<>();
        Queue<Integer> queue = new ArrayDeque<>();
        reachable.add(source);
        queue.add(source);

        while (!queue.isEmpty()) {
            int node = queue.remove();
            for (int next = 0; next < n; next++) {
                // Spare capacity forward, plus flow that could be sent back along a reverse edge.
                int residual = network.capacity(node, next) - result.flowOn(node, next) + result.flowOn(next, node);
                if (residual > 0 && !reachable.contains(next)) {
                    reachable.add(next);
                    queue.add(next);
                }
            }
        }

        // 2. The cut consists of original edges that go from the reachable side to the other side.
        List<int[]> cut = new ArrayList<>();
        int total = 0;
        for (int[] edge : network.edges()) {
            boolean leavesSourceSide = reachable.contains(edge[0]) && !reachable.contains(edge[1]);
            if (leavesSourceSide) {
                cut.add(edge);
                total += edge[2];
            }
        }
        return new MinCut(reachable, cut, total);
    }

    public Set<Integer> getSourceSide() {
        return sourceSide;
    }

    public List<int[]> getCutEdges() {
        return cutEdges;
    }

    /** Total capacity of the cut edges - always equal to the maximum flow. */
    public int getCapacity() {
        return capacity;
    }
}
