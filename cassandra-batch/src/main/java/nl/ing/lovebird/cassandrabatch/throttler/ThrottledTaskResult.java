package nl.ing.lovebird.cassandrabatch.throttler;

import com.datastax.driver.core.PagingState;
import lombok.Data;

@Data
public class ThrottledTaskResult {
    private final PagingState pagingState;
    private final int processedRecords;
    private final int fixedRecords;
}
