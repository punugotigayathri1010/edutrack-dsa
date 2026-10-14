package randomizedparallel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Quicksort with a <b>random pivot</b>.
 *
 * <p>Choosing the pivot at random means no fixed input (already sorted, reversed, many equal
 * marks ...) can force the slow O(n^2) case: the expected running time is O(n log n) for every
 * input. The partition is three-way (smaller / equal / larger), so lots of equal values are
 * handled quickly, and the loop continues on the larger side to keep the recursion shallow.</p>
 */
public final class RandomizedQuickSort {

    private RandomizedQuickSort() {
    }

    /** Sorts {@code values} in ascending order. */
    public static void sort(int[] values, Random random) {
        sort(values, 0, values.length - 1, random);
    }

    private static void sort(int[] a, int low, int high, Random random) {
        while (low < high) {
            int pivot = a[low + random.nextInt(high - low + 1)];

            // Three-way partition: a[low..lt-1] < pivot, a[lt..gt] == pivot, a[gt+1..high] > pivot.
            int lt = low;
            int gt = high;
            int i = low;
            while (i <= gt) {
                if (a[i] < pivot) {
                    swap(a, lt++, i++);
                } else if (a[i] > pivot) {
                    swap(a, i, gt--);
                } else {
                    i++;
                }
            }

            // Recurse into the smaller part, loop on the larger one.
            if (lt - low < high - gt) {
                sort(a, low, lt - 1, random);
                low = gt + 1;
            } else {
                sort(a, gt + 1, high, random);
                high = lt - 1;
            }
        }
    }

    private static void swap(int[] a, int i, int j) {
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    /** Sorts a list with the given ordering (use a RandomAccess list such as ArrayList). */
    public static <T> void sort(List<T> list, Comparator<? super T> order, Random random) {
        sort(list, order, 0, list.size() - 1, random);
    }

    private static <T> void sort(List<T> list, Comparator<? super T> order, int low, int high, Random random) {
        while (low < high) {
            T pivot = list.get(low + random.nextInt(high - low + 1));

            int lt = low;
            int gt = high;
            int i = low;
            while (i <= gt) {
                int compare = order.compare(list.get(i), pivot);
                if (compare < 0) {
                    Collections.swap(list, lt++, i++);
                } else if (compare > 0) {
                    Collections.swap(list, i, gt--);
                } else {
                    i++;
                }
            }

            if (lt - low < high - gt) {
                sort(list, order, low, lt - 1, random);
                low = gt + 1;
            } else {
                sort(list, order, gt + 1, high, random);
                high = lt - 1;
            }
        }
    }
}
