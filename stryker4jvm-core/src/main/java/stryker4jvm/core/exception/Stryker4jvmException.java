package stryker4jvm.core.exception;

public abstract class Stryker4jvmException extends Exception {
    public Stryker4jvmException(String msg, Throwable cause) {
        super(msg, cause);
        initCause(cause);
    }
}
