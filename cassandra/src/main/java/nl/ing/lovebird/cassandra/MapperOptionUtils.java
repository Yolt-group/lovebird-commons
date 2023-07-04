package nl.ing.lovebird.cassandra;

import com.datastax.driver.mapping.Mapper;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class MapperOptionUtils {

    private MapperOptionUtils() {
    }

    public static Mapper.Option[] withTracing(final Mapper.Option... options) {
        final Mapper.Option tracing = Mapper.Option.tracing(true);
        if (options == null) {
            return new Mapper.Option[]{tracing};
        }

        final Set<Mapper.Option> optionsList = Arrays.stream(options).collect(Collectors.toSet());
        optionsList.add(tracing);
        return optionsList.toArray(new Mapper.Option[0]);
    }
}
