package nl.ing.lovebird.cassandrabatch.throttler;

import com.datastax.driver.core.PagingState;

@FunctionalInterface
public interface ThrottledTask {
    ThrottledTaskResult execute(PagingState pagingState, int pageSize);
}
