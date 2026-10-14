package services;

import java.util.List;

import data.DocumentData;
import models.Document;
import stringalgorithms.KMP;
import suffixstructures.SuffixArray;
import suffixstructures.SuffixArrayAnalysis;
import suffixstructures.SuffixArrayAnalysis.Comparison;
import suffixstructures.SuffixArrayAnalysis.Phrase;
import suffixstructures.LCP;
import util.TextUtil;

/**
 * Document analysis: finds repeated text, searches inside a document and compares two documents.
 *
 * <p>In plain words:</p>
 * <ul>
 *   <li><b>Analyse</b> - shows which pieces of text appear more than once in one document;</li>
 *   <li><b>Search</b> - shows every place where a word or phrase appears, with the text around it;</li>
 *   <li><b>Compare</b> - shows the text two documents have in common and how similar they are.</li>
 * </ul>
 *
 * <p>Behind the scenes these use a suffix array (a sorted list of all endings of the text) and its
 * LCP array, but the printed reports only use everyday words. Upper/lower case and extra spaces
 * are ignored.</p>
 */
public class DocumentService {

    /** Shortest repeated phrase (characters) listed for a single document. */
    private static final int MIN_REPEATED_PHRASE = 6;
    /** Shortest shared phrase (characters) that counts towards similarity. */
    private static final int MIN_SHARED_PHRASE = 8;
    /** Characters of surrounding text shown next to a search result. */
    private static final int CONTEXT = 12;
    /** How many phrases are listed at most. */
    private static final int PHRASE_LIMIT = 5;

    // ------------------------------------------------------------------
    // Sample documents
    // ------------------------------------------------------------------

    public void printDocumentList() {
        TextUtil.printSection("Sample documents");
        for (Document document : DocumentData.getAll()) {
            System.out.println("  " + document);
        }
    }

    // ------------------------------------------------------------------
    // 1. Analysis of one document
    // ------------------------------------------------------------------

    public void analyze(String rawText) {
        String text = TextUtil.normalise(rawText);
        if (text.isEmpty()) {
            System.out.println("The document is empty.");
            return;
        }

        int[] suffixArray = SuffixArray.build(text);
        int[] lcp = LCP.build(text, suffixArray);

        TextUtil.printBanner("DOCUMENT ANALYSIS");
        System.out.println("Document : " + TextUtil.truncate(TextUtil.quote(text), 90));
        int words = countWords(text);
        System.out.println("Size     : " + text.length() + " characters, " + words + (words == 1 ? " word" : " words"));

        TextUtil.printSection("Repeated text");
        Phrase longest = SuffixArrayAnalysis.longestRepeatedSubstring(text, suffixArray, lcp);
        if (longest == null) {
            System.out.println("  Nothing is repeated in this document.");
            System.out.println();
            return;
        }
        String longestShown = longest.text.trim().isEmpty() ? longest.text : longest.text.trim();
        System.out.println("  Longest repeated text : " + TextUtil.quote(longestShown)
                + " (" + longestShown.length() + " characters, " + longest.positions.size() + " times)");

        List<Phrase> phrases = SuffixArrayAnalysis.findRepeatedPhrases(
                text, suffixArray, lcp, MIN_REPEATED_PHRASE, PHRASE_LIMIT);
        if (phrases.isEmpty()) {
            System.out.println("  Repeated phrases      : none of " + MIN_REPEATED_PHRASE + " or more characters");
        } else {
            System.out.println("  Repeated phrases (longest first):");
            for (Phrase phrase : phrases) {
                System.out.println("    " + TextUtil.quote(TextUtil.truncate(phrase.text, 60))
                        + " - " + phrase.positions.size() + " times, at character " + join(phrase.positions));
            }
        }
        System.out.println();
    }

    private int countWords(String text) {
        return text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
    }

    // ------------------------------------------------------------------
    // 2. Searching inside a document
    // ------------------------------------------------------------------

    public void searchPattern(String rawText, String rawPattern) {
        String text = TextUtil.normalise(rawText);
        String pattern = TextUtil.normalise(rawPattern);
        if (text.isEmpty() || pattern.isEmpty()) {
            System.out.println("Both the document and the search text must contain something.");
            return;
        }

        int[] suffixArray = SuffixArray.build(text);
        List<Integer> positions = SuffixArray.search(text, suffixArray, pattern);

        TextUtil.printBanner("DOCUMENT SEARCH");
        System.out.println("Looking for : " + TextUtil.quote(pattern));

        if (positions.isEmpty()) {
            System.out.println("Not found in the document.");
            System.out.println();
            return;
        }

        System.out.println("Found " + positions.size() + " time(s):");
        for (int i = 0; i < positions.size(); i++) {
            System.out.println("  #" + (i + 1) + " at character " + TextUtil.padRight(String.valueOf(positions.get(i)), 5)
                    + showWithContext(text, positions.get(i), pattern.length()));
        }

        // Silent safety check against a second search method.
        if (!KMP.search(text, pattern).equals(positions)) {
            System.out.println("Warning: internal consistency check failed. Please report this.");
        }
        System.out.println();
    }

    /** Text around a match, with the match itself in square brackets. */
    private String showWithContext(String text, int start, int length) {
        int from = Math.max(0, start - CONTEXT);
        int to = Math.min(text.length(), start + length + CONTEXT);
        return (from > 0 ? "..." : "") + text.substring(from, start)
                + "[" + text.substring(start, start + length) + "]"
                + text.substring(start + length, to) + (to < text.length() ? "..." : "");
    }

    // ------------------------------------------------------------------
    // 3. Comparing two documents
    // ------------------------------------------------------------------

    public void compare(String rawA, String rawB) {
        String a = TextUtil.normalise(rawA);
        String b = TextUtil.normalise(rawB);
        if (a.isEmpty() || b.isEmpty()) {
            System.out.println("Both documents must contain some text.");
            return;
        }

        Comparison result = SuffixArrayAnalysis.compare(a, b, MIN_SHARED_PHRASE);

        TextUtil.printBanner("DOCUMENT COMPARISON");
        System.out.println("Document 1 : " + a.length() + " characters");
        System.out.println("Document 2 : " + b.length() + " characters");

        TextUtil.printSection("Text the two documents share");
        if (result.longestCommon.length() < MIN_SHARED_PHRASE) {
            System.out.println("  No meaningful shared text.");
        } else {
            System.out.println("  Longest shared text : " + TextUtil.quote(TextUtil.truncate(result.longestCommon, 60))
                    + " (" + result.longestCommon.length() + " characters)");
        }
        if (result.longestCommon.length() >= MIN_SHARED_PHRASE && result.sharedPhrases.isEmpty()) {
            System.out.println("  Shared phrases      : none of " + MIN_SHARED_PHRASE + " or more characters");
        } else if (!result.sharedPhrases.isEmpty()) {
            System.out.println("  Shared phrases:");
            for (String phrase : result.sharedPhrases) {
                System.out.println("    - " + TextUtil.quote(TextUtil.truncate(phrase, 62)));
            }
        }

        TextUtil.printSection("Similarity");
        System.out.println("  Shared text in document 1 : " + result.coveredA + " of " + result.lengthA + " characters");
        System.out.println("  Shared text in document 2 : " + result.coveredB + " of " + result.lengthB + " characters");

        int percent = (int) Math.round(result.similarity * 100);
        System.out.println("  Similarity : " + percent + "%  " + bar(percent) + "  " + verdict(percent));
        System.out.println();
    }

    private String bar(int percent) {
        int filled = percent / 5;   // 20 segments
        return "[" + TextUtil.repeat('#', filled) + TextUtil.repeat('.', 20 - filled) + "]";
    }

    private String verdict(int percent) {
        if (percent >= 60) {
            return "HIGH - the documents share a lot of text";
        }
        if (percent >= 30) {
            return "MODERATE - some shared passages";
        }
        return "LOW - mostly different";
    }

    private String join(List<Integer> numbers) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < numbers.size(); i++) {
            if (i > 0) {
                text.append(", ");
            }
            text.append(numbers.get(i));
        }
        return text.toString();
    }
}
