package services;

/** Outcome of a service operation: did it work, and a message that can be shown to the user. */
public class ServiceResult {

    private final boolean success;
    private final String message;

    private ServiceResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ServiceResult ok(String message) {
        return new ServiceResult(true, message);
    }

    public static ServiceResult fail(String message) {
        return new ServiceResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
