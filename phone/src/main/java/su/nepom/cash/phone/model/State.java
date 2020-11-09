package su.nepom.cash.phone.model;

/**
 * Состояние асинхронной операции
 */
public class State {
    private Throwable error = null;
    private boolean inProgress;

    public Throwable getError() {
        return error;
    }

    void setError(Throwable error) {
        this.error = error;
    }

    public boolean hasError() {
        return error != null;
    }

    public Throwable getAndResetError() {
        Throwable e = error;
        error = null;
        return e;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }
}
