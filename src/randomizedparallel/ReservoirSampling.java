package randomizedparallel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Reservoir sampling (Algorithm R): pick k items uniformly at random from a stream whose length
 * is not known in advance, using only O(k) memory and a single pass.
 *
 * <p>The first k items fill the reservoir. The item at position i (counting from 1, i &gt; k)
 * replaces a random reservoir slot with probability k / i. By induction every item of the
 * stream ends up in the final sample with the same probability k / n.</p>
 */
public final class ReservoirSampling {

    private ReservoirSampling() {
    }

    public static <T> List<T> sample(Iterable<T> stream, int k, Random random) {
        List<T> reservoir = new ArrayList<>();
        if (k <= 0) {
            return reservoir;
        }
        long seen = 0;
        for (T item : stream) {
            seen++;
            if (reservoir.size() < k) {
                reservoir.add(item);
            } else {
                long slot = (long) (random.nextDouble() * seen); // uniform in [0, seen)
                if (slot < k) {
                    reservoir.set((int) slot, item);
                }
            }
        }
        return reservoir;
    }
}
