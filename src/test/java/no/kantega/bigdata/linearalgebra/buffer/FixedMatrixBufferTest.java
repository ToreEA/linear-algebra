package no.kantega.bigdata.linearalgebra.buffer;

import no.kantega.bigdata.linearalgebra.Size;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Unit test for the FixedMatrixBuffer class
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class FixedMatrixBufferTest {

    @Test
    public void shouldReturnSize() {
        final int rows = 3;
        final int cols = 5;

        MatrixBuffer buffer = FixedMatrixBuffer.allocate(rows, cols);

        assertThat(buffer.size(), equalTo(Size.of(rows, cols)));
    }

    @Test
    public void shouldGetSameValueAsSet() {
        final int rows = 3;
        final int cols = 5;

        MatrixBuffer buffer = FixedMatrixBuffer.allocate(rows, cols);

        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                buffer.set(i, j, 3.14d);
                assertThat(buffer.get(i, j), is(3.14d));
            }
        }
    }

    @Test
    public void shouldReturnRowVector() {
        final int rows = 3;
        final int cols = 5;

        MatrixBuffer buffer = FixedMatrixBuffer.allocate(rows, cols);
        for (int j = 1; j <= cols; j++) {
            buffer.set(2, j, 3.14d);
        }

        VectorBuffer row = buffer.row(2);

        for (int index = 1; index <= cols; index++) {
            assertThat(row.get(index), is(3.14d));
        }
    }

    @Test
    public void shouldReturnColumnVector() {
        final int rows = 3;
        final int cols = 5;

        MatrixBuffer buffer = FixedMatrixBuffer.allocate(rows, cols);
        for (int i = 1; i <= rows; i++) {
            buffer.set(i, 3, 3.14d);
        }

        VectorBuffer column = buffer.column(3);

        for (int index = 1; index <= rows; index++) {
            assertThat(column.get(index), is(3.14d));
        }
    }

    @Test
    public void shouldCreateExactCopy() {
        final int rows = 3;
        final int cols = 5;

        MatrixBuffer buffer = FixedMatrixBuffer.allocate(rows, cols);

        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                buffer.set(i, j, i + j/10.0d);
            }
        }

        MatrixBuffer copy = buffer.copy();

        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                assertThat(copy.get(i, j), is(i + j/10.0d));
            }
        }
    }

    @Test
    public void shouldSwapIndicesWhenTransposing() {
        final int rows = 3;
        final int cols = 5;

        MatrixBuffer buffer = FixedMatrixBuffer.allocate(rows, cols);

        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                buffer.set(i, j, i + j/10.0d);
            }
        }

        buffer.transpose();

        for (int i = 1; i <= cols; i++) {
            for (int j = 1; j <= rows; j++) {
                assertThat(buffer.get(i, j), is(j + i/10.0d));
            }
        }
    }
}