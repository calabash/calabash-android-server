package sh.calaba.instrumentationbackend.entrypoint;

public class UnableToLoadCalabashException extends Exception {
    public UnableToLoadCalabashException() {
    }

    public UnableToLoadCalabashException(String detailMessage) {
        super(detailMessage);
    }

    public UnableToLoadCalabashException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UnableToLoadCalabashException(Throwable throwable) {
        super(throwable);
    }
}