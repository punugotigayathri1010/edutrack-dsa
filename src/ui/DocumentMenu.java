package ui;

import java.util.Scanner;

import data.DocumentData;
import models.Document;
import services.DocumentService;
import util.TextUtil;

/**
 * Menu for the document tools (analyse, search, compare).
 *
 * <p>Wherever a document is needed, the user can type a sample document ID (D101, D102 ...)
 * or paste any text. Pressing Enter uses a default sample document.</p>
 */
public class DocumentMenu {

    private static final String DEFAULT_DOCUMENT = "D101";
    private static final String DEFAULT_SECOND_DOCUMENT = "D102";

    private final DocumentService documentService = new DocumentService();

    public void show(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("--- DOCUMENT ANALYSIS ---");
            System.out.println("1. Analyse a document");
            System.out.println("2. Search inside a document");
            System.out.println("3. Compare two documents");
            System.out.println("4. List sample documents");
            System.out.println("0. Back");

            String choice = InputHelper.readLine(scanner, "Choice: ");
            switch (choice) {
                case "1":
                    documentService.analyze(readDocument(scanner, "Document: ", DEFAULT_DOCUMENT));
                    break;
                case "2":
                    searchInDocument(scanner);
                    break;
                case "3":
                    compareDocuments(scanner);
                    break;
                case "4":
                    documentService.printDocumentList();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void searchInDocument(Scanner scanner) {
        String text = readDocument(scanner, "Document: ", DEFAULT_DOCUMENT);
        String pattern = InputHelper.readRequired(scanner, "Search for: ",
                value -> value.isEmpty() ? "Please type something to search for." : null);
        documentService.searchPattern(text, pattern);
    }

    private void compareDocuments(Scanner scanner) {
        String first = readDocument(scanner, "First document: ", DEFAULT_DOCUMENT);
        String second = readDocument(scanner, "Second document: ", DEFAULT_SECOND_DOCUMENT);
        documentService.compare(first, second);
    }

    /**
     * Reads a document: a known ID gives the stored text, an empty line gives the default
     * sample, anything else is used as the text itself.
     */
    private String readDocument(Scanner scanner, String prompt, String defaultId) {
        String input = InputHelper.readLine(scanner, prompt);
        if (input.isEmpty()) {
            input = defaultId;
        }

        Document stored = DocumentData.findById(input);
        if (stored != null) {
            System.out.println("Using " + stored.getId() + ": " + TextUtil.truncate(stored.getTitle(), 50));
            return stored.getContent();
        }
        return input;
    }
}
