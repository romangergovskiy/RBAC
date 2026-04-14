package rbac.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class AuditLog {
    private final List<AuditEntry> entries = Collections.synchronizedList(new ArrayList<>());
    private final BlockingQueue<AuditEntry> queue = new LinkedBlockingQueue<>();
    private final AtomicLong enqueued = new AtomicLong(0);
    private final AtomicLong processed = new AtomicLong(0);
    private final Thread worker;
    private volatile boolean running = true;

    public AuditLog() {
        worker = new Thread(this::runWorker, "audit-log-worker");
        worker.setDaemon(true);
        worker.start();
    }

    private void runWorker() {
        while (running || !queue.isEmpty()) {
            try {
                AuditEntry e = queue.poll(250, TimeUnit.MILLISECONDS);
                if (e == null) continue;
                entries.add(e);
                processed.incrementAndGet();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable ignored) {
                // keep worker alive; caller can still read already processed entries
            }
        }
    }

    private void awaitProcessed(long expected, long timeoutMs) {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
        while (processed.get() < expected && System.nanoTime() < deadline) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void flushPending() {
        long expected = enqueued.get();
        if (processed.get() >= expected) return;
        awaitProcessed(expected, 1_000);
    }

    public void log(String action, String performer, String target, String details) {
        String ts = DateUtils.getCurrentDateTime();
        AuditEntry entry = new AuditEntry(
            ts,
            action,
            performer != null ? performer : "system",
            target != null ? target : "-",
            details != null ? details : ""
        );
        enqueued.incrementAndGet();
        queue.offer(entry);
    }

    public List<AuditEntry> getAll() {
        flushPending();
        synchronized (entries) {
            return new ArrayList<>(entries);
        }
    }

    public List<AuditEntry> getByPerformer(String performer) {
        flushPending();
        if (performer == null) return List.of();
        synchronized (entries) {
            return entries.stream().filter(e -> performer.equals(e.performer())).collect(Collectors.toList());
        }
    }

    public List<AuditEntry> getByAction(String action) {
        flushPending();
        if (action == null) return List.of();
        synchronized (entries) {
            return entries.stream().filter(e -> action.equals(e.action())).collect(Collectors.toList());
        }
    }

    public void printLog() {
        flushPending();
        System.out.println(FormatUtils.formatHeader("Audit log"));
        List<AuditEntry> snapshot = getAll();
        for (AuditEntry e : snapshot) {
            System.out.printf("[%s] %s by %s on %s — %s%n",
                e.timestamp(), e.action(), e.performer(), e.target(), e.details());
        }
        if (snapshot.isEmpty()) System.out.println("(empty)");
    }

    public void saveToFile(String filename) throws IOException {
        Objects.requireNonNull(filename, "filename");
        flushPending();
        StringBuilder sb = new StringBuilder();
        for (AuditEntry e : getAll()) {
            sb.append(e.timestamp()).append("\t").append(e.action()).append("\t").append(e.performer()).append("\t")
                .append(e.target().replace("\t", " ")).append("\t").append(e.details().replace("\t", " ")).append("\n");
        }
        Files.writeString(Path.of(filename), sb.toString());
    }

    public void shutdown() {
        running = false;
        worker.interrupt();
        flushPending();
    }
}
