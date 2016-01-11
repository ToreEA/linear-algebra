package no.kantega.bigdata.linearalgebra.buffer;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Unit test for the FixedVectorBuffer class
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class FixedVectorBufferTest {

    @Test
    public void shouldGetSameAsSet() {
        final int size = 5;
        VectorBuffer buffer = FixedVectorBuffer.allocate(size);
        for (int index = 0; index < size; index++) {
            buffer.set(index, 3.14d);
            assertThat(buffer.get(index), is(3.14d));
        }
    }

    @Test
    public void shouldReturnSize() {
        final int size = 5;
        VectorBuffer buffer = FixedVectorBuffer.allocate(size);
        assertThat(buffer.size(), is(size));
    }
}