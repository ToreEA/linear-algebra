// This software is produced by Statens vegvesen. Unauthorized redistribution,
// reproduction or usage of this software in whole or in part without the
// express written consent of Statens vegvesen is strictly prohibited.
// Copyright © 2015 Statens vegvesen
// ALL RIGHTS RESERVED
package no.kantega.bigdata.linearalgebra;

import no.kantega.bigdata.linearalgebra.buffer.FixedMatrixBuffer;
import no.kantega.bigdata.linearalgebra.buffer.MatrixBuffer;
import no.kantega.bigdata.linearalgebra.utils.NumberFormatter;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static no.kantega.bigdata.linearalgebra.utils.Assert.precondition;
import static no.kantega.bigdata.linearalgebra.utils.Assert.require;
import static no.kantega.bigdata.linearalgebra.utils.StreamUtils.asStream;

/**
 * Implements a matrix
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class Matrix {
    private Size size;
    private final MatrixBuffer elements;

    public static Matrix identity(int rows, int cols) {
        require(() -> rows == cols, "The identity matrix should be a square matrix");
        return new Matrix(rows, cols).transform((p, v) -> p.isOnDiagonal() ? 1.0d : 0.0d);
    }

    public static Matrix zero(int rows, int cols) {
        return constant(rows, cols, 0.0d);
    }

    public static Matrix fromRowMajorSequence(int rows, int cols, double... values) {
        requireNonNull(values, "values can't be null");
        require(() -> rows * cols == values.length, "values array must contain exactly %d x %d elements", rows, cols);

        return new Matrix(rows, cols).transform((p, v) -> values[(p.row() - 1) * cols + (p.col() - 1)]);
    }

    public static Matrix constant(int rows, int cols, double value) {
        return new Matrix(rows, cols).populate(() -> value);
    }

    public static Matrix random(int rows, int cols, double minValue, double maxValue) {
        return new Matrix(rows, cols).populate(() -> minValue + Math.random() * (maxValue - minValue));
    }

    private Matrix(int rows, int cols) {
        this.size = Size.of(rows, cols);
        this.elements = FixedMatrixBuffer.allocate(rows, cols);
    }

    private Matrix(MatrixBuffer elements) {
        requireNonNull(elements, "elements can't be null");
        this.size = elements.size();
        this.elements = elements;
    }

    public Size size() {
        return size;
    }

    public boolean isSquare() {
        return size().rows() == size().cols();
    }

    public boolean isIdentity() {
        return isSquare() && !anyMatch((p,v) -> p.isOnDiagonal() ? v != 1.0d : v != 0.0d);
    }

    /**
     * A zero matrix is a matrix with all of its entries equal to zero.
     */
    public boolean isZero() {
        return !anyMatch((p,v) -> v != 0.0d);
    }

    /**
     * A diagonal matrix is a symmetric matrix with all of its entries equal to zero except may be the ones on the diagonal.
     */
    public boolean isDiagonal() {
        return isSquare() && !anyMatch((p,v) -> p.isOnDiagonal() ? v == 0.0d : v != 0.0d);
    }

    /**
     * A symmetrical matrix is identical to its transpose. Thus, only square matrices can be symmetrical.
     */
    public boolean isSymmetrical() {
        return isSquare() && !lowerTriangularPositions().anyMatch((p) -> elements.get(p.row(), p.col()) != elements.get(p.col(), p.row()));
    }

    public boolean isTriangular() {
        return isUpperTriangular() || isLowerTriangular();
    }

    public boolean isUpperTriangular() {
        return isSquare() && !anyMatch((p,v) -> p.isBelowDiagonal() && v != 0.0d);
    }

    public boolean isLowerTriangular() {
        return isSquare() && !anyMatch((p,v) -> p.isAboveDiagonal() && v != 0.0d);
    }

    // AxAt = I?
    public boolean isOrthogonal() {
        return this.multiply(copy().transpose()).isIdentity();
    }

    /**
     * A square matrix that is not invertible is called singular or degenerate.
     * A square matrix is singular if and only if its determinant is 0.
     */
    public boolean isInvertable() {
        return isSquare() && determinant() != 0.0d;
    }

    // 3x3
    public Matrix invert() {
        return null;
    }

    /**
     * Determinants are mainly used as a theoretical tool. They are rarely calculated explicitly in numerical linear algebra,
     * where for applications like checking invertibility and finding eigenvalues the determinant has largely been supplanted
     * by other techniques. Nonetheless, explicitly calculating determinants is required in some situations.
     *
     * Naive methods of implementing an algorithm to compute the determinant include using the Leibniz formula or Laplace's formula.
     * Both these approaches are extremely inefficient for large matrices, requiring close to n! operations. A more efficient method
     * is using the LU decomposition, the QR decomposition and others.
     *
     * Basic properties of the derminant are:
     * 1) det(I) = 1
     * 2) det(A^T) = det(A)
     * 3) det(A^-1) = det(A)^-1
     * 4) det(AB) = det(A)det(B)
     *
     * Other important properties of the determinant include the following, which are invariant under elementary row
     * and column operations:
     * 1. Switching two rows or columns changes the sign.
     * 2. Scalars can be factored out from rows and columns.
     * 3. Multiples of rows and columns can be added together without changing the determinant's value.
     * 4. Scalar multiplication of a row by a constant c multiplies the determinant by c.
     * 5. A determinant with a row or column of zeros has value 0.
     * 6. Any determinant with two rows or columns equal has value 0.
     */
    public double determinant() {
        precondition(this::isSquare, "The determinant can be computed on a square matrix only");

        if (size.rows() == 2 && size.cols() == 2) {
            // ad - bc
            return elements.get(1, 1) * elements.get(2, 2)
                    - elements.get(1, 2) * elements.get(2, 1);
        } else if (size.rows() == 3 && size.cols() == 3) {
            // aei - afh - bdi + bfg + cdh -ceg
            return elements.get(1, 1) * elements.get(2, 2) * elements.get(3, 3)
                    - elements.get(1, 1) * elements.get(2, 3) * elements.get(3, 2)
                    - elements.get(1, 2) * elements.get(2, 1) * elements.get(3, 3)
                    + elements.get(1, 2) * elements.get(2, 3) * elements.get(3, 1)
                    + elements.get(1, 3) * elements.get(2, 1) * elements.get(3, 2)
                    - elements.get(1, 3) * elements.get(2, 2) * elements.get(3, 1);
        } else if (isTriangular()) {
            return diagonalPositions().map(p -> elements.get(p.row(), p.col())).reduce(1.0d, (acc, v) -> acc *= v);
        } else {
            // In numerical analysis, LU decomposition (where 'LU' stands for 'lower upper', and also called LU factorization)
            // factors a matrix as the product of a lower triangular matrix and an upper triangular matrix. The product sometimes
            // includes a permutation matrix as well. The LU decomposition can be viewed as the matrix form of Gaussian elimination.
            // Computers usually solve square systems of linear equations using the LU decomposition, and it is also a key step when
            // inverting a matrix, or computing the determinant of a matrix. The LU decomposition was introduced by mathematician
            // Alan Turing in 1948.

            // This LU decomposition algorithm requires 2n^3/3 operations.




            throw new UnsupportedOperationException("can't compute determinant of a matrix with this size");
        }
    }

    public double at(Position pos) {
        requireNonNull(pos, "pos can't be null");
        return at(pos.row(), pos.col());
    }

    public double at(int row, int col) {
        return elements.get(requireValidRow(row), requireValidColumn(col));
    }

    public void setAt(Position pos, double value) {
        requireNonNull(pos, "pos can't be null");
        setAt(pos.row(), pos.col(), value);
    }

    public void setAt(int row, int col, double value) {
        elements.set(requireValidRow(row), requireValidColumn(col), value);
    }

    public Matrix populate(Supplier<Double> valueSupplier) {
        return transform((p, v) -> valueSupplier.get());
    }

    /**
     *
     * The transpose reflects the elements of a matrix along the diagonal.
     * Properties of the transpose operation:
     * - (X+Y)^T = X^T + Y^T
     * - (XZ)^T = Z^T X^T
     * - (X^T)^T = X
     */
    public Matrix transpose() {
        elements.transpose();
        size = Size.of(size.cols(), size().rows());
        return this;
    }

    public Matrix copy() {
        return new Matrix(elements.copy());
    }

    public Matrix multiply(Matrix other) {
        requireNonNull(other, "other can't be null");
        require(() -> size().cols() == other.size().rows(), "number of columns in first matrix must match number of rows in second matrix");

        Matrix result = new Matrix(this.size().rows(), other.size().cols());
        result.transform((p, v) -> {
            Vector rowVectorA = this.rowVector(p.row());
            Vector colVectorB = other.columnVector(p.col());
            return rowVectorA.innerProduct(colVectorB);
        });
        return result;
    }

    public Matrix multiplyConstant(double constant) {
        transform((p,v) -> v * constant);
        return this;
    }

    public Matrix add(Matrix other) {
        requireNonNull(other, "other can't be null");
        require(() -> size().equals(other.size()), "can't add a matrix of different size");
        transform((p,v) -> v + other.elements.get(p.row(), p.col()));
        return this;
    }

    public Matrix transform(BiFunction<Position, Double, Double> func) {
        requireNonNull(func, "func can't be null");
        rowMajorPositions().forEach(pos -> elements.set(pos.row(), pos.col(), func.apply(pos, elements.get(pos.row(), pos.col()))));
        return this;
    }

    public void forEach(BiConsumer<Position, Double> consumer) {
        requireNonNull(consumer, "consumer can't be null");
        rowMajorPositions().forEach(pos -> consumer.accept(pos, elements.get(pos.row(), pos.col())));
    }

    public boolean anyMatch(BiPredicate<Position, Double> predicate) {
        requireNonNull(predicate, "predicate can't be null");
        return rowMajorPositions().anyMatch(pos -> predicate.test(pos, elements.get(pos.row(), pos.col())));
    }

    public IntStream rowIndices() {
        return IntStream.rangeClosed(1, size.rows()).parallel();
    }

    public IntStream columnIndices() {
        return IntStream.rangeClosed(1, size.cols()).parallel();
    }

    public Stream<Position> rowMajorPositions() {
        return asStream(rowMajorPositionIterator(), true);
    }

    public Stream<Position> columnMajorPositions() {
        return asStream(columnMajorPositionIterator(), true);
    }

    public Stream<Position> diagonalPositions() {
        return asStream(diagonalPositionIterator(), true);
    }

    public Stream<Position> lowerTriangularPositions() {
        return asStream(lowerTriangularPositionIterator(), true);
    }

    public PositionIterator rowMajorPositionIterator() {
        return PositionIterator.rowMajor(size);
    }

    public PositionIterator columnMajorPositionIterator() {
        return PositionIterator.columnMajor(size);
    }

    public PositionIterator diagonalPositionIterator() {
        return PositionIterator.diagonal(size);
    }

    public PositionIterator lowerTriangularPositionIterator() {
        return PositionIterator.lowerTriangle(size);
    }

    public Vector rowVector(int row) {
        return Vector.from(elements.row(requireValidRow(row)));
    }

    public Vector columnVector(int col) {
        return Vector.from(elements.column(requireValidColumn(col)));
    }

    /*
    public Matrix doElementaryelRowOp(ElementaryRowOperation op) {

    }
    */

    public Matrix RowOp_multiplyConstant(int row, double constant) {
        requireValidRow(row);
        require(() -> constant != 0.0d, "constant must be nonzero");
        columnIndices().forEach(j -> elements.set(row, j, constant * elements.get(row, j)));
        return this;
    }

    public Matrix RowOp_swapRows(int rowA, int rowB) {
        requireValidRow(rowA);
        requireValidRow(rowB);
        columnIndices().forEach(j -> {
            double tmp = elements.get(rowA, j);
            elements.set(rowA, j, elements.get(rowB, j));
            elements.set(rowB, j, tmp);
        });
        return this;
    }

    public Matrix RowOp_addMultipleOfOtherRow(int row, double multiple, int otherRow) {
        requireValidRow(row);
        requireValidRow(otherRow);
        require(() -> row != otherRow, "can't add multiple of the same row");
        columnIndices().forEach(j -> elements.set(row, j, elements.get(row, j) + multiple * elements.get(otherRow, j)));
        return this;
    }

    /**
     * Gaussian Elimination Method (GEM).
     * Reduces the matrix to its row echelon form using elementary row operations..
     * Using partial pivoting to reduce numeric instability (accumulated round-off errors due to small pivots).
     * Gaussian elimination with partial pivoting is considered to be one of the most fundamental algorithms
     * in numerical linear algebra, e.g. for solving systems of linear equations.
     */
    public Matrix gaussianElimination() {
        int minDim = Math.min(size.rows(), size.cols());
        for (int k = 1; k <= minDim; k++) {

            // Find the k-th pivot
            int iMax = k;
            double maxAbs = 0.0d;
            for (int i = k; i <= size.rows(); i++) {
                double absValue = Math.abs(elements.get(i, k));
                if (absValue > maxAbs) {
                    maxAbs = absValue;
                    iMax = i;
                }
            }

            if (elements.get(iMax, k) == 0.0d) {
                throw new SingularMatrixException();
            }

            if (k != iMax) {
                RowOp_swapRows(k, iMax);
            }

            // Do for all rows below pivot
            for (int i = k + 1; i <= size.rows(); i++) {
                double multiplier = elements.get(i, k) / elements.get(k, k);

                // Do for all remaining elements in current row
                // The code below is an optimized version of RowOp_addMultipleOfOtherRow(i, -multiplier, k);

                for (int j = k + 1; j <= size.cols(); j++) {
                    elements.set(i, j, elements.get(i, j) - elements.get(k, j) * multiplier);
                }
                // Fill lower triangular matrix with zeros
                elements.set(i, k, 0.0d);
            }
        }
        return this;
    }

    /**
     * Gauss-Jordan Elimination.
     * Reduces the matrix to its reduced row echelon form. This form tells whether the linear system has
     * - A unique solution
     * - No solution
     * - Infinitie solutions
     * Same as Gaussian Elimination + back substitution.
     * Used to find the inverse of an invertible matrix.
     */
    public Matrix gaussJordanElimination() {
        gaussianElimination();

        int minDim = Math.min(size.rows(), size.cols());
        for (int k = 1; k <= minDim; k++) {
            // Make all pivots 1
            if (elements.get(k, k) != 1.0d) {
                // RowOp_multiplyConstant:
                double multiplier = 1 / elements.get(k, k);
                for (int j = k + 1; j <= size.cols(); j++) {
                    elements.set(k, j, elements.get(k , j) * multiplier);
                }
                elements.set(k, k, 1.0d);
            }

        }

        for (int k = minDim; k > 1; k--) {
            // Cancel out (make zero) rest of the column
            for (int i = k - 1; i >= 1; i--) {
                double multiplier = elements.get(i, k);
                for (int j = k + 1; j <= size.cols(); j++) {
                    elements.set(i, j, elements.get(i, j) - multiplier * elements.get(k, j));
                }
                elements.set(i, k, 0.0d);
            }
        }
        return this;
    }

    public Matrix GramSchmidtProcess() {
        return this;
    }

    /**
     * In numerical analysis, LU decomposition (where 'LU' stands for 'lower upper', and also called LU factorization)
     * factors a matrix as the product of a lower triangular matrix and an upper triangular matrix. The product sometimes
     * includes a permutation matrix as well (to avoid numerical instability). The LU decomposition can be viewed as the matrix form of Gaussian elimination.
     * Computers usually solve square systems of linear equations using the LU decomposition, and it is also a key step when
     * inverting a matrix, or computing the determinant of a matrix. The LU decomposition was introduced by mathematician
     * Alan Turing in 1948.
     *
     * This LU decomposition algorithm requires 2n^3/3 operations.
     */
    /*public LUDecompositionResult LUDecomposition() {
        precondition(this::isSquare, "LU decomposition can be performed on a square matrix only");


        return this;
    }*/

    @Override
    public String toString() {
        return toString(NumberFormatter.pretty());
    }

    public String toString(Function<Double, String> formatter) {
        requireNonNull(formatter, "formatter can't be null");
        StringBuilder sb = new StringBuilder();
        rowMajorPositions().forEachOrdered(pos -> {
            sb.append(formatter.apply(elements.get(pos.row(), pos.col())));
            sb.append(pos.col() == size.cols() ? "\n" : " ");
        });
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Matrix)) return false;

        Matrix other = (Matrix) o;

        return size.equals(other.size()) && !anyMatch((p,v) -> v != other.elements.get(p.row(), p.col()));
    }

    private int requireValidRow(int row) {
        require(() -> row >= 1 && row <= size.rows(), "row must be between 1 and %d", size.rows());
        return row;
    }

    private int requireValidColumn(int col) {
        require(() -> col >= 1 && col <= size.cols(), "column must be between 1 and %d", size.cols());
        return col;
    }
}
