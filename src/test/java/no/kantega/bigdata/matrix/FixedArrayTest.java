package no.kantega.bigdata.matrix;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * TODO: Purpose and responsibility
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class FixedArrayTest {

    @Test
    public void shouldGetSameAsSet() {
        final int size = 5;
        Array array = FixedArray.ofSize(size);
        for (int index = 1; index <= size; index++) {
            array.set(index, 3.14d);
            assertThat(array.get(index), is(3.14d));
        }
    }

    @Test
    public void shouldReturnSize() {
        final int size = 5;
        Array array = FixedArray.ofSize(size);
        assertThat(array.size(), is(size));
    }
}