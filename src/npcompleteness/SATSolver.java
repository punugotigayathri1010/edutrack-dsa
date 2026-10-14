package npcompleteness;

/**
 * A DPLL solver for the Boolean satisfiability problem (SAT), the first NP-complete problem.
 *
 * <p>The formula is in conjunctive normal form: an AND of clauses, where each clause is an OR
 * of literals. Variables are numbered 1..n; the literal {@code 3} means "variable 3 is true"
 * and {@code -3} means "variable 3 is false".</p>
 *
 * <p>The search repeats two steps: <b>unit propagation</b> (a clause with one open literal
 * forces that literal) and <b>branching</b> (try a variable true, then false). It is exact but
 * exponential in the worst case, which is expected for an NP-complete problem.</p>
 */
public class SATSolver {

    private final int numVars;
    private final int[][] clauses;
    private int decisions;

    public SATSolver(int numVars, int[][] clauses) {
        this.numVars = numVars;
        this.clauses = clauses;
    }

    /**
     * Looks for a satisfying assignment.
     *
     * @return an array indexed 1..numVars (index 0 unused) holding the value of each variable,
     *         or {@code null} when the formula is unsatisfiable
     */
    public boolean[] solve() {
        decisions = 0;
        int[] assignment = new int[numVars + 1];
        int[] result = search(assignment);
        if (result == null) {
            return null;
        }
        boolean[] model = new boolean[numVars + 1];
        for (int v = 1; v <= numVars; v++) {
            model[v] = result[v] == 1;
        }
        return model;
    }

    /** Number of branching decisions made by the last call to {@link #solve()}. */
    public int getDecisions() {
        return decisions;
    }

    /** True when {@code model} makes every clause true. */
    public static boolean satisfies(boolean[] model, int[][] clauses) {
        for (int[] clause : clauses) {
            boolean clauseTrue = false;
            for (int literal : clause) {
                boolean value = model[Math.abs(literal)];
                if (literal > 0 == value) {
                    clauseTrue = true;
                    break;
                }
            }
            if (!clauseTrue) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------

    /** assignment[v] is 0 (open), 1 (true) or -1 (false). Returns a full assignment or null. */
    private int[] search(int[] assignment) {
        int[] current = assignment.clone();
        if (!propagate(current)) {
            return null;
        }

        int branchVar = pickOpenVariable(current);
        if (branchVar == 0) {
            return current; // every clause is satisfied
        }

        decisions++;
        for (int value = 1; value >= -1; value -= 2) {
            int[] attempt = current.clone();
            attempt[branchVar] = value;
            int[] found = search(attempt);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /** Applies unit propagation. Returns false when some clause can no longer be satisfied. */
    private boolean propagate(int[] assignment) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int[] clause : clauses) {
                boolean satisfied = false;
                int open = 0;
                int lastOpen = 0;
                for (int literal : clause) {
                    int state = assignment[Math.abs(literal)];
                    if (state == 0) {
                        open++;
                        lastOpen = literal;
                    } else if ((literal > 0) == (state == 1)) {
                        satisfied = true;
                        break;
                    }
                }
                if (satisfied) {
                    continue;
                }
                if (open == 0) {
                    return false;
                }
                if (open == 1) {
                    assignment[Math.abs(lastOpen)] = lastOpen > 0 ? 1 : -1;
                    changed = true;
                }
            }
        }
        return true;
    }

    /** First open variable that occurs in a clause that is not yet satisfied; 0 if none. */
    private int pickOpenVariable(int[] assignment) {
        for (int[] clause : clauses) {
            boolean satisfied = false;
            int firstOpen = 0;
            for (int literal : clause) {
                int state = assignment[Math.abs(literal)];
                if (state == 0) {
                    if (firstOpen == 0) {
                        firstOpen = Math.abs(literal);
                    }
                } else if ((literal > 0) == (state == 1)) {
                    satisfied = true;
                    break;
                }
            }
            if (!satisfied && firstOpen != 0) {
                return firstOpen;
            }
        }
        return 0;
    }
}
