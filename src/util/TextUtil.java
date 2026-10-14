package util;

/**
 * Small helpers for formatting console output.
 *
 * <p>Everything here is written for Java 8 compatibility, so methods such as
 * {@code String.repeat} (Java 11) are deliberately avoided.</p>
 */
public final class TextUtil {

    /** Width used for section banners printed by the services. */
    public static final int LINE_WIDTH = 70;

    private TextUtil() {
        // Utility class - no instances.
    }

    /** Returns {@code character} repeated {@code times} times. */
    public static String repeat(char character, int times) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; i++) {
            builder.append(character);
        }
        return builder.toString();
    }

    /** Pads {@code text} with spaces on the right up to {@code width}. */
    public static String padRight(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        return text + repeat(' ', width - text.length());
    }

    /** Pads {@code text} with spaces on the left up to {@code width}. */
    public static String padLeft(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        return repeat(' ', width - text.length()) + text;
    }

    /** Shortens {@code text} to {@code maxLength} characters, ending with "..." when cut. */
    public static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    /** Wraps {@code text} in double quotes. */
    public static String quote(String text) {
        return "\"" + text + "\"";
    }

    /** Prints a title between two full-width '=' lines. */
    public static void printBanner(String title) {
        System.out.println();
        System.out.println(repeat('=', LINE_WIDTH));
        System.out.println("  " + title);
        System.out.println(repeat('=', LINE_WIDTH));
    }

    /** Prints a smaller section heading followed by a '-' underline. */
    public static void printSection(String title) {
        System.out.println();
        System.out.println(title);
        System.out.println(repeat('-', Math.max(title.length(), 20)));
    }

    /**
     * Normalises free text before string algorithms are applied:
     * lower-case, runs of whitespace collapsed to one space, no outer spaces.
     */
    public static String normalise(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase().replaceAll("\\s+", " ").trim();
    }
}
