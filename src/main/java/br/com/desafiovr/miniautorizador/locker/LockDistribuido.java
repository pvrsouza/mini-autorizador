package br.com.desafiovr.miniautorizador.locker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

@Service
@Slf4j
public class LockDistribuido {

    private static final long INTERVALOS_DE_RETENTETIVAS = 100L;
    private final LockRegistry lockRegistry;

    public LockDistribuido(LockRegistry lockRegistry) {
        this.lockRegistry = lockRegistry;
    }

    public <T> ExecucaoLockResultado<T> lock(final String chave,
                                             final int tempoMaximoDeEsperaParaAdiquirirBloqueio,
                                             final int lockTimeoutSeconds,
                                             final Callable<T> task) {
        try {
            return tryToGetLock(() -> {
                Lock lock = lockRegistry.obtain(chave);

                final Boolean lockAcquired;
                try {
                    lockAcquired = lock.tryLock(lockTimeoutSeconds, TimeUnit.SECONDS);

                    if (lockAcquired == Boolean.FALSE) {
                        log.error("lockAcquired retornou {}. Não foi possível adiquirir o bloqueio para a chave '{}'", lockAcquired, chave);
                        return null;
                    }

                } catch (InterruptedException e) {
                    log.error("InterruptedException - Falha ao adiquirir o bloqueio para a chave '{}'", chave);
                    return null;
                }

                log.info("Bloqueio adiquirido com sucesso para a chave {}", chave);

                try {
                    T taskResult = task.call();
                    return ExecucaoLockResultado.getBloqueioAdiquiridoResultado(taskResult);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return ExecucaoLockResultado.getBloqueioAdiquiridoComException(e);
                } finally {
                    releaseLock(chave);
                }
            }, chave, tempoMaximoDeEsperaParaAdiquirirBloqueio);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            return ExecucaoLockResultado.getBloqueioAdiquiridoComException(e);
        }
    }

    private void releaseLock(final String key) {
        lockRegistry.obtain(key).unlock();
    }

    private static <T> T tryToGetLock(final Supplier<T> task,
                                      final String lockKey,
                                      final int tempoMaximoDeEsperaParaAdiquirirBloqueio) throws Exception {
        final long tempoMaximoDeEsperaBloqueio = TimeUnit.SECONDS.toMillis(tempoMaximoDeEsperaParaAdiquirirBloqueio);

        final long startTimestamp = System.currentTimeMillis();
        while (true) {
            log.info("Tentando pegar um bloqueio para a key '{}'", lockKey);
            final T response = task.get();
            if (response != null) {
                return response;
            }
            log.info("Retentativa de obter bloqueio para a key  '{}'", lockKey);
            sleep(INTERVALOS_DE_RETENTETIVAS);

            if (System.currentTimeMillis() - startTimestamp > tempoMaximoDeEsperaBloqueio) {
                throw new Exception("Falha ao tentar adiquirir um bloqueio. Expirou o tempo máximo de espera para obter bloqueio: " + tempoMaximoDeEsperaBloqueio + " milliseconds");
            }
        }
    }

    private static void sleep(final long sleepTimeMillis) {
        try {
            Thread.sleep(sleepTimeMillis);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
        }
    }

}
