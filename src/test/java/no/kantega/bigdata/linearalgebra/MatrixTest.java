package no.kantega.bigdata.linearalgebra;

import no.kantega.bigdata.linearalgebra.algorithms.LUDecompositionResult;
import no.kantega.bigdata.linearalgebra.utils.NumberFormatter;
import org.junit.Test;

import static no.kantega.bigdata.linearalgebra.matcher.MatrixIsCloseTo.closeToMatrix;
import static no.kantega.bigdata.linearalgebra.matcher.VectorIsCloseTo.closeToVector;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.*;

/**
 * Unit test for the Matrix class
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class MatrixTest {

    public static final double EPSILON = 0.000000000001;

    @Test
    public void shouldCreateRandomMatrix() {
        Matrix m = Matrix.random(10, 10, -9.9d, +9.9d);
        //print("Random matrix:", m);
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
    public void shouldAdd() {
        Matrix a = Matrix.fromRowMajorSequence(3, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        Matrix b = Matrix.fromRowMajorSequence(3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2);

        Matrix result = a.add(b);

        assertEqualToNoDecimals(result, "3 3 3\n3 3 3\n3 3 3\n");
    }

    @Test
    public void shouldDetectOrthogonality() {
        Matrix m = Matrix.fromRowMajorSequence(3, 3, 1.0d, 0.0d, 0.0d, 0.0d, 3.0d / 5.0d, -4.0d / 5.0d, 0.0d, 4.0d / 5.0d, 3.0d / 5.0d);
        assertThat(m.isOrthogonal(), is(true));
    }

    @Test
    public void shouldPerformRowOpMultiplyConstant() {
        Matrix m = Matrix.fromRowMajorSequence(3, 3, 1, 2, 3, 4, 5, 6, 7, 8, 9).rowOp_multiplyConstant(1, 2);
        assertEqualToNoDecimals(m, "1 2 3\n8 10 12\n7 8 9\n");
    }

    @Test
    public void shouldPerformRowOpSwapRows() {
        Matrix m = Matrix.fromRowMajorSequence(3, 3, 1, 2, 3, 4, 5, 6, 7, 8, 9).rowOp_swapRows(0, 2);
        assertEqualToNoDecimals(m, "7 8 9\n4 5 6\n1 2 3\n");
    }

    @Test
    public void shouldPerformRowOpAddMultipleOfOtherRow() {
        Matrix m = Matrix.fromRowMajorSequence(3, 3, 1, 2, 3, 4, 5, 6, 7, 8, 9).rowOp_addMultipleOfOtherRow(0, 2, 2);
        assertEqualToNoDecimals(m, "15 18 21\n4 5 6\n7 8 9\n");
    }

    @Test
    public void shouldPerformGaussianElimination() {
        Matrix m = Matrix.fromRowMajorSequence(3, 4, 2,1,1,5, 4,-6,0,-2, -2,7,2,9).gaussianElimination();
        assertEqualToNoDecimals(m, "4 -6 0 -2\n0 4 1 6\n0 0 1 2\n");
    }

    @Test
    public void shouldPerformGaussJordanElimination() {
        Matrix m = Matrix.fromRowMajorSequence(3, 6, 4,3,2,1,0,0, 5,6,3,0,1,0, 3,5,2,0,0,1).gaussJordanElimination();
        assertEqualToNoDecimals(m, "1 0 0 3 -4 3\n0 1 0 1 -2 2\n0 0 1 -7 11 -9\n");
    }

    @Test
    public void shouldPerformLUDecomposition() {
        Matrix a1 = Matrix.fromRowMajorSequence(3, 3, 1,2,4, 3,8,14, 2,6,13);
        LUDecompositionResult lud1 = a1.luDecomposition();
        assertLUDecomposition("a1", lud1, a1, 6.0d);

        Matrix a2 = Matrix.fromRowMajorSequence(4, 4, 6,1,-6,-5, 2,2,3,2, 4,-3,0,1, 0,2,0,1);
        LUDecompositionResult lud2 = a2.luDecomposition();
        assertLUDecomposition("a2", lud2, a2, 234.0d);

        Matrix a3 = Matrix.fromRowMajorSequence(6, 6, 1,1,-2,1,3,-1, 2,-1,1,2,1,-3, 1,3,-3,-1,2,1, 5,2,-1,-1,2,1, -3,-1,2,3,1,3, 4,3,1,-6,-3,-2);
        LUDecompositionResult lud3 = a3.luDecomposition();
        assertLUDecomposition("a3", lud3, a3, -852.0d);
    }

    @Test
    public void shouldSolveLinearEquationsUsingLUD() {
        // System of 3 linear equations with 4 unknowns
        Matrix a1 = Matrix.fromRowMajorSequence(3, 3, 1,2,4, 3,8,14, 2,6,13);
        LUDecompositionResult lud1 = a1.luDecomposition();

        Vector b1 = Vector.of(4.0d, 12.0d, 11.0d);
        Vector solution1 = Vector.of(2.0d, -1.0d, 1.0d);
        Vector x1 = lud1.solve(b1);
        assertThat("x1", x1, closeToVector(solution1, EPSILON));

        // System of 4 linear equations with 4 unknowns
        Matrix a2 = Matrix.fromRowMajorSequence(4, 4, 6,1,-6,-5, 2,2,3,2, 4,-3,0,1, 0,2,0,1);
        LUDecompositionResult lud2 = a2.luDecomposition();

        Vector b2 = Vector.of(6.0d, -2.0d, -7.0d, 0.0d);
        Vector solution2 = Vector.of(-0.5d, 1.0d, 0.3333333333333d, -2.0d);
        Vector x2 = lud2.solve(b2);
        assertThat("x2", x2, closeToVector(solution2, EPSILON));

        // System of 6 linear equations with 6 unknowns
        Matrix a3 = Matrix.fromRowMajorSequence(6, 6, 1,1,-2,1,3,-1, 2,-1,1,2,1,-3, 1,3,-3,-1,2,1, 5,2,-1,-1,2,1, -3,-1,2,3,1,3, 4,3,1,-6,-3,-2);
        LUDecompositionResult lud3 = a3.luDecomposition();

        Vector b3 = Vector.of(4.0d, 20.0d, -15.0d, -3.0d, 16.0, -27.0);
        Vector solution3 = Vector.of(1.0d, -2.0d, 3.0d, 4.0d, 2.0d, -1.0d);
        Vector x3 = lud3.solve(b3);
        assertThat("x3", x3, closeToVector(solution3, EPSILON));
    }

    @Test
    public void shouldComputeDeterminantOfRegularMatrix() {
        Matrix m = Matrix.fromRowMajorSequence(3, 3, -2,2,-3, -1,1,3, 2,0,-1);
        assertThat(m.determinant(), is(18.0d));
    }

    @Test
    public void shouldComputeDeterminantOfTriangularMatrix() {
        Matrix m = Matrix.fromRowMajorSequence(3, 3, 4,-6,0, 0,4,1, 0,0,1);
        assertThat(m.determinant(), is(4.0d * 4.0d * 1.0d));
    }

    @Test
    public void shouldComputeDeterminantOfBigMatrices() {
        Matrix m1 = Matrix.fromRowMajorSequence(4, 4, 6,1,-6,-5, 2,2,3,2, 4,-3,0,1, 0,2,0,1);
        assertThat(m1.determinant(), closeTo(234.0d, EPSILON));

        Matrix m2 = Matrix.fromRowMajorSequence(6, 6, 1,1,-2,1,3,-1, 2,-1,1,2,1,-3, 1,3,-3,-1,2,1, 5,2,-1,-1,2,1, -3,-1,2,3,1,3, 4,3,1,-6,-3,-2);
        assertThat(m2.determinant(), closeTo(-852.0d, EPSILON));
    }

    @Test
    public void shouldComputeInverse() {
        Matrix m1 = Matrix.fromRowMajorSequence(2, 2, 1,2, 3,4);
        assertInverseMatrix("m1", m1);

        Matrix m2 = Matrix.fromRowMajorSequence(3, 3, 1,2,4, 3,8,14, 2,6,13);
        assertInverseMatrix("m2", m2);

        Matrix m3 = Matrix.fromRowMajorSequence(4, 4, 6,1,-6,-5, 2,2,3,2, 4,-3,0,1, 0,2,0,1);
        assertInverseMatrix("m3", m3);
    }

    @Test
    public void shouldDetectEqualMatrix() {
        Matrix m = Matrix.fromRowMajorSequence(3, 3, 9,3,4, 7,4,3, 4,8,6);
        Matrix n = Matrix.fromRowMajorSequence(3, 3, 9,3,4, 7,4,3, 4,8,6);
        assertThat(m, equalTo(n));
    }

    private void assertEqualTo(Matrix matrix, String expected) {
        String actual = matrix.toString(NumberFormatter.compact());
        assertThat(actual, equalTo(expected));
    }

    private void assertEqualToNoDecimals(Matrix matrix, String expected) {
        String actual = matrix.toString(NumberFormatter.compactNoDecimals());
        assertThat(actual, equalTo(expected));
    }

    private void assertInverseMatrix(String message, Matrix m) {
        Matrix inv = m.invert();
        Matrix pr = m.multiply(inv);
        assertThat(message, pr, closeToMatrix(Matrix.identity(m.size().rows(), m.size().cols()), EPSILON));
    }

    private void assertLUDecomposition(String message, LUDecompositionResult lud, Matrix a, double detA) {
        //print("A : ", a);

        //print("L : ", lud.getL());
        //print("U : ", lud.getU());

        Matrix lu = lud.lowerMatrix().multiply(lud.getU());
        //print("LU : ", lu);

        Matrix pa = lud.permutationMatrix().multiply(a);
        //print("PA : ", pa);

        assertThat(message, pa, closeToMatrix(lu, EPSILON));
        assertThat(message, lud.determinant(), closeTo(detA, EPSILON));
    }

    private void print(String msg, Matrix m) {
        System.out.print(msg + "\n" + m.toString());
    }
}
