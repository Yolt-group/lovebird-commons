package nl.ing.lovebird.cassandrabatch.throttler;

import com.datastax.driver.core.PagingState;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CassandraBatchThrottler {

    private static final int MINIMUM_TPS = 1;
    private static final int ITERATION_TIME_IN_MILLIS = 1000;
    private static final int PAGE_ADJUST_AMOUNT = 100;
    private static final int DEFAULT_MAX_PAGE_SIZE = 10000;
    private static final int LOG_PER_ITERATIONS = 60;
    private final int maxReadsPerSecond;

    public CassandraBatchThrottler(final int maxReadsPerSecond) {
        if (maxReadsPerSecond < MINIMUM_TPS) {
            throw new IllegalArgumentException(String.format("Minimum maxReadsPerSecond is %s", MINIMUM_TPS));
        }
        this.maxReadsPerSecond = maxReadsPerSecond;
    }

    public void startBatch(final ThrottledTask task) {
        try {
            executeBatch(task);
        } catch (RuntimeException e) {
            log.error("Batch failed with exception.", e);
        }
    }

    private void executeBatch(final ThrottledTask task) {
        PagingState pagingState = null;
        boolean keepGoing = true;
        int totalProcessed = 0;
        int totalFixed = 0;
        int pageSize = Math.min(maxReadsPerSecond, DEFAULT_MAX_PAGE_SIZE);

        final int permitsPerSecond = 1;
        final RateLimiter rateLimiter = RateLimiter.create(permitsPerSecond);

        log.info("Starting batch with a maximum of {} reads per second.", maxReadsPerSecond);
        int iterations = 0;
        while (keepGoing) {
            final long startTime = System.currentTimeMillis();
            final ThrottledTaskResult throttleTaskResult = task.execute(pagingState, pageSize);
            final long runtime = System.currentTimeMillis() - startTime;

            pagingState = throttleTaskResult.getPagingState();

            if (runtime > ITERATION_TIME_IN_MILLIS) {
                pageSize = decreasePageSize(pageSize);
            } else if (pageSize > maxReadsPerSecond) {
                pageSize = decreasePageSize(pageSize);
            } else if (pageSize < maxReadsPerSecond) {
                pageSize = increasePageSize(pageSize);
            }
            if (pageSize <= 0) {
                pageSize = 1;
            }

            if (pagingState == null) {
                keepGoing = false;
            }

            totalProcessed += throttleTaskResult.getProcessedRecords();
            totalFixed += throttleTaskResult.getFixedRecords();

            if (iterations % LOG_PER_ITERATIONS == 0) {
                log.info("Total so far: {}, fixed: {} Current page size: {}", totalProcessed, totalFixed, pageSize);
            }

            iterations++;
            rateLimiter.acquire();
        }
        log.info("Finished fetching {} rows. Fixed {} rows.", totalProcessed, totalFixed);
    }

    private int increasePageSize(int pageSize) {
        final int pageAdjustSize = getPageAdjustSize(pageSize);
        pageSize += pageAdjustSize;
        return pageSize;
    }

    private int decreasePageSize(int pageSize) {
        final int pageAdjustSize = getPageAdjustSize(pageSize);
        pageSize -= pageAdjustSize;
        return pageSize;
    }

    private int getPageAdjustSize(final int pageSize) {
        final int pageAdjustAmount;
        if (pageSize < PAGE_ADJUST_AMOUNT) {
            pageAdjustAmount = 1;
        } else {
            pageAdjustAmount = PAGE_ADJUST_AMOUNT;
        }
        return pageAdjustAmount;
    }

}
