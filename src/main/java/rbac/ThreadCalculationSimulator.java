package rbac;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadCalculationSimulator {
    private static final int DEFAULT_THREAD_COUNT = 5;
    private static final int DEFAULT_CALCULATION_LENGTH = 40;
    private static final int MIN_STEP_DELAY_MS = 40;
    private static final int MAX_STEP_DELAY_MS = 130;
    private static final int RENDER_INTERVAL_MS = 80;

    public static void main(String[] args) {
        int threadCount = parsePositiveArg(args, 0, DEFAULT_THREAD_COUNT);
        int calculationLength = parsePositiveArg(args, 1, DEFAULT_CALCULATION_LENGTH);

        var workers = new ArrayList<WorkerState>(threadCount);
        var threads = new ArrayList<Thread>(threadCount);

        for (int i = 0; i < threadCount; i++) {
            WorkerState worker = new WorkerState(i + 1, calculationLength);
            workers.add(worker);
            threads.add(new Thread(worker, "calc-" + (i + 1)));
        }

        System.out.println("Multithreaded calculation simulation");
        System.out.println("threads=" + threadCount + ", length=" + calculationLength);

        for (int i = 0; i < workers.size(); i++) {
            System.out.println();
        }

        for (Thread thread : threads) {
            thread.start();
        }

        renderLoop(workers, threads);

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Print one final stable frame.
        System.out.print(ansiMoveUp(workers.size()));
        for (WorkerState worker : workers) {
            System.out.println(renderLine(worker));
        }
    }

    private static void renderLoop(List<WorkerState> workers, List<Thread> threads) {
        while (isAnyAlive(threads)) {
            System.out.print(ansiMoveUp(workers.size()));
            for (WorkerState worker : workers) {
                System.out.println(renderLine(worker));
            }
            try {
                Thread.sleep(RENDER_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private static boolean isAnyAlive(List<Thread> threads) {
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                return true;
            }
        }
        return false;
    }

    private static String renderLine(WorkerState state) {
        int progress = state.progress.get();
        int total = state.totalSteps;
        int filled = Math.min(progress, total);
        int percent = Math.round((filled * 100.0f) / total);

        String bar = "[" + "#".repeat(filled) + "-".repeat(total - filled) + "]";
        String threadId = state.threadId == -1 ? "pending" : Long.toString(state.threadId);
        String duration = state.completed ? state.elapsedMillis + " ms" : "...";

        return String.format(
                "Thread #%d | id=%s | %s %3d%% | time=%s",
                state.order,
                threadId,
                bar,
                percent,
                duration
        );
    }

    private static String ansiMoveUp(int lines) {
        return "\u001B[" + lines + "A";
    }

    private static int parsePositiveArg(String[] args, int index, int fallback) {
        if (args.length <= index) {
            return fallback;
        }
        try {
            int value = Integer.parseInt(args[index]);
            return value > 0 ? value : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static final class WorkerState implements Runnable {
        private final int order;
        private final int totalSteps;
        private final AtomicInteger progress = new AtomicInteger(0);
        private volatile long threadId = -1;
        private volatile boolean completed = false;
        private volatile long elapsedMillis = 0;

        private WorkerState(int order, int totalSteps) {
            this.order = order;
            this.totalSteps = totalSteps;
        }

        @Override
        public void run() {
            threadId = Thread.currentThread().threadId();
            long started = System.nanoTime();

            for (int step = 1; step <= totalSteps; step++) {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(MIN_STEP_DELAY_MS, MAX_STEP_DELAY_MS + 1));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                progress.set(step);
            }

            elapsedMillis = Math.round((System.nanoTime() - started) / 1_000_000.0);
            completed = true;
        }
    }
}
