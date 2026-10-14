package stringalgorithms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Aho-Corasick multi-pattern search (case-insensitive).
 *
 * <p>All keywords are stored in a trie. "Failure links" connect every trie node to
 * the longest proper suffix that is also a trie prefix, so the text is scanned once
 * and all keywords are found together.</p>
 *
 * <p>Time: O(text length + total pattern length + matches).</p>
 */
public class AhoCorasick {

    /** One state of the trie / automaton. */
    private static class TrieNode {
        final Map<Character, Integer> children = new HashMap<>();
        int failureLink = 0;
        final List<String> matchedPatterns = new ArrayList<>();
    }

    private AhoCorasick() {
    }

    /** Returns the set of patterns (in order of discovery) that appear in {@code text}. */
    public static Set<String> search(String text, List<String> patterns) {
        List<TrieNode> trie = buildTrie(patterns);
        buildFailureLinks(trie);
        return scan(text, trie);
    }

    /** Step 1: insert every pattern into a trie. */
    private static List<TrieNode> buildTrie(List<String> patterns) {
        List<TrieNode> trie = new ArrayList<>();
        trie.add(new TrieNode()); // node 0 is the root

        for (String pattern : patterns) {
            int current = 0;
            for (char c : pattern.toLowerCase().toCharArray()) {
                Integer next = trie.get(current).children.get(c);
                if (next == null) {
                    trie.add(new TrieNode());
                    next = trie.size() - 1;
                    trie.get(current).children.put(c, next);
                }
                current = next;
            }
            trie.get(current).matchedPatterns.add(pattern);
        }
        return trie;
    }

    /** Step 2: breadth-first pass that fills in failure links and inherited matches. */
    private static void buildFailureLinks(List<TrieNode> trie) {
        Queue<Integer> queue = new ArrayDeque<>();

        // Children of the root always fail back to the root.
        for (int child : trie.get(0).children.values()) {
            trie.get(child).failureLink = 0;
            queue.add(child);
        }

        while (!queue.isEmpty()) {
            int node = queue.remove();

            for (Map.Entry<Character, Integer> edge : trie.get(node).children.entrySet()) {
                char c = edge.getKey();
                int child = edge.getValue();
                queue.add(child);

                // Follow failure links until a node with a transition on c is found.
                int fallback = trie.get(node).failureLink;
                while (fallback != 0 && !trie.get(fallback).children.containsKey(c)) {
                    fallback = trie.get(fallback).failureLink;
                }
                Integer target = trie.get(fallback).children.get(c);
                if (target != null && target != child) {
                    fallback = target;
                }

                trie.get(child).failureLink = fallback;
                trie.get(child).matchedPatterns.addAll(trie.get(fallback).matchedPatterns);
            }
        }
    }

    /** Step 3: run the text through the automaton and collect matches. */
    private static Set<String> scan(String text, List<TrieNode> trie) {
        Set<String> found = new LinkedHashSet<>();
        int state = 0;

        for (char c : text.toLowerCase().toCharArray()) {
            while (state != 0 && !trie.get(state).children.containsKey(c)) {
                state = trie.get(state).failureLink;
            }
            Integer next = trie.get(state).children.get(c);
            if (next != null) {
                state = next;
            }
            found.addAll(trie.get(state).matchedPatterns);
        }
        return found;
    }
}
