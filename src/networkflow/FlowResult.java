package networkflow;

import java.util.Collections;
import java.util.List;

/**
 * Everything a maximum-flow algorithm reports: the flow value, the flow on each edge,
 * every augmenting path it used, and some simple statistics.
 */
public class FlowResult {

    /** One augmenting path: the nodes on it, how much flow it carried and the running total. */
    public static class AugmentingStep {
        public final List<Integer> path;
        public final int amount;
        public final int totalFlowAfter;
        /** Dinic phase (level graph) number; 0 for algorithms without phases. */
        public final int phase;

        public AugmentingStep(List<Integer> path, int amount, int totalFlowAfter, int phase) {
            this.path = path;
            this.amount = amount;
            this.totalFlowAfter = totalFlowAfter;
            this.phase = phase;
        }
    }

    private final String algorithmName;
    private final int maxFlow;
    private final int[][] flow;
    private final List<AugmentingStep> steps;
    private final int phases;
    private final long elapsedMicros;

    public FlowResult(String algorithmName, int maxFlow, int[][] flow,
                      List<AugmentingStep> steps, int phases, long elapsedMicros) {
        this.algorithmName = algorithmName;
        this.maxFlow = maxFlow;
        this.flow = flow;
        this.steps = Collections.unmodifiableList(steps);
        this.phases = phases;
        this.elapsedMicros = elapsedMicros;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public int getMaxFlow() {
        return maxFlow;
    }

    /** Flow sent along the edge from -> to (0 when the edge carries nothing). */
    public int flowOn(int from, int to) {
        return flow[from][to];
    }

    public List<AugmentingStep> getSteps() {
        return steps;
    }

    public int getAugmentationCount() {
        return steps.size();
    }

    /** Number of Dinic phases; 0 for Ford-Fulkerson and Edmonds-Karp. */
    public int getPhases() {
        return phases;
    }

    public long getElapsedMicros() {
        return elapsedMicros;
    }
}
