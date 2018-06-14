package sh.calaba.instrumentationbackend.query.ui;

public class InvalidUIObjectException extends RuntimeException {
    public InvalidUIObjectException() {
        super();
    }

    public InvalidUIObjectException(String detailMessage) {
        super(detailMessage);
    }

    public InvalidUIObjectException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public InvalidUIObjectException(Throwable throwable) {
        super(throwable);
    }
}
