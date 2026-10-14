package networkflow;

import java.util.ArrayList;
import java.util.List;

/**
 * A directed flow network stored as a capacity matrix, with a printable name for every node.
 *
 * <p>Example: node 0 = "SOURCE", node 1 = "F101", ... Adding the same edge twice
 * adds the capacities together.</p>
 */
public class FlowNetwork {

    private final String[] names;
    private final int[][] capacity;

    public FlowNetwork(String... nodeNames) {
        this.names = nodeNames.clone();
        this.capacity = new int[nodeNames.length][nodeNames.length];
    }

    /** Creates a network whose nodes are simply called "0", "1", "2" ... from a capacity matrix. */
    public static FlowNetwork fromMatrix(int[][] matrix) {
        String[] nodeNames = new String[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            nodeNames[i] = String.valueOf(i);
        }
        FlowNetwork network = new FlowNetwork(nodeNames);
        for (int from = 0; from < matrix.length; from++) {
            for (int to = 0; to < matrix.length; to++) {
                network.capacity[from][to] = matrix[from][to];
            }
        }
        return network;
    }

    public void addEdge(int from, int to, int edgeCapacity) {
        capacity[from][to] += edgeCapacity;
    }

    public int size() {
        return names.length;
    }

    public String name(int node) {
        return names[node];
    }

    public int capacity(int from, int to) {
        return capacity[from][to];
    }

    /** Lists every edge with positive capacity as {from, to, capacity}. */
    public List<int[]> edges() {
        List<int[]> edges = new ArrayList<>();
        for (int from = 0; from < size(); from++) {
            for (int to = 0; to < size(); to++) {
                if (capacity[from][to] > 0) {
                    edges.add(new int[] {from, to, capacity[from][to]});
                }
            }
        }
        return edges;
    }

    /** Formats a node path as "SOURCE -> F101 -> CS301 -> SINK". */
    public String describePath(List<Integer> path) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) {
                text.append(" -> ");
            }
            text.append(names[path.get(i)]);
        }
        return text.toString();
    }
}
