package de.viadee.sonarIssueScoring.service.misc;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.util.concurrent.Uninterruptibles;

public class ParallelismManagerTest {
    @Test
    public void testSingleCycle() throws ExecutionException, InterruptedException {
        AtomicInteger order = new AtomicInteger();

        ParallelismManager mgr = new ParallelismManager();

        ExecutorService executor = Executors.newFixedThreadPool(3);

        Future<Optional<Integer>> futureRegularFirst = executor.submit(() -> mgr.runIfNotAlreadyWaiting("keyA", () -> {
            Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
            return order.incrementAndGet();
        }));

        Uninterruptibles.sleepUninterruptibly(20, TimeUnit.MILLISECONDS);

        //KeyA is already running, should not return anything
        Future<Optional<Integer>> futureBlocked = executor.submit(() -> mgr.runIfNotAlreadyWaiting("keyA", order::incrementAndGet));
        //KeyB can run after keyA
        Future<Optional<Integer>> futureRegularSecond = executor.submit(() -> mgr.runIfNotAlreadyWaiting("keyB", order::incrementAndGet));

        Assert.assertEquals(Optional.of(1), futureRegularFirst.get());
        Assert.assertEquals(Optional.of(2), futureRegularSecond.get());
        Assert.assertEquals(Optional.empty(), futureBlocked.get());

        executor.shutdown();
    }
}