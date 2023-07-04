package nl.ing.lovebird.cassandra;

import com.datastax.driver.mapping.Mapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MapperOptionUtilsTest {

    @Test
    void whenNoVarargs_shouldReturnTracing() {
        Mapper.Option[] options = MapperOptionUtils.withTracing();

        assertThat(options).containsExactlyInAnyOrder(Mapper.Option.tracing(true));
    }

    @Test
    void whenVarargsNull_shouldReturnTracing() {
        Mapper.Option[] options = MapperOptionUtils.withTracing(null);

        assertThat(options).containsExactlyInAnyOrder(Mapper.Option.tracing(true));
    }

    @Test
    void whenEmptyArray1_shouldReturnTracing() {
        Mapper.Option[] options = MapperOptionUtils.withTracing(new Mapper.Option[0]);

        assertThat(options).containsExactlyInAnyOrder(Mapper.Option.tracing(true));
    }

    @Test
    void whenEmptyArray2_shouldReturnTracing() {
        Mapper.Option[] options = MapperOptionUtils.withTracing(new Mapper.Option[]{});

        assertThat(options).containsExactlyInAnyOrder(Mapper.Option.tracing(true));
    }

    @Test
    void whenOnlyHasTracing_shouldReturnTracing() {
        Mapper.Option[] options = MapperOptionUtils.withTracing(Mapper.Option.tracing(true));

        assertThat(options).containsExactlyInAnyOrder(Mapper.Option.tracing(true));
    }

    @Test
    void whenSingleElementInArray_shouldAddTracing() {
        Mapper.Option[] options = MapperOptionUtils.withTracing(Mapper.Option.ttl(10));

        assertThat(options).containsExactlyInAnyOrder(Mapper.Option.tracing(true), Mapper.Option.ttl(10));
    }

    @Test
    void whenMultipleElementsInArray_shouldAddTracing() {
        Mapper.Option[] options = MapperOptionUtils.withTracing(Mapper.Option.ttl(10), Mapper.Option.timestamp(100));

        assertThat(options).containsExactlyInAnyOrder(
                Mapper.Option.timestamp(100), Mapper.Option.tracing(true), Mapper.Option.ttl(10));
    }

    @Test
    void whenAlreadyHasTracing_shouldReturnInitialArray() {
        Mapper.Option[] options = MapperOptionUtils.withTracing(Mapper.Option.ttl(10), Mapper.Option.tracing(true));

        assertThat(options).containsExactlyInAnyOrder(Mapper.Option.tracing(true), Mapper.Option.ttl(10));
    }
}
