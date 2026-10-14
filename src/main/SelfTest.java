package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import networkflow.BipartiteMatching;
import networkflow.Dinic;
import networkflow.EdmondsKarp;
import networkflow.FlowNetwork;
import networkflow.FlowResult;
import networkflow.FordFulkerson;
import networkflow.MinCut;
import npcompleteness.SATSolver;
import npcompleteness.SimpleGraph;
import randomizedparallel.RandomizedQuickSort;
import randomizedparallel.ReservoirSampling;
import services.ExamScheduleService;
import stringalgorithms.KMP;
import suffixstructures.Kasai;
import suffixstructures.SuffixArray;
import suffixstructures.SuffixArrayAnalysis;

/**
 * Self-check for the algorithm packages. Run with:
 *
 * <pre>java -cp bin main.SelfTest</pre>
 *
 * <p>Every check compares an algorithm with a slow but obviously correct method on many random
 * inputs. It prints one PASS/FAIL line per group and ends with the total.</p>
 */
public class SelfTest {

    private static int failures = 0;

    public static void main(String[] args) {
        Random random = new Random(2024);

        checkMaximumFlow(random);
        checkMinCutEqualsFlow(random);
        checkSuffixArray(random);
        checkPatternSearch(random);
        checkDistinctSubstrings(random);
        checkLongestCommonSubstring(random);
        checkKonigTheorem(random);

        // M5 - NP-completeness
        checkSatSolver(random);
        checkExamColouring(random);

        // M6 - randomized and parallel
        checkRandomizedQuickSort(random);
        checkReservoirSampling(random);

        System.out.println();
        System.out.println(failures == 0 ? "ALL CHECKS PASSED" : failures + " CHECK(S) FAILED");
        if (failures > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------
    // Network flow
    // ------------------------------------------------------------------

    /** Ford-Fulkerson, Edmonds-Karp and Dinic must agree, and flow must be valid on every edge. */
    private static void checkMaximumFlow(Random random) {
        boolean ok = true;
        for (int round = 0; round < 300 && ok; round++) {
            FlowNetwork network = randomNetwork(random, 2 + random.nextInt(7));
            int sink = network.size() - 1;

            FlowResult ff = FordFulkerson.solve(network, 0, sink);
            FlowResult ek = EdmondsKarp.solve(network, 0, sink);
            FlowResult dinic = Dinic.solve(network, 0, sink);

            ok = ff.getMaxFlow() == ek.getMaxFlow() && ek.getMaxFlow() == dinic.getMaxFlow();
            ok = ok && isValidFlow(network, ff, 0, sink) && isValidFlow(network, ek, 0, sink)
                    && isValidFlow(network, dinic, 0, sink);
        }
        report("Max flow: Ford-Fulkerson = Edmonds-Karp = Dinic, flows are valid (300 random networks)", ok);
    }

    /** Max-flow min-cut theorem: the cut capacity equals the flow value. */
    private static void checkMinCutEqualsFlow(Random random) {
        boolean ok = true;
        for (int round = 0; round < 300 && ok; round++) {
            FlowNetwork network = randomNetwork(random, 2 + random.nextInt(7));
            FlowResult result = EdmondsKarp.solve(network, 0, network.size() - 1);
            MinCut cut = MinCut.compute(network, result, 0);
            ok = cut.getCapacity() == result.getMaxFlow()
                    && cut.getSourceSide().contains(0)
                    && !cut.getSourceSide().contains(network.size() - 1);
        }
        report("Min cut capacity equals max flow (300 random networks)", ok);
    }

    private static FlowNetwork randomNetwork(Random random, int nodes) {
        String[] names = new String[nodes];
        for (int i = 0; i < nodes; i++) {
            names[i] = "n" + i;
        }
        FlowNetwork network = new FlowNetwork(names);
        for (int from = 0; from < nodes; from++) {
            for (int to = 0; to < nodes; to++) {
                if (from != to && random.nextInt(100) < 45) {
                    network.addEdge(from, to, 1 + random.nextInt(9));
                }
            }
        }
        return network;
    }

    /** Capacity limits respected, and flow in equals flow out at every node except source and sink. */
    private static boolean isValidFlow(FlowNetwork network, FlowResult result, int source, int sink) {
        int n = network.size();
        for (int node = 0; node < n; node++) {
            int in = 0;
            int out = 0;
            for (int other = 0; other < n; other++) {
                if (result.flowOn(other, node) > network.capacity(other, node)) {
                    return false;
                }
                in += result.flowOn(other, node);
                out += result.flowOn(node, other);
            }
            if (node == source && out - in != result.getMaxFlow()) {
                return false;
            }
            if (node == sink && in - out != result.getMaxFlow()) {
                return false;
            }
            if (node != source && node != sink && in != out) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Suffix array
    // ------------------------------------------------------------------

    /** The suffix array must equal the result of sorting all suffixes, and LCP must match a direct count. */
    private static void checkSuffixArray(Random random) {
        boolean ok = true;
        for (int round = 0; round < 400 && ok; round++) {
            String text = randomText(random, random.nextInt(40), 3);
            int[] fast = SuffixArray.build(text);
            int[] slow = naiveSuffixArray(text);
            ok = Arrays.equals(fast, slow);

            int[] lcp = Kasai.build(text, fast);
            for (int i = 0; i + 1 < fast.length && ok; i++) {
                ok = lcp[i] == commonPrefix(text, fast[i], fast[i + 1]);
            }
        }
        report("Suffix array and LCP match brute force (400 random texts)", ok);
    }

    /** Suffix-array search must return the same positions as KMP. */
    private static void checkPatternSearch(Random random) {
        boolean ok = true;
        for (int round = 0; round < 400 && ok; round++) {
            String text = randomText(random, random.nextInt(60), 3);
            String pattern = randomText(random, 1 + random.nextInt(4), 3);
            int[] suffixArray = SuffixArray.build(text);
            ok = SuffixArray.search(text, suffixArray, pattern).equals(KMP.search(text, pattern));
        }
        report("Suffix-array binary search equals KMP (400 random searches)", ok);
    }

    /** Distinct substrings from LCP must equal a brute-force set count. */
    private static void checkDistinctSubstrings(Random random) {
        boolean ok = true;
        for (int round = 0; round < 300 && ok; round++) {
            String text = randomText(random, random.nextInt(25), 3);
            int[] suffixArray = SuffixArray.build(text);
            int[] lcp = Kasai.build(text, suffixArray);

            java.util.Set<String> all = new java.util.HashSet<>();
            for (int i = 0; i < text.length(); i++) {
                for (int j = i + 1; j <= text.length(); j++) {
                    all.add(text.substring(i, j));
                }
            }
            ok = SuffixArrayAnalysis.countDistinctSubstrings(text.length(), lcp) == all.size();
        }
        report("Distinct substring count equals brute force (300 random texts)", ok);
    }

    /** The longest common substring found through one suffix array must equal a brute-force answer. */
    private static void checkLongestCommonSubstring(Random random) {
        boolean ok = true;
        for (int round = 0; round < 300 && ok; round++) {
            String a = randomText(random, random.nextInt(30), 3);
            String b = randomText(random, random.nextInt(30), 3);
            int expected = naiveLongestCommonSubstring(a, b);
            SuffixArrayAnalysis.Comparison result = SuffixArrayAnalysis.compare(a, b, 1);
            ok = result.longestCommon.length() == expected;
            if (ok && expected > 0) {
                ok = a.contains(result.longestCommon) && b.contains(result.longestCommon);
            }
        }
        report("Longest common substring of two documents equals brute force (300 random pairs)", ok);
    }

    // ------------------------------------------------------------------
    // Bipartite matching
    // ------------------------------------------------------------------

    /** Konig: minimum vertex cover size = maximum matching size, and the cover touches every edge. */
    private static void checkKonigTheorem(Random random) {
        boolean ok = true;
        for (int round = 0; round < 300 && ok; round++) {
            int left = 1 + random.nextInt(6);
            int right = 1 + random.nextInt(6);
            BipartiteMatching matching = new BipartiteMatching(left);
            List<int[]> edges = new ArrayList<>();
            for (int u = 0; u < left; u++) {
                for (int v = 0; v < right; v++) {
                    if (random.nextInt(100) < 40) {
                        matching.addEdge(u, v);
                        edges.add(new int[] {u, v});
                    }
                }
            }
            int size = matching.maxMatching(right);
            BipartiteMatching.VertexCover cover = matching.minimumVertexCover();

            ok = cover.size() == size;
            for (int[] edge : edges) {
                ok = ok && (cover.left.contains(edge[0]) || cover.right.contains(edge[1]));
            }
        }
        report("Konig: vertex cover size = matching size and covers all edges (300 random graphs)", ok);
    }

    // ------------------------------------------------------------------
    // M5: NP-completeness
    // ------------------------------------------------------------------

    /** The DPLL solver must agree with trying all 2^n assignments, and its model must be valid. */
    private static void checkSatSolver(Random random) {
        boolean ok = true;
        for (int round = 0; round < 500 && ok; round++) {
            int numVars = 1 + random.nextInt(6);
            int[][] clauses = randomFormula(random, numVars, 1 + random.nextInt(10));

            boolean[] model = new SATSolver(numVars, clauses).solve();
            boolean expected = bruteForceSatisfiable(numVars, clauses);

            ok = (model != null) == expected;
            if (model != null) {
                ok = ok && SATSolver.satisfies(model, clauses);
            }
        }
        report("SAT: DPLL agrees with brute force and models are valid (500 random formulas)", ok);
    }

    /** SAT-based exam colouring must match brute-force colouring, and slots must not clash. */
    private static void checkExamColouring(Random random) {
        boolean ok = true;
        for (int round = 0; round < 300 && ok; round++) {
            int n = 1 + random.nextInt(6);
            int slots = 1 + random.nextInt(3);
            SimpleGraph graph = randomGraph(random, n, 40);

            int[] slotOf = ExamScheduleService.colourWithSat(graph, slots);
            boolean possible = bruteForceColourable(graph, slots);

            ok = (slotOf != null) == possible;
            if (slotOf != null) {
                for (int[] edge : graph.edges()) {
                    ok = ok && slotOf[edge[0]] != slotOf[edge[1]];
                }
                for (int slot : slotOf) {
                    ok = ok && slot >= 0 && slot < slots;
                }
            }
        }
        report("Exam timetable: SAT colouring matches brute force, no clashes (300 random graphs)", ok);
    }

    // ------------------------------------------------------------------
    // M6: randomized and parallel
    // ------------------------------------------------------------------

    /** Randomized QuickSort must give the same result as Arrays.sort, including with duplicates. */
    private static void checkRandomizedQuickSort(Random random) {
        boolean ok = true;
        for (int round = 0; round < 300 && ok; round++) {
            int[] values = new int[random.nextInt(60)];
            int range = 1 + random.nextInt(round % 2 == 0 ? 5 : 1000);
            for (int i = 0; i < values.length; i++) {
                values[i] = random.nextInt(range) - range / 2;
            }
            int[] expected = values.clone();
            Arrays.sort(expected);

            int[] sorted = values.clone();
            RandomizedQuickSort.sort(sorted, random);
            ok = Arrays.equals(sorted, expected);

            List<Integer> list = new ArrayList<>();
            for (int value : values) {
                list.add(value);
            }
            RandomizedQuickSort.sort(list, Collections.<Integer>reverseOrder(), random);
            for (int i = 0; i < expected.length; i++) {
                ok = ok && list.get(i) == expected[expected.length - 1 - i];
            }
        }

        // Already-sorted and reversed input of a large size must also work (the classic bad cases).
        int[] ascending = new int[50000];
        for (int i = 0; i < ascending.length; i++) {
            ascending[i] = i;
        }
        int[] descending = new int[ascending.length];
        for (int i = 0; i < descending.length; i++) {
            descending[i] = descending.length - i;
        }
        RandomizedQuickSort.sort(ascending, random);
        RandomizedQuickSort.sort(descending, random);
        for (int i = 1; i < ascending.length; i++) {
            ok = ok && ascending[i - 1] <= ascending[i] && descending[i - 1] <= descending[i];
        }
        report("Randomized QuickSort = Arrays.sort, also on sorted/reversed/duplicate input", ok);
    }

    /** Samples have the right size and come from the stream; every item is picked about equally often. */
    private static void checkReservoirSampling(Random random) {
        boolean ok = true;
        for (int round = 0; round < 200 && ok; round++) {
            int n = random.nextInt(30);
            int k = 1 + random.nextInt(8);
            List<Integer> stream = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                stream.add(i);
            }
            List<Integer> sample = ReservoirSampling.sample(stream, k, random);
            ok = sample.size() == Math.min(k, n) && stream.containsAll(sample)
                    && new java.util.HashSet<>(sample).size() == sample.size();
        }

        // Fairness: 10 items, sample 3, 30000 trials -> each item expected 9000 times.
        int items = 10;
        int sampleSize = 3;
        int trials = 30000;
        int[] picked = new int[items];
        List<Integer> stream = new ArrayList<>();
        for (int i = 0; i < items; i++) {
            stream.add(i);
        }
        for (int t = 0; t < trials; t++) {
            for (int chosen : ReservoirSampling.sample(stream, sampleSize, random)) {
                picked[chosen]++;
            }
        }
        double expected = (double) trials * sampleSize / items;
        for (int count : picked) {
            ok = ok && Math.abs(count - expected) < 0.06 * expected;
        }
        report("Reservoir Sampling: right size, no repeats, every item equally likely", ok);
    }

    // ------------------------------------------------------------------
    // Slow reference implementations for M5 / M6
    // ------------------------------------------------------------------

    /** Random clauses of 1-3 literals over variables 1..numVars. */
    private static int[][] randomFormula(Random random, int numVars, int clauseCount) {
        int[][] clauses = new int[clauseCount][];
        for (int c = 0; c < clauseCount; c++) {
            int[] clause = new int[1 + random.nextInt(3)];
            for (int i = 0; i < clause.length; i++) {
                int variable = 1 + random.nextInt(numVars);
                clause[i] = random.nextBoolean() ? variable : -variable;
            }
            clauses[c] = clause;
        }
        return clauses;
    }

    private static boolean bruteForceSatisfiable(int numVars, int[][] clauses) {
        for (int mask = 0; mask < (1 << numVars); mask++) {
            boolean[] model = new boolean[numVars + 1];
            for (int v = 1; v <= numVars; v++) {
                model[v] = (mask >> (v - 1) & 1) == 1;
            }
            if (SATSolver.satisfies(model, clauses)) {
                return true;
            }
        }
        return false;
    }

    private static SimpleGraph randomGraph(Random random, int vertices, int edgePercent) {
        SimpleGraph graph = new SimpleGraph(vertices);
        for (int u = 0; u < vertices; u++) {
            for (int v = u + 1; v < vertices; v++) {
                if (random.nextInt(100) < edgePercent) {
                    graph.addEdge(u, v);
                }
            }
        }
        return graph;
    }

    private static boolean bruteForceColourable(SimpleGraph graph, int slots) {
        int n = graph.size();
        int[] slotOf = new int[n];
        return colourFrom(graph, slots, slotOf, 0);
    }

    private static boolean colourFrom(SimpleGraph graph, int slots, int[] slotOf, int vertex) {
        if (vertex == graph.size()) {
            return true;
        }
        for (int s = 0; s < slots; s++) {
            boolean clash = false;
            for (int u = 0; u < vertex; u++) {
                if (graph.hasEdge(u, vertex) && slotOf[u] == s) {
                    clash = true;
                    break;
                }
            }
            if (!clash) {
                slotOf[vertex] = s;
                if (colourFrom(graph, slots, slotOf, vertex + 1)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Slow reference implementations and helpers
    // ------------------------------------------------------------------

    private static int[] naiveSuffixArray(String text) {
        Integer[] order = new Integer[text.length()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        final String s = text;
        Arrays.sort(order, (x, y) -> s.substring(x).compareTo(s.substring(y)));
        int[] result = new int[order.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = order[i];
        }
        return result;
    }

    private static int commonPrefix(String text, int i, int j) {
        int length = 0;
        while (i + length < text.length() && j + length < text.length()
                && text.charAt(i + length) == text.charAt(j + length)) {
            length++;
        }
        return length;
    }

    private static int naiveLongestCommonSubstring(String a, String b) {
        int best = 0;
        for (int i = 0; i < a.length(); i++) {
            for (int j = 0; j < b.length(); j++) {
                best = Math.max(best, commonPrefix2(a, i, b, j));
            }
        }
        return best;
    }

    private static int commonPrefix2(String a, int i, String b, int j) {
        int length = 0;
        while (i + length < a.length() && j + length < b.length() && a.charAt(i + length) == b.charAt(j + length)) {
            length++;
        }
        return length;
    }

    private static String randomText(Random random, int length, int alphabetSize) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append((char) ('a' + random.nextInt(alphabetSize)));
        }
        return text.toString();
    }

    private static void report(String name, boolean passed) {
        System.out.println((passed ? "PASS  " : "FAIL  ") + name);
        if (!passed) {
            failures++;
        }
    }
}
