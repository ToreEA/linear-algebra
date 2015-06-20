package no.kantega.bigdata.linearalgebra;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * TODO: Purpose and responsibility
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class FixedTableTest {

    @Test
    public void shouldReturnSize() {
        final int rows = 3;
        final int cols = 5;

        Table table = FixedTable.ofSize(rows, cols);

        assertThat(table.size(), equalTo(Size.of(rows, cols)));
    }

    @Test
    public void shouldGetSameValueAsSet() {
        final int rows = 3;
        final int cols = 5;

        Table table = FixedTable.ofSize(rows, cols);

        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                table.set(i, j, 3.14d);
                assertThat(table.get(i, j), is(3.14d));
            }
        }
    }

    @Test
    public void shouldReturnRowVector() {
        final int rows = 3;
        final int cols = 5;

        Table table = FixedTable.ofSize(rows, cols);
        for (int j = 1; j <= cols; j++) {
            table.set(2, j, 3.14d);
        }

        Array row = table.row(2);

        for (int index = 1; index <= cols; index++) {
            assertThat(row.get(index), is(3.14d));
        }
    }

    @Test
    public void shouldReturnColumnVector() {
        final int rows = 3;
        final int cols = 5;

        Table table = FixedTable.ofSize(rows, cols);
        for (int i = 1; i <= rows; i++) {
            table.set(i, 3, 3.14d);
        }

        Array column = table.column(3);

        for (int index = 1; index <= rows; index++) {
            assertThat(column.get(index), is(3.14d));
        }
    }

    @Test
    public void shouldCreateExactCopy() {
        final int rows = 3;
        final int cols = 5;

        Table table = FixedTable.ofSize(rows, cols);

        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                table.set(i, j, i + j/10.0d);
            }
        }

        Table copy = table.copy();

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

        Table table = FixedTable.ofSize(rows, cols);

        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                table.set(i, j, i + j/10.0d);
            }
        }

        table.transpose();

        for (int i = 1; i <= cols; i++) {
            for (int j = 1; j <= rows; j++) {
                assertThat(table.get(i, j), is(j + i/10.0d));
            }
        }
    }
}