package no.kantega.bigdata.matrix;

import org.junit.Test;

import java.util.function.Function;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * TODO: Purpose and responsibility
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class MatrixTest {

    @Test
    public void shouldCreateRandomMatrix() {
        Matrix m = Matrix.random(10, 10, -9.9d, +9.9d);
        print("Random matrix:", m);
        assertThat(m.anyMatch((p,v) -> v > 9.9d || v < -9.9d), is(false));
    }

    @Test
    public void shouldCreateZeroMatrix() {
        Matrix m = Matrix.zero(3, 3);
        assertEqualToNoDecimals(m, "0 0 0\n0 0 0\n0 0 0\n");
    }

    @Test
    public void shouldCreateUnitMatrix() {
        Matrix m = Matrix.identity(3, 3);
        assertEqualToNoDecimals(m, "1 0 0\n0 1 0\n0 0 1\n");
    }

    @Test
    public void shouldPrefillWithSingleValue() {
        Matrix m = Matrix.constant(3, 3, 1.0d);
        assertEqualToNoDecimals(m, "1 1 1\n1 1 1\n1 1 1\n");
    }

    @Test
    public void shouldPrefillWithRowWiseValues() {
        Matrix m = Matrix.fromRowMajorSequence(3, 2, 1.1d, 1.2d, 2.1d, 2.2d, 3.1d, 3.2d);
        assertEqualTo(m, "1.1 1.2\n2.1 2.2\n3.1 3.2\n");
    }

    @Test
    public void shouldTranspose() {
        Matrix m = Matrix.fromRowMajorSequence(2, 3, 1.0d, 2.0d, 3.0d, 4.0d, 5.0d, 6.0d).transpose();
        assertEqualToNoDecimals(m, "1 4\n2 5\n3 6\n");
    }

    @Test
    public void shouldMultiply() {
        Matrix a = Matrix.fromRowMajorSequence(2, 3, 2, 1, 4, 1, 5, 2);
        Matrix b = Matrix.fromRowMajorSequence(3, 2, 3, 2, -1, 4, 1, 2);

        Matrix result = a.multiply(b);

        assertEqualToNoDecimals(result, "9 16\n0 26\n");
    }

    @Test
    public void shouldDetectOrthogonality() {
        Matrix m = Matrix.fromRowMajorSequence(3, 3, 1.0d, 0.0d, 0.0d, 0.0d, 3.0d / 5.0d, -4.0d / 5.0d, 0.0d, 4.0d / 5.0d, 3.0d / 5.0d);
        assertThat(m.isOrthogonal(), is(true));
    }

    @Test
    public void shouldPerformRowOpMultiplyConstant() {
        Matrix m = Matrix.fromRowMajorSequence(3, 3, 1, 2, 3, 4, 5, 6, 7, 8, 9).RowOp_multiplyConstant(2, 2);
        assertEqualToNoDecimals(m, "1 2 3\n8 10 12\n7 8 9\n");
    }

    @Test
    public void shouldPerformRowOpSwapRows() {
        Matrix m = Matrix.fromRowMajorSequence(3, 3, 1, 2, 3, 4, 5, 6, 7, 8, 9).RowOp_swapRows(1, 3);
        assertEqualToNoDecimals(m, "7 8 9\n4 5 6\n1 2 3\n");
    }

    @Test
    public void shouldPerformRowOpAddMultipleOfOtherRow() {
        Matrix m = Matrix.fromRowMajorSequence(3, 3, 1, 2, 3, 4, 5, 6, 7, 8, 9).RowOp_addMultipleOfOtherRow(1, 2, 3);
        assertEqualToNoDecimals(m, "15 18 21\n4 5 6\n7 8 9\n");
    }

    @Test
    public void shouldPerformGaussianElimination() {
        Matrix m = Matrix.fromRowMajorSequence(3, 4, 9,3,4,7, 4,3,4,8, 1,1,1,3).gaussianElimination();
        assertEqualToNoDecimals(m, "1 1 1 3\n0 -1 0 -4\n0 0 -5 4\n");
    }

    private void assertEqualTo(Matrix matrix, String expected) {
        String actual = matrix.toString(decimals());
        assertThat(actual, equalTo(expected));
    }

    private void assertEqualToNoDecimals(Matrix matrix, String expected) {
        String actual = matrix.toString(noDecimals());
        assertThat(actual, equalTo(expected));
    }

    private Function<Double, String> noDecimals() {
        return v -> String.format("%.0f", v);
    }

    private Function<Double, String> decimals() {
        return v -> String.format("%.1f", v);
    }

    private void print(String msg, Matrix m) {
        System.out.print(msg + "\n" + m.toString());
    }
}

/*
1    3/9     4/9 7/9
0    3-12/9  4-16/9  8 -
1    1       1   3


 */