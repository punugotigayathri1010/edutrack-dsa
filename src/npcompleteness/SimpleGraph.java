package npcompleteness;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A small undirected graph stored as an adjacency matrix.
 *
 * <p>It is shared by the three graph problems of module M5 (Clique, Independent Set and
 * Vertex Cover) and by the exam clash graph, where a vertex is a course and an edge means
 * that at least one student takes both courses.</p>
 */
public class SimpleGraph {

    private final int size;
    private final boolean[][] adjacent;

    public SimpleGraph(int size) {
        this.size = size;
        this.adjacent = new boolean[size][size];
    }

    public int size() {
        return size;
    }

    /** Adds the edge u-v (self-loops are ignored). */
    public void addEdge(int u, int v) {
        if (u == v) {
            return;
        }
        adjacent[u][v] = true;
        adjacent[v][u] = true;
    }

    public boolean hasEdge(int u, int v) {
        return adjacent[u][v];
    }

    /** Returns every edge once, as {u, v} with u < v. */
    public List<int[]> edges() {
        List<int[]> result = new ArrayList<>();
        for (int u = 0; u < size; u++) {
            for (int v = u + 1; v < size; v++) {
                if (adjacent[u][v]) {
                    result.add(new int[] {u, v});
                }
            }
        }
        return result;
    }

    public int edgeCount() {
        return edges().size();
    }

    /** Returns the complement graph: u-v is an edge exactly when it is not an edge here. */
    public SimpleGraph complement() {
        SimpleGraph result = new SimpleGraph(size);
        for (int u = 0; u < size; u++) {
            for (int v = u + 1; v < size; v++) {
                if (!adjacent[u][v]) {
                    result.addEdge(u, v);
                }
            }
        }
        return result;
    }

    /** True when every two different vertices of {@code vertices} are joined by an edge. */
    public boolean isClique(Collection<Integer> vertices) {
        List<Integer> list = new ArrayList<>(vertices);
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (!adjacent[list.get(i)][list.get(j)]) {
                    return false;
                }
            }
        }
        return true;
    }

    /** True when no two vertices of {@code vertices} are joined by an edge. */
    public boolean isIndependentSet(Collection<Integer> vertices) {
        List<Integer> list = new ArrayList<>(vertices);
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (adjacent[list.get(i)][list.get(j)]) {
                    return false;
                }
            }
        }
        return true;
    }

    /** True when every edge has at least one end inside {@code vertices}. */
    public boolean isVertexCover(Collection<Integer> vertices) {
        for (int[] edge : edges()) {
            if (!vertices.contains(edge[0]) && !vertices.contains(edge[1])) {
                return false;
            }
        }
        return true;
    }
}
