package rbac.util;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class BackgroundExecutor {
    private final ExecutorService executor;

    public BackgroundExecutor(String threadNamePrefix, int threads) {
        if (threads <= 0) throw new IllegalArgumentException("threads must be > 0");
        String prefix = (threadNamePrefix == null || threadNamePrefix.isBlank()) ? "rbac-bg" : threadNamePrefix.trim();
        ThreadFactory tf = new ThreadFactory() {
            private int n = 0;

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, prefix + "-" + (++n));
                t.setDaemon(true);
                return t;
            }
        };
        this.executor = Executors.newFixedThreadPool(threads, tf);
    }

    public Future<?> submit(Runnable task) {
        Objects.requireNonNull(task, "task");
        return executor.submit(task);
    }

    public <T> Future<T> submit(java.util.concurrent.Callable<T> task) {
        Objects.requireNonNull(task, "task");
        return executor.submit(task);
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}

