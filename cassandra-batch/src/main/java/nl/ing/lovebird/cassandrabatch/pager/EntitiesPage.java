package nl.ing.lovebird.cassandrabatch.pager;


import com.datastax.driver.core.PagingState;
import lombok.Data;

import java.util.List;

@Data
public class EntitiesPage<T> {
    private final List<T> entities;
    private final PagingState pagingState;
}
