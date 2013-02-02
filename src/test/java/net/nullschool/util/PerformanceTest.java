package net.nullschool.util;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;


/**
 * 2013-02-01<p/>
 *
 * Some micro-benchmarks, represented as Tasks that are executed and measured through a common harness.
 * Run with "-Xms1G -Xmx2G -XX:+PrintCompilation -XX:+PrintGCDetails"
 *
 * Released to the public domain: http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Cameron Beccario
 */
public class PerformanceTest {

    private static final long NANOS_PER_SECOND = 1000000000;
    private static final long BYTES_PER_MEGABYTE = 1024 * 1024;

    interface Task extends Callable<Void> {

        int getByteCount();
    }

    /**
     * Generates lots of longs using {@link Random}.
     */
    private static class LongRandomTask implements Task {

        private static final int SAMPLES = 20000000;
        public int getByteCount() { return SAMPLES * 8; }
        private final Random random = new Random();

        public Void call() throws Exception {
            for (int i = 0; i < SAMPLES; i++) {
                random.nextLong();
            }
            return null;
        }
    }

    /**
     * Generates lots of longs using {@link SecureRandom}.
     */
    private static class LongSecureRandomTask implements Task {

        private static final int SAMPLES = 2000000;
        public int getByteCount() { return SAMPLES * 8; }
        private final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        LongSecureRandomTask() throws Exception {
        }

        public Void call() throws Exception {
            for (int i = 0; i < SAMPLES; i++) {
                random.nextLong();
            }
            return null;
        }
    }

    /**
     * Generates lots of longs using {@link DigitalRandom}.
     */
    private static class LongDigitalRandomTask implements Task {

        private static final int SAMPLES = 20000000;
        public int getByteCount() { return SAMPLES * 8; }
        private final DigitalRandom random = new DigitalRandom();

        public Void call() throws Exception {
            for (int i = 0; i < SAMPLES; i++) {
                random.nextLong();
            }
            return null;
        }
    }

    /**
     * Generates lots of longs using {@link ThreadLocalRandom}.
     */
    private static class LongThreadLocalRandomTask implements Task {

        private static final int SAMPLES = 200000000;
        public int getByteCount() { return SAMPLES * 8; }

        public Void call() throws Exception {
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int i = 0; i < SAMPLES; i++) {
                random.nextLong();
            }
            return null;
        }
    }

    /**
     * Fills a large byte array many times over using {@link Random}.
     */
    private static class BytesRandomTask implements Task {

        private static final int SAMPLES = 50;
        private static final int ARRAY_SIZE = 2 * 1024 * 1024;
        public int getByteCount() { return SAMPLES * ARRAY_SIZE; }
        private final Random random = new Random();

        public Void call() throws Exception {
            byte[] bytes = new byte[ARRAY_SIZE];
            for (int i = 0; i < SAMPLES; i++) {
                random.nextBytes(bytes);
            }
            return null;
        }
    }

    /**
     * Fills a large byte array many times over using {@link SecureRandom}.
     */
    private static class BytesSecureRandomTask implements Task {

        private static final int SAMPLES = 25;
        private static final int ARRAY_SIZE = 2 * 1024 * 1024;
        public int getByteCount() { return SAMPLES * ARRAY_SIZE; }
        private final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        BytesSecureRandomTask() throws Exception {
        }

        public Void call() throws Exception {
            byte[] bytes = new byte[ARRAY_SIZE];
            for (int i = 0; i < SAMPLES; i++) {
                random.nextBytes(bytes);
            }
            return null;
        }
    }

    /**
     * Fills a large byte array many times over using {@link DigitalRandom}.
     */
    private static class BytesDigitalRandomTask implements Task {

        private static final int SAMPLES = 50;
        private static final int ARRAY_SIZE = 2 * 1024 * 1024;
        public int getByteCount() { return SAMPLES * ARRAY_SIZE; }
        private final DigitalRandom random = new DigitalRandom();

        public Void call() throws Exception {
            byte[] bytes = new byte[ARRAY_SIZE];
            for (int i = 0; i < SAMPLES; i++) {
                random.nextBytes(bytes);
            }
            return null;
        }
    }

    /**
     * Fills a large byte array many times over using {@link ThreadLocalRandom}.
     */
    private static class BytesThreadLocalRandomTask implements Task {

        private static final int SAMPLES = 100;
        private static final int ARRAY_SIZE = 2 * 1024 * 1024;
        public int getByteCount() { return SAMPLES * ARRAY_SIZE; }

        public Void call() throws Exception {
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            byte[] bytes = new byte[ARRAY_SIZE];
            for (int i = 0; i < SAMPLES; i++) {
                random.nextBytes(bytes);
            }
            return null;
        }
    }

    /**
     * Generates lots of UUIDs using {@link DigitalRandom}.
     */
    private static class DigitalRandomUUIDTask implements Task {

        private static final int SAMPLES = 3000000;
        public int getByteCount() { return SAMPLES * 16; }
        private final DigitalRandom random = new DigitalRandom();

        public Void call() throws Exception {
            for (int i = 0; i < SAMPLES; i++) {
                random.nextUUID();
            }
            return null;
        }
    }

    /**
     * Generates lots of UUIDs using {@link UUID#randomUUID()}.
     */
    private static class StandardUUIDTask implements Task {

        private static final int SAMPLES = 300000;
        public int getByteCount() { return SAMPLES * 16; }

        public Void call() throws Exception {
            for (int i = 0; i < SAMPLES; i++) {
                UUID.randomUUID();
            }
            return null;
        }
    }

    /**
     * The main benchmark harness. Measures only one kind of task. Choose the task to measure below.
     */
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newCachedThreadPool();
        final int CORES = Runtime.getRuntime().availableProcessors();
        final int MAX_RUNS = 40;

        DescriptiveStatistics[] stats = new DescriptiveStatistics[CORES];
        for (int i = 0; i < stats.length; i++) {
            // Measure only the latter half of runs, allowing the first half to warm up the JVM.
            stats[i] = new DescriptiveStatistics(MAX_RUNS / 2);
        }

        for (int run = 0; run < MAX_RUNS; run++) {
            System.out.println("Run " + run + "...");

            // Execute one task, then two concurrently, then three concurrently, and so on.
            for (int threads = 1; threads <= CORES; threads++) {

                // Create fresh tasks to execute concurrently, one per thread.
                List<Task> tasks = new ArrayList<>();
                for (int i = 0; i < threads; i++) {
                    tasks.add(new LongRandomTask());  // <-- Measure this task type.
                }

                // Execute tasks in a thread pool. Wait for all to finish. Measure the time this takes.
                long start = System.nanoTime();
                for (Future<Void> future : executor.invokeAll(tasks)) {
                    future.get();
                }
                long nanos = System.nanoTime() - start;

                Task task = tasks.get(0);

                // Calculate MiB/s
                double mbps =
                    ((double)threads * task.getByteCount() / BYTES_PER_MEGABYTE) /
                    ((double)nanos / NANOS_PER_SECOND);
                // System.out.println(threads + " " + mbps);
                stats[threads-1].addValue(mbps);
            }
        }

        executor.shutdown();

        // Print the results.
        for (int i = 0; i < stats.length; i++) {
            DescriptiveStatistics stat = stats[i];
            System.out.println(String.format(
                "Threads: %s, MiB/sec: mean=%s, max=%s, min=%s, std=%s",
                i + 1,
                (long)stat.getMean(),
                (long)stat.getMax(),
                (long)stat.getMin(),
                (long)stat.getStandardDeviation()));
        }
    }
}
