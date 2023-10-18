package br.com.desafiovr.miniautorizador.locker;

public class ExecucaoLockResultado<T> {
    private final boolean bloqueioAdiquirido;
    public final T resultadoBloqueioAdiquirido;
    public final Exception exception;

    private ExecucaoLockResultado(boolean bloqueioAdiquirido, T resultadoBloqueioAdiquirido, final Exception exception) {
        this.bloqueioAdiquirido = bloqueioAdiquirido;
        this.resultadoBloqueioAdiquirido = resultadoBloqueioAdiquirido;
        this.exception = exception;
    }

    public static <T> ExecucaoLockResultado<T> getBloqueioAdiquiridoResultado(final T result) {
        return new ExecucaoLockResultado<>(true, result, null);
    }

    public static <T> ExecucaoLockResultado<T> getBloqueioAdiquiridoComException(final Exception e) {
        return new ExecucaoLockResultado<>(true, null, e);
    }

    public static <T> ExecucaoLockResultado<T> bloqueioNaoAdiquirido() {
        return new ExecucaoLockResultado<>(false, null, null);
    }

    public boolean isBloqueioAdiquirido() {
        return bloqueioAdiquirido;
    }

    public T getResultadoBloqueioAdiquirido() {
        return resultadoBloqueioAdiquirido;
    }

    public boolean hasException() {
        return exception != null;
    }

    public Exception getException() {
        return exception;
    }
}