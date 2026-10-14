package networkflow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

/**
 * Maximum bipartite matching (for example: faculty on the left, courses on the right,
 * one course per faculty) using augmenting paths, plus a minimum vertex cover
 * computed with Konig's theorem.
 *
 * <p>Konig's theorem: in a bipartite graph the size of a maximum matching equals the
 * size of a minimum vertex cover. The cover is the smallest set of faculty/courses that
 * touches every allowed faculty-course pair - it shows where the conflicts are concentrated.</p>
 */
public class BipartiteMatching {

    /** A vertex cover, split into the left-side and the right-side vertices it contains. */
    public static class VertexCover {
        public final List<Integer> left;
        public final List<Integer> right;

        VertexCover(List<Integer> left, List<Integer> right) {
            this.left = left;
            this.right = right;
        }

        public int size() {
            return left.size() + right.size();
        }
    }

    private final List<List<Integer>> adjacency = new ArrayList<>();
    private int[] matchOfRight = new int[0];   // matchOfRight[v] = left vertex matched to v, or -1
    private int[] matchOfLeft = new int[0];    // matchOfLeft[u]  = right vertex matched to u, or -1

    public BipartiteMatching(int leftCount) {
        for (int i = 0; i < leftCount; i++) {
            adjacency.add(new ArrayList<Integer>());
        }
        matchOfLeft = new int[leftCount];
        Arrays.fill(matchOfLeft, -1);
    }

    /** Allows the pair (left vertex u, right vertex v). */
    public void addEdge(int u, int v) {
        adjacency.get(u).add(v);
    }

    /**
     * Computes a maximum matching.
     *
     * @param rightCount number of vertices on the right side
     * @return the size of the matching
     */
    public int maxMatching(int rightCount) {
        matchOfRight = new int[rightCount];
        Arrays.fill(matchOfRight, -1);
        Arrays.fill(matchOfLeft, -1);

        int matched = 0;
        for (int u = 0; u < adjacency.size(); u++) {
            boolean[] visited = new boolean[rightCount];
            if (tryToMatch(u, visited)) {
                matched++;
            }
        }
        return matched;
    }

    /** Tries to find an augmenting path that starts at the free left vertex u. */
    private boolean tryToMatch(int u, boolean[] visited) {
        for (int v : adjacency.get(u)) {
            if (visited[v]) {
                continue;
            }
            visited[v] = true;

            boolean rightIsFree = matchOfRight[v] == -1;
            if (rightIsFree || tryToMatch(matchOfRight[v], visited)) {
                matchOfRight[v] = u;
                matchOfLeft[u] = v;
                return true;
            }
        }
        return false;
    }

    /** Right vertex matched to left vertex u after {@link #maxMatching}, or -1. */
    public int getMatchOfLeft(int u) {
        return matchOfLeft[u];
    }

    /** Left vertex matched to right vertex v after {@link #maxMatching}, or -1. */
    public int getMatchOfRight(int v) {
        return matchOfRight[v];
    }

    /**
     * Builds a minimum vertex cover from the current maximum matching (Konig's construction):
     * start from unmatched left vertices, walk along non-matching edges to the right and back
     * along matching edges to the left. Cover = (left vertices NOT reached) + (right vertices reached).
     */
    public VertexCover minimumVertexCover() {
        boolean[] reachedLeft = new boolean[adjacency.size()];
        boolean[] reachedRight = new boolean[matchOfRight.length];
        Queue<Integer> queue = new ArrayDeque<>();

        for (int u = 0; u < adjacency.size(); u++) {
            if (matchOfLeft[u] == -1) {
                reachedLeft[u] = true;
                queue.add(u);
            }
        }

        while (!queue.isEmpty()) {
            int u = queue.remove();
            for (int v : adjacency.get(u)) {
                if (reachedRight[v]) {
                    continue;
                }
                reachedRight[v] = true;
                int partner = matchOfRight[v];
                if (partner != -1 && !reachedLeft[partner]) {
                    reachedLeft[partner] = true;
                    queue.add(partner);
                }
            }
        }

        List<Integer> left = new ArrayList<>();
        for (int u = 0; u < reachedLeft.length; u++) {
            if (!reachedLeft[u]) {
                left.add(u);
            }
        }
        List<Integer> right = new ArrayList<>();
        for (int v = 0; v < reachedRight.length; v++) {
            if (reachedRight[v]) {
                right.add(v);
            }
        }
        return new VertexCover(left, right);
    }
}
