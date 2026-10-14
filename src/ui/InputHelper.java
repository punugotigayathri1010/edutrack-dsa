package ui;

import java.util.Scanner;
import java.util.function.Function;

/**
 * Reusable console-input helpers, so the menus do not repeat the same read-and-check loops.
 *
 * <p>A "validator" is a function that receives the typed text and returns {@code null}
 * when it is fine, or an error message when it is not. The helpers keep asking until the
 * input is valid.</p>
 */
public final class InputHelper {

    private InputHelper() {
    }

    /** Prints a prompt and returns the whole line the user types (trimmed). */
    public static String readLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            // Input stream closed (for example Ctrl+D or the end of a piped file): stop cleanly.
            System.out.println();
            System.out.println("Input closed. Exiting EduTrack.");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    /** Asks until the answer passes the validator. */
    public static String readRequired(Scanner scanner, String prompt, Function<String, String> validator) {
        while (true) {
            String input = readLine(scanner, prompt);
            String error = validator.apply(input);
            if (error == null) {
                return input;
            }
            System.out.println("  ! " + error);
        }
    }

    /** Like {@link #readRequired}, but pressing Enter accepts {@code defaultValue}. */
    public static String readWithDefault(Scanner scanner, String prompt, String defaultValue,
                                         Function<String, String> validator) {
        while (true) {
            String input = readLine(scanner, prompt);
            if (input.isEmpty()) {
                input = defaultValue;
            }
            String error = validator.apply(input);
            if (error == null) {
                return input;
            }
            System.out.println("  ! " + error);
        }
    }

    /** Returns {@code null} when the user just presses Enter; otherwise asks until the answer is valid. */
    public static String readOptional(Scanner scanner, String prompt, Function<String, String> validator) {
        while (true) {
            String input = readLine(scanner, prompt);
            if (input.isEmpty()) {
                return null;
            }
            String error = validator.apply(input);
            if (error == null) {
                return input;
            }
            System.out.println("  ! " + error);
        }
    }

    /** Asks for a whole number between {@code min} and {@code max} (inclusive). */
    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            String input = readLine(scanner, prompt);
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the message below
            }
            System.out.println("  ! Please enter a whole number from " + min + " to " + max + ".");
        }
    }

    /** Like {@link #readInt}, but Enter returns {@code defaultValue}. */
    public static int readIntWithDefault(Scanner scanner, String prompt, int defaultValue, int min, int max) {
        while (true) {
            String input = readLine(scanner, prompt);
            if (input.isEmpty()) {
                return defaultValue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the message below
            }
            System.out.println("  ! Please enter a whole number from " + min + " to " + max + ".");
        }
    }

    /** Returns {@code null} on Enter, otherwise a number in range. */
    public static Integer readOptionalInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            String input = readLine(scanner, prompt);
            if (input.isEmpty()) {
                return null;
            }
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the message below
            }
            System.out.println("  ! Please enter a whole number from " + min + " to " + max + ".");
        }
    }

    /** Yes/no question; anything except "y" or "yes" counts as no. */
    public static boolean confirm(Scanner scanner, String question) {
        String answer = readLine(scanner, question + " ").toLowerCase();
        return answer.equals("y") || answer.equals("yes");
    }
}
