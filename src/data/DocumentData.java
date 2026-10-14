package data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import models.Document;

/**
 * Sample academic documents used by the suffix-array demos.
 *
 * <p>D101 and D102 are two submissions for the same assignment and share long
 * phrases, while D103 is unrelated lecture material - so the similarity report
 * shows both a high and a low score.</p>
 */
public class DocumentData {

    private static final List<Document> documents = new ArrayList<>(Arrays.asList(
            new Document("D101", "Assignment A101 - Submission 1",
                    "Pattern matching finds every occurrence of a pattern inside a text. "
                            + "The Knuth Morris Pratt algorithm avoids re-checking characters "
                            + "by using a failure table."),
            new Document("D102", "Assignment A101 - Submission 2",
                    "The Knuth Morris Pratt algorithm avoids re-checking characters "
                            + "by using a failure table. Pattern matching finds all positions "
                            + "of a word inside a document."),
            new Document("D103", "Lecture Notes - Operating Systems",
                    "Scheduling decides which process runs on the processor next. "
                            + "A good scheduler keeps the processor busy and gives every process "
                            + "a fair share of time.")));

    private DocumentData() {
    }

    /** Returns a read-only view of all sample documents. */
    public static List<Document> getAll() {
        return Collections.unmodifiableList(documents);
    }

    /** Finds a document by ID (case-insensitive); returns {@code null} when missing. */
    public static Document findById(String id) {
        for (Document document : documents) {
            if (document.getId().equalsIgnoreCase(id)) {
                return document;
            }
        }
        return null;
    }
}
