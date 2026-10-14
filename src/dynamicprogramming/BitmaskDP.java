package dynamicprogramming;

import java.util.ArrayList;
import java.util.List;

/**
 * Helpers for bitmask dynamic programming over subsets of courses/resources.
 *
 * <p>A subset of n items is stored in one int: bit i is 1 when item i is chosen.
 * This keeps every subset of a small set addressable as a DP state (2^n states).</p>
 */
public class BitmaskDP {

    private BitmaskDP() {
    }

    /** Returns the indexes of the items whose bits are set in {@code mask}. */
    public static List<Integer> selectedCourses(int mask, int courseCount) {
        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < courseCount; i++) {
            boolean chosen = (mask & (1 << i)) != 0;
            if (chosen) {
                selected.add(i);
            }
        }
        return selected;
    }
}
