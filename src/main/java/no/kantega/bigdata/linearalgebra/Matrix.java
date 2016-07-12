package no.kantega.bigdata.linearalgebra;

import no.kantega.bigdata.linearalgebra.algorithms.LUDecompositionResult;
import no.kantega.bigdata.linearalgebra.buffer.FixedRowMajorMatrixBuffer;
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
    private static final double TINY = 1e-20;

    private Size size;
    private MatrixBuffer elements;

    /**
     * Creates a non-populated matrix of specified size
     *
     * @param rows the number of rows
     * @param cols the number of columns
     * @return a new matrix
     */
    public static Matrix newInstance(int rows, int cols) {
        return new Matrix(rows, cols);
    }

    /**
     * Creates the identity matrix (aka unit matrix) of specified size
     *
     * @param size the number of rows and columns in the matrix
     * @return a new matrix
     */
    public static Matrix identity(int size) {
        require(() -> size > 0, "size must be greater than 0");
        return new Matrix(size, size).transform((p, v) -> p.isOnDiagonal() ? 1.0d : 0.0d);
    }

    /**
     * Creates a matrix of specified size populated with zero values
     *
     * @param rows the number of rows
     * @param cols the number of columns
     * @return a new matrix
     */
    public static Matrix zero(int rows, int cols) {
        return constant(rows, cols, 0.0d);
    }

    /**
     * Creates a matrix with specified size and values
     *
     * @param rows the number of rows
     * @param cols the number of columns
     * @param values the values to populate matrix with
     * @return a new matrix
     */
    public static Matrix fromRowMajorSequence(int rows, int cols, double... values) {
        requireNonNull(values, "values can't be null");
        require(() -> rows * cols == values.length, "values array must contain exactly %d x %d elements", rows, cols);

        return new Matrix(rows, cols).transform((p, v) -> values[p.row() * cols + p.col()]);
    }

    /**
     * Creates a matrix with specified size and sets all elements to specified value
     *
     * @param rows the number of rows
     * @param cols the number of columns
     * @param value the constant value
     * @return a new matrix
     */
    public static Matrix constant(int rows, int cols, double value) {
        return new Matrix(rows, cols).populate(() -> value);
    }

    /**
     * Creates a matrix of specified size and populates with random values within specified range
     *
     * @param rows the number of rows
     * @param cols the number of columns
     * @param minValue the lower bound of element values
     * @param maxValue the upper bound of element values
     * @return a new matrix
     */
    public static Matrix random(int rows, int cols, double minValue, double maxValue) {
        double diff = maxValue - minValue;
        return new Matrix(rows, cols).populate(() -> minValue + Math.random() * diff);
    }

    /**
     * Creates a matrix using specified buffer
     *
     * @param buffer the buffer to consume
     * @return a new matrix
     */
    public static Matrix from(MatrixBuffer buffer) {
        return new Matrix(buffer);
    }

    /**
     * Creates a new matrix of specified size
     *
     * @param rows the number of rows
     * @param cols the number of columns
     */
    private Matrix(int rows, int cols) {
        this.size = Size.of(rows, cols);
        this.elements = FixedRowMajorMatrixBuffer.allocate(rows, cols);
    }

    /**
     * Creates a new matrix consuming specified buffer
     *
     * @param buffer the buffer to consume
     */
    private Matrix(MatrixBuffer buffer) {
        requireNonNull(buffer, "buffer can't be null");
        this.size = buffer.size();
        this.elements = buffer;
    }

    /**
     * Gets the matrix size
     *
     * @return the matrix size
     */
    public Size size() {
        return size;
    }

    /**
     * Gets the matrix rank.
     *
     * The rank of a matrix is the number of non all-zeros rows after reducing to reduced echelon form.
     *
     * The rank of a matrix A is the dimension of the vector space generated (or spanned) by its columns.
     * This is the same as the dimension of the space spanned by its rows. It is a measure of the "nondegenerateness"
     * of the system of linear equations and linear transformation encoded by A. There are multiple equivalent
     * definitions of rank. A matrix's rank is one of its most fundamental characteristics.
     *
     * A square matrix that is nonsingular (determinant != 0) has full-rank, a n x n matrix has rank n.
     *
     * If a matrix A is not full rank, one of the columns is fully explained by the others, in the sense that it is a
     * linear combination of the others. A trivial example is when a column is duplicated. This can also happen if you
     * have 0-1 variables and a column consists of only 0 or only 1. In that case, the rank of the matrix A is less than
     * n and A^TxA has no inverse.
     *
     * @return the matrix rank
     */
    public int rank() {
        // TODO: Reduce matrix to row echelon form +
        // Reduce matrix to reduced row echelon form
        return 0;
    }

    /**
     * Get the matrix trace.
     *
     * The trace of a matrix is the sum of its diagonal elements (a11 + a22 + .. + ann).
     *
     * @return the matrix trace
     */
    public double trace() {
        // TODO
        return 0.0d;
    }

    /**
     * Gets whether this matrix is square, i.e. has the same number of rows and columns
     *
     * @return true if this is a square matrix; else false
     */
    public boolean isSquare() {
        return size().rows() == size().cols();
    }

    /**
     * Gets whether this matrix is an identity, aka unit, matrix.
     * An identity matrix is a square matrix having ones along the diagonal, and zeros in the other positions.
     *
     * @return true if this is an identity matrix; else false
     */
    public boolean isIdentity() {
        return isSquare() && !anyMatch((p,v) -> p.isOnDiagonal() ? v != 1.0d : v != 0.0d);
    }

    /**
     * Gets whether this matrix is a zero matrix.
     * A zero matrix is a matrix with all of its elements equal to zero.
     *
     * @return true if this is a zero matrix; else false
     */
    public boolean isZero() {
        return !anyMatch((p,v) -> v != 0.0d);
    }

    /**
     * Gets whether this matrix is diagonal.
     * A diagonal matrix is a square matrix with all of its elements equal to zero except may be the ones on the diagonal.
     *
     * @return true if this is a diagonal matrix; else false
     */
    public boolean isDiagonal() {
        return isSquare() && !anyMatch((p,v) -> p.isOnDiagonal() ? v == 0.0d : v != 0.0d);
    }

    /**
     * Gets whether this matrix is symmetrical.
     * A symmetrical matrix is identical to its transpose. Thus, only square matrices can be symmetrical.
     *
     * @return true if this matrix is a symmetrical matrix; else false
     */
    public boolean isSymmetrical() {
        return isSquare() && !lowerTriangularPositions().anyMatch((p) -> elements.get(p.row(), p.col()) != elements.get(p.col(), p.row()));
    }

    /**
     * Gets whether this matrix is triangular.
     * A triangular matrix has all zero elements below or above the diagonal. Only square matrices can be triangular.
     *
     * @return true if this matrix is triangular; else false
     */
    public boolean isTriangular() {
        return isUpperTriangular() || isLowerTriangular();
    }

    /**
     * Gets whether this matrix is upper triangular.
     * An upper triangular matrix has all zero elements below the diagonal. Only square matrices can be upper triangular.
     *
     * @return true if this matrix is upper triangular; else false
     */
    public boolean isUpperTriangular() {
        return isSquare() && !anyMatch((p,v) -> p.isBelowDiagonal() && v != 0.0d);
    }

    /**
     * Gets whether this matrix is lower triangular.
     * An upper triangular matrix has all zero elements above the diagonal. Only square matrices can be lower triangular.
     *
     * @return true if this matrix is lower triangular; else false
     */
    public boolean isLowerTriangular() {
        return isSquare() && !anyMatch((p,v) -> p.isAboveDiagonal() && v != 0.0d);
    }

    /**
     * Gets whether this matrix is orthogonal.
     * An orthogonal matrix is a square matrix with elements whose columns and rows are orthogonal unit vectors (i.e., orthonormal vectors).
     * A matrix is orthogonal if the product of the matrix and its transpose equals the identity matrix, that is A x A^T = I
     * This in turn means that its transposed is equal to its inverse, that is A^T = A^-1, and that the matrix is invertible.
     * The determinant of an orthogonal matrix is either +1 or -1.
     *
     * @return true if this matrix is orthogonal; else false
     */
    public boolean isOrthogonal() {
        // Using same buffer for both original matrix and the transposed to save memory
        Matrix transposed = new Matrix(elements.transpose());
        return this.multiply(transposed).isIdentity();
    }

    /**
     * Gets whether this matrix is involutory.
     * An involutory matrix is a matrix that is its own inverse, that is A x A = I.
     * Involutory matrices are all square roots of the identity matrix.
     *
     * @return true if this matrix is involutory; else false
     */
    public boolean isInvolutory() {
        return multiply(this).isIdentity();
    }

    /**
     * Gets whether this matrix is invertible.
     * A matrix is invertible if it is square and its determinant is non-zero.
     *
     * A square matrix that is not invertible is called singular or degenerate.
     * When a matrix representing a system of linear equations is singular it means it
     * does not have a single solution. It either has no solutions at all, or infinitely number
     * of solutions.
     *
     * A square matrix is singular if and only if its determinant is 0.
     *
     * @return true if this matrix is invertible; else false
     */
    public boolean isInvertable() {
        return isSquare() && determinant() != 0.0d;
    }

    /**
     * Gets the inverse of this matrix, if it is square and nonsingular (invertible).
     *
     * Properties of the inverse matrix include:
     *
     * 1) A^-1 multiplied with A, gives identity matrix I
     * 2) (A^−1)^−1 = A
     * 3) (kA)^−1 = k^−1A^−1 for nonzero scalar k
     * 4) (A^T)^−1 = (A^−1)^T
     * 5) det(A^−1) = det(A)^−1
     *
     * @return the inverse of this matrix
     * @throws IllegalStateException when matrix is non-square
     * @throws SingularMatrixException when matrix is singular
     */
    public Matrix invert() {
        precondition(this::isSquare, "The inverse can be computed on a square matrix only");

        if (size.rows() == 2 && size.cols() == 2) {

            // Original matrix elements denoted as follows:
            // a b
            // c d

            double determinant = elements.get(0, 0) * elements.get(1, 1) - elements.get(0, 1) * elements.get(1, 0); // ad - bc

            if (determinant == 0.0d) {
                throw new SingularMatrixException();
            }

            Matrix inv = new Matrix(size.rows(), size.cols());
            inv.elements.set(0, 0, elements.get(1, 1) / determinant);
            inv.elements.set(0, 1, -elements.get(0, 1) / determinant);
            inv.elements.set(1, 0, -elements.get(1, 0) / determinant);
            inv.elements.set(1, 1, elements.get(0, 0) / determinant);
            return inv;
        } else if (size.rows() == 3 && size.cols() == 3) {

            // Original matrix elements denoted as follows:
            // a b c
            // d e f
            // g h i

            double A = (elements.get(1,1)*elements.get(2,2) - elements.get(1,2)*elements.get(2,1));  // ei - fh
            double B = -(elements.get(1,0)*elements.get(2,2) - elements.get(1,2)*elements.get(2,0)); // -(di - fg)
            double C = (elements.get(1,0)*elements.get(2,1) - elements.get(1,1)*elements.get(2,0));  // dh - eg
            double D = -(elements.get(0,1)*elements.get(2,2) - elements.get(0,2)*elements.get(2,1)); // -(bi - ch)
            double E = (elements.get(0,0)*elements.get(2,2) - elements.get(0,2)*elements.get(2,0));  // ai - cg
            double F = -(elements.get(0,0)*elements.get(2,1) - elements.get(0,1)*elements.get(2,0)); // -(ah - bg)
            double G = (elements.get(0,1)*elements.get(1,2) - elements.get(0,2)*elements.get(1,1));  // bf - ce
            double H = -(elements.get(0,0)*elements.get(1,2) - elements.get(0,2)*elements.get(1,0)); // -(af - cd)
            double I = (elements.get(0,0)*elements.get(1,1) - elements.get(0,1)*elements.get(1,0));  // ae - bd

            double determinant = elements.get(0,0)*A + elements.get(0,1)*B + elements.get(0,2)*C; // aA + bB + cC

            if (determinant == 0.0d) {
                throw new SingularMatrixException();
            }

            Matrix inv = new Matrix(size.rows(), size.cols());
            inv.elements.set(0,0,A / determinant);
            inv.elements.set(1,0,B / determinant);
            inv.elements.set(2,0,C / determinant);
            inv.elements.set(0,1,D / determinant);
            inv.elements.set(1,1,E / determinant);
            inv.elements.set(2,1,F / determinant);
            inv.elements.set(0,2,G / determinant);
            inv.elements.set(1,2,H / determinant);
            inv.elements.set(2,2,I / determinant);
            return inv;
        } else {
            return calcLuDecomposition().inverse();
        }
    }

    /**
     * Gets the determinant of this matrix.
     *
     * Determinants are mainly used as a theoretical tool. They are rarely calculated explicitly in numerical linear algebra,
     * where for applications like checking invertibility and finding eigenvalues the determinant has largely been supplanted
     * by other techniques. Nonetheless, explicitly calculating determinants is required in some situations.
     *
     * Naive methods of implementing an algorithm to compute the determinant include using the Leibniz formula or Laplace's formula.
     * Both these approaches are extremely inefficient for large matrices, requiring close to n! operations. A more efficient method
     * is using the LU decomposition, the QR decomposition and others.
     *
     * Basic properties of the determinant are:
     *
     * 1) det(I) = 1
     * 2) det(A^T) = det(A)
     * 3) det(A^-1) = det(A)^-1
     * 4) det(AB) = det(A)det(B)
     *
     * Other important properties of the determinant include the following, which are invariant under elementary row
     * and column operations:
     *
     * 1) Switching two rows or columns changes the sign.
     * 2) Scalars can be factored out from rows and columns.
     * 3) Multiples of rows and columns can be added together without changing the determinant's value.
     * 4) Scalar multiplication of a row by a constant c multiplies the determinant by c.
     * 5) A determinant with a row or column of zeros has value 0.
     * 6) Any determinant with two rows or columns equal has value 0.
     *
     * The determinant of a matrix will be zero if
     *
     * i) An entire row is zero.
     * ii) Two rows or columns are equal.
     * iii) A row or column is a constant multiple of another row or column (linear dependent).
     *
     * A matrix is invertible (non-singular) if and only if the determinant is not zero. So, if the determinant is zero,
     * the matrix is singular and does not have an inverse.
     *
     * @return the determinant of this matrix
     * @throws IllegalStateException if matrix is non-square
     */
    public double determinant() {
        precondition(this::isSquare, "The determinant can be computed on a square matrix only");

        if (size.rows() == 2 && size.cols() == 2) {
            // ad - bc
            return elements.get(0, 0) * elements.get(1, 1)
                 - elements.get(0, 1) * elements.get(1, 0);
        } else if (size.rows() == 3 && size.cols() == 3) {
            // aei - afh - bdi + bfg + cdh -ceg
            return elements.get(0, 0) * elements.get(1, 1) * elements.get(2, 2)
                 - elements.get(0, 0) * elements.get(1, 2) * elements.get(2, 1)
                 - elements.get(0, 1) * elements.get(1, 0) * elements.get(2, 2)
                 + elements.get(0, 1) * elements.get(1, 2) * elements.get(2, 0)
                 + elements.get(0, 2) * elements.get(1, 0) * elements.get(2, 1)
                 - elements.get(0, 2) * elements.get(1, 1) * elements.get(2, 0);
        } else if (isTriangular()) {
            return diagonalPositions().map(p -> elements.get(p.row(), p.col())).reduce(1.0d, (acc, v) -> acc *= v);
        } else {
            return calcLuDecomposition().determinant();
        }
    }

    /**
     * Gets the matrix element at specified position
     *
     * @param pos the element position
     * @return the matrix element
     */
    public double at(Position pos) {
        requireNonNull(pos, "pos can't be null");
        return elements.get(pos.row(), pos.col());
    }

    /**
     * Gets the matrix element at specified position
     *
     * @param row the row position (1-based)
     * @param col the column position (1-based)
     * @return the matrix element
     */
    public double at(int row, int col) {
        return elements.get(requireValidRow(row)-1, requireValidColumn(col)-1);
    }

    /**
     * Sets the matrix element at specified position
     *
     * @param pos the element position
     * @param value the element value
     */
    public void setAt(Position pos, double value) {
        requireNonNull(pos, "pos can't be null");
        elements.set(pos.row(), pos.col(), value);
    }

    /**
     * Sets the matrix element at specified position
     *
     * @param row the row position (1-based)
     * @param col the column position (1-based)
     * @param value the element value
     */
    public void setAt(int row, int col, double value) {
        requireValidRow(row);
        requireValidColumn(col);
        elements.set(row-1, col-1, value);
    }

    /**
     * Populates the matrix with element values from specified supplier
     *
     * @param valueSupplier the element value supplier
     * @return the populated matrix
     */
    public Matrix populate(Supplier<Double> valueSupplier) {
        return transform((p, v) -> valueSupplier.get());
    }

    /**
     * Transposes the matrix.
     *
     * The transpose reflects the elements of a matrix along the diagonal.
     * Properties of the transpose operation:
     *
     * 1) (X+Y)^T = X^T + Y^T
     * 2) (XZ)^T = Z^T X^T
     * 3) (X^T)^T = X
     *
     * @return the transposed matrix
     */
    public Matrix transpose() {
        elements = elements.transpose();
        size = Size.of(size.cols(), size().rows());
        return this;
    }

    /**
     * Creates a copy of this matrix
     *
     * @return a copy of this matrix
     */
    public Matrix copy() {
        return new Matrix(elements.copy());
    }

    /**
     * Calculates the matrix product of this and the specified matrix.
     *
     * One can form many definitions of matrix multiplication. However, the most useful definition can be motivated by
     * linear equations and linear transformations on vectors, which have numerous applications in applied mathematics,
     * physics, and engineering. This definition is often called the matrix product. In words, if A is an n × m matrix
     * and B is an m × p matrix, their matrix product AB is an n × p matrix, in which the m entries across the rows of
     * A are multiplied with the m entries down the columns of B.
     *
     * This method performs matrix multiplication using the naïve O(n^3) algorithm. For square matrices whose sizes are powers of two,
     * the Strassen algorithm is much faster (O(n^2.8)) than the naïve algorithm. For other cases, it is significantly
     * slower.
     *
     * @param other the matrix to be multiplied with
     * @return the matrix product
     */
    public Matrix multiply(Matrix other) {
        requireNonNull(other, "other can't be null");
        require(() -> size().cols() == other.size().rows(), "number of columns in first matrix must match number of rows in second matrix");

        Matrix result = new Matrix(this.size().rows(), other.size().cols());
        result.transform((p, v) -> {
            Vector rowVectorA = this.rowVector(p.row()+1);
            Vector colVectorB = other.columnVector(p.col()+1);
            return rowVectorA.innerProduct(colVectorB);
        });

        // The implementation below is 30% slower than the above
        /*for (int i=0; i<size.rows(); ++i) {
            for (int j=0; j<other.size().cols(); ++j) {
                double innerProduct = 0.0d;
                for (int k=0; k<size.cols(); ++k) {
                    innerProduct += elements.get(i, k) * other.elements.get(k, j);
                }
                result.elements.set(i, j, innerProduct);
            }
        }*/

        return result;
    }

    /**
     * Multiplies with specified scalar value
     *
     * @param scalar the scalar to multiply with
     * @return this matrix after multiplication
     */
    public Matrix multiplyScalar(double scalar) {
        transform((p,v) -> v * scalar);
        return this;
    }

    /**
     * Divides by specified scalar value
     *
     * @param scalar the scalar to divide by
     * @return this matrix after division
     */
    public Matrix divideScalar(double scalar) {
        transform((p,v) -> v / scalar);
        return this;
    }

    /**
     * Adds specified matrix to this matrix. The matrices must have the same size.
     *
     * @param other the matrix to add
     * @return this matrix after addition
     */
    public Matrix add(Matrix other) {
        requireNonNull(other, "other can't be null");
        require(() -> size().equals(other.size()), "can't add a matrix of different size");
        transform((p,v) -> v + other.elements.get(p.row(), p.col()));
        return this;
    }

    /**
     * Subtracts specified matrix from this matrix. The matrices must have the same size.
     *
     * @param other the matrix to subtract
     * @return this matrix after subtraction
     */
    public Matrix subtract(Matrix other) {
        requireNonNull(other, "other can't be null");
        require(() -> size().equals(other.size()), "can't subtract a matrix of different size");
        transform((p,v) -> v - other.elements.get(p.row(), p.col()));
        return this;
    }

    /**
     * Modifies the element values using the specfied bi-function
     *
     * @param func the function transforming element values
     * @return this matrix after transformation
     */
    public Matrix transform(BiFunction<Position, Double, Double> func) {
        requireNonNull(func, "func can't be null");
        rowMajorPositions().forEach(pos -> elements.set(pos.row(), pos.col(), func.apply(pos, elements.get(pos.row(), pos.col()))));
        return this;
    }

    /**
     * Iterates over the element values, invoking specified consumer
     *
     * @param consumer the consumer to invoke on each element value
     */
    public void forEach(BiConsumer<Position, Double> consumer) {
        requireNonNull(consumer, "consumer can't be null");
        rowMajorPositions().forEach(pos -> consumer.accept(pos, elements.get(pos.row(), pos.col())));
    }

    /**
     * Tests whether specified predicate holds for any element value
     *
     * @param predicate the predicate to test
     * @return true if predicate holds for at least one element value
     */
    public boolean anyMatch(BiPredicate<Position, Double> predicate) {
        requireNonNull(predicate, "predicate can't be null");
        return rowMajorPositions().anyMatch(pos -> predicate.test(pos, elements.get(pos.row(), pos.col())));
    }

    private IntStream rowIndices() {
        return IntStream.rangeClosed(0, size.rows()-1).parallel();
    }

    private IntStream columnIndices() {
        return IntStream.rangeClosed(0, size.cols()-1).parallel();
    }

    /**
     * Gets a stream of element positions in row major order
     *
     * @return a stream of positions
     */
    public Stream<Position> rowMajorPositions() {
        return asStream(rowMajorPositionIterator(), true);
    }

    /**
     * Gets a stream of element positions in column major order
     *
     * @return a stream of positions
     */
    public Stream<Position> columnMajorPositions() {
        return asStream(columnMajorPositionIterator(), true);
    }

    /**
     * Gets a stream of element positions along the diagonal
     *
     * @return a stream of diagonal positions
     */
    public Stream<Position> diagonalPositions() {
        return asStream(diagonalPositionIterator(), true);
    }

    /**
     * Gets a stream of lower triangular element positions
     *
     * @return a stream of lower triangular positions
     */
    public Stream<Position> lowerTriangularPositions() {
        return asStream(lowerTriangularPositionIterator(), true);
    }

    /**
     * Gets a position iterator running in row major order
     *
     * @return a row major position iterator
     */
    public PositionIterator rowMajorPositionIterator() {
        return PositionIterator.rowMajor(size);
    }

    /**
     * Gets a position iterator running in column major order
     *
     * @return a column major position iterator
     */
    public PositionIterator columnMajorPositionIterator() {
        return PositionIterator.columnMajor(size);
    }

    /**
     * Gets a position iterator running through diagonal elements
     *
     * @return a diagonal position iterator
     */
    public PositionIterator diagonalPositionIterator() {
        return PositionIterator.diagonal(size);
    }

    /**
     * Gets a position iterator running through lower triangular elements
     *
     * @return a lower triangular position iterator
     */
    public PositionIterator lowerTriangularPositionIterator() {
        return PositionIterator.lowerTriangle(size);
    }

    /**
     * Gets the specified row vector.
     * Modifications of the vector are reflected in the matrix.
     *
     * @param row the row (1-based)
     * @return the specified row vector
     */
    public Vector rowVector(int row) {
        return Vector.from(elements.row(requireValidRow(row)-1));
    }

    /**
     * Gets the specified column vector.
     * Modifications of the vector are reflected in the matrix.
     *
     * @param col the column (1-based)
     * @return the specified column vector
     */
    public Vector columnVector(int col) {
        return Vector.from(elements.column(requireValidColumn(col)-1));
    }

    /**
     * Performs the elementary row operation <strong>Row Multiplication</strong>,
     * i.e. multiplying a row with a non-zero constant.
     *
     * To apply a set of this kind of row operation, one might create a scaling matrix S and multiply
     * with the matrix.
     *
     * @param row the row number (0-based)
     * @param constant the constant to multiply with
     * @return this matrix after row operation
     */
    Matrix rowOp_multiplyConstant(int row, double constant) {
        require(() -> constant != 0.0d, "constant must be non-zero");
        columnIndices().forEach(j -> elements.set(row, j, constant * elements.get(row, j)));
        return this;
    }

    /**
     * Performs the elementary row operation <strong>Row Switching</strong>,
     * i.e. switching a row within the matrix with another row.
     *
     * To apply a set of this kind of row operation, one might create a permutation matrix P and multiply
     * with the matrix.
     *
     * @param rowA the first row number (0-based)
     * @param rowB the second row number (0-based)
     * @return this matrix after row operation
     */
    Matrix rowOp_swapRows(int rowA, int rowB) {
        columnIndices().forEach(j -> {
            double tmp = elements.get(rowA, j);
            elements.set(rowA, j, elements.get(rowB, j));
            elements.set(rowB, j, tmp);
        });
        return this;
    }

    /**
     * Performs the elementary row operation <strong>Row Addition</strong>,
     * i.e. replacing a row by the sum of that row and a multiple of another row.
     *
     * To apply a set of this kind of row operation, one might create an elimination matrix M
     * and multiply with the matrix.
     *
     * @param row the row number (0-based)
     * @param multiple the multiplication constant
     * @param otherRow the row number (0-based) of other row
     * @return this matrix after row operation
     */
    Matrix rowOp_addMultipleOfOtherRow(int row, double multiple, int otherRow) {
        require(() -> row != otherRow, "can't add multiple of the same row");
        columnIndices().forEach(j -> elements.set(row, j, elements.get(row, j) + multiple * elements.get(otherRow, j)));
        return this;
    }

    /**
     * Performs the <strong>Gaussian Elimination Method</strong> (GEM).
     *
     * Reduces the matrix to its row echelon form using elementary row operations.
     * Using partial pivoting to reduce numeric instability (accumulated round-off errors due to small pivots).
     * Gaussian elimination with partial pivoting is considered to be one of the most fundamental algorithms
     * in numerical linear algebra, e.g. for solving systems of linear equations.
     *
     * @return this matrix after gaussian elimination
     * @throws SingularMatrixException when matrix is singular
     */
    public Matrix gaussianElimination() {
        int minDim = Math.min(size.rows(), size.cols());
        for (int k = 0; k < minDim; k++) {

            // Find the pivot for the k'th column
            int iMax = partialPivotRow(k);

            if (k != iMax) {
                rowOp_swapRows(k, iMax);
            }

            // Do for all rows below pivot
            for (int i = k + 1; i < size.rows(); i++) {
                double multiplier = elements.get(i, k) / elements.get(k, k);

                // Do for all remaining elements in current row
                // The code below is an optimized version of rowOp_addMultipleOfOtherRow(i, -multiplier, k);

                for (int j = k + 1; j < size.cols(); j++) {
                    elements.set(i, j, elements.get(i, j) - elements.get(k, j) * multiplier);
                }
                // Fill lower triangular matrix with zeros
                elements.set(i, k, 0.0d);
            }
        }
        return this;
    }

    /**
     * Gets the k'th pivot row for partial pivoting.
     *
     * The pivot or pivot element is the element of a matrix, or an array, which is selected first by an algorithm (e.g. Gaussian elimination,
     * simplex algorithm, etc.), to do certain calculations. In the case of matrix algorithms, a pivot entry is usually required to be at least
     * distinct from zero, and often distant from it; in this case finding this element is called pivoting. Pivoting may be followed by an interchange
     * of rows or columns to bring the pivot to a fixed position and allow the algorithm to proceed successfully, and possibly to reduce round-off
     * error. It is often used for verifying row echelon form.
     *
     * Pivoting might be thought of as swapping or sorting rows or columns in a matrix, and thus it can be represented as multiplication by
     * permutation matrices. However, algorithms rarely move the matrix elements because this would cost too much time; instead, they just keep
     * track of the permutations.
     *
     * Overall, pivoting adds more operations to the computational cost of an algorithm. These additional operations are sometimes necessary for the
     * algorithm to work at all. Other times these additional operations are worthwhile because they add numerical stability to the final result.
     *
     * This method finds the row with the largest absolute value looking from the diagonal downwards at specified column. Used in partial pivoting.
     *
     * @param k the diagonal element (i.e. column) to find pivot row for (0-based)
     * @return the row (0-based) holding the pivot element. The return value is always >= k.
     * @throws SingularMatrixException when the largest absolute value is close to zero, indicating a singular matrix.
     */
    private int partialPivotRow(int k) {
        int iMax = k;
        double maxAbs = Math.abs(elements.get(k, k));

        for (int i = k; i < size.rows(); i++) {
            double absValue = Math.abs(elements.get(i, k));
            if (absValue > maxAbs) {
                maxAbs = absValue;
                iMax = i;
            }
        }

        if (maxAbs <= TINY) {
            throw new SingularMatrixException();
        }

        return iMax;
    }

    /**
     * Performs <strong>Gauss-Jordan Elimination</strong> on the matrix.
     *
     * Reduces the matrix to its reduced row echelon form. This form tells whether the linear system has
     *
     *   i) A unique solution
     *  ii) No solution
     * iii) Infinitie solutions
     *
     * Same as Gaussian Elimination, succeeded by back substitution.
     * Often used to find the inverse of an invertible matrix.
     *
     * @return this matrix after gauss jordan elimination
     * @throws SingularMatrixException when matrix is singular
     */
    public Matrix gaussJordanElimination() {
        gaussianElimination();

        int minDim = Math.min(size.rows(), size.cols());
        for (int k = 0; k < minDim; k++) {
            // Make all pivots 1
            if (elements.get(k, k) != 1.0d) {
                // rowOp_multiplyConstant:
                double divisor = elements.get(k, k);
                for (int j = k + 1; j < size.cols(); j++) {
                    elements.set(k, j, elements.get(k , j) / divisor);
                }
                elements.set(k, k, 1.0d);
            }
        }

        for (int k = minDim-1; k > 0; k--) {
            // Cancel out (make zero) rest of the column
            for (int i = k - 1; i >= 0; i--) {
                double multiplier = elements.get(i, k);
                for (int j = k + 1; j < size.cols(); j++) {
                    elements.set(i, j, elements.get(i, j) - multiplier * elements.get(k, j));
                }
                elements.set(i, k, 0.0d);
            }
        }
        return this;
    }

    // TODO
    public Matrix gramSchmidtProcess() {
        return this;
    }

    /**
     * Performs LU decomposition of the matrix.
     *
     * In numerical analysis, LU decomposition (where 'LU' stands for 'lower upper', and also called LU factorization)
     * factors a matrix as the product of a lower triangular matrix and an upper triangular matrix. The product sometimes
     * includes a permutation matrix as well (to avoid numerical instability). The LU decomposition can be viewed as the matrix
     * form of Gaussian elimination. Computers usually solve square systems of linear equations using the LU decomposition, and
     * it is also a key step when inverting a matrix, or computing the determinant of a matrix. The LU decomposition was
     * introduced by mathematician Alan Turing in 1948.
     *
     * Using the Doolittle algorithm with partial pivoting, producing unit diagonals for the lower triangle. The algorithm below,
     * however does not store these 1's, since both the L and U matrices are stored within the same physical matrix. The diagonal
     * of the physical matrix belongs to the U matrix.
     *
     * This LU decomposition algorithm requires 2n^3/3 operations for a n x n matrix.
     *
     * @return the result of the LU decomposition
     * @throws SingularMatrixException when matrix is singular
     */
    public LUDecompositionResult calcLuDecomposition() {
        precondition(this::isSquare, "LU decomposition can be performed on a square matrix only");

        int d = size.rows();

        Matrix LU = copy();

        int[] pi = new int[d];
        for (int i = 0; i < d; i++) {
            pi[i] = i;
        }

        int k0;
        double signOfDeterminant = 1.0d;

        for (int k=0; k<d; ++k) {

            // find the row with the biggest pivot
            k0 = LU.partialPivotRow(k);

            if (k != k0) {
                // switch two rows in permutation matrix
                int p = pi[k];
                pi[k] = pi[k0];
                pi[k0] = p;

                LU.rowOp_swapRows(k, k0);

                // switching rows means the determinant changes sign
                signOfDeterminant *= -1;
            }

            for (int j=k; j<d; ++j) {
                double sum = 0.0d;
                for (int p=0; p<k; ++p) {
                    sum += LU.elements.get(k, p) * LU.elements.get(p, j);
                }
                LU.elements.set(k, j, LU.elements.get(k, j) - sum); // not dividing by diagonals
            }

            for (int i=k+1; i<d; ++i) {
                double sum=0.0d;
                for (int p=0; p<k; ++p) {
                    sum += LU.elements.get(i, p) * LU.elements.get(p, k);
                }
                LU.elements.set(i, k, (LU.elements.get(i, k) - sum) / LU.elements.get(k, k));
            }
        }

        return new LUDecompositionResult(LU, pi, signOfDeterminant);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toString(NumberFormatter.pretty());
    }

    /**
     * Formats the element values into a string using the specified value formatter
     *
     * @param formatter the element value formatter
     * @return a string representation of this matrix
     */
    public String toString(Function<Double, String> formatter) {
        requireNonNull(formatter, "formatter can't be null");
        StringBuilder sb = new StringBuilder();
        rowMajorPositions().forEachOrdered(pos -> {
            sb.append(formatter.apply(elements.get(pos.row(), pos.col())));
            sb.append(pos.isLastColumn() ? "\n" : " ");
        });
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
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
