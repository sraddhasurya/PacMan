package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

/**
 * A min priority queue of distinct elements of type `KeyType` associated with (extrinsic) double
 * priorities, implemented using a binary heap paired with a hash table.
 */
public class MinPQueue<KeyType> {

    /**
     * Pairs an element `key` with its associated priority `priority`.
     */
    private record Entry<KeyType>(KeyType key, double priority) {
        // Note: This is equivalent to declaring a static nested class with fields `key` and
        //  `priority` and a corresponding constructor and observers, overriding `equals()` and
        //  `hashCode()` to depend on the fields, and overriding `toString()` to print their values.
        // https://docs.oracle.com/en/java/javase/17/language/records.html
    }

    /**
     * ArrayList representing a binary min-heap of element-priority pairs.  Satisfies
     * `heap.get(i).priority() >= heap.get((i-1)/2).priority()` for all `i` in `[1..heap.size())`.
     */
    private final ArrayList<Entry<KeyType>> heap;

    /**
     * Associates each element in the queue with its index in `heap`.  Satisfies
     * `heap.get(index.get(e)).key().equals(e)` if `e` is an element in the queue. Only maps
     * elements that are in the queue (`index.size() == heap.size()`).
     */
    private final Map<KeyType, Integer> index;

    /**
     * Asserts invariants for the fields heap and index
     */
    private void assertInv() {
        // heap invariant
        for (int i = 1; i < heap.size(); i++) {
            assert heap.get((i - 1) / 2).priority() <= heap.get(i).priority();
        }

        // index invariant
        for (Map.Entry<KeyType, Integer> entry : index.entrySet()) {
            KeyType key = entry.getKey();
            int indx = entry.getValue();
            assert indx >= 0 && indx < heap.size();
            assert heap.get(indx).key().equals(key);
        }
        assert heap.size() == index.size();
    }


    /**
     * Create an empty queue.
     */
    public MinPQueue() {
        index = new HashMap<>();
        heap = new ArrayList<>();
    }

    /**
     * Return whether this queue contains no elements.
     */
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    /**
     * Return the number of elements contained in this queue.
     */
    public int size() {
        return heap.size();
    }

    /**
     * Return an element associated with the smallest priority in this queue.  This is the same
     * element that would be removed by a call to `remove()` (assuming no mutations in between).
     * Throws NoSuchElementException if this queue is empty.
     */
    public KeyType peek() {
        // Propagate exception from `List::getFirst()` if empty.
        return heap.getFirst().key();
    }

    /**
     * Return the minimum priority associated with an element in this queue.  Throws
     * NoSuchElementException if this queue is empty.
     */
    public double minPriority() {
        return heap.getFirst().priority();
    }

    /**
     * Swap the Entries at indices `i` and `j` in `heap`, updating `index` accordingly.  Requires
     * {@code 0 <= i,j < heap.size()}.
     */
    private void swap(int i, int j) {
        // assert preconditions
        assert i >= 0;
        assert j < heap.size();

        // swap entries
        Entry<KeyType> keyi = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, keyi);

        // update indices
        index.put(heap.get(i).key(), i);
        index.put(heap.get(j).key(), j);

    }

    // TODO 8b: Implement private helper methods for bubbling entries up and down in the heap.
    //  Their interfaces are up to you, but you must write precise specifications.

    /**
     * Repeatedly swaps element with its parent until it has lower priority and min-heap invariant
     * is restored or when the root is reached.
     */
    private void bubbleUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (heap.get(i).priority() < heap.get(parent).priority()) {
                swap(i, parent);
                i = parent;
            } else {
                break;
            }
        }
        assertInv();
    }

    /**
     * Repeatedly swaps parent with the child with the lowest priority until the min-heap invariant
     * is restored of a leaf is reached.
     */
    private void bubbleDown(int i) {
        int size = heap.size();
        while (true) {
            int leftChild = 2 * i + 1;
            int rightChild = 2 * i + 2;
            int current = i;

            if (leftChild < size && heap.get(leftChild).priority() < heap.get(current).priority()) {
                current = leftChild;
            }
            if (rightChild < size && heap.get(rightChild).priority() < heap.get(current)
                    .priority()) {
                current = rightChild;
            }
            if (current != i) {
                swap(i, current);
                i = current;
            } else {
                break;
            }
        }
        assertInv();
    }


    /**
     * Add element `key` to this queue, associated with priority `priority`.  Requires `key` is not
     * contained in this queue.
     */
    private void add(KeyType key, double priority) {
        assert (!heap.contains(key));
        Entry<KeyType> entry = new Entry<>(key, priority);
        heap.add(entry);
        int num = heap.size() - 1;
        index.put(key, num);
        bubbleUp(num);
        assertInv();

    }

    /**
     * Change the priority associated with element `key` to `priority`.  Requires that `key` is
     * contained in this queue.
     */
    private void update(KeyType key, double priority) {
        assert index.containsKey(key);
        int i = index.get(key);
        Entry<KeyType> update = new Entry<>(key, priority);
        heap.set(i, update);
        if (i > 0 && heap.get(i).priority() < heap.get((i - 1) / 2).priority()) {
            bubbleUp(i);
        } else {
            bubbleDown(i);
        }
        assertInv();
    }

    /**
     * If `key` is already contained in this queue, change its associated priority to `priority`.
     * Otherwise, add it to this queue with that priority.
     */
    public void addOrUpdate(KeyType key, double priority) {
        if (!index.containsKey(key)) {
            add(key, priority);
        } else {
            update(key, priority);
        }
    }

    /**
     * Remove and return the element associated with the smallest priority in this queue.  If
     * multiple elements are tied for the smallest priority, an arbitrary one will be removed.
     * Throws NoSuchElementException if this queue is empty.
     */
    public KeyType remove() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        KeyType min = heap.get(0).key();
        swap(0, heap.size() - 1);
        heap.remove(heap.size() - 1);
        index.remove(min);
        if (!heap.isEmpty()) {
            bubbleDown(0);
        }
        assertInv();
        return min;
    }

}
