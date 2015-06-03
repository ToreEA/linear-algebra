// This software is produced by Statens vegvesen. Unauthorized redistribution,
// reproduction or usage of this software in whole or in part without the
// express written consent of Statens vegvesen is strictly prohibited.
// Copyright © 2015 Statens vegvesen
// ALL RIGHTS RESERVED
package no.kantega.bigdata.matrix;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static no.kantega.bigdata.matrix.utils.Argument.require;
import static no.kantega.bigdata.matrix.utils.StreamUtils.asStream;

/**
 * TODO: Purpose and responsibility
 *
 * @author Tore Eide Andersen (Kantega AS)
 */
public class Matrix {
    private Size size;
    private final Table elements;

    public static Matrix identity(int rows, int cols) {
        require(() -> rows == cols, "The identity matrix should be a square matrix");
        return new Matrix(rows, cols).transform((p, v) -> p.isOnDiagonal() ? 1.0d : 0.0d);
    }

    public static Matrix zero(int rows, int cols) {
        return constant(rows, cols, 0.0d);
    }

    public static Matrix fromRowMajorSequence(int rows, int cols, double... values) {
        requireNonNull(values, "values can't be null");
        require(() -> rows * cols == values.length, "values array must contain exactly " + rows + " x " + cols + " elements");

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
        this.elements = FixedTable.ofSize(rows,cols);
    }

    private Matrix(Table elements) {
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

    public boolean isDiagonal() {
        return isSquare() && !anyMatch((p,v) -> p.isOnDiagonal() ? v == 0.0d : v != 0.0d);
    }

    public boolean isUpperTriangular() {
        return isSquare() && !anyMatch((p,v) -> p.isBelowDiagonal() ? v == 0.0d : v != 0.0d);
    }

    public boolean isLowerTriangular() {
        return isSquare() && !anyMatch((p,v) -> p.isAboveDiagonal() ? v == 0.0d : v != 0.0d);
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
        return determinant() != 0.0d;
    }

    // 3x3
    public Matrix invert() {
        return null;
    }

    // 3x3
    public double determinant() {
        return 1.0d;
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

    public Matrix transform(BiFunction<Position, Double, Double> func) {
        requireNonNull(func, "func can't be null");
        rowWisePositions().forEach(pos -> elements.set(pos.row(), pos.col(), func.apply(pos, elements.get(pos.row(), pos.col()))));
        return this;
    }

    public void forEach(BiConsumer<Position, Double> consumer) {
        requireNonNull(consumer, "consumer can't be null");
        rowWisePositions().forEach(pos -> consumer.accept(pos, elements.get(pos.row(), pos.col())));
    }

    public boolean anyMatch(BiPredicate<Position, Double> predicate) {
        requireNonNull(predicate, "predicate can't be null");
        return rowWisePositions().anyMatch(pos -> predicate.test(pos, elements.get(pos.row(), pos.col())));
    }

    public IntStream rowIndices() {
        return IntStream.rangeClosed(1, size.rows()).parallel();
    }

    public IntStream columnIndices() {
        return IntStream.rangeClosed(1, size.cols()).parallel();
    }

    public Stream<Position> rowWisePositions() {
        return asStream(rowMajorPositionIterator(), true);
    }

    public Stream<Position> columnMajorPositions() {
        return asStream(columnMajorPositionIterator(), true);
    }

    public PositionIterator rowMajorPositionIterator() {
        return PositionIterator.rowMajor(size);
    }

    public PositionIterator columnMajorPositionIterator() {
        return PositionIterator.columnMajor(size);
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
     * Using partial pivoting to reduce numeric instability (accumulated round-off errors)
     */
    public Matrix gaussianElimination() {
        int minDim = Math.min(size.rows(), size.cols());
        for (int k = 1; k <= minDim; k++) {

            // Find the k-th pivot:
            int iMax = k;
            double maxAbs = 0.0d;
            for (int i = k; i <= size.rows(); i++) {
                double absValue = Math.abs(elements.get(i, k));
                if (absValue >= maxAbs) {
                    maxAbs = absValue;
                    iMax = i;
                }
            }

            if (elements.get(iMax, k) == 0.0d) {
                throw new IllegalStateException("Matrix is singular!");
            }

            RowOp_swapRows(k, iMax);

            // Do for all rows below pivot:
            for (int i = k + 1; i <= size.rows(); i++) {
                double multiplier = elements.get(i, k) / elements.get(k, k);

                // Do for all remaining elements in current row:
                for (int j = k + 1; j <= size.cols(); j++) {
                    elements.set(i, j, elements.get(i, j) - elements.get(k, j) * multiplier);
                }
                // Fill lower triangular matrix with zeros:
                elements.set(i, k, 0.0d);
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return toString(e -> String.format("%8.1f", e));
    }

    public String toString(Function<Double, String> formatter) {
        requireNonNull(formatter, "formatter can't be null");
        StringBuilder sb = new StringBuilder();
        PositionIterator it = rowMajorPositionIterator();
        while (it.hasNext()) {
            Position pos = it.next();
            sb.append(formatter.apply(elements.get(pos.row(), pos.col())));
            sb.append(pos.col() == size.cols() ? "\n" : " ");
        }
        return sb.toString();
    }

    private int requireValidRow(int row) {
        require(() -> row >= 1 && row <= size.rows(), String.format("row must be between 1 and %d", size.rows()));
        return row;
    }

    private int requireValidColumn(int col) {
        require(() -> col >= 1 && col <= size.cols(), String.format("column must be between 1 and %d", size.cols()));
        return col;
    }
}
