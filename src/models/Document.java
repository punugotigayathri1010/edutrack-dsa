package models;

/** An academic document (assignment submission, lecture notes ...) stored as plain text. */
public class Document {

    private final String id;
    private final String title;
    private final String content;

    public Document(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return id + " | " + title + " | " + content.length() + " characters";
    }
}
